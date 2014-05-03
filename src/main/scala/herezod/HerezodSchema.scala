package herezod

import kenbot.gcsolved.core.Field.symbolAndType2Field
import kenbot.gcsolved.core.types.BoolType
import kenbot.gcsolved.core.types.FileType
import kenbot.gcsolved.core.types.IntType
import kenbot.gcsolved.core.types.ListType
import kenbot.gcsolved.core.types.RefType
import kenbot.gcsolved.core.types.SelectOneType
import kenbot.gcsolved.core.types.StringType
import kenbot.gcsolved.core.types.ValueType
import kenbot.gcsolved.core.types.DoubleType
import kenbot.gcsolved.core.Field.symbolAndValue2namePair
import kenbot.gcsolved.core.ResourceSchema
import kenbot.gcsolved.core.ValueData

object HerezodSchema {
  import kenbot.gcsolved.core._

  val RectType = ValueType("Rect") defines (
    'X1 -> IntType ^ (required=true), 
    'Y1 -> IntType ^ (required=true), 
    'X2 -> IntType ^ (required=true), 
    'Y2 -> IntType ^ (required=true))
    
  val RangeType = ValueType("Range") defines (
      'Min -> IntType ^ (required=true), 
      'Max -> IntType ^ (required=true))
      
  val PointType = ValueType("Point") defines (
      'X -> IntType ^ (required=true), 
      'Y -> IntType ^ (required=true))
      
  val ColorType = ValueType("Color") defines (
      'R -> IntType(Some(0), Some(255)) ^ (required=true, description="Red"), 
      'G -> IntType(Some(0), Some(255)) ^ (required=true, description="Green"), 
      'B -> IntType(Some(0), Some(255)) ^ (required=true, description="Blue"))

  val ActorState = SelectOneType("ActorState", StringType, "Moving", "Idle", "Attacking", "Dying", "Passing Out")
  val Race = SelectOneType("Race", StringType, "Human", "Orc")
  val Direction = SelectOneType("Direction", StringType, "Up", "Down", "Left", "Right", "Up-Left", "Up-Right", "Down-Left", "Down-Right")
  val OrthogonalDirection = SelectOneType("OrthogonalDirection", StringType, "Up", "Down", "Left", "Right")
  val TerrainLayer = SelectOneType("TerrainLayer", StringType, "Ground", "Scenery")
  val TerrainNeighbourCombo = SelectOneType("TerrainNeighbourCombo", StringType, 
      "None", "U", "D", "UD", "L", "UL", "DL", "UDL", "R", "UR", 
      "DR", "UDR", "LR", "ULR", "DLR", "ALL")
  
  val ImageType = ValueType("Image").abstractly

  val StillImageType = ValueType("StillImage") /*defines (
    'Filename -> FileType("image", "png") ^ (required=true, description="File containing the image"),
    'Bounds -> RectType ^ (description="Bounds of the image within the file")) */

  val AnimImageType = ValueType("Animation") extend ImageType defines 'Frames -> ListType(StillImageType)

  val SideEffect = RefType("SideEffect").abstractly defines 'Name -> StringType ^ (isId=true)
    
  val GraphicFX = RefType("GraphicFX") defines 'Name -> StringType ^ (isId=true)
      
  val Tool = RefType("Tool") defines 'Name -> StringType ^ (isId=true)
  
  val Terrain = RefType("Terrain").abstractly defines (
      'Name -> StringType ^ (isId=true),
      'Layer -> TerrainLayer,
      'IsOrganic -> BoolType,
      'BlocksProjectiles -> BoolType, 
      'BurnTime -> RangeType, 
      'MapColor -> ColorType, 
      'HidesGround -> BoolType,
      'MyNeighbourID -> IntType,
      'ContentOffsetY -> IntType,
      'Overhang -> PointType, 
      'ClipContentRect -> RectType
      //'OverlayImage -> ImageType,
      //'OverlayImageClippable -> ImageType
      )
  
  val DoorTerrain = RefType("DoorTerrain") extend Terrain defines (
      'DefaultImageCombo -> TerrainNeighbourCombo,    
      //'OpenImages -> ValueType.of(Direction, ListType(ImageType)) ^ (required=true), 
      //'ClosedImages -> ValueType.of(Direction, ListType(ImageType)) ^ (required=true),
      //'SolidToVision -> ValueType.of(OrthogonalDirection, BoolType),
      //'SolidToMovement -> ValueType.of(OrthogonalDirection, BoolType),
      //'SolidToProjectiles -> ValueType.of(OrthogonalDirection, BoolType),
      'DamagedTerrain -> NormalTerrain,
      'BurntTerrain -> NormalTerrain,
      'Flammable -> BoolType, 
      'OpenSideEffects -> ListType(SideEffect),
      'CloseSideEffects -> ListType(SideEffect),
      'TryLockedSideEffects -> ListType(SideEffect),
      'DamageSideEffects -> ListType(SideEffect),
      'BurnSideEffects -> ListType(SideEffect),
      'DamageLevel -> IntType,
      'BurnLevel -> IntType,
      'TimeToClose -> RangeType,
      'Lock -> BoolType,
      'LockComplexity -> IntType,
      'LockDestroysKey -> BoolType,
      'KeyType -> Tool)
  
  val VerticalTerrain = RefType("VerticalTerrain") extend Terrain defines (
      'DefaultImageCombo -> TerrainNeighbourCombo)   
      //'Images -> ValueType.of(TerrainNeighbourCombo, ListType(ImageType)))
      
  lazy val NormalTerrain: RefType = RefType("NormalTerrain") extend Terrain definesLazy Seq(
      'DefaultImageCombo -> TerrainNeighbourCombo ^ (default = Some("UL")),
      //'Images -> ValueType.of(TerrainNeighbourCombo, ListType(ImageType)),
      //'HoleImages -> ListType(ImageType),
      //'CoolImages -> ListType(StillImageType),
      //'FilledHoleImages -> ListType(ImageType),
      //'SolidToVision -> ValueType.of(OrthogonalDirection, BoolType),
      //'SolidToMovement -> ValueType.of(OrthogonalDirection, BoolType),
      //'SolidToProjectiles -> ValueType.of(OrthogonalDirection, BoolType),
      'SolidToVisionEdgesOnly -> BoolType,
      'SolidToMovementEdgesOnly -> BoolType,
      'SolidToProjectilesEdgesOnly -> BoolType,
      'Diggable -> BoolType,
      'Flammable -> BoolType, 
      'HidesItems -> BoolType,
      'IsLiquid -> BoolType,
      'DamageSideEffects -> ListType(SideEffect),
      'BurnSideEffects -> ListType(SideEffect),
      'DamagedTerrain -> NormalTerrain,
      'BurntTerrain -> NormalTerrain,
      'NeighbourIDs -> RangeType,
      'EnterSideEffects -> ListType(SideEffect),
      'LeaveSideEffects -> ListType(SideEffect), 
      'EnterSquareSideEffects -> ListType(SideEffect), 
      'LeaveSquareSideEffects -> ListType(SideEffect), 
      'DamageLevel -> IntType,
      'BurnLevel -> IntType,
      'SpeedPenalty -> DoubleType,
      'ItemDropGFX -> GraphicFX,
      'StaminaPenalty -> IntType)
      
  import Field._
  
  //val someImage = ValueData(StillImageType, 'Filename -> new File("abc.png"), 
  //    'Bounds -> ValueData(RectType, 'X1 -> 1, 'Y1 -> 2, 'X2 -> 3, 'Y2 -> 4))

  val ActorType: RefType = RefType("ActorType") definesLazy Seq(
    'Name -> StringType ^ (isId=true, description="The name of the actor type (singular)"),
    'NamePlural -> StringType ^ (description="The name of the actor type (plural)"),
    'AfraidOf -> ActorType ^ (description="Another type of actor that this one is afraid of"),
    'ImageFile -> FileType("images", "png", "jpg") ^ (description="The image file to use"),
    'Race -> Race ^ (description="Don't be a racist!  They're all equal."),
    'Health -> RangeType ^ (description="The health points available"),
    'HealthRegen -> RangeType ^ (default=Some(RangeType('Min -> 10, 'Max -> 10)))
    //'Magic -> RangeType,
    //'MagicRegen -> RangeType,
    //'CanBeMale -> BoolType,
    //'CanBeFemale -> BoolType ^ (default=Some(true)),
    //'BloodColor -> ColorType
    //'Images ->  ValueType.of(ActorState, ValueType.of(Direction, ListType(ImageType)))
    //'CorpseImages -> ListType(StillImageType) ^ (default=Some(List(someImage)), description="This is what it looks like dead")
    )
  
  val Foo1 = RefType("Foo1") defines ('Id -> StringType ^ (isId = true), 'TNC -> ActorState)
  lazy val Foo2: RefType = RefType("Foo2") extend Foo1 definesLazy Seq('TNC2 -> ActorState, 'Recursive -> Foo2)
  
  import meta._
  
    
  val Schema = ResourceSchema().
    //addRefTypes(Foo1, Foo2).addSelectOneTypes(ActorState)
    addRefTypes(ActorType).
    addValueTypes(RangeType)
   // addSelectOneTypes(ActorState, Race, Direction, OrthogonalDirection, TerrainLayer, TerrainNeighbourCombo)

  val SchemaAsLibrary = Schema.asLibrary
}
