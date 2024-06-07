package main

import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.text.Text
import scalafx.stage.Stage

import scala.collection.mutable
import utils.{Constants, Camera, Grid, InputManager}
import menus.PauseMenu
import main.Gui
import buildables.{Generator, ProgressionBasket}
import items.{Iron, IronOre}

class Game(stage: Stage) {
  def getStage = stage

  val root = new StackPane()
  root.setFocusTraversable(false)
  val scene = new Scene(root)
  // val root = new Pane()
  // .children.add(root)

  stage.scene = scene
  // Grid.getGrids.foreach(g => root.children.add(g.render()))
  val animator: Ticker = new Ticker(animateAll)

  val gui = new Gui(this)

  val camera = new Camera(this)
  val input = new InputManager(this)
  val grid = new Grid(this)
  val gridHolder = grid.getGridHolder
  val tutorial = new Tutorial(this)

  // gui.bp.foreach(root.children.add(_))

  var inMenu = false

  def pauseAnimation() = {
    if (!inMenu) {
      animator.stop()
      new PauseMenu(this)
      inMenu = true
    }
  }

  // Holds all of the update methods of each machine and item on screen
  // Also includes their index as an integer so they can be removed later
  var animations = mutable.Buffer(camera.update _, grid.update _)

  def animateAll() = {

    animations.foreach(a => a.apply())
  }

  animator.start()

  // Sets up the start area
  camera.setX(Constants.gridSize * -30)
  camera.setY(Constants.gridSize * -30)
  new Generator(
    grid.getGridAtPos(4, 2),
    new IronOre(grid.getGridAtPos(4, 2), grid.getGridAtPos(4, 2))
  )
  new Generator(
    grid.getGridAtPos(20, 0),
    new IronOre(grid.getGridAtPos(20, 0), grid.getGridAtPos(20, 0))
  )
  new ProgressionBasket(
    grid.getGridAtPos(10, 15),
    new IronOre(grid.getGridAtPos(10, 15), grid.getGridAtPos(10, 15)),
    50
  )
  new ProgressionBasket(
    grid.getGridAtPos(22, 20),
    new Iron(grid.getGridAtPos(22, 20), grid.getGridAtPos(22, 20)),
    50
  )
  gui.lock(1)
  gui.lock(2)
  gui.lock(3)
}

class Tutorial(game: Game) {

  var firstCheckPointTriggered = false

  def triggerFirstCheckpoint(): Unit = {
    if (firstCheckPointTriggered) return
    firstCheckPointTriggered = true
    tutorialText.text =
      "Congratulations on completing your first objective!\n" +
        "This is a generator, it spits out iron ores around itself\n" +
        "Check out your new objective"
    objectives.text = "Find and click on a progression basket.\n" +
      "(Hint: grey circle)"

  }

  var secondCheckpointTriggered = false

  def triggerSecondCheckpoint(): Unit = {
    if (!firstCheckPointTriggered || secondCheckpointTriggered) return
    game.gui.unlock(1)
    secondCheckpointTriggered = true
    tutorialText.text = "Congratulations, you've unlocked springboards!\n" +
      "This is a progression basket, your task is to fill it with ores using springboards.\n" +
      "You can press the number 1 key to select a springboard and mouse1 to place it down"
    objectives.text = "Fill the nearest progression basket\n" +
      "(Hint: rotate springboards with the R key.\n" +
      "select the bulldozer with number4 to erase mistakes)"
  }

  var thirdCheckpointTriggered = false

  def triggerThirdCheckpoint(): Unit = {
    if (!secondCheckpointTriggered || thirdCheckpointTriggered) return
    game.gui.unlock(2)
    thirdCheckpointTriggered = true
    tutorialText.text = "That filled up nicely! You've unlocked smelteries\n" +
      "Smelteries can be used to smelt iron ore in to iron.\n" +
      "Select them with the number2 -key and get building!"
    objectives.text = "Fill the furthest progression basket\n" +
      "(Hint: click on springboards to change their distance\n" +
      "select the bulldozer with number4 to erase mistakes)"
  }

  var fourthCheckpointTriggered = false

  def triggerFourthCheckpoint(): Unit = {
    if (!thirdCheckpointTriggered || fourthCheckpointTriggered) return
    game.gui.unlock(3)
    fourthCheckpointTriggered = true
    tutorialText.text = "Congratulations you've won the game!!\n" +
      "You can continue playing around in this sandbox if you want.\n" +
      "You've unlocked a nigh infinite progression basket!"
    objectives.text = "Nothing :)\n" +
      "(Hint: play again!)"
  }

  val tutorialText = new Text {
    text = "Hi, and welcome to Untitled Factory!\n" +
      "You can move around using WASD -keys and zoom in and out using the scroll wheel.\n" +
      "You can find your current objective to the right of me!"
    alignmentInParent = Pos.TopCenter
    font = Constants.globalFont
    style = "-fx-text-alignment: center;" +
      "-fx-font-size: 20px;" +
      "-fx-font-weight: bold;"
    mouseTransparent = true
  }
  game.root.children.add(tutorialText)
  val objectiveHolder = new VBox {
    alignmentInParent = Pos.TopRight
    maxWidth = 250
    maxHeight = 150
    style = "-fx-background-color: #b8b8b8;" +
      "-fx-border-color: #6d6d6e;" +
      "-fx-border-width: 5px;"

  }
  game.root.children.add(objectiveHolder)
  val objectiveLabel = new Label {
    text = "Current Objective:"
    alignmentInParent = Pos.TopLeft
    font = Constants.globalFont
    style = "-fx-text-alignment: left;" +
      "-fx-font-size: 20px;" +
      "-fx-font-weight: bold;"
  }
  objectiveHolder.children.add(objectiveLabel)
  val objectives = new Text {
    text = "- Find and click on an iron ore generator\n" +
      "(Hint: its the dark grey cube\n" +
      "with a red circle inside it)"
    alignmentInParent = Pos.TopRight
    font = Constants.globalFont
    style = "-fx-text-alignment: left;" +
      "-fx-font-size: 20px;" +
      "-fx-font-weight: bold;"
    wrappingWidth = 250
  }
  objectiveHolder.children.add(objectives)

}
