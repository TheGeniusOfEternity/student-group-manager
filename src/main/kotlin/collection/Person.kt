package collection

import annotations.Pos
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Class, representing [StudyGroup.groupAdmin]
 */
class Person (
    @Pos(81) private val name: String,
    @Pos(82) private val birthday: Date?,
    @Pos(83) private val nationality: Country?
) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
    }

    /**
     * @return [Person] string representation
     */
    override fun toString(): String {
        return "- Name: $name\n" +
                "- Birthday: ${SimpleDateFormat("dd.MM.yyyy").format(birthday)}\n" +
                "- Nationality: $nationality\n"
    }
}