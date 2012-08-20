package kenbot.gcsolved.editor.gui.widgets
import scala.swing.event.SelectionChanged
import scala.swing.ComboBox
import scala.swing.Component
import scala.swing.GridPanel
import scala.swing.Label
import kenbot.gcsolved.editor.gui.util.FilteringComboBox
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.resource.types.ObjectType
import kenbot.gcsolved.resource.types.ResourceType
import kenbot.gcsolved.resource.types.ValueType
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.ResourceSchema
import kenbot.gcsolved.resource.ValueData
import scala.swing.FlowPanel
import scala.swing.TextField

class ChooseTypeWidget(theField: Field, 
    schema: ResourceSchema, 
    makeWidget: Field => FieldWidget, 
    parentWidget:  => Option[FieldWidget] = None, 
    level: Int = 0) extends DefaultFieldWidget(theField, parentWidget, level) {
  
  top => 
  
  val editor = new TextField with MyEditorMixin
  def rawFieldValue: Option[Any] = None
  def fieldValue_=(v: Option[Any]) {}
    /*
 
  lazy val fieldType: ValueType = theField.fieldType.asInstanceOf[ValueType]
  lazy val concreteSubTypes = schema.valueTypes.filter(t => !t.isAbstract && t <:< fieldType).toList
  //def makeWidgets(forSubType: ObjectType): List[FieldWidget] = forSubType.fields.values.toList map makeWidget
  
  private lazy val centerContent = new GridPanel(1,1)
  private lazy val typesCombo = new FilteringComboBox(concreteSubTypes, false)(_.name)
  private var currentFieldWidgets: List[FieldWidget] = Nil
  
  lazy val editor = new NestedBorderPanel with MyEditorMixin {
    north = typesCombo
    center = centerContent
    
    top listenTo typesCombo.selection
    typesCombo.selectedItem foreach updateForType
  } 
  
  
  
  def updateForType(t: ObjectType) {
    centerContent.contents.clear()
    deafTo(currentFieldWidgets: _*)
    
    val typePanel = new EditScreenPanel(makeWidgets(t))
    
    currentFieldWidgets = typePanel.fieldEditors.toList
    listenTo(currentFieldWidgets: _*)
    
    centerContent.contents += typePanel
    validateAndUpdate()
    editor.revalidate()
    editor.repaint()
  }
  
  reactions += {
    case ValidationEvent(widget, success) if widget ne this => validateAndUpdate()
    
    case SelectionChanged(`typesCombo`) => 
      val selectedType = typesCombo.selectedItem getOrElse noTypeSelectedError
      updateForType(selectedType)
  }

  //override def hasFocus: Boolean = subWidgets.exists(_.hasFocus)
  
  override def subWidgets: Seq[FieldWidget] = currentFieldWidgets
  
  //override def hasFocus_=(b: Boolean) { subWidgets.headOption.foreach(_.hasFocus = b) }
  
  def rawFieldValue: Option[Any] = {
    if (subWidgets.isEmpty) {
      None
    }
    else {
      val namedValues = subWidgets.flatMap(_.namedValue).toMap
      val valueType = typesCombo.selectedItem getOrElse noTypeSelectedError
      Some(ValueData(valueType, namedValues))
    }
  }
  
  def fieldValue_=(v: Option[Any]) { 
    v match {
      case Some(vd: ValueData) => subWidgets foreach { w => 
        w.fieldValue = vd.fields get w.field.name 
      }
      case None => subWidgets.foreach(_.fieldValue = None)
      case x => error("Expected a ValueData, got " + x.getClass.getName + ": " + x)
    }
  }

  private def noTypeSelectedError = error("Expected a value to be selected")*/
}