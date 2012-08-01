package kenbot.gcsolved.editor.gui.typeselector


import scala.swing.event.SelectionChanged
import scala.swing.ComboBox
import scala.swing.Component
import scala.swing.FlowPanel
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication
import kenbot.gcsolved.resource.Field.symbolAndType2Field
import kenbot.gcsolved.resource.types.AnyRefType
import kenbot.gcsolved.resource.types.AnyType
import kenbot.gcsolved.resource.types.AnyValueType
import kenbot.gcsolved.resource.types.BoolType
import kenbot.gcsolved.resource.types.DoubleType
import kenbot.gcsolved.resource.types.FileType
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.types.ListType
import kenbot.gcsolved.resource.types.MapType
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.ResourceType
import kenbot.gcsolved.resource.types.StringType
import kenbot.gcsolved.resource.ResourceSchema
import kenbot.gcsolved.resource.types.UserType



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
    refinementWidget.foreach(contents +=) 
    revalidate()
    repaint()
  }
 
  reactions += {
    case SelectionChanged(_) => 
      refinementWidget = currentTypeSelection.makeRefinementWidget(schema)
      updateComponents()
  }
}








