package kenbot.gcsolved.editor.gui.widgets
import scala.swing.Swing
import scala.util.DynamicVariable
import scala.swing.event.FocusGained
import scala.swing.event.FocusLost

trait WidgetFocusBehaviour extends FieldWidget {
  
  self => 
  
  private var hasFocusVar = false
  
  final def hasFocus: Boolean = hasFocusVar
  
  final def hasFocus_=(focusGained: Boolean) { 
    val isChange = focusGained != hasFocusVar
    hasFocusVar = focusGained
    
    if (isChange) {
      if (focusGained) {
        def noChildrenHaveFocus = subWidgets.nonEmpty && subWidgets.forall(!_.hasFocus)
        if (noChildrenHaveFocus) {
          subWidgets(0).hasFocus = true
        }
      }
      else {
        subWidgets.foreach(_.hasFocus = false)
      }
      publish(WidgetFocusEvent(this, focusGained, level))
    }
  }
  
  private def unFocusChildrenExcept(focusedChild: FieldWidget) {
    val otherChildren = subWidgets.filter(focusedChild ne)
    otherChildren.foreach(_.hasFocus = false)
  }
  
  reactions += {
    case FocusGained(`editor`, other, false) => 
      println(field.name + ": Editor gained focus")
      hasFocus = true
      validateAndUpdate()

    case FocusLost(`editor`, other, false) =>
      println(field.name + ": Editor lost focus")
      validateAndUpdate()
          
    case WidgetFocusEvent(`self`, true, _) if subWidgets.isEmpty => 
      println(field.name + ": Gained from self, no children")
      editor.requestFocusInWindow()
      
    case WidgetFocusEvent(focusedChild, true, level) if level == this.level+1 => 
      println(field.name + ": Child from next level gained focus")
      hasFocus = true
      unFocusChildrenExcept(focusedChild)
      publish(WidgetFocusEvent(this, true, this.level))
      
    case WidgetFocusEvent(unfocusedChild, false, level) if level == this.level+1 =>
      println(field.name + ": Child from next level lost focus")
      if (subWidgets.forall(!_.hasFocus)) {
        hasFocus = false
      }
  }
}
