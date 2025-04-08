package handlers

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery


object ConnectionHandler {
    private const val QUEUE_NAME = "TEST"
    fun initializeConnection(): Boolean {
        val factory = ConnectionFactory().apply { this.host = State.host }
        try {
            factory.newConnection().use { connection ->
                connection.isOpen
            }
            IOHandler printInfoLn "Connection established"
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun handleConnectionFail() {
        var msg = "Server is not responding, should retry connection? (Y/n): "
        while (!State.serverConnection) {
            IOHandler printInfoLn msg
            IOHandler printInfo "& "
            val input = readln()
            when (input) {
                "Y" -> {
                    State.serverConnection = initializeConnection()
                }
                "n" -> {
                    State.isRunning = false
                    break
                }
                else -> {
                    msg = "Incorrect option, should retry connection? (Y/n): "
                }
            }
        }
    }

    fun sendMessage(msg: String) {
        val factory = ConnectionFactory()
        factory.host = "localhost"
        factory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare(QUEUE_NAME, false, false, false, null)
                channel.basicPublish("", QUEUE_NAME, null, msg.toByteArray())
                println(" [x] Sent '$msg'")
            }
        }
    }

    fun receiveMessage() {
        val factory = ConnectionFactory()
        factory.host = "localhost"
        val connection = factory.newConnection()
        val channel = connection.createChannel()

        channel.queueDeclare(QUEUE_NAME, false, false, false, null)
        println(" [*] Waiting for messages. To exit press CTRL+C")

        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            val message = String(delivery.body, charset("UTF-8"))
            println(" [x] Received '$message'")
        }
        channel.basicConsume(QUEUE_NAME, true, deliverCallback) { _: String? -> }
    }
}