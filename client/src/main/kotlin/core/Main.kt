package core

import handlers.ConnectionHandler
import handlers.IOHandler
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
 * Entry point of the program
 */
fun main() {
    loadProgram()
    try {
        while (State.isRunning) {
            IOHandler.handle()
            Thread.sleep(100)
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
    IOHandler.loadCredentials()
    IOHandler.getServerAddress()
    IOHandler.getAuthCredentials()
    ConnectionHandler.initializeConnection()
}