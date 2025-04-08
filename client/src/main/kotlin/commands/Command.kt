package commands

/**
 * Initialize new command, which has two default options: execute and describe
 */
interface Command {
    /**
     * Initiate command
     */
    fun execute(args: List<String>)

    /**
     * Shows command description
     */
    fun describe()
}