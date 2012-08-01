package kenbot.gcsolved.resource.types
import org.scalatest._
import matchers._
import sys.error
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DoubleTypeSpec extends Spec with ShouldMatchers {
  
  describe("Double") {
    it("should accept doubles") {
      DoubleType acceptsValue (3.5) should be (true)
    }
    it("should not accept non-doubles") {
      DoubleType acceptsValue ("fred") should be (false)
    }
  }

}