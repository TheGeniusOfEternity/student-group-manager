package collection

import annotations.Pos
import kotlinx.serialization.Serializable
import serializers.DateSerializer
import java.util.Date

/**
 * Class, representing [StudyGroup.groupAdmin]
 */
@Serializable
class Person (
    @Pos(81) private val name: String,
    @Pos(82) @Serializable(DateSerializer::class) private val birthday: Date?,
    @Pos(83) private val nationality: Country?
) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
    }

    fun getName() = name
    fun getBirthday() = birthday
    fun getNationality() = nationality

    /**
     * @return [Person] string representation
     */
    override fun toString(): String {
        return "- Name: $name\n" +
                "- Birthday: ${birthday}\n" +
                "- Nationality: $nationality\n"
    }
}