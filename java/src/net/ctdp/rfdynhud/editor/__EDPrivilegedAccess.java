package net.ctdp.rfdynhud.editor;

import java.io.IOException;

import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;

public class __EDPrivilegedAccess
{
    public static boolean isEditorMode = false;
    
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
