package invoker

import collection.CollectionInfo
import commands.*
import dto.CommandParam
import kotlin.collections.HashMap

/**
 * Singleton class-handler, invokes all commands
 *
 * @property commands List of all existing commands
 */
object Invoker {
    val commands: HashMap<String, Command> = HashMap()
    init {
        commands["authorize"] = AuthorizeCmd()
        commands["refresh"] = RefreshTokenCmd()
        commands["get_by_id"] = GetByIdCmd()
        commands["get_commands_list"] = GetCommandsListCmd()
        commands["info"] = InfoCmd()
        commands["show"] = ShowCmd()
        commands["insert"] = InsertCmd()
        commands["update"] = UpdateCmd()
        commands["remove"] = RemoveCmd()
        commands["clear"] = ClearCmd()
        commands["history"] = HistoryCmd()
        commands["remove_lower"] = RemoveLowerCmd()
        commands["remove_any_by_transferred_students"] = RemoveByTransfStudsCmd()
        commands["filter_greater_than_students_count"] = FilterGreaterStudsCountCmd()
        commands["print_unique_average_mark"] = PrintUniqueAvgMarkCmd()
    }

    /**
     * Initiates [Command] execution
     */
    fun run(commandName: String, args: List<CommandParam?>, clientId: String) {
        val command: Command? = this.commands[commandName]
        if (command != null) {
            CollectionInfo.updateCommandHistory(commandName)
            command.execute(args, clientId)
        } else {
            println("Command not found: $commandName")
        }
    }
}