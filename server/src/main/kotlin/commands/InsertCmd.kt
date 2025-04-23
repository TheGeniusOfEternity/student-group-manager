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
        if (args.size == 1) {
            State.source = InputSource.CONSOLE
            val group = (args[0] as CommandParam.StudyGroupParam).value
            if (group != null) {
                if (Receiver.getStudyGroup(group.getId()) != null) {
                    IOHandler.responsesThread.add("insert error: group already exists. Type 'update' to rewrite the group")
                } else {
                    Receiver.addStudyGroup(group.getId(), group)
                    IOHandler.responsesThread.add("Successfully added new group, type 'show' to see all groups")
                }
            } else {
                IOHandler.responsesThread.add("insert error: group data can't be validated")
            }
        } else {
            IOHandler.responsesThread.add("insert: invalid count of arguments")
        }
    }

    override fun describe(): String {
        return "insert <key> {element} - adds to collection element with <key> id"
    }
}