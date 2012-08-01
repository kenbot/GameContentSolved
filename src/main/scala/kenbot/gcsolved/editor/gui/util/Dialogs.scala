package kenbot.gcsolved.editor.gui.util
import scala.swing.Dialog
import scala.swing.Frame
import scala.swing.Component
import kenbot.gcsolved.editor.gui.EditorDialog
import scala.swing.Window

object Dialogs {
  module => 
  
  private def contentPane(parent: Window) = if (parent == null) null 
                                            else parent.contents.head
  
  implicit def enrichWindow(w: Window) = new {
    def info(title: String = "Information", message: String) = module.info(w, title, message)
    def error(title: String = "Error", message: String) = module.error(w, title, message)
    def warning(title: String = "Warning", message: String) = module.warning(w, title, message)
    def confirm(title: String = "Confirm", message: String) = module.confirm(w, title, message)
    def modal[C <: Component, R](title: String, child: C)(f: C => R): R = module.modal(w, title, child)(f)
  }
  
  def info(parent: Window, title: String = "Information", message: String) = Dialog.showMessage(contentPane(parent), message, title, Dialog.Message.Info, null)
  def error(parent: Window, title: String = "Error", message: String) = Dialog.showMessage(contentPane(parent), message, title, Dialog.Message.Error, null)
  def warning(parent: Window, title: String = "Warning", message: String) = Dialog.showMessage(contentPane(parent), message, title, Dialog.Message.Warning, null)
  def confirm(parent: Window, title: String = "Confirm", message: String) = Dialog.showConfirmation(contentPane(parent), message, title, 
      Dialog.Options.OkCancel, 
      Dialog.Message.Question, null) == Dialog.Result.Ok
        
  def modal[C <: Component, R](parent: Window, title: String, child: C)(f: C => R): R = {
    val dialog = new EditorDialog(title, parent, child)
    f(child)
  }
}