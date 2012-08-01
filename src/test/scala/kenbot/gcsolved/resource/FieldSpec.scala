package kenbot.gcsolved.resource
import org.scalatest._
import matchers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.types.StringType

@RunWith(classOf[JUnitRunner])  
class FieldSpec extends Spec with ShouldMatchers {
  val field = Field("foo", IntType, "category", false, false, Some(5), "a cool field")
  val requiredField = Field("fooReq", IntType, required=true)
  val wordyField = Field("maryHadALittleLamb", IntType)
  
  describe("equality") {
    it("should equal a field with equivalent values") {
      field should equal (Field("foo", IntType, "category", false, false, Some(5), "a cool field"))
    }
    
    it("should not equal a field with different values") {
      field should not equal (Field("foozle", StringType, "categodry", false, false, Some("blah"), "monkey"))
    }
  }
  
  describe("human readable name") {
    it("should insert spaces before capital letters") {
      wordyField.humanReadableName should equal ("Mary Had A Little Lamb")
    }
  }
  
  describe("ID fields") {
    it("should be automatically required") {
      field ^ (isId = true) should be ('required)
    }
  }
  
  describe("validation") {
    it("should accept values that the type accepts") {
      field acceptsValue Some("44") should be (true)
    }
    it("should reject values that the type rejects") {
      field acceptsValue Some("abc") should be (false)
    }
    it("should accept a blank value if not required") {
      field acceptsValue None should be (true)
    }
    it("should reject a blank value if required") {
      requiredField acceptsValue None should be (false)
    }
  }
}
  