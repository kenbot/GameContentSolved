package kenbot.gcsolved.editor.gui

import scala.swing.{Button, Label, Orientation, SplitPane}
import scala.swing.event.Event
import EditorDialog.Result.{Cancel, Close, OK}
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.editor.gui.sidebar.{LibraryPage, SelectionEvent, SideBar}
import kenbot.gcsolved.editor.gui.util.{Dialogs, NestedBorderPanel, ToolBar}
import scala.swing.BorderPanel
import scala.swing.Publisher
import kenbot.gcsolved.editor.screens.ListAndEditScreen
import kenbot.gcsolved.editor.Settings
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter
import java.io.File



class MainPanel(settings: Settings) extends NestedBorderPanel with Publisher {
  top => 
  

  val fileToolBar = new ToolBar("File", Orientation.Horizontal) {
    contents += Button("New") { settings.newLibrary() }
    contents += Button("Open") { 
      val chooser = new JFileChooser
      chooser.setFileFilter(new FileFilter {
        def accept(f: File) = f.isDirectory() || settings.environment.isLibraryFile(f)
        def getDescription() = "Resource Libraries"
      })
      
      chooser.showOpenDialog(top.peer) match {
        case JFileChooser.APPROVE_OPTION => settings.loadLibrary(chooser.getSelectedFile().getName())
        case _ => 
      }
    }
    contents += Button("Save") { settings.saveLibrary() }
    contents += Button("Revert") { settings.currentLibrary }
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
      case OK => settings.replaceLibrary(listAndEditScreen, listAndEditScreen.updatedLibrary); true
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
