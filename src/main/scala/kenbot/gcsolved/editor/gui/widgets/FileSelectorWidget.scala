package kenbot.gcsolved.editor.gui.widgets
import java.io.File

import scala.swing.event.FocusGained
import scala.swing.event.FocusLost
import scala.swing.Button
import scala.swing.TextField

import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.JFileChooser
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.core.types.FileType
import kenbot.gcsolved.core.Field
import kenbot.gcsolved.core.ResourceEnvironment

class FileSelectorWidget(theField: Field, 
    env: ResourceEnvironment, 
    parentWidget:  => Option[FieldWidget] = None, 
    level: Int = 0) extends DefaultFieldWidget(theField, parentWidget, level) {
  
  top => 
    
  require(theField.fieldType.isInstanceOf[FileType])

  
  lazy val editor = new NestedBorderPanel with MyEditorMixin {
    val textField = new TextField { // Make it a auto-complete combobox, with existing files from the environment
      columns = 20
    }
    val loadButton = Button("Load")(askForFile())
    
    override def requestFocusInWindow() = textField.requestFocusInWindow()
    override def hasFocus: Boolean = textField.hasFocus || loadButton.hasFocus
    
    listenTo(textField, loadButton)
    
    reactions += {
      case FocusGained(`textField` | `loadButton`, other, temp) => 
        textField.selectAll()
        publish(FocusGained(this, other, temp))
        
      case FocusLost(`textField` | `loadButton`, other, temp) => 
        publish(FocusLost(this, other, temp))
    }
    
    west = loadButton
    center = textField
  } 
  
    
  def fileType: FileType = theField.fieldType.asInstanceOf[FileType]
  
  def currentText: String = editor.textField.text
  
  protected override def enforceEditorEditable(b: Boolean) { 
    editor.textField.enabled = b
    editor.loadButton.enabled = b
  }
  
  def fileTypeDescription = fileType.extensions.map(e => "*." + e.toUpperCase).mkString(", ")
  
  def askForFile() {
    val chooser = new JFileChooser
    val filter = new FileNameExtensionFilter(fileTypeDescription, fileType.extensions: _*)
    chooser.setFileFilter(filter)
    
    val returnVal = chooser.showOpenDialog(editor.peer)
    if (returnVal == JFileChooser.APPROVE_OPTION) {
       println("You chose to open this file: " + chooser.getSelectedFile.getName)
    }
  }
  
  def rawFieldValue: Option[Any] = if (currentText == "") None
                                   else Some(new File(currentText))
  
  def fieldValue_=(value: Option[Any]) { 
    value foreach { v => editor.textField.text = v.toString } 
    validateAndUpdate()
  }
}
