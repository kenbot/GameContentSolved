package kenbot.gcsolved.resource



object LibraryEditSession {

  def apply(library: ResourceLibrary, editItems: Seq[RefData]) = 
    new LibraryEditSession(library, editItems, None, None, None)

}



class LibraryEditSession private (
    val library: ResourceLibrary, 
    itemsToEdit: Seq[RefData],
    originalContextUnlessNew: Option[LibraryEditSession],
    val previousContext: Option[LibraryEditSession],
    val nextContext: Option[LibraryEditSession]) {
  
  val originalContext: LibraryEditSession = originalContextUnlessNew getOrElse this 
  val currentEdits: IndexedSeq[RefData] = itemsToEdit.toIndexedSeq
  
  def canUndo = previousContext.isDefined
  def canRedo = nextContext.isDefined
  
  def undo: LibraryEditSession = previousContext map { prev => 
    new LibraryEditSession(prev.library, prev.currentEdits, Some(originalContext), 
                           prev.previousContext, Some(this)) 
  } getOrElse this  

  def reset = originalContext

  def redo: LibraryEditSession = nextContext getOrElse this 

  def importLinked: LibraryEditSession = applyEdits(currentEdits) 

  def delete: LibraryEditSession = {
    val newLibrary = library removeResources currentEdits.map(_.ref)
    val externals = currentEdits.flatMap(newLibrary findResource _.ref)

    new LibraryEditSession(newLibrary, externals, Some(originalContext), Some(this), None)
  }

  def selectItems(itemsToEdit: Seq[RefData]): LibraryEditSession = {
    new LibraryEditSession(library, itemsToEdit, Some(originalContext), Some(this), None)
  } 

  def applyEdits(newEdits: Seq[RefData]): LibraryEditSession = {
    require(currentEdits.size == newEdits.size, 
            "Expecting edits to " + currentEdits.size + " resources, found: " + newEdits.size)

    val idSwaps = for {
      (oldItem, newItem) <- currentEdits zip newEdits 
      if oldItem.hasId && newItem.hasId && oldItem.id != newItem.id 
    } 
    yield (oldItem.ref, newItem.id)
      
    val libWithSwappedIds = (library /: idSwaps) {
      case (lib, (oldRef, newId)) => lib.updateResourceId(oldRef, newId) 
    }

    val approvedEdits = newEdits.filter(r => r.hasId)  
    val libWithEdits = libWithSwappedIds.addResources(approvedEdits: _*)  
    val editsInLibrary = newEdits.map { r => libWithEdits findResource r.ref getOrElse r }
     
    new LibraryEditSession(libWithEdits, editsInLibrary, Some(originalContext), Some(this), None)
  }
  
  def externalResourcesSelected: Boolean = currentEdits.exists { r => 
    r.definedIn.nonEmpty && !r.isDefinedIn(library)
  }
 
  def isAddedSinceOriginal(ref: ResourceRef) = !(originalContext.library containsRef ref)
  def isModifiedSinceOriginal(r: RefData) = !isAddedSinceOriginal(r.ref) && 
					    Some(r) != (originalContext.library findResource r.ref)
}
