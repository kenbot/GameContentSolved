package kenbot.gcsolved.core.types
import org.scalatest._
import sys.error
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class DoubleTypeSpec extends FunSpec with ShouldMatchers {
  
  describe("Double") {
    it("should accept doubles") {
      DoubleType acceptsValue (3.5) should be (true)
    }
    it("should not accept non-doubles") {
      DoubleType acceptsValue ("fred") should be (false)
    }
  }

}
