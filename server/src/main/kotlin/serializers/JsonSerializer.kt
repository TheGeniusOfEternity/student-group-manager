package serializers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Serializes / deserializes objects for exchanging with server
 */
object JsonSerializer{
    val jsonSerializer: Json
        get() = Json {
            ignoreUnknownKeys = true
        }
    /**
     * Serializes [T] to [ByteArray] for sending to server
     * @param obj - [T], that is needed to be serialized for sending
     * @return [ByteArray] value of [T] [obj] for network transfer
     */
    inline fun <reified T>serialize(obj: T): ByteArray {
        return jsonSerializer.encodeToString(obj).toByteArray()
    }
    /**
     * Deserializes [ByteArray] to [T] on receiving from server
     * @param bytes - serialized that was sent from server
     * @return [T] - Deserialized value of [ByteArray]
     */
    inline fun <reified T>deserialize(bytes: ByteArray): T {
        return jsonSerializer.decodeFromString(String(bytes, charset("UTF-8")))
    }
}