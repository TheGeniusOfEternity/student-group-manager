package collection

import annotations.Pos
import java.util.Date

class Person (
    @Pos(1) private val name: String,
    @Pos(2) private val birthday: Date?,
    @Pos(3) private val nationality: Country?
) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
    }

    override fun toString(): String {
        return "- Name: $name\n" +
                "- Birthday: $birthday\n" +
                "- Nationality: $nationality\n"
    }
}