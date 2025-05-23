package commands
import collection.StudyGroup
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver
import java.util.ArrayList

/**
 * Shows list of [StudyGroup]
 */
class ShowCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>, clientId: String) {
        var responseMsg = "Collection info: \n\n"
        if (args.size == 1) {
            val groups = Receiver.getStudyGroups()
            val users = Receiver.getUsers()
            if (groups.isEmpty()) {
                responseMsg = "No groups found"
            } else {
                IOHandler printInfoLn users.toString()
                groups.forEach { group ->
                    val creatorName = users.entries.find { user -> user.value.id == group.value.getUserId() }?.value?.username
                    responseMsg += "${group.value}\nCreated by '$creatorName'\n\n"
                }
            }
        } else {
            responseMsg = "show: Too many arguments"
        }
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(responseMsg.removeSuffix("\n\n"))
    }

    override fun describe(): String {
        return "show - shows all elements from collection"
    }
}