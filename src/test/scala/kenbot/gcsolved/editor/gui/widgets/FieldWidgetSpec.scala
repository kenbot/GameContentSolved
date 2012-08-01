package kenbot.gcsolved.editor.gui.widgets

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.resource.Field.symbolAndType2Field
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.types.StringType
import org.scalatest.junit.JUnitRunner
import kenbot.gcsolved.resource.types.BoolType
import kenbot.gcsolved.resource.types.DoubleType
import kenbot.gcsolved.resource.Field

@RunWith(classOf[JUnitRunner])
class FieldWidgetSpec extends Spec with ShouldMatchers with WidgetSpecMethods {
  
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