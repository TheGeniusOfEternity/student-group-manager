package common.serializers

import collection.StudyGroup
import kotlinx.serialization.encodeToString

/**
 * Serializes / deserializes  [StudyGroup] objects for exchanging with server
 */
object StudyGroupJsonSerializer : JsonSerializer<StudyGroup> {
    /**
     * Serializes [StudyGroup] to [ByteArray] for sending to server
     * @param obj - [StudyGroup], that is needed to be serialized for sending
     * @return [ByteArray] value of [StudyGroup] [obj] for network transfer
     */
    override fun serialize(obj: StudyGroup): ByteArray {
        return jsonSerializer.encodeToString(obj).toByteArray()
    }
    /**
     * Deserializes [ByteArray] to [StudyGroup] on receiving from server
     * @param bytes - serialized that was sent from server
     * @return [StudyGroup] - Deserialized value of [bytes]
     */
    override fun deserialize(bytes: ByteArray): StudyGroup {
        return jsonSerializer.decodeFromString(String(bytes, charset("UTF-8")))
    }
}