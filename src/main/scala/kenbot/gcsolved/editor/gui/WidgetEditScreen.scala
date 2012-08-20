package kenbot.gcsolved.editor.gui
import scala.swing.BoxPanel
import scala.swing.Orientation

import javax.swing.BorderFactory
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.editor.gui.widgets.FieldWidget
import kenbot.gcsolved.editor.gui.widgets.WidgetDecoratorPanel
import kenbot.gcsolved.editor.gui.widgets.WidgetFocusEvent
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.RefData

class WidgetEditScreen(theRefType: RefType, 
            initialValues: Seq[RefData],
            makeWidget: (Field => FieldWidget)) extends EditScreen {
  
  
  self => 
  
    
  val objectType: RefType = theRefType
  
  lazy val fieldWidgets: Seq[FieldWidget] = objectType.fields.values.toList map makeWidget
  
  val panel = new EditScreenPanel(fieldWidgets) {
    border = BorderFactory.createEmptyBorder(40, 0, 10, 0)
  }
  
  private[this] var editableVar = true
  override def editable = editableVar
  override def editable_=(b: Boolean) {
    editableVar = b
    fieldWidgets.foreach(_.editable = b)
  }
  
  def values: Seq[RefData] = {
    val namedValues = fieldWidgets.flatMap(_.namedValue).toMap
    Seq(RefData(theRefType, namedValues))
  }
  
  def values_=(newValues: Seq[RefData]) {
    for (w <- fieldWidgets) {
      val newValue = newValues.headOption.flatMap(_ get w.field.name)
      w.fieldValue = newValue
    }
  }

      
  listenTo(fieldWidgets: _*)
  deafTo(self)
  
  reactions += {
    case w @ WidgetFocusEvent(widget, true, level) => 
      fieldWidgets.filter(widget ne).foreach(_.hasFocus = false)
      publish(w)
      
    case w: WidgetFocusEvent => 
      publish(w)
  }
  
  require(values.forall(_.resourceType <:< objectType))
  
  
  values = initialValues

}

class EditScreenPanel(val fieldEditors: Seq[FieldWidget]) extends NestedBorderPanel {
  north = new BoxPanel(Orientation.Vertical) {
    contents ++= fieldEditors.map(w => new WidgetDecoratorPanel(w))
  }
}