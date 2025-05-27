package handlers

import com.rabbitmq.client.*
import dto.CommandParam
import dto.ExecuteCommandDto
import invoker.Invoker
import io.jsonwebtoken.ExpiredJwtException
import receiver.Receiver
import serializers.JsonSerializer
import services.JwtTokenService
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Handles all logic with connection to broker and requests from client
 * @property HEALTH_CHECK_REQUESTS - name of broker's queue for receiving status check requests from client
 * @property HEALTH_CHECK_RESPONSES - name of broker's queue for sending response on status check  to client
 * @property DATA_REQUESTS - name of broker's queue for receiving data requests (command executions) from client
 * @property DATA_RESPONSES - name of broker's queue for sending response on command execution to client
 * @property factory - object for connection to broker
 * @property currentConnection - connection to broker entity
 */
object ConnectionHandler {
    private const val HEALTH_CHECK_REQUESTS = "health-check-requests"
    private const val HEALTH_CHECK_RESPONSES = "health-check-responses"
    private const val DATA_REQUESTS = "data-requests"
    const val DATA_RESPONSES = "data-responses"
    private val factory = ConnectionFactory()
    var currentConnection: Connection? = null

    /**
     * Tries to connect to broker and enables request listening
     * Otherwise calls [handleConnectionFail]
     */
    fun initializeConnection() {
        try {
            factory.apply {
                isAutomaticRecoveryEnabled = false
                this.host = "localhost"
                this.username = State.credentials["RABBITMQ_USERNAME"] ?: error("RABBITMQ_USERNAME not set")
                this.password = State.credentials["RABBITMQ_PASSWORD"] ?: error("RABBITMQ_PASSWORD not set")
            }
            currentConnection = factory.newConnection("server")
            State.isRunning = true
            IOHandler printInfoLn "Connection to RabbitMQ established"
            IOHandler printInfoLn "Server is running on ${getLocalIpAddress()}"
            Thread {
                startHeartbeat()
            }.start()
            handleRequests()
            State.isConnectionFailNotified = false
        } catch (e: Exception) {
            handleConnectionFail()
        }
    }

