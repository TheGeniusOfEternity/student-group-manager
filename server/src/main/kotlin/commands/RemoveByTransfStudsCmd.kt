package commands

import handlers.IOHandler
import receiver.Receiver

/**
 * Removes study groups with given transferred students amount
 */
class RemoveByTransfStudsCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<Any?>) {
        if (args.size == 1) {
            try {
                val groups = Receiver.getStudyGroups().filter { it.value.getTransferredStudents() == (args[0] as String).toLong() }
                if (groups.isEmpty()) {
                    IOHandler printInfoLn "remove_any_by_transferred_students error: no group with such amount"
                } else {
                    Receiver.removeStudyGroup(groups[groups.keys.minOf { it }]!!.getId())
                    IOHandler printInfoLn "group #${groups[groups.keys.minOf { it }]!!.getId()} was successfully removed"
                }
            } catch (e: NumberFormatException) {
                IOHandler printInfoLn "remove_any_by_transferred_students error: invalid number"
            }
        } else {
            IOHandler printInfoLn "remove_any_by_transferred_students error: invalid count of arguments"
        }
    }
    override fun describe(): String {
        return "remove_any_by_transferred_students <transferred_students> - removes element from collection, whose transferred students count equals to given"
    }
}