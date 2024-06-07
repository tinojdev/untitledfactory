package main

import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, StackPane}
import scalafx.scene.text.Text

import scala.collection.mutable.Buffer

import buildables.{Blueprints, ProgressionBasket, Smeltery, SpringBoard}
import utils.{Constants, GridSquare}
import items.Iron

class Gui(game: Game) {

  // Which toolbar item should be highlighted (0) is none at all
  var selected = 0

  private var userDir = System.getProperty("user.dir")

  def select(toSelect: Int): Unit = {

    // If selecting the same tool remove the selection
    if (toSelect == selected && toSelect != 0) {
      select(0)
      return
    }

    // Based on which selected change that tools border color
    if (toSelect == 0) {
      selected = toSelect
      tools.foreach(t => {
        t.style = "-fx-background-color: lightgrey;" +
          "-fx-border-color: darkgrey;" +
          "-fx-border-width: 4px;" +
          "-fx-border-radius: 8px;" +
          "-fx-background-radius: 10px;"
      })
      ghosts.foreach(_.opacity = 0)
      // Doesn't select it if the tool is locked
    } else if (!lockedTools.exists(a => a._1 == toSelect)) {
      selected = toSelect
      tools.foreach(t => {
        t.style = "-fx-background-color: lightgrey;" +
          "-fx-border-color: darkgrey;" +
          "-fx-border-width: 4px;" +
          "-fx-border-radius: 8px;" +
          "-fx-background-radius: 10px;"
      })
      tools(toSelect - 1).style = "-fx-background-color: lightgrey;" +
        "-fx-border-color: red;" +
        "-fx-border-width: 4px;" +
        "-fx-border-radius: 8px;" +
        "-fx-background-radius: 10px;"
      ghosts.foreach(_.opacity = 0)
      ghosts(selected - 1).opacity = 0.5
    }
  }

  // Holds all of the locked tools
  private var lockedTools: Buffer[(Int, ImageView)] = Buffer()

  def lock(toolToLock: Int) = {
    val img = new ImageView {
      image = new Image("file:" + userDir + "/src/icons/lock.png")
    }
    img.fitWidth = 75
    img.fitHeight = 75
    img.scaleX = 1.5
    img.scaleY = 1.5
    img.viewOrder_(-100)
    lockedTools += ((toolToLock, img))
    tools(toolToLock - 1).children.add(img)
  }

  def unlock(toolToUnlock: Int) = {
    val toRemove = lockedTools.filter(_._1 == toolToUnlock).head
    lockedTools = lockedTools.filterNot(_._1 == toolToUnlock)
    tools(toolToUnlock - 1).children.remove(toRemove._2)
  }

  private var rotateAmount = 0

  def rotate() = {
    if (selected == 1) {
      ghosts(0).rotate = (ghosts(0).rotate + 90).toDouble
      rotateAmount += 1
    }
  }

  def squareClicked(gridSquare: GridSquare): Unit = {
    try {
      selected match {
        case 0 =>
        case 1 => {
          new SpringBoard(gridSquare, rotateAmount % 4, 2)
        }
        case 2 => {
          new Smeltery(gridSquare)
        }
        case 3 => {
          new ProgressionBasket(
            gridSquare,
            new Iron(gridSquare, gridSquare),
            3000
          )
        }
        case 4 => {
          gridSquare.remove(true)
          gridSquare.removeItems()
        }
        case other =>
      }
    } catch {
      case e: Exception =>
    }
  }

  // Holds all of the "ghost" nodes for placing machines
  private val ghosts = Vector(
    Blueprints.ghostSpringBoard,
    Blueprints.ghostSmeltery,
    Blueprints.ghostProgressionBasket,
    Blueprints.ghostCross
  )
  ghosts.foreach(_.opacity = 0)

  def handleMouseInput(e: MouseEvent, itemPane: Pane) = {
    if (selected != 0) {
      try {
        ghosts.foreach(b => itemPane.getChildren.add(b))
        ghosts.foreach(_.opacity = 0)
        ghosts(selected - 1).opacity = 0.5
      } catch {
        case e: IllegalArgumentException =>
      }
    } else {
      ghosts.foreach(_.opacity = 0)
    }
  }

  private val container = new HBox {
    alignmentInParent = Pos.BottomCenter
    alignment = Pos.Center
    maxHeight = 125
    prefHeight = 125
    minHeight = 125
    maxWidth = 600
    prefWidth = 600
    minWidth = 600
    spacing = 10
    style = "-fx-background-color: grey;" +
      "-fx-padding: 0px 10px 0px 10px;" +
      "-fx-background-radius: 20px 20px 0px 0px"

  }
  // Holds all of the containers for toolbar items for easier selecting later
  private val tools: Buffer[StackPane] = Buffer()
  // All of the tool's nodes
  private val itemList = Vector(
    Blueprints.guiSpringBoard,
    Blueprints.guiSmeltery,
    Blueprints.guiProgressionBasket,
    Blueprints.guiCross
  )
  for (i <- itemList.indices) {
    val innerContainer = new StackPane {
      maxHeight = 90
      minWidth = 100
      alignment = Pos.Center
      padding = Insets(15)

      style = "-fx-background-color: lightgrey;" +
        "-fx-border-color: darkgrey;" +
        "-fx-border-width: 4px;" +
        "-fx-border-radius: 8px;" +
        "-fx-background-radius: 10px;"
      onMouseClicked = (
          e =>
            style = "-fx-background-color: lightgrey;" +
              "-fx-border-color: red;" +
              "-fx-border-width: 4px;" +
              "-fx-border-radius: 8px;" +
              "-fx-background-radius: 10px;"
      )
    }
    tools += innerContainer
    container.children.add(innerContainer)
    val item = itemList(i)
    innerContainer.children.add(item)
    val text = new Text {
      alignmentInParent = Pos.BaselineLeft
      translateX = -7
      translateY = -3
      text = (i + 1).toString + "."
      font = Constants.globalFont
      scaleX = 1.5
      scaleY = 1.5
    }
    innerContainer.children.add(text)
    item.alignmentInParent = Pos.Center
  }
  game.root.children.add(container)
  container.viewOrder_(-1000)

}
