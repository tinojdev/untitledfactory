package items

import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.{Black, Red, Transparent, Yellow}
import scalafx.scene.shape.Rectangle

import utils.{Constants, GridSquare}

abstract class Item(startLocation: GridSquare, startTarget: GridSquare) {

  var currentLocation: GridSquare = startLocation
  private var targetLocation: Option[GridSquare] = None
  private var inMachine = false
  val color: Color
  // Used to calculate item render position in between squares
  private var isMoving = false
  private var x0ffset = 0.0
  private var xDis = 0.0
  private var yOffset = 0.0
  private var yDis = 0.0
  private var zOffset = 0.0
  private var zDis = 0.0

  private var timeToMove = 0.0
  private var movedFor = 0.0

  val renderableObject: Node

  def setInMachine(in: Boolean) = {
    inMachine = in
    renderableObject match {
      case rec: Rectangle => {
        if (in) {
          rec.fill = Transparent
          rec.stroke = Transparent
        } else {
          rec.fill = color
          rec.stroke = Black
        }
      }
      case other =>
    }

  }

  def update() = {
    if (isMoving) {
      if (movedFor <= 1) {
        // Upon reaching target reset numbers and set the new square as current location
        x0ffset += ((xDis * 100) / timeToMove) * movedFor
        yOffset += ((yDis * 100) / timeToMove) * movedFor
        currentLocation.getItemPane.children.remove(renderableObject)
        targetLocation.get.getItemPane.children.add(renderableObject)
        currentLocation = targetLocation.get
        targetLocation = None
        isMoving = false
        x0ffset = 0
        yOffset = 0
        zOffset = 0
        currentLocation.addItem(this)

      } else {
        x0ffset += (xDis * 100) / timeToMove
        yOffset += (yDis * 100) / timeToMove

        // After the midpoint item starts to scale down and before it scales up
        if (movedFor > (timeToMove / 2))
          zOffset += (Constants.itemMaxHeight / 10.0) / timeToMove
        else
          zOffset -= (Constants.itemMaxHeight / 10.0) / timeToMove

        movedFor -= 1
      }
    }
    renderableObject.layoutX = x0ffset
    renderableObject.layoutY = yOffset
    renderableObject.scaleX = zOffset + 1
    renderableObject.scaleY = zOffset + 1
    renderableObject.viewOrder_(-2)
  }

  def remove(): Unit = {
    currentLocation.getItemPane.children.remove(renderableObject)
    currentLocation.getGame.animations -= update
  }

  def goto(destination: GridSquare): Unit = {
    // Return if trying to go to the same square of if already moving
    if (destination == currentLocation || isMoving) return
    targetLocation = Some(destination)
    destination.reserve()
    // Calculate the distances that the item needs to travel and how long is it going to take.
    xDis = destination.getCoords._1 - currentLocation.getCoords._1
    yDis = destination.getCoords._2 - currentLocation.getCoords._2
    timeToMove = math.hypot(xDis, yDis) * Constants.itemSpeed
    movedFor = timeToMove
    isMoving = true
  }

  goto(startTarget)
  startLocation.getGame.animations += update
}

class IronOre(startLocation: GridSquare, startTarget: GridSquare)
    extends Item(startLocation, startTarget) {

  val color = Red

  val renderableObject = {
    val rec = new Rectangle {
      x = 25
      y = 25
      width = 50
      height = 50
      stroke = Black
      fill = color
      viewOrder_(-2)
    }
    rec
  }
  // startLocation.getGame.grid.itemGrid.children.add(renderableObject)
  currentLocation.getItemPane.children.add(renderableObject)
}

class Iron(startLocation: GridSquare, startTarget: GridSquare)
    extends Item(startLocation, startTarget: GridSquare) {

  val color = Yellow

  val renderableObject = {
    new Rectangle {
      x = 25
      y = 25
      width = 50
      height = 50
      stroke = Black
      fill = color
      viewOrder_(-2)
    }
  }
  currentLocation.getItemPane.children.add(renderableObject)

}
