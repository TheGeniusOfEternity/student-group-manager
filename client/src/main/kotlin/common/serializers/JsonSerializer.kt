package common.serializers

import kotlinx.serialization.json.Json

/**
 * Serializes / deserializes  [T] objects for exchanging with server
 */
interface JsonSerializer<T> {
    /**
     * Serializer, that converts object into JSON
     */
    val jsonSerializer: Json
        get() = Json {
            ignoreUnknownKeys = true
        }
    /**
     * Serializes [T] to [ByteArray] for sending to server
     * @param obj - Object, that is needed to be serialized for sending
     * @return [ByteArray] value of [obj] for network transfer
     */
    fun serialize(obj: T): ByteArray
    /**
     * Deserializes [ByteArray] to [T] on receiving from server
     * @param bytes - serialized that was sent from server
     * @return [T] - Deserialized value of [bytes]
     */
    fun deserialize(bytes: ByteArray): T
}