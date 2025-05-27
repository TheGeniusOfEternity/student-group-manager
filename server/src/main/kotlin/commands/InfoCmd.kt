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
    override fun execute(args: List<CommandParam?>, clientId: String, correlationId: String) {
        if (args.size == 1) {
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(Pair(CollectionInfo.toString(), correlationId))
        } else {
            IOHandler.responsesThreads.getOrPut(clientId) { ArrayList() }.add(Pair("info: Too many arguments", correlationId))
        }
    }

    override fun describe(): String {
        return "info - shows information about collection (type, data initialized, elements count)"
    }
}