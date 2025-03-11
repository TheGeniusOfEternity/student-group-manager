package commands
import collection.StudyGroup
import receiver.Receiver

/**
 * Shows list of [StudyGroup]
 */
class ShowCmd: Command {
    override fun execute(args: List<String>) {
        if (args.isEmpty()) {
            val groups = Receiver.getStudyGroups();
            if (groups.isEmpty()) {
                println("No groups found")
            } else {
                groups.forEach { group ->
                    println(group.value.toString())
                }
            }
        } else {
            println("show: Too many arguments")
        }
    }

    override fun describe() {
        println("show - shows all elements from collection")
    }
}