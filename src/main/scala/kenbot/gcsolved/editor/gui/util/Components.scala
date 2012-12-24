package kenbot.gcsolved.editor.gui.util
import scala.swing.Component
import java.awt.Point
import scala.sys.error

object Components {
  def isChildOf(c: Component, parent: Component): Boolean = {
    val jparent = parent.peer
    def isPeerChildOf(jc: java.awt.Component): Boolean = jc.getParent match {
      case null => false
      case `jparent` => true
      case directParent => isPeerChildOf(directParent)
    }
    isPeerChildOf(c.peer)
  }
  
    
  implicit private def pointAddition(p: Point) = new {
    def +(other: Point): Point = new Point(p.x + other.x, p.y + other.y)
  }
  
  def getLocationUnderParent(c: Component, parent: Component): Point = {
    val jparent = parent.peer
    def getPeerLocationUnderParent(jc: java.awt.Component): Point = jc.getParent match {
      case null => error("Not actually under expected parent")
      case `jparent` => jc.getLocation
      case directParent => jc.getLocation + getPeerLocationUnderParent(directParent)
    }
    getPeerLocationUnderParent(c.peer)
  }

}