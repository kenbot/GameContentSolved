package kenbot.gcsolved.editor.gui
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.StringType
import kenbot.gcsolved.core.RefData
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.Field
import kenbot.gcsolved.core.Field._
import kenbot.gcsolved.core.ResourceLibrary
import kenbot.gcsolved.core.ResourceSchema
import kenbot.gcsolved.core.ResourceRef
import kenbot.gcsolved.editor.gui.widgets.TextFieldWidget

@RunWith(classOf[JUnitRunner])  
class WidgetEditScreenSpec extends Spec with ShouldMatchers {
  
  lazy val bananaType = RefType("Banana", 'name -> StringType ^ (isId = true))
  
  def editScreen = new WidgetEditScreen(bananaType, Seq(bananaType.emptyData), new TextFieldWidget(_))
  
  describe("Creating") {
    it("should create widgets for all the fields") {
      editScreen.fieldWidgets.size should equal (bananaType.fields.size)
    }
  }
}
