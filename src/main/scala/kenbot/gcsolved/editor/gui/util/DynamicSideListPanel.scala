package kenbot.gcsolved.editor.gui.util
import scala.swing.Swing.pair2Dimension
import scala.swing.event.Event
import scala.swing.event.FocusGained
import scala.swing.event.FocusLost
import scala.swing.event.ListSelectionChanged
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Component
import scala.swing.Label
import scala.swing.ListView
import scala.swing.MainFrame
import scala.swing.Publisher
import scala.swing.ScrollPane
import scala.swing.SimpleSwingApplication
import scala.swing.Swing
import scala.swing.TextField
import DynamicSideListPanel._
import scala.swing.Alignment
import java.awt.Color
import javax.swing.JComponent
import scala.util.DynamicVariable
import scala.util.DynamicVariable

object DynamicSideListPanel {
  
  trait ItemDescription[C <: Component] {
    def newPanel: C
    def getLabel(c: C): String
    def getForeground(c: C): Color
    def getBackground(c: C): Color
  }
  
  sealed trait ComponentsChanged extends Event {val source: Component}
  case class ComponentAdded(source: DynamicSideListPanel[_ <: Component], addedUserPanel: Component) extends ComponentsChanged
  case class ComponentSelected(source: DynamicSideListPanel[_ <: Component], selectedUserPanel: Component) extends ComponentsChanged
  case class ComponentRemoved(source: DynamicSideListPanel[_ <: Component], removedUserPanel: Component) extends ComponentsChanged
}


class DynamicSideListPanel[C <: Component : ItemDescription](initialPanels: Seq[C]) 
    extends NestedBorderPanel with Publisher with SuppressableEvents {
  
  private[this] val itemDesc = implicitly[ItemDescription[C]]
  import itemDesc._
  
  val listView = new ListView(initialPanels) {
    preferredSize = (150, 100)
    
    val rendererLabel = new Label {
      horizontalAlignment = Alignment.Left
    }
    
    renderer = new ListView.AbstractRenderer[C, Label](rendererLabel) {
      override def configure(listView: ListView[_], isSelected: Boolean, focused: Boolean, panel: C, index: Int) {
        val labelString = getNonEmptyLabel(panel)
        component.text = labelString
        component.foreground = getForeground(panel)
        component.background = getBackground(panel)
        listView.tooltip = if (index != -1) labelString else ""
      }
    }
    suppressEvents {
      if (initialPanels.nonEmpty)
        selectIndices(0)
    }
  }
  
  val enterButton = Button("Enter") {
    listView.repaint()
    val lastIndex = lastSelectedIndex
                            
    if (lastIndex == numPanels-1) {
      userPanels :+= newPanel
    }
    selectIndex(lastIndex+1, true)
  }
  
  private val editorPanel = new NestedBorderPanel {
    south = enterButton
  }

  override def enabled: Boolean = listView.enabled
  override def enabled_=(b: Boolean) {
    listView.enabled = b
    enterButton.enabled = b
  }
  
  
  def currentPanel: C = editorPanel.center.asInstanceOf[C]
  def selectedIndices = listView.selection.indices
  def selectedItems = listView.selection.items
  def lastSelectedIndex: Int = if (selectedIndices.nonEmpty) selectedIndices.max
                               else 0
  def numPanels = userPanels.size
  
  
  def userPanels: Seq[C] = listView.listData
  
  def userPanels_=(panelList: Seq[C]) {
    val nonEmptyPanelList = if (panelList.nonEmpty) panelList else Seq(newPanel)
    val oldPanels = userPanels
    //deafTo(oldPanels: _*)
    //listenTo(nonEmptyPanelList: _*)
    listView.listData = nonEmptyPanelList
    suppressEvents {
      selectIndex(0)
    }
    
    oldPanels.map(ComponentRemoved(this, _)) foreach publish
    panelList.map(ComponentAdded(this, _)) foreach publish
  }
  
  def clear() { userPanels = Nil }
    
  private def getNonEmptyLabel(panel: C): String = {
    val label = getLabel(panel)
    if (label == "") "<new>" else label
  }
  
  west = new ScrollPane(listView) {
    import ScrollPane.BarPolicy._
    horizontalScrollBarPolicy = Never
    verticalScrollBarPolicy = AsNeeded
  }
  
  center = editorPanel

  reactions += {
    case ListSelectionChanged(_, _, true) =>
      selectIndex(listView.selection.leadIndex, false)
      
    case FocusGained(src, other, temp) if src ne this => 
      val gainedFromOurs = other map isOurComponent getOrElse false
      if (!gainedFromOurs) {
        publish(FocusGained(this, other, temp))
      }
      
    case FocusLost(src, other, temp) if src ne this  => 
      val lostToOurs = other map isOurComponent getOrElse false
      if (!lostToOurs) {
        publish(FocusLost(this, other, temp))
      }
  }
  
  private def isOurComponent(c: Component) = (listView eq c) || (enterButton eq c)
  
  override def hasFocus: Boolean = listView.hasFocus || enterButton.hasFocus || currentPanel.hasFocus
  
  def selectIndex(index: Int, updateListView: Boolean = false) {
    select(listView.listData(index), updateListView)
  }
  
  def select(panel: C, updateListView: Boolean = false) {
    if (updateListView) {
      listView.selectIndices(userPanels indexOf panel)
    }
    
    editorPanel.center = panel
    if (!shouldSuppressEvents)
      publish(ComponentSelected(this, panel))
    revalidate()
    repaint()
  }
  
  listenTo(listView, listView.selection,  enterButton)
  
  userPanels = initialPanels
}
