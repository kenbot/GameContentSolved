package kenbot.gcsolved.editor.gui.widgets
import scala.swing.event.EditDone
import scala.swing.Reactor
import scala.swing.TextField
import kenbot.gcsolved.core.Field
import kenbot.gcsolved.editor.gui.util.TextFields
import java.awt.Dimension
import scala.swing.Swing.pair2Dimension

class TextFieldWidget(theField: Field, 
    val columns: Int = 20,
    parentWidget: => Option[FieldWidget] = None, 
    level: Int = 0) extends DefaultFieldWidget(theField, parentWidget, level) {
  
  top => 
    
  lazy val editor = new TextField with MyEditorMixin {
    this.columns = top.columns
    TextFields.autoSelectOnFocus(this)
    top.listenTo(this, keys)
  }
  
  def rawFieldValue: Option[Any] = {
    val text = editor.text.trim
    if (text != "") Some(text) 
    else None
  }
  
  def fieldValue_=(value: Option[Any]) { 
    editor.text = value.map(_.toString) getOrElse ""
    validateAndUpdate()
  }
  
  reactions += { 
    case EditDone(_) => validateAndUpdate()
  }
}