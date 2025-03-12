package commands
import State

/**
 * Stops program running
 */
class ExitCmd : Command {
    override fun execute(args: List<String>) {
        println("Exiting console...")
        State.isRunning = false
    }

    override fun describe() {
        println("exit - stops program running")
    }
}