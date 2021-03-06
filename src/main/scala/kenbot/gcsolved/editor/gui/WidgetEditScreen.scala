package kenbot.gcsolved.editor.gui
import scala.swing.BoxPanel
import scala.swing.Orientation
import javax.swing.BorderFactory
import kenbot.gcsolved.editor.gui.util.{NestedBorderPanel, Components}
import kenbot.gcsolved.editor.widgets.FieldWidget
import kenbot.gcsolved.editor.widgets.WidgetDecoratorPanel
import kenbot.gcsolved.editor.widgets.WidgetFocusEvent
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.Field
import kenbot.gcsolved.core.RefData
import scala.swing.ScrollPane
import scala.swing.Component
import kenbot.gcsolved.editor.gui.util.Components
import kenbot.gcsolved.editor.gui.util.Components
import kenbot.gcsolved.editor.gui.util.Components
import kenbot.gcsolved.editor.screens.EditScreen

class WidgetEditScreen(theRefType: RefType, 
            initialValues: Seq[RefData],
            makeWidget: (Field => FieldWidget)) extends EditScreen {
  
  
  self => 

  val objectType: RefType = theRefType
  
  lazy val fieldWidgets: Seq[FieldWidget] = objectType.fields.values.toList map makeWidget
  
  private lazy val editScreenPanel = new WidgetEditScreenPanel(fieldWidgets)
  
  private lazy val scroller = new ScrollPane(editScreenPanel) {
    import ScrollPane.BarPolicy
    verticalScrollBar.unitIncrement = 16
    verticalScrollBarPolicy = BarPolicy.AsNeeded
    horizontalScrollBarPolicy = BarPolicy.Never
  }
  
  
  def panel: Component = scroller
  
  def isBulkEditSupported = false  
  
  
  private def centerScrollBarOn(w: FieldWidget) {
    val loc = Components.getLocationUnderParent(w.editor, editScreenPanel)
    val scrollTo = loc.y + w.editor.size.height/2 - scroller.size.height/2

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
    case w @ WidgetFocusEvent(widget, true, 0) => 
      fieldWidgets.filter(widget ne).foreach(_.hasFocus = false)
      centerScrollBarOn(widget)
      publish(w)
      
    case w @ WidgetFocusEvent(widget, false, level) => 
      fireUpdatedValues()
      publish(w) 
      
    case w: WidgetFocusEvent => 
      publish(w)
  }
  
  require(values.forall(_.resourceType <:< objectType))
  
  values = initialValues

}

class WidgetEditScreenPanel(val widgets: Seq[FieldWidget]) extends NestedBorderPanel {
  north = new BoxPanel(Orientation.Vertical) {
    contents ++= widgets.map(w => new WidgetDecoratorPanel(w))
  }
}
