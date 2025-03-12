import collection.CollectionInfo
import handlers.ReadDataFileHandler
import handlers.ReadScriptFileHandler
import invoker.Invoker
import parsers.InputParser
import receiver.Receiver
import java.io.FileReader
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * A type alias for a list of [Property], used for representing collections of text data.
 */
typealias GroupData = ArrayList<Pair<String, String?>>
/**
 * A type alias for pair (propertyName, propertyValue)
 */
typealias Property = Pair<String, String?>

object State {
    var source: InputSource = InputSource.CONSOLE
}

enum class InputSource {
    FILE, CONSOLE
}

/**
 * Entry point of the program
 */
fun main() {
    val inputParser = InputParser()
    val readScriptFileHandler = ReadScriptFileHandler()
    val readDataFileHandler = ReadDataFileHandler()
    try {
        Receiver.loadFromFile(CollectionInfo.getDefaultFileName())
        while (true) {
            when (State.source) {
                InputSource.FILE -> {
                    val openedFileName = CollectionInfo.getOpenedFileName()
                    if (openedFileName != null) {
                        if (CollectionInfo.getOpenedFileName()!!.first.contains("resources/data/")) {
                            readDataFileHandler.handle(openedFileName.first, openedFileName.second)
                        } else {
                            readScriptFileHandler.handle(openedFileName.first, openedFileName.second)
                        }
                    } else {
                        println("program's state error: no file opened")
                        State.source = InputSource.CONSOLE
                    }

                }
                InputSource.CONSOLE -> {
                    print("& ")
                    inputParser.parseCommand()
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}