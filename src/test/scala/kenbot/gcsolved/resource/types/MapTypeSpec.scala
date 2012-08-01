package kenbot.gcsolved.resource.types
import org.scalatest._
import matchers._
import sys.error
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MapTypeSpec extends Spec with ShouldMatchers {
  
  describe("Map") {
    val mapType = MapType(StringType, ListType(IntType))
    
    it("should accept maps containing entirely appropriate keys and values") {
      mapType acceptsValue (Map("a" -> List(1,2,3), "b" -> List(6,7,8))) should be (true)
    }
    
    it("should not accept maps containing some or all invalid values") {
      mapType acceptsValue (Map("a" -> List(1,"rubbish",3), "b" -> List(6,7,8))) should be (false)
    }
    
    it("should not accept non-maps") {
      mapType acceptsValue (List("a", "b", "66")) should be (false)
    }
  }
}