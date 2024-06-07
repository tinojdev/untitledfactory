package menus

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Button
import scalafx.scene.input.KeyCode.Escape
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout.VBox

import main.Game
import utils.Constants

class PauseMenu(game: Game) {

  private val container = new VBox {
    padding = Insets(25, 100, 25, 100)
    spacing = 20
    alignment = Pos.Center
    maxWidth = 300
    maxHeight = 250
    style = "-fx-background-color: #787878;" +
      "-fx-background-radius: 20px"

  }
  private val continueButton = new Button {
    text = "Continue"
    font = Constants.globalFont
    prefHeight = 50
    maxWidth = 300
    alignment = Pos.Center
    onAction = (e => {
      game.animator.start()
      game.root.children.remove(container)
      game.input.handleInput()
      game.inMenu = false
    })
  }
  private val exitButton = new Button {
    text = "Exit"
    font = Constants.globalFont
    prefHeight = 50
    maxWidth = 300
    alignment = Pos.Center
    onAction = (e => {
      new StartMenu(game.getStage)
    })
  }
  // Has to be done this way for some reason
  game.scene.onKeyPressed = (e: KeyEvent) => {
    continue(e.getCode)
  }

  def continue(e: KeyCode) = if (e == Escape) continueButton.fire()

  container.children = Array(continueButton, exitButton)
  game.root.children.add(container)
}
