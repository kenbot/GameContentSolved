package kenbot.gcsolved.editor.gui.widgets
import kenbot.gcsolved.resource.Field
import scala.swing.Component
import scala.swing.Publisher

trait FieldWidget extends Publisher {
  def field: Field
  val editor: Component with WidgetEditor
  
  def validateAndUpdate()
  def valid: Boolean
  def subWidgets: Seq[FieldWidget]
  
  def level: Int
  var hasFocus: Boolean
  def parentAllowsValidation: Boolean
  def parentWidget: Option[FieldWidget]
  def parentsToRoot: List[FieldWidget] = parentWidget.toList.flatMap(p => p :: p.parentsToRoot)
  
  final def namedValue: Option[(String, Any)] = fieldValue.map(field.name ->)
  
  def fieldValue: Option[Any]
  def fieldValue_=(v: Option[Any]): Unit
}