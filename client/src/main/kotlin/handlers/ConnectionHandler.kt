package handlers

import collection.StudyGroup
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
import gui.controllers.AuthController
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
    private val DATA_RESPONSES = "data-responses-${State.appName}"
    private val responseHandlers = mutableMapOf<String, (Delivery) -> Unit>()
    private var factory: ConnectionFactory = ConnectionFactory()
    private var currentConnection: Connection? = null
    private var publishChannel = currentConnection?.createChannel()
    private var consumeChannel = currentConnection?.createChannel()

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
                    val handler = responseHandlers[correlationId]
                    if (handler != null) {
                        try {
                            handler(delivery)
                        } catch (e: Exception) {
                            IOHandler printInfoLn "Error in handler for $correlationId: ${e.printStackTrace()}"
                            IOHandler printInfoLn JsonSerializer.deserialize<String>(delivery.body)
                        } finally {
                            consumeChannel!!.basicAck(delivery.envelope.deliveryTag, false)
                        }
                    } else {
                        consumeChannel!!.basicAck(delivery.envelope.deliveryTag, false)
                        IOHandler printInfoLn "No handler registered for correlationId: $correlationId"
                    }
                }
                consumeChannel!!.basicConsume(DATA_RESPONSES, false, deliverCallback)  { _: String? -> }
            } else State.connectedToServer = false

        } catch (e: Exception) {
            State.connectedToServer = false
        }
    }

    fun initializeConnection(): String? {
        factory.apply {
            this.host = State.host
            this.username = State.credentials["RABBITMQ_USERNAME"] ?: error("RABBITMQ_USERNAME not set")
            this.password = State.credentials["RABBITMQ_PASSWORD"] ?: error("RABBITMQ_PASSWORD not set")
        }
        factory.connectionTimeout = 1000
        factory.requestedHeartbeat = 2

        try {
            currentConnection = factory.newConnection("client")
            publishChannel = currentConnection?.createChannel()
            consumeChannel = currentConnection?.createChannel()

            if (!preflightSucceeded()) return "Server is not responding, try to reconnect?"
            else {
                IOHandler.responsesThreads.add("Successfully connected to server")
                startResponseConsumer()
                return null
            }
        } catch (e: TimeoutException) {
            return "RabbitMQ is probably offline, try to reconnect?"
        } catch (e: Exception) {
            return "Server is not responding, try to reconnect?"
        }
    }

    fun authorize(controllerLatch: CountDownLatch?) {
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
            val latch = CountDownLatch(1)
            registerResponseHandler("authorize") { delivery ->
                latch.countDown()
                val response = JsonSerializer.deserialize<String>(delivery.body)
                if (!response.contains("authorize")) {
                    IOHandler printInfoLn "Authentication successful"
                    State.credentials["ACCESS_TOKEN"] = response.split("#&#")[0]
                    State.credentials["REFRESH_TOKEN"] = response.split("#&#")[1]
                    State.isAuthenticated = true
                    controllerLatch?.countDown()
                    IOHandler printInfoLn "Welcome back, '${State.credentials["TEMP_USERNAME"]}', GOIDA!"
                    thread { loadCommandsList() }
                }
            }
            sendMessage(byteData, mapOf("paramsType" to "string"), "authorize", null)
            val success = latch.await(3000, TimeUnit.MILLISECONDS)
            if (!success) State.connectedToServer = false
        }
    }

    private fun loadCommandsList() {
        val byteData = JsonSerializer.serialize(
            ExecuteCommandDto("get_commands_list", null)
        )
        val latch = CountDownLatch(1)
        registerResponseHandler("get_commands_list") { delivery ->
            try {
                val response = JsonSerializer.deserialize<ArrayList<CommandInfoDto>>(delivery.body)
                response.forEach{ command ->
                    Invoker.commands[command.name] = ServerCmd(command.name, command.description, command.paramTypeName)
                }
                IOHandler.responsesThreads.add("Commands loaded")
            } catch (e: Exception) { checkJwt(delivery) }
            latch.countDown()
        }
        sendMessage(byteData, mapOf("paramsType" to null), "get_commands_list")
        val success = latch.await(3000, TimeUnit.MILLISECONDS)
        if (!success) State.connectedToServer = false
    }

    fun loadCollectionInfo() {
        val byteData = JsonSerializer.serialize(
            ExecuteCommandDto("show", null)
        )
        val latch = CountDownLatch(1)
        registerResponseHandler("load_collection") { delivery ->
            try {
                val response = JsonSerializer.deserialize<ArrayList<StudyGroup>>(delivery.body)
                State.localCollection.clear()
                response.forEach{ group ->
                    State.localCollection[group.getId()] = group
                }
            } catch (e: Exception) {
                IOHandler printInfoLn e.message
                checkJwt(delivery)
            }
            latch.countDown()
        }
        sendMessage(byteData, mapOf("paramsType" to null), "load_collection")
        val success = latch.await(500, TimeUnit.MILLISECONDS)
        if (!success) State.connectedToServer = false
    }

    fun sendMessage(data: ByteArray, headers: Map<String, Any?>, correlationId: String, refresh: Boolean? = false) {
        if (!State.connectedToServer || !preflightSucceeded()) return
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
            State.connectedToServer = false
        }
    }

    private fun handleAuthorizationFail(msg: String? = null) {
        IOHandler.responsesThreads.add(msg)
        State.credentials.remove("ACCESS_TOKEN")
        State.credentials.remove("REFRESH_TOKEN")
    }

    private fun checkJwt(delivery: Delivery): String? {
        try {
            val response = JsonSerializer.deserialize<String>(delivery.body)
            when {
                response.contains("JWT refresh token is expired") -> {
                    (SceneHandler.controllers["auth"] as AuthController)
                        .handleAuthFail("JWT refresh token is expired, need to reauthenticate")
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

    private fun preflightSucceeded(): Boolean {
        val socket = Socket()
        val socketAddress = InetSocketAddress(State.host, 1234)
        socket.soTimeout = 1000
        try {
            socket.connect(socketAddress, 1000)

            val output = DataOutputStream(socket.getOutputStream())
            val input = DataInputStream(socket.getInputStream())

            output.writeUTF("PING")
            output.flush()

            try {
                when (input.readUTF()) {
                    "PONG" -> {
                        State.connectedToServer = true
                        return true
                    }
                    else -> {
                        IOHandler.responsesThreads.add("Protocol mismatch")
                        return false
                    }
                }
            } catch (_: SocketTimeoutException) {
                State.connectedToServer = false
                return false
            }
        } catch (_: IOException) {
            State.connectedToServer = false
            return false
        }
    }

    fun closeConnection() {
        try {
            consumeChannel?.queueDelete(DATA_RESPONSES)
            if (currentConnection?.isOpen == true) currentConnection?.close()
        } catch (_: Exception) {}
    }
}