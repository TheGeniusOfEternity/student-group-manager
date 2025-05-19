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
 * @property isRunning - check if program is running or not
 * @property isConnectionFailNotified - check if user received error message on connection fail
 * @property credentials - storage for all auth info
 */
object State {
    var isRunning = false
    var isConnectionFailNotified = false
    var credentials: HashMap<String, String> = HashMap()
}

/**
 * Entry point of the program
 */
fun main() {
    IOHandler.loadCredentials()
    ConnectionHandler.initializeConnection()
    try {
        Receiver.loadFromFile(CollectionInfo.getDefaultFileName())
        while (State.isRunning) {
            if (IOHandler.responsesThreads.isNotEmpty()) {
                IOHandler printInfoLn IOHandler.responsesThreads.keys.toString()
                IOHandler.responsesThreads.forEach { (clientId, responseThread) ->
                    ConnectionHandler.handleResponse(clientId, responseThread)
                }
            }
            Thread.sleep(100)
        }
        exitProcess(0)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}