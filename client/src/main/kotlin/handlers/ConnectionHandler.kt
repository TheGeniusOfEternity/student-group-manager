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
import dto.CommandParam
import dto.ExecuteCommandDto
import invoker.Invoker
import serializers.JsonSerializer

object ConnectionHandler {
    private const val HEALTH_CHECK_REQUESTS = "health-check-requests"
    private const val HEALTH_CHECK_RESPONSES = "health-check-responses"
    const val DATA_REQUESTS = "data-requests"
    private const val DATA_RESPONSES = "data-responses"
    private var factory: ConnectionFactory = ConnectionFactory()
    private var currentConnection: Connection? = null

    fun initializeConnection() {
        factory.apply {
            isAutomaticRecoveryEnabled = false
            this.host = "localhost"
            this.username = State.credentials["RABBITMQ_USERNAME"] ?: error("RABBITMQ_USERNAME not set")
            this.password = State.credentials["RABBITMQ_PASSWORD"] ?: error("RABBITMQ_PASSWORD not set")
        }
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
                    channel?.basicAck(delivery.envelope.deliveryTag, false)
                    channel?.close()
                    if (State.tasks == 2) {
                        IOHandler printInfoLn "Connection established"
                        authorize()
                        State.tasks--
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
        if (currentConnection?.isOpen == true) currentConnection?.close()
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
        val authHeaders = headers.toMutableMap()
        if (State.credentials["ACCESS_TOKEN"] == null && State.isAuthorized) {
            handleAuthorizationFail("Authorization token not set")
            return
        }
        authHeaders["authorization"] = State.credentials["ACCESS_TOKEN"]
        val properties = AMQP.BasicProperties.Builder()
            .headers(authHeaders)
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
            Thread.sleep(200)
        } catch (e: Exception) {
            handleConnectionFail("RabbitMQ is probably offline, try to reconnect? (Y/n): ")
        }
    }

    private fun loadCommandsList() {
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
            IOHandler printInfoLn "Commands loaded"
        }
        receiveMessage(DATA_RESPONSES, deliverCallback, channel)
        channel?.close()
    }

    private fun filterDeliverCallback(callback: DeliverCallback, channel: Channel?)  = DeliverCallback { consumerTag: String?, delivery: Delivery ->
        try {
            val props = delivery.properties
            if (props.appId == State.appName) {
                callback.handle(consumerTag, delivery)
                channel?.basicAck(delivery.envelope.deliveryTag, false)
            }
        } catch (_: Exception) {}
    }

    private fun authorize() {
        State.tasks++
        val channel = currentConnection?.createChannel()
        val bytedData = JsonSerializer.serialize(
            ExecuteCommandDto("authorize", CommandParam.StringParam(
                "${State.credentials["TEMP_USERNAME"]}:${State.credentials["TEMP_PASSWORD"]}")
            )
        )
        sendMessage(bytedData, DATA_REQUESTS, mapOf("paramsType" to "string"), channel)
        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            val response = JsonSerializer.deserialize<ArrayList<String>>(delivery.body)
            channel?.basicAck(delivery.envelope.deliveryTag, false)
            if (response[0].contains("authorize")) {
                handleAuthorizationFail(response[0])
            } else {
                State.credentials["ACCESS_TOKEN"] = response[0].split('#')[0]
                State.credentials["REFRESH_TOKEN"] = response[0].split('#')[1]
                State.isAuthorized = true
                IOHandler printInfoLn "Welcome back, '${State.credentials["TEMP_USERNAME"]}', GOIDA!"
                loadCommandsList()
                handleResponses()
            }
            State.tasks--
        }
        receiveMessage(DATA_RESPONSES, deliverCallback, channel)
        channel?.close()
    }

    private fun handleAuthorizationFail(msg: String? = null) {
        while (State.isRunning) {
            IOHandler printInfoLn msg
            IOHandler printInfoLn "Retry authorization? (Y/n): "
            IOHandler printInfo "& "
            val input = readln()
            when (input) {
                "Y" -> {
                    IOHandler.getAuthCredentials()
                    authorize()
                    break
                }
                "n" -> {
                    Invoker.commands["exit"]!!.execute(listOf())
                    break
                }
            }
        }
    }

    fun fetch(data: ByteArray, queueName: String, headers: Map<String, String?>, cmdName: String? = null) {
        if (currentConnection?.isOpen == true) {
            val channel = currentConnection?.createChannel()
            sendMessage(data, queueName, headers, channel)
            State.tasks++
            channel?.close()
        } else handleConnectionFail("cmd execution error, broker is offline")
    }


    private fun handleResponses() {
        try {
            val channel = currentConnection?.createChannel()
            channel?.queueDeclare(DATA_RESPONSES, false, false, false, null)

            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                val responses = JsonSerializer.deserialize<ArrayList<String>>(delivery.body)

                responses.forEach { response ->
                    if (response.contains("refresh token is expired")) {
                        handleAuthorizationFail("Auth token is expired, need to reauthenticate")
                    } else if (response.contains("access token is expired")) {
                        IOHandler printInfoLn "Access token is expired, need to refresh token"
                        State.tasks++
                        val bytedData = JsonSerializer.serialize(
                            ExecuteCommandDto("refresh", CommandParam.StringParam(
                                State.credentials["REFRESH_TOKEN"])
                            )
                        )
                        sendMessage(bytedData, DATA_REQUESTS, mapOf("paramsType" to "string"), channel)
                    } else if (response.contains("#")) {
                        State.credentials["ACCESS_TOKEN"] = response.split('#')[0]
                        State.credentials["REFRESH_TOKEN"] = response.split('#')[1]
                    } else IOHandler.responsesThreads.add(response)
                }
            }
            channel?.basicConsume(DATA_RESPONSES, true, deliverCallback) { _: String? -> }
        } catch (e: Exception) {
            handleConnectionFail()
        }
    }
}