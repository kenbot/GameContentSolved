package herezod

import kenbot.gcsolved.resource.Field.symbolAndType2Field
import kenbot.gcsolved.resource.types.AnyRefType
import kenbot.gcsolved.resource.types.AnyValueType
import kenbot.gcsolved.resource.types.BoolType
import kenbot.gcsolved.resource.types.FileType
import kenbot.gcsolved.resource.types.IntType
import kenbot.gcsolved.resource.types.ListType
import kenbot.gcsolved.resource.types.MapType
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.resource.types.SelectOneType
import kenbot.gcsolved.resource.types.StringType
import kenbot.gcsolved.resource.types.ValueType
import kenbot.gcsolved.resource.types.DoubleType
import java.io.File
import org.w3c.dom.css.Rect

package object types {
  import kenbot.gcsolved.resource._

  val RectType = ValueType("Rect", 
    'X1 -> IntType ^ (required=true), 
    'Y1 -> IntType ^ (required=true), 
    'X2 -> IntType ^ (required=true), 
    'Y2 -> IntType ^ (required=true))
    
  val RangeType = ValueType("Range", 
      'Min -> IntType ^ (required=true), 
      'Max -> IntType ^ (required=true))
      
  val PointType = ValueType("Point", 
      'X -> IntType ^ (required=true), 
      'Y -> IntType ^ (required=true))
      
  val ColorType = ValueType("Color", 
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
  
  val ImageType = ValueType("Image", AnyValueType, true)

  val StillImageType = ValueType("StillImage", ImageType, false,
    'Filename -> FileType("image", "png") ^ (required=true, description="File containing the image"),
    'Bounds -> RectType ^ (description="Bounds of the image within the file"))

  val AnimImageType = ValueType("Animation", ImageType, false,
    'Frames -> ListType(StillImageType))

  val SideEffect = RefType("SideEffect", AnyRefType, true, 
      'Name -> StringType ^ (isId=true))
    
  val GraphicFX = RefType("GraphicFX", AnyRefType, false,
      'Name -> StringType ^ (isId=true))
      
  val Tool = RefType("Tool", AnyRefType, false, 
      'Name -> StringType ^ (isId=true))
  
  val Terrain = RefType("Terrain", AnyRefType, true,
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
      'ClipContentRect -> RectType,
      'OverlayImage -> ImageType,
      'OverlayImageClippable -> ImageType)
    
  val DoorTerrain: RefType = RefType("DoorTerrain", Terrain, false,
      'DefaultImageCombo -> TerrainNeighbourCombo,    
      'OpenImages -> MapType(Direction, ListType(ImageType)) ^ (required=true), 
      'ClosedImages -> MapType(Direction, ListType(ImageType)) ^ (required=true),
      'SolidToVision -> MapType(OrthogonalDirection, BoolType),
      'SolidToMovement -> MapType(OrthogonalDirection, BoolType),
      'SolidToProjectiles -> MapType(OrthogonalDirection, BoolType),
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
      'KeyType -> Tool )
      
  
  
  val VerticalTerrain: RefType = RefType("VerticalTerrain", Terrain, false,
      'DefaultImageCombo -> TerrainNeighbourCombo,    
      'Images -> MapType(TerrainNeighbourCombo, ListType(ImageType)))
      
  lazy val NormalTerrain: RefType = RefType.recursive("NormalTerrain", Terrain, false, Seq(
      'DefaultImageCombo -> TerrainNeighbourCombo ^ (default = Some("UL")),
      'Images -> MapType(TerrainNeighbourCombo, ListType(ImageType)),
      'HoleImages -> ListType(ImageType),
      'CoolImages -> ListType(StillImageType),
      'FilledHoleImages -> ListType(ImageType),
      'SolidToVision -> MapType(OrthogonalDirection, BoolType),
      'SolidToMovement -> MapType(OrthogonalDirection, BoolType),
      'SolidToProjectiles -> MapType(OrthogonalDirection, BoolType),
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
      'StaminaPenalty -> IntType))
      
  import Field._
  
  val someImage = ValueData(StillImageType, 'Filename -> new File("abc.png"), 
      'Bounds -> ValueData(RectType, 'X1 -> 1, 'Y1 -> 2, 'X2 -> 3, 'Y2 -> 4))
  
  val ActorType: RefType = RefType.recursive("ActorType", Seq(
      
    'Name -> StringType ^ (isId=true, description="The name of the actor type (singular)"),
    'NamePlural -> StringType ^ (description="The name of the actor type (plural)"),
    'AfraidOf -> ActorType ^ (description="Another type of actor that this one is afraid of"),
    'ImageFile -> FileType("images", "png", "jpg") ^ (description="The image file to use"),
    'Race -> Race ^ (description="Don't be a racist!  They're all equal."),
    'Health -> RangeType ^ (description="The health points available"),
    'HealthRegen -> RangeType ^ (default=Some(ValueData(PointType, 'Min -> 10, 'Max -> 10))),
    'Magic -> RangeType,
    'MagicRegen -> RangeType,
    'CanBeMale -> BoolType,
    'CanBeFemale -> BoolType ^ (default=Some(true)),
    'BloodColor -> ColorType,
    'Images -> MapType(ActorState, MapType(Direction, ListType(ImageType))),
    'CorpseImages -> ListType(StillImageType) ^ (default=Some(List(someImage)), description="This is what it looks like dead")))

  
  val HerezodSchema = ResourceSchema().
      addValueTypes(RectType, RangeType, PointType, ColorType, ImageType, StillImageType, AnimImageType).
      addSelectOneTypes(ActorState, Race, Direction, TerrainNeighbourCombo, OrthogonalDirection).
      addRefTypes(ActorType, Terrain, NormalTerrain, VerticalTerrain, Tool, DoorTerrain, GraphicFX, SideEffect)
}
