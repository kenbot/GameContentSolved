package kenbot.gcsolved.editor.widgets
import scala.swing.Frame
import scala.swing.MainFrame
import org.scalatest.matchers.ShouldMatchers
import org.scalatest._
import kenbot.gcsolved.core.types.ResourceType
import kenbot.gcsolved.core.Field
import scala.swing.Swing
import kenbot.gcsolved.core.types.StringType

trait WidgetSpecMethods {
  this: FunSpec with ShouldMatchers =>
 
    
  def describe(fieldType: ResourceType)(thunk: => Unit) {
    describe("For a " + fieldType.name + " field")(thunk)
  }  
    
  def checkSettingEditableAlsoAppliesToSubWidgets(fieldWidget: FieldWidget) {
    describe("Setting the editable state") {
      it("should apply to all the subwidgets") {
        assume(fieldWidget.subWidgets.nonEmpty, "This test is meaningless if there are no subwidgets")  
        
        fieldWidget.editable = false
        fieldWidget.subWidgets.forall(_.editable) should be (false)
          
        fieldWidget.editable = true
        fieldWidget.subWidgets.forall(_.editable) should be (true)
      }
    }
    
  }
  
  def checkValidation(makeWidget: Field => FieldWidget, field: Field, rubbishValue: Any, validValue: Any) {
    describe("Validation") {
      it("should fail when a rubbish value is selected") {
        val widget = makeWidget(field)
        widget.fieldValue = Some(rubbishValue)
        widget.validateAndUpdate()
        
        widget.valid should be (false)
      }
      
      it("should succeed when a valid value is selected") {
        val widget = makeWidget(field)
        widget.fieldValue = Some(validValue)
        widget.validateAndUpdate()
        
        widget.valid should be (true)
      }
      
      it("should succeed when None is selected and the field is not required") {
        val widget = makeWidget(field ^ (required=false))
        widget.fieldValue = None
        widget.validateAndUpdate()
        
        widget.valid should be (true)
      }
      
      it("should fail when None is selected and the field is required") {
        val widget = makeWidget(field ^ (required=true))
        widget.fieldValue = None
        widget.validateAndUpdate()
        
        widget.valid should be (false)
      }
    }
  }

  def checkSettingValues[A](widget: FieldWidget, valueToSet: A) {
    
    describe("Setting an empty value") {
      it("should return an empty value") {
        widget.fieldValue = Some(valueToSet)
        widget.fieldValue = None
        val value = widget.fieldValue
        value should equal (None)
      }
    }
    describe("Setting a value") {
      it("should return the same value") {
        widget.fieldValue = Some(valueToSet)
        widget.fieldValue should equal (Some(valueToSet))
      }
    }
  }
  
  
  def checkFocusing(parentWithChildrenAndGrandChildren: FieldWidget) {
    describe("Focusing on a widget") {
      val parent = parentWithChildrenAndGrandChildren
      val widget = parent.subWidgets(0)
      val children = widget.subWidgets
      
      assume(children.size > 1)
      
      it("should leave it focused") {
        widget.hasFocus = true
        widget.hasFocus should be (true)
      }
      
      it("should also give the parent focus") {
        widget.hasFocus = true
        parent.hasFocus should be (true)
      }
      
      it("should make any siblings unfocused") {
        children(1).hasFocus = true
        children(0).hasFocus = true
        children(1).hasFocus should be (false)
      }
      
      it("should leave the first child focused") {
        widget.hasFocus = true
        children(0).hasFocus should be (true)
        children(1).hasFocus should be (false)
      }
    }
  }
}
