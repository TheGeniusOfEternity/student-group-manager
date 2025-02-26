package collection

import java.util.Date

class Person (
    private val name: String,
    private val birthday: Date?,
    private val nationality: Country?
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