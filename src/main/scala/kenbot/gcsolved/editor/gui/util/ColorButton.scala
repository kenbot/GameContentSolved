package kenbot.gcsolved.editor.gui.util

import scala.swing._
import java.awt.{Color, Graphics, Dimension}
import java.awt.event.ActionListener
import javax.swing.{JColorChooser, JPanel, Icon}
import scala.swing.event._

class ColorButton extends Button {
  self => 
  
  preferredSize = new Dimension(32, 32)
  
  icon = new Icon {
    def getIconWidth() = size.width - 4
    def getIconHeight() = size.height - 4
    def paintIcon(c: java.awt.Component, g: Graphics, x: Int, y: Int) = {
      g.setColor(color)
      g.fillRect(x, y, getIconWidth, getIconHeight)
      g.setColor(Color.black)
      g.drawRect(x, y, getIconWidth, getIconHeight)
    }
  }
  
  var color = Color.white
  
  def heldValue = color
  def heldValue_=(newVal: Color) = color = newVal
  
  reactions += {
    case ButtonClicked(self) =>
      val pane = new JColorChooser(color)
      pane.setPreviewPanel(new JPanel)
      
      val selectColorOnOk: ActionListener = Swing.ActionListener {event =>
        val selectedColor = pane.getColor
        if (selectedColor != null)
          color = selectedColor
      }
      
      val colorDialog = JColorChooser.createDialog(self.peer, "Choose Color", true, pane, selectColorOnOk, null);
      colorDialog.setVisible(true)

      repaint
  }

}
