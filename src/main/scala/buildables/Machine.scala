package buildables

import javafx.scene.input.MouseEvent

import scala.collection.mutable.{Buffer, Queue}

import utils.{GridSquare, Constants}
import items.Item

abstract class Machine(mainGridSquare: GridSquare) {
  val game = mainGridSquare.getGame
  val maxItems: Int
  var holdingItems: Queue[Item]

  def update(): Unit

  def onItemEnter(item: Item): Unit

  def canReceiveItem(item: Item): Boolean

  def remove(): Unit

  def showWindow(event: MouseEvent): Unit

  def getAdjacent: Buffer[GridSquare] = {

    var collector: Buffer[GridSquare] = Buffer()
    val thisX = mainGridSquare.getX
    val thisY = mainGridSquare.getY
    // Gets the adjacent gridsquares of this 2x2 machine
    for (i <- -1 to 2) {
      for (j <- -1 to 2) {
        // Ignore the squares where this machine is and those that are inaccesible
        try {
          val gridPos = game.grid.getGridAtPos(thisX + i, thisY + j)
          if (!gridPos.canReceiveMachine && gridPos.getMachine.get == this)
            throw new Exception("Already taken")
          if (
            thisX + i >= Constants.gridSize || thisY + j >= Constants.gridSize
          ) throw new Exception("Out of bounds")
          if (thisX + i < 0 || thisY + j < 0)
            throw new Exception("Out of bounds")
          collector += gridPos
        } catch {
          case e: IndexOutOfBoundsException =>
          case e: Exception                 =>
        }
      }
    }

    collector
  }

}
