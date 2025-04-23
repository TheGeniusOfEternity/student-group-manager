package commands
import State
import collection.StudyGroup
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Starts insertion process: user manually step by step enters new [StudyGroup] data
 */
class InsertCmd : Command {
    override val paramTypeName = "StudyGroup"
    override fun execute(args: List<CommandParam?>) {
        val responseMsg: String
        if (args.size == 1) {
            State.source = InputSource.CONSOLE
            val group = (args[0] as CommandParam.StudyGroupParam).value
            if (group != null) {
                Receiver.addStudyGroup(group.getId(), group)
                responseMsg = "Successfully added new group, type 'show' to see all groups"
            } else {
                responseMsg = "insert error: group data can't be validated"
            }
        } else {
            responseMsg = "insert: invalid count of arguments"
        }
        IOHandler.responsesThread.add(responseMsg)
    }

    override fun describe(): String {
        return "insert <key> {element} - adds to collection element with <key> id"
    }
}