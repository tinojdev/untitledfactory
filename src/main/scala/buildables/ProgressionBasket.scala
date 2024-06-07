package buildables

import javafx.scene.input.MouseEvent
import scalafx.scene.paint.Color.{Black, LightGray}
import scalafx.scene.shape.Circle
import scalafx.scene.text.Text
import scalafx.scene.{Group, Node}

import scala.collection.mutable.Queue
import scala.util.Random

import items.{Iron, IronOre, Item}
import utils.{Constants, GridSquare}

class ProgressionBasket(
    mainGridSquare: GridSquare,
    itemType: Item,
    itemCount: Int
) extends Machine(mainGridSquare) {
  itemType.remove()

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
  val rand = new Random

  def remove(): Unit = {
    // Baskets cannot be removed
    if (true) return
    gridsToFill.foreach(_.remove(false))
    mainGridSquare.remove(false)
    mainGridSquare.getItemPane.children.remove(group)
    removed = true
    holdingItems.foreach(_.remove())
    holdingItems = Queue()
  }

  val maxItems = itemCount
  var holdingItems: Queue[Item] = Queue()
  var canReceive = true

  def canReceiveItem(item: Item): Boolean =
    canReceive && (item.getClass == itemType.getClass)

  def onItemEnter(item: Item) = {

    holdingItems += item
    if (holdingItems.length >= maxItems) {
      canReceive = false
      itemType match {
        case item: IronOre => game.tutorial.triggerThirdCheckpoint()
        case item: Iron => {
          game.tutorial.triggerFourthCheckpoint()
        }
      }
    }
    item.setInMachine(true)
    currentAmount.text = holdingItems.length + " / " + maxItems
    var itemToAdd: Option[Node] = None
    item match {
      case iron: Iron       => itemToAdd = Some(Blueprints.basketIron)
      case ironOre: IronOre => itemToAdd = Some(Blueprints.basketIronOre)
      case other            =>
    }
    itemToAdd.get.layoutX = rand.between(15, 90)
    itemToAdd.get.layoutY = rand.between(15, 90)
    group.children.add(itemToAdd.get)
  }

  // Doesn't have a window
  def showWindow(event: MouseEvent) = {
    game.tutorial.triggerSecondCheckpoint()
  }

  private var removed = false

  def update(): Unit = {
    if (removed) return
  }

  private val group = new Group
  private val background = new Circle {
    centerX = 100
    centerY = 100
    radius = 90
    stroke = Black
    strokeWidth = 7
    fill = LightGray
  }
  private val currentAmount = new Text {
    x = 70
    y = 0
    font = Constants.globalFont
    text = holdingItems.length + " / " + maxItems
    scaleX = 2.2
    scaleY = 2.2
    viewOrder_(-10.0)
  }
  private val typeOf = itemType match {
    case iron: IronOre => Blueprints.basketIronOre
    case iron: Iron    => Blueprints.basketIron
  }
  typeOf.layoutX = 90
  typeOf.layoutY = -55
  typeOf.scaleX = 0.5
  typeOf.scaleY = 0.5

  group.children = Vector(background, currentAmount, typeOf)
  mainGridSquare.getItemPane.children.add(group)
}
