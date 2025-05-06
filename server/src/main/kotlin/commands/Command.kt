package commands

import dto.CommandParam

/**
 * Initialize new command, which has two default options: execute and describe
 */
interface Command {
    val paramTypeName: String?
    /**
     * Initiate command
     */
    fun execute(args: List<CommandParam?>, clientId: String)

    /**
     * Shows command description
     */
    fun describe(): String
}