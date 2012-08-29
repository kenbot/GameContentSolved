package kenbot.gcsolved.editor.gui
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.StringType
import kenbot.gcsolved.resource.RefData
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.Field._
import kenbot.gcsolved.resource.ResourceLibrary
import kenbot.gcsolved.resource.ResourceSchema
import kenbot.gcsolved.resource.ResourceRef

@RunWith(classOf[JUnitRunner])  
class ListAndEditItemSpec extends Spec with ShouldMatchers {
  
  val bananaType = RefType("Banana", 'foo -> StringType ^ (isId = true), 'noo -> IntType)
  val schema = ResourceSchema().addRefTypes(bananaType)
  
  val booLibrary = ResourceLibrary("boo", schema).
    addResource(RefData(bananaType, "foo" -> "flib"))
    
  val mooLibrary = ResourceLibrary("moo", schema).
    addResource(RefData(bananaType, "foo" -> "blah")).
    addResource(RefData(bananaType, "foo" -> "flob"))
  
  val refData1 = mooLibrary.findResource(ResourceRef("blah", bananaType)).get
  val refData2 = mooLibrary.findResource(ResourceRef("flob", bananaType)).get
  val externalData = booLibrary.findResource(ResourceRef("flib", bananaType)).get
  val noLibraryData = RefData(bananaType, "foo" -> "hhh")
  val itemWithDifferent = ListAndEditItem(refData1, Some(refData2), "moo")
  val itemWithSame = ListAndEditItem(refData1, Some(refData1), "moo")
  val itemWithMissing = ListAndEditItem(refData1, None, "moo")
  
  describe("Modified status") {
    it ("should be true if the original item is different from the current") {
      itemWithDifferent should be ('isModified)
    }
    
    it ("should be false if the original item is the same as the current") {
      itemWithSame should not be ('isModified)
    }
    
    it ("should be false if the original item is missing") {
      itemWithMissing should not be ('isModified)
    }
  }
  
  describe("External status") {
    it ("should be false if defined in the local library") {
      ListAndEditItem(refData1, Some(refData1), "moo").isExternal should be (false)
    }
    
    it ("should be true if defined in a different library") {
      val itemWithExternal = ListAndEditItem(externalData, Some(externalData), "tttt")
      itemWithExternal should be ('isExternal)
    }
    
    it ("should be false if not defined in a library") {
      val itemWithExternal = ListAndEditItem(noLibraryData, Some(noLibraryData), "tttt")
      itemWithExternal should not be ('isExternal)
    }
  }
  
  describe("New status") {
    it ("should be false if the original item exists") {
      itemWithSame should not be ('isNew)
    }
    
    it ("should be true if the original item is missing") {
      itemWithMissing should be ('isNew)
    }
  }
  
  describe("Displayed text") {
    it("should start with a * if modified") {
      itemWithDifferent.toString should include ("*")
    }
    
    it("should not start with a * if not modified") {
      itemWithSame.toString should not include ("*")
    }
    
    it("should end in \"(new)\" if there is no original data") {
      itemWithMissing.toString should include ("(new)")
    }
  }
  
  describe("Updating current item from library") {
    val dataInLibrary = mooLibrary.findResource(refData1.ref).get
    val currentData = refData1.updateField("noo", 444)
    val item = ListAndEditItem(currentData, None, "moo")
    
    it("should replace the current item with the one from the library") {
      item.updateCurrentFromLibrary(mooLibrary)
      item.current should not equal (currentData)
      item.current should equal (dataInLibrary)
    }
  }
  
  describe("Updating library from current item") {
    val dataInLibrary = mooLibrary.findResource(refData1.ref).get
    val currentData = refData1.updateField("noo", 444)
    val item = ListAndEditItem(currentData, None, "moo")
    
    it("should replace the one from the library with the current item") {
      val updatedLib = item.updateLibraryFromCurrent(mooLibrary)
      
      updatedLib.findResource(currentData.ref) should not equal (Some(dataInLibrary))
      updatedLib.findResource(currentData.ref) should equal (Some(currentData))
    }
    
    
  }
}
