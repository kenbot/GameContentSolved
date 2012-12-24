package kenbot.gcsolved.editor.gui.sidebar
import scala.swing.Swing.pair2Dimension
import scala.swing.Alignment
import scala.swing.GridPanel
import scala.swing.Label
import scala.swing.TextArea
import scala.swing.TextField
import kenbot.gcsolved.core.ResourceLibrary
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import javax.swing.BorderFactory
import java.awt.Color
import scala.swing.BoxPanel
import scala.swing.Orientation
import kenbot.gcsolved.editor.gui.util.TextFields
import scala.swing.Component

class AboutPage(library: ResourceLibrary) extends NestedBorderPanel {
  
   val nameText = new TextField(library.name) {
     TextFields.autoSelectOnFocus(this)
   }
   
   val descriptionText = new TextArea(library.description) {
     charWrap = true
     lineWrap = true
     rows = 10
     columns = 10
     border = BorderFactory.createLineBorder(new Color(0x80, 0x80, 0x80), 1)
     TextFields.autoSelectOnFocus(this)
   }
   
   val versionLabel = new Label(library.version.toString)
   
   border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
   
   def createLabel(text: String) = new Label(text) {
     horizontalAlignment = Alignment.Left
     preferredSize = (200,20)
   }
   
   def leftAlign(c: Component): Component = new NestedBorderPanel {
     west = c
   }
   
   north = new BoxPanel(Orientation.Vertical) {
     contents += leftAlign(createLabel("Library name"))
     contents += nameText
     contents += leftAlign(createLabel("Description"))
     contents += descriptionText
     contents += leftAlign(createLabel("Version"))
     contents += versionLabel
   }
}