package commands

import receiver.Receiver

/**
 * Shows study groups with count of students greater than given
 */
class FilterGreaterStudsCountCmd: Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            try {
                val groups = Receiver.getStudyGroups().filter { it.value.getStudentsCount() > args[0].toLong() }
                if (groups.isEmpty()) {
                    println("filter_greater_than_students_count error: no group with such amount")
                } else {
                    groups.forEach{ group -> println(group.value.toString()) }
                }
            } catch (e: NumberFormatException) {
                println("filter_greater_than_students_count error: incorrect number format")
            }
        } else {
            println("filter_greater_than_students_count error: invalid count of arguments")
        }
    }

    override fun describe() {
        println("filter_greater_than_students_count <studentsCount> - shows only groups with greater count of students ")
    }
}