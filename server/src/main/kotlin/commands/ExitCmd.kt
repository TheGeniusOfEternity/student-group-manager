package commands
import State
import handlers.IOHandler

/**
 * Stops program running
 */
class ExitCmd : Command {
    override fun execute(args: List<Any?>) {
        IOHandler printInfoLn "Exiting console..."
        State.isRunning = false
    }

    override fun describe(): String {
        return "exit  - stops program running"
    }
}