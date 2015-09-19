package kenbot.gcsolved.editor.widgets

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

import kenbot.gcsolved.core.Field.symbolAndType2Field
import kenbot.gcsolved.core.types.BoolType

@RunWith(classOf[JUnitRunner])
class CheckBoxWidgetSpec extends FunSpec with ShouldMatchers with WidgetSpecMethods {
  
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
