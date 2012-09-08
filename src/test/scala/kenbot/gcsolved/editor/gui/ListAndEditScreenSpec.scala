package kenbot.gcsolved.editor.gui
import scala.sys.error
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import kenbot.gcsolved.editor.gui.widgets.TextFieldWidget
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.StringType
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.RefData
import kenbot.gcsolved.resource.ResourceLibrary
import kenbot.gcsolved.resource.ResourceRef
import kenbot.gcsolved.resource.ResourceSchema
import scala.swing.Publisher

@RunWith(classOf[JUnitRunner])  
class ListAndEditScreenSpec extends Spec with ShouldMatchers with Publisher {

  import Field._
  
  val legoManType = RefType("LegoMan", 
      'id -> IntType ^ (isId = true),
      'head -> StringType, 
      'body -> StringType, 
      'pants -> StringType)
      
  val legoTruckType = RefType("LegoTruck", 'id -> IntType ^ (isId = true), 'legoMan -> legoManType)
      
  val schema = ResourceSchema().addRefTypes(legoManType, legoTruckType)
  val library = {
    val legoMan = RefData(legoManType, 
      'id -> 1, 'head -> "pirate", 'body -> "fireman", 'pants -> "green")

    val legoMan2 = RefData(legoManType, 
      'id -> 2, 'head -> "stormTrooper", 'body -> "waitress", 'pants -> "red")
    
    ResourceLibrary("foo", schema).addResources(legoMan, legoMan2)
  }
  
  val legoMan = library.findResource(ResourceRef("1", legoManType)).get
  val legoMan2 = library.findResource(ResourceRef("2", legoManType)).get
  val truckContainingMan = RefData(legoTruckType, 'id -> 9, 'legoMan -> legoMan.ref)
  
  
  describe("Modifying resources") {
    
    it("should trigger a library update when the edit screen fires a value update") {
      val screen = newScreen()
      
      findUpdatedResourceValue(screen, legoMan.ref, "head") should equal ("pirate")

      fireValueUpdate(screen, legoMan.updateField("head", "spaceman"))
      
      findUpdatedResourceValue(screen, legoMan.ref, "head") should equal ("spaceman")
    }

    describe("by changing the ID") {
      val screen = newScreen(library addResource truckContainingMan)

      fireValueUpdate(screen, legoMan.updateField("id", 222))

      val oldId = legoMan.ref
      val newId = ResourceRef("222", legoManType)
      
      
      it("should not leave a resource with the old ID in the library") {
        screen.updatedLibrary findResource oldId should be ('isEmpty)
      } 

      it("should leave a resource with the new ID in the library") {
        screen.updatedLibrary findResource newId should not be ('isEmpty)
      }
      
      it("should update other resources that refer to it") {
        findUpdatedResourceValue(screen, truckContainingMan.ref, "legoMan") should equal (newId)
      }
      
      it("should not leave a resource with the old ID in the list view") {
        screen.allResources.exists(_.currentId == oldId.id) should be (false)
      }
      
      it("should leave a resource with the new ID in the list view") {
        screen.allResources.exists(_.currentId == newId.id) should be (true)
      }
      
    }
  }
  
  describe("Changing the selection") {
    val screen = newScreen()

    it("should save any changes in progress in the EditScreen") {

      screen.editScreen.values = Seq(legoMan.updateField("head", "darthVader") ) 

      findUpdatedResourceValue(screen, legoMan.ref, "head") should equal ("pirate")
      
      fireResourcesSelected(screen, Nil)
      
      findUpdatedResourceValue(screen, legoMan.ref, "head") should equal ("darthVader")
    }
    
    it("should not add external resources") {
      val linkedLib = ResourceLibrary("blah", schema).addLinkedLibraries(library)
      val screen = newScreen(linkedLib)
       
      fireResourcesSelected(screen, Nil) 
      
      screen.updatedLibrary containsLocally legoMan should be (false) 
    } 
  }

  describe("Deleting") {
    val screen = newScreen()
    screen.delete()
 
    it ("should remove the resource from the library, if it already exists") {
      screen.updatedLibrary contains legoMan should be (false)
    }
    
    it ("should remove the item from the screen") {
      screen.allResources.exists(_.currentId == legoMan.id) should be (false)
    }

    describe ("a local resource that exists in a linked library") {
      val linkedLib = ResourceLibrary("blah", schema).addLinkedLibraries(library).addResource(legoMan)
      val screen = newScreen(linkedLib)
      screen.delete()
      val remainingItem = screen.allResources.find(_.currentId == legoMan.id)

      it("should still hold the linked item in the library") {
        screen.updatedLibrary contains legoMan should be (true)
      }
      
      it ("should retain the listing from the screen") {
 	remainingItem should be ('defined)
      }
      
      it ("should be marked as external") {
        remainingItem.get should be ('isExternal)
      }
     
      it ("should leave the the edit screen non-editable") {
        screen.editScreen should not be ('editable)
      }
    } 
  }
  
  
  describe("Undo changes") {
    val screen = newScreen()
    
    fireValueUpdate(screen, legoMan.updateField("head", "spaceman"))
    
    findUpdatedResourceValue(screen, legoMan.ref, "head") should equal ("spaceman")
    screen.undoChanges()
    
    it("should revert the selected resource/s to the original state in the library") {
      findUpdatedResourceValue(screen, legoMan.ref, "head") should equal ("pirate")
    }
    
    it("should revert the selected resource/s to the original state in the edit screen") {
      screen.editScreen.values(0)("head") should equal ("pirate")
    }
    
    it("should revert the selected resource/s to the original state in the list view") {
      val legoManItem = screen.allResources.find(_.currentId == legoMan.id).get
      legoManItem.current("head") should equal ("pirate")
    }
    
    describe("if reverting an ID change") {
      val screen = newScreen()
      val oldId = legoMan.ref
      val newId = ResourceRef("222", legoManType)
      
      fireValueUpdate(screen, legoMan.updateField("id", 222))
      
      findUpdatedResourceValue(screen, newId, "id") should equal (222)
      screen.undoChanges()
      
      it("should have the old ID back in the library") {
        screen.updatedLibrary containsRef oldId should be (true)
      }
      
      it("shouldn't have the new ID in the library anymore") {
        screen.updatedLibrary containsRef newId should be (false)
      }
      
      it("should have the old ID back in the list view") {
        screen.allResources.exists(_.current.ref == oldId) should be (true)
      }
      
      it("shouldn't have the new ID in the list view anymore") {
        screen.allResources.exists(_.current.ref == newId) should be (false)
      }
      
      it("should have the old ID back in the edit screen") {
        screen.editScreen.values(0).id should equal (legoMan.id)
      }
    }
  }
  
  describe("Viewing a resource from another library") {
    val linkedLib = ResourceLibrary("blah", schema).addLinkedLibraries(library)
    val screen = newScreen(linkedLib)
    
    it("should not be editable") {
      screen.editScreen should not be ('editable)
    }
    
    describe("Importing") {
      it("should make the screen editable") {
        screen.importSelected()
        screen.editScreen should be ('editable)
      }
      
      it("should add the resource locally to the library") {
        screen.importSelected()
        val screenItem = screen.selectedResources.head.current
        screen.updatedLibrary containsLocally screenItem should be (true)
      }
    }
  }
  
  describe("Creating a new resource") {
    
    it("should add the resource to the list view, the first time") {
      val screen = newScreen()
      val numStartingResources = screen.allResources.size
      screen.addNew()
      screen.allResources.size should equal (numStartingResources + 1)
    }
    
    it("shouldn't add the resource to the list view if there's already a new one with no ID") {
      val screen = newScreen()
      val numStartingResources = screen.allResources.size
      screen.addNew()
      screen.addNew()
      screen.allResources.size should equal (numStartingResources + 1)
    }
    
    
    it("shouldn't add anything to the library yet") {
      val screen = newScreen()
      val startingLibrarySize = screen.updatedLibrary.allResources.size
      screen.addNew()
      screen.updatedLibrary.allResources.size should equal (startingLibrarySize)
    }
  }
  
  describe("Cloning a resource") {
    it("should add another resource to the library") {
      pending
    }
    
    it("should add another resource to the view") {
      pending
    }
    
    it("should have all the same fields as the original, other than ID") {
      pending
    }
  }
  
  describe("When no resources are selected") {
    it("should disable the Undo Changes button") {
      pending
    }
    
    it("should disable the Delete button") {
      pending
    }
    
    it("should disable the Clone button") {
      pending
    }
  }
    
  private def newScreen(lib: ResourceLibrary = library) = {
    new ListAndEditScreen(legoManType, Seq(legoMan), lib, DummyEditScreen.apply) 
  }
 
  private def fireValueUpdate(screen: ListAndEditScreen, value: RefData) {
    screen listenTo this
    publish(UpdateValues(screen.editScreen, Seq(value)))
    screen deafTo this
  }

  private def fireResourcesSelected(screen: ListAndEditScreen, values: Seq[ListAndEditItem]) {
    screen listenTo this
    import screen.panel.events._
    publish(ResourcesSelected(values))
    screen deafTo this
  }
  
  
  private def findUpdatedResourceValue(screen: ListAndEditScreen, ref: ResourceRef, property: String): Any = {
    val resource = screen.updatedLibrary.findResource(ref)
    val prop = resource.map(_ get property) getOrElse error("Couldn't find resource " + ref)
    prop getOrElse error("No value for " + property + " on " + ref)
  }
}
