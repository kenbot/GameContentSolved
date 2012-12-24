package kenbot.gcsolved.core

import java.io.File

import scala.sys.error

import kenbot.gcsolved.core.types.RefType

object ResourceLibrary {
  type ResourceLibraryRef = String
  def apply(id: String, schema: ResourceSchema): ResourceLibrary = apply(id, id, schema)
  def apply(id: String, name: String, schema: ResourceSchema): ResourceLibrary = 
      new ResourceLibrary(id, name, "", 0, Map(), Set(), schema, Seq())
}

case class ResourceRef(id: String, refType: RefType) {
  private def equality = (id, refType.name)
  override def hashCode() = equality.hashCode
  override def equals(a: Any) = a match {
    case rr: ResourceRef => equality == rr.equality
    case _ => false
  }
}


final class ResourceLibrary private (
    val id: String, 
    val name: String, 
    val description: String,
    val version: Int,
    private val resourceMap: Map[ResourceRef, RefData],
    private val fileSet: Set[File],
    val schema: ResourceSchema,
    val linkedLibraries: Seq[ResourceLibrary]) {

  
  import ResourceLibrary.ResourceLibraryRef

  def ref: ResourceLibraryRef = id
  def unsaved: Boolean = version == 0
  
  def contains(resource: RefData) = containsRef(resource.ref)
  def containsLocally(resource: RefData) = containsRefLocally(resource.ref)
  def containsRefLocally(resourceRef: ResourceRef): Boolean = resourceMap contains resourceRef
  
  def containsRef(resourceRef: ResourceRef): Boolean = 
      containsRefLocally(resourceRef) || linkedLibraries.exists(_ containsRefLocally resourceRef)
      
  def findLocalResource(resourceRef: ResourceRef): Option[RefData] = resourceMap get resourceRef
  
  def findResource(resourceRef: ResourceRef): Option[RefData] = {
    val res = findLocalResource(resourceRef)
    (res /: linkedLibraries)(_ orElse _.findResource(resourceRef))
  }
  
  def apply(resourceRef: ResourceRef): RefData = findResource(resourceRef) getOrElse error("Resource " + resourceRef + " not found")

  def files: Set[File] = fileSet
  
  def addFiles(filesToAdd: File*): ResourceLibrary = copy(fileSet = fileSet ++ filesToAdd)
  
  def findResourcesThatReferTo(resourceRef: ResourceRef): Seq[RefData] = 
      allResources.filter(_ refersTo resourceRef).toSeq
      
  def allResources: Iterator[RefData] = (localResources.toSet /: linkedLibraries) { (rs, lib) =>
    def alreadyDefined(r: RefData) = rs.exists(_.ref == r.ref)
    rs ++ (lib.allResources filterNot alreadyDefined)
  }.iterator
  
  def allResourcesByType(refType: RefType): Iterator[RefData] = allResources filter (_.resourceType <:< refType)
  def localResources: Iterator[RefData] = resourceMap.valuesIterator
  def localResourcesByType(refType: RefType): Iterator[RefData] = localResources filter (_.resourceType <:< refType)
  def search(searchString: String): Iterator[RefData] = allResources filter (_ matches searchString)
  def invalidResources: Seq[RefData] = (allResources filterNot isResourceValid).toSeq
  
  def failures: List[String] = {
    val regularFailures = allResources.flatMap(_.failures).toList
    def invalidMessage(ref: ResourceRef) = "Can't resolve resource: " + ref
    val linkageFailures = allResources.flatMap(_.externalRefs filterNot containsRef map invalidMessage)
    
    regularFailures ++ linkageFailures
  }
  
  def isShadowingLinkedResource(ref: ResourceRef): Boolean = {
    containsRefLocally(ref) && linkedLibraries.exists(_ containsRef ref) 
  } 

  def isResourceValid(resource: RefData): Boolean = resource.valid && (resource.externalRefs forall containsRef)
  def valid: Boolean = allResources forall isResourceValid
  def isLibraryLinked(library: ResourceLibrary): Boolean = linkedLibraries.exists(_.ref == library.ref)
  
  private def copy(
      id: String = this.id, 
      name: String = this.name, 
      description: String = this.description,
      version: Int = this.version,
      resourceMap: Map[ResourceRef, RefData] = this.resourceMap,
      fileSet: Set[File] = this.fileSet,
      schema: ResourceSchema = this.schema,
      linkedLibraries: Seq[ResourceLibrary] = this.linkedLibraries) = {
      
    new ResourceLibrary(id, name, description, version, resourceMap, fileSet, schema, linkedLibraries)
  }
   
  // Updating methods
  def updateId(id: String) = copy(id = id)
  def updateName(name: String) = copy(name = name)
  def updateDescription(description: String) = copy(description = description)
  def updateResourceId(resourceRef: ResourceRef, newId: String) = {
    val data = findResource(resourceRef) getOrElse error("Couldn't find resource: " + resourceRef)
    val newRef = resourceRef.copy(id = newId)
    val newData = data updateId newId
    val newResourceMap: Map[ResourceRef, RefData] = resourceMap map {
      case (`resourceRef`, refData) => (newRef -> newData)
      case (id, refData) => id -> refData.updateResourceRefs(resourceRef, newId)
    }
    copy(resourceMap = newResourceMap)
  }

  def markAsSaved() = copy(version = version+1)

  private def asNewEntry(r: RefData) = r.ref -> (r asDefinedIn this)
  def addResource(resource: RefData): ResourceLibrary = copy(resourceMap = resourceMap + asNewEntry(resource))
  def addResources(resources: RefData*): ResourceLibrary = copy(resourceMap = resourceMap ++ (resources map asNewEntry))
  
  def removeResource(resourceRef: ResourceRef): ResourceLibrary = {
    require(findResourcesThatReferTo(resourceRef).isEmpty, 
        "Cannot remove '" + resourceRef + "', as other resources still refer to it")
    
    copy(resourceMap = resourceMap - resourceRef)
  }
  
  def removeResources(resourceRefs: Seq[ResourceRef]): ResourceLibrary = (this /: resourceRefs) { _ removeResource _ }
  
  
  def allExternalRefs: Iterator[ResourceRef] = localResources.flatMap(_.externalRefs)
  
  def containsReferencesTo(library: ResourceLibrary): Boolean = {
    allExternalRefs.exists(r => !containsRefLocally(r) && (library containsRefLocally r))
  }
  
  def addLinkedLibraries(libraries: ResourceLibrary*) = {
    // TODO disallow cyclic linking
    copy(linkedLibraries = linkedLibraries ++ libraries)
  }
  
  def removeLinkedLibrary(library: ResourceLibrary): Option[ResourceLibrary] = {
    require(linkedLibraries contains library, 
      "Can't remove library '%s' since it isn't linked" format library.ref)
    
    if (containsReferencesTo(library)) None
    else Some(copy(linkedLibraries = linkedLibraries.filterNot(library==)))
  }
  
  def copyAllFromLibrary(library: ResourceLibrary): ResourceLibrary = error("pending")
  def copyUsedFromLibrary(library: ResourceLibrary): ResourceLibrary = error("pending")
    
  private def equality = (id, name, description, version, resourceMap, schema, linkedLibraries)
  
  override def equals(a: Any): Boolean = a match {
    case rl: ResourceLibrary => equality == rl.equality
    case _ => false
  }
  
  override def hashCode() = equality.hashCode
}
