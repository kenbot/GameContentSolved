package kenbot.gcsolved.editor.screens
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.StringType
import kenbot.gcsolved.core.Field._
import kenbot.gcsolved.editor.gui.WidgetEditScreen
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import kenbot.gcsolved.editor.widgets.TextFieldWidget

@RunWith(classOf[JUnitRunner])  
class WidgetEditScreenSpec extends Spec with ShouldMatchers {
  
  lazy val bananaType = RefType("Banana") defines ('name -> StringType ^ (isId = true))
  
  def editScreen = new WidgetEditScreen(bananaType, Seq(bananaType.emptyData), new TextFieldWidget(_))
  
  describe("Creating") {
    it("should create widgets for all the fields") {
      editScreen.fieldWidgets.size should equal (bananaType.fields.size)
    }
  }
}
