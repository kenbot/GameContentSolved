package kenbot.gcsolved.editor.gui.util
import scala.swing.TextComponent
import scala.swing.event.FocusGained
import scala.swing.Publisher
import scala.swing.Swing.onEDT
import scala.swing.event.FocusLost

object TextFields {
  def autoSelectOnFocus(text: TextComponent) {
    text.reactions += {
      case FocusGained(`text`, _, _) => text.selectAll
    }
  }
}