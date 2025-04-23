import handlers.ConnectionHandler
import handlers.IOHandler
import java.io.IOException
import java.util.concurrent.Phaser
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
 * @property connectedToServer - is client connect to server, or not
 */
object State {
    var isRunning = false
    var connectedToServer = false
    var host = "localhost"
    var tasks = 1
}

/**
 * Entry point of the program
 */
fun main() {
    loadProgram()
    try {
        while (State.isRunning) {
            IOHandler.handle()
        }
        exitProcess(0)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

/**
 * Sets program's logging, initiates connection to server & enables [State.isRunning]
 */
fun loadProgram() {
    State.isRunning = true
    ConnectionHandler.initializeConnection()
}