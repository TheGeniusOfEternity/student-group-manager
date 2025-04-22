package commands

import collection.StudyGroup
import handlers.IOHandler
import receiver.Receiver

/**
 * Removes [StudyGroup] from [Receiver.stdGroupCollection]
 */
class RemoveCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<Any?>) {
        if (args.size == 1) {
            try {
                if (Receiver.getStudyGroup((args[0] as String).toLong()) == null) {
                    IOHandler printInfoLn "remove error: group with id ${(args[0] as String)} not found"
                } else {
                    Receiver.removeStudyGroup((args[0] as String).toLong())
                    IOHandler printInfoLn "Group #${(args[0] as String)} was removed"
                }
            } catch (e: NumberFormatException) {
                IOHandler printInfoLn "${(args[0] as String)} is not a correct number, must be Long"
            }
        } else {
            IOHandler printInfoLn "remove: invalid count of arguments."
        }
    }

    override fun describe(): String {
        return "remove <key> - removes element by its id"
    }
}