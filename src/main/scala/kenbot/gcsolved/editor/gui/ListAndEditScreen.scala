package kenbot.gcsolved.editor.gui


import scala.swing.ScrollPane._
import scala.swing.Swing.onEDT
import scala.swing.Swing.pair2Dimension
import scala.swing.event.ButtonClicked
import scala.swing.event.ListSelectionChanged
import scala.swing.Button
import scala.swing.Component
import scala.swing.Label
import scala.swing.ListView
import scala.swing.Reactor
import scala.swing.Swing
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.editor.gui.widgets.WidgetFocusEvent
import kenbot.gcsolved.editor.GameContentEditor
import kenbot.gcsolved.editor.LibraryChangedEvent
import kenbot.gcsolved.editor.PublishableLibraryChanges
import kenbot.gcsolved.editor.Settings
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.RefData
import kenbot.gcsolved.resource.ResourceLibrary
import scala.collection.mutable.ListBuffer
import scala.swing.FlowPanel
import scala.swing.Publisher
import kenbot.gcsolved.editor.gui.widgets.FieldWidget
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.ResourceRef
import kenbot.gcsolved.editor.gui.util.SuppressableEvents


class ListAndEditScreen(val refType: RefType, 
            initialValues: Seq[RefData], 
            val originalLibrary: ResourceLibrary, 
            makeWidget: Field => FieldWidget) extends Reactor with SuppressableEvents {
  
  
  val editScreen: EditScreen = new WidgetEditScreen(refType, Seq.empty, makeWidget) // TODO: replace with factory function
  lazy val panel = new ListAndEditScreenPanel(initialValuesOrDefault.toList, editScreen.panel)
  private[this] var updatedLibraryVar = originalLibrary
  
  def updatedLibrary = updatedLibraryVar
  private def updatedLibrary_=(lib: ResourceLibrary) { 
    updatedLibraryVar = lib 
  }
  
  def selectedResources: Seq[ListAndEditItem] = panel.selectedResources
  def selectedResources_=(selected: Seq[ListAndEditItem]) {
    panel.selectedResources = selected
    onResourcesSelected(selectedResources)
  }
  
  def allResources: Seq[ListAndEditItem] = panel.allResources

  def addNew() {
    val newValue = refType.emptyData
    val newItem = ListAndEditItem(newValue, None, originalLibrary.id)
    
    suppressEvents {
      panel.allResources :+= newItem
      selectedResources = Seq(newItem)
    }
  }
  
  def importSelected() {
    updatedLibrary = updatedLibrary.addResources(editScreen.values: _*)
    selectedResources.foreach(_ updateCurrentFromLibrary updatedLibrary)
    onResourcesSelected(selectedResources)
  }
  
  def undoChanges() {
    selectedResources.foreach(_.resetToOriginal())
    updatedLibrary = originalLibrary
    onResourcesSelected(selectedResources)
  }
  
  def delete() {
    val selectedRefs = selectedResources.filter(!_.isNew).map(_.current.ref)
    updatedLibrary = updatedLibrary.removeResources(selectedRefs)
    panel updateResourcesFromLibrary updatedLibrary
    
    suppressEvents {
      def shouldRemove(item: ListAndEditItem) = selectedResources.contains(item) && !item.isExternal
      panel.allResources = panel.allResources filterNot shouldRemove
      selectedResources = Nil
    }
    updateButtonStates()
    panel.repaint()
  }
  
  private def initialValuesOrDefault = {
    val data = if (initialValues.nonEmpty) initialValues
               else List(refType.emptyData)
              
    data map newListItem
  }
    
  private def newListItem(refData: RefData) = ListAndEditItem(refData, Some(refData), originalLibrary.id)


  private def updateLibraryAndViewWithEdits() {
    val edited = editScreen.values
    val selected = selectedResources
    
    if (!selected.exists(_.isExternal)) {
      // TODO Awful!  refactor!  
      // This is so that if the ID is changed, then the ID will be updated throughout the library.
      // This can only be done when 1 resource is selected.  It doesn't make sense to change the ID
      // in bulk edit mode.
      if (selected.size == 1) { 
        selected.head.current = edited.head
        updatedLibrary = selected.head.updateLibraryFromCurrent(updatedLibrary)
      }
      else {
        updatedLibrary = updatedLibrary.addResources(edited: _*)
      }
      panel updateResourcesFromLibrary updatedLibrary
      updateButtonStates()
    }
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
  
  private def onResourcesSelected(selected: Seq[ListAndEditItem]) {
    var hasExternalResources = selected.exists(_.isExternal)
    
    editScreen.values = selected.map(_.current)
    editScreen.editable = !hasExternalResources
    
    panel.importButton.visible = hasExternalResources
    panel.title = getTitleForSelectedResources(selected)
    updateButtonStates()
  }
  
  private def updateButtonStates() {
    val nonEmpty = selectedResources.nonEmpty
    val noneExternal = selectedResources.forall(!_.isExternal)
    val anyModified = selectedResources.exists(_.isModified)
    panel.deleteButton.enabled = nonEmpty && noneExternal
    panel.revertButton.enabled = nonEmpty && noneExternal && anyModified
    panel.repaint()
  }
  
  
  listenTo(panel.newButton, panel.revertButton, 
      panel.importButton, panel.deleteButton, panel.listView.selection, editScreen)
  
  reactions += {
    case _ if shouldSuppressEvents => 
    
    case ButtonClicked(panel.newButton) => addNew()
    case ButtonClicked(panel.revertButton) => undoChanges()
    case ButtonClicked(panel.importButton) => importSelected()
    case ButtonClicked(panel.deleteButton) => delete()
    case ListSelectionChanged(_, _, true) => onResourcesSelected(selectedResources)
    
    // TODO ListAndEditScreen shouldn't know about widgets.  Refactor.
    case WidgetFocusEvent(w, false, _) => 
      updateLibraryAndViewWithEdits()
      
    case LibraryChangedEvent(source, newLib, _) => 
      println("library changed")
      panel.allResources = newLib.allResourcesByType(refType).toList map newListItem
  }
  
  selectedResources = allResources.headOption.toList
}
