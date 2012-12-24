package kenbot.gcsolved.editor.gui.widgets

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.core.Field.symbolAndType2Field
import kenbot.gcsolved.core.Field.symbolAndValue2namePair
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.ValueType
import kenbot.gcsolved.core.Field
import kenbot.gcsolved.core.ValueData
import org.scalatest.junit.JUnitRunner
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.AnyRefType
import kenbot.gcsolved.core.RefData
import kenbot.gcsolved.core.types.ListType
import kenbot.gcsolved.editor.gui.TestSettings

@RunWith(classOf[JUnitRunner])
class DynamicListWidgetSpec extends Spec with ShouldMatchers with WidgetSpecMethods {
  
  def makeWidget(f: Field): FieldWidget = new TextFieldWidget(f)
  val listType = ListType(IntType)
  val field: Field = 'Foo -> listType
  
  
  describe("For a list type") {
    checkSettingValues(new DynamicListWidget(field, makeWidget), List(1,2,3))
    checkValidation(f => new DynamicListWidget(f, makeWidget), field, List("aaa", "bbb", "ccc"), List(5,6,7))
  }
  
  checkFocusing {
    val nestedType = ListType(ListType(IntType))
    val parentWidget = TestSettings.makeWidget('parent -> nestedType)
    parentWidget.fieldValue = Some(List(List(1,2,3), List(4,5), List(9,9,9)))
    parentWidget
  }
}