package kenbot.gcsolved.editor.gui.widgets
import scala.swing.TextField
import kenbot.gcsolved.core.Field
import scala.swing.Reactor
import scala.swing.event.EditDone
import scala.swing.ComboBox
import kenbot.gcsolved.core.types.SelectOneType
import kenbot.gcsolved.editor.gui.util.FilteringComboBox


class ComboBoxWidget[A](theField: Field, 
                        items: Seq[A], 
                        textFunc: A => String = (_: A).toString, 
                        parentWidget:  => Option[FieldWidget] = None, 
                        level: Int = 0) extends DefaultFieldWidget(theField, parentWidget, level) with Reactor {
  top =>
  
  lazy val editor = new FilteringComboBox(items, !field.required)(textFunc) with MyEditorMixin
  
  def rawFieldValue: Option[Any] = editor.selectedItem
  
  def fieldValue_=(value: Option[Any]) { 
    editor.selectedItem = value.asInstanceOf[Option[A]]
    validateAndUpdate()
  }
}
