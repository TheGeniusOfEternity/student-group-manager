package handlers

import State
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object ConnectionHandler {
    private const val HEALTH_CHECK_REQUESTS = "health-check-requests"
    private const val HEALTH_CHECK_RESPONSES = "health-check-responses"

    fun initializeConnection() {
        val factory = ConnectionFactory().apply { this.host = State.host }
        var response: String

        try {
            val connection = factory.newConnection()
            val channel = connection.createChannel()
            if (!connection.isOpen) IOHandler printInfoLn "RabbitMQ is offline"
            channel.queueDeclare(HEALTH_CHECK_REQUESTS, false, false, false, null)
            channel.queueDeclare(HEALTH_CHECK_RESPONSES, false, false, false, null)

            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                response = String(delivery.body, charset("UTF-8"))
                if (response == "Yes, I am GOIDA!") {
                    State.connectedToServer = true
                }
            }
            channel.queuePurge(HEALTH_CHECK_REQUESTS)
            channel.basicPublish("", HEALTH_CHECK_REQUESTS, null, "Are you GOIDA?".toByteArray())
            channel.basicConsume(HEALTH_CHECK_RESPONSES, true, deliverCallback) { _: String? -> }
        } catch (e: Exception) {
            println(e.printStackTrace().toString())
        }
    }

    fun handleConnectionFail() {
        var msg = "Server is not responding, should retry connection? (Y/n): "
        while (!State.connectedToServer) {
            IOHandler printInfoLn msg
            IOHandler printInfo "& "
            val input = readln()
            when (input) {
                "Y" -> {
                    initializeConnection()
                    if (State.connectedToServer) IOHandler printInfoLn "Connection established"
                }
                "n" -> {
                    State.isRunning = false
                    break
                }
                "late connection" -> {
                    break
                }
                else -> {
                    msg = "Incorrect option, should retry connection? (Y/n): "
                }
            }
        }
    }

    fun sendMessage(msg: String, queueName: String) {
        val factory = ConnectionFactory()
        factory.host = State.host
        factory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare(queueName, false, false, false, null)
                channel.basicPublish("", queueName, null, msg.toByteArray())
                println(" [x] Sent '$msg'")
            }
        }
    }

    fun receiveMessage(queueName: String) {
        val factory = ConnectionFactory()
        factory.host = State.host
        val connection = factory.newConnection()
        val channel = connection.createChannel()

        channel.queueDeclare(queueName, false, false, false, null)
        println(" [*] Waiting for messages. To exit press CTRL+C")

        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            val message = String(delivery.body, charset("UTF-8"))
            println(" [x] Received '$message'")
        }
        channel.basicConsume(queueName, true, deliverCallback) { _: String? -> }
    }
}