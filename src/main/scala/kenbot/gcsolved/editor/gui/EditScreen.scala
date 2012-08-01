package kenbot.gcsolved.editor.gui
import java.awt.Color
import scala.swing.Swing.pair2Dimension
import scala.swing.BoxPanel
import scala.swing.Component
import scala.swing.Orientation
import javax.swing.BorderFactory
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.editor.gui.widgets.FieldWidget
import kenbot.gcsolved.resource.types.ObjectType
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.ResourceType
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.RefData
import scala.swing.Publisher
import kenbot.gcsolved.editor.gui.widgets.WidgetFocusEvent
import kenbot.gcsolved.editor.gui.widgets.WidgetDecoratorPanel


trait EditScreen extends Publisher {
  def objectType: ObjectType
  
  def values: Seq[RefData]
  def values_=(v: Seq[RefData]): Unit
  
  def makeWidget: Field => FieldWidget

  def fieldWidgets: Seq[FieldWidget]
  
  def panel: Component 
}

object EditScreen {
  def apply(theRefType: RefType, 
            initialValues: Seq[RefData],
            makeWidgetFunc: (Field => FieldWidget)): EditScreen = new EditScreen {
    
    top => 
    
    val objectType: RefType = theRefType
    
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

    val makeWidget = makeWidgetFunc
    
    lazy val fieldWidgets: Seq[FieldWidget] = objectType.fields.values.toList map makeWidget
    
        
    listenTo(fieldWidgets: _*)
    deafTo(top)
    
    reactions += {
      case w @ WidgetFocusEvent(widget, true, level) => 
        fieldWidgets.filter(widget ne).foreach(_.hasFocus = false)
        publish(w)
        
      case w: WidgetFocusEvent => 
        publish(w)
    }
    
    require(values.forall(_.resourceType <:< objectType))
    
    val panel = new EditScreenPanel(fieldWidgets) {
      border = BorderFactory.createEmptyBorder(40, 0, 10, 0)
    }
    
    values = initialValues
  }
}


class EditScreenPanel(val fieldEditors: Seq[FieldWidget]) extends NestedBorderPanel {
  north = new BoxPanel(Orientation.Vertical) {
    contents ++= fieldEditors.map(w => new WidgetDecoratorPanel(w))
  }
}


