import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object Json {
    val serializer = Json{
        ignoreUnknownKeys = true
    }

    inline fun <reified T> serialize(obj: T): String {
        return serializer.encodeToString(obj)
    }

    inline fun <reified T> deserialize(str: String): T {
        return serializer.decodeFromString(str)
    }
}