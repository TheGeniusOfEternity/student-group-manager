import invoker.Invoker
import receiver.Receiver

fun main() {
    val invoker = Invoker
    val receiver = Receiver
    receiver.readFileData("src/main/resources/src.csv")
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