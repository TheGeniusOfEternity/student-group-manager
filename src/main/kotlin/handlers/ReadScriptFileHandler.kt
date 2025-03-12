package handlers

import collection.CollectionInfo
import commands.Command
import parsers.InputParser
import java.io.FileReader
import java.io.IOException

class ReadScriptFileHandler: Handler<String, Int?> {
    override fun handle(data: String, option: Int?): Boolean {
        try {
            val fileReader = FileReader(data)
            CollectionInfo.setOpenedFilename(Pair(data, option))
            val inputParser = InputParser()
            inputParser.parseScript(fileReader)
            return true

        } catch (e: IOException) {
            println("read $data error: no such file found")
            return false
        }
    }
}