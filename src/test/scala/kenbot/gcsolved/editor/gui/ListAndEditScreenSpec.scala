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

@RunWith(classOf[JUnitRunner])  
class ListAndEditScreenSpec extends Spec with ShouldMatchers {

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
      

    
    ResourceLibrary("foo", schema).addResources(legoMan)
  }
  
  val legoMan = library.findResource(ResourceRef("1", legoManType)).get
  val truckContainingMan = RefData(legoTruckType, 'id -> 9, 'legoMan -> legoMan.ref)
  
  
  describe("Modifying resources") {
    
    it("should trigger a library update when the widget loses focus") {
      val screen = newScreen()
      
      findUpdatedResourceValue(screen, legoMan.ref, "head") should equal ("pirate")
      
      updateWidget(screen, "head", Some("spaceman"))
      
      findUpdatedResourceValue(screen, legoMan.ref, "head") should equal ("spaceman")
    }

    describe("by changing the ID") {
      val screen = newScreen(library addResource truckContainingMan)
      val oldId = legoMan.ref
      val newId = ResourceRef("222", legoManType)
      
      updateWidget(screen, "id", Some(222))
      
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

  describe("Delete") {
    val screen = newScreen()
    screen.delete()
    
    it ("should remove the resource from the library, if it already exists") {
      screen.updatedLibrary contains legoMan.ref should be (false)
    }
    
    it ("should remove the listing from the screen") {
      screen.allResources.exists(_.currentId == legoMan.id) should be (false)
    }
  }
  
  
  describe("Undo changes") {
    val screen = newScreen()
    updateWidget(screen, "head", Some("spaceman"))
    findUpdatedResourceValue(screen, legoMan.ref, "head") should equal ("spaceman")
    screen.undoChanges()
    
    it("should revert the selected resource/s to the original state in the library") {
      findUpdatedResourceValue(screen, legoMan.ref, "head") should equal ("pirate")
    }
    
    it("should revert the selected resource/s to the original state in the widgets") {
      findWidget(screen, "head").flatMap(_.fieldValue) should equal (Some("pirate"))
    }
    
    it("should revert the selected resource/s to the original state in the list view") {
      val legoManItem = screen.allResources.find(_.currentId == legoMan.id).get
      legoManItem.current("head") should equal ("pirate")
    }
    
    describe("if reverting an ID change") {
      val screen = newScreen()
      val oldId = legoMan.ref
      val newId = ResourceRef("222", legoManType)
      updateWidget(screen, "id", Some(222))
      findUpdatedResourceValue(screen, newId, "id") should equal (222)
      screen.undoChanges()
      
      it("should have the old ID back in the library") {
        screen.updatedLibrary contains oldId should be (true)
      }
      
      it("shouldn't have the new ID in the library anymore") {
        screen.updatedLibrary contains newId should be (false)
      }
      
      it("should have the old ID back in the list view") {
        screen.allResources.exists(_.current.ref == oldId) should be (true)
      }
      
      it("shouldn't have the new ID in the list view anymore") {
        screen.allResources.exists(_.current.ref == newId) should be (false)
      }
      
      it("should have the old ID back in the widget") {
        findWidget(screen, "id").get.fieldValue.get.toString should equal (oldId.id)
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
        val screenItemRef = screen.selectedResources.head.current.ref
        screen.updatedLibrary containsLocally screenItemRef should be (true)
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
    
  private def newScreen(lib: ResourceLibrary = library) = new ListAndEditScreen(legoManType, Seq(legoMan), lib, new TextFieldWidget(_))
  
  
  //////////////////////
  // TODO Just awful!!  ListAndEditScreen shouldn't know anything about widgets.  Kill ASAP
  private def updateWidget(screen: ListAndEditScreen, name: String, value: Option[Any]) {
    val w = findWidget(screen, name).get
    w.fieldValue = value
    w.hasFocus = true
    w.hasFocus = false
  }
  private def findWidget(screen: ListAndEditScreen, name: String) = screen.editScreen.asInstanceOf[WidgetEditScreen].fieldWidgets.find(_.field.name == name)
  //////////////////////
  
  
  
  private def findUpdatedResourceValue(screen: ListAndEditScreen, ref: ResourceRef, property: String): Any = {
    val resource = screen.updatedLibrary.findResource(ref)
    val prop = resource.map(_ get property) getOrElse error("Couldn't find resource " + ref)
    prop getOrElse error("No value for " + property + " on " + ref)
  }
}