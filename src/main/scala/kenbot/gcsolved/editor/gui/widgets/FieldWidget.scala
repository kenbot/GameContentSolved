package kenbot.gcsolved.editor.gui.widgets
import kenbot.gcsolved.core.Field
import scala.swing.Component
import scala.swing.Publisher

trait FieldWidget extends Publisher {
  def field: Field
  val editor: Component with WidgetEditor
  
  var hasFocus: Boolean
  var editable: Boolean
  var fieldValue: Option[Any]
  
  def validateAndUpdate()
  def valid: Boolean
  def subWidgets: Seq[FieldWidget]
  def level: Int
  def parentAllowsValidation: Boolean
  def parentWidget: Option[FieldWidget]
  def parentsToRoot: List[FieldWidget] = parentWidget.toList.flatMap(p => p :: p.parentsToRoot)
  
  final def namedValue: Option[(String, Any)] = fieldValue.map(field.name ->)
  
  override def toString() = getClass.getSimpleName + "(" + field.name + ")"
}
