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
    private const val COLLECTION_TYPE: String = "TreeMap"
    private val commandsHistory: Array<String> = Array(11) {""}
    private var defaultFileName: String = "src/main/resources/data/src.csv"
    private var openedFileName: Pair<String, Int?>? = null
    /**
     * Get count of elements in collection
     * @return [elementsCount] of elements in collection
     */
    fun getElementsCount(): Int {
        return elementsCount
    }

    /**
     * Update [defaultFileName]
     */
    fun updateDefaultFileName(newFileName: String) {
        defaultFileName = newFileName
    }

    /**
     * Get output file name by default
     * @return [defaultFileName]
     */
    fun getDefaultFileName(): String {
        return defaultFileName
    }

    /**
     * Update [openedFileName]
     */
    fun setOpenedFilename(newFileName: Pair<String, Int?>) {
        openedFileName = newFileName
    }

    /**
     * Get output file name by default
     * @return [openedFileName]
     */
    fun getOpenedFileName(): Pair<String, Int?>? {
        return openedFileName
    }

    /**
     * Clears opened filename on file's close
     */
    fun removeOpenedFileName() {
        openedFileName = null
    }

    /**
     * Update [elementsCount]
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

    /**
     * Get info about collection
     * @return  [CollectionInfo] string representation
     */
    override fun toString(): String {
        return "Type: $COLLECTION_TYPE\n" +
                "Created Date: $createdDate\n" +
                "Elements count: $elementsCount\n" +
                "Commands history: ${commandsList()}\n" +
                "Default file name: $defaultFileName\n" +
                "Opened file name: ${openedFileName?.first}\n"
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