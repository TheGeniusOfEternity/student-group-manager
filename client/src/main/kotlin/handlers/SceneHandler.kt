package handlers

import invoker.Invoker
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import kotlin.system.exitProcess

object SceneHandler {
    private val scenes = mutableMapOf<String, Scene>()
    val controllers = mutableMapOf<String, Any>()
    var primaryStage: Stage? = null
    var modalStage: Stage? = null
    fun init(stage: Stage) {
        primaryStage = stage
    }

    /**
     * Add new scene and controller
     * @param name - name of the scene
     * @param loader - [FXMLLoader] instance for loading scene from fxml file
     */
    fun addSceneAndController(name: String, loader: FXMLLoader) {
        scenes[name] = Scene(loader.load())
        controllers[name] = loader.getController()
    }

    /**
     * Switch scene by its name
     * @param sceneName - name of the scene to switch to
     * @param title - title of new window after scene switch
     */
    fun switchTo(sceneName: String, title: String = "Student Group Manager") {
        val scene = scenes[sceneName]
        if (scene != null && primaryStage != null) {
            primaryStage!!.scene = scene
            primaryStage!!.title = title
            primaryStage?.setOnCloseRequest {
                Invoker.commands["exit"]!!.execute(listOf())
                Platform.exit()
                exitProcess(0)
            }
            primaryStage!!.centerOnScreen()
        } else {
            throw IllegalArgumentException("Scene '$sceneName' not found or Stage not initialized")
        }
    }
}