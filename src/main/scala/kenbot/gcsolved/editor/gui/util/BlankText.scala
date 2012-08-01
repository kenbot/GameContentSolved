package kenbot.gcsolved.editor.gui.util
import java.awt.Graphics2D
import scala.swing.TextField
import java.awt.Color
import scala.swing.TextComponent

trait BlankText extends TextComponent {
  var blankText: String = ""
    
  override def paintComponent(g: Graphics2D) {
    super.paintComponent(g)
    if (text == "") {
      g.setColor(new Color(0xAA, 0xAA, 0xAA))
      val fontHeight = g.getFontMetrics.getHeight
      val fieldHeight = size.height
      val diff = (fieldHeight - fontHeight)/4
      
      g.drawString(blankText, 3, fontHeight + diff)
    }
  }
}