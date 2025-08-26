package gui.controllers

import collection.Coordinates
import collection.Country
import collection.FormOfEducation
import collection.StudyGroup
import commands.ServerCmd
import core.GroupData
import core.Property
import core.State
import handlers.SceneHandler
import invoker.Invoker
import javafx.animation.FadeTransition
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import javafx.util.Duration
import javafx.util.converter.IntegerStringConverter
import validators.GroupDataValidator
import java.time.ZoneId


class StudyGroupController {
    lateinit var deleteButton: Button
    lateinit var saveButton: Button

    lateinit var ownerText: Text
    lateinit var groupTitle: Text

    lateinit var groupAdminNationalityLabel: Label
    lateinit var groupAdminBirthLabel: Label
    lateinit var groupAdminNameLabel: Label
    lateinit var formOfEducationLabel: Label
    lateinit var averageMarkLabel: Label
    lateinit var transferredStudentsLabel: Label
    lateinit var studentsCountLabel: Label
    lateinit var yLabel: Label
    lateinit var xLabel: Label
    lateinit var nameLabel: Label

    lateinit var enableAdmin: CheckBox

    lateinit var groupAdminNationalityInput: ChoiceBox<Country?>
    lateinit var groupAdminBirthInput: DatePicker
    lateinit var groupAdminNameInput: TextField
    lateinit var formOfEducationInput: ChoiceBox<FormOfEducation?>
    lateinit var averageMarkInput: TextField
    lateinit var transferredStudentsInput: TextField
    lateinit var studentsCountInput: TextField
    lateinit var yInput: TextField
    lateinit var xInput: TextField
    lateinit var nameInput: TextField

    lateinit var group: StudyGroup

    @FXML
    lateinit var rootPane: AnchorPane

