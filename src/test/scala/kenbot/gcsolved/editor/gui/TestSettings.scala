package kenbot.gcsolved.editor.gui
import java.io.File

import scala.swing.Frame

import kenbot.gcsolved.editor.PublishableLibraryChanges
import kenbot.gcsolved.editor.Settings
import kenbot.gcsolved.resource.ResourceEnvironment
import kenbot.gcsolved.resource.ResourceLibrary
import kenbot.gcsolved.resource.ResourceSchema

object TestSettings extends Settings with PublishableLibraryChanges {
 
  def makeWidget = DefaultMakeWidget(this, None)
  def makeEditScreen = DummyEditScreen.apply
  
  def iconDir = "icondir"
  def iconFileExt = ".foo"
  def imageFileExt = ".bar"
  def frameTitle = "foo"
  def resourceDir = "resourcedir"
  
  lazy val initialLibrary = ResourceLibrary("foo", ResourceSchema())
  lazy val environment = ResourceEnvironment(new File(resourceDir))
  def mainFrame: Frame = sys.error("You don't need Swing for tests.")
  
}
