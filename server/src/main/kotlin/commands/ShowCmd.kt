package commands
import collection.StudyGroup
import dto.CommandParam
import handlers.ConnectionHandler
import handlers.IOHandler
import receiver.Receiver
import java.util.ArrayList

/**
 * Shows list of [StudyGroup]
 */
class ShowCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>) {
        var responseMsg = "Collection info: \n\n"
        if (args.isEmpty()) {
            val groups = Receiver.getStudyGroups();
            if (groups.isEmpty()) {
                responseMsg = "No groups found"
            } else {
                groups.forEach { group ->
                    responseMsg += group.value.toString()
                }
            }
        } else {
            responseMsg = "show: Too many arguments"
        }
        IOHandler.responsesThread.add(responseMsg)
    }

    override fun describe(): String {
        return "show - shows all elements from collection"
    }
}