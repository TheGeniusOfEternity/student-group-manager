package commands
import State
import handlers.IOHandler

/**
 * Stops program running
 */
class ExitCmd : Command {
    override val description: String = "exit  - stops program running"
    override fun execute(args: List<String>) {
        IOHandler printInfoLn "Exiting console..."
        State.isRunning = false
    }

    override fun describe() {
        IOHandler printInfoLn description
    }
}