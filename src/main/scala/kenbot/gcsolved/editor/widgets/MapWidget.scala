package kenbot.gcsolved.editor.widgets
import scala.swing.Component
import scala.swing.ListView
import kenbot.gcsolved.editor.gui.util.DynamicSideListPanel
import kenbot.gcsolved.core.types.ListType
import kenbot.gcsolved.core.types.ResourceType
import kenbot.gcsolved.core.Field
import scala.swing.Label
import java.awt.Color
import scala.swing.Alignment
import kenbot.gcsolved.editor.gui.util.DynamicListPanel
import scala.swing.TextField
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel
import kenbot.gcsolved.editor.gui.util.DynamicMapPanel
import kenbot.gcsolved.core.types.StringType
import kenbot.gcsolved.core.types.IntType


class MapWidget(theField: Field, 
    makeWidget: Field => FieldWidget, 
    parentWidget:  => Option[FieldWidget] = None, 
    level: Int = 0) extends DefaultFieldWidget(theField, parentWidget, level) {
  
  top => 

  //val mapType = field.fieldType.asInstanceOf[MapType]
  val keyType = StringType
  val valueType = IntType
  type Key = keyType.Value
  type Value = valueType.Value
  
  private implicit object MapPanelSupport extends DynamicMapPanel.Support[Key, Value] {
    type KeyComponent = Component with WidgetEditor
    type ValueComponent = Component with WidgetEditor
    
    def newKeyComponent(): KeyComponent = makeWidget("Key" -> keyType).editor
    def newValueComponent(): ValueComponent = makeWidget("Value" -> valueType).editor
    def getKey(kc: KeyComponent) = kc.widget.fieldValue map keyType.asValue
    def getValue(vc: ValueComponent) = vc.widget.fieldValue map valueType.asValue
    def makeKeyComponent(key: Key) = {
      val widget = makeWidget("Key" -> keyType)
      widget.fieldValue = Some(key)
      widget.editor
    }
    def makeValueComponent(value: Value) = {
      val widget = makeWidget("Value" -> valueType)
      widget.fieldValue = Some(value)
      widget.editor
    }
  }
  
  lazy val editor = new DynamicMapPanel[Key, Value] with MyEditorMixin {
    top listenTo this
  }
  
  override def subWidgets: Seq[FieldWidget] = 
    editor.keyPanels.map(_.asInstanceOf[WidgetEditor].widget) ++ editor.valuePanels.map(_.asInstanceOf[WidgetEditor].widget)
  
  reactions += {
    case ValidationEvent(widget, success) if widget ne this => validateAndUpdate()
  }
  
  def rawFieldValue: Option[Any] = {
    val mapValue = editor.values 
    if (mapValue.nonEmpty) Some(mapValue) else None
  }
  
  def fieldValue_=(v: Option[Any]) {
    editor.values = (v getOrElse Map()).asInstanceOf[Map[Key, Value]]
  }
}