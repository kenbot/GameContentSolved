package kenbot.gcsolved.core
import scala.sys.error
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import kenbot.gcsolved.core.types._
import Field._
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class LibraryEditSessionSpec extends Spec with ShouldMatchers {
  val gumbyType = RefType("Gumby", 'foo -> StringType ^ (isId = true), 'bar -> IntType)
  val schema = ResourceSchema() addRefTypes gumbyType
  val gumby = RefData(gumbyType, 'foo -> "zzz", 'bar -> 44) 
  val library = ResourceLibrary("lib", schema) addResource gumby
  val context = LibraryEditSession(library, Seq(gumby))
  
  describe("Creating") {
    it("should be its own original context") {
      context.originalContext should equal (context) 
    } 
    it("should not be able to undo") {
      context.canUndo should be (false)
    }
    it("should not be able to redo") {
      context.canRedo should be (false)
    }
  } 
  
  describe("Applying edits") {
    val contextWithEdits = context applyEdits Seq(gumby.updateField("bar", 2))

    it("should fail if the edits have a different number of items") {
      evaluating { contextWithEdits applyEdits Seq() } should produce [IllegalArgumentException]
    }
    
    it("should leave the current edits with the new updates") {
      contextWithEdits.currentEdits(0)("bar") should equal (2)
    }
    
    it("should leave the current library  with the new updates") {
      contextWithEdits.library(gumby.ref)("bar") should equal (2)
    }

    it("should enabled undo") {
      contextWithEdits.canUndo should be (true)
    }
    
    it("should not be able to redo") {
      contextWithEdits.canRedo should be (false) 
    }
   
    describe("with modified ids") {
      val gumbyWithNewId = gumby.updateField("foo", "yyy")
      val contextWithNewId = context applyEdits Seq(gumbyWithNewId)

      it("should result in the new ID in the library") {
        contextWithNewId.library contains gumbyWithNewId should be (true) 
      }
      
      it("should not have the old ID in the library") {
        contextWithNewId.library contains gumby should be (false)
      }
      
      it("should not be considered a new resource") {
        contextWithNewId isAddedSinceOriginal gumbyWithNewId.ref should be (false)
      }
    } 
    
    describe("Importing items") {
      val libWithLinked = ResourceLibrary("gump", schema).addLinkedLibraries(library)
      val contextAfterImport = LibraryEditSession(libWithLinked, Seq(gumby)).importLinked

      it("should not contain external resources afterward") {
        contextAfterImport.externalResourcesSelected should be (false) 
      }
    }
     
    describe("Deleting items") {
      val contextAfterDelete = context.delete

      it("remove the selected items from the library") {
        contextAfterDelete.library findResource gumby.ref should equal (None)
      }     
      
      it("remove the selected items from the current edits") {
        contextAfterDelete.currentEdits should be ('isEmpty)
      }
       
      describe("that have no ID yet") {
        it("should just clear them out of the current edits") {
          val contextWithNew = context selectItems Seq(gumbyType.emptyData) 
          val contextAfterDelete = contextWithNew.delete
          contextAfterDelete.currentEdits should be ('empty) 
        }
      }

      describe("when they shadow external resources") {
        val gumby2 = RefData(gumbyType, 'foo -> "zzz", 'bar -> 9) 
        val libWithLinked = ResourceLibrary("gump", schema).addLinkedLibraries(library).addResource(gumby2)
        val contextAfterDelete = LibraryEditSession(libWithLinked, Seq(gumby2)).delete
        it("should leave the external resources in the library") {
          contextAfterDelete.library(gumby.ref)("bar") should equal (44)
        }
     
        it("should leave the external resource in the current edits") {
          contextAfterDelete.currentEdits(0)("bar") should equal (44)
        }
        
        it("should recognise that external items are selected now") {
          contextAfterDelete.externalResourcesSelected should be (true)
        }
      }
    }
  
    describe("Selecting items") {
      val gumby2 = RefData(gumbyType, 'foo -> "ddd", 'bar -> 9) 
      val library2 = library addResource gumby2
      val context = LibraryEditSession(library2, Seq(gumby))

      val afterSelection = context selectItems Seq(gumby2)

      it("should have the same library") {
        afterSelection.library should equal (library2) 
      }
    }
  }
} 
