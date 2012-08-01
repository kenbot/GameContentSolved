package kenbot.gcsolved.editor.gui.widgets
import scala.swing.TextField
import kenbot.gcsolved.resource.Field
import scala.swing.Reactor
import scala.swing.event.EditDone
import scala.swing.ComboBox
import kenbot.gcsolved.resource.types.SelectOneType
import kenbot.gcsolved.editor.gui.util.FilteringComboBox


class ComboBoxWidget(theField: Field, items: Seq[Any], 
    parentWidget:  => Option[FieldWidget] = None, 
    level: Int = 0) extends DefaultFieldWidget(theField, parentWidget, level) with Reactor {

  lazy val editor = new FilteringComboBox(items, !field.required)(_.toString) with MyEditorMixin
  
  def rawFieldValue: Option[Any] = editor.selectedItem
  
  def fieldValue_=(value: Option[Any]) { 
    editor.selectedItem = value
    validateAndUpdate()
  }
}