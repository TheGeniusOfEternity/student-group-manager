package commands

import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Shows study groups with count of students greater than given
 */
class FilterGreaterStudsCountCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<CommandParam?>) {
        if (args.size == 1) {
            val amount = (args[0] as CommandParam.LongParam).value
            if (amount != null) {
                val groups = Receiver.getStudyGroups().filter { it.value.getStudentsCount() > amount }
                if (groups.isEmpty()) {
                    IOHandler.responsesThread.add("filter_greater_than_students_count error: no group with such amount")
                } else {
                    groups.forEach{ group ->
                        IOHandler.responsesThread.add(group.value.toString())
                    }
                }
            } else IOHandler.responsesThread.add("filter_greater_than_students_count error:  is not a number")
        } else {
            IOHandler.responsesThread.add("filter_greater_than_students_count error: invalid count of arguments")
        }
    }

    override fun describe(): String {
        return "filter_greater_than_students_count <studentsCount> - shows only groups with greater count of students "
    }
}