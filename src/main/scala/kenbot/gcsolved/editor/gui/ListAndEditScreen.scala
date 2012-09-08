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
            makeEditScreen: RefType => EditScreen) extends Reactor with SuppressableEvents {
  
  val editScreen: EditScreen = makeEditScreen(refType)
 
  lazy val panel = new ListAndEditScreenPanel(initialValuesOrDefault.toList, editScreen.panel)

  private var updatedLibraryVar = originalLibrary
  private var allResourcesVar: Seq[ListAndEditItem] = initialValuesOrDefault.toList
  
  def allResources = allResourcesVar
  private def allResources_=(rs: Seq[ListAndEditItem]) { allResourcesVar = rs }
  def updatedLibrary = updatedLibraryVar
  private def updatedLibrary_=(lib: ResourceLibrary) { updatedLibraryVar = lib }

  def selectedResources = allResources.filter(_.isSelected)

  def select(items: Seq[ListAndEditItem]) {
    allResources = allResources.map(r => r.select(items contains r))
  }


  def addNew() {
    println("addNew")
    val alreadyExistingNewOne = allResources.find(r => r.isNew && r.hasNoIdYet)
    
    if (alreadyExistingNewOne.isDefined) {
      select(alreadyExistingNewOne.toList)
    } 
    else suppressEvents {
      val newItem = ListAndEditItem.asNew(refType.emptyData, originalLibrary.id) 
      allResources +:= newItem
      select(Seq(newItem)) 
    }
    updateView() 
    updateEditScreen()
  }
  

  def importSelected() {
    println("importSelected: " + selectedResources.map(_.currentId))
    addSelectedToLibrary()
    updateSelectedOnly(_ updateFromLibrary updatedLibrary)
    updateView()
    updateEditScreen()
  }
  
  def undoChanges() {
    println("undoChanges: " + selectedResources.map(_.currentId))
    updateSelectedOnly(_.reset)
    addSelectedToLibrary()
    updateEditScreen()
    updateView()
  }
  
  def delete() {
    println("delete: " + selectedResources.map(_.currentId))
    if (isSelectedShadowingLinked) {
      unimportSelected()
    }
    else {
      suppressEvents {
        removeSelectedFromLibrary()
        allResources = allResources.filterNot(_.isSelected)
      }
      updateView()
    }
  }
  
  def unimportSelected() {
    println("unimport: " + selectedResources.map(_.currentId))
    removeSelectedFromLibrary() 
    updateSelectedOnly(_ updateFromLibrary updatedLibrary)
    //updateEditScreen()
    updateView()
  }
  
  private def updateEditScreen() {
    println("updateEditScreen: " + selectedResources.map(_.currentId))
    val selected = selectedResources
    suppressEvents {
      editScreen.values = selected.map(_.current)
    }
    editScreen.editable = selected.forall(!_.isExternal)
    editScreen.panel.revalidate()
    editScreen.panel.repaint()
  }
  
  private def updateView() {
    println("updateView: " + selectedResources.map(_.currentId))
    panel.allResources = allResources
    panel.revalidate()
    panel.repaint()
  }
  
  private def addSelectedToLibrary() {
    println("addSelectedToLibrary: " + selectedResources.map(_.currentId))
    selectedResources.headOption.foreach { r => 
      updatedLibrary = r addToLibrary updatedLibrary
    }
  }

  private def removeSelectedFromLibrary() {
    println("removeSelectedFromLibrary: " + selectedResources.map(_.currentId))
    val refsToRemove = selectedResources.filter(!_.isNew).map(_.current.ref)
    updatedLibrary = updatedLibrary removeResources refsToRemove 
  }
  
  private def initialValuesOrDefault = {
    val data = if (initialValues.nonEmpty) initialValues
               else List(refType.emptyData)
              
    data map newListItem
  }
    
  private def newListItem(refData: RefData) = ListAndEditItem.asExisting(refData, Some(refData), originalLibrary.id)

  private def updateSelectedOnly(f: ListAndEditItem => ListAndEditItem) {
    allResources = allResources.map(r => if (r.isSelected) f(r) else r) 
  } 

  private def updateFromEditScreen(singleValue: RefData) {
    println("updateFromEditScreen: " + singleValue.id)
    updateSelectedOnly(_ withCurrent singleValue)
    addSelectedToLibrary()   
    updateView()
  }
  
  private def updateForListSelection(newSelection: Seq[ListAndEditItem]) {
    println("updateForListSelection: " + newSelection.map(_.currentId))

    editScreen.values.headOption.foreach { singleValue => 
      if (updatedLibrary containsLocally singleValue) {
        updateSelectedOnly(_ withCurrent singleValue) 
        addSelectedToLibrary()
      } 
    }

    select(newSelection)
    updateEditScreen()
  }
  
  private def isSelectedShadowingLinked = {
    val shadowing = for (r <- allResources.find(_.isSelected))
                    yield updatedLibrary isShadowingLinkedResource r.current.ref 
    shadowing getOrElse false
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
    case UpdateValues(_, Seq(singleValue)) => updateFromEditScreen(singleValue) 
    case UpdateValues(_, values) => error("Bulk edit is not supported")
    case LibraryChangedEvent(source, newLib, _) => error("No idea what to do here.")
      // Don't forget to re-add the new items! 
      //panel.allResources = newLib.allResourcesByType(refType).toList map newListItem
  }
  
  select(allResources.headOption.toList)
  updateView()
  updateEditScreen()
}
