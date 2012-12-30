package kenbot.gcsolved.editor.widgets

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.core.Field.symbolAndType2Field
import kenbot.gcsolved.core.Field.symbolAndValue2namePair
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.ValueType
import kenbot.gcsolved.core.Field
import kenbot.gcsolved.core.ValueData
import kenbot.gcsolved.core.types.ResourceType
import kenbot.gcsolved.editor.gui.TestSettings

@RunWith(classOf[JUnitRunner])
class EditScreenWidgetSpec extends Spec with ShouldMatchers with WidgetSpecMethods {
  
  val EditScreenType = ValueType("EditScreenType", 'a -> IntType, 'b -> IntType)
  val NestedType = ValueType("Nested", 'nested -> EditScreenType)
                                
  val makeWidget = TestSettings.makeWidget
  
  lazy val editScreenWidget: EditScreenWidget = makeWidget('Foo -> EditScreenType).asInstanceOf[EditScreenWidget]
  
  
  def mkData(a: Int, b: Int) = ValueData(EditScreenType, 'a -> a, 'b -> b)
  
  describe(EditScreenType) {
    checkSettingValues(editScreenWidget, mkData(5,6))
  }
  
  checkSettingEditableAlsoAppliesToSubWidgets(editScreenWidget)
  
  checkFocusing(makeWidget('parent -> NestedType))
}