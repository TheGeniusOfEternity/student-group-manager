package commands
import receiver.Receiver
/**
 * Clears [Receiver.stdGroupCollection]
 */
class ClearCmd: Command {
    override fun execute(args: List<String>) {
       if (args.isEmpty()) {
           Receiver.clearStudyGroups()
           println("Collection was successfully cleared")
       } else {
           println("clear: invalid count of arguments")
       }
    }

    override fun describe() {
        println("clear: erases all study groups from collection")
    }
}