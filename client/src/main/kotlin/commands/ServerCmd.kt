package commands

import GroupData
import Property
import collection.StudyGroup
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import dto.CommandParam
import dto.ExecuteCommandDto
import handlers.ConnectionHandler
import handlers.IOHandler
import serializers.JsonSerializer
import validators.PropertyValidator
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ServerCmd(val name: String, override val description: String, override val paramTypeName: String?) : Command {
    override fun execute(args: List<String>) {
        if (args.isEmpty()) {
            IOHandler printInfoLn "command execution error - command name is empty"
        } else {
            if (args.size == 2 && args[1].toLongOrNull() != null) {
                val propertyValidator = PropertyValidator()
                if (propertyValidator.validateData(Property("id", args[1]))) {
                    val latch = CountDownLatch(1)
                    val bytedCheckRequest = JsonSerializer.serialize(ExecuteCommandDto("get_by_id", CommandParam.LongParam(args[1].toLong())))
                    ConnectionHandler.sendMessage(
                        bytedCheckRequest,
                        ConnectionHandler.DATA_REQUESTS,
                        mapOf("paramsType" to "Id")
                    )
                    val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                        val data = JsonSerializer.deserialize<ArrayList<StudyGroup?>>(delivery.body)
                        if (data[0] != null) IOHandler printInfoLn "${args[0]} error: element with such id already exists"
                        else serverExecute(args[1].toLong())
                        latch.countDown()
                    }
                    ConnectionHandler.receiveMessage(ConnectionHandler.DATA_RESPONSES, deliverCallback, latch)
                    latch.await()
                } else IOHandler printInfoLn "$name error: wrong argument type"
            } else serverExecute()
        }
    }

    override fun describe() {
        IOHandler printInfoLn description
    }

    private fun compareTypes(provided: String?, required: String?): Boolean {
        return when (provided) {
            null -> required == null
            else -> provided.contains(required.toString())
        }
    }

    private fun serverExecute(id: Long? = null) {
        val params: CommandParam?
        if (paramTypeName != null && id == null) IOHandler printInfoLn "$name error - invalid count of arguments"
        else {
            when (paramTypeName) {
                "StudyGroup" -> {
                    val groupData = GroupData()
                    groupData.add(Property("id", id.toString()))
                    params = CommandParam.StudyGroupParam(IOHandler.handleUserInput(groupData, "collection.StudyGroup"))
                }
                else -> {
                    params = if (id !== null) CommandParam.LongParam(id) else null
                }
            }
            if (compareTypes(params?.javaClass?.typeName, paramTypeName)) {
                val latch = CountDownLatch(1)
                val bytedData = JsonSerializer.serialize(
                    ExecuteCommandDto(name, params)
                )
                ConnectionHandler.sendMessage(bytedData, ConnectionHandler.DATA_REQUESTS, mapOf("paramsType" to paramTypeName))
                val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                    val responses = JsonSerializer.deserialize<ArrayList<String>>(delivery.body)
                    responses.forEach { response ->
                        IOHandler printInfoLn response
                    }
                    latch.countDown()
                }

                ConnectionHandler.receiveMessage(ConnectionHandler.DATA_RESPONSES, deliverCallback, latch)
            } else IOHandler printInfoLn "data serialization error: incorrect params's type (${params?.javaClass?.typeName}), $paramTypeName expected"
        }
    }
}