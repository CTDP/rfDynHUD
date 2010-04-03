package net.ctdp.rfdynhud.editor;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;

public class EDPrivilegedAccess
{
    public static final void getEditorPresetsProperties( EditorPresets editorPresets, FlaggedList props )
    {
        editorPresets.getProperties( props );
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
