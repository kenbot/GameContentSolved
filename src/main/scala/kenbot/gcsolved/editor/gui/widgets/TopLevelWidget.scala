package kenbot.gcsolved.editor.gui.widgets
import kenbot.gcsolved.resource.Field.symbolAndType2Field
import kenbot.gcsolved.resource.types.RefType
import scala.swing.BoxPanel
import scala.swing.Orientation
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.editor.gui.util.NestedBorderPanel


/*
class TopLevelWidget(val refType: RefType, makeWidget: Field => FieldWidget) extends DefaultFieldWidget('top -> refType, None, -1) {
  
  lazy val editor = new NestedBorderPanel with MyEditorMixin {
    north = new BoxPanel(Orientation.Vertical){
      contents ++= subWidgets.map(w => new WidgetDecoratorPanel(w))
    }
  }
  
  override val subWidgets: Seq[FieldWidget] = (refType.fields.values map makeWidget).toList
  listenTo(subWidgets: _*)
  println(subWidgets.size)
 
  def rawFieldValue: Option[Any] = sys.error("Top level value is not used.")
  def fieldValue_=(value: Option[Any]): Unit = sys.error("Top level value is not used.")
}*/