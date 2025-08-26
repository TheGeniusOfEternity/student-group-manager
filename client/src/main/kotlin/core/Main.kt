package core

import gui.controllers.AuthController
import gui.controllers.MainController
import handlers.ConnectionHandler
import handlers.IOHandler
import handlers.SceneHandler
import invoker.Invoker
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.util.Duration
import java.io.IOException
import kotlin.system.exitProcess

/**
 * A type alias for a list of [Property], used for representing collections of text data.
 */
typealias GroupData = ArrayList<Pair<String, String?>>
/**
 * A type alias for pair (propertyName, propertyValue)
 */
typealias Property = Pair<String, String?>


class Main : Application() {
    override fun start(stage: Stage) {
        val authLoader = FXMLLoader(javaClass.getResource("/views/AuthView.fxml"))
        val mainLoader = FXMLLoader(javaClass.getResource("/views/MainView.fxml"))

        SceneHandler.init(stage)
        SceneHandler.addSceneAndController("auth", authLoader)
        SceneHandler.addSceneAndController("main", mainLoader)
        SceneHandler.switchTo("auth")

        stage.title = "Student Group Manager - Authorization"
        stage.isResizable = false
        stage.show()

        val timeline = Timeline(KeyFrame(Duration.seconds(0.5), {
            if (!State.isRunning) this.stop()
            else if (!State.connectedToServer && State.host != null)
                    (SceneHandler.controllers["auth"] as AuthController).handleConnectionFail()
            IOHandler.handle()
        }))
        timeline.cycleCount = Timeline.INDEFINITE  // бесконечный цикл
        timeline.play()
    }
}

/**
 * Entry point of the program
 */
fun main() {
    State.isRunning = true
    IOHandler.loadCredentials()
    Application.launch(Main::class.java)
}