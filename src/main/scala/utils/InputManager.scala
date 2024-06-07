package utils

import scalafx.Includes._
import scalafx.scene.input.KeyCode
import scalafx.scene.input.KeyCode.{
  A,
  D,
  Digit1,
  Digit2,
  Digit3,
  Digit4,
  Escape,
  R,
  S,
  W
}

import main.Game

class InputManager(game: Game) {
  private val gui = game.gui
  private val cam = game.camera

  // Handles all of the key inputs from the scene
  def handleInput() = game.scene.onKeyPressed = (e => handleKeyInput(e.getCode))

  game.scene.onKeyReleased = (e => handleKeyUnpress(e.getCode))
  handleInput()

  def handleKeyInput(code: KeyCode) = {
    code match {
      case Digit1 => gui.select(1)
      case Digit2 => gui.select(2)
      case Digit3 => gui.select(3)
      case Digit4 => gui.select(4)
      case W      => cam.addKey(code)
      case S      => cam.addKey(code)
      case A      => cam.addKey(code)
      case D      => cam.addKey(code)
      case R      => gui.rotate()
      case Escape => game.pauseAnimation()
      case other  =>
    }
  }

  def handleKeyUnpress(code: KeyCode) = {
    code match {
      case W     => cam.removeKey(code)
      case S     => cam.removeKey(code)
      case A     => cam.removeKey(code)
      case D     => cam.removeKey(code)
      case other =>
    }
  }
}
