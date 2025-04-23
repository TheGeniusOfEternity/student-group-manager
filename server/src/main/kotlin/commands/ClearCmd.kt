package commands
import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver
/**
 * Clears [Receiver.stdGroupCollection]
 */
class ClearCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>) {
       if (args.isEmpty()) {
           Receiver.clearStudyGroups()
           IOHandler.responsesThread.add("Collection was successfully cleared")
       } else {
           IOHandler.responsesThread.add("clear error: invalid count of arguments")
       }
    }

    override fun describe(): String {
        return "clear: erases all study groups from collection"
    }
}