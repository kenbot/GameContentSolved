package kenbot.gcsolved.editor.screens
import scala.swing.Component
import kenbot.gcsolved.core.types.ObjectType
import kenbot.gcsolved.core.RefData
import scala.swing.Publisher
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

