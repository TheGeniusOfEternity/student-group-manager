package collection

import java.time.LocalDate
import commands.Command
import receiver.Receiver
import java.util.*

/**
 * Singleton class-storage of program
 */
object CollectionInfo {
    private val createdDate: LocalDate = LocalDate.now()
    private var elementsCount: Int = 0
    private const val COLLECTION_TYPE: String = "TreeMap"
    private val commandsHistory: Array<String> = Array(11) {""}
    private var defaultFileName: String = "data/src.csv"
    private var openedFiles: Stack<Pair<String, Int?>> = Stack()

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
     * Add new opened file into [openedFiles]
     */
    fun addOpenedFile(newFile: Pair<String, Int?>) {
        openedFiles.add(newFile)
    }

    /**
     * Get currently opened files
     * @return [openedFiles]
     */
    fun getOpenedFiles(): Stack<Pair<String, Int?>> {
        return openedFiles
    }


    /**
     * Clears opened filename on file's close
     */
    fun removeOpenedFile() {
        openedFiles.removeLast()
    }

    /**
     * Update [elementsCount]
     */
    fun updateElementsCount() {
        elementsCount = Receiver.getStudyGroups().size
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
                "Default file name: $defaultFileName"
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