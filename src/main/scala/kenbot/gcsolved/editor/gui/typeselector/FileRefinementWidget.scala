package kenbot.gcsolved.editor.gui.typeselector
import java.awt.Dimension

import scala.swing.Swing.pair2Dimension
import scala.swing.FlowPanel
import scala.swing.Label
import scala.swing.TextField

import kenbot.gcsolved.editor.gui.util.BlankText
import kenbot.gcsolved.editor.gui.util.TextFields

class FileRefinementWidget extends FlowPanel with TypeRefinementWidget {

  private val buttonSize: Dimension = (60, 20)
  
  private val categoryText = new TextField with BlankText {
    blankText = "Category"
    tooltip = "Category (eg. \"images\")"
    preferredSize = buttonSize
    TextFields.autoSelectOnFocus(this)
  }
  
  private val dashLabel = new Label("-")
  
  private val extensionsText = new TextField with BlankText { 
    blankText = "File types"
    tooltip = "File types (eg. \"png,wav\")"
    preferredSize = buttonSize
    TextFields.autoSelectOnFocus(this)
  }
  
  contents += categoryText
  contents += extensionsText
  
  def category: String = categoryText.text
  def category_=(cat: String) { categoryText.text = cat }
  
  def extensions: List[String] = extensionsText.text.split(",").toList.map(_.trim)
  def extensions_=(exts: List[String]) { extensionsText.text = exts.mkString(",") }
}