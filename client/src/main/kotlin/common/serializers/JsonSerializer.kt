package common.serializers

/**
 * Serializes / deserializes  [T] objects for exchanging with server
 */
interface Serializer<T> {
    /**
     * Serializes [T] to [ByteArray] for sending to server
     * @param obj - Object, that is needed to be serialized for sending
     * @return [ByteArray] value of [obj] for network transfer
     */
    fun serialize(obj: T): ByteArray
    /**
     * Serializes [T] to [ByteArray] for sending to server
     * @param bytes - serialized value of [T] for network transfer
     * @return [T] - Object, that was sent from server
     */
    fun deserialize(bytes: ByteArray): T
}