package net.ctdp.rfdynhud.widgets;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.input.InputMappings;

public class __WCPrivilegedAccess
{
    public static final void setInputMappings( WidgetsConfiguration config, InputMappings inputMappings )
    {
        config.setInputMappings( inputMappings );
    }
    
    public static final void sortWidgets( WidgetsConfiguration config )
    {
        config.sortWidgets();
    }
    
    public static final void setJustLoaded( WidgetsConfiguration config, LiveGameData gameData, EditorPresets editorPresets )
    {
        config.setJustLoaded( gameData, editorPresets );
    }
    
    public static final boolean setGameResolution( int gameResX, int gameResY, WidgetsConfiguration widgetsConfig )
    {
        return ( widgetsConfig.setGameResolution( gameResX, gameResY ) );
    }
}
