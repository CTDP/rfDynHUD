package net.ctdp.rfdynhud.editor;

import java.io.IOException;

import net.ctdp.rfdynhud.widgets._util.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;

public class EDPrivilegedAccess
{
    public static final void getEditorPresetsProperties( EditorPresets editorPresets, WidgetPropertiesContainer propsCont )
    {
        editorPresets.getProperties( propsCont );
    }
    
    public static final void saveProperties( EditorPresets editorPresets, WidgetsConfigurationWriter writer ) throws IOException
    {
        editorPresets.saveProperties( writer );
    }
    
    public static final void loadProperty( EditorPresets editorPresets, String key, String value )
    {
        editorPresets.loadProperty( key, value );
    }
}
