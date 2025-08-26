package gui.controllers

import core.State
import handlers.SceneHandler
import handlers.SceneHandler.modalStage
import handlers.SceneHandler.primaryStage
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window

class ModalConnectionFailController {

    @FXML
    lateinit var modal: AnchorPane
    @FXML
    lateinit var modalText: Text
    @FXML
    lateinit var modalYesButton: Button
    @FXML
    lateinit var modalNoButton: Button

    private lateinit var authController: AuthController

    fun initialize() {
        authController = SceneHandler.controllers["auth"] as AuthController
        modalYesButton.setOnMouseClicked {
            modalStage?.close()
            authController.connectToServer()
        }
        modalNoButton.setOnMouseClicked {
            modalStage?.close()
            if (primaryStage?.title == "Student Group Manager - Collection") {
                Platform.exit()
                State.isRunning = false
            } else State.host = null
        }
    }

    fun showModalWindow(
        fxmlPath: String,
        title: String = "Модальное окно",
        resizable: Boolean = false,
        alwaysOnTop: Boolean = true,
        owner: Window? = primaryStage
    ) {
        if (modalStage != null && modalStage!!.isShowing) {
            modalStage!!.toFront()
            return
        }
        val loader = FXMLLoader(javaClass.getResource(fxmlPath))
        val root = loader.load<Parent>()

        modalStage = Stage().apply {
            scene = Scene(root)
            initOwner(owner)
            initModality(Modality.WINDOW_MODAL)
            isResizable = resizable
            this.title = title
            isAlwaysOnTop = alwaysOnTop

            setOnCloseRequest {
                modalStage = null
            }
        }
        modalStage!!.show()
    }
}
