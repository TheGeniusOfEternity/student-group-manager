package handlers

import core.State
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
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
    private const val DATA_RESPONSES = "data-responses"
    var factory = ConnectionFactory().apply { this.host = State.host }
    private var currentConnection: Connection? = null

    fun initializeConnection() {
        var response: String
        factory.connectionTimeout = 1000
        State.tasks++
        try {
            currentConnection = factory.newConnection("client")
            val channel = currentConnection?.createChannel()
            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                response = String(delivery.body, charset("UTF-8"))
                if (response == "Yes, I am GOIDA!") {
                    State.connectedToServer = true
                    if (State.tasks == 2) {
                        IOHandler printInfoLn "Connection established"
                        State.tasks--
                        loadResponses(channel)
                        if (Invoker.commands.size == 3) loadCommandsList()
                    }
                }
            }
            sendMessage("Are you GOIDA?".toByteArray(), HEALTH_CHECK_REQUESTS, mapOf("paramsType" to null), channel)
            receiveMessage(HEALTH_CHECK_RESPONSES, deliverCallback, channel)
            Thread.sleep(300)
            if (!State.connectedToServer) {
                State.tasks--
                handleConnectionFail()
            }
        } catch (e: Exception) {
            State.tasks--
            handleConnectionFail("RabbitMQ is probably offline, try to reconnect? (Y/n): ")
        }
    }

    fun handleConnectionFail(errorMsg: String? = null) {
        currentConnection?.close()
        var msg = errorMsg ?: "Server is not responding, should retry connection? (Y/n): "
        while (!State.connectedToServer) {
            IOHandler printInfoLn msg
            IOHandler printInfo "& "
            val input = readln()
            when (input) {
                "Y" -> {
                    initializeConnection()
                    break
                }
                "n" -> {
                    Invoker.commands["exit"]!!.execute(listOf())
                    break
                }
                "change address" -> {
                    State.host = null
                    IOHandler.getServerAddress()
                    initializeConnection()
                    break
                }
                else -> {
                    msg = "Incorrect option, should retry connection? (Y/n): "
                }
            }
        }
    }

    private fun sendMessage(data: ByteArray, queueName: String, headers: Map<String, Any?>, channel: Channel?) {
        val properties = AMQP.BasicProperties.Builder()
            .headers(headers)
            .appId(State.appName)
            .build()
        try {
            channel?.queueDeclare(queueName, false, false, false, null)
            channel?.basicPublish("", queueName, properties, data)
        } catch (e: Exception) {
            handleConnectionFail("RabbitMQ is probably offline, try to reconnect? (Y/n): ")
        }
    }

    private fun receiveMessage(queueName: String, callback: DeliverCallback, channel: Channel?) {
        try {
            channel?.queueDeclare(queueName, false, false, false, null)
            channel?.basicConsume(queueName, false, filterDeliverCallback(callback, channel)) { _: String? ->}
        } catch (e: Exception) {
            handleConnectionFail("RabbitMQ is probably offline, try to reconnect? (Y/n): ")
        }
    }

    private fun loadCommandsList() {
        IOHandler printInfoLn "Getting available commands..."
        val channel = currentConnection?.createChannel()
        val bytedData = JsonSerializer.serialize(
            ExecuteCommandDto("get_commands_list", null)
        )
        sendMessage(bytedData, DATA_REQUESTS, mapOf("paramsType" to null), channel)
        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            val response = JsonSerializer.deserialize<ArrayList<CommandInfoDto>>(delivery.body)
            response.forEach{ command ->
                Invoker.commands[command.name] = ServerCmd(command.name, command.description, command.paramTypeName)
            }
        }
        receiveMessage(DATA_RESPONSES, deliverCallback, channel)
    }

    private fun filterDeliverCallback(callback: DeliverCallback, channel: Channel?)  = DeliverCallback { consumerTag: String?, delivery: Delivery ->
        try {
            val props = delivery.properties
            if (props.appId == State.appName) {
                callback.handle(consumerTag, delivery)
                channel?.basicAck(delivery.envelope.deliveryTag, false)
                channel?.close()
            }
        } catch (_: Exception) {}
    }

    fun fetch(data: ByteArray, queueName: String, headers: Map<String, String?>): ArrayList<String> {
        val channel = currentConnection?.createChannel()
        sendMessage(data, queueName, headers, channel)

        return loadResponses(channel)
    }

    private fun loadResponses(channel: Channel?): ArrayList<String> {
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
        receiveMessage(DATA_RESPONSES, deliverCallback, channel)
        responseLatch.await(100, TimeUnit.MILLISECONDS)
        State.tasks--
        return responses
    }
}