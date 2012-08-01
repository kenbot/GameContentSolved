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

@RunWith(classOf[JUnitRunner])
class CheckBoxWidgetSpec extends Spec with ShouldMatchers with WidgetSpecMethods {
  
  describe("For a Bool field: ") {
    val widget = new CheckBoxWidget('Foo -> BoolType)
    
    describe("Setting false") {
      it("should return false") {
        widget.fieldValue = Some(false)
        widget.fieldValue should equal (Some(false))
      }
    }
    describe("Setting true") {
      it("should return true") {
        widget.fieldValue = Some(true)
        widget.fieldValue should equal (Some(true))
      }
    }  
  }
}