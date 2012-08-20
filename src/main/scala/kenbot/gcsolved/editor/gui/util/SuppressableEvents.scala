package kenbot.gcsolved.editor.gui.util
import scala.util.DynamicVariable


/**
 * When mixed-in, allows a class to suppress events by including code in a suppressEvents { } block.
 * The class' event-handling code should consult the shouldSuppressEvents variable before deciding to handle an event.
 */
trait SuppressableEvents {
  private[this] val shouldSuppress = new DynamicVariable(false)
  
  protected def suppressEvents(thunk: => Unit) {
    shouldSuppress.withValue(true)(thunk)
  }
  
  protected def shouldSuppressEvents: Boolean = shouldSuppress.value
}