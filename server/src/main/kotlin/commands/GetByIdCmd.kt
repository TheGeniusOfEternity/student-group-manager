package commands

import collection.StudyGroup
import dto.CommandParam
import handlers.ConnectionHandler
import handlers.IOHandler
import receiver.Receiver
import java.util.ArrayList

class GetByIdCmd : Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 1) {
            val group = Receiver.getStudyGroup((args[0] as CommandParam.LongParam).value!!);
            val response = ArrayList<StudyGroup?>()
            response.add(group)
            ConnectionHandler.handleResponse(clientId, response)
        } else {
            IOHandler printInfoLn "get_by_id: invalid count of arguments"
        }
    }

    override fun describe(): String {
        return "get_by_id <key> - returns element with given <key>"
    }
}