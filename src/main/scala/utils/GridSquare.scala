package utils

import scalafx.scene.layout.Pane

import main.Game
import items.Item
import buildables.Machine

class GridSquare(posX: Int, posY: Int, game: Game, pane: Pane, itemPane: Pane) {
  private var holdingItem: Option[Item] = None
  private var holdingMachine: Option[Machine] = None

  private def isEmpty = holdingMachine.isEmpty

  pane.onMouseClicked = (p => {
    if (!isEmpty && game.gui.selected == 0) holdingMachine.get.showWindow(p)
    game.gui.squareClicked(this)

  })
  pane.onMouseMoved = (e => {
    game.gui.handleMouseInput(e, itemPane)
  })

  def getGame: Game = game

  def getItemPane: Pane = itemPane

  // Items reserve squares in advance so that it cant accept two items when one is already in the air
  private var reserved = false

  def reserve() {
    reserved = true
  }

  def fill(machine: Machine): Unit = {
    if (holdingItem.isDefined) holdingItem.get.remove()
    holdingItem = None
    holdingMachine = Some(machine)
  }

  // Only the first call can remove the actual machine
  def remove(first: Boolean) = {
    if (holdingMachine.isDefined) {
      if (first) holdingMachine.get.remove()
      holdingMachine = None
      pane.style = "-fx-background-color: black, white;" +
        "-fx-background-insets: 0, 0 1 1 0;"
    }
  }

  def removeItems() = {
    if (holdingItem.isDefined) {
      holdingItem.get.remove()
      holdingItem = None
    }
  }

  def addItem(item: Item) = {
    reserved = false
    holdingItem = Some(item)
    if (holdingMachine.isDefined) holdingMachine.get.onItemEnter(item)
  }

  def getMachine = holdingMachine

  private var isHighlighted = false

  def highlight(state: Boolean) = {
    if (!isHighlighted && state) {
      isHighlighted = true
      pane.style = "-fx-background-color: black, #ff8880;" +
        "-fx-background-insets: 0, 0 1 1 0;" +
        "-fx-border-color: #3d3d3d;" +
        "-fx-border-width: 2px"
    } else if (!state) {
      isHighlighted = false
      pane.style = "-fx-background-color: black, white;" +
        "-fx-background-insets: 0, 0 1 1 0;"
    }
  }

  def canReceiveItem(item: Item): Boolean =
    (getMachine.isDefined && getMachine.get.canReceiveItem(
      item: Item
    )) || (holdingItem.isEmpty && getMachine.isEmpty && !reserved)

  def canReceiveMachine: Boolean = {
    this.isEmpty
  }

  def getItems = holdingItem

  def getCoords = (posX, posY)

  def getX = posX

  def getY = posY

}
