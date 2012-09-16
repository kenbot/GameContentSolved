package kenbot.gcsolved.editor.gui
import java.awt.Font
import scala.swing.ScrollPane.BarPolicy
import scala.swing.Swing.pair2Dimension
import scala.swing.Alignment
import scala.swing.Button
import scala.swing.Component
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.ListView
import scala.swing.ScrollPane
import scala.swing.event.Event
import scala.swing.event.ButtonClicked
import javax.swing.BorderFactory
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.editor.gui.util.SearchBar
import kenbot.gcsolved.resource.{RefData, ResourceRef}
import scala.swing.event.ListSelectionChanged
import kenbot.gcsolved.resource.ResourceLibrary
import scala.swing.GridPanel
import scala.swing.event.MouseEntered
import scala.swing.event.MouseExited
import java.awt.Color

case class ViewItem(id: String, isModified: Boolean, isNew: Boolean, isSelected: Boolean, externalRef: Option[String]) {
  def isExternal = externalRef.isDefined
  override def toString() = {
    def modifiedStr = if (isModified) "*" else ""
    def externalStr = externalRef.map(" - " + _) getOrElse ""
    def newStr = if (isNew) " (new)" else ""
    <html>{modifiedStr + id}<span color="blue">{newStr}</span> <span color="gray">{externalStr}</span></html>.toString
  }
}
	
class ListAndEditScreenPanel(initialValues: Seq[ViewItem], mainPanel: Component, supportMultiSelect: Boolean) extends NestedBorderPanel  {
  
  top => 

  object events { 
    case object NewPressed extends Event
    case object ClonePressed extends Event
    case object UndoPressed extends Event
    case object DeletePressed extends Event
    case object ImportPressed extends Event
    case class ResourcesSelected(items: Seq[String]) extends Event
  } 
  
  private case class ViewItemWrapper(var item: ViewItem) {
    override def toString() = item.toString
  }
   
  import events._

  private var allResourcesVar: IndexedSeq[ViewItemWrapper] = (initialValues map ViewItemWrapper.apply).toIndexedSeq
  private val newButton = Button("New")(publish(NewPressed))
  private val cloneButton = Button("Clone")(publish(ClonePressed))
  private val revertButton = Button("Undo Changes")(publish(UndoPressed))
  private val deleteButton = Button("Delete")(publish(DeletePressed))
  
  private val importButton = new Button("This one lives in another library. Click here to make a local one, so you can edit it.") {
    opaque = false
    borderPainted = false
    contentAreaFilled = false
    horizontalAlignment = Alignment.Left
    private val normalTextColor = foreground
    listenTo(mouse.moves)
    reactions += {
      case ButtonClicked(_) => top publish ImportPressed
      case MouseEntered(_, _, _) => 
        foreground = Color.blue
        repaint()
        
      case MouseExited(_,_,_) => 
        foreground = normalTextColor
        repaint()
    }
  }

  private val searchBar = SearchBar { searchString => 
    listView.listData = allResources.filter(_.id.toLowerCase contains searchString.toLowerCase) map ViewItemWrapper.apply 
    listView.repaint()
  }
  
  private val listView = new ListView(initialValues map ViewItemWrapper.apply) { 
    if (initialValues.nonEmpty)
      selectIndices(0)
    
    import ListView.IntervalMode._
    selection.intervalMode = if (supportMultiSelect) MultiInterval 
                             else Single

    listenTo(selection) 
    reactions += { 
      case ListSelectionChanged(_, _, false) => 
        val selectedResources = selection.items.toSeq
        updateViewForSelection(selectedResources.map(_.item))
        top publish ResourcesSelected(selectedResources.map(_.item.id))
    }
  }
 
  private val pleaseSelectPanel = new FlowPanel {
    contents += new Label("Select entries from the left to edit them") 
  }
  
  private val titleLabel = new Label("") {
    horizontalAlignment = Alignment.Left
    font = new Font("Arial", Font.PLAIN, 20)
    border = BorderFactory.createEmptyBorder(0, 10, 0, 0)
  }


  def allResources: Seq[ViewItem] = allResourcesVar.map(_.item)
  def allResources_=(resources: Seq[ViewItem]) {
    allResourcesVar = (resources map ViewItemWrapper.apply).toIndexedSeq
    listView.listData = allResourcesVar
     
    val selectedResources = allResources.filter(_.isSelected)
    val indicesToSelect = selectedResources.map(allResources indexOf _) 
    listView.selectIndices(indicesToSelect: _*)
    updateViewForSelection(selectedResources)
  }
  
  def updateSelectedOnly(items: Seq[ViewItem]) {
    listView.selection.items zip items foreach {
      case (old, newOne) => old.item = newOne 
    }
  }

  private def updateButtonStates(selectedResources: Seq[ViewItem]) {
    val anythingSelected = selectedResources.nonEmpty
    val noneExternal = selectedResources.forall(!_.isExternal)
    val anyModified = selectedResources.exists(_.isModified)
    importButton.visible = !noneExternal
    deleteButton.enabled = anythingSelected && noneExternal
    revertButton.enabled = anythingSelected && noneExternal && anyModified
  }

  
  private def getTitleForSelectedResources(resources: Seq[ViewItem]): String = {
    val result = resources.size match {
      case 0 => "" 
      case 1 => resources.head.id
      case 2 | 3 => resources.map(_.id).mkString(", ")
      case n => resources(0).id + ", " + 
                resources(1).id + " + " + 
                (resources.size-2) + " more"
    }
    result
  }
 
  private def updateViewForSelection(selectedResources: Seq[ViewItem]) {
     titleLabel.text = getTitleForSelectedResources(selectedResources) 
     updateButtonStates(selectedResources)
     centerPanel.center = if (selectedResources.nonEmpty) mainPanel
                          else pleaseSelectPanel
  }

    
  west = new NestedBorderPanel {
    preferredSize = (150, 200)
    north = new NestedBorderPanel {
      center = newButton
      south = searchBar
    }
    center = new ScrollPane(listView) {
      verticalScrollBar.unitIncrement = 16
      verticalScrollBarPolicy = BarPolicy.AsNeeded
      horizontalScrollBarPolicy = BarPolicy.Never
    }
  }

  val centerPanel = new NestedBorderPanel {
    north = new NestedBorderPanel {
      center = titleLabel
      east = new FlowPanel { 
        contents ++= Seq(revertButton, deleteButton) 
      }
      south = importButton
    }
    center = mainPanel
  }
  
  center = centerPanel
}
