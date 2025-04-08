import ch.qos.logback.classic.LoggerContext
import handlers.ConnectionHandler
import handlers.IOHandler
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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
 * @property connectedToServer - is client connect to server, or not
 */
object State {
    var isRunning = false
    var connectedToServer = false
    var host = "localhost"
}

/**
 * Entry point of the program
 */
fun main() {
    loadProgram()

    if (State.connectedToServer) IOHandler printInfoLn "Connection established"
    try {
        while (State.isRunning) {
            if (State.connectedToServer) {
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

/**
 * Sets program's logging, initiates connection to server & enables [State.isRunning] flag true
 */
fun loadProgram() {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val logger = loggerContext.getLogger("com.rabbitmq.client.impl.ConsumerWorkService")
    logger.level = ch.qos.logback.classic.Level.INFO
    State.isRunning = true
    ConnectionHandler.initializeConnection()
    val latch = CountDownLatch(1)
    latch.await(100, TimeUnit.MILLISECONDS)
}