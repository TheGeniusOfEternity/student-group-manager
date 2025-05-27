import handlers.ConnectionHandler
import handlers.DatabaseHandler
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
    @Volatile
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
        DatabaseHandler.setUp()
        Receiver.loadFromDatabase()
        while (State.isRunning) {
            if (IOHandler.responsesThreads.isNotEmpty()) {
                val clientsToRemove = mutableListOf<String>()
                IOHandler printInfoLn IOHandler.responsesThreads.keys.toString()
                IOHandler.responsesThreads.forEach { (clientId, responseThread) ->
                    responseThread.forEach{ response ->
                        if (response != null) {
                            clientsToRemove.add(clientId)
                            ConnectionHandler.handleResponse(clientId, response.first, response.second)
                        }
                    }
                }
                clientsToRemove.forEach { IOHandler.responsesThreads.remove(it) }
            }
            Thread.sleep(100)
        }
        exitProcess(0)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}