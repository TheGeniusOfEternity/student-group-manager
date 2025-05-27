package commands
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver
import java.util.ArrayList

/**
 * Clears [Receiver.stdGroupCollection]
 */
class ClearCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        val responseMsg: String
        if (args.size == 1) {
            Receiver.clearStudyGroups()
            responseMsg = "Collection was successfully cleared"
        } else responseMsg = "clear error: invalid count of arguments"
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(Pair(responseMsg, correlationId))
    }

    override fun describe(): String {
        return "clear: erases all study groups from collection"
    }
}