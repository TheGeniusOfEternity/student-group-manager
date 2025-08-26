package commands

import dao.StudyGroupDao
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Removes all elements from collection, whose id lower than given
 */
class RemoveLowerCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        var responseMsg: String
        if (args.size == 2) {
            val id = (args[0] as CommandParam.LongParam).value
            if (id != null) {
                val comparedGroup = Receiver.getStudyGroup(id)
                if (comparedGroup != null) {
                    val groups = Receiver.getStudyGroups().filter { it.value < comparedGroup }
                    if (groups.isNotEmpty()) {
                        groups.forEach { group ->
                            responseMsg = if (Receiver.getUser(group.value.getOwnerName())?.id == (args[1] as CommandParam.LongParam).value!!.toInt()) {
                                try  {
                                    StudyGroupDao.delete(id.toInt())
                                    "Successfully removed group #$id"
                                } catch (e: Exception) {
                                    "remove_lower error: undefined error"
                                }
                            } else "remove_lower error: group #${group.key} - only creator can delete his group"
                            StudyGroupDao.delete(group.key.toInt())
                        }
                        responseMsg = "All groups with studentsCount less than ${comparedGroup.getStudentsCount()} were successfully removed"
                    } else responseMsg = "remove_lower error: no less groups found"
                } else responseMsg = "remove_lower error: group #$id not found"
            } else responseMsg = "remove_lower error: provided id is not a number"
        } else responseMsg = "remove_lower error: invalid count of arguments."
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(Pair(responseMsg, correlationId))
    }

    override fun describe(): String {
        return "remove_lower <group_id> - removes all elements from collection, whose studentsCount is lower than group's given"
    }
}