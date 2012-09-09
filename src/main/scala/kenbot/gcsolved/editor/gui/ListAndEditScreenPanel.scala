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
import kenbot.gcsolved.resource.RefData
import scala.swing.event.ListSelectionChanged
import kenbot.gcsolved.resource.ResourceLibrary
import scala.swing.GridPanel
import scala.swing.event.MouseEntered
import scala.swing.event.MouseExited
import java.awt.Color


class ListAndEditScreenPanel(initialValues: Seq[ListAndEditItem], mainPanel: Component) extends NestedBorderPanel  {
  
  top => 

  object events { 
    case object NewPressed extends Event
    case object ClonePressed extends Event
    case object UndoPressed extends Event
    case object DeletePressed extends Event
    case object ImportPressed extends Event
    case class ResourcesSelected(items: Seq[ListAndEditItem]) extends Event
  } 
  import events._

  private var allResourcesVar: List[ListAndEditItem] = initialValues.toList
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
      case ButtonClicked(_) => top publish(ImportPressed)
      case MouseEntered(_, _, _) => 
        foreground = Color.blue
        repaint()
        
      case MouseExited(_,_,_) => 
        foreground = normalTextColor
        repaint()
    }
  }

  private val searchBar = SearchBar { searchString => 
    listView.listData = allResources.filter(_.current matches searchString) 
    listView.repaint
  }
  
  private val listView = new ListView(initialValues) { 
    if (initialValues.nonEmpty)
      selectIndices(0)
   
    listenTo(selection) 
    reactions += { 
      case ListSelectionChanged(_, _, true) => 
        val selectedResources = selection.items.toSeq
        updateViewForSelection(selectedResources)
        top publish ResourcesSelected(selectedResources)
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


  def allResources: Seq[ListAndEditItem] = allResourcesVar
  def allResources_=(resources: Seq[ListAndEditItem]) {
    allResourcesVar = resources.toList
    listView.listData = resources
     
    val selectedResources = allResources.filter(_.isSelected)
    val indicesToSelect = selectedResources.map(allResources indexOf _) 
    listView.selectIndices(indicesToSelect: _*)
    updateViewForSelection(selectedResources)
  }

  private def updateButtonStates(selectedResources: Seq[ListAndEditItem]) {
    val anythingSelected = selectedResources.nonEmpty
    val noneExternal = selectedResources.forall(!_.isExternal)
    val anyModified = selectedResources.exists(_.isModified)
    importButton.visible = !noneExternal
    deleteButton.enabled = anythingSelected && noneExternal
    revertButton.enabled = anythingSelected && noneExternal && anyModified
  }

  
  private def getTitleForSelectedResources(resources: Seq[ListAndEditItem]): String = {
    val result = resources.size match {
      case 0 => "" 
      case 1 => resources.head.currentId
      case 2 | 3 => resources.map(_.currentId).mkString(", ")
      case n => resources(0).currentId + ", " + 
                resources(1).currentId + " + " + 
                (resources.size-2) + " more"
    }
    result
  }
 
  private def updateViewForSelection(selectedResources: Seq[ListAndEditItem]) {
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
