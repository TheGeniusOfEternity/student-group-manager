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
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 1) {
            Receiver.clearStudyGroups()
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("Collection was successfully cleared")
        } else {
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("clear error: invalid count of arguments")
        }
    }

    override fun describe(): String {
        return "clear: erases all study groups from collection"
    }
}