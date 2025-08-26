package commands
import collection.StudyGroup
import dto.CommandParam
import handlers.ConnectionHandler
import handlers.IOHandler
import receiver.Receiver
import kotlin.collections.ArrayList

/**
 * Shows list of [StudyGroup]
 */
class ShowCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        val responseMsg: String
        if (args.size == 1) {
            val groups = Receiver.getStudyGroups()
            if (groups.isEmpty()) {
                responseMsg = "No groups found"
                ConnectionHandler.handleResponse<ArrayList<StudyGroup>>(clientId, ArrayList(), correlationId)
            } else {
                val groupsData = ArrayList<StudyGroup>()
                groups.forEach { group ->
                    groupsData.add(group.value)
                }
                ConnectionHandler.handleResponse<ArrayList<StudyGroup>>(clientId, groupsData, correlationId)
                return
            }
        } else {
            ConnectionHandler.handleResponse<ArrayList<StudyGroup>>(clientId, ArrayList(), correlationId)
            responseMsg = "show: Too many arguments"
        }
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(Pair(responseMsg.removeSuffix("\n\n"), correlationId))
    }

    override fun describe(): String {
        return "show - shows all elements from collection"
    }
}