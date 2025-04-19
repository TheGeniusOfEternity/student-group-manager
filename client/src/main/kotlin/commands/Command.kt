package commands

import handlers.IOHandler

/**
 * Initialize new command, which has two default options: execute and describe
 */
interface Command {
    val description: String
    /**
     * Initiate command
     */
    fun execute(args: List<String>)

    /**
     * Shows command description
     */
    fun describe() {
        IOHandler printInfoLn description
    }
}