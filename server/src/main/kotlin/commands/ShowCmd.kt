package commands
import collection.StudyGroup
import handlers.IOHandler
import receiver.Receiver

/**
 * Shows list of [StudyGroup]
 */
class ShowCmd: Command {
    override fun execute(args: List<Any?>) {
        if (args.isEmpty()) {
            val groups = Receiver.getStudyGroups();
            if (groups.isEmpty()) {
                IOHandler printInfoLn "No groups found"
            } else {
                groups.forEach { group ->
                    IOHandler printInfoLn group.value.toString()
                }
            }
        } else {
            IOHandler printInfoLn "show: Too many arguments"
        }
    }

    override fun describe() {
        IOHandler printInfoLn "show - shows all elements from collection"
    }
}