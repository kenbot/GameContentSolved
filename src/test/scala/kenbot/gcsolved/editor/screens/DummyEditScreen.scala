package kenbot.gcsolved.editor.screens

import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.RefData


object DummyEditScreen {
  def apply(refType: RefType) = new DummyEditScreen(refType)
}

class DummyEditScreen(val objectType: RefType) extends EditScreen {
  def isBulkEditSupported = false
  var editable = true
  var values: Seq[RefData] = Nil  

  def panel = new scala.swing.FlowPanel
}
