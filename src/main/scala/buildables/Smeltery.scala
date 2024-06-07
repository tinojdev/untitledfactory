package buildables

import javafx.scene.input.MouseEvent
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, Label, ProgressBar}
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.{
  Black,
  Blue,
  Green,
  LightGray,
  Red,
  White,
  Yellow
}
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.scene.{Group, Node}

import scala.collection.mutable.{Buffer, Queue}
import scala.util.Random

import utils.{Constants, GridSquare}
import items.{Item, Iron}

class Smeltery(mainGridSquare: GridSquare) extends Machine(mainGridSquare) {

  private val gridX = mainGridSquare.getX
  private val gridY = mainGridSquare.getY

  // Fill all of the squares that the machine inhabits
  // Returns with exceptions if these are not "legal" spots
  if (gridX + 1 >= Constants.gridSize || gridY + 1 >= Constants.gridSize)
    throw new Exception("Trying to place out of bounds!")

  // Holds all of the gridsquares where this machine is
  private val gridsToFill = Vector(
    game.grid.getGridAtPos(gridX + 1, gridY),
    game.grid.getGridAtPos(gridX, gridY + 1),
    game.grid.getGridAtPos(gridX + 1, gridY + 1)
  )
  if (
    !gridsToFill.forall(
      _.canReceiveMachine
    ) || !mainGridSquare.canReceiveMachine
  ) throw new Exception("The square is already taken!")
  gridsToFill.foreach(g => {
    g.fill(this)
  })
  mainGridSquare.fill(this)

  private val rand = new Random()
  private var rotationSpeed = 1

  // Keeps track of every available GridSquare where this machine can throw items to.
  private var throwsTo: Buffer[GridSquare] = getAdjacent

  var holdingItems: Queue[Item] = Queue()
  var holdingItemType: Option[Item] = None
  val maxItems: Int = 50
  private var canReceive = true

  def canReceiveItem(item: Item) =
    canReceive && (holdingItemType.isEmpty || holdingItemType.get.getClass == item.getClass)

  def onItemEnter(item: Item) = {
    holdingItemType = Some(item)
    holdingItems += item
    if (holdingItems.length >= maxItems) canReceive = false
    else canReceive = true
    item.setInMachine(true)
  }

  def errorMessage: (String, Color) = {
    if (!canReceive) {
      if (!throwsTo.exists(_.canReceiveItem(holdingItemType.get)))
        return ("Inventory full, no free spaces near!", Red)
      return ("Inventory full", Red)
    }
    if (
      holdingItemType.isDefined && !throwsTo.exists(
        _.canReceiveItem(holdingItemType.get)
      )
    ) return ("No free spaces near!", Red)
    if (holdingItems.nonEmpty) return ("Working correctly!", Green)
    ("No items to smelt!", Yellow)
  }

  private var popUp: Option[Node] = None

  def showPopUpError(): Unit = {
    if (popUp.isDefined) return

    popUp = Some(new Text {
      text = "!"
      x = 75
      y = 165
      fill = Color.Red
      stroke = Black
      strokeWidth = 3
      font = Constants.globalFont
      style = "-fx-color: #b80c00;" +
        "-fx-font-size: 200px"
    })
    group.children.add(popUp.get)
  }

  def closePopUpError(): Unit = {
    if (popUp.isEmpty) return
    group.children.remove(popUp.get)
    popUp = None
  }

  private val processingTime: Int = Constants.smelterySpeed
  private var timer = 0
  private var removed = false

  def update(): Unit = {
    if (removed) return
    if (
      holdingItems.nonEmpty && throwsTo.exists(
        _.canReceiveItem(holdingItemType.get)
      )
    ) {
      closePopUpError()
      timer += 1
      if (timer > processingTime * 10) {
        val itemToProcess = holdingItems.dequeue()
        val itemToThrow =
          new Iron(itemToProcess.currentLocation, itemToProcess.currentLocation)
        itemToProcess.remove()
        val throwTo = throwsTo.filter(_.canReceiveItem(holdingItemType.get))
        itemToThrow.setInMachine(false)
        itemToThrow.goto(throwTo(rand.between(0, throwTo.length)))
        if (holdingItems.isEmpty) holdingItemType = None
        timer = 0
      }
      spinner.rotate = (spinner.rotate + rotationSpeed).toDouble
    } else {
      if (
        holdingItemType.isDefined && !throwsTo.exists(
          _.canReceiveItem(holdingItemType.get)
        )
      ) showPopUpError()
    }
    // Update window objects if it is open
    if (windowOpen) {
      throwsTo.foreach(_.highlight(true))
      informationToUpdate.foreach {
        case pr: ProgressBar => pr.progress = timer / (processingTime * 10.0)
        case la: Label       => la.text = holdingItems.length + " / " + maxItems
        case txt: Text => {
          txt.text = errorMessage._1
          txt.fill = errorMessage._2
        }
        case other =>
      }
    }
  }

  def remove() = {
    gridsToFill.foreach(_.remove(false))
    mainGridSquare.remove(false)
    mainGridSquare.getItemPane.children.remove(group)
    removed = true
    if (windowOpen) {
      informationToUpdate.foreach {
        case b: Button => b.fire()
        case other     =>
      }
      // to prevent opening any new windows
      windowOpen = true
      holdingItems.foreach(_.remove())
      holdingItems = Queue()
    }
  }
  // Add the update function to the games main animation buffer
  mainGridSquare.getGame.animations += (update)

  // Constructs the rendered object
  private val backGround = new Rectangle {
    x = -1
    y = -1
    width = 200
    height = 200
    stroke = Black
    fill = LightGray
  }
  private val spinner = new Rectangle {
    x = 30
    y = 30
    width = 140
    height = 140
    rotate = 45
    stroke = Black
    fill = Blue
  }
  private val group = new Group
  group.children = Vector(backGround, spinner)
  group.onMouseClicked = (e => showWindow(e))
  mainGridSquare.getItemPane.children.add(group)

  private var windowOpen = false
  // Holds the updatable nodes for later use
  private var informationToUpdate: Buffer[Node] = Buffer()

  // Constructs the information window for this machine
  def showWindow(event: MouseEvent): Unit = {
    if (windowOpen) return
    throwsTo.foreach(_.highlight(true))
    val container = new VBox {
      layoutY = event.getSceneY
      layoutX = event.getSceneX
      style = "-fx-background-color: lightgrey;" +
        "-fx-border-color: grey;" +
        "-fx-border-width: 0px 5px 5px 5px;"
      maxWidth = 100
      maxHeight = 200
    }
    val innerContainer = new StackPane {
      onMouseDragged = (e => {
        container.layoutX = e.getSceneX - 50
        container.layoutY = e.getSceneY - 10
      })
      style = "-fx-background-color: grey;" +
        "-fx-padding: 0px 0px 0px 0px"
    }
    val exitButton = new Button {
      text = "X"
      onAction = (e => {
        throwsTo.foreach(_.highlight(false))
        game.grid.root2.children.remove(container)
        windowOpen = false
        informationToUpdate = Buffer()
      })
      alignmentInParent = Pos.TopRight
      textFill = White
      style = "-fx-background-color: red;" +
        "-fx-background-radius: 0px;"
      viewOrder_(-1)
    }
    innerContainer.children = exitButton
    val innerContainer2 = new StackPane {}
    val currentAmount = new Label {
      text = holdingItems.length + " / " + maxItems
      font = Constants.globalFont
    }
    innerContainer2.children = currentAmount
    val innerContainer3 = new StackPane {}
    val progressBar = new ProgressBar {
      progress = timer / (processingTime * 100)
    }
    innerContainer3.children = progressBar
    val innerContainer4 = new StackPane {}
    val currentStatus = new Label {
      text = "Status: "
      font = Constants.globalFont
    }
    innerContainer4.children = currentStatus

    val innerContainer5 = new StackPane {}
    val statusMessage = new Text {
      text = errorMessage._1
      fill = errorMessage._2
    }
    innerContainer5.children = statusMessage
    informationToUpdate =
      Buffer(exitButton, currentAmount, progressBar, statusMessage)
    container.children = Vector(
      innerContainer,
      innerContainer2,
      innerContainer3,
      innerContainer4,
      innerContainer5
    )
    game.grid.root2.children.add(container)
    windowOpen = true
  }
}
