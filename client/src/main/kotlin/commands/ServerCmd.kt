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
            if (args.size == 2) {
                serverExecute(args[1].toLongOrNull())
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

    private fun serverExecute(num: Long? = null) {
        val params: CommandParam?
        val responses: ArrayList<String>
        if (paramTypeName != null && num == null) IOHandler printInfoLn "$name error - invalid count of arguments"
        else {
            when (paramTypeName) {
                "StudyGroup" -> {
                    val groupData = GroupData()
                    val propertyValidator = PropertyValidator()
                    if (!propertyValidator.validateData(Property("id", num.toString()))) {
                        IOHandler printInfoLn "$name error - $num is not a valid id"
                        return
                    }
                    groupData.add(Property("id", num.toString()))
                    params = CommandParam.StudyGroupParam(IOHandler.handleUserInput(groupData, "collection.StudyGroup"))
                }
                else -> {
                    params = if (num !== null) CommandParam.LongParam(num) else null
                }
            }
            if (compareTypes(params?.javaClass?.typeName, paramTypeName)) {
                val bytedData = JsonSerializer.serialize(
                    ExecuteCommandDto(name, params)
                )
                ConnectionHandler.sendMessage(bytedData, ConnectionHandler.DATA_REQUESTS, mapOf("paramsType" to paramTypeName))
                responses = ConnectionHandler.fetchResponses()
                if (responses.isEmpty()) {
                    State.connectedToServer = false
                    ConnectionHandler.handleConnectionFail("Connection lost, try to reconnect? (Y/n)")
                }
            } else IOHandler printInfoLn "data serialization error: incorrect params's type (${params?.javaClass?.typeName}), $paramTypeName expected"
        }
    }
}