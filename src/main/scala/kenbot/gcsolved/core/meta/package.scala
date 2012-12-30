package kenbot.gcsolved.core
import scala.sys.error

import kenbot.gcsolved.core.Field.symbolAndType2Field
import kenbot.gcsolved.core.Field.symbolAndValue2namePair
import kenbot.gcsolved.core.types.AnyRefType
import kenbot.gcsolved.core.types.AnyType
import kenbot.gcsolved.core.types.AnyValueType
import kenbot.gcsolved.core.types.BoolType
import kenbot.gcsolved.core.types.DoubleType
import kenbot.gcsolved.core.types.FileType
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.ListType
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.ResourceType
import kenbot.gcsolved.core.types.SelectOneType
import kenbot.gcsolved.core.types.StringType
import kenbot.gcsolved.core.types.ValueType

package object meta {
  
  lazy val MetaSchema = ResourceSchema().
      addRefTypes(RefTypeDefinition, ValueTypeDefinition, SelectOneTypeDefinition).
      addValueTypes(MetaFieldType, MetaAnyType, MetaAnyRefType, MetaAnyValueType, 
                    MetaIntType, MetaStringType, MetaBoolType, MetaDoubleType, 
                    MetaFileType, MetaListType, MetaRefType, 
                    MetaValueType, MetaSelectOneType)
  

  lazy val SchemaLibrary = ResourceLibrary("Schema", MetaSchema)
  
  type MetaAnyType = ValueType
  lazy val MetaAnyType = ValueType("MetaAnyType", AnyValueType)
  lazy val MetaNothingType = ValueType("MetaNothingType", AnyValueType)
  lazy val MetaAnyRefType = ValueType("MetaAnyRefType", MetaAnyType)
  lazy val MetaAnyValueType = ValueType("MetaAnyValueType", MetaAnyType)
  lazy val MetaIntType = ValueType("MetaIntType", MetaAnyType, 'Min -> IntType, 'Max -> IntType)
  lazy val MetaStringType = ValueType("MetaStringType", MetaAnyType, 'MaxLength -> IntType)
  lazy val MetaBoolType = ValueType("MetaBooleanType", MetaAnyType)
  lazy val MetaDoubleType = ValueType("MetaDoubleType", MetaAnyType)
  lazy val MetaFileType = ValueType("MetaFileType", MetaAnyType, 'Path -> StringType, 'Extensions -> ListType(StringType))
  lazy val MetaListType = ValueType("MetaListType", MetaAnyType, 'ElementType -> MetaAnyType, 'MaxLength -> IntType)
  lazy val MetaRefType = ValueType("MetaRefType", MetaAnyRefType, 'RefType -> RefTypeDefinition)
  lazy val MetaValueType = ValueType("MetaValueType", MetaAnyValueType, 'ValueType -> ValueTypeDefinition)
  lazy val MetaSelectOneType = ValueType("MetaSelectOneType", MetaAnyType, 'SelectOneType -> SelectOneTypeDefinition)
  
  lazy val RefTypeDefinition: RefType = RefType.recursive("RefTypeDefinition", AnyRefType, false, Seq(
      'Name -> StringType ^ (isId=true), 
      'Parent -> RefTypeDefinition, 
      'Abstract -> BoolType, 
      'Fields -> ListType(MetaFieldType)))
  
  lazy val ValueTypeDefinition: RefType = RefType.recursive("ValueTypeDefinition", AnyRefType, false, Seq(
      'Name -> StringType ^ (isId=true), 
      'Parent -> ValueTypeDefinition, 
      'Abstract -> BoolType, 
      'Fields -> ListType(MetaFieldType)))

  lazy val SelectOneTypeDefinition: RefType = RefType("SelectOneTypeDefinition", AnyRefType, false, 
      'Name -> StringType ^ (isId=true), 
      'ValueType -> MetaAnyType, 
      'Values -> ListType(AnyType))
      
  lazy val MetaFieldType: ValueType = ValueType("MetaField", 
      'Name -> StringType ^ (required=true, description="The field name"), 
      'FieldType -> MetaAnyType ^ (required=true, description="The type of this field"), 
      'Category -> StringType ^ (description="Optionally some category that the field belongs in, like 'Details' or 'Images'"),
      'Description -> StringType ^ (description="Description of what the field does"),
      'Required -> BoolType ^ (description="Whether or not the field is required"), 
      'IsId -> BoolType ^ (description="Whether or not this field is the ID. Only one field can be the ID, and it will always be required."),
      'Default -> AnyType ^ (description="Optionally, some default value for the field"))
                    
                    
  implicit def enrichLibrary(lib: ResourceLibrary) = new RichResourceLibrary(lib)    
  implicit def enrichSchema(s: ResourceSchema) = new RichResourceSchema(s)    
  implicit def enrichField(f: Field) = new RichField(f)
  implicit def enrichResourceType(rt: ResourceType) = new RichResourceType(rt)
  implicit def enrichSelectOneType(s1t: SelectOneType) = new RichSelectOneType(s1t)
  implicit def enrichRefType(rt: RefType) = new RichRefType(rt)
  implicit def enrichValueType(vt: ValueType) = new RichValueType(vt)

  
  class RichResourceSchema(schema: ResourceSchema) {
    def asLibrary: ResourceLibrary = ResourceLibrary("Schema", MetaSchema).
      addResources(AnyRefType.asData +: schema.userRefTypes.map(_.asData): _*).
      addResources(AnyValueType.asData +: schema.userValueTypes.map(_.asData): _*).
      addResources(schema.userSelectOneTypes.map(_.asData): _*).
      addLinkedLibraries(schema.linkedSchemas.map(_.asLibrary): _*)
  }
  
  
  class RichResourceLibrary(library: ResourceLibrary) {
    def asSchema: ResourceSchema = {
      require(library.schema == MetaSchema, "This library cannot be converted into a schema.")
      
      lazy val context: SchemaContext = new SchemaContext(
          ResourceSchema(refTypes.toSeq, valueTypes.toSeq, selectOneTypes.toSeq))
      
      import context._        
      
      def refTypes = library allResourcesByType RefTypeDefinition map (_.asRefType)
      def valueTypes = library allResourcesByType ValueTypeDefinition map (_.asValueType)
      def selectOneTypes = library allResourcesByType SelectOneTypeDefinition map (_.asSelectOneType)

      context.schema
    }
  }
  
  class RichValueType(valueType: ValueType) {
    def metaRef: ResourceRef = ResourceRef(valueType.name, ValueTypeDefinition)
    
    def asData: RefData = RefData(ValueTypeDefinition, 
      'Name -> valueType.name,
      'Parent -> valueType.parent.metaRef, 
      'Abstract -> valueType.isAbstract,
      'Fields -> valueType.localFields.values.toList.map(_.asData))
  }
  
  class RichRefType(refType: RefType) {
    def metaRef: ResourceRef = ResourceRef(refType.name, RefTypeDefinition)
    
    def asData: RefData = RefData(RefTypeDefinition, 
      'Name -> refType.name,
      'Parent -> refType.parent.metaRef,
      'Abstract -> refType.isAbstract,
      'Fields -> refType.localFields.values.toList.map(_.asData))
  }

  class RichSelectOneType(selectOneType: SelectOneType) {
    def metaRef: ResourceRef = ResourceRef(selectOneType.name, SelectOneTypeDefinition)
    
    def asData: RefData = RefData(SelectOneTypeDefinition, 
      'Name -> selectOneType.name,
      'ValueType -> selectOneType.valueType.typeDescriptor, 
      'Values -> selectOneType.values.map(selectOneType.valueType.asAny))
  } 
  
  class RichField(field: Field) {
    def asData: ValueData = {
      
      val baseFields = Map(
        "Name" -> field.name,
        "FieldType" -> field.fieldType.typeDescriptor,
        "Required" -> field.required, 
        "IsId" -> field.isId)
      
      val defaultMapping = field.default.map("Default" -> field.fieldType.asAny(_))
      val categoryMapping = if (field.category.nonEmpty) Some("Category" -> field.category) 
                            else None
                            
      val descriptionMapping = if (field.description.nonEmpty) Some("Description" -> field.description) 
                               else None    
      
      ValueData(MetaFieldType, baseFields ++ defaultMapping ++ categoryMapping ++ descriptionMapping)
    }
  } 

  class RichResourceType(resourceType: ResourceType) {
    def typeDescriptor: ValueData = resourceType match {    
      case AnyType | AnyValueType | AnyRefType | DoubleType | BoolType | StringType => ValueData(resourceType.metaType)
      
      case IntType(minOpt, maxOpt) => 
        val minMapping = minOpt.map("Min" ->)
        val maxMapping = maxOpt.map("Max" ->)
        val fields = Map() ++ minMapping ++ maxMapping
        ValueData(MetaIntType, fields)
        
      case FileType(path, extensions @ _*) => ValueData(MetaFileType, 
          'Path -> path,
          'Extensions -> extensions.toList)
          
      case ListType(elementType, maxLengthOpt) => 
          val lengthMapping = maxLengthOpt.map("Length" ->)
          val fields = Map("ElementType" -> elementType.typeDescriptor) ++ lengthMapping
          ValueData(MetaListType, fields)

      case rt: RefType => ValueData(MetaRefType, 'RefType -> rt.metaRef)
      case vt: ValueType => ValueData(MetaValueType, 'ValueType -> vt.metaRef)
      case s1t: SelectOneType =>  ValueData(MetaSelectOneType, 'SelectOneType -> s1t.metaRef)
      case x => error("Unknown type: " + x) 
    }
  }

  class SchemaContext(currentSchema: => ResourceSchema) {
    
    lazy val schema = currentSchema
    
    implicit def enrichRefData(rd: RefData): RichRefData = new RichRefData(rd)
    implicit def enrichValueData(vd: ValueData) = new RichValueData(vd)
    
    class RichValueData(valueData: ValueData) {
      def asResourceType: ResourceType = valueData.resourceType match {    
        case MetaAnyType => AnyType
        case MetaAnyRefType => AnyRefType
        case MetaAnyValueType => AnyValueType
        case MetaIntType => IntType(valueData.getAs[Int]("Min"), valueData.getAs[Int]("Max"))
        case MetaDoubleType => DoubleType
        case MetaBoolType => BoolType
        case MetaStringType => StringType
        case MetaFileType => FileType(valueData("Category").toString, valueData("Extensions").asInstanceOf[List[String]]: _*)
        case MetaListType => ListType(valueData("ElementType").asInstanceOf[ValueData].asResourceType)
        case MetaRefType => schema.findRefType(valueData("RefType").asInstanceOf[ResourceRef].id).get
        case MetaValueType => schema.findValueType(valueData("ValueType").asInstanceOf[ResourceRef].id).get
        case MetaSelectOneType => schema.findSelectOneType(valueData("SelectOneType").asInstanceOf[ResourceRef].id).get
        case x => error("Unknown meta-type: " + x) 
      }
    
      def asField: Field = {
        val name = valueData("Name").asInstanceOf[String]
        val fieldType = valueData("FieldType").asInstanceOf[ValueData].asResourceType
        val category = valueData.getOrElse("Category", "")
        val description = valueData.getOrElse("Description", "")
        val required = valueData("Required").asInstanceOf[Boolean]
        val isId = valueData("IsId").asInstanceOf[Boolean]
        val default: Option[fieldType.Value] = valueData.get("Default").map { data => 
          fieldType asValue (AnyType asValue data).rawValue
        }
        Field[fieldType.Value](name, fieldType, category, required, isId, default, description)
      }
    }
    
    class RichRefData(refData: RefData) {
      def asRefType: RefType = {
        val name = refData("Name").asInstanceOf[String]
        val parentName = refData("Parent").asInstanceOf[ResourceRef].id
        def parent: RefType = schema.findRefType(parentName) getOrElse error("No RefType found called \"" + parentName + "\"") 
        val isAbstract = refData("Abstract").asInstanceOf[Boolean]
        val fields: List[Field] = refData("Fields").asInstanceOf[List[ValueData]].map(_.asField)
        RefType(name, parent, isAbstract, fields: _*)
      }
        
      def asValueType: ValueType = {
        val name = refData("Name").asInstanceOf[String]
        val parentName = refData("Parent").asInstanceOf[ResourceRef].id
        def parent: ValueType = schema.findValueType(parentName) getOrElse error("No ValueType found called \"" + parentName + "\"")
        val isAbstract = refData("Abstract").asInstanceOf[Boolean]
        val fields: List[Field] = refData("Fields").asInstanceOf[List[ValueData]].map(_.asField)
        ValueType(name, parent, isAbstract, fields: _*)
      }
        
      def asSelectOneType: SelectOneType = {
        val name = refData("Name").asInstanceOf[String]
        val valueType: ResourceType = refData("ValueType").asInstanceOf[ValueData].asResourceType
        val values: List[valueType.Value] = {
          val anyDataList = refData("Values").asInstanceOf[List[AnyData]]
          anyDataList.map(valueType asValue _.rawValue)
        }
        SelectOneType[valueType.Value](name, valueType, values: _*)
      }
    }
  }
}
