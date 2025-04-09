package handlers

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import dto.ExecuteCommandDto
import invoker.Invoker
import serializers.JsonSerializer
import kotlin.reflect.typeOf

object ConnectionHandler {
    private const val HEALTH_CHECK_REQUESTS = "health-check-requests"
    private const val HEALTH_CHECK_RESPONSES = "health-check-responses"
    private const val DATA_REQUESTS = "data-requests"
    private const val DATA_RESPONSES = "data-responses"
    private val factory = ConnectionFactory().apply { this.host = "localhost" }
    fun initializeConnection() {

        try {
            val connection = factory.newConnection()
            val channel = connection.createChannel()

            channel.queueDeclare(HEALTH_CHECK_REQUESTS, false, false, false, null)

            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                val response = String(delivery.body, charset("UTF-8"))
                if (response == "Are you GOIDA?") {
                    IOHandler printInfoLn "GOIDA requested, sending response..."
                    channel.basicPublish("", HEALTH_CHECK_RESPONSES, null, "Yes, I am GOIDA!".toByteArray())
                    State.isRunning = true
                }
            }
            channel.basicConsume(HEALTH_CHECK_REQUESTS, true, deliverCallback) { _: String? -> }
            Thread.sleep(100)
            if (!State.isRunning) {
                connection.close()
                initializeConnection()
            }
        } catch (e: Exception) {
            IOHandler printInfoLn e.printStackTrace().toString()
        }
    }

    fun handleRequests() {
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        var commandRequest: ExecuteCommandDto<*>
        channel.queueDeclare(DATA_REQUESTS, false, false, false, null)

        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            commandRequest = when (delivery.properties.headers["paramsType"]) {
                String -> { JsonSerializer.deserialize<ExecuteCommandDto<String>>(delivery.body) }
                Long -> { JsonSerializer.deserialize<ExecuteCommandDto<Long>>(delivery.body) }
                else -> { JsonSerializer.deserialize<ExecuteCommandDto<Nothing>>(delivery.body) }
            }
            Invoker.run(commandRequest.name,
                if (commandRequest.params != null) listOf(commandRequest.params) else listOf()
            )

        }
        channel.basicConsume(DATA_REQUESTS, true, deliverCallback) { _: String? -> }
        Thread.sleep(Long.MAX_VALUE)
    }

    fun handleResponse(commandResponse: ArrayList<*>) {
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        val bytedResponse = when(commandResponse[0]) {
            is String -> {
                JsonSerializer.serialize<ArrayList<String>>(commandResponse
                    .filterIsInstance<String>() as ArrayList<String>)
            }
            else -> {
                null
            }
        }

        channel.queueDeclare(DATA_RESPONSES, false, false, false, null)
        channel.basicPublish("", DATA_RESPONSES, null, bytedResponse)
    }
}