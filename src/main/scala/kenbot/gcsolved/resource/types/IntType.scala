package kenbot.gcsolved.resource.types

import kenbot.gcsolved.resource.meta._

case object IntType extends IntType(None, None) {
  def apply(min: Option[Int] = None, max: Option[Int] = None) = new IntType(min, max)
  
  def unapply(res: ResourceType): Option[(Option[Int], Option[Int])] = res match {
    case i: IntType => Some((i.min, i.max))
    case _ => None
  }
}

class IntType(val min: Option[Int] = None, 
              val max: Option[Int] = None) extends ResourceType("Int") {
  
  def metaType: MetaAnyType = MetaIntType
  
  type Value = Int
  
  override def <:<(other: ResourceType): Boolean = other match {
    case AnyType => true
    case other: IntType => 
      def minConforms = (min, other.min) match {
        case (Some(ourMin), Some(theirMin)) => ourMin >= theirMin
        case (None, Some(_)) => false
        case (_, None) => true
      }
      def maxConforms = (max, other.max) match {
        case (Some(ourMax), Some(theirMax)) => ourMax <= theirMax
        case (None, Some(_)) => false
        case (_, None) => true
      }
      minConforms && maxConforms
      
    case _ => false
  }
  
  
  def asValue(a: Any): Int  = a match {
    case i: Int => i
    case x => a.toString.toInt
  }

  def isTooSmall(i: Int) = min.map(i <) getOrElse false
  def isTooLarge(i: Int) = max.map(i >) getOrElse false
  
  override def getFailures(value: Any): List[String] = {
    def minError(i: Int) = if (isTooSmall(i)) List("Less than minimum value of " + min.get + ": " + value) else Nil
    def maxError(i: Int) = if (isTooLarge(i)) List("Greater than maximum value of " + max.get + ": " + value) else Nil
    
    try {
      val i = asValue(value)
      minError(i) ++ maxError(i)
    }
    catch {
      case _ => List("Not an int value: " + value)
    }
  }
  
  override def equals(a: Any): Boolean = a match {
    case other: IntType => min == other.min && max == other.max
    case _ => false
  }
  override def hashCode = (min, max).hashCode
}
