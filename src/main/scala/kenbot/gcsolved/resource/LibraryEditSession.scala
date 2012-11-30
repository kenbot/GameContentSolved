package kenbot.gcsolved.resource

import LibraryEditSession.IdChangeMap

object LibraryEditSession {

  def apply(library: ResourceLibrary, editItems: Seq[RefData]) = 
    new LibraryEditSession(library, editItems, None, None, None, Map())

  private[resource] type IdChangeMap = Map[ResourceRef, ResourceRef]
}

/**
 * A library with a selected set of resources currently being edited.  The selected set can be replaced with modified copies, and 
 * ID changes will be automatically handled.
 */
class LibraryEditSession private (
    val library: ResourceLibrary, 
    itemsToEdit: Seq[RefData],
    originalContextUnlessNew: Option[LibraryEditSession],
    val previousContext: Option[LibraryEditSession],
    val nextContext: Option[LibraryEditSession],
    private val idsChangedFrom: IdChangeMap) {
  
  val originalContext: LibraryEditSession = originalContextUnlessNew getOrElse this 
  val currentEdits: IndexedSeq[RefData] = itemsToEdit.toIndexedSeq
  
  def canUndo = previousContext.isDefined
  def canRedo = nextContext.isDefined
  
  def undo: LibraryEditSession = previousContext map { prev => 
    new LibraryEditSession(prev.library, prev.currentEdits, Some(originalContext), 
                           prev.previousContext, Some(this), prev.idsChangedFrom) 
  } getOrElse this  

  def reset = originalContext

  def redo: LibraryEditSession = nextContext getOrElse this 

  def importLinked: LibraryEditSession = applyEdits(currentEdits) 

  def delete: LibraryEditSession = {
    val newLibrary = library removeResources currentEdits.map(_.ref)
    val externals = currentEdits.flatMap(newLibrary findResource _.ref)

    new LibraryEditSession(newLibrary, externals, Some(originalContext), Some(this), None, idsChangedFrom)
  }

  def selectItems(itemsToEdit: Seq[RefData]): LibraryEditSession = {
    new LibraryEditSession(library, itemsToEdit, Some(originalContext), Some(this), None, idsChangedFrom)
  } 

  def applyEdits(newEdits: Seq[RefData]): LibraryEditSession = {
    require(currentEdits.size == newEdits.size, 
            "Expecting edits to " + currentEdits.size + " resources, found: " + newEdits.size)
      
    val (libWithSwappedIds, swappedIds) = swapIds(currentEdits, newEdits)
    val approvedEdits = newEdits.filter(_.hasId)
    val libWithEdits = libWithSwappedIds.addResources(approvedEdits: _*)  
    val editsInLibrary = newEdits map { r => libWithEdits findResource r.ref getOrElse r }
    new LibraryEditSession(libWithEdits, editsInLibrary, Some(originalContext), Some(this), None, swappedIds)
  }

  
  def externalResourcesSelected: Boolean = currentEdits.exists { r => 
    r.definedIn.nonEmpty && !r.isDefinedIn(library)
  }
 
  def isAddedSinceOriginal(ref: ResourceRef): Boolean = {
    !(originalContext.library containsRef getOriginalRef(ref))
  }
  def isModifiedSinceOriginal(r: RefData) = !isAddedSinceOriginal(r.ref) && 
					    Some(r) != (originalContext.library findResource r.ref)
				
  private def getOriginalRef(ref: ResourceRef): ResourceRef = idsChangedFrom.getOrElse(ref, ref)
  
  private def swapIds(oldEdits: Seq[RefData], newEdits: Seq[RefData]): (ResourceLibrary, IdChangeMap) = {
    def hasDifferentIds(oldItem: RefData, newItem: RefData) = 
      oldItem.hasId && newItem.hasId && oldItem.id != newItem.id 
    
    val oldAndNewSideBySide = newEdits zip oldEdits
    val idSwaps = for ((newItem, oldItem) <- oldAndNewSideBySide if hasDifferentIds(oldItem, newItem))
                  yield (newItem.ref, oldItem.ref)
      
    val libWithSwappedIds = (library /: idSwaps) {
      case (lib, (newRef, oldRef)) => lib.updateResourceId(oldRef, newRef.id) 
    }
    
    val idChanges = (idsChangedFrom /: idSwaps) {
      case (result, (newRef, oldRef)) => result + (newRef -> getOriginalRef(oldRef))
    }
    
    (libWithSwappedIds, idChanges)
  }
}
