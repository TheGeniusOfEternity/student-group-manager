import ch.qos.logback.classic.LoggerContext
import handlers.ConnectionHandler
import handlers.IOHandler
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.logging.ConsoleHandler
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
 */
object State {
    var isRunning = false
    var serverConnection = false
    var host = "localhost"
}

/**
 * Entry point of the program
 */
fun main() {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val logger = loggerContext.getLogger("com.rabbitmq.client.impl.ConsumerWorkService")
    logger.level = ch.qos.logback.classic.Level.INFO

    State.isRunning = true
    State.serverConnection = ConnectionHandler.initializeConnection()
    try {
        while (State.isRunning) {
            if (State.serverConnection) {
                IOHandler.handle()
            } else {
                ConnectionHandler.handleConnectionFail()
            }

        }
        exitProcess(0)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}