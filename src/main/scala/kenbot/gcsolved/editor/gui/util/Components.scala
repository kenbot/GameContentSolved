package kenbot.gcsolved.editor.gui.util
import scala.swing.Component

class Components {
  def isChildOf(c: Component, parent: Component): Boolean = {
    def isPeerChildOf(jc: java.awt.Component, jparent: java.awt.Container): Boolean = jc.getParent match {
      case null => false
      case `jparent` => true
      case directParent => isPeerChildOf(directParent, jparent)
    }
    isPeerChildOf(c.peer, parent.peer)
  }
}