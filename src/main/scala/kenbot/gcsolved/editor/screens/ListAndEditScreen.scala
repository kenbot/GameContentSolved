package kenbot.gcsolved.editor.screens

import scala.swing.Reactor

import kenbot.gcsolved.core.{LibraryEditSession, RefData, ResourceLibrary, ResourceRef}
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.editor.LibraryChangedEvent
import kenbot.gcsolved.editor.gui.{ListAndEditScreenPanel, ViewItem}
import kenbot.gcsolved.editor.gui.util.SuppressableEvents


class ListAndEditScreen(val refType: RefType, 
            val originalLibrary: ResourceLibrary, 
            makeEditScreen: RefType => EditScreen) extends Reactor with SuppressableEvents {
  
  val editScreen: EditScreen = makeEditScreen(refType)
 
  lazy val panel = new ListAndEditScreenPanel(initialValuesOrDefault map makeViewItem, editScreen.panel, editScreen.isBulkEditSupported)
   
  var editSession = LibraryEditSession(originalLibrary, Seq(initialValuesOrDefault.head))

  def updatedLibrary = editSession.library

  def selectedResources = editSession.currentEdits 
  def allResources: Seq[RefData] = updatedLibrary.allResourcesByType(refType).toIndexedSeq ++ selectedResources.filter(!_.hasId)

  def addNew() {
    editSession = editSession selectItems Seq(refType.emptyData) 
    updateView() 
    updateEditScreen()
  }

  def importSelected() {
    editSession = editSession.importLinked
    updateEditScreen()
    updateView()
  }
  
  def undoChanges() {
    editSession = editSession.reset 
    updateEditScreen()
    updateView()
  }
  
  def delete() {
    editSession = editSession.delete 
    updateEditScreen()
    updateView()
  }

  private def updateEditScreen() {
    val selected = selectedResources
    suppressEvents {
      editScreen.values = selected
    }
    editScreen.editable = !editSession.externalResourcesSelected 
    editScreen.panel.revalidate()
    editScreen.panel.repaint()
  }
  
  private def updateView() {
    suppressEvents {
      panel.allResources = allResources map makeViewItem
      panel.revalidate()
      panel.repaint()
    }
  }
  
  private def updateFromEditScreen(values: Seq[RefData]) {
    editSession = editSession applyEdits values 
    val viewItems = values map makeViewItem
    panel updateSelectedOnly viewItems
    panel.updateTitleAndButtons()
    panel.repaint()
  }
  
  private def updateForListSelection(selectedIds: Seq[String]) {

    if (!editSession.externalResourcesSelected && selectedResources.nonEmpty) {
      editSession = editSession applyEdits editScreen.values
    }

    val items = for (id <- selectedIds) yield {
      if (id.isEmpty) refType.emptyData
      else updatedLibrary(ResourceRef(id, refType))
    }
                      
    editSession = editSession selectItems items
    updateEditScreen()
  }
  
  def initialValuesOrDefault: Seq[RefData] = {
    val initialValues = originalLibrary.allResourcesByType(refType).toIndexedSeq
    if (initialValues.nonEmpty) initialValues else Vector(refType.emptyData)
  }

  def makeViewItem(r: RefData): ViewItem = {
    val isNew = editSession isAddedSinceOriginal r.ref
    val modified = editSession isModifiedSinceOriginal r 
    val selected = editSession.currentEdits contains r
    val externalRef = r.definedIn.filterNot(updatedLibrary.ref ==)
    ViewItem(r.id, modified, isNew, selected, externalRef)
  }

  listenTo(panel, editScreen) 

  import panel.events._ 

  reactions += {
    case _ if shouldSuppressEvents => 
    
    case NewPressed => addNew()
    case UndoPressed => undoChanges()
    case ImportPressed => importSelected()
    case DeletePressed => delete() 
    case ResourcesSelected(newSelection) => updateForListSelection(newSelection) 
    case UpdateValues(_, values) => updateFromEditScreen(values) 
    case LibraryChangedEvent(source, newLib, _) => sys.error("No idea what to do here.")
      // Don't forget to re-add the new items! 
      //panel.allResources = newLib.allResourcesByType(refType).toList map newListItem
  }
  
  updateView()
  updateEditScreen()
}
