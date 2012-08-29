package kenbot.gcsolved.editor.gui.widgets

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.resource.Field.symbolAndType2Field
import kenbot.gcsolved.resource.Field.symbolAndValue2namePair
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.types.ValueType
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.ValueData
import kenbot.gcsolved.resource.types.ResourceType
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