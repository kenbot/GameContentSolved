package kenbot.gcsolved.resource
import scala.sys.error
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers._
import org.scalatest._
import Field._
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.AnyRefType
import kenbot.gcsolved.resource.types.StringType

@RunWith(classOf[JUnitRunner])
class RefDataSpec extends Spec with ShouldMatchers {
  val rabbit = RefType("Rabbit", 'name -> StringType ^ (isId = true))
  def makeData = RefData(rabbit, 
    'name -> "Roger",
    'weight -> 44, 
    'toes -> 3, 
    'colour -> "white",
    'otherRabbit -> ResourceRef("bob", rabbit))
    
  val data = makeData


  describe("Creation") {
    it("should have the given id") {data.id should equal ("Roger")}
    it("should have the given resource type") {data.resourceType should equal (rabbit)}
    it("should have the given fields") {
      data[Any]("weight") should equal (44)
      data[Any]("toes") should equal (3)
      data[Any]("colour") should equal ("white")
    }
    it("should throw an exception if the resource type is abstract") {
     val abstractType = RefType("AbstractType", AnyRefType, true)
     evaluating { RefData(abstractType) } should produce [IllegalArgumentException]
    }
  }

  describe("Updating field values") {
    it("should result in the new value being set") {
      val newData = data.updateField("colour", "red")
      newData[Any]("colour") should equal ("red")
    }
  }
  
  describe("Equality") {
    it("should be equal to a resource with identical fields") {
      data should equal (makeData)
    }
    it("should not be equal to a resource with a different version") {
      data should not equal (data.incrementVersion)
    }
    it("should not be equal to a resource with a different id") {
      data should not equal (data updateId "baz")
    }
    it("should not be equal to a resource with different fields") {
      data should not equal (data.updateField("colour", "blue"))
    }
  }
  
  describe("References to other resources") {
    it("should be in the external references list") {
      data.externalRefs should contain (ResourceRef("bob", rabbit))
    }
    it("should result in \"refersTo\" returning true") {
      data refersTo ResourceRef("bob",rabbit) should be (true)
    }
  }

  describe("Search matching") {
    it("should fail if nothing matches") {
      data matches "befuzzle" should be (false)
    }
    it("should succeed if the id contains the string") {
      data matches "ger" should be (true)
    }
    it("should succeed if the resource type contains the string") {
      data matches "rab" should be (true)
    }
    it("should succeed if any of the field values contain the string") {
      data matches "44" should be (true)
      data matches "bo" should be (true)
    }
  }

  describe("Incrementing the version") {
    it ("should result in a resource with a version number incremented by 1") {
      data.incrementVersion().version should equal (1)
    }
  }
}
  