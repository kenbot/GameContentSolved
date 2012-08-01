package kenbot.gcsolved.editor.gui

import scala.swing.Swing.pair2Dimension
import scala.swing.Swing.pair2Point
import scala.swing.Button
import scala.swing.Component
import scala.swing.Dialog
import scala.swing.GridPanel
import scala.swing.Window

import EditorDialog.Result
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel

object EditorDialog {
  
  def apply(title: String, parent: Window, child: Component)(closeHandler: PartialFunction[Result, Boolean]): EditorDialog = {
    new EditorDialog(title, parent, child, closeHandler)
  }
  
  val defaultCloseHandler: PartialFunction[Result, Boolean] = { case _ => true }
  
  type Result = Result.Value
  
  object Result extends Enumeration {
    val OK = Value("OK")
    val Cancel = Value("Cancel")
    val Close = Value("Close")
  }
}

class EditorDialog(theTitle: String, parent: Window, child: Component, 
    closeHandler: PartialFunction[Result, Boolean] = EditorDialog.defaultCloseHandler) {
  
  top =>

  private var resultVar = Result.Close
  
  def result = resultVar
  
  object dialog extends Dialog(parent) {
    thisDialog =>
    
    title = theTitle
    
    override def contents_=(c: Component) {
      if (peer.getContentPane.getComponentCount > 0) {
        val old = peer.getContentPane.getComponent(0)
        peer.getContentPane.remove(old)
      }
      peer.getContentPane.add(c.peer)
    }

    def relativeToParentWidth(ratio: Double) = (parent.size.width * ratio).toInt
    def relativeToParentHeight(ratio: Double) = (parent.size.height * ratio).toInt
    size = (relativeToParentWidth(0.9), relativeToParentHeight(0.9))
    this.location = (relativeToParentWidth(0.05), relativeToParentHeight(0.05))
    setLocationRelativeTo(parent)
    modal = true
    
    contents = new NestedBorderPanel {
      center = child
      south = new NestedBorderPanel {
        east = new GridPanel(1,2) {
          contents += Button("OK") { handleResultAndCloseIfAllowed(Result.OK) }
          contents += Button("Cancel") { handleResultAndCloseIfAllowed(Result.Cancel) }
        }
      }
    }
    
    private def handleResultAndCloseIfAllowed(res: Result.Value) {
      resultVar = res
      val allowedToClose = !(closeHandler isDefinedAt res) || closeHandler(res)
      if (allowedToClose) {
        thisDialog.visible = false
        dispose
      }
    }
  }
  
  dialog.visible = true
}
