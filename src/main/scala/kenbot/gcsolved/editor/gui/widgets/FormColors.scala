package kenbot.gcsolved.editor.gui.widgets
import java.awt.Color
import scala.swing.FlowPanel


object FormColors {
  val errorColor = new Color(0xFF, 0xBB, 0xBB)
  val selectedErrorColor = new Color(0xEE, 0xAA, 0xAA)
  val selectedColor = new Color(0xDD, 0xDD, 0xFF)
  val selectedTextColor = new Color(0x88, 0x88, 0xBB)
  val selectedErrorTextColor = new Color(0xAA, 0x66, 0x66)
  val validColor = (new FlowPanel).background
  
  def getColor(hasFocus: Boolean, valid: Boolean): Color = (hasFocus, valid) match {
    case (true, true) => selectedColor
    case (false, true) => validColor
    case (true, false) => selectedErrorColor
    case (false, false) => errorColor
  }
}