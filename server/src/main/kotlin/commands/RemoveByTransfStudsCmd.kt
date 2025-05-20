package commands

import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Removes study groups with given transferred students amount
 */
class RemoveByTransfStudsCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 2) {
            val count = (args[0] as CommandParam.LongParam).value
            val groups = Receiver.getStudyGroups().filter { it.value.getTransferredStudents() == count }
            if (groups.isEmpty()) {
                IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("remove_any_by_transferred_students error: no group with such amount")
            } else {
                Receiver.removeStudyGroup(groups[groups.keys.minOf { it }]!!.getId())
                IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("Group #${groups[groups.keys.minOf { it }]!!.getId()} was successfully removed")
            }
        } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("remove_any_by_transferred_students error: invalid count of arguments")
    }
    override fun describe(): String {
        return "remove_any_by_transferred_students <transferred_students> - removes first element from collection, whose transferred students count equals to given"
    }
}