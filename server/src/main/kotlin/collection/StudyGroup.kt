package collection

import serializers.LocalDateSerializer
import annotations.Nested
import annotations.Pos
import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * Class of study group, element of [CollectionInfo]
 */
@Serializable
class StudyGroup(
    @Pos(10) private val id: Long,
    @Pos(20) val name: String,
    @Pos(30) @Nested private val coordinates: Coordinates,
    @Pos(40) private val studentsCount: Int,
    @Pos(50) private val transferredStudents: Long?,
    @Pos(60) private val averageMark: Int?,
    @Pos(70) private val formOfEducation: FormOfEducation?,
    @Pos(80) @Nested private val groupAdmin: Person?
): Comparable<StudyGroup> {
    init {
        require(id > 0) { "id must be greater than zero" }
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(studentsCount > 0) { "id must be greater than zero" }
        require(transferredStudents == null || transferredStudents > 0)
            { "transferredStudents must be greater than zero" }
        require(averageMark == null || averageMark > 0)
            { "averageMark must be greater than zero" }
    }
    @Pos(90) @Serializable(LocalDateSerializer::class) private var creationDate: LocalDate = LocalDate.now()

    override fun compareTo(other: StudyGroup): Int {
        return studentsCount.compareTo(other.studentsCount)
    }

    /**
     * @return id of this StudyGroup
     */
    fun getId(): Long {
        return id
    }

    /**
     * @return [studentsCount] of the group
     */
    fun getStudentsCount(): Int {
        return studentsCount
    }

    /**
     * @return [transferredStudents] of the group
     */
    fun getTransferredStudents(): Long? {
        return transferredStudents
    }

    /**
     * @return [averageMark] of study group
     */
    fun getAverageMark(): Int? {
        return averageMark
    }


    /**
     * @return [StudyGroup] string representation
     */
    override fun toString(): String {
        return "Student Group #" + id + "\n" +
                "Creation Date: " + creationDate + "\n" +
                "Name: " + name + "\n" +
                "Coordinates: " + coordinates + "\n" +
                "Students count: " + studentsCount + "\n" +
                "Transferred Students: " + transferredStudents + "\n"  +
                "Average mark: " + averageMark + "\n" +
                "Form of Education: " + formOfEducation + "\n" +
                "Group admin: \t\n" + groupAdmin
    }
}