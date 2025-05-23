package handlers

import com.rabbitmq.client.*
import dto.ExecuteCommandDto
import invoker.Invoker
import serializers.JsonSerializer

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
            val channel = currentConnection?.createChannel()
            channel?.queueDeclare(HEALTH_CHECK_REQUESTS, false, false, false, null)
            channel?.queueDeclare(HEALTH_CHECK_RESPONSES, false, false, false, null)
            channel?.queuePurge(HEALTH_CHECK_REQUESTS)
            channel?.queuePurge(HEALTH_CHECK_RESPONSES)

            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                val response = String(delivery.body, charset("UTF-8"))
                if (response == "Are you GOIDA?") {
                    IOHandler printInfoLn "GOIDA requested, sending response..."
                    val properties = AMQP.BasicProperties.Builder().appId(delivery.properties.appId).build()
                    channel?.basicPublish("", HEALTH_CHECK_RESPONSES, properties, "Yes, I am GOIDA!".toByteArray())
                    State.isConnectionFailNotified = false
                    handleRequests()
                }
            }
            channel?.basicConsume(HEALTH_CHECK_REQUESTS, true, deliverCallback) { _: String? -> }
            State.isRunning = true
            IOHandler printInfoLn "Connection to RabbitMQ established"
            State.isConnectionFailNotified = false
        } catch (e: Exception) {
            handleConnectionFail()
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
                try {
                    commandRequest = JsonSerializer.deserialize<ExecuteCommandDto>(delivery.body)
                    Invoker.run(
                        commandRequest!!.name,
                        if (commandRequest!!.params != null) listOf(commandRequest!!.params) else listOf(),
                        clientId
                    )
                } catch (e: Exception) {
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("execution error: " + e.message.toString())
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
    inline fun <reified T> handleResponse(clientId: String, commandResponse: ArrayList<T?>?) {
        val channel = currentConnection?.createChannel()
        val bytedResponse = JsonSerializer.serialize<ArrayList<T?>?>(commandResponse)
        val properties = AMQP.BasicProperties.Builder().appId(clientId).build()

        channel?.queueDeclare(DATA_RESPONSES, false, false, false, null)
        channel?.basicPublish("", DATA_RESPONSES, properties, bytedResponse)
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
}