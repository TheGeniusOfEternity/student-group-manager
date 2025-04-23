package handlers

import State
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import commands.ServerCmd
import dto.CommandInfoDto
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
    private var currentConnection: Connection? = null

    fun initializeConnection() {
        var response = ""
        State.tasks++
        try {
            currentConnection = factory.newConnection("client")
            val channel = currentConnection?.createChannel()

            channel?.queueDeclare(HEALTH_CHECK_REQUESTS, false, false, false, null)
            channel?.queueDeclare(HEALTH_CHECK_RESPONSES, false, false, false, null)
            channel?.queuePurge(HEALTH_CHECK_REQUESTS)
            channel?.queuePurge(HEALTH_CHECK_RESPONSES)
            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                response = String(delivery.body, charset("UTF-8"))
                if (response == "Yes, I am GOIDA!") {
                    State.connectedToServer = true
                    if (State.tasks == 2) {
                        IOHandler printInfoLn "Connection established"
                        State.tasks--
                        fetchResponses()
                        if (Invoker.commands.size == 2) loadCommandsList()
                    }
                }
            }
            channel?.basicPublish("", HEALTH_CHECK_REQUESTS, null, "Are you GOIDA?".toByteArray())
            channel?.basicConsume(HEALTH_CHECK_RESPONSES, true, deliverCallback) { _: String? -> }
            Thread.sleep(300)
            if (!State.connectedToServer) {
                currentConnection?.close()
                State.tasks--
                handleConnectionFail()
            }
        } catch (e: Exception) {
            handleConnectionFail("RabbitMQ is probably offline, try to reconnect? (Y/n): ")
        }
    }

    fun handleConnectionFail(errorMsg: String? = null) {
        var msg = errorMsg ?: "Server is not responding, should retry connection? (Y/n): "
        while (!State.connectedToServer) {
            IOHandler printInfoLn msg
            IOHandler printInfo "& "
            val input = readln()
            when (input) {
                "Y" -> {
                    initializeConnection()
                }
                "n" -> {
                    Invoker.commands["exit"]!!.execute(listOf())
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
        val channel = currentConnection?.createChannel()
        channel?.queueDeclare(queueName, false, false, false, null)
        channel?.basicPublish("", queueName, properties, data)
    }

    fun receiveMessage(queueName: String, callback: DeliverCallback) {
        val channel = currentConnection?.createChannel()
        val latch = CountDownLatch(1)
        channel?.queueDeclare(queueName, false, false, false, null)
        channel?.basicConsume(queueName, true, callback) { _: String? ->}
        latch.await(100, TimeUnit.MILLISECONDS)
        channel?.close()
    }

    private fun loadCommandsList() {
        IOHandler printInfoLn "Getting available commands..."
        val bytedData = JsonSerializer.serialize(
            ExecuteCommandDto("get_commands_list", null)
        )
        sendMessage(bytedData, DATA_REQUESTS, mapOf("paramsType" to null))
        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            val response = JsonSerializer.deserialize<ArrayList<CommandInfoDto>>(delivery.body)
            response.forEach{ command ->
                Invoker.commands[command.name] = ServerCmd(command.name, command.description, command.paramTypeName)
            }
        }
        receiveMessage(DATA_RESPONSES, deliverCallback)
    }

    fun fetchResponses(): ArrayList<String> {
        State.tasks++
        val responseLatch = CountDownLatch(1)
        var responses: ArrayList<String> = ArrayList()
        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            responses = JsonSerializer.deserialize<ArrayList<String>>(delivery.body)
            responses.forEach { response ->
                IOHandler printInfoLn response
            }
            responseLatch.countDown()
        }
        receiveMessage(DATA_RESPONSES, deliverCallback)
        responseLatch.await(100, TimeUnit.MILLISECONDS)
        State.tasks--
        return responses
    }
}