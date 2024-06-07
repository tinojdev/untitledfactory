package buildables

import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color.{Black, Blue, DarkBlue, LightGray, Red, Yellow}
import scalafx.scene.shape.{Circle, Polygon, Rectangle}
import scalafx.scene.text.Text
import scalafx.scene.{Group, Node}

import scala.collection.mutable.Buffer

import utils.Constants

case object Blueprints {
  // Stores renderable objects for all machines, items and gui objects

  // Renderable objects for the gui toolbar
  // --------------------------------------
  def guiSpringBoard: Node = {
    val group = new Group
    val rec = new Rectangle {
      x = 13
      y = 13
      width = 75
      height = 75
      stroke = Red
      strokeWidth = 5
      fill = DarkBlue
    }
    val p = new Polygon {
      points.addAll(25, 25, 25, 75, 75, 50)
    }
    group.children.add(rec)
    group.children.add(p)
    group.viewOrder_(-1)
    group
  }

  def guiSmeltery: Node = {
    val group = new Group
    val rec = new Rectangle {
      x = 1 * 0.38
      y = 1 * 0.38
      width = 197 * 0.38
      height = 197 * 0.38
      stroke = Black
      strokeWidth = 3 * 0.38
      fill = LightGray
    }
    group.children.add(rec)
    val rec2 = new Rectangle {
      x = 30 * 0.38
      y = 30 * 0.38
      width = 140 * 0.38
      height = 140 * 0.38
      rotate = 45
      stroke = Black
      fill = Blue
    }
    group.children.add(rec2)
    group
  }

  def guiProgressionBasket: Node = {

    val group = new Group
    var collector = Buffer[Node]()

    collector = collector :+ new Circle {
      centerX = 100 * 0.38
      centerY = 100 * 0.38
      radius = 90 * 0.38
      stroke = Black
      strokeWidth = 7 * 0.38
      fill = LightGray
    }
    group.children = collector
    group
  }

  def guiCross: Node = {
    new Text {
      text = "X"
      font = Constants.globalFont
      fill = Red
      style = "-fx-background: white;" +
        "-fx-color: red;" +
        "-fx-font-size: 45px;" +
        "-fx-font-weight: bold;" +
        "-fx-text-align: center;" +
        "-fx-padding: 0px"
    }
  }

  // Used for progresionbaskets
  // ------------------------------------------------------------------
  def basketIronOre: Node = {
    new Rectangle {
      x = 25
      y = 25
      width = 50
      height = 50
      stroke = Black
      fill = Red
      viewOrder_(-2)
    }
  }

  def basketIron: Node = {
    new Rectangle {
      x = 25
      y = 25
      width = 50
      height = 50
      stroke = Black
      fill = Yellow
      viewOrder_(-2)
    }
  }

  // ----------------------------------------------------------
  // Used for machine "ghosts" in gui
  def ghostSpringBoard = {
    val group = new Group
    val rec = new Rectangle {
      x = 13
      y = 13
      width = 75
      height = 75
      stroke = Red
      strokeWidth = 5
      fill = DarkBlue
    }
    val p = new Polygon {
      points.addAll(25, 25, 25, 75, 75, 50)
    }
    group.children.add(rec)
    group.viewOrder_(-10)
    group.children.add(p)
    group.opacity = 0.5
    group
  }

  def ghostSmeltery = {
    val group = new Group
    val rec = new Rectangle {
      x = 0
      y = 0
      width = 200
      height = 200
      stroke = Black
      strokeWidth = 3
      fill = LightGray
    }
    group.children.add(rec)
    val rec2 = new Rectangle {
      x = 30
      y = 30
      width = 140
      height = 140
      rotate = 45
      stroke = Black
      fill = Blue
      viewOrder_(-10)
    }
    group.children.add(rec2)
    group.children.add(new ImageView {
      image = new Image("file: lock.png")
    })
    group
  }

  def ghostProgressionBasket = {
    new Circle {
      centerX = 100
      centerY = 100
      radius = 90
      stroke = Black
      strokeWidth = 7
      fill = LightGray
      viewOrder_(-10)
    }
  }

  def ghostCross: Node = {
    val txt = new Text {
      text = "X"
      x = 25
      y = 80
      font = Constants.globalFont
      fill = Red
      style = "-fx-background: white;" +
        "-fx-color: red;" +
        "-fx-font-size: 90px;" +
        "-fx-font-weight: bold;" +
        "-fx-text-align: center;" +
        "-fx-padding: 0px"
      viewOrder_(-10)
    }
    txt
  }
}
