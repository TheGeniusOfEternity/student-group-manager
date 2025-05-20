package commands

import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Removes all elements from collection, whose id lower than given
 */
class RemoveLowerCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 2) {
            val id = (args[0] as CommandParam.LongParam).value
            if (id != null) {
                val comparedGroup = Receiver.getStudyGroup(id)
                if (comparedGroup != null) {
                    val groups = Receiver.getStudyGroups().filter { it.value < comparedGroup }
                    if (groups.isNotEmpty()) {
                        groups.forEach { group ->
                            Receiver.removeStudyGroup(key = group.key)
                        }
                        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("All groups with studentsCount less than ${comparedGroup.getStudentsCount()} were successfully removed")
                    } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("remove_lower error: no less groups found")
                } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("remove_lower error: group #$id not found")
            } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("remove_lower error: provided id is not a number")
        } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("remove_lower error: invalid count of arguments.")
    }

    override fun describe(): String {
        return "remove_lower <key> - removes all elements from collection, whose studentsCount is lower than given"
    }
}