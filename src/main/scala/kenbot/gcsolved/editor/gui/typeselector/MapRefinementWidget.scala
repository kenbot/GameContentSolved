package kenbot.gcsolved.editor.gui.typeselector
import java.awt.Dimension
import kenbot.gcsolved.core.ResourceSchema
import scala.swing.FlowPanel
import scala.swing.Swing.pair2Dimension
import scala.swing.Label
import kenbot.gcsolved.core.types.ResourceType
import kenbot.gcsolved.core.types.SelectOneType

class MapRefinementWidget(schema: ResourceSchema) extends FlowPanel with TypeRefinementWidget {
  private val buttonSize: Dimension = (30, 20)
  
  def keyType: SelectOneType = keySelector.resourceType.asInstanceOf[SelectOneType]
  def keyType_=(t: SelectOneType) { keySelector.resourceType = t }
  
  def valueType: ResourceType = valueSelector.resourceType
  def valueType_=(t: ResourceType) { valueSelector.resourceType = t }
  
  private val keySelector = new TypeSelectorWidget(schema)
  private val valueSelector = new TypeSelectorWidget(schema)

  contents += new Label("of")
  contents += keySelector
  contents += new Label("\u2192") // -> Right arrow
  contents += valueSelector
}