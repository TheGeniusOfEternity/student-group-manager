package commands

import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Removes all elements from collection, whose id lower than given
 */
class RemoveLowerCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<CommandParam?>) {
        if (args.size == 1) {
            val comparedGroup = Receiver.getStudyGroup((args[0] as String).toLong())
            if (comparedGroup != null) {
                val groups = Receiver.getStudyGroups().filter { it.value < comparedGroup }
                if (groups.isNotEmpty()) {
                    groups.forEach { group ->
                        Receiver.removeStudyGroup(key = group.key)
                    }
                    IOHandler printInfoLn "All groups with studentsCount less than ${comparedGroup.getStudentsCount()} were successfully removed"
                } else {
                    IOHandler printInfoLn "remove_lower error: no less groups found"
                }
            } else {
                IOHandler printInfoLn "remove_lower error: group #${(args[0] as String)} not found"
            }

        } else {
            IOHandler printInfoLn "remove_lower error: invalid count of arguments"
        }
    }

    override fun describe(): String {
        return "remove_lower <key> - removes all elements from collection, whose id is lower than given"
    }
}