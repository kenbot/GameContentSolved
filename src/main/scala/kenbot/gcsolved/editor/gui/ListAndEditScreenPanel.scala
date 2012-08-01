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
import javax.swing.BorderFactory
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.editor.gui.util.SearchBar
import kenbot.gcsolved.resource.RefData
import scala.swing.event.ListSelectionChanged
import kenbot.gcsolved.resource.ResourceLibrary
import scala.swing.GridPanel

class ListAndEditScreenPanel(initialValues: Seq[ListAndEditItem], mainPanel: Component) extends NestedBorderPanel  {
  
  val pleaseSelectPanel = new FlowPanel {
    contents += new Label("Select entries from the left to edit them") 
  }
  
  val newButton = new Button("New")
  val cloneButton = new Button("Clone")
  val revertButton = new Button("Undo Changes")
  val deleteButton = new Button("Delete")
  val importButton = new Button("Click here to import them") {
    opaque = false
    borderPainted = false
    contentAreaFilled = false
  }

  val searchBar = SearchBar { searchString => 
    listView.listData = allResources.filter(_.current matches searchString) 
    listView.repaint
  }
  
  val listView = new ListView(initialValues) { 
    if (initialValues.nonEmpty)
      selectIndices(0)
  }

  val mainScrollPane = new ScrollPane(mainPanel) {
    verticalScrollBar.unitIncrement = 16
    verticalScrollBarPolicy = BarPolicy.AsNeeded
    horizontalScrollBarPolicy = BarPolicy.Never
  }
  
  private val titleLabel = new Label("") {
    horizontalAlignment = Alignment.Left
    font = new Font("Arial", Font.PLAIN, 20)
    border = BorderFactory.createEmptyBorder(0, 10, 0, 0)
  }
  
  def title = titleLabel.text
  def title_=(t: String) { 
    titleLabel.text = t
  }
  
  def selectedResources: Seq[ListAndEditItem] = listView.selection.items.toList
  def selectedResources_=(selected: Seq[ListAndEditItem]) {
    val indices = selected map { s => allResources indexWhere s.eq }
    listView.selectIndices(indices: _*)
  }
  
  listenTo(listView.selection)
  
  reactions += {
    case ListSelectionChanged(_, _, true) => 
      if (selectedResources.nonEmpty) mainScrollPane.contents = mainPanel
      else mainScrollPane.contents = pleaseSelectPanel
      revalidate()
      repaint()
  }
  
  private var allResourcesVar: List[ListAndEditItem] = initialValues.toList
  
  def allResources: Seq[ListAndEditItem] = allResourcesVar

  def allResources_=(resources: Seq[ListAndEditItem]) {
    val currentSelection = listView.selection.indices.toSet
    
    allResourcesVar = resources.toList
    listView.listData = resources
    
    listView.selection.indices.clear()
    listView.selection.indices ++= currentSelection
    listView.repaint()
  }
  
  def updateResourcesFromLibrary(lib: ResourceLibrary) {
    allResourcesVar = allResourcesVar filter { r => lib.contains(r.current.ref)}
    allResources foreach { _.updateCurrentFromLibrary(lib) }
  }
  
  def centerScrollBarOn(c: Component) {
    val scrollTo = c.location.y - mainScrollPane.size.height/2
    mainScrollPane.verticalScrollBar.value = scrollTo
    mainScrollPane.repaint()
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

  center = new NestedBorderPanel {
    north = new NestedBorderPanel {
      center = titleLabel
      east = new FlowPanel { 
        contents ++= Seq(revertButton, cloneButton, importButton, deleteButton) 
      }
    }
    center = mainScrollPane
  }
}
