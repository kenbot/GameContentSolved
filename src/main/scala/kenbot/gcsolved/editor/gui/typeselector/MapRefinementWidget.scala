package kenbot.gcsolved.editor.gui.typeselector
import java.awt.Dimension
import kenbot.gcsolved.resource.ResourceSchema
import scala.swing.FlowPanel
import scala.swing.Swing.pair2Dimension
import scala.swing.Label
import kenbot.gcsolved.resource.types.ResourceType

class MapRefinementWidget(schema: ResourceSchema) extends FlowPanel with TypeRefinementWidget {
  private val buttonSize: Dimension = (30, 20)
  
  def keyType: ResourceType = keySelector.resourceType
  def keyType_=(t: ResourceType) { keySelector.resourceType = t }
  
  def valueType: ResourceType = valueSelector.resourceType
  def valueType_=(t: ResourceType) { valueSelector.resourceType = t }
  
  private val keySelector = new TypeSelectorWidget(schema)
  private val valueSelector = new TypeSelectorWidget(schema)

  contents += new Label("of")
  contents += keySelector
  contents += new Label("\u2192") // -> Right arrow
  contents += valueSelector
}