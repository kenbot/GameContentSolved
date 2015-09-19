package kenbot.gcsolved.editor.gui.typeselector

import scala.swing.Component
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IntRefinementWidgetSpec extends FunSpec with ShouldMatchers {
  

  describe("Initial state") {
    val refinementWidget = new IntRefinementWidget
    
    it("should have no min selected") {
      refinementWidget.min should equal (None)
    }
    it("should have no max selected") {
      refinementWidget.min should equal (None)
    }
    shouldNotShowTextFields(refinementWidget)
  }
  
  describe("Setting min") {
    val refinementWidget = new IntRefinementWidget

    describe("to a number") {
      it("should set the min") {
        refinementWidget.min = Some(6)
        refinementWidget.min should equal (Some(6))
      }
    }

    describe("to none") {
      it("should clear the min value") {
        refinementWidget.min = None
        refinementWidget.min should equal (None)
      }
    }
    shouldShowTextFields(refinementWidget)
  }
  
  describe("Setting max") {
    val refinementWidget = new IntRefinementWidget
    describe("to a number") {
      it("should set the max") {
        refinementWidget.max = Some(10)
        refinementWidget.max should equal (Some(10))
      }
    }
    describe("to none") {
      it("should clear the max value") {
        refinementWidget.max = None
        refinementWidget.max should equal (None)
      }
    }
    shouldShowTextFields(refinementWidget)
  }
  
  private def shouldShowTextFields(refinementWidget: IntRefinementWidget) {
    it("should be showing the text fields") {
      refinementWidget.showTextFields should be (true)
    }
  }
  
  private def shouldNotShowTextFields(refinementWidget: IntRefinementWidget) {
    it("should not be showing the text fields") {
      refinementWidget.showTextFields should be (false)
    }
  }
  
}
