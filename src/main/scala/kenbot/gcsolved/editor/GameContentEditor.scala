package kenbot.gcsolved.editor

import java.io.File
import scala.swing.Swing.pair2Dimension
import scala.swing.Dimension
import scala.swing.Frame
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication
import herezod.types.ActorType
import herezod.types.HerezodSchema
import kenbot.gcsolved.editor.gui.DefaultMakeWidget
import kenbot.gcsolved.editor.gui.MainPanel
import kenbot.gcsolved.resource.Field.symbolAndValue2namePair
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.RefData
import kenbot.gcsolved.resource.ResourceEnvironment
import kenbot.gcsolved.resource.ResourceLibrary
import kenbot.gcsolved.resource.ResourceSchema
import javax.swing.UIManager



object GameContentEditor extends SimpleSwingApplication {

  
  type Settings = kenbot.gcsolved.editor.Settings with PublishableLibraryChanges
  
  object Settings extends kenbot.gcsolved.editor.Settings with PublishableLibraryChanges {
    val makeWidget = DefaultMakeWidget(this)
    val iconDir = "/icons"
    val iconFileExt = "png"
    val imageFileExt = "png"     
    val frameTitle = "GameContentSolved"
    val resourceDir = "examples"
    

    lazy val otherLibrary = ResourceLibrary("blah", HerezodSchema).
        addResource( RefData(ActorType, 'Name -> "Gremlin") )
    
    lazy val initialLibrary = ResourceLibrary("new", HerezodSchema).
        addLinkedLibraries(otherLibrary).
        addResource( RefData(ActorType, 'Name -> "Orc") ).
        addResource( RefData(ActorType, 'Name -> "Human") ).
        addResource( RefData(ActorType, 'Name -> "Gnome") )
        
    lazy val environment = ResourceEnvironment(new File(resourceDir))
    
    lazy val mainFrame = new MainFrame {
      contents = new MainPanel(Settings)
      title = Settings.frameTitle + " - <none>"
      size = (1024, 768): Dimension
    }
  }
  
  override lazy val top = Settings.mainFrame
}
