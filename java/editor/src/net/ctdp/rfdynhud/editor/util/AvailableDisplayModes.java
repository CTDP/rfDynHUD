package net.ctdp.rfdynhud.editor.util;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.HashMap;

public class AvailableDisplayModes
{
    private static final HashMap<String, java.awt.DisplayMode> displayModes = new HashMap<String, java.awt.DisplayMode>();
    static
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for ( DisplayMode dm : ge.getDefaultScreenDevice().getDisplayModes() )
        {
            if ( ( dm.getWidth() >= 800 ) && ( dm.getWidth() >= 600 ) && ( dm.getBitDepth() == 32 ) )
            {
                String key = dm.getWidth() + "x" + dm.getHeight();
                
                DisplayMode dm2 = displayModes.get( key );
                if ( dm2 == null )
                {
                    displayModes.put( key, dm );
                }
                else
                {
                    if ( dm.getRefreshRate() > dm2.getRefreshRate() )
                    {
                        displayModes.remove( key );
                        displayModes.put( key, dm );
                    }
                }
            }
        }
    }
    
    public static final java.awt.DisplayMode getDisplayMode( String resString )
    {
        return ( displayModes.get( resString ) );
    }
    
    public static final Collection<java.awt.DisplayMode> getAll()
    {
        return ( displayModes.values() );
    }
}
