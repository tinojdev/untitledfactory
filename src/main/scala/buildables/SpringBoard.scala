package buildables

import javafx.scene.input.MouseEvent
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, ComboBox, Label}
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.{Black, DarkBlue, Green, Red, White}
import scalafx.scene.shape.{Polygon, Rectangle}
import scalafx.scene.text.Text
import scalafx.scene.{Group, Node}

import scala.collection.mutable.{Buffer, Queue}

import main.Game
import utils.{Constants, GridSquare}
import items.Item

class SpringBoard(
    mainGridSquare: GridSquare,
    direction: Int,
    startStrength: Int
) extends Machine(mainGridSquare) {
  // Direction: 0 -> right, 1 -> down, 2 -> left, 3 -> up
  // => rotation = (direction % 4) * 90

  // If it cannot send an item to the next gridsquare it will stay here
  // Can hold more than one item if many are flying in to it at once
  val maxItems = 1
  var holdingItems: Queue[Item] = Queue()
  // Dictates how far items will fly
  private var springStrength = startStrength
  // Tells the springboard where to send items
  private var launchesTo: GridSquare = mainGridSquare
  changeStrength(springStrength)

  mainGridSquare.fill(this)

  def canReceiveItem(item: Item) = holdingItems.isEmpty

  def changeStrength(newStrength: Int): Unit = {
    val thisX = mainGridSquare.getX
    val thisY = mainGridSquare.getY
    springStrength = newStrength
    launchesTo.highlight(false)
    (direction % 4) match {
      case 0 =>
        launchesTo = game.grid.getGridAtPos(thisX + springStrength, thisY)
      case 1 =>
        launchesTo = game.grid.getGridAtPos(thisX, thisY + springStrength)
      case 2 =>
        launchesTo = game.grid.getGridAtPos(thisX - springStrength, thisY)
      case 3 =>
        launchesTo = game.grid.getGridAtPos(thisX, thisY - springStrength)
      case other =>
    }
  }

  // Holds scale up/down values for update()
  private var animationStack: Queue[Double] = Queue()
  // Machines that are removed ignore update commands
  private var removed = false

  def update(): Unit = {
    if (removed) return
    if (animationStack.nonEmpty) {
      val scaleValue = animationStack.dequeue()
      group.scaleX = group.scaleX.value + scaleValue / 4
      group.scaleY = group.scaleY.value + scaleValue / 4
    }
    // Tries to launch item in inventory
    if (holdingItems.nonEmpty) launchItem()
    // Updates window objects if the window is open
    if (windowOpen) {
      launchesTo.highlight(true)
      informationToUpdate.foreach {
        case err: Text => {
          err.text = errorMessage._1
          err.fill = errorMessage._2
        }
        case other =>
      }
    }
  }

  def remove() = {
    mainGridSquare.getItemPane.children.remove(group)
    removed = true

    if (windowOpen) {
      informationToUpdate.foreach {
        case b: Button => b.fire()
        case other     =>
      }
    }
    windowOpen = true
    holdingItems.foreach(_.remove())
    holdingItems = Queue()
  }

  def onItemEnter(item: Item) = {
    holdingItems += item
  }

  def launchItem() = {
    if (launchesTo.canReceiveItem(holdingItems(0))) {
      val itemToLaunch = holdingItems.dequeue()
      itemToLaunch.goto(launchesTo)
      animationStack = Queue()
      // Reset scale and add new values to the queue
      group.scaleX = 1
      group.scaleY = 1
      (1 until 10).foreach(n => animationStack.enqueue(1 / 10.0))
      (1 until 25).foreach(n => animationStack.enqueue(-1 / 25.0))
      closePopUpError()
    } else {
      showPopUpError()
    }
  }

  // Gives error messages for the information window
  def errorMessage: (String, Color) = {
    if (holdingItems.isEmpty) return ("Ready to launch!", Green)
    ("Cannot launch!", Red)
  }
  // Add the machines update command to the games main animation queue
  game.animations += update _

  private val group = new Group
  private val background = new Rectangle {
    x = 13
    y = 13
    width = 75
    height = 75
    stroke = Red
    strokeWidth = 5
    fill = DarkBlue
  }
  private val triangle = new Polygon {
    points.addAll(25, 25, 25, 75, 75, 50)
  }
  group.children.add(background)
  group.children.add(triangle)
  background.rotate = (direction % 4) * 90
  triangle.rotate = (direction % 4) * 90
  mainGridSquare.getItemPane.children.add(group)

  private var popUp: Option[Node] = None

  def showPopUpError(): Unit = {
    if (popUp.isDefined) return

    popUp = Some(new Text {
      text = "!"
      x = 35
      y = 10
      fill = Color.Red
      stroke = Black
      strokeWidth = 2
      font = Constants.globalFont
      style = "-fx-font-size: 100px"
      viewOrder_(-10)
    })
    group.children.add(popUp.get)
  }

  def closePopUpError(): Unit = {
    if (popUp.isEmpty) return
    group.children.remove(popUp.get)
    popUp = None
  }

  private var windowOpen = false
  // Holds the updatable nodes for later use
  private var informationToUpdate: Buffer[Node] = Buffer()

  def showWindow(event: MouseEvent): Unit = {
    if (windowOpen) return
    launchesTo.highlight(true)
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
      style = "-fx-background-color: grey;" +
        "-fx-padding: 0px 0px 0px 0px"
      onMouseDragged = (e => {
        container.layoutX = e.getSceneX - 50
        container.layoutY = e.getSceneY - 10
      })
    }
    val exitButton = new Button {
      text = "X"
      onAction = (e => {
        game.grid.root2.children.remove(container)
        windowOpen = false
        informationToUpdate = Buffer()
        launchesTo.highlight(false)
      })
      alignmentInParent = Pos.TopRight
      textFill = White
      style = "-fx-background-color: red;" +
        "-fx-background-radius: 0px;"
      translateX = 5
    }
    innerContainer.children = exitButton
    val innerContainer2 = new StackPane {}

    val currentAmount = new Label {
      text = "Spring strength"
      font = Constants.globalFont
    }
    innerContainer2.children = currentAmount
    val innerContainer3 = new StackPane {}
    val choiceBox = new ComboBox[String] {
      items = ObservableBuffer("1", "2", "3", "4")
      onAction = (a => changeStrength(this.getValue.toInt))
    }
    choiceBox.getSelectionModel.select(startStrength - 1)
    innerContainer3.children.add(choiceBox)
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
      Buffer(exitButton, choiceBox, currentAmount, statusMessage)
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
