package kenbot.gcsolved.editor

import java.io.File
import scala.swing.Swing.pair2Dimension
import scala.swing.Dimension
import scala.swing.Frame
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication
import herezod.HerezodSchema
import kenbot.gcsolved.editor.gui.{WidgetEditScreen}
import kenbot.gcsolved.editor.screens.EditScreen
import kenbot.gcsolved.editor.widgets.DefaultMakeWidget
import kenbot.gcsolved.editor.gui.MainPanel
import kenbot.gcsolved.core.Field.symbolAndValue2namePair
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.Field
import kenbot.gcsolved.core.RefData
import kenbot.gcsolved.core.ResourceEnvironment
import kenbot.gcsolved.core.ResourceLibrary
import kenbot.gcsolved.core.ResourceSchema
import javax.swing.UIManager
import herezod.HerezodSchema



object Main extends SimpleSwingApplication {
  
  object Settings extends Settings {
    val makeEditScreen: RefType => EditScreen = 
      r => new WidgetEditScreen(r, 
          currentLibrary.allResourcesByType(r).toList, 
          DefaultMakeWidget(this)) 
      
    val iconDir = "/icons"
    val iconFileExt = "png"
    val imageFileExt = "png"
    val frameTitle = "GameContentSolved"
    val resourceDir = "examples"
    
    import HerezodSchema._
  
    lazy val otherLibrary = ResourceLibrary("blah", HerezodSchema.Schema).
        addResource( ActorType('Name -> "Gremlin") )
    
    lazy val initialLibrary = ResourceLibrary("new", HerezodSchema.Schema).
        addLinkedLibraries(otherLibrary).
        addResource( ActorType('Name -> "Orc") ).
        addResource( ActorType('Name -> "Human") ).
        addResource( ActorType('Name -> "Gnome") )
        
    lazy val environment = ResourceEnvironment(new File(resourceDir))
    
    lazy val mainFrame = new MainFrame {
      contents = new MainPanel(Settings)
      title = frameTitle + " - <none>"
      size = (1024, 768)
    }
  }
  
  override lazy val top = Settings.mainFrame
}
