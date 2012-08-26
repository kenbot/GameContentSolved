package kenbot.gcsolved.editor.gui
import scala.swing.BoxPanel
import scala.swing.Orientation
import javax.swing.BorderFactory
import kenbot.gcsolved.editor.gui.util.{NestedBorderPanel, Components}
import kenbot.gcsolved.editor.gui.widgets.FieldWidget
import kenbot.gcsolved.editor.gui.widgets.WidgetDecoratorPanel
import kenbot.gcsolved.editor.gui.widgets.WidgetFocusEvent
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.RefData
import scala.swing.ScrollPane
import scala.swing.Component
import kenbot.gcsolved.editor.gui.util.Components
import kenbot.gcsolved.editor.gui.util.Components
import kenbot.gcsolved.editor.gui.util.Components

class WidgetEditScreen(theRefType: RefType, 
            initialValues: Seq[RefData],
            makeWidget: (Field => FieldWidget)) extends EditScreen {
  
  
  self => 
  
    
  val objectType: RefType = theRefType
  
  lazy val fieldWidgets: Seq[FieldWidget] = objectType.fields.values.toList map makeWidget
  
  private lazy val editScreenPanel = new EditScreenPanel(fieldWidgets)
  
  private lazy val scroller = new ScrollPane(editScreenPanel) {
    import ScrollPane.BarPolicy
    verticalScrollBar.unitIncrement = 16
    verticalScrollBarPolicy = BarPolicy.AsNeeded
    horizontalScrollBarPolicy = BarPolicy.Never
  }
  
  
  def panel: Component = scroller
  
    
  private def centerScrollBarOn(w: FieldWidget) {
    
    val loc = Components.getLocationUnderParent(w.editor, editScreenPanel)
    val scrollTo = loc.y - scroller.size.height/2
    println("scrollTo: " + scrollTo)

    scroller.verticalScrollBar.value = scrollTo
    scroller.repaint()
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
      println("GAIN FOCUS: " + widget.field.name)
      centerScrollBarOn(widget)
      publish(w)
      
    case w @ WidgetFocusEvent(widget, false, level) => 
      println("LOST FOCUS: " + widget.field.name)
      publish(w) 
      
    case w: WidgetFocusEvent => 
      publish(w)
  }
  
  require(values.forall(_.resourceType <:< objectType))
  
  
  values = initialValues

}

class EditScreenPanel(val widgets: Seq[FieldWidget]) extends NestedBorderPanel {
  north = new BoxPanel(Orientation.Vertical) {
    contents ++= widgets.map(w => new WidgetDecoratorPanel(w))
  }
}