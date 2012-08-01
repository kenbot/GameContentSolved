package kenbot.gcsolved.editor.gui.util

import scala.swing.{Component, BorderPanel}

class NestedBorderPanel extends BorderPanel {
  
  import BorderPanel.Position._
  type Position = BorderPanel.Position.Value
  override type Constraints = Position
  
  private def getForPosition(pos: Position): Component = {
    val firstInPosition = layout.filter(_._2 == pos).headOption
    firstInPosition.map(_._1) getOrElse sys.error("No component found at " + pos)
  }
  
  private def setPosition(c: Component, pos: Position) {
    layout(c) = pos
  }
  
  def north: Component = getForPosition(North)
  def north_=(c: Component) { setPosition(c, North) }
  
  def south: Component = getForPosition(South)
  def south_=(c: Component) { setPosition(c, South) }
  
  def east: Component = getForPosition(East)
  def east_=(c: Component) { setPosition(c, East) }
  
  def west: Component = getForPosition(West)
  def west_=(c: Component) { setPosition(c, West) }
  
  def center: Component = getForPosition(Center)
  def center_=(c: Component) { setPosition(c, Center) }
}