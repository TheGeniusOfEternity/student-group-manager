package commands

import handlers.IOHandler
import receiver.Receiver

/**
 * Shows only unique average marks from all study groups
 */
class PrintUniqueAvgMarkCmd: Command {
    override fun execute(args: List<String>) {
        if (args.isEmpty()) {
            val avgMarks = mutableListOf<Int?>()
            Receiver.getStudyGroups().forEach { group ->
                val mark = group.value.getAverageMark()
                if (mark != null) {
                    avgMarks.add(mark.toInt())
                }
            }
            IOHandler printInfoLn "All unique marks: $avgMarks"
        } else {
            IOHandler printInfoLn "print_unique_average_mark error: invalid count of arguments"
        }
    }

    override fun describe() {
        IOHandler printInfoLn "print_unique_average_mark - prints all unique average marks from all study groups"
    }
}