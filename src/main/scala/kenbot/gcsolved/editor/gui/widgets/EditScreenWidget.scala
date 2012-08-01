package kenbot.gcsolved.editor.gui.widgets
import kenbot.gcsolved.resource.Field
import scala.swing.Reactor
import kenbot.gcsolved.resource.ValueData
import kenbot.gcsolved.resource.types.ValueType
import sys.error
import javax.swing.BorderFactory
import java.awt.Color
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import scala.swing.BoxPanel
import scala.swing.Orientation

class EditScreenWidget(theField: Field, 
    theSubWidgets: => Seq[FieldWidget], 
    parentWidget:  => Option[FieldWidget] = None, 
    level: Int = 0) extends DefaultFieldWidget(theField, parentWidget, level) {
  
  self => 
    
  override lazy val subWidgets = theSubWidgets  
    
  require(theField.fieldType.isInstanceOf[ValueType], "Expecting a value type, found: " + theField.fieldType)  
  
  lazy val editor = new NestedBorderPanel with MyEditorMixin {
    north = new BoxPanel(Orientation.Vertical) {
      contents ++= subWidgets.map(w => new WidgetDecoratorPanel(w))
    }
    border = BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1)
  }

  listenTo(subWidgets: _*)
  deafTo(this)
  
  reactions += {
    case ValidationEvent(widget, success) => validateAndUpdate()
    case wfe: WidgetFocusEvent => publish(wfe)
  }
  
  def valueType: ValueType = theField.fieldType.asInstanceOf[ValueType]
  
  def rawFieldValue: Option[Any] = {
    val emptyMap = Map.empty[Field.Name, Any]
    val fieldMap = (emptyMap /: subWidgets) {
      (map, widget) => widget.fieldValue match {
        case Some(v) => map + (widget.field.name -> v)
        case None => map
      }
    }

    if (fieldMap.isEmpty) None
    else Some(ValueData(valueType, fieldMap))
  }
  
  
  def fieldValue_=(value: Option[Any]): Unit = value match {
    case Some(vd: ValueData) => subWidgets foreach { w => 
      w.fieldValue = vd.fields get w.field.name 
    }
    case None => subWidgets foreach { _.fieldValue = None }
    case x => error("Expected a ValueData, got " + x.getClass.getName + ": " + x)
  }
}