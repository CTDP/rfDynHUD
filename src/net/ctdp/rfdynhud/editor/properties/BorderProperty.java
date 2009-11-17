package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.render.BorderCache;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

public abstract class BorderProperty extends Property
{
    private final WidgetsConfiguration widgetsConfig;
    
    public final WidgetsConfiguration getWidgetsConfiguration()
    {
        return ( widgetsConfig );
    }
    
    public BorderProperty( String key, boolean readonly, WidgetsConfiguration widgetsConfig )
    {
        super( key, readonly, PropertyEditorType.BORDER, null, null );
        
        this.widgetsConfig = widgetsConfig;
    }
    
    public BorderProperty( String key, WidgetsConfiguration widgetsConfig )
    {
        this( key, false, widgetsConfig );
    }
    
    public static final BorderWrapper getBorderFromBorderName( String borderName, BorderWrapper border, WidgetsConfiguration widgetsConfig )
    {
        if ( border == null )
        {
            if ( ( borderName == null ) || borderName.equals( "" ) )
            {
                border = new BorderWrapper( null );
            }
            else
            {
                String borderName_ = widgetsConfig.getBorderName( borderName );
                
                if ( borderName_ == null )
                    border = new BorderWrapper( BorderCache.getTexturedBorder( borderName ) );
                else
                    border = new BorderWrapper( BorderCache.getTexturedBorder( borderName_ ) );
            }
        }
        
        return ( border );
    }
}
