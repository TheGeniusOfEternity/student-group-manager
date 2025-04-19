package commands

import collection.StudyGroup
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import dto.ExecuteCommandDto
import handlers.ConnectionHandler
import handlers.IOHandler
import serializers.JsonSerializer

class ServerCmd(override val description: String) : Command {
    override fun execute(args: List<String>) {
        if (args.isEmpty()) {
            IOHandler printInfoLn "command execution error - command name is empty"
        } else {
            if (args.size == 2 && args[1].toLongOrNull() != null) {
                val bytedCheckRequest = JsonSerializer.serialize(ExecuteCommandDto("get_by_id", args[1].toLong()))
                ConnectionHandler.sendMessage(
                    bytedCheckRequest,
                    ConnectionHandler.DATA_REQUESTS,
                    mapOf("paramsType" to "Id")
                )
                val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                    val data = JsonSerializer.deserialize<StudyGroup?>(delivery.body)
                    if (data != null) IOHandler printInfoLn "${args[0]} error: element with such id already exists"
                    else serverExecute()
                }
                ConnectionHandler.receiveMessage(ConnectionHandler.DATA_RESPONSES, deliverCallback)
            } else serverExecute()
        }
    }

    override fun describe() {
        IOHandler printInfoLn description
    }

    fun serverExecute() {
        IOHandler printInfoLn "not implemented yet"
    }
}