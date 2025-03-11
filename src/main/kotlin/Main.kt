import invoker.Invoker
import receiver.Receiver

/**
 * A type alias for a list of [Property], used for representing collections of text data.
 */
typealias GroupData = ArrayList<Pair<String, String?>>
/**
 * A type alias for pair (propertyName, propertyValue)
 */
typealias Property = Pair<String, String?>

/**
 * Entry point of the program
 */
fun main() {
    val invoker = Invoker
    val receiver = Receiver
    receiver.loadFromFile("src/main/resources/src.csv")
    while (true) {
        print("& ")
        var commandName = ""
        val input = readlnOrNull()
        if (!input.isNullOrEmpty()) {
            val query: List<String> = input.trim().split(" ")
            commandName = query.first()
            invoker.run(commandName, query.drop(1))
        }
    }
}