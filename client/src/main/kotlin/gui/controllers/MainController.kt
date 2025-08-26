package gui.controllers

import collection.*
import core.State
import handlers.ConnectionHandler
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.util.Duration
import java.util.*

class MainController {
    @FXML
    lateinit var rootPane: AnchorPane
    @FXML
    lateinit var addButton: Button

    @FXML
    lateinit var ownerColumn: TableColumn<StudyGroup, String>
    @FXML
    lateinit var groupAdminNationalityColumn: TableColumn<StudyGroup, Country?>
    @FXML
    lateinit var groupAdminBirthdayColumn: TableColumn<StudyGroup, Date?>
    @FXML
    lateinit var groupAdminNameColumn: TableColumn<StudyGroup, String>
    @FXML
    lateinit var formOfEducationColumn: TableColumn<StudyGroup, FormOfEducation?>
    @FXML
    lateinit var averageMarkColumn: TableColumn<StudyGroup, Int?>
    @FXML
    lateinit var transferredStudentsColumn: TableColumn<StudyGroup, Long?>
    @FXML
    lateinit var studentsCountColumn: TableColumn<StudyGroup, Int>
    @FXML
    lateinit var yColumn: TableColumn<StudyGroup, Long>
    @FXML
    lateinit var xColumn: TableColumn<StudyGroup, Int>
    @FXML
    lateinit var nameColumn: TableColumn<StudyGroup, String>
    @FXML
    lateinit var idColumn: TableColumn<StudyGroup, Long>


    @FXML
    lateinit var groupTable: TableView<StudyGroup>


    @FXML
    lateinit var groups: Pane
    @FXML
    lateinit var sidebar: Pane

    private lateinit var studyGroupRoot: Parent
    private lateinit var studyGroupController: StudyGroupController

    private lateinit var popupRoot: Parent
    lateinit var popUpController: PopUpController

    private var selectedIndex: Int = -1

    fun initialize() {
        idColumn.cellValueFactory = PropertyValueFactory("id")
        nameColumn.cellValueFactory = PropertyValueFactory("name")

        xColumn.setCellValueFactory { cellData ->
            val coordinates = cellData.value.getCoordinates()
            SimpleIntegerProperty(coordinates.getX()).asObject()
        }
        yColumn.setCellValueFactory { cellData ->
            val coordinates = cellData.value.getCoordinates()
            SimpleLongProperty(coordinates.getY()).asObject()
        }

        studentsCountColumn.cellValueFactory = PropertyValueFactory("studentsCount")
        transferredStudentsColumn.cellValueFactory = PropertyValueFactory("transferredStudents")
        averageMarkColumn.cellValueFactory = PropertyValueFactory("averageMark")
        formOfEducationColumn.cellValueFactory = PropertyValueFactory("formOfEducation")

        groupAdminNameColumn.setCellValueFactory { cellData ->
            val groupAdmin = cellData.value.getGroupAdmin()
            if (groupAdmin != null) SimpleStringProperty(groupAdmin.getName())
            else SimpleStringProperty("")
        }
        groupAdminBirthdayColumn.setCellValueFactory { cellData ->
            val groupAdmin = cellData.value.getGroupAdmin()
            if (groupAdmin != null) SimpleObjectProperty(groupAdmin.getBirthday())
            else SimpleObjectProperty(null)
        }
        groupAdminNationalityColumn.setCellValueFactory { cellData ->
            val groupAdmin = cellData.value.getGroupAdmin()
            if (groupAdmin != null) SimpleObjectProperty(groupAdmin.getNationality())
            else SimpleObjectProperty(null)
        }
        ownerColumn.cellValueFactory = PropertyValueFactory("ownerName")

        val timeline = Timeline(KeyFrame(Duration.millis(2000.0), {
            reloadCollection()
        }))
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()

        addButton.setOnMouseClicked { event ->
            if (event.clickCount == 1) {
                studyGroupController.showGroup()
            }
        }

        groupTable.setOnMouseClicked { event ->
            if (event.clickCount == 1) {
                studyGroupController.hideGroup()
                val oldSelectedIndex = selectedIndex
                selectedIndex = groupTable.selectionModel.selectedIndex
                if (oldSelectedIndex != selectedIndex) {
                    val group = groupTable.items[selectedIndex]
                    groupTable.selectionModel.select(selectedIndex)
                    studyGroupController.showGroup(group)
                } else {
                    selectedIndex = -1
                    groupTable.selectionModel.clearSelection()
                }
            }
        }

        val studyGroupLoader = FXMLLoader(javaClass.getResource("/views/StudyGroupView.fxml"))
        val popUpLoader = FXMLLoader(javaClass.getResource("/views/PopUpView.fxml"))

        studyGroupRoot = studyGroupLoader.load()
        popupRoot = popUpLoader.load()
        studyGroupController = studyGroupLoader.getController()
        popUpController = popUpLoader.getController()
        rootPane.children.add(studyGroupRoot)
        rootPane.children.add(popupRoot)
        studyGroupRoot.isVisible = false
        popupRoot.isVisible = false
    }

    fun reloadCollection() {
        if (State.connectedToServer && State.isAuthenticated) {
            selectedIndex = groupTable.selectionModel.selectedIndex
            ConnectionHandler.loadCollectionInfo()
            groupTable.items = FXCollections.observableArrayList(State.localCollection.values)
            if (selectedIndex >= 0 && selectedIndex < groupTable.items.size) {
                groupTable.selectionModel.select(selectedIndex)
            } else groupTable.selectionModel.clearSelection()
        }
    }
}