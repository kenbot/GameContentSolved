package kenbot.gcsolved.editor.gui
import kenbot.gcsolved.resource.types.RefType
import kenbot.gcsolved.editor.gui.widgets.CheckBoxWidget
import kenbot.gcsolved.resource.types.BoolType
import kenbot.gcsolved.resource.types.ValueType
import kenbot.gcsolved.editor.gui.widgets.FieldWidget
import kenbot.gcsolved.editor.gui.widgets.EditScreenWidget
import kenbot.gcsolved.editor.gui.widgets.ComboBoxWidget
import kenbot.gcsolved.editor.gui.widgets.TextFieldWidget
import kenbot.gcsolved.editor.gui.widgets.ChooseTypeWidget
import kenbot.gcsolved.resource.types.StringType
import kenbot.gcsolved.resource.types.SelectOneType
import kenbot.gcsolved.resource.types.ResourceType
import kenbot.gcsolved.resource.ResourceLibrary
import kenbot.gcsolved.resource.Field
import kenbot.gcsolved.resource.types.ListType
import kenbot.gcsolved.editor.gui.widgets.DynamicListWidget
import kenbot.gcsolved.resource.types.FileType
import kenbot.gcsolved.editor.gui.widgets.FileSelectorWidget
import kenbot.gcsolved.editor.Settings
import kenbot.gcsolved.resource.types.IntType

object DefaultMakeWidget {
  def apply(settings: Settings, parentWidget: => Option[FieldWidget] = None, level: Int = 0) = {
    new DefaultMakeWidget(settings, parentWidget, level)
  }
}

class DefaultMakeWidget(settings: Settings, parentWidget: => Option[FieldWidget], level: Int) extends (Field => FieldWidget) {
  private def makeNextWidget(withParent: => FieldWidget) = DefaultMakeWidget(settings, Some(withParent), level+1)
  
  def apply(field: Field): FieldWidget = (field.fieldType: ResourceType) match {
    case IntType => new TextFieldWidget(field, 5, parentWidget, level)
    case StringType => new TextFieldWidget(field, 20, parentWidget, level)
    case BoolType => new CheckBoxWidget(field, parentWidget, level)
    case FileType(_,_*) => new FileSelectorWidget(field, settings.environment, parentWidget, level)
    case rt: RefType => 
      val resourcesOfType = settings.currentLibrary.allResourcesByType(rt).map(_.ref).toSeq
      new ComboBoxWidget(field, resourcesOfType, parentWidget, level)
      
    case s1t: SelectOneType => new ComboBoxWidget(field, s1t.values, parentWidget, level)
    
    case ListType(_, _) => 
      lazy val w: FieldWidget = new DynamicListWidget(field, makeNextWidget(w), parentWidget, level); w
    
    case vt: ValueType if vt.isAbstract => 
      lazy val w: FieldWidget = new ChooseTypeWidget(field, settings.schema, makeNextWidget(w), parentWidget, level); w
      
    case vt: ValueType => 
      lazy val w: FieldWidget = new EditScreenWidget(field, vt.fields.values.toSeq map makeNextWidget(w), parentWidget, level); w
    
    case x => new TextFieldWidget(field, 30, parentWidget, level)
  }
}