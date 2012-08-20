package kenbot.gcsolved.resource
import org.scalatest._
import matchers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import Field._
import kenbot.gcsolved.resource.types.ValueType
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.StringType

@RunWith(classOf[JUnitRunner])
class ValueDataSpec extends Spec with ShouldMatchers {
  val rabbit = ValueType("Rabbit")
  val ghost = RefType("Ghost", 'id -> StringType ^ (isId = true))
  def makeData = ValueData(rabbit, 
    'weight -> 44, 
    'toes -> 3, 
    'colour -> "white",
    'hauntedBy -> ResourceRef("casper", ghost))
    
  val data = makeData


  describe("Creation") {
    it("should have the given resource type") {data.resourceType should equal (rabbit)}
    it("should have the given fields") {
      data("weight") should equal (44)
      data("toes") should equal (3)
      data("colour") should equal ("white")
    }
  }

  describe("Updating field values") {
    it("should result in the new value being set") {
      val newData = data.updateField("colour", "red")
      newData("colour") should equal ("red")
    }
  }
  
  describe("Equality") {
    it("should be equal to a resource with identical fields") {
      data should equal (makeData)
    }
    it("should not be equal to a resource with different fields") {
      data should not equal (data.updateField("colour", "blue"))
    }
  }
  
  describe("References to other resources") {
    it("should be in the external references list") {
      data.externalRefs should contain (ResourceRef("casper", ghost))
    }
    it("should result in \"refersTo\" returning true") {
      data refersTo ResourceRef("casper", ghost) should be (true)
    }
  }

  describe("Search matching") {
    it("should fail if nothing matches") {
      data matches "befuzzle" should be (false)
    }
    it("should succeed if the resource type contains the string") {
      data matches "rab" should be (true)
    }
    it("should succeed if any of the field values contain the string") {
      data matches "44" should be (true)
      data matches "asper" should be (true)
    }
  }
}
  