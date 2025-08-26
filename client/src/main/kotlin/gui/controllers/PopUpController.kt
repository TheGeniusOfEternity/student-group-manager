package gui.controllers

import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.TranslateTransition
import javafx.fxml.FXML
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import javafx.util.Duration

class PopUpController {
    @FXML
    private lateinit var popUp: AnchorPane
    @FXML
    private lateinit var popUpText: Text

    fun hidePopUp() {
        val fadeTransition = FadeTransition(Duration.millis(300.0), popUp).apply {
            fromValue = 1.0
            toValue = 0.0
        }
        fadeTransition.play()
    }

    fun showPopUp(message: String = "Error") {
        val translateTransition = TranslateTransition(Duration.millis(300.0), popUp).apply {
            fromX = -300.0
            toX = 0.0
        }
        val fadeTransition = FadeTransition(Duration.millis(300.0), popUp).apply {
            fromValue = 0.0
            toValue = 1.0
        }
        val parallelTransition = ParallelTransition(translateTransition, fadeTransition)
        popUpText.text = message
        popUp.isVisible = true
        parallelTransition.play()
    }
}