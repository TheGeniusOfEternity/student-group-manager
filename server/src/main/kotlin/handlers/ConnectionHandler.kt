package handlers

import com.rabbitmq.client.*
import dto.ExecuteCommandDto
import invoker.Invoker
import serializers.JsonSerializer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object ConnectionHandler {
    private const val HEALTH_CHECK_REQUESTS = "health-check-requests"
    private const val HEALTH_CHECK_RESPONSES = "health-check-responses"
    private const val DATA_REQUESTS = "data-requests"
    const val DATA_RESPONSES = "data-responses"
    private val factory = ConnectionFactory().apply { this.host = "localhost" }
    var currentConnection: Connection? = null
    fun initializeConnection() {
        try {
            factory.apply { isAutomaticRecoveryEnabled = false }
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
                }
            }
            channel?.basicConsume(HEALTH_CHECK_REQUESTS, true, deliverCallback) { _: String? -> }
            State.isRunning = true
            IOHandler printInfoLn "Connection to RabbitMQ established"
        } catch (e: Exception) {
            handleConnectionFail()
        }
    }

    fun handleRequests() {
        try {
            val channel = currentConnection?.createChannel()
            val latch = CountDownLatch(1)
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
                    latch.countDown()
                } catch (e: Exception) {
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("execution error: " + e.message.toString())
                }
            }
            channel?.basicConsume(DATA_REQUESTS, true, deliverCallback) { _: String? -> }
            latch.await(100, TimeUnit.MILLISECONDS)
            channel?.close()
        } catch (e: Exception) {
            handleConnectionFail()
        }
    }

    inline fun <reified T> handleResponse(clientId: String, commandResponse: ArrayList<T?>?) {
        val channel = currentConnection?.createChannel()
        val bytedResponse = JsonSerializer.serialize<ArrayList<T?>?>(commandResponse)
        val properties = AMQP.BasicProperties.Builder().appId(clientId).build()

        channel?.queueDeclare(DATA_RESPONSES, false, false, false, null)
        channel?.basicPublish("", DATA_RESPONSES, properties, bytedResponse)
        channel?.close()
        IOHandler.responsesThreads.remove(clientId)
    }

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