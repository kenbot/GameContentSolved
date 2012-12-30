package kenbot.gcsolved.editor.gui.typeselector


import scala.swing.event.SelectionChanged
import scala.swing.ComboBox
import scala.swing.Component
import scala.swing.FlowPanel
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication
import kenbot.gcsolved.core.Field.symbolAndType2Field
import kenbot.gcsolved.core.types.AnyRefType
import kenbot.gcsolved.core.types.AnyType
import kenbot.gcsolved.core.types.AnyValueType
import kenbot.gcsolved.core.types.BoolType
import kenbot.gcsolved.core.types.DoubleType
import kenbot.gcsolved.core.types.FileType
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.ListType
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.ResourceType
import kenbot.gcsolved.core.types.StringType
import kenbot.gcsolved.core.ResourceSchema
import kenbot.gcsolved.core.types.UserType



trait TypeRefinementWidget extends Component



class TypeSelectorWidget(schema: ResourceSchema, 
                         topType: ResourceType = AnyType, 
                         allowAbstract: Boolean = true) extends FlowPanel {
  self => 

  lazy val selectableTypes: List[TypeSelection] = (TypeSelection.systemTypesUnder(topType) ++ 
                         filterSelections(schema.userRefTypes) ++ 
                         filterSelections(schema.userValueTypes) ++ 
                         filterSelections(schema.selectOneTypes)).sortBy(_.name.toLowerCase)
  
  private def filterSelections(userTypes: Seq[ResourceType with UserType]): List[TypeSelection] = {
    userTypes.filter(_ <:< topType).map(UserTypeSelection.apply).toList
  }
  
  def resourceType: ResourceType = currentTypeSelection.getResourceType(refinementWidget)
  def resourceType_=(rt: ResourceType): Unit = selectorCombo.selection.item = TypeSelection.forResourceType(rt)

  def currentTypeSelection: TypeSelection = selectorCombo.selection.item
  
  val selectorCombo = new ComboBox(selectableTypes)
  var refinementWidget: Option[TypeRefinementWidget] = None


  listenTo(selectorCombo.selection)
  updateComponents()
  
  def selectedType: ResourceType = null
  def selectedType_=(rt: ResourceType) {}
  
  private def updateComponents() {
    contents.clear()
    contents += selectorCombo
    refinementWidget foreach contents.+=
    revalidate()
    repaint()
  }
 
  reactions += {
    case SelectionChanged(_) => 
      refinementWidget = currentTypeSelection.makeRefinementWidget(schema)
      updateComponents()
  }
}