    private fun startHeartbeat(port: Int = 1234, heartbeatTimeoutMs: Long = 1000) {
        val serverSocket = ServerSocket(port)
        println("Server listening on port $port")

        val clientsLastHeartbeat = ConcurrentHashMap<Socket, Long>()
        val clientPool = Executors.newCachedThreadPool()
        val scheduler = Executors.newSingleThreadScheduledExecutor()

        scheduler.scheduleAtFixedRate({
            val now = System.currentTimeMillis()
            clientsLastHeartbeat.entries.removeIf { (socket, lastHeartbeat) ->
                if (now - lastHeartbeat > heartbeatTimeoutMs) {
                    println("Client ${socket.inetAddress.hostAddress} timed out. Closing connection.")
                    try {
                        socket.close()
                    } catch (_: Exception) {}
                    true
                } else false
            }
        }, heartbeatTimeoutMs, heartbeatTimeoutMs, TimeUnit.MILLISECONDS)

        while (State.isRunning) {
            val clientSocket = serverSocket.accept()
            println("Client connected: ${clientSocket.inetAddress.hostAddress}")
            clientsLastHeartbeat[clientSocket] = System.currentTimeMillis()

            clientPool.submit {
                try {
                    val input = DataInputStream(clientSocket.getInputStream())
                    val output = DataOutputStream(clientSocket.getOutputStream())

                    while (!clientSocket.isClosed) {
                        val message = input.readUTF()
                        if (message == "PING") {
                            clientsLastHeartbeat[clientSocket] = System.currentTimeMillis()
                            output.writeUTF("PONG")
                            output.flush()
                        }
                    }
                } catch (e: Exception) {
                    println("Client ${clientSocket.inetAddress.hostAddress} disconnected or error: ${e.message}")
                } finally {
                    clientsLastHeartbeat.remove(clientSocket)
                    try {
                        clientSocket.close()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    /**
     * Listener of client requests - receive message from broker, deserializes it and executes needed command
     * On error calls [handleConnectionFail]
     */
    private fun handleRequests() {
        try {
            val channel = currentConnection?.createChannel()
            var commandRequest: ExecuteCommandDto?
            channel?.queueDeclare(DATA_REQUESTS, false, false, false, null)

            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                val clientId = delivery.properties.appId
                val correlationId = delivery.properties.correlationId
                val userId: Long?
                try {
                    if (delivery.properties.headers["authorization"] != null) {
                        val jwtToken = JwtTokenService.decodeToken(
                            String((delivery.properties.headers["authorization"] as LongString).bytes,
                                StandardCharsets.UTF_8)
                        )
                        when (jwtToken.body["typ"]) {
                            "access" -> {
                                IOHandler printInfoLn "Access token provided"
                                if (jwtToken.body.expiration.before(Date())) {
                                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
                                        .add(Pair("authorization error: access token is expired", correlationId))
                                    return@DeliverCallback
                                }
                            }

                            "refresh" -> {
                                IOHandler printInfoLn "Refresh token provided"
                                if (jwtToken.body.expiration.before(Date())) {
                                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
                                        .add(Pair("refresh_token error: refresh token is expired", correlationId))
                                    return@DeliverCallback
                                }
                            }

                            else -> {
                                IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
                                    .add(Pair("authorization error: invalid token", correlationId))
                                return@DeliverCallback
                            }
                        }
                        userId = jwtToken.body.subject.toLong()
                    } else userId = null
                    commandRequest = JsonSerializer.deserialize<ExecuteCommandDto>(delivery.body)
                    val params = buildList {
                        commandRequest!!.params?.let { add(it) }
                        userId?.let { add(CommandParam.LongParam(it)) }
                    }
                    IOHandler printInfoLn "${commandRequest!!.name} cmd"
                    Invoker.run(
                        commandRequest!!.name,
                        params,
                        clientId,
                        correlationId
                    )
                    Receiver.loadFromDatabase()
                } catch (e: ExpiredJwtException) {
                    val tokenType = e.claims["typ"] as String
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
                        .add(Pair("execution error: JWT $tokenType token is expired", correlationId))
                } catch (e: Exception) {
                    IOHandler printInfoLn e.printStackTrace().toString()
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
                        .add(Pair("execution error: " + e.message.toString(), correlationId))
                }
            }
            channel?.basicConsume(DATA_REQUESTS, true, deliverCallback) { _: String? -> }
        } catch (e: Exception) {
            handleConnectionFail()
        }
    }

    /**
     * Sends all responses from [IOHandler.responsesThreads] to client, serializing them to messages and sending them to broker
     */
    inline fun <reified T> handleResponse(clientId: String, commandResponse: T?, correlationId: String) {
        val channel = currentConnection?.createChannel()
        val byteResponse = JsonSerializer.serialize<T?>(commandResponse)
        val properties = AMQP.BasicProperties.Builder().appId(clientId).correlationId(correlationId).build()

        channel?.queueDeclare(DATA_RESPONSES, false, false, false, null)
        channel?.basicPublish("", DATA_RESPONSES, properties, byteResponse)
        channel?.close()
        IOHandler.responsesThreads.remove(clientId)
    }

    /**
     * Called by different connection loss issues. Retries to restore connection until success or server shutdown
     */
    private fun handleConnectionFail(msg: String? = null) {
        if (currentConnection?.isOpen == true) currentConnection?.close()
        if (!State.isConnectionFailNotified) {
            val message = msg ?: "RabbitMQ is probably offline, retrying..."
            IOHandler printInfoLn message
            State.isConnectionFailNotified = true
        }
        Thread.sleep(100)
        initializeConnection()
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (face in interfaces) {
                val addresses = face.inetAddresses
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress && addr is InetAddress) {
                        val ip = addr.hostAddress
                        if (ip.indexOf(':') < 0) { // IPv4 check
                            return ip
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            println("Error getting IP address: ${ex.message}")
        }
        return null
    }
}