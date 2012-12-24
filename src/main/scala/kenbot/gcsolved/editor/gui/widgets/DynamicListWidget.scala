package kenbot.gcsolved.editor.gui.widgets
import scala.swing.Component
import scala.swing.ListView
import kenbot.gcsolved.editor.gui.util.DynamicSideListPanel
import kenbot.gcsolved.core.types.ListType
import kenbot.gcsolved.core.types.ResourceType
import kenbot.gcsolved.core.Field
import scala.swing.Label
import java.awt.Color
import scala.swing.Alignment


class DynamicListWidget(theField: Field, 
    makeWidget: Field => FieldWidget, 
    parentWidget:  => Option[FieldWidget] = None, 
    level: Int = 0) extends DefaultFieldWidget(theField, parentWidget, level) {
  
  top => 
  
  private def initialPanels = Seq[Component with WidgetEditor]() 
  private def getPanelLabel(p: Component with WidgetEditor) = p.widget.fieldValue.getOrElse("").toString
  
  type ListPanel = Component with WidgetEditor
  
  private implicit object listDescription extends DynamicSideListPanel.ItemDescription[ListPanel] {
    override def newPanel = registerNewElementWidget().editor
    override def getLabel(panel: ListPanel) = getPanelLabel(panel)
    override def getForeground(panel: ListPanel) = Color.black 
    override def getBackground(panel: ListPanel) = {
      val selected = editor.selectedItems contains panel
      FormColors.getColor(selected, panel.widget.valid)
    }
  }
  
  lazy val editor = new DynamicSideListPanel(initialPanels) with MyEditorMixin {
    top listenTo this
  }
  
  reactions += {
    case ValidationEvent(widget, success) if widget ne this => validateAndUpdate()
    case DynamicSideListPanel.ComponentAdded(`editor`, added: WidgetEditor) => top listenTo added.widget
    case DynamicSideListPanel.ComponentSelected(`editor`, selected: WidgetEditor) => selected.widget.hasFocus = true
    case DynamicSideListPanel.ComponentRemoved(`editor`, removed: WidgetEditor) => top deafTo removed.widget
    //case WidgetFocusEvent(widget) if widget ne top => publish(WidgetFocusEvent(top))
  }

  //override def hasFocus: Boolean = editor.hasFocus || subWidgets.exists(_.hasFocus)
  

  private def registerNewElementWidget() = {
    val elementField: Field = field.name + "[]" -> field.fieldType.asInstanceOf[ListType].elementType
    val elementWidget = makeWidget(elementField)
    listenTo(elementWidget)
    elementWidget
  }
  
  override def subWidgets: Seq[FieldWidget] = editor.userPanels.map(_.widget) 
  
  
  
  def rawFieldValue: Option[Any] = {
    val listValues = subWidgets.map(_.fieldValue).flatten.toList
    if (listValues.nonEmpty) Some(listValues) else None
  } 
  
  def fieldValue_=(v: Option[Any]): Unit = { 
    deafTo(subWidgets: _*)
    v match {
      case Some(list: List[Any]) => 
        val editors = list.map { value => 
          val elementWidget = registerNewElementWidget()
          elementWidget.fieldValue = Some(value)
          elementWidget.editor
        }
        editor.userPanels = editors
        
      case _ => editor.clear()
    }
    editor.revalidate()
    editor.repaint()
  }
}