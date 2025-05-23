package commands
import collection.StudyGroup
import dao.StudyGroupDao
import dto.CommandParam
import handlers.DatabaseHandler
import handlers.IOHandler
import receiver.Receiver

/**
 * Starts insertion process: user manually step by step enters new [StudyGroup] data
 */
class InsertCmd : Command {
    override val paramTypeName = "StudyGroup"
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 2) {
            val group = (args[0] as CommandParam.StudyGroupParam).value
            if (group != null) {
                val groupDao = StudyGroupDao(DatabaseHandler.connection!!)
                if (groupDao.getById(group.getId()) != null) {
                    IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("insert error: group already exists. Type 'update' to rewrite the group")
                } else {
                    val groupId = groupDao.insert(group, (args[1] as CommandParam.LongParam).value!!.toInt())
                    if (groupId != null) IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("Successfully added new group, type 'show' to see all groups")
                    else IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("insert error: SQL query failed")
                }
            } else {
                IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("insert error: group data can't be validated")
            }
        } else {
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("insert: invalid count of arguments")
        }
    }

    override fun describe(): String {
        return "insert <key> {element} - adds to collection element with <key> id"
    }
}