package commands

import collection.StudyGroup
import receiver.Receiver

/**
 * Removes [StudyGroup] from [Receiver.stdGroupCollection]
 */
class RemoveCmd: Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            try {
                if (Receiver.getStudyGroup(args[0].toLong()) == null) {
                    println("remove error: group with id ${args[0]} not found")
                } else {
                    Receiver.removeStudyGroup(args[0].toLong())
                    println("Group #${args[0]} was removed")
                }
            } catch (e: NumberFormatException) {
                println("${args[0]} is not a correct number, must be Long")
            }
        } else {
            println("remove: invalid count of arguments.")
        }
    }

    override fun describe() {
        println("remove <key> - removes element by its id")
    }
}