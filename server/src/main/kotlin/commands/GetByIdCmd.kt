package commands

import handlers.IOHandler
import receiver.Receiver

class GetByIdCmd : Command {
    override fun execute(args: List<Any?>) {
        if (args.size == 1) {
            val group = Receiver.getStudyGroup(((args[0] as String) as String).toLong());
            if (group == null) {
                IOHandler printInfoLn "No groups found"
            } else {
                IOHandler printInfoLn group.toString()
            }
        } else {
            IOHandler printInfoLn "get_by_id: invalid count of arguments"
        }
    }

    override fun describe() {
        IOHandler printInfoLn "get_by_id <key> - returns element with given <key>"
    }
}