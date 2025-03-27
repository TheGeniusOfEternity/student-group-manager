package commands
import State
import handlers.IOHandler

/**
 * Stops program running
 */
class ExitCmd : Command {
    override fun execute(args: List<String>) {
        IOHandler printInfoLn "Exiting console..."
        State.isRunning = false
    }

    override fun describe() {
        IOHandler printInfoLn "exit  - stops program running"
    }
}