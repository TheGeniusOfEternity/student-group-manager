package commands

import collection.StudyGroup
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Removes [StudyGroup] from [Receiver.stdGroupCollection]
 */
class RemoveCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 1) {
            val id = (args[0] as CommandParam.LongParam).value
            if (id != null) {
                if (Receiver.getStudyGroup(id) == null) {
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("remove error: group with id $id not found")
                } else {
                    Receiver.removeStudyGroup(id)
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("Group #$id was removed")
                }
            } else {
                IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("remove error: provided id is not a number")
            }
        } else {
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("remove: invalid count of arguments.")
        }
    }

    override fun describe(): String {
        return "remove <key> - removes element by its id"
    }
}