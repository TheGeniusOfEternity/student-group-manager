package gui.controllers

import core.State
import handlers.ConnectionHandler
import handlers.IOHandler
import handlers.SceneHandler
import javafx.animation.*
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.util.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AuthController {
    @FXML
    lateinit var ipButton: Button
    @FXML
    lateinit var authButton: Button

    @FXML
    lateinit var rootPane: AnchorPane
    @FXML
    lateinit var serverSetupPane: Pane
    @FXML
    lateinit var authPane: Pane
    @FXML
    private lateinit var ipLabel: Label
    @FXML
    private lateinit var loginLabel: Label
    @FXML
    private lateinit var passwordLabel: Label
    @FXML
    private lateinit var ipInput: TextField
    @FXML
    private lateinit var loginInput: TextField
    @FXML
    private lateinit var passwordInput: TextField
    
    private lateinit var popupRoot: Parent
    private lateinit var popUpController: PopUpController

    @FXML
    fun handleIpAddressClick() {
        if (IOHandler.isValidIPv4(ipInput.text)) {
            State.host = ipInput.text
            popUpController.hidePopUp()
            connectToServer()
        } else {
            popUpController.showPopUp("Некорректный адрес сервера")
            ipInput.text = ""
        }
    }

    @FXML
    fun handleAuthClick() {
        if (loginInput.text.isNotBlank() && passwordInput.text.isNotBlank()) {
            popUpController.hidePopUp()
            State.credentials["TEMP_USERNAME"] = loginInput.text
            State.credentials["TEMP_PASSWORD"] = passwordInput.text
            val latch = CountDownLatch(1)
            ConnectionHandler.authorize(latch)
            val success = latch.await(2000, TimeUnit.MILLISECONDS)
            if (success && State.isAuthenticated) {
                SceneHandler.switchTo("main", "Student Group Manager - Collection")
                (SceneHandler.controllers["main"] as MainController).reloadCollection()
            } else handleAuthFail()
        } else popUpController.showPopUp("Заполните поля")
    }

    @FXML
    private fun initialize() {
        ipLabel.labelFor = ipInput
        loginLabel.labelFor = loginInput
        passwordLabel.labelFor = passwordInput

        val popUpLoader = FXMLLoader(javaClass.getResource("/views/PopUpView.fxml"))
        popupRoot = popUpLoader.load()
        popUpController = popUpLoader.getController()
        rootPane.children.add(popupRoot)
        popupRoot.isVisible = false
    }

    private fun hideServerSetupPane() {
        val translateTransition = TranslateTransition(Duration.millis(600.0), serverSetupPane).apply {
            toX = 1000.0
        }
        val fadeTransition = FadeTransition(Duration.millis(600.0), serverSetupPane).apply {
            fromValue = 1.0
            toValue = 0.0
        }
        val parallelTransition = ParallelTransition(translateTransition, fadeTransition)
        parallelTransition.play()
    }

    fun handleConnectionFail(message: String = "Ошибка подключения к серверу") {
        if (!State.connectedToServer) {
            ConnectionHandler.closeConnection()
            ModalConnectionFailController().showModalWindow(
                "/views/ModalConnectionFailView.fxml",
                title = message,
            )
        }
    }

    fun handleAuthFail(msg: String = "Неправильный логин или пароль") {
        State.credentials.remove("TEMP_USERNAME")
        State.credentials.remove("TEMP_PASSWORD")
        SceneHandler.switchTo("auth")
        popUpController.showPopUp(msg)
        loginInput.text = ""
        passwordInput.text = ""
    }

    fun connectToServer() {
        val fail = ConnectionHandler.initializeConnection()
        if (fail == null) hideServerSetupPane()
    }
}