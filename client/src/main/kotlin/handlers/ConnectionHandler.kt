package handlers

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery


object ConnectionHandler {
    private const val QUEUE_NAME = "TEST"
    fun sendMessage(msg: String) {
        val factory = ConnectionFactory()
        factory.host = "localhost"
        val connection = factory.newConnection().use { connection ->
            val channel = connection.createChannel().use { channel ->
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
        channel.basicConsume(QUEUE_NAME, true, deliverCallback) { consumerTag: String? -> }
    }
}