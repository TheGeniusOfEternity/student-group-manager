package commands

import receiver.Receiver

/**
 * Removes all elements from collection, whose id lower than given
 */
class RemoveLowerCmd: Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            val groups = Receiver.getStudyGroups().filter { it.key < args[0].toInt() }
            if (groups.isNotEmpty()) {
                groups.forEach { group ->
                    Receiver.removeStudyGroup(key = group.key)
                }
                println("All groups with id's less that ${args[0]} were successfully removed")
            } else {
                println("remove_lower error: no less groups found")
            }
        } else {
            println("remove_lower error: invalid count of arguments")
        }
    }

    override fun describe() {
        println("remove_lower <key> - removes all elements from collection, whose id is lower than given")
    }
}