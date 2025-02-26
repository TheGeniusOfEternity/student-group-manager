package collection

import java.time.LocalDate

class StudyGroup(
    private var id: Long,
    private var name: String,
    private var coordinates: Coordinates,
    private var studentsCount: Int,
    private var transferredStudents: Long?,
    private var averageMark: Int?,
    private var formOfEducation: FormOfEducation?,
    private var groupAdmin: Person?,
) {
    init {
        require(id > 0) { "id must be greater than zero" }
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(studentsCount > 0) { "id must be greater than zero" }
        require(transferredStudents == null || transferredStudents!! > 0)
            { "transferredStudents must be greater than zero" }
        require(averageMark == null || averageMark!! > 0)
            { "averageMark must be greater than zero" }
    }
    private var creationDate: LocalDate = LocalDate.now()

    fun getId(): Long {
        return id
    }

    override fun toString(): String {
        return "Student Group #" + id + "\n" +
                "Name: " + name + "\n" +
                "Coordinates: " + coordinates + "\n" +
                "Students count: " + studentsCount + "\n" +
                "Transferred Students: " + transferredStudents + "\n"  +
                "Average mark: " + averageMark + "\n" +
                "Form of Education: " + formOfEducation + "\n" +
                "Group admin: \t\n" + groupAdmin +
                "Creation Date: " + creationDate
    }
}