package kenbot.gcsolved.editor.gui.widgets

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.core.Field.symbolAndType2Field
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.StringType
import org.scalatest.junit.JUnitRunner
import kenbot.gcsolved.core.types.BoolType
import kenbot.gcsolved.core.types.DoubleType
import kenbot.gcsolved.core.Field

@RunWith(classOf[JUnitRunner])
class TextWidgetSpec extends Spec with ShouldMatchers with WidgetSpecMethods {
  
  def createTextFieldWidget(f: Field) = new TextFieldWidget(f) 
  
  describe(IntType) {
    val field: Field = 'Foo -> IntType
    checkSettingValues(createTextFieldWidget(field), 66)
    checkValidation(createTextFieldWidget, field, "asdf", 234)
  }
  
  describe(StringType) {
    val field: Field = 'Foo -> StringType
    checkSettingValues(createTextFieldWidget(field), "bah")
  }
  
  describe(BoolType) {
    val field: Field = 'Foo -> BoolType
    checkSettingValues(createTextFieldWidget(field), true)
    checkValidation(createTextFieldWidget, field, "asdf", true)
  }
  
  describe(DoubleType) {
    val field: Field = 'Foo -> DoubleType
    checkSettingValues(createTextFieldWidget(field), 6.056)
    checkValidation(createTextFieldWidget, field, "asdf", 5.5)
  }
}