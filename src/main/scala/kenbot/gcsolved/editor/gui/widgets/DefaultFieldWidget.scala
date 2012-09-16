package kenbot.gcsolved.editor.gui.widgets
import java.awt.Color
import java.awt.Dimension
import scala.swing.Swing.pair2Dimension
import scala.swing.event.Event
import scala.swing.event.FocusGained
import scala.swing.event.FocusLost
import scala.swing.event.MousePressed
import scala.swing.Component
import scala.swing.Alignment
import scala.swing.FlowPanel
import scala.swing.Label
import javax.swing.BorderFactory
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.resource.Field
import scala.swing.Swing


case class ValidationEvent(widget: FieldWidget, valid: Boolean) extends Event

case class WidgetFocusEvent(widget: FieldWidget, focusGained: Boolean, level: Int) extends Event {
  def focusLost = !focusGained
}


abstract class DefaultFieldWidget(val field: Field, parentWidgetParam: => Option[FieldWidget], val level: Int) 
    extends FieldWidget with WidgetFocusBehaviour { 
  
  top =>   
    
  override lazy val parentWidget = parentWidgetParam
  private var acceptableValue = true
  private var editableVar = true
  
  override def editable = editableVar
  override def editable_=(b: Boolean) {
    this.editableVar = b
    subWidgets.foreach(_.editable = b)
    enforceEditorEditable(b)
  }
  
  protected def enforceEditorEditable(b: Boolean)
  
  /**
   * Mix this in to editors to mark it as belonging to us 
   */
  protected trait MyEditorMixin extends WidgetEditor { 
    this: Component => 
    
    def widget: FieldWidget = top
  }  
  
  final def valid: Boolean = !parentAllowsValidation || acceptableValue

  def parentAllowsValidation = parentWidget.map(w => w.field.required || w.fieldValue.isDefined) getOrElse true

  def validateAndUpdate() {
    acceptableValue = field acceptsValue rawFieldValue
    publish(ValidationEvent(this, valid))
  }

  def subWidgets: Seq[FieldWidget] = Seq()
  
  def fieldValue: Option[Any] = {
    if (field acceptsValue rawFieldValue) rawFieldValue.map(field.fieldType.asValue) 
    else rawFieldValue
  }
    
  def rawFieldValue: Option[Any]
  def fieldValue_=(v: Option[Any]): Unit

}
