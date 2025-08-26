package collection

import serializers.LocalDateSerializer
import annotations.Nested
import annotations.Pos
import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * Class of study group, element of collection
 */
@Serializable
class StudyGroup(
    @Pos(10) private var id: Long,
    @Pos(20) private var name: String,
    @Pos(30) @Nested private var coordinates: Coordinates,
    @Pos(40) private var studentsCount: Int,
    @Pos(50) private var transferredStudents: Long?,
    @Pos(60) private var averageMark: Int?,
    @Pos(70) private var formOfEducation: FormOfEducation?,
    @Pos(80) @Nested private var groupAdmin: Person?,
    @Pos(100) private var ownerName: String? = null,
): Comparable<StudyGroup> {
    init {
        require(id > 0 || id == -1L) { "id must be greater than zero" }
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(studentsCount > 0) { "studentsCount must be greater than zero" }
        require(transferredStudents == null || transferredStudents!! > 0)
            { "transferredStudents must be greater than zero" }
        require(averageMark == null || averageMark!! > 0)
            { "averageMark must be greater than zero" }
    }
    @Pos(90) @Serializable(LocalDateSerializer::class) private var creationDate: LocalDate = LocalDate.now()

    override fun compareTo(other: StudyGroup): Int {
        return studentsCount.compareTo(other.studentsCount)
    }

    /**
     * @return id of this StudyGroup
     */
    fun getId() = id
    fun getName() = name
    fun getCoordinates() = coordinates
    fun getStudentsCount() = studentsCount
    fun getTransferredStudents() = transferredStudents
    fun getAverageMark() = averageMark
    fun getFormOfEducation() = formOfEducation
    fun getGroupAdmin() = groupAdmin
    fun getOwnerName() = ownerName

    fun setId(id: Long) {
        this.id = id
    }

    fun setName(name: String) {
        this.name = name
    }
    fun setCoordinates(coordinates: Coordinates) {
        this.coordinates = coordinates
    }
    fun setStudentsCount(studentsCount: Int) {
        this.studentsCount = studentsCount
    }
    fun setTransferredStudents(transferredStudents: Long?) {
        this.transferredStudents = transferredStudents
    }
    fun setAverageMark(mark: Int?) {
        this.averageMark = mark
    }
    fun setFormOfEducation(formOfEducation: FormOfEducation?) {
        this.formOfEducation = formOfEducation
    }
    fun setGroupAdmin(groupAdmin: Person?) {
        this.groupAdmin = groupAdmin
    }
    fun setOwnerName(ownerName: String?) {
        this.ownerName = ownerName
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
                "Group admin: \t\n" + groupAdmin + "\n" +
                "Owner: " + ownerName
    }
}