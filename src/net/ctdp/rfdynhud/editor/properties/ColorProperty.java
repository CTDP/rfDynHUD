package net.ctdp.rfdynhud.editor.properties;

import java.awt.Color;

import org.openmali.vecmath2.util.ColorUtils;

import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

public abstract class ColorProperty extends Property
{
    public static final Color FALLBACK_COLOR = Color.MAGENTA;
    
    private final WidgetsConfiguration widgetsConfig;
    
    public final WidgetsConfiguration getWidgetsConfiguration()
    {
        return ( widgetsConfig );
    }
    
    public ColorProperty( String key, boolean readonly, WidgetsConfiguration widgetsConfig )
    {
        super( key, readonly, PropertyEditorType.COLOR, null, null );
        
        this.widgetsConfig = widgetsConfig;
    }
    
    public ColorProperty( String key, WidgetsConfiguration widgetsConfig )
    {
        this( key, false, widgetsConfig );
    }
    
    public static final Color getColorFromColorKey( String colorKey, Color color, WidgetsConfiguration widgetsConfig )
    {
        if ( color == null )
        {
            color = widgetsConfig.getNamedColor( colorKey );
            if ( color == null )
            {
                if ( ( color = ColorUtils.hexToColor( colorKey, false ) ) == null )
                    color = FALLBACK_COLOR;
            }
        }
        
        return ( color );
    }
}
