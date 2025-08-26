package commands

import receiver.Receiver
import collection.StudyGroup
import dao.StudyGroupDao
import dto.CommandParam
import handlers.IOHandler

/**
 * Update [StudyGroup] by its id, works similar to [InsertCmd]
 */
class UpdateCmd : Command {
    override val paramTypeName = "StudyGroup"
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        val responseMsg: String
        if (args.size == 2) {
            val group = (args[0] as CommandParam.StudyGroupParam).value
                if (group != null) {
                    val groupToUpdate = Receiver.getStudyGroup(group.getId())
                    responseMsg = if (groupToUpdate != null) {
                        if (Receiver.getUser(groupToUpdate.getOwnerName())?.id == (args[1] as CommandParam.LongParam).value!!.toInt()) {
                            if (StudyGroupDao.update(group, (args[1] as CommandParam.LongParam).value!!.toInt()) != null) {
                                "Successfully updated new group, type 'show' to see all groups"
                            } else {
                                "update error: undefined error"
                            }
                        } else "update error: only creator can update his group"
                    } else {
                        "update error: group with this id not found, use 'insert'"
                    }
                } else {
                    responseMsg = "update error: group data is invalid"
                }
        } else {
            responseMsg = "update error: invalid count of arguments"
        }
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(Pair(responseMsg, correlationId))
    }

    override fun describe(): String {
        return "update <key> {element} - updates element from collection with provided id"
    }
}