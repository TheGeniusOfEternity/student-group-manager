package handlers

import State
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import commands.ServerCmd
import dto.ExecuteCommandDto
import invoker.Invoker
import serializers.JsonSerializer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object ConnectionHandler {
    private const val HEALTH_CHECK_REQUESTS = "health-check-requests"
    private const val HEALTH_CHECK_RESPONSES = "health-check-responses"
    const val DATA_REQUESTS = "data-requests"
    const val DATA_RESPONSES = "data-responses"
    private val factory = ConnectionFactory().apply { this.host = State.host }

    fun initializeConnection() {
        var response = ""
        val latch = CountDownLatch(1)
        val connection = factory.newConnection()
        try {
            val channel = connection.createChannel()
            if (!connection.isOpen) IOHandler printInfoLn "RabbitMQ is offline"
            channel.queueDeclare(HEALTH_CHECK_REQUESTS, false, false, false, null)
            channel.queueDeclare(HEALTH_CHECK_RESPONSES, false, false, false, null)

            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                response = String(delivery.body, charset("UTF-8"))
                if (response == "Yes, I am GOIDA!") {
                    State.connectedToServer = true
                    latch.countDown()
                    IOHandler printInfoLn "Connection established"
                    loadCommandsList()
                }
            }
            channel.queuePurge(HEALTH_CHECK_REQUESTS)
            channel.queuePurge(HEALTH_CHECK_RESPONSES)
            channel.basicPublish("", HEALTH_CHECK_REQUESTS, null, "Are you GOIDA?".toByteArray())
            channel.basicConsume(HEALTH_CHECK_RESPONSES, true, deliverCallback) { _: String? -> }

            latch.await(100, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            println(e.printStackTrace().toString())
        } finally {
            if (response.isEmpty()) {
                connection.close()
            }
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

    fun sendMessage(data: ByteArray, queueName: String, headers: Map<String, Any?>) {
        val properties = AMQP.BasicProperties.Builder().headers(headers).build()
        factory.newConnection().use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare(queueName, false, false, false, null)
                channel.basicPublish("", queueName, properties, data)
            }
        }
    }

    fun receiveMessage(queueName: String, callback: DeliverCallback) {
        val connection = factory.newConnection()
        val channel = connection.createChannel()

        channel.queueDeclare(queueName, false, false, false, null)
        channel.basicConsume(queueName, true, callback) { _: String? -> }
    }

    private fun loadCommandsList() {
        IOHandler printInfoLn "Getting available commands..."
        val bytedData = JsonSerializer.serialize(
            ExecuteCommandDto("get_commands_list", null)
        )
        sendMessage(bytedData, DATA_REQUESTS, mapOf("paramsType" to null))
        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            val response = JsonSerializer.deserialize<List<Pair<String, String>>>(delivery.body)
            response.forEach{ command ->
                Invoker.commands[command.first] = ServerCmd(command.second)
            }
        }
        receiveMessage(DATA_RESPONSES, deliverCallback)
    }
}