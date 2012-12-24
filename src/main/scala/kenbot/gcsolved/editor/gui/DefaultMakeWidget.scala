package kenbot.gcsolved.editor.gui

import kenbot.gcsolved.core.{Field, ResourceRef}
import kenbot.gcsolved.core.types.{BoolType, FileType, IntType, ListType, MapType, RefType, ResourceType, SelectOneType, StringType, ValueType}
import kenbot.gcsolved.editor.Settings
import kenbot.gcsolved.editor.gui.widgets.{CheckBoxWidget, ChooseTypeWidget, ComboBoxWidget, DynamicListWidget, EditScreenWidget, FieldWidget, FileSelectorWidget, MapWidget, TextFieldWidget}

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
      new ComboBoxWidget[ResourceRef](field, resourcesOfType, _.id, parentWidget, level)
      
    case s1t: SelectOneType => new ComboBoxWidget[s1t.Value](field, s1t.values, _.toString, parentWidget, level)
    
    case ListType(_, _) => 
      lazy val w: FieldWidget = new DynamicListWidget(field, makeNextWidget(w), parentWidget, level); w
    
    case MapType(_, _) => 
      lazy val w: FieldWidget = new MapWidget(field, makeNextWidget(w), parentWidget, level); w
      
    case vt: ValueType if vt.isAbstract => 
      lazy val w: FieldWidget = new ChooseTypeWidget(field, settings.schema, makeNextWidget(w), parentWidget, level); w
      
    case vt: ValueType => 
      lazy val w: FieldWidget = new EditScreenWidget(field, vt.fields.values.toSeq map makeNextWidget(w), parentWidget, level); w
    
    case x => new TextFieldWidget(field, 30, parentWidget, level)
  }
}