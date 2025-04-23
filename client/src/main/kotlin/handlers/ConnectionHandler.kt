package handlers

import State
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import commands.ExitCmd
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
        State.latch = CountDownLatch(1)
        currentConnection = factory.newConnection("client")
        try {
            val channel = currentConnection?.createChannel()
            if (!currentConnection?.isOpen!!) IOHandler printInfoLn "RabbitMQ is offline"
            channel?.queueDeclare(HEALTH_CHECK_REQUESTS, false, false, false, null)
            channel?.queueDeclare(HEALTH_CHECK_RESPONSES, false, false, false, null)

            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                response = String(delivery.body, charset("UTF-8"))
                if (response == "Yes, I am GOIDA!") {
                    State.connectedToServer = true
                    IOHandler printInfoLn "Connection established"
                    fetchResponses()
                    if (Invoker.commands.size == 2) loadCommandsList()
                    else State.latch?.countDown()
                }
            }
            channel?.queuePurge(HEALTH_CHECK_REQUESTS)
            channel?.queuePurge(HEALTH_CHECK_RESPONSES)
            channel?.basicPublish("", HEALTH_CHECK_REQUESTS, null, "Are you GOIDA?".toByteArray())
            val tag = channel?.basicConsume(HEALTH_CHECK_RESPONSES, true, deliverCallback) { _: String? -> }

            State.latch?.await(100, TimeUnit.MILLISECONDS)
            if (!State.connectedToServer) {
                channel?.basicCancel(tag)
                handleConnectionFail()
            }
        } catch (e: Exception) {
            println(e.printStackTrace().toString())
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
                    currentConnection?.close()
                    initializeConnection()
                }
                "n" -> {
                    Invoker.commands["exit"]!!.execute(listOf())
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
        val channel = currentConnection?.createChannel()
        channel?.queueDeclare(queueName, false, false, false, null)
        channel?.basicPublish("", queueName, properties, data)
    }

    fun receiveMessage(queueName: String, callback: DeliverCallback) {
        val msgLatch = CountDownLatch(1)
        val channel = currentConnection?.createChannel()
        channel?.queueDeclare(queueName, false, false, false, null)
        channel?.basicConsume(queueName, true, callback) { _: String? ->}
        msgLatch.await(100, TimeUnit.MILLISECONDS)
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
            State.latch?.countDown()
        }
        receiveMessage(DATA_RESPONSES, deliverCallback)
    }

    fun fetchResponses(): ArrayList<String> {
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
        return responses
    }
}