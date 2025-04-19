package commands
import receiver.Receiver
import collection.CollectionInfo
import handlers.IOHandler

/**
 * Shows info about Collection, storing in [Receiver]
 */
class InfoCmd: Command {
    override fun execute(args: List<Any?>) {
        if (args.isEmpty()) {
            IOHandler printInfo CollectionInfo.toString()
        } else {
            IOHandler printInfoLn "info: Too many arguments"
        }
    }

    override fun describe(): String {
        return "info - shows information about collection (type, data initialized, elements count)"
    }
}