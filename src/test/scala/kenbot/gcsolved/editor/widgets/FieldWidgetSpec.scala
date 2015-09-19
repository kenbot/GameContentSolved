package kenbot.gcsolved.editor.widgets

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import kenbot.gcsolved.core.Field.symbolAndType2Field
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.StringType
import org.scalatest.junit.JUnitRunner
import kenbot.gcsolved.core.types.BoolType
import kenbot.gcsolved.core.types.DoubleType
import kenbot.gcsolved.core.Field

@RunWith(classOf[JUnitRunner])
class FieldWidgetSpec extends FunSpec with ShouldMatchers with WidgetSpecMethods {
  
  def makeWidget(field: Field, parentWidget: Option[FieldWidget]) = 
      new TextFieldWidget(field, parentWidget = parentWidget)
  
  describe("Detecting parents to root") {
    it("should contain all the parents in order, including the root") {
      val field: Field = 'foo1 -> StringType
      val widget1 = makeWidget(field, None)
      val widget2 = makeWidget(field, Some(widget1))
      val widget3 = makeWidget(field, Some(widget2))
      widget3.parentsToRoot should equal (widget2 :: widget1 :: Nil)
    }
  }
}
