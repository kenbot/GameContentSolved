package kenbot.gcsolved.editor.gui.util

import java.awt.{Color, Graphics, Polygon, Graphics2D}
import javax.swing.Icon

object Icons {

  class AddIcon(var foreground: Color) extends Icon {
    def this() = this(new Color(0, 180, 0))
    
    override def paintIcon(c: java.awt.Component, g: Graphics, x: Int, y: Int) {
      val g2 = g.asInstanceOf[Graphics2D]
      
      val line1 = new Polygon(Array(7, 10, 10, 7), Array(2, 2, 15, 15), 4)
      val line2 = new Polygon(Array(2, 15, 15, 2), Array(7, 7, 10, 10), 4)
      line1.translate(x, y)
      line2.translate(x, y)
  
      g setColor foreground 
      g2 fill line1 
      g2 fill line2 
    }
    
    override def getIconWidth() = 16
    override def getIconHeight() = 16
  }


  class DeleteIcon(var foreground: Color) extends Icon {
    def this() = this(Color.red)
    
    override def paintIcon(c: java.awt.Component, g: Graphics, x: Int, y: Int) {
      val g2 = g.asInstanceOf[Graphics2D]
  
      val line1 = new Polygon(Array(2, 4, 14, 12), Array(4, 2, 12, 14), 4)
      val line2 = new Polygon(Array(2, 12, 14, 4), Array(12, 2, 4, 14), 4)
      line1.translate(x, y)
      line2.translate(x, y)
      
      g setColor foreground
      g2 fill line1
      g2 fill line2
    }
    
    override def getIconWidth() = 16
    override def getIconHeight() = 16
  }
}
