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
import kotlin.system.exitProcess

/**
 * A type alias for a list of [Property], used for representing collections of text data.
 */
typealias GroupData = ArrayList<Pair<String, String?>>
/**
 * A type alias for pair (propertyName, propertyValue)
 */
typealias Property = Pair<String, String?>

/**
 * Singleton object for storing program state
 * @property source - data source from what program should read data
 * @property isRunning - check if program is running or not
 */
object State {
    var source: InputSource = InputSource.CONSOLE
    var isRunning = false
}

enum class InputSource {
    FILE, CONSOLE
}

/**
 * Entry point of the program
 */
fun main() {
    val readScriptFileHandler = ReadScriptFileHandler()
    val readDataFileHandler = ReadDataFileHandler()
    State.isRunning = true
    try {
        Receiver.loadFromFile(CollectionInfo.getDefaultFileName())
        while (State.isRunning) {
            when (State.source) {
                InputSource.FILE -> {
                    val openedFiles = CollectionInfo.getOpenedFiles()
                    if (openedFiles.size != 0) {
                        if (openedFiles.lastElement().first.contains("data/")) {
                            readDataFileHandler.handle(
                                openedFiles.lastElement().first,
                                openedFiles.lastElement().second,
                            )
                        } else {
                            readScriptFileHandler.handle(
                                openedFiles.lastElement().first,
                                openedFiles.lastElement().second,
                            )
                        }
                    } else {
                        println("program's state error: no file opened")
                        State.source = InputSource.CONSOLE
                    }
                }
                InputSource.CONSOLE -> {
                    print("& ")
                    InputParser.parseCommand()
                }
            }
        }
        exitProcess(0)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}