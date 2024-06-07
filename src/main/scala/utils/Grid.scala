package utils

import scalafx.scene.layout.{GridPane, Pane}

import scala.collection.mutable.Buffer

import main.Game

class Grid(game: Game) {
  private val gridSize = Constants.gridSize
  private val cam = game.camera
  private val grid = new GridPane
  private val itemGrid = new GridPane
  // itemGrid ignores mouseclicks
  itemGrid.mouseTransparent = true

  def getGridHolder = grid

  def update() = {
    grid.layoutX = cam.getX
    itemGrid.layoutX = cam.getX
    grid.layoutY = cam.getY
    itemGrid.layoutY = cam.getY
  }

  // Initializes the gridsquares
  private var collector = Buffer[GridSquare]()

  for (y <- 0 until gridSize) {
    for (x <- 0 until gridSize) {
      val pane = new Pane {
        style = "-fx-background-color: black, white;" +
          "-fx-background-insets: 0, 0 1 1 0;"
        minWidth = 100
        minHeight = 100
        prefHeight = 100
        prefWidth = 100
      }
      // Add a another pane over the earlier pane
      // This is where items and machines are rendered
      val itemPane = new Pane {
        minWidth = 100
        minHeight = 100
        prefHeight = 100
        prefWidth = 100
      }
      // Holds background panes
      grid.add(pane, x, y)
      // Holds itempanes
      itemGrid.add(itemPane, x, y)
      collector = collector :+ new GridSquare(x, y, game, pane, itemPane)

    }
  }
  private var grids = collector.toVector

  def getGrid = grids

  def getGridAtPos(x: Int, y: Int) = grids(y * Constants.gridSize + x)

  // Holds the gridPane, without this the gridpane cant move inside stackpane
  val root2 = new Pane
  root2.onScroll = (e => {
    if (e.getDeltaY > 0) {
      grid.scaleX = (grid.scaleX * (1 + (Constants.zoomSpeed / 100.0))).toDouble
      itemGrid.scaleX =
        (itemGrid.scaleX * (1 + (Constants.zoomSpeed / 100.0))).toDouble
      grid.scaleY = (grid.scaleY * (1 + (Constants.zoomSpeed / 100.0))).toDouble
      itemGrid.scaleY =
        (itemGrid.scaleY * (1 + (Constants.zoomSpeed / 100.0))).toDouble
    } else {
      grid.scaleX = (grid.scaleX * (1 - (Constants.zoomSpeed / 100.0))).toDouble
      itemGrid.scaleX =
        (itemGrid.scaleX * (1 - (Constants.zoomSpeed / 100.0))).toDouble
      grid.scaleY = (grid.scaleY * (1 - (Constants.zoomSpeed / 100.0))).toDouble
      itemGrid.scaleY =
        (itemGrid.scaleY * (1 - (Constants.zoomSpeed / 100.0))).toDouble
    }
  })

  def getGridScaleX = grid.scaleX

  def getGridScaleY = grid.scaleY

  root2.children = grid
  root2.children.add(itemGrid)
  game.root.children.add(root2)

}
