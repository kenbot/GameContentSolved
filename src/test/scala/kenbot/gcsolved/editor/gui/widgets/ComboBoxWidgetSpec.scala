package kenbot.gcsolved.editor.gui.widgets

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec

import kenbot.gcsolved.resource.Field.symbolAndType2Field
import kenbot.gcsolved.resource.Field.symbolAndValue2namePair
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.SelectOneType
import kenbot.gcsolved.resource.types.StringType
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.RefData

@RunWith(classOf[JUnitRunner])  
class ComboBoxWidgetSpec extends Spec with ShouldMatchers with WidgetSpecMethods {
  
  val PotatoType = RefType("Potato", 'Name -> StringType ^ (isId = true))
  val colorList = List("red", "green", "blue", "purple")
  val ColorsType = SelectOneType("Colors", StringType, colorList: _*)
  
  describe(PotatoType) { 
    val p1 = RefData(PotatoType, 'Name -> "a")
    val p2 = RefData(PotatoType, 'Name -> "b")
    val p3 = RefData(PotatoType, 'Name -> "c")
    
    val field: Field = 'Foo -> PotatoType
    val refList = List(p1.ref, p2.ref, p3.ref)
    val comboBoxWidget = new ComboBoxWidget(field, refList)
    checkSettingValues(new ComboBoxWidget(field, refList), p2.ref)
  }
  
  describe(ColorsType) {
    val field: Field = 'Foo -> ColorsType
    checkSettingValues(new ComboBoxWidget(field, colorList), "purple")
  }
 
}