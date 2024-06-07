package utils

import scalafx.scene.input.KeyCode
import scalafx.scene.input.KeyCode.{A, D, S, W}

import main.Game

class Camera(game: Game) {

  private var posX = 0
  private var posY = 0
  private var cameraSpeed = 10

  def getX = posX

  def getY = posY

  def setX(i: Int) = posX = i

  def setY(i: Int) = posY = i

  private var movingUp = false
  private var movingDown = false
  private var movingLeft = false
  private var movingRight = false

  def addKey(k: KeyCode) = {
    k match {
      case W     => movingUp = true
      case S     => movingDown = true
      case A     => movingLeft = true
      case D     => movingRight = true
      case other =>
    }
  }

  def removeKey(k: KeyCode) = {
    k match {
      case W     => movingUp = false
      case S     => movingDown = false
      case A     => movingLeft = false
      case D     => movingRight = false
      case other =>
    }
  }

  def update() = {

    if (movingUp) posY = posY + 1 * cameraSpeed
    if (movingDown) posY = posY - 1 * cameraSpeed
    if (movingLeft) posX = posX + 1 * cameraSpeed
    if (movingRight) posX = posX - 1 * cameraSpeed

  }

}
