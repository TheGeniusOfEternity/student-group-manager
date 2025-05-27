package commands

import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Shows study groups with count of students greater than given
 */
class FilterGreaterStudsCountCmd: Command {
    override val paramTypeName = "Long"
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        var responseMsg = ""
        if (args.size == 2) {
            val amount = (args[0] as CommandParam.LongParam).value
            if (amount != null) {
                val groups = Receiver.getStudyGroups().filter { it.value.getStudentsCount() > amount }
                if (groups.isEmpty()) {
                    responseMsg = "filter_greater_than_students_count error: no group with such amount"
                } else {
                    groups.forEach{ group ->
                        responseMsg = group.value.toString()
                    }
                }
            } else responseMsg = "filter_greater_than_students_count error:  is not a number"
        } else {
            responseMsg = "filter_greater_than_students_count error: invalid count of arguments"
        }
        IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(Pair(responseMsg, correlationId))
    }

    override fun describe(): String {
        return "filter_greater_than_students_count <studentsCount> - shows only groups with greater count of students "
    }
}