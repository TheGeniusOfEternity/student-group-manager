package commands

import receiver.Receiver
import collection.StudyGroup
import dao.StudyGroupDao
import dto.CommandParam
import handlers.DatabaseHandler
import handlers.IOHandler

/**
 * Update [StudyGroup] by its id, works similar to [InsertCmd]
 */
class UpdateCmd : Command {
    override val paramTypeName = "StudyGroup"
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 2) {
            val group = (args[0] as CommandParam.StudyGroupParam).value
                if (group != null) {
                    val groupDao = StudyGroupDao(DatabaseHandler.connection!!)
                    if (groupDao.getById(group.getId()) != null) {
                        if (groupDao.getUserByGroupId(group.getId())?.id == (args[1] as CommandParam.LongParam).value!!.toInt()) {
                            if (groupDao.update(group, (args[1] as CommandParam.LongParam).value!!.toInt()) != null) {
                                IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("Successfully updated new group, type 'show' to see all groups")
                            } else {
                                IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("update error: undefined error")
                            }
                        } else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("update error: only creator can update the group")
                    } else {
                        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("update error: group with this id not found, use 'insert'")
                    }
                } else {
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("update error: group data is invalid")
                }
        } else {
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("update error: invalid count of arguments")
        }
    }

    override fun describe(): String {
        return "update <key> {element} - updates element from collection with provided id"
    }
}