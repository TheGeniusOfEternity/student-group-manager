package collection

import java.time.LocalDate

class CollectionInfo (
    private val createdDate: LocalDate = LocalDate.now(),
    private var elementsCount: Int = 0,
    private val collectionType: String = "TreeMap",
    private val commandsHistory: Array<String> = Array(11) {""}
) {
    init {
        require(elementsCount >= 0) { "elementsCount can't be negative" }
    }

    fun getElementCount(): Int {
        return elementsCount
    }

    fun incrementElementCount() {
        elementsCount++
    }

    fun updateCommandHistory(commandName: String) {
        var currIndex = commandsHistory.indexOfFirst { it.isEmpty() }

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
        return "Type: $collectionType\n" +
                "Created Date: $createdDate\n" +
                "Elements count: $elementsCount\n" +
                "Commands history: ${commandsHistory.commandsList()}\n"
    }

    private fun <T>Array<T>.commandsList(): String {
        var output = ""
        commandsHistory.forEach {
            if (it.isNotEmpty()) {
                output += "$it, "
            }
        }
        return output
    }
}