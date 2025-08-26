package collection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import kotlin.test.assertFailsWith

class StudyGroupTest {
    @Test
    fun `test valid StudyGroup creation`() {
        val group = StudyGroup(
            id = 1,
            name = "Test Group",
            coordinates = Coordinates(10, 20L),
            studentsCount = 10,
            transferredStudents = 5L,
            averageMark = 4,
            formOfEducation = FormOfEducation.FULL_TIME_EDUCATION,
            groupAdmin = Person(
                "Person",
                SimpleDateFormat("dd.mm.yyyy").parse("10.11.2016"),
                Country.CHINA
            )
        )
        assertNotNull(group)
        assertEquals(1, group.getId())
        assertEquals("Test Group", group.getName())
    }

    @Test
    fun `test invalid id creation`() {
        assertFailsWith<IllegalArgumentException> {
            StudyGroup(
                id = -2,
                name = "Test Group",
                coordinates = Coordinates(10, 20L),
                studentsCount = 10,
                transferredStudents = 5L,
                averageMark = 4,
                formOfEducation = FormOfEducation.FULL_TIME_EDUCATION,
                groupAdmin = Person(
                    "John Doe",
                    SimpleDateFormat("yyyy-mm-dd").parse("2006-10-23"),
                    Country.CHINA
                )
            )
        }
    }

    @Test
    fun `test invalid studentsCount creation`() {
        assertFailsWith<IllegalArgumentException> {
            StudyGroup(
                id = 1,
                name = "Test Group",
                coordinates = Coordinates(10, 20L),
                studentsCount = 0,
                transferredStudents = 5L,
                averageMark = 4,
                formOfEducation = FormOfEducation.FULL_TIME_EDUCATION,
                groupAdmin = Person(
                    "John Doe",
                    SimpleDateFormat("yyyy-mm-dd").parse("2006-10-23"),
                    Country.CHINA
                )
            )
        }
    }

    @Test
    fun `test invalid transferredStudents creation`() {
        assertFailsWith<IllegalArgumentException> {
            StudyGroup(
                id = 1,
                name = "Test Group",
                coordinates = Coordinates(10, 20L),
                studentsCount = 10,
                transferredStudents = -1L,
                averageMark = 4,
                formOfEducation = FormOfEducation.FULL_TIME_EDUCATION,
                groupAdmin = Person(
                    "John Doe",
                    SimpleDateFormat("yyyy-mm-dd").parse("2006-10-23"),
                    Country.CHINA
                )
            )
        }
    }
}