import collection.CollectionInfo
import handlers.ConnectionHandler
import handlers.IOHandler
import receiver.Receiver
import java.io.IOException
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
    ConnectionHandler.initializeConnection()
    try {
        Receiver.loadFromFile(CollectionInfo.getDefaultFileName())
        while (State.isRunning) {
            ConnectionHandler.handleRequests()
        }
        exitProcess(0)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}