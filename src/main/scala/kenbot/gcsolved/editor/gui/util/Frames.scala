package kenbot.gcsolved.editor.gui.util
import javax.swing.JComponent
import java.awt.Window
import scala.sys.error

object Frames {

  def getRootPane(comp: JComponent): Window = {
    var peer = comp.getParent
    while (peer != null) {
      peer match {
        case w: Window => return w
        case _ => peer = peer.getParent
      }
    }
    error("Expecting to find root window")
  }
}