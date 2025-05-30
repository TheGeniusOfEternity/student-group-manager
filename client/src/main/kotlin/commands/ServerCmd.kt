package commands

import core.GroupData
import core.Property
import dto.CommandParam
import dto.ExecuteCommandDto
import handlers.ConnectionHandler
import handlers.IOHandler
import serializers.JsonSerializer
import validators.PropertyValidator

class ServerCmd(val name: String, override val description: String, override val paramTypeName: String?) : Command {
    override fun execute(args: List<String>) {
        if (args.isEmpty()) {
            IOHandler printInfoLn "command execution error - command name is empty"
        } else {
            if (args.size == 2) {
                serverExecute(args[1])
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

    private fun serverExecute(arg: String? = null) {
        val params: CommandParam?
        if (paramTypeName != null && arg == null) IOHandler printInfoLn "$name error - invalid count of arguments"
        else {
            when (paramTypeName) {
                "StudyGroup" -> {
                    val groupData = GroupData()
                    val propertyValidator = PropertyValidator()
                    if (!propertyValidator.validateData(Property("id", arg))) {
                        IOHandler printInfoLn "$name error - $arg is not a valid id"
                        return
                    }
                    groupData.add(Property("id", arg))
                    params = CommandParam.StudyGroupParam(IOHandler.handleUserInput(groupData, "collection.StudyGroup"))
                }
                "String" -> {
                    params = CommandParam.StringParam(arg)
                }
                else -> {
                    params = if (arg !== null) CommandParam.LongParam(arg.toLongOrNull()) else null
                }
            }
            if (compareTypes(params?.javaClass?.typeName, paramTypeName)) {
                val byteData = JsonSerializer.serialize(
                    ExecuteCommandDto(name, params)
                )
                ConnectionHandler.sendMessage(byteData, mapOf("param" to paramTypeName), "cmd")
            } else IOHandler printInfoLn "data serialization error: incorrect params type (${params?.javaClass?.typeName}), $paramTypeName expected"
        }
    }
}