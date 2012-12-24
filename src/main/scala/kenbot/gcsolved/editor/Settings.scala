package kenbot.gcsolved.editor
import kenbot.gcsolved.editor.gui.widgets.FieldWidget
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.editor.gui.{EditScreen, WidgetEditScreen}
import kenbot.gcsolved.core.ResourceSchema
import scala.swing.Frame
import kenbot.gcsolved.core.ResourceEnvironment
import kenbot.gcsolved.core.ResourceLibrary
import kenbot.gcsolved.core.Field
import scala.swing.Publisher
import scala.swing.event.Event
import scala.swing.Component
import kenbot.gcsolved.editor.gui.DefaultMakeWidget


case class LibraryChangedEvent(source: Any, newLibrary: ResourceLibrary, oldLibrary: ResourceLibrary) extends Event
case class LibraryChangeRequest(source: Any, newLibrary: ResourceLibrary) extends Event

trait Settings {
 
  def makeEditScreen: RefType => EditScreen
  
  def iconDir: String
  def iconFileExt: String
  def imageFileExt: String
  def frameTitle: String
  def resourceDir: String
  
  def currentLibrary: ResourceLibrary
  
  final def schema: ResourceSchema = currentLibrary.schema
  def environment: ResourceEnvironment
  def mainFrame: Frame
  
}

trait PublishableLibraryChanges extends Publisher {
  this: Settings => 
    
  def initialLibrary: ResourceLibrary
  
  private var libraryVar = initialLibrary
  
  final def currentLibrary: ResourceLibrary = libraryVar
  
  def updateLibrary(source: Any, f: ResourceLibrary => ResourceLibrary) {
    updateLibrary(source, f(currentLibrary))
  }
  
  def updateLibrary(source: Any, newLibrary: ResourceLibrary) {
    val event = LibraryChangedEvent(source, newLibrary, currentLibrary)
    libraryVar = newLibrary
    publish(event)
  }
  
}
