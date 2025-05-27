package commands

import dto.CommandParam
import handlers.IOHandler
import receiver.Receiver

/**
 * Shows only unique average marks from all study groups
 */
class PrintUniqueAvgMarkCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        if (args.size == 1) {
            val avgMarks = mutableListOf<Int?>()
            Receiver.getStudyGroups().forEach { group ->
                val mark = group.value.getAverageMark()
                if (mark != null) {
                    avgMarks.add(mark.toInt())
                }
            }
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
                .add(Pair("All unique marks: $avgMarks", correlationId))
        } else {
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }
                .add(Pair("print_unique_average_mark error: invalid count of arguments", correlationId))
        }
    }

    override fun describe(): String {
        return "print_unique_average_mark - prints all unique average marks from all study groups"
    }
}