package kenbot.gcsolved.editor.gui
import java.io.File
import scala.swing.Frame
import kenbot.gcsolved.editor.Settings
import kenbot.gcsolved.core.ResourceEnvironment
import kenbot.gcsolved.core.ResourceLibrary
import kenbot.gcsolved.core.ResourceSchema
import kenbot.gcsolved.editor.widgets.DefaultMakeWidget
import kenbot.gcsolved.editor.screens.DummyEditScreen

object TestSettings extends Settings {
 
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
