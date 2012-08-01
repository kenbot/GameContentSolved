package kenbot.gcsolved.editor.gui.typeselector

import scala.swing.event.EditDone
import scala.swing.FlowPanel
import scala.swing.TextField
import scala.swing.Button
import scala.swing.Label
import scala.swing.SimpleSwingApplication
import scala.swing.MainFrame
import java.awt.Dimension
import scala.swing.Swing.{pair2Dimension, onEDT}
import scala.swing.event.KeyPressed
import kenbot.gcsolved.resource.ResourceSchema
import kenbot.gcsolved.resource.types.ResourceType


class ListRefinementWidget(schema: ResourceSchema) extends FlowPanel with TypeRefinementWidget {
  
  private val buttonSize: Dimension = (30, 20)
  
  def elementType: ResourceType = typeSelector.resourceType
  def elementType_=(t: ResourceType) { typeSelector.resourceType = t }
  
  val typeSelector = new TypeSelectorWidget(schema)

  contents += new Label("of")
  contents += typeSelector
}