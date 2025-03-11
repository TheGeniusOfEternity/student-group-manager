package collection

import java.time.LocalDate
import commands.Command
import receiver.Receiver

/**
 * Singleton class-storage of program
 */
object CollectionInfo {
    private val createdDate: LocalDate = LocalDate.now()
    private var elementsCount: Int = 0
    private const val COLLECTION_TYPES: String = "TreeMap"
    private val commandsHistory: Array<String> = Array(11) {""}

    /**
     * @return [elementsCount] of elements in collection
     */
    fun getElementsCount(): Int {
        return elementsCount
    }

    /**
     * update [elementsCount]
     */
    fun updateElementsCount() {
        elementsCount = Receiver.getStudyGroups().size
    }

    /**
     * @return [commandsHistory]
     */
    fun getCommandHistory(): Array<String> {
        return commandsHistory
    }

    /**
     * @param commandName Name of executed [Command]
     */
    fun updateCommandHistory(commandName: String) {
        val currIndex = commandsHistory.indexOfFirst { it.isEmpty() }

        if (currIndex == -1) {
            for (i in 0 until commandsHistory.size - 2) {
                commandsHistory[i] = commandsHistory[i + 1]
            }
            commandsHistory[commandsHistory.size - 1] = commandName
        } else {
            commandsHistory[currIndex] = commandName
        }
    }

    override fun toString(): String {
        return "Type: $COLLECTION_TYPES\n" +
                "Created Date: $createdDate\n" +
                "Elements count: $elementsCount\n" +
                "Commands history: ${commandsList()}\n"
    }

    /**
     * @return [commandsHistory] string representation
     */
    fun commandsList(): String {
        var output = ""
        commandsHistory.forEach {
            if (it.isNotEmpty()) {
                output += "$it, "
            }
        }
        return output
    }
}