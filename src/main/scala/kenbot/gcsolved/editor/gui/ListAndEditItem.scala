package kenbot.gcsolved.editor.gui
import kenbot.gcsolved.resource.RefData
import kenbot.gcsolved.resource.ResourceLibrary
import scala.sys.error


object ListAndEditItem {
  def apply(current: RefData, original: Option[RefData], localLibraryRef: ResourceLibrary.ResourceLibraryRef) = {
    new ListAndEditItem(current, original, localLibraryRef)
  }
}

class ListAndEditItem(private var currentVar: RefData, val original: Option[RefData], val localLibraryRef: ResourceLibrary.ResourceLibraryRef) {
  
  private var previousRef = original.map(_.ref)
  def current = currentVar
  def current_=(c: RefData) {
    previousRef = Some(currentVar.ref)
    currentVar = c
  }
  
  def printState() {
    println("Current: " + current.debugString)
    println("Original: " + original.map(_.debugString))
  }
  
  def hasNoIdYet = currentId.isEmpty
  def isNew = original.isEmpty
  def isModified = original.map(current !=) getOrElse false 
  def isIdModified = !isNew && previousRef.map(_.id != currentId).getOrElse(false)
  def isExternal = current.definedIn.map(localLibraryRef !=) getOrElse false
  def canImport = isExternal
  def isValid = current.valid
    
  def resetToOriginal() {
    current = original getOrElse current.resourceType.emptyData
  }
  
  def updateCurrentFromLibrary(lib: ResourceLibrary) {
    current = lib.findResource(current.ref) getOrElse error("")
  }
  
  def updateLibraryFromCurrent(lib: ResourceLibrary): ResourceLibrary = {
    original match {
      case Some(orig) if isIdModified && previousRef.isDefined => 
        lib.updateResourceId(previousRef.get, current.id).addResource(current)
        
      case _ => lib.addResource(current)
    }
  }
  
  def currentId = current.id
  
  private def equality = (currentVar, original, localLibraryRef)
  
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
