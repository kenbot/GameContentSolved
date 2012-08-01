package kenbot.gcsolved.editor.gui.util
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Insets
import scala.collection.mutable.ListBuffer
import scala.swing.event.ButtonClicked
import scala.swing.event.Event
import scala.swing.Alignment
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Component
import scala.swing.MainFrame
import scala.swing.Orientation
import scala.swing.Publisher
import scala.swing.ScrollPane
import scala.swing.SimpleSwingApplication
import scala.swing.TextField
import scala.swing.Swing.onEDT
import Icons.AddIcon
import Icons.DeleteIcon
import kenbot.gcsolved.editor.gui.widgets.WidgetEditor
import scala.swing.event.FocusGained
import scala.swing.event.FocusLost


class DynamicListPanel[C <: Component](initialPanels: Seq[C], newPanel: => C) 
    extends NestedBorderPanel with Publisher {

  top => 
  
  import DynamicListPanel._
    
  private val listPanel = new BoxPanel(Orientation.Vertical)
  
  private val rows = new ListBuffer[Row]()
  
  north = listPanel
  
  val addButtonRow1 = createAddRow(new AddButton(addNewRow()))
  val addButtonRow2 = createAddRow(new AddButton(addNewRow()))
  
  class Row(val userPanel: C) extends NestedBorderPanel {
    val removeButton = new RemoveButton(removeRow(this))
    center = userPanel
    east = removeButton
  }
  
  def userPanels: Seq[C] = rows.map(_.userPanel).toList
  
  def userPanels_=(panels: Seq[C]) { 
    clear()
    rows ++= panels.map(p => new Row(p))
    listPanel.contents ++ panels
    listPanel.contents += addButtonRow2
    
    revalidate()
    repaint()
  }
  
  
  userPanels = initialPanels
  
  private def createAddRow(addButton: AddButton) = new NestedBorderPanel {
    east = addButton
  }
  
  def addNewRow() {
    addRow(new Row(newPanel))
  }
  
  def addRow(row: Row) {
    rows += row
    listPanel.contents -= addButtonRow2
    listPanel.contents += row
    listPanel.contents += addButtonRow2
    revalidate()
    repaint()
    
    fireComponentAdded(row.userPanel)
  }
  
  private def removeRow(row: Row) {
    rows -= row
    listPanel.contents -= row
    revalidate()
    repaint()
    
    fireComponentRemoved(row.userPanel)
  }
  
  def clear() {
    rows.clear()
    listPanel.contents.clear()
    listPanel.contents += addButtonRow2
    revalidate()
    repaint()
  }
  
  private def fireComponentRemoved(innerComponent: Component) = onEDT(publish(ComponentRemoved(this, innerComponent)))
  private def fireComponentAdded(innerComponent: Component) = onEDT(publish(ComponentAdded(this, innerComponent)))
  
  lazy val scrollPane = new ScrollPane {
    import ScrollPane.BarPolicy._
    
    horizontalScrollBarPolicy = Never
    verticalScrollBarPolicy = AsNeeded

    contents = new NestedBorderPanel {north = top}
    listenTo(top)
    reactions += {
      case cc: ComponentsChanged =>
        verticalScrollBar.value = verticalScrollBar.maximum
        revalidate()
        repaint()
    }
  }
}


object DynamicListPanel {
  
  sealed trait ComponentsChanged {val source: Component}
  case class ComponentAdded(source: Component, addedUserPanel: Component) extends ComponentsChanged with Event 
  case class ComponentRemoved(source: Component, removedUserPanel: Component) extends ComponentsChanged with Event
    
  object AddButton {
    val colorIcon = new AddIcon
    val greyIcon = new AddIcon(Color.lightGray)
  }
  
  class AddButton(doIt: => Unit) extends Button { me => 
    cursor = new Cursor(Cursor.HAND_CURSOR)
    icon = AddButton.greyIcon
    rolloverIcon = AddButton.colorIcon
    tooltip = "Add"
    contentAreaFilled = false
    preferredSize = new Dimension(32,32)
    margin = new Insets(5,5,5,5)
    background = new Color(220,220,220)
    horizontalAlignment = Alignment.Right
    horizontalTextPosition = Alignment.Left
    
    listenTo(this)
    
    reactions += { 
      case FocusGained(`me`, _, _) => icon = AddButton.colorIcon; repaint()
      case FocusLost(`me`, _, _) =>  icon = AddButton.greyIcon; repaint()
      case ButtonClicked(_) => doIt 
    }
  }
  
  object RemoveButton {
    val colorIcon = new DeleteIcon
    val greyIcon = new DeleteIcon(Color.lightGray)
  }
  
  class RemoveButton(doIt: => Unit) extends Button { me => 
    cursor = new Cursor(Cursor.HAND_CURSOR)
    icon = new DeleteIcon(Color.lightGray)
    rolloverIcon = new DeleteIcon
    tooltip = "Remove"
    contentAreaFilled = false
    preferredSize = new Dimension(32,32)
    
    listenTo(this)
    
    reactions += { 
      case FocusGained(`me`, _, _) => icon = RemoveButton.colorIcon; repaint()
      case FocusLost(`me`, _, _) =>  icon = RemoveButton.greyIcon; repaint()
      case ButtonClicked(_) => doIt 
    }
  }
}