    private var transition: FadeTransition = FadeTransition()
    fun initialize() {
        formOfEducationInput.items.addAll(FormOfEducation.entries)
        formOfEducationInput.items.add(null)
        groupAdminNationalityInput.items.addAll(Country.entries)
        groupAdminNationalityInput.items.addAll(null)

        saveButton.setOnMouseClicked {
            val groupData = GroupData()
            val groupDataValidator = GroupDataValidator()
            try {
                val mode = if (group.getId() == -1L) "insert" else "update"
                groupData.add(Property("id", if (group.getId() == -1L)
                    ((State.localCollection.keys.maxOrNull() ?: 0) + 1).toString()
                else group.getId().toString()
                ))
                groupData.add(Property("name", nameInput.text))
                groupData.add(Property("x", xInput.text.toString()))
                groupData.add(Property("y", yInput.text. toString()))
                groupData.add(Property("studentsCount", studentsCountInput.text.toString()))
                groupData.add(Property("transferredStudents", transferredStudentsInput.text.toString()))
                groupData.add(Property("averageMark", averageMarkInput.text.toString()))
                groupData.add(Property("formOfEducation", if (formOfEducationInput.value != null)
                    formOfEducationInput.value.toString() else null))
                if (enableAdmin.isSelected) {
                    groupData.add(Property("name", groupAdminNameInput.text))
                    groupData.add(Property("birthday", (groupAdminBirthInput.value ?: "").toString()))
                    groupData.add(Property("nationality", if (groupAdminNationalityInput.value != null)
                        groupAdminNationalityInput.value.toString() else null))
                }
                group = groupDataValidator.validateData(groupData)!!
                State.tempGroup = group
                if (mode == "insert") {
                    group.setOwnerName(State.credentials["TEMP_USERNAME"])
                    (Invoker.commands["insert"] as ServerCmd)
                        .serverExecute(group.getId().toString())
                }
                else (Invoker.commands["update"] as ServerCmd)
                    .serverExecute(group.getId().toString())

                (SceneHandler.controllers["main"] as MainController).reloadCollection()
                hideGroup()
                (SceneHandler.controllers["main"] as MainController).groupTable.selectionModel.clearSelection()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        deleteButton.setOnMouseClicked {
            try {
                (Invoker.commands["remove"] as ServerCmd)
                    .serverExecute(group.getId().toString())
                (SceneHandler.controllers["main"] as MainController).reloadCollection()
                hideGroup()
                (SceneHandler.controllers["main"] as MainController).groupTable.selectionModel.clearSelection()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        enableAdmin.setOnMouseClicked { showAdminForm() }
    }


    fun showGroup(studyGroup: StudyGroup? = null) {
        enableAdmin.isDisable = studyGroup != null && (!studyGroup.getOwnerName().equals(State.credentials["TEMP_USERNAME"]))
        enableAdmin.isSelected = studyGroup?.getGroupAdmin() != null
        showAdminForm()

        if (studyGroup != null && (!studyGroup.getOwnerName().equals(State.credentials["TEMP_USERNAME"]))) {
            saveButton.isDisable = true
            deleteButton.isDisable = true
        } else {
            saveButton.isDisable = false
            deleteButton.isDisable = false
        }
        listOf(xInput, yInput, studentsCountInput, transferredStudentsInput, averageMarkInput, transferredStudentsInput)
            .forEach { input ->
                input.textFormatter = TextFormatter(
                    IntegerStringConverter(),
                    null
                )
                { c: TextFormatter.Change? -> if (c!!.text.matches("\\d*".toRegex())) c else null }
                input.clear()
                input.isDisable = studyGroup != null && (!studyGroup.getOwnerName().equals(State.credentials["TEMP_USERNAME"]))
            }
        listOf(
            nameInput, formOfEducationInput, groupAdminNameInput, groupAdminBirthInput,
            groupAdminNationalityInput
        ).forEach { input ->
            when (input) {
                is TextField -> input.clear()
                is DatePicker -> input.value = null
                is ComboBox<*> -> input.selectionModel.clearSelection()
            }
            input.isDisable = studyGroup != null && (!studyGroup.getOwnerName().equals(State.credentials["TEMP_USERNAME"]))
        }
        if (studyGroup != null) {
            group = studyGroup
            nameInput.text = studyGroup.getName()
            xInput.text = studyGroup.getCoordinates().getX().toString()
            yInput.text = studyGroup.getCoordinates().getY().toString()
            studentsCountInput.text = studyGroup.getStudentsCount().toString()
            transferredStudentsInput.text = studyGroup.getTransferredStudents().toString()
            averageMarkInput.text = studyGroup.getAverageMark().toString()
            formOfEducationInput.value = studyGroup.getFormOfEducation()
            groupAdminNameInput.text = (studyGroup.getGroupAdmin()?.getName() ?: "").toString()
            groupAdminBirthInput.value = studyGroup.getGroupAdmin()?.getBirthday()
                ?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            groupAdminNationalityInput.value = studyGroup.getGroupAdmin()?.getNationality()

            groupTitle.text = "Группа №${studyGroup.getId()}"
            ownerText.text = "Владелец: ${studyGroup.getOwnerName()}"

            saveButton.text = "Обновить"
            deleteButton.isVisible = true
        } else {
            groupTitle.text = "Новая группа"
            ownerText.text = ""

            saveButton.text = "Сохранить"
            deleteButton.isVisible = false

            group = StudyGroup(
                -1,
                "New group",
                Coordinates(0, 0),
                1,
                null,
                null,
                null,
                null,
                null
            )
        }
        transition.stop()
        transition = FadeTransition(Duration.millis(300.0), rootPane).apply {
            fromValue = 0.0
            toValue = 1.0
        }
        rootPane.translateY = 0.0
        transition.play()
        rootPane.isVisible = true
    }

    fun hideGroup() {
        transition.stop()
        transition = FadeTransition(Duration.millis(300.0), rootPane).apply {
            fromValue = 1.0
            toValue = 0.0
        }
        transition.setOnFinished{ rootPane.translateY = -680.0 }
        transition.play()
    }

    private fun showAdminForm() {
        if (enableAdmin.isSelected) {
            groupAdminNameInput.isVisible = true
            groupAdminBirthInput.isVisible = true
            groupAdminNationalityInput.isVisible = true
            groupAdminNameLabel.isVisible = true
            groupAdminBirthLabel.isVisible = true
            groupAdminBirthLabel.isVisible = true
        } else {
            groupAdminNameInput.isVisible = false
            groupAdminBirthInput.isVisible = false
            groupAdminNationalityInput.isVisible = false
            groupAdminNameLabel.isVisible = false
            groupAdminBirthLabel.isVisible = false
            groupAdminNationalityLabel.isVisible = false
        }
    }
}