package kenbot.gcsolved.resource
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers._
import org.scalatest._

import Field._
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.types.ListType
import kenbot.gcsolved.resource.types.MapType
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.StringType
import kenbot.gcsolved.resource.types.ValueType

@RunWith(classOf[JUnitRunner])
class ResourceLibrarySpec extends Spec with ShouldMatchers {
  val library = ResourceLibrary("gumby", ResourceSchema())

  def idField = 'id -> StringType ^ (isId = true)
  
  describe("A new ResourceLibrary") {
    it("should have the given id") {
      library.id should equal ("gumby")
    }
    it("should be version 0") {
      library.version should equal (0)
    }
    it("should be unsaved") {
      library should be ('unsaved)
    }
  }

  describe("Resources") {
    val fooType = RefType("foo", idField, 'a -> IntType)
    val blahType = RefType("blah", idField, 'b -> IntType)
    val fooRes = RefData(fooType, 'id -> "bob", 'a -> 1, 'b -> 2, 'c -> 3)
    val blahRes = RefData(blahType, 'id -> "floop", 'a -> 4, 'b -> 5, 'c -> 6)
    val libWithAddedResource = library addResources fooRes
    
    it("should all be iterable through allResources") {
      libWithAddedResource.allResources.toSet should equal (Set(fooRes))
    }
    
    describe("when searched by type") {
      it("should return all resources of that type") {
        val fooRes2 = RefData(fooType, 'id -> "fred", 'a -> 8, 'b -> 7, 'c -> 6)
        val lib2 = library.addResources(fooRes, fooRes2, blahRes)
        lib2.allResourcesByType(fooType).toSet should equal (Set(fooRes, fooRes2))
      }
      
      it("should return all resources of sub-types") {
        val subFooType = RefType("subFoo", fooType, false)
        val subFooRes = RefData(subFooType, 'id -> "fred", 'a -> 8, 'b -> 7, 'c -> 6)
        val lib2 = library.addResources(fooRes, subFooRes, blahRes)
        lib2.allResourcesByType(fooType).toSet should equal (Set(fooRes, subFooRes))
      }
    }
    
    describe("Adding") {
      it ("should result in the library containing it") {
        libWithAddedResource contains fooRes.ref should be (true)
      }
      it ("should result in the resource knowing it is defined in the library") {
        val addedResource = libWithAddedResource.findResource(fooRes.ref).get
        addedResource.definedIn should equal (Some(libWithAddedResource.id))
      }
      it ("should result in the library being able to find it by reference") {
        libWithAddedResource findResource fooRes.ref should equal (Some(fooRes))
      }
      it ("should result in the library being able to find it locally by reference") {
        libWithAddedResource findLocalResource fooRes.ref should equal (Some(fooRes))
      }
    }
     
    describe("Removing") {
      it ("should fail if the resource could not be found") {
        val fakeRes = RefData(RefType("boo", idField), 'id -> "blah")
        evaluating { 
          libWithAddedResource removeResource fakeRes.ref 
        } should produce [IllegalArgumentException]
      }
      it ("should fail if other resources refer to it") {
        val res2 = RefData(fooType, 'id -> "other", 'referToFoo -> ResourceRef(fooRes.id, fooType))
        val libWithTwoResources = libWithAddedResource addResources res2
        evaluating { 
          libWithTwoResources removeResource fooRes.ref 
        } should produce [IllegalArgumentException]
      }
      it ("should result in the library without the resource, if the remove succeeds") {
        val libWithRemovedResource = libWithAddedResource removeResource fooRes.ref
        libWithRemovedResource contains fooRes.ref should be (false)
      }
    }
    
    describe("within a linked library") {
      val linkedRes = RefData(fooType, 'id -> "gumby", 'a -> 33, 'b -> 44, 'c -> 55)
      val linkedLibrary = ResourceLibrary("pokey", ResourceSchema.Empty) addResources linkedRes
      val libWithLinked = libWithAddedResource addLinkedLibraries linkedLibrary 
      
      it ("should be iterable through allResources") {
        libWithLinked.allResources.toSet should equal (Set(fooRes, linkedRes))
      }
      
      it ("should be findable") {
        libWithLinked contains linkedRes.ref should be (true)
      }
      
      it ("should be findable by reference") {
        libWithLinked findResource linkedRes.ref should equal (Some(linkedRes))
      }
      
      it ("should should not be findable locally by reference") {
        libWithLinked findLocalResource linkedRes.ref should equal (None)
      }
      
      describe("if shadowed by a locally stored resource") {
        val res2 = fooRes.updateField("a", 4)
        val linkedLibrary2 = linkedLibrary addResource res2
        val libWithLinked = libWithAddedResource addLinkedLibraries linkedLibrary2
        
        it ("should should not be findable if shadowed by a locally stored resource") {
          val foundRes = (libWithLinked findResource res2.ref).get
          foundRes("a") should equal (1)
        }
        
        it ("should not appear in 'allResources' if shadowed by a locally stored resource") {
          println(libWithLinked.allResources.toList.map(_.debugString))
          
          println("****" + libWithLinked.allResources.toList)
          println("****" + libWithLinked.allResources.map(_.ref).toList)
          libWithLinked.allResources.exists(_("a") == 4) should be (false)
        }
      }

      
      
      it ("should not be removable from the linking library") {
        evaluating {
          libWithLinked removeResource linkedRes.ref
        } should produce [IllegalArgumentException]
      }
    }
    
    it ("should not be findable in the library if not there") {
      val fakeRes = RefData(RefType("boo", idField), 'id -> "blah")
      library findResource fakeRes.ref should equal (None)
    }
    
    describe("Validation") {
      it ("should succeed for a resource containing all valid values") {
        pending
      }
      it ("should fail for a resource containing invalid values") {
        pending
      }
      it ("should fail if the resource has direct broken references") {
        pending
      }
      it ("should fail if the resource has nested broken references") {
        pending
      }
      it ("should make the library invalid if any resources are invalid") {
        pending
      }
      it ("should make the library valid if all resources are valid") {
        pending
      }
      it ("should be able to list all invalid resources") {
        pending
      }
    }
    
    describe("Searching") {
      it("should find all resources matching text") {
        pending
      }
    }
  }

  describe("Linked libraries") {
    lazy val bananaType: RefType = RefType("banana", idField, 'color -> StringType, Field("otherBanana", bananaType))
    val otherLibResource = RefData(bananaType, 'id -> "b1", 'color -> "red")
    val otherLib = ResourceLibrary("other", ResourceSchema.Empty) addResources otherLibResource
    val linkedLib = library addLinkedLibraries otherLib
    val tangledLib = linkedLib addResource RefData(bananaType, 'id -> "z", 'otherBanana -> otherLibResource.ref)
    
    describe("Adding a linked library") {
      it ("should result in the library being linked") {
        linkedLib isLibraryLinked otherLib should be (true)
      }
    }
    
    describe("Removing a linked library") {
      it ("should throw an error if the library wasn't linked") {
        evaluating {
          linkedLib removeLinkedLibrary ResourceLibrary("bogus", ResourceSchema.Empty)
        } should produce [RuntimeException]
      }
      it ("should have no result if the library could not be removed because of existing references") {
        tangledLib removeLinkedLibrary otherLib should equal (None)
      }
      it ("should result in the library without the resource, if the remove succeeds") {
        val libUnlinked = (linkedLib removeLinkedLibrary otherLib).get
        libUnlinked isLibraryLinked otherLib should be (false)
      }
    }

    describe("A library containing references to a resource in another library") {
      describe("the external resource") {
        it("should be in allResources") {
          tangledLib.allResources.toList should contain (otherLibResource)
        }
        it("should be in allExternalRefs") {
          tangledLib.allExternalRefs.toList should contain (otherLibResource.ref)
        }
        it("should be found by contains") {
          tangledLib contains otherLibResource.ref should be (true)
        }
        it("should not be found by containsLocally") {
          tangledLib containsLocally otherLibResource.ref should be (false)
        }
        it("should not be in localResources") {
          tangledLib.localResources.toList should not contain (otherLibResource)
        }
      }
      it("containsReferencesTo should return true if something from the other library is referenced") {
        tangledLib containsReferencesTo otherLib should be (true)
      }
      it("containsReferencesTo should return false if nothing from the other library is referenced") {
        linkedLib containsReferencesTo otherLib should be (false)
      }
    }
  }
  
  describe("Copying resources") {
    it("if all resources are copied, all resources should be found in the new library") {
      pending
    }
    it("if only used resources are copied, used resources should be found in the new library") {
      pending
    }
    it("if only used resources are copied, unused resources should not found in the new library") {
      pending
    }
  }
  
  describe("Renaming the library") {
    it ("should result in a renamed library") {
      (library updateName "fooble").name should equal ("fooble")
    }
  }
  
  describe("Renaming a resource") {
    lazy val fooType: RefType = RefType("Foo", idField, Field("other", fooType))
    val fooListType = RefType("FooList", idField, 'foos -> ListType(fooType))
    val fooMapType = RefType("FooMap", idField, 'foos -> MapType(fooType, fooType))
    val fooValueType = ValueType("FooValue", idField, 'foo -> fooType)
    val fooValueDataType = RefType("FooValueData", idField, 'foo -> fooValueType)
    
    val a = RefData(fooType, 'id -> "a")
    val b = RefData(fooType, 'id -> "b", 'other -> a.ref)
    val fooList = RefData(fooListType, 'id -> "fooList", 'foos -> List(a.ref, b.ref))
    val fooMap = RefData(fooMapType, 'id -> "fooMap", 'foos -> Map(a.ref -> a.ref))
    val fooValue = ValueData(fooValueType, 'foo -> a.ref)
    val fooValueData = RefData(fooValueDataType, 'id -> "fooValueData", 'foo -> fooValue)
    val libWithData = (library /: List(a, b, fooList, fooMap, fooValueData))(_ addResources _)
    val updatedLib = libWithData updateResourceId (a.ref, "monkey")
    val newRef = ResourceRef("monkey", fooType)

    it ("should result in a library containing the renamed resource") {
      updatedLib.findResource(ResourceRef("monkey", fooType)) should not equal (None)
    }
    it ("should result in a library with all direct references to the resource having been updated") {
      val res = updatedLib.findResource(b.ref).get
      res("other") should equal (newRef)
    }
    it ("should result in a library with all list references to the resource having been updated") {
      val res = updatedLib.findResource(fooList.ref).get
      res("foos") should equal (List(newRef, b.ref))
    }
    it ("should result in a library with all map references to the resource having been updated") {
      val res = updatedLib.findResource(fooMap.ref).get
      res.fields("foos") should equal (Map(newRef -> newRef))
    }
    it ("should result in a library with all ValueData references to the resource having been updated") {
      val res = updatedLib.findResource(fooValueData.ref).get
      res.fields("foo") should equal (ValueData(fooValueType, 'foo -> newRef))
    }
  }
  
  describe("Marking as saved") {
    it ("should increment the version") {
      val version = library.version
      library.markAsSaved.version should equal (version + 1)
    }
  }
}
  