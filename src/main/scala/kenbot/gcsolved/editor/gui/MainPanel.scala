package kenbot.gcsolved.editor.gui

import scala.swing.Swing.pair2Dimension
import scala.swing.Swing.pair2Point
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.SplitPane
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.editor.gui.sidebar.LibraryPage
import kenbot.gcsolved.editor.gui.sidebar.SelectionEvent
import kenbot.gcsolved.editor.gui.sidebar.SideBar
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.editor.gui.util.ToolBar
import kenbot.gcsolved.editor.Settings
import scala.swing.BorderPanel
import kenbot.gcsolved.editor.GameContentEditor
import kenbot.gcsolved.editor.gui.util.Dialogs



class MainPanel(settings: GameContentEditor.Settings) extends NestedBorderPanel {
  
  val fileToolBar = new ToolBar("File", Orientation.Horizontal) {
    contents += new Button("New")
    contents += new Button("Open")
    contents += new Button("Save")
    contents += new Button("Revert")
  }
  val sideBar = new SideBar(settings)
  val mainPanel = new Label("center panel")
  import BorderPanel.Position._
  
  listenTo(sideBar.libraryPage, sideBar.schemaPage)
  
  reactions += {
    case SelectionEvent(src: LibraryPage, refType: RefType) => editDialog(refType)
  }
  
  def editDialog(refType: RefType) {
    val parent = settings.mainFrame
    import EditorDialog.Result._
    
    val initialValues = settings.currentLibrary.allResourcesByType(refType).toSeq
    val listAndEditScreen = new ListAndEditScreen(refType, settings.currentLibrary, settings.makeEditScreen)
    
    import Dialogs.enrichWindow
    
    EditorDialog(refType.name, parent, listAndEditScreen.panel) {
      case OK => settings.updateLibrary(listAndEditScreen, listAndEditScreen.updatedLibrary); true
      case Cancel | Close => Dialogs.confirm(null, 
          title = "Cancel changes?", 
          message = "Are you sure you wish to close now? Your changes will be lost.")
    }
  }
  
  north = fileToolBar
  center = new SplitPane(Orientation.Vertical, sideBar, mainPanel) {
    dividerLocation = 500
  }
}
