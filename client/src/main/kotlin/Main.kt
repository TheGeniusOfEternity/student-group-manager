import ch.qos.logback.classic.LoggerContext
import handlers.IOHandler
import org.slf4j.LoggerFactory
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
 */
object State {
    var isRunning = false
}

/**
 * Entry point of the program
 */
fun main() {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val logger = loggerContext.getLogger("com.rabbitmq.client.impl.ConsumerWorkService")
    logger.level = ch.qos.logback.classic.Level.INFO

    State.isRunning = true
    try {
        while (State.isRunning) {
            IOHandler.handle()
        }
        exitProcess(0)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}