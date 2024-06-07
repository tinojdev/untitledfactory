package menus

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox
import scalafx.stage.Stage

import utils.Constants
import main.Game

class StartMenu(stage: Stage) {

  private val container = new VBox {
    padding = Insets(25, 100, 25, 100)
    spacing = 20
    alignment = Pos.TopCenter
  }
  private val titleText = new Label {
    text = "Untitled\nFactory"
    font = Constants.globalFont
    alignment = Pos.Center
    style = "-fx-font-size: 25px;"
  }
  private val startButton = new Button {
    text = "Start a new game"
    font = Constants.globalFont
    prefHeight = 50
    maxWidth = 300
    alignment = Pos.Center
    onAction = (e => {
      new Game(stage)
    })
  }
  private val exitButton = new Button {
    text = "Exit game"
    font = Constants.globalFont
    prefHeight = 50
    maxWidth = 300
    alignment = Pos.Center
    onAction = (e => {
      stage.close()
    })
  }
  container.children = Array(titleText, startButton, exitButton)

  private val scene = new Scene(container)
  stage.scene = scene
}
