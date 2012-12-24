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