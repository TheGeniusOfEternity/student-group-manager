package handlers

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery

object ConnectionHandler {
    private const val HEALTH_CHECK_REQUESTS = "health-check-requests"
    private const val HEALTH_CHECK_RESPONSES = "health-check-responses"
    fun initializeConnection() {
        val factory = ConnectionFactory().apply { this.host = "localhost" }
        var request = ""
        try {
            val connection = factory.newConnection()
            val channel = connection.createChannel()

            channel.queueDeclare(HEALTH_CHECK_REQUESTS, false, false, false, null)

            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                val response = String(delivery.body, charset("UTF-8"))
                if (response == "Are you GOIDA?") {
                    IOHandler printInfoLn "GOIDA requested, sending response..."
                    channel.basicPublish("", HEALTH_CHECK_RESPONSES, null, "Yes, I am GOIDA!".toByteArray())
                }
            }
            channel.basicConsume(HEALTH_CHECK_REQUESTS, true, deliverCallback) { _: String? -> }
        } catch (e: Exception) {
            IOHandler printInfoLn e.printStackTrace().toString()
        }
    }
}