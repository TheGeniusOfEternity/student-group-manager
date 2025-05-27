package commands
import collection.StudyGroup
import dao.StudyGroupDao
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Starts insertion process: user manually step by step enters new [StudyGroup] data
 */
class InsertCmd : Command {
    override val paramTypeName = "StudyGroup"
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        val responseMsg: String
        if (args.size == 2) {
            val group = (args[0] as CommandParam.StudyGroupParam).value
            if (group != null) {
                if (Receiver.getStudyGroup(group.getId()) != null) {
                    responseMsg = "insert error: group already exists. Type 'update' to rewrite the group"
                } else {
                    val groupId = StudyGroupDao.insert(group, (args[1] as CommandParam.LongParam).value!!.toInt())
                    responseMsg = if (groupId != null) "Successfully added new group, type 'show' to see all groups"
                    else "insert error: SQL query failed"
                }
            } else {
                responseMsg = "insert error: group data can't be validated"
            }
        } else {
            responseMsg = "insert: invalid count of arguments"
        }
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(Pair(responseMsg, correlationId))
    }

    override fun describe(): String {
        return "insert <key> {element} - adds to collection element with <key> id"
    }
}