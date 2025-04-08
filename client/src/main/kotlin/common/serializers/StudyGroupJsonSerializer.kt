package common.serializers

import collection.StudyGroup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Serializes / deserializes  [StudyGroup] objects for exchanging with server
 */
class StudyGroupSerializer : Serializer<StudyGroup> {
    private val serializer = Json {
        ignoreUnknownKeys = true
    }

    override fun serialize(obj: StudyGroup): ByteArray {
        return serializer.encodeToString(obj).toByteArray()
    }

    override fun deserialize(bytes: ByteArray): StudyGroup {
        return serializer.decodeFromString(String(bytes, charset("UTF-8")))
    }

}