package handlers

import State
import collection.CollectionInfo
import parsers.InputParser
import java.io.FileReader

object InputHandler : Handler<String, Int?> {
    override fun handle(data: String, option: Int?) {
        try {
            CollectionInfo.addOpenedFile(Pair(data, option))
            val fileReader = FileReader(data)
            if (CollectionInfo.getOpenedFiles().lastElement().first.contains("data/")) {
                InputParser.parse(fileReader)
            } else {
                InputParser.parseScript(fileReader, data)
            }
            CollectionInfo.removeOpenedFile()
        } catch (e: Exception) {
            println("input error: ${e.message}")
        }
        State.source = InputSource.CONSOLE
    }
}