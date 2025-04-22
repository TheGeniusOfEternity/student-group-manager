package commands
import handlers.IOHandler
import receiver.Receiver
/**
 * Clears [Receiver.stdGroupCollection]
 */
class ClearCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<Any?>) {
       if (args.isEmpty()) {
           Receiver.clearStudyGroups()
           IOHandler printInfoLn "Collection was successfully cleared"
       } else {
           IOHandler printInfoLn "clear error: invalid count of arguments"
       }
    }

    override fun describe(): String {
        return "clear: erases all study groups from collection"
    }
}