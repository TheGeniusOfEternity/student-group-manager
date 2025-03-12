package commands

import receiver.Receiver

class RemoveByTransfStudsCmd: Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            try {
                val groups = Receiver.getStudyGroups().filter { it.value.getTransferredStudents() == args[0].toLong() }
                if (groups.isEmpty()) {
                    println("remove_any_by_transferred_students error: no group with such amount")
                } else {
                    Receiver.removeStudyGroup(groups[groups.keys.minOf { it }]!!.getId())
                    println("group #${groups[groups.keys.minOf { it }]!!.getId()} was successfully removed")
                }


            } catch (e: NumberFormatException) {
                println("remove_any_by_transferred_students error: invalid number")
            }
        } else {
            println("remove_any_by_transferred_students error: invalid count of arguments")
        }
    }
    override fun describe() {
        println("remove_any_by_transferred_students <transferred_students> - removes element from collection, whose transferred students count equals to given")
    }
}