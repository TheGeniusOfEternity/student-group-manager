package commands

import handlers.IOHandler
import receiver.Receiver

/**
 * Shows study groups with count of students greater than given
 */
class FilterGreaterStudsCountCmd: Command {
    override fun execute(args: List<Any?>) {
        if (args.size == 1) {
            try {
                val groups = Receiver.getStudyGroups().filter { it.value.getStudentsCount() > (args[0] as String).toLong() }
                if (groups.isEmpty()) {
                    IOHandler printInfoLn "filter_greater_than_students_count error: no group with such amount"
                } else {
                    groups.forEach{ group -> IOHandler printInfoLn group.value.toString() }
                }
            } catch (e: NumberFormatException) {
                IOHandler printInfoLn "filter_greater_than_students_count error: incorrect number format"
            }
        } else {
            IOHandler printInfoLn "filter_greater_than_students_count error: invalid count of arguments"
        }
    }

    override fun describe(): String {
        return "filter_greater_than_students_count <studentsCount> - shows only groups with greater count of students "
    }
}