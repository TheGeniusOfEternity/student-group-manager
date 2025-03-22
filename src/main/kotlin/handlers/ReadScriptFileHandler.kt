package handlers

import collection.CollectionInfo
import parsers.InputParser
import java.io.FileReader
import java.io.IOException

/**
 * Reads data from script files
 */
class ReadScriptFileHandler: Handler<String, Int?> {
    /**
     * Reads script files
     * @param data Filename
     * @param option Lines of file count, that were already read
     * @return Was file read or not
     */
    override fun handle(data: String, option: Int?): Boolean {
        try {
            val fileReader = FileReader(data)
            CollectionInfo.addOpenedFile(Pair(data, option))
            InputParser.parseScript(fileReader, data)
            return true

        } catch (e: IOException) {
            println("read $data error: no such file found")
            return false
        }
    }
}