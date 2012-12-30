package kenbot.gcsolved.editor
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.editor.gui.{WidgetEditScreen}
import kenbot.gcsolved.editor.screens.EditScreen
import kenbot.gcsolved.core.ResourceSchema
import scala.swing.Frame
import kenbot.gcsolved.core.ResourceEnvironment
import kenbot.gcsolved.core.ResourceLibrary
import kenbot.gcsolved.core.Field
import scala.swing.Publisher
import scala.swing.event.Event
import scala.swing.Component
import kenbot.gcsolved.editor.widgets.DefaultMakeWidget
import kenbot.gcsolved.core.ResourceLibrary.ResourceLibraryRef


case class LibraryChangedEvent(source: Any, newLibrary: ResourceLibrary, oldLibrary: ResourceLibrary) extends Event
case class LibraryChangeRequest(source: Any, libraryChange: ResourceLibrary => ResourceLibrary) extends Event

trait Settings extends Publisher {
  
  def makeEditScreen: RefType => EditScreen
  def initialLibrary: ResourceLibrary
  def iconDir: String
  def iconFileExt: String
  def imageFileExt: String
  def frameTitle: String
  def resourceDir: String
  final def currentLibrary: ResourceLibrary = libraryVar
  final def schema: ResourceSchema = currentLibrary.schema
  def environment: ResourceEnvironment
  def mainFrame: Frame
  
  private var libraryVar = initialLibrary
  
  def updateLibrary(requestedBy: Any, f: ResourceLibrary => ResourceLibrary) = replaceLibrary(requestedBy, f(currentLibrary))
  
  def replaceLibrary(requestedBy: Any, newLibrary: ResourceLibrary): ResourceLibrary = {
    val event = LibraryChangedEvent(requestedBy, newLibrary, currentLibrary)
    libraryVar = newLibrary
    publish(event)
    currentLibrary
  }

  def newLibrary(): ResourceLibrary = replaceLibrary(Settings.this, ResourceLibrary("new library", schema))
  def saveLibrary() { environment saveLibrary currentLibrary }
  def loadLibrary(libRef: ResourceLibraryRef): ResourceLibrary = replaceLibrary(Settings.this, environment loadLibrary libRef)
  
  reactions += {
    case LibraryChangeRequest(source, f) => updateLibrary(source, f)
  }
}

