package kenbot.gcsolved.editor.gui
import kenbot.gcsolved.resource.RefData
import kenbot.gcsolved.resource.ResourceLibrary
import scala.sys.error

case class ListAndEditItem(var current: RefData, original: Option[RefData], localLibraryRef: ResourceLibrary.ResourceLibraryRef) {
  
  
  
  def isNew = original.isEmpty
  def isModified = original.map(current !=) getOrElse false
  def isIdModified = !isNew && original.map(_.id != current.id).getOrElse(false)
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
      case Some(orig) if isIdModified => 
        lib.updateResourceId(orig.ref, current.id).addResource(current)
        
      case _ => lib.addResource(current)
    }
  }
  
  def currentId = if (current.id.nonEmpty) current.id else "(new)"
  
  override def toString() = {
    def modifiedStr = if (isModified) "*" else ""
    def externalStr = if (isExternal) " - " + current.definedIn.get else ""
    def newStr = if (isNew) " (new)" else ""
    modifiedStr + current.id + newStr + externalStr
  }
}
