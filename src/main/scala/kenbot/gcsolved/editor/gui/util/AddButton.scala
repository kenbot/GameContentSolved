package kenbot.gcsolved.editor.gui.util

import java.awt.Cursor
import java.awt.Dimension
import Icons.AddIcon
import scala.swing.Button
import java.awt.Color

class AddButton(text: String) extends Button(text) {
  cursor = new Cursor(Cursor.HAND_CURSOR)
  icon = new AddIcon(Color.lightGray)
  rolloverIcon = new AddIcon
  tooltip = "Add"
  contentAreaFilled = false
  preferredSize = new Dimension(20, 16)
}
