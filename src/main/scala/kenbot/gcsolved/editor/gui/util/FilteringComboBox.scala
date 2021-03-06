package kenbot.gcsolved.editor.gui.util

import scala.annotation.implicitNotFound
import scala.collection.mutable.ListBuffer
import scala.swing.Swing.{onEDT, pair2Dimension}
import scala.swing.event.FocusGained
import scala.swing.event.KeyTyped
import scala.swing.event.SelectionChanged
import scala.swing.ComboBox
import scala.swing.Component
import scala.swing.ListView
import kenbot.gcsolved.core.Field
import javax.swing.ComboBoxModel
import javax.swing.JTextField
import FilteringComboBox.{BlankItem, withBlankItem}
import scala.swing.event.KeyPressed
import scala.swing.event.Key
import scala.swing.event.FocusLost

object FilteringComboBox {

  private[util] def withBlankItem(seq: Seq[String], showBlankRow: Boolean) = {
    if (showBlankRow) BlankItem +: seq
    else seq
  } 
  
  val BlankItem = "(none)"
}



class FilteringComboBox[A](originalItems: Seq[A], useBlankItem: Boolean = true)(a2s: A => String) 
    extends ComboBox[String](withBlankItem(originalItems map a2s, useBlankItem)) {

  makeEditable()
  
  private val originalStrings: Seq[String] = originalItems map a2s
  
  require(originalStrings.distinct == originalStrings, "All combo box items must be unique: " + originalStrings)
  
  private val itemMap: Map[String, A] = originalItems.map(i => a2s(i) -> i).toMap
  
  def selectedItem: Option[A] = {
    val selectedString = selection.item
    if (selectedString == BlankItem) None
    else itemMap get selectedString
  }
  
  def selectedItem_=(optA: Option[A]) {
    optA match {
      case Some(a) => selection.item = a2s(a)
      case None => selection.item = BlankItem
    }
  }
  
  private implicit def modelToSeq(model: ComboBoxModel[String]): Seq[String] = {
    val list = new ListBuffer[String]
    for (i <- 0 until model.getSize) {
      val a = model.getElementAt(i).asInstanceOf[String]
      list += a
    }
    list.toList
  }
  
  override def hasFocus: Boolean = peer.getEditor.getEditorComponent.hasFocus
  
  private def editorTextField = peer.getEditor.getEditorComponent.asInstanceOf[JTextField]
  editorTextField.setPreferredSize(200, 21) // We don't want the size jumping around
  
  private def currentText: String = editorTextField.getText
  
  def items: Seq[String] = peer.getModel.toList
  
  def items_=(as: Seq[String]) { 
    peer.setModel(ComboBox.newConstantModel(as)) 
    repaint()
  }
  
  listenTo(selection, keys)
  listenTo(Component.wrap(editorTextField), Component.wrap(editorTextField).keys)
  
  reactions += {
    case SelectionChanged(_) => 
      peer.getEditor.setItem(selection.item)
      repaint() 
      
    case FocusGained(src, other, temp) if src ne this => 
      peer.getEditor.selectAll()
      publish(FocusGained(this, other, temp))
      
    case FocusLost(src, other, temp) if src ne this  => 
      publish(FocusLost(this, other, temp))

    case KeyTyped(src, ch, _, _) if ch.toLower >= 'a' && ch.toLower <= 'z' => onEDT {
      val text = currentText
      val filtered = originalStrings.filter(_.toLowerCase contains text.toLowerCase)
      items = if (text == "" && useBlankItem) (BlankItem +: filtered) 
              else filtered
              
      editorTextField.setText(text)
      peer.showPopup()
    }
  }
}
