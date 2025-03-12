package commands

import receiver.Receiver

/**
 * Removes all elements from collection, whose id lower than given
 */
class RemoveLowerCmd: Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            val comparedGroup = Receiver.getStudyGroup(args[0].toLong())
            if (comparedGroup != null) {
                val groups = Receiver.getStudyGroups().filter { it.value < comparedGroup }
                if (groups.isNotEmpty()) {
                    groups.forEach { group ->
                        Receiver.removeStudyGroup(key = group.key)
                    }
                    println("All groups with studentsCount less than ${comparedGroup.getStudentsCount()} were successfully removed")
                } else {
                    println("remove_lower error: no less groups found")
                }
            } else {
                println("remove_lower error: group #${args[0]} not found")
            }

        } else {
            println("remove_lower error: invalid count of arguments")
        }
    }

    override fun describe() {
        println("remove_lower <key> - removes all elements from collection, whose id is lower than given")
    }
}