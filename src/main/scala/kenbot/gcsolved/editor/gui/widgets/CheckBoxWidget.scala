package kenbot.gcsolved.editor.gui.widgets
import scala.swing.TextField
import kenbot.gcsolved.resource.Field
import scala.swing.Reactor
import scala.swing.event.EditDone
import scala.swing.CheckBox

class CheckBoxWidget(theField: Field, parentWidget:  => Option[FieldWidget] = None, level: Int = 0) 
    extends DefaultFieldWidget(theField, parentWidget, level) with Reactor {

  lazy val editor = new CheckBox with MyEditorMixin
  
  def rawFieldValue: Option[Any] = Some(editor.selected)
  
  def fieldValue_=(value: Option[Any]) { 
    value foreach { b => editor.selected = (b == true) } 
    validateAndUpdate()
  }
}