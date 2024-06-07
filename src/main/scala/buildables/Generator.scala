package buildables

import javafx.scene.input.MouseEvent
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, Label, ProgressBar}
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.{Black, Green, Grey, Red, White}
import scalafx.scene.shape.{Circle, Rectangle}
import scalafx.scene.text.Text
import scalafx.scene.{Group, Node}

import scala.collection.mutable
import scala.collection.mutable.{Buffer, Queue}
import scala.util.Random

import items.{Item, IronOre}
import utils.{Constants, GridSquare}

class Generator(mainGridSquare: GridSquare, item: Item)
    extends Machine(mainGridSquare) {

  // Initializes the class returns if it runs in to exceptions
  // ---------------------------------------------------------

  item.remove()

  private val gridX = mainGridSquare.getX
  private val gridY = mainGridSquare.getY

  // Fill all of the squares next to this one which this machine inhabits
  if (gridX + 1 >= Constants.gridSize || gridY + 1 >= Constants.gridSize)
    throw new Exception("Trying to place out of bounds!")

  val gridsToFill = Vector(
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
  val rand = new Random()
  // ---------------------------------------------------------

  // Cant receive items
  val maxItems = 0
  var holdingItems = Queue()

  def canReceiveItem(item: Item): Boolean = false

  def onItemEnter(item: Item) = {}

  private val throwsTo: Buffer[GridSquare] = getAdjacent

  private val processingTime = Constants.generatorSpeed
  private var timer = 0
  private var animationQueue: mutable.Queue[Int] = mutable.Queue()
  (1 to 50).toVector.foreach(a => animationQueue.enqueue(-1))
  (1 to 50).toVector.foreach(a => animationQueue.enqueue(1))

  private var removed = false

  def update(): Unit = {
    if (removed) return
    if (throwsTo.exists(_.canReceiveItem(item))) {
      closePopUpError()
      timer += 1
      if (timer > processingTime * 10) {
        val throwTo = throwsTo.filter(_.canReceiveItem(item))
        val itemToThrow =
          new IronOre(mainGridSquare, throwTo(rand.nextInt(throwTo.length)))
        timer = 0
      }
      val scaleValue = animationQueue.dequeue()
      animationQueue.enqueue(scaleValue)
      upperCircle.scaleX = (upperCircle.scaleX + scaleValue * 0.01).toDouble
      upperCircle.scaleY = (upperCircle.scaleY + scaleValue * 0.01).toDouble
    } else {
      showPopUpError()
    }
    if (windowOpen) {
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

  def remove(): Unit = {
    // Generators cannot be removed
    if (true) return
    gridsToFill.foreach(_.remove(false))
    mainGridSquare.remove(false)
    mainGridSquare.getItemPane.children.remove(group)
    removed = true

    if (windowOpen) {
      informationToUpdate.foreach {
        case b: Button => b.fire()
        case other     =>
      }
    }
  }

  def errorMessage: (String, Color) = {
    if (!throwsTo.exists(_.canReceiveItem(item)))
      return ("No free spaces near!", Red)
    ("Generating ores!", Green)
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

  private val group = new Group
  private val background = new Rectangle {
    x = -1
    y = -1
    width = 200
    height = 200
    stroke = Black
    fill = Grey.darker.darker.darker
  }
  private val upperCircle = new Circle {
    centerX = 100
    centerY = 100
    radius = 75
    stroke = Black
    fill = Red
    scaleX = 1.2
    scaleY = 1.2
  }
  group.children.add(background)
  group.children.add(upperCircle)
  mainGridSquare.getItemPane.children.add(group)
  game.animations += update _

  // Constructs the information window for this machine
  private var windowOpen = false
  // Holds the updatable nodes for later use
  private var informationToUpdate: Buffer[Node] = Buffer()

  def showWindow(event: MouseEvent): Unit = {
    if (windowOpen) return
    game.tutorial.triggerFirstCheckpoint()
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
        game.grid.root2.children.remove(container)
        windowOpen = false
        informationToUpdate = Buffer()
        throwsTo.foreach(_.highlight(false))
      })
      alignmentInParent = Pos.TopRight
      textFill = White
      style = "-fx-background-color: red;" +
        "-fx-background-radius: 0px;"
      viewOrder_(-1)
    }
    innerContainer.children = exitButton
    val innerContainer3 = new StackPane {}
    val progressBar = new ProgressBar {
      progress = timer / (processingTime * 10)
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
    informationToUpdate = Buffer(exitButton, progressBar, statusMessage)
    container.children =
      Vector(innerContainer, innerContainer3, innerContainer4, innerContainer5)
    game.grid.root2.children.add(container)
    windowOpen = true
  }
}
