package kenbot.gcsolved.editor.gui
import kenbot.gcsolved.resource.RefData
import kenbot.gcsolved.resource.ResourceLibrary
import kenbot.gcsolved.resource.ResourceRef
import scala.sys.error

import ResourceLibrary.ResourceLibraryRef

object ListAndEditItem {
  def asExisting(current: RefData, original: Option[RefData], 
      localLibraryRef: ResourceLibraryRef) = {

    new ListAndEditItem(current, original, original.map(_.ref), localLibraryRef, false)
  }
 
  def asNew(current: RefData, localLibraryRef: ResourceLibraryRef) = {
    new ListAndEditItem(current, None, None, localLibraryRef, false)
  } 
}

class ListAndEditItem private (
    val current: RefData, 
    val original: Option[RefData], 
    private val previousRef: Option[ResourceRef],
    val localLibraryRef: ResourceLibraryRef, 
    val isSelected: Boolean) {
  
  
  def hasNoIdYet = currentId.isEmpty
  def isNew = original.isEmpty
  def isModified = original.map(current !=) getOrElse false 
  def isIdModified = !isNew && previousRef.map(_.id != currentId).getOrElse(false)
  def isExternal = current.definedIn.map(localLibraryRef !=) getOrElse false
  def canImport = isExternal
  def isValid = current.valid

  def reset = {
    val newCurrent = original getOrElse current.resourceType.emptyData
    new ListAndEditItem(newCurrent, original, Some(current.ref), localLibraryRef, isSelected)
  }
  
  def updateFromLibrary(lib: ResourceLibrary) = {
    val latest = lib findResource current.ref getOrElse error("Resource wasn't in library anymore: " + current.ref)
    
    withCurrent(latest)
  }
 
  def addToLibrary(lib: ResourceLibrary): ResourceLibrary = previousRef match {
    case Some(prevRef) => lib.replaceResource(prevRef, current)
    case None => lib addResource current 
  } 

  def withCurrent(c: RefData) = new ListAndEditItem(c, original, Some(current.ref), localLibraryRef, isSelected) 
  
  def select(s: Boolean) = new ListAndEditItem(current, original, previousRef, localLibraryRef, s)
  
  def currentId = current.id
  
  private def equality = (current, original, previousRef, localLibraryRef)

  override def hashCode() = equality.hashCode
  
  override def equals(a: Any) = a match {
    case other: ListAndEditItem => equality == other.equality
    case _ => false
  }
    
  override def toString() = {
    def modifiedStr = if (isModified) "*" else ""
    def externalStr = if (isExternal) " - " + current.definedIn.get else ""
    def newStr = if (isNew) " (new)" else ""
    <html>{modifiedStr + current.id}<span color="blue">{newStr}</span> <span color="gray">{externalStr}</span></html>.toString
  }
}
