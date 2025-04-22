package commands

import collection.StudyGroup
import handlers.ConnectionHandler
import handlers.IOHandler
import receiver.Receiver
import java.util.ArrayList

class GetByIdCmd : Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<Any?>) {
        IOHandler printInfoLn args.toString()
//        if (args.size == 1) {
//            val group = Receiver.getStudyGroup(((args[0] as String)).toLong());
//            val response = ArrayList<StudyGroup>()
//
//            IOHandler printInfoLn group.toString()
////            ConnectionHandler.handleResponse(response)
//        } else {
//            IOHandler printInfoLn "get_by_id: invalid count of arguments"
//        }
    }

    override fun describe(): String {
        return "get_by_id <key> - returns element with given <key>"
    }
}