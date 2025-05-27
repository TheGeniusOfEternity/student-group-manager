package handlers

import core.State
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import commands.ServerCmd
import dto.CommandInfoDto
import dto.CommandParam
import dto.ExecuteCommandDto
import invoker.Invoker
import serializers.JsonSerializer
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

object ConnectionHandler {
    private const val DATA_REQUESTS = "data-requests"
    private const val DATA_RESPONSES = "data-responses"
    private val responseHandlers = mutableMapOf<String, (Delivery) -> Unit>()
    private var factory: ConnectionFactory = ConnectionFactory()
    private var currentConnection: Connection? = null
    private var publishChannel = currentConnection?.createChannel()
    private var consumeChannel = currentConnection?.createChannel()
    private var heartLatch = CountDownLatch(1)

    private fun registerResponseHandler(key: String, handler: (Delivery) -> Unit) {
        responseHandlers[key] = handler
    }

    private fun startResponseConsumer() {
        registerResponseHandler("cmd") { delivery ->
            val response = checkJwt(delivery)
            if (response != null) IOHandler.responsesThreads.add(response)
        }
        try {
            if (consumeChannel != null) {
                consumeChannel!!.queueDeclare(DATA_RESPONSES, false, false, false, null)
                val deliverCallback = DeliverCallback { _, delivery ->

                    val correlationId = delivery.properties.correlationId ?: ""
                    val clientId = delivery.properties.appId

                    if (State.appName == clientId) {
                        val handler = responseHandlers[correlationId]
                        if (handler != null) {
                            try {
                                handler(delivery)
                            } catch (e: Exception) {
                                println("Error in handler for $correlationId: ${e.printStackTrace()}")
                                IOHandler printInfoLn JsonSerializer.deserialize<String>(delivery.body)
                            } finally {
                                consumeChannel!!.basicAck(delivery.envelope.deliveryTag, false)
                                if (correlationId != "cmd") responseHandlers.remove(correlationId)
                                if (State.tasks > 1) State.tasks--
                            }
                        } else {
                            consumeChannel!!.basicAck(delivery.envelope.deliveryTag, false)
                            responseHandlers.remove(correlationId)
                            println("No handler registered for correlationId: $correlationId")
                        }
                    } else consumeChannel!!.basicNack(delivery.envelope.deliveryTag, false, true)
                }
                consumeChannel!!.basicConsume(DATA_RESPONSES, false, deliverCallback)  { _: String? -> }
            } else handleConnectionFail()

        } catch (e: Exception) {
            handleConnectionFail()
        }
    }

    fun initializeConnection() {
        heartLatch = CountDownLatch(1)
        factory.apply {
            this.host = State.host
            this.username = State.credentials["RABBITMQ_USERNAME"] ?: error("RABBITMQ_USERNAME not set")
            this.password = State.credentials["RABBITMQ_PASSWORD"] ?: error("RABBITMQ_PASSWORD not set")
        }
        factory.connectionTimeout = 1000
        factory.requestedHeartbeat = 2

        try {
            val socket = Socket()
            val socketAddress = InetSocketAddress(State.host, 1234)
            socket.connect(socketAddress, 1000)

            currentConnection = factory.newConnection("client")
            publishChannel = currentConnection?.createChannel()
            consumeChannel = currentConnection?.createChannel()

            IOHandler printInfoLn "Connected to RabbitMQ established ${State.host}"

            startHeartbeat(socket, heartbeatTimeoutMs = 400)
            startResponseConsumer()

            val gotBeat = heartLatch.await(2000, TimeUnit.MILLISECONDS)

            if (gotBeat) authorize()
            else handleConnectionFail()
        } catch (e: TimeoutException) {
            handleConnectionFail("RabbitMQ is probably offline, try to reconnect? (Y/n): ")
        } catch (e: Exception) {
            handleConnectionFail()
        }
    }

    private fun startHeartbeat(
        socket: Socket,
        heartbeatIntervalMs: Long = 500,
        heartbeatTimeoutMs: Int = 1000
    ) {
        val output = DataOutputStream(socket.getOutputStream())
        val input = DataInputStream(socket.getInputStream())
        socket.soTimeout = heartbeatTimeoutMs

        thread(start = true) {
            try {
                while (State.isRunning) {
                    output.writeUTF("PING")
                    output.flush()

                    try {
                        when (input.readUTF()) {
                            "PONG" -> {
                                State.connectedToServer = true
                                if (heartLatch.count > 0) heartLatch.countDown()
                            }
                            else -> {
                                IOHandler.printInfoLn("Protocol mismatch")
                                break
                            }
                        }
                    } catch (_: SocketTimeoutException) {
                        State.connectedToServer = false
                        break
                    }
                    Thread.sleep(heartbeatIntervalMs)
                }
            } catch (_: IOException) {
                State.connectedToServer = false
            } finally {
                try {
                    socket.close()
                } catch (_: IOException) {}
            }
        }
    }


    private fun authorize() {
        if (consumeChannel != null) {
            if (State.credentials["TEMP_USERNAME"] == null ||
                State.credentials["TEMP_PASSWORD"] == null) {
                IOHandler.getAuthCredentials()
                return
            }
            val byteData = JsonSerializer.serialize(
                ExecuteCommandDto("authorize", CommandParam.StringParam(
                    "${State.credentials["TEMP_USERNAME"]}:${State.credentials["TEMP_PASSWORD"]}",
                ))
            )
            val correlationId = UUID.randomUUID().toString()
            val latch = CountDownLatch(1)
            registerResponseHandler(correlationId) { delivery ->
                latch.countDown()
                val response = JsonSerializer.deserialize<String>(delivery.body)
                if (!response.contains("authorize")) {
                    IOHandler printInfoLn "Authentication successful"
                    State.credentials["ACCESS_TOKEN"] = response.split("#&#")[0]
                    State.credentials["REFRESH_TOKEN"] = response.split("#&#")[1]
                    State.isAuthenticated = true
                    IOHandler printInfoLn "Welcome back, '${State.credentials["TEMP_USERNAME"]}', GOIDA!"
                    thread { loadCommandsList() }
                } else handleAuthorizationFail()
            }
            sendMessage(byteData, mapOf("paramsType" to "string"), correlationId, null)
            val success = latch.await(3000, TimeUnit.MILLISECONDS)
            if (!success) State.connectedToServer = false
        }
    }

    private fun loadCommandsList() {
        val byteData = JsonSerializer.serialize(
            ExecuteCommandDto("get_commands_list", null)
        )
        val correlationId = UUID.randomUUID().toString()
        val latch = CountDownLatch(1)
        registerResponseHandler(correlationId) { delivery ->
            try {
                val response = JsonSerializer.deserialize<ArrayList<CommandInfoDto>>(delivery.body)
                response.forEach{ command ->
                    Invoker.commands[command.name] = ServerCmd(command.name, command.description, command.paramTypeName)
                }
                IOHandler printInfoLn "Commands loaded"
            } catch (e: Exception) { checkJwt(delivery) }
            latch.countDown()
        }
        sendMessage(byteData, mapOf("paramsType" to null), correlationId)
        val success = latch.await(3000, TimeUnit.MILLISECONDS)
        if (!success) State.connectedToServer = false
    }

    fun sendMessage(data: ByteArray, headers: Map<String, Any?>, correlationId: String, refresh: Boolean? = false) {
        if (!State.connectedToServer) return
        State.tasks++
        val authHeaders = headers.toMutableMap()
        when {
            refresh == null -> authHeaders["authorization"] = null
            refresh -> authHeaders["authorization"] = State.credentials["REFRESH_TOKEN"]
            else -> authHeaders["authorization"] = State.credentials["ACCESS_TOKEN"]
        }
        val properties = AMQP.BasicProperties.Builder()
            .headers(authHeaders)
            .appId(State.appName)
            .correlationId(correlationId)
            .build()
        try {
            publishChannel!!.queueDeclare(DATA_REQUESTS, false, false, false, null)
            publishChannel!!.basicPublish("", DATA_REQUESTS, properties, data)
        } catch (e: Exception) {
            handleConnectionFail("send message - fail")
        }
    }


    fun handleConnectionFail(errorMsg: String? = null) {
        try { if (currentConnection?.isOpen == true) currentConnection?.close() } catch (_: Exception) {}
        var msg = errorMsg ?: "Server is not responding, should retry connection? (Y/n): "
        State.connectedToServer = false
        while (!State.connectedToServer) {
            IOHandler printInfoLn msg
            IOHandler printInfo "& "
            val input = readln()
            when (input) {
                "Y" -> {
                    initializeConnection()
                    break
                }
                "n" -> {
                    Invoker.commands["exit"]!!.execute(listOf())
                    break
                }
                "change address" -> {
                    State.host = null
                    IOHandler.getServerAddress()
                    initializeConnection()
                    break
                }
                else -> {
                    msg = "Incorrect option, should retry connection? (Y/n): "
                }
            }
        }
    }

    private fun handleAuthorizationFail(msg: String? = null) {
        var message = msg ?: "Authorization failed"
        State.credentials.remove("ACCESS_TOKEN")
        State.credentials.remove("REFRESH_TOKEN")

        while (State.isRunning) {
            IOHandler printInfoLn message
            IOHandler printInfoLn "Retry authorization? (Y/n): "
            IOHandler printInfo "& "
            val input = readln()
            when (input) {
                "Y" -> {
                    IOHandler.getAuthCredentials()
                    thread { authorize() }
                    break
                }
                "n" -> {
                    Invoker.commands["exit"]!!.execute(listOf())
                    break
                }
                else -> message = "Incorrect option"
            }
        }
    }

    private fun checkJwt(delivery: Delivery): String? {
        try {
            val response = JsonSerializer.deserialize<String>(delivery.body)
            when {
                response.contains("JWT refresh token is expired") -> {
                    handleAuthorizationFail("JWT refresh token is expired, need to reauthenticate")
                    if (State.tasks > 1) State.tasks--
                    return null
                }

                response.contains("JWT access token is expired") -> {
                    IOHandler printInfoLn "JWT access token is expired, need to refresh token"
                    val byteData = JsonSerializer.serialize(
                        ExecuteCommandDto(
                            "refresh", CommandParam.StringParam(
                                State.credentials["REFRESH_TOKEN"]
                            )
                        )
                    )
                    sendMessage(byteData, mapOf("paramsType" to "string"), "cmd", refresh = true)
                    return null
                }

                response.contains("#&#") -> {
                    State.credentials["ACCESS_TOKEN"] = response.split("#&#")[0]
                    State.credentials["REFRESH_TOKEN"] = response.split("#&#")[1]
                    IOHandler.responsesThreads.add("Tokens refreshed")
                    return null
                }

                response.contains("authorize: incorrect password") -> {
                    handleAuthorizationFail()
                    return null
                }
                else -> return response
            }
        } catch (e: Exception) {
            return null
        }
    }
}