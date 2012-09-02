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
import scala.swing.event.Event


trait UpdatedValuesEvent extends Event { val src: EditScreen }
case class UpdateValues(src: EditScreen, values: Seq[RefData]) extends UpdatedValuesEvent

trait EditScreen extends Publisher {
  def objectType: ObjectType
  def isBulkEditSupported: Boolean
  var editable: Boolean
  var values: Seq[RefData]
  
  def panel: Component 

  //def replaceBadValues(message: String, correctValues: Seq[RefData]): Unit 

  protected final def fireUpdatedValues() {
    publish(UpdateValues(this, values)) 
  }
}

