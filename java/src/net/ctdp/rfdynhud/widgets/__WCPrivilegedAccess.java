package net.ctdp.rfdynhud.widgets;

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
    
    public static final void setJustLoaded( WidgetsConfiguration config, LiveGameData gameData )
    {
        config.setJustLoaded( gameData );
    }
    
    public static final void setGameResolution( int gameResX, int gameResY, WidgetsConfiguration widgetsConfig )
    {
        widgetsConfig.setGameResolution( gameResX, gameResY );
    }
}
