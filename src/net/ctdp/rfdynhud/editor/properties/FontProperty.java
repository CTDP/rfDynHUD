package net.ctdp.rfdynhud.editor.properties;

import java.awt.Font;

import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.FontUtils;

public abstract class FontProperty extends Property
{
    private final WidgetsConfiguration widgetsConfig;
    
    public final WidgetsConfiguration getWidgetsConfiguration()
    {
        return ( widgetsConfig );
    }
    
    public FontProperty( String key, boolean readonly, WidgetsConfiguration widgetsConfig )
    {
        super( key, readonly, PropertyEditorType.FONT, null, null );
        
        this.widgetsConfig = widgetsConfig;
    }
    
    public FontProperty( String key, WidgetsConfiguration widgetsConfig )
    {
        this( key, false, widgetsConfig );
    }
    
    public static final Font getFontFromFontKey( String fontKey, Font font, WidgetsConfiguration widgetsConfig )
    {
        if ( font == null )
        {
            font = widgetsConfig.getNamedFont( fontKey );
            if ( font == null )
                //font = Font.decode( fontKey );
                font = FontUtils.parseFont( fontKey, widgetsConfig.getGameResY() );
        }
        
        return ( font );
    }
}
