package commands

import collection.StudyGroup
import dao.StudyGroupDao
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Removes [StudyGroup] from [Receiver.stdGroupCollection]
 */
class RemoveCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        val responseMsg: String
        if (args.size == 2) {
            val id = (args[0] as CommandParam.LongParam).value
            if (id != null) {
                val group = Receiver.getStudyGroup(id)
                responseMsg = if (group != null) {
                    if (Receiver.getUser(group.getUserId())?.id == (args[1] as CommandParam.LongParam).value!!.toInt()) {
                        try  {
                            StudyGroupDao.delete(id.toInt())
                            "Successfully removed group #$id, type 'show' to see all groups"
                        } catch (e: Exception) {
                            "remove error: undefined error"
                        }
                    } else "remove error: only creator can delete his group"
                } else {
                    "remove error: group with this id not found"
                }
            } else {
                responseMsg = "remove error: provided id is not a number"
            }
        } else {
            responseMsg = "remove error: invalid count of arguments."
        }
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
            .add(Pair(responseMsg, correlationId))
    }

    override fun describe(): String {
        return "remove <key> - removes element by its id"
    }
}