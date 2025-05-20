package commands
import receiver.Receiver
import collection.CollectionInfo
import dto.CommandParam
import handlers.IOHandler
import java.util.ArrayList

/**
 * Shows info about Collection, storing in [Receiver]
 */
class InfoCmd: Command {
    override val paramTypeName = null
    override fun execute(args: List<CommandParam?>, clientId: String) {
        if (args.size == 1) {
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(CollectionInfo.toString())
        } else {
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add("info: Too many arguments")
        }
    }

    override fun describe(): String {
        return "info - shows information about collection (type, data initialized, elements count)"
    }
}