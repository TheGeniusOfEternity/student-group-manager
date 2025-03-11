package commands

import kotlin.system.exitProcess

/**
 * Stops program running
 */
class ExitCmd : Command {
    override fun execute(args: List<String>) {
        println("Exiting console...")
        exitProcess(0)
    }

    override fun describe() {
        println("exit - stops program running")
    }
}