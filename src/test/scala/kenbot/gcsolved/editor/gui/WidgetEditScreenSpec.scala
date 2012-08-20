package kenbot.gcsolved.editor.gui
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.StringType
import kenbot.gcsolved.resource.RefData
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.Field._
import kenbot.gcsolved.resource.ResourceLibrary
import kenbot.gcsolved.resource.ResourceSchema
import kenbot.gcsolved.resource.ResourceRef
import kenbot.gcsolved.editor.gui.widgets.TextFieldWidget

@RunWith(classOf[JUnitRunner])  
class WidgetEditScreenSpec extends Spec with ShouldMatchers {
  
  val bananaType = RefType("Banana", 'foo -> StringType ^ (isId = true), 'noo -> IntType)
  val schema = ResourceSchema().addRefTypes(bananaType)
  val data = RefData(bananaType, "foo" -> "flib")
    
  
  val editScreen = new WidgetEditScreen(bananaType, Seq(data), new TextFieldWidget(_))
  
  describe("Creating") {
    it("should create widgets for all the fields") {
      editScreen.fieldWidgets.size should equal(bananaType.fields.size)
    }
  }

}