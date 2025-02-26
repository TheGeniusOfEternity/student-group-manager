package collection

import annotations.Nested
import annotations.Pos
import java.time.LocalDate

class StudyGroup(
    @Pos(1) private var id: Long,
    @Pos(2) var name: String,
    @Pos(3) @Nested private var coordinates: Coordinates,
    @Pos(4) private var studentsCount: Int,
    @Pos(5) private var transferredStudents: Long?,
    @Pos(6) private var averageMark: Int?,
    @Pos(7) private var formOfEducation: FormOfEducation?,
    @Pos(8) @Nested private var groupAdmin: Person?
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

    fun setId(id: Long): StudyGroup {
        this.id = id
        return this
    }

    fun setName(name: String): StudyGroup {
        this.name = name
        return this
    }

    fun setCoordinates(coordinates: Coordinates): StudyGroup {
        this.coordinates = coordinates
        return this
    }

    fun setStudentsCount(count: Int): StudyGroup {
        this.studentsCount = count
        return this
    }

    fun setTransferredStudents(count: Long?): StudyGroup {
        this.transferredStudents = count
        return this
    }

    fun setAverageMark(avgMark: Int?): StudyGroup {
        this.averageMark = avgMark
        return this
    }

    fun setFormOfEducation(formOfEducation: FormOfEducation): StudyGroup {
        this.formOfEducation = formOfEducation
        return this
    }

    fun setGroupAdmin(groupAdmin: Person): StudyGroup {
        this.groupAdmin = groupAdmin
        return this
    }


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