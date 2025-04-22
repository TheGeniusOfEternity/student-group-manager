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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ServerCmd(val name: String, override val description: String, override val paramTypeName: String?) : Command {
    override fun execute(args: List<String>) {
        if (args.isEmpty()) {
            IOHandler printInfoLn "command execution error - command name is empty"
        } else {
            if (args.size == 2 && args[1].toLongOrNull() != null) {
                val bytedCheckRequest = JsonSerializer.serialize(ExecuteCommandDto("get_by_id", CommandParam.LongParam(args[1].toLong())))
                ConnectionHandler.sendMessage(
                    bytedCheckRequest,
                    ConnectionHandler.DATA_REQUESTS,
                    mapOf("paramsType" to "Id")
                )
                val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                    val data = JsonSerializer.deserialize<StudyGroup?>(delivery.body)
                    if (data != null) IOHandler printInfoLn "${args[0]} error: element with such id already exists"
                    else serverExecute(args[1].toLong())
                }
                ConnectionHandler.receiveMessage(ConnectionHandler.DATA_RESPONSES, deliverCallback)
            } else serverExecute()
        }
    }

    override fun describe() {
        IOHandler printInfoLn description
    }

    private fun serverExecute(id: Long? = null) {
        val params: CommandParam?
        if (id == null) { params = null }
        else {
            when (paramTypeName) {
                "StudyGroup" -> {
                    val groupData = GroupData()
                    groupData.add(Property("id", id.toString()))
                    params = CommandParam.StudyGroupParam(IOHandler.handleUserInput(groupData, "collection.StudyGroup"))
                    if (params == null) IOHandler printInfoLn "insertion error: group data can't be validated"
                }
                else -> {
                    params = CommandParam.LongParam(id)
                }
            }
        }

        if (params?.javaClass?.typeName == paramTypeName) {
            val latch = CountDownLatch(1)
            val bytedData = JsonSerializer.serialize(
                ExecuteCommandDto(name, params)
            )
            ConnectionHandler.sendMessage(bytedData, ConnectionHandler.DATA_REQUESTS, mapOf("paramsType" to paramTypeName))
            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                println("niga")
                val response = JsonSerializer.deserialize<ArrayList<String>>(delivery.body)
                println(response)
                latch.countDown()
            }

//            ConnectionHandler.receiveMessage(ConnectionHandler.DATA_RESPONSES, deliverCallback)
            latch.await(100, TimeUnit.MILLISECONDS)
        } else IOHandler printInfoLn "data serialization error: incorrect params's type (${params?.javaClass?.typeName}), $paramTypeName expected"
    }
}