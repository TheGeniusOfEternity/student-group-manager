package commands

import dao.StudyGroupDao
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Removes study groups with given transferred students amount
 */
class RemoveByTransferredStudentsCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        val responseMsg: String
        if (args.size == 2) {
            val count = (args[0] as CommandParam.LongParam).value
            val groups = Receiver.getStudyGroups().filter { it.value.getTransferredStudents() == count }
            if (groups.isEmpty()) {
                responseMsg = "remove_any_by_transferred_students error: no group with such amount"
            } else {
                val group = groups[groups.keys.minOf { it }]
                responseMsg = if (group != null) {
                    if (Receiver.getUser(group.getOwnerName())?.id == (args[1] as CommandParam.LongParam).value!!.toInt()) {
                        try  {
                            StudyGroupDao.delete(group.getId().toInt())
                            "Successfully removed group #${group.getId()}, type 'show' to see all groups"
                        } catch (e: Exception) {
                            "remove_any_by_transferred_students error: undefined error"
                        }
                    } else "remove_any_by_transferred_students error: only creator can delete his group"
                } else {
                    "remove_any_by_transferred_students error: group with this id not found"
                }
            }
        } else responseMsg = "remove_any_by_transferred_students error: invalid count of arguments"
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(Pair(responseMsg, correlationId))
    }
    override fun describe(): String {
        return "remove_any_by_transferred_students <transferred_students> - remove first element from collection, whose transferred students count equals to given"
    }
}