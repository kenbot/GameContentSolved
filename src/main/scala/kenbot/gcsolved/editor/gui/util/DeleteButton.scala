package kenbot.gcsolved.editor.gui.util

import scala.swing.Button
import scala.swing.event.ActionEvent
import java.awt.{Cursor, Color, Dimension}
import Icons.DeleteIcon

trait DeleteButton extends Button {
  cursor = new Cursor(Cursor.HAND_CURSOR)
  icon = new DeleteIcon(Color.lightGray)
  rolloverIcon = new DeleteIcon
  tooltip = "Remove"
  contentAreaFilled = false
  preferredSize = new Dimension(20, 16)
}
