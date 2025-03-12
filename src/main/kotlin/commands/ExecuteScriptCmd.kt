package commands

import collection.CollectionInfo
import handlers.ReadScriptFileHandler
import java.io.File

/**
 * Executes commands from the file
 */
class ExecuteScriptCmd: Command {
    override fun execute(args: List<String>) {
        if (args.size == 1) {
            val readScriptFileHandler = ReadScriptFileHandler()
            val filename = "scripts/${args[0]}"
            if (File(filename).exists()) {
                if (CollectionInfo.getOpenedFileName()?.first != filename) {
                    readScriptFileHandler.handle(filename, null)
                } else {
                    println("execute_script error: infinite loop")
                }

            } else {
                println("execute_script error: file not found: $filename")
            }

        } else {
            println("execute_script error: invalid count of arguments")
        }
    }

    override fun describe() {
        println("execute_script <filename> - executes commands from the file")
    }
}