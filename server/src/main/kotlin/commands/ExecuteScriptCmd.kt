package commands

import collection.CollectionInfo
import dto.CommandParam
import handlers.IOHandler
import java.io.File

/**
 * Executes commands from the file
 */
class ExecuteScriptCmd: Command {
    override val paramTypeName = "String"
    override fun execute(args: List<CommandParam?>) {
        if (args.size == 1) {
            val filename = "scripts/${(args[0] as CommandParam.StringParam).value}"
            if (File(filename).exists()) {
                if (CollectionInfo.getOpenedFiles().none { it.first.contains(filename) }) {
                    IOHandler.handleFileInput(filename, null)
                    IOHandler.responsesThread.add("\nEnd of executing script $filename\n\n")
                } else {
                    IOHandler.responsesThread.add("execute_script error: file $filename is already opened at the moment\n")
                }
            } else {
                IOHandler.responsesThread.add("execute_script error: file not found: $filename")
            }
        } else {
            IOHandler.responsesThread.add("execute_script error: invalid count of arguments")
        }
    }

    override fun describe(): String {
        return "execute_script <filename> - executes commands from the file"
    }
}