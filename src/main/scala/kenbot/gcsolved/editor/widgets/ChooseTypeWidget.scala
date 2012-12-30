package kenbot.gcsolved.editor.widgets
import scala.swing.event.{SelectionChanged, FocusGained, FocusLost}
import scala.swing.ComboBox
import scala.swing.Component
import scala.swing.GridPanel
import scala.swing.Label
import kenbot.gcsolved.editor.gui.util.FilteringComboBox
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.editor.gui.util.SuppressableEvents
import kenbot.gcsolved.editor.gui.WidgetEditScreenPanel
import kenbot.gcsolved.core.types.ObjectType
import kenbot.gcsolved.core.types.ResourceType
import kenbot.gcsolved.core.types.ValueType
import kenbot.gcsolved.core.Field
import kenbot.gcsolved.core.ResourceSchema
import kenbot.gcsolved.core.ValueData
import scala.swing.FlowPanel
import scala.swing.TextField
import scala.sys.error

class ChooseTypeWidget(theField: Field, 
    schema: ResourceSchema, 
    makeWidget: Field => FieldWidget, 
    parentWidget:  => Option[FieldWidget] = None, 
    level: Int = 0) extends DefaultFieldWidget(theField, parentWidget, level) with SuppressableEvents {
  
  top => 
  
 
  lazy val fieldType: ValueType = theField.fieldType.asInstanceOf[ValueType]
  lazy val concreteSubTypes = schema.valueTypes.filter(t => !t.isAbstract && t <:< fieldType).toList
  def makeWidgets(forSubType: ObjectType): List[FieldWidget] = forSubType.fields.values.toList map makeWidget
  
  private lazy val centerContent = new GridPanel(1,1)
  private lazy val typesCombo = new FilteringComboBox(concreteSubTypes, false)(_.name)
  private var currentFieldWidgets: List[FieldWidget] = Nil
  
  lazy val editor = new NestedBorderPanel with MyEditorMixin {
    north = typesCombo
    center = centerContent
    
    top listenTo typesCombo.selection
    
    //listenTo(typesCombo)
    //reactions += {
    //  case FocusGained(`typesCombo`, other, temp) => publish(FocusGained(this, other, temp)) 
    //  case FocusLost(`typesCombo`, other, temp) => publish(FocusLost(this, other, temp)) 
    //}
    
    typesCombo.selectedItem foreach updateForType
  } 
  
  
  def updateForType(t: ObjectType) {
    centerContent.contents.clear()
    deafTo(currentFieldWidgets: _*)
    
    val typePanel = new WidgetEditScreenPanel(makeWidgets(t))
    
    currentFieldWidgets = typePanel.widgets.toList
    listenTo(currentFieldWidgets: _*)
    
    centerContent.contents += typePanel
  }

  reactions += {
    case _ if shouldSuppressEvents =>

    case ValidationEvent(widget, success) if widget ne this => validateAndUpdate()
    
    case SelectionChanged(`typesCombo`) => 
      val selectedType = typesCombo.selectedItem getOrElse noTypeSelectedError
      updateForType(selectedType)
      validateAndUpdate()
      editor.revalidate()
      editor.repaint()
  }

  override def subWidgets: Seq[FieldWidget] = currentFieldWidgets
  
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

  private def noTypeSelectedError = error("Expected a value to be selected")
}
