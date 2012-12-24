package kenbot.gcsolved.editor.gui.typeselector

import scala.swing.event.EditDone
import scala.swing.FlowPanel
import scala.swing.TextField
import scala.swing.Button
import scala.swing.Label
import scala.swing.SimpleSwingApplication
import scala.swing.MainFrame
import java.awt.Dimension
import scala.swing.Swing.{pair2Dimension, onEDT}
import scala.swing.event.KeyPressed
import java.awt.Graphics2D
import java.awt.Color
import kenbot.gcsolved.editor.gui.util.BlankText
import kenbot.gcsolved.editor.gui.util.TextFields



class IntRefinementWidget extends FlowPanel with TypeRefinementWidget {
  
  private val buttonSize: Dimension = (30, 20)
  
  private var showTextVar = false
  
  def showTextFields = showTextVar
  
  private val moreButton = Button("...") {
    showTextVar = true
    onEDT(minText.requestFocus())
    updateComponents()
  }
  
  private val minText = new TextField with BlankText {
    blankText = "Min"
    preferredSize = buttonSize
    TextFields.autoSelectOnFocus(this)
  }
  
  private val dashLabel = new Label("-")
  
  private val maxText = new TextField with BlankText { 
    blankText = "Max"
    preferredSize = buttonSize
    TextFields.autoSelectOnFocus(this)
  }
  
  contents += moreButton
  contents += minText
  contents += dashLabel
  contents += maxText
  
  updateComponents()
  
  private def updateComponents() {
    minText.visible = showTextVar
    dashLabel.visible = showTextVar
    maxText.visible = showTextVar
    moreButton.visible = !showTextVar
    
    revalidate()
    repaint()
  }

  
  def min: Option[Int] = 
    if (showTextVar) tryInt(minText.text.toInt) 
    else None
  
  def min_=(minValue: Option[Int]): Unit = {
    minText.text = minValue.map(_.toString) getOrElse ""
    showTextVar = true //(minValue orElse max).isDefined
    updateComponents()
  }

  
  def max: Option[Int] = 
    if (showTextVar) tryInt(maxText.text.toInt) 
    else None

  
  def max_=(maxValue: Option[Int]): Unit = {
    maxText.text = maxValue.map(_.toString) getOrElse ""
    showTextVar = true //(min orElse maxValue).isDefined
    updateComponents()
  }
  
    
  private def tryInt(expr: => Int) = 
    try Some(expr) 
    catch { case _: NumberFormatException => None }
}