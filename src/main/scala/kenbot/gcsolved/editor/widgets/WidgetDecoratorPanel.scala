package kenbot.gcsolved.editor.widgets
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import java.awt.Color
import javax.swing.BorderFactory
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.Alignment
import scala.swing.event.MousePressed


class WidgetDecoratorPanel(val widget: FieldWidget) extends NestedBorderPanel {
  
  top => 
  
  import FormColors._
  
  private def rightMarginPx = 20
  private def leftMarginPx = if (widget.level == 0) 60 else 20
 
  def field = widget.field
  
  def required = field.required && widget.parentAllowsValidation
  
  val label = new Label {
    def updateText() = {
      def redStar = <span color="red">*</span>
      text = <html> 
        { field.humanReadableName }{ if (required) redStar } 
      </html>.toString
    }  
    yAlignment = Alignment.Top
    xAlignment = Alignment.Left
    opaque = false
    updateText()
  }
    
  val descriptionLabel = new Label {
    def updateText() = {
      text = if (widget.hasFocus || !widget.valid) top.tooltip else ""
      foreground = if (widget.valid) selectedTextColor else selectedErrorTextColor
    }  
    border = BorderFactory.createEmptyBorder(0, 5, 0, 0)
    updateText()
  }
  
  border = BorderFactory.createEmptyBorder(3, leftMarginPx, 10, rightMarginPx)
  north = new NestedBorderPanel { 
    opaque = false
    west = new FlowPanel {
      opaque = false
      contents += label
      contents += descriptionLabel
    } 
  }
  west = widget.editor
    
  listenTo(widget, label.mouse.clicks)
  
  private def updateText() {
    background = FormColors.getColor(widget.hasFocus, widget.valid || widget.subWidgets.nonEmpty)
    tooltip = if (widget.valid) field.description
              else <html> 
                     { failures.map { f => <p> {f} </p>} } 
                   </html>.toString
  }
  
  def updateAll() {
    updateText()
    label.updateText()
    descriptionLabel.updateText()
    revalidate()
    repaint()
  }
  
  reactions += {
    case MousePressed(`label`, _, _, _, _) if widget.editable => widget.hasFocus = true
    case ValidationEvent(`widget`, _)  => updateAll()
    case WidgetFocusEvent(`widget`, _, _) => updateAll()
  }
  
  def failures: List[String] = if (widget.subWidgets.isEmpty) field.getFailures(widget.fieldValue) 
                               else Nil
}
