package kenbot.gcsolved.editor.gui.util

import scala.swing.Dimension
import scala.swing.Insets
import scala.swing.Action
import scala.swing.Component
import scala.swing.Orientable
import scala.swing.Orientation
import scala.swing.SequentialContainer

import javax.swing.JToolBar

class ToolBar(name: String, orientationValue: Orientation.Value) extends Component 
    with SequentialContainer.Wrapper 
    with Orientable.Wrapper {
  
  def this() = this("", Orientation.Horizontal)
  
  lazy override val peer = new JToolBar(name, orientationValue.id) with SuperMixin
  
  def addAction(action: Action) { peer.add(action.peer) }
  def addSeparator() { peer.addSeparator() }
  def addSeparator(size: Dimension) { peer.addSeparator(size) }
  
  def margin: Insets = peer.getInsets
  def rollover: Boolean = peer.isRollover
  def rollover_=(b: Boolean) { peer.setRollover(b) }
  def borderPainted: Boolean = peer.isBorderPainted
  def borderPainted_=(b: Boolean)  { peer.setBorderPainted(b) }
  def floatable: Boolean = peer.isFloatable
  def floatable_=(b: Boolean) { peer.setFloatable(b) }

}