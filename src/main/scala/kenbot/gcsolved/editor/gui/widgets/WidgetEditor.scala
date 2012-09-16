package kenbot.gcsolved.editor.gui.widgets
import scala.swing.Component

trait WidgetEditor {
  this: Component => 
    
  def widget: FieldWidget

  widget listenTo this 
}
