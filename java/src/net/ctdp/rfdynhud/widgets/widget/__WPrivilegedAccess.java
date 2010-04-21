package net.ctdp.rfdynhud.widgets.widget;

import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

public class __WPrivilegedAccess
{
    public static final void setConfiguration( WidgetsConfiguration config, Widget widget )
    {
        widget.setConfiguration( config );
    }
    
    public static final void setLocalStore( Object localStore, Widget widget )
    {
        widget.setLocalStore( localStore );
    }
    
    public static final boolean isInitialized( Widget widget )
    {
        return ( widget.isInitialized() );
    }
    
    public static final void onColorChanged( ColorProperty property, String oldValue, String newValue, Widget widget )
    {
        widget.onColorChanged( property, oldValue, newValue );
    }
    
    public static final void onFontChanged( FontProperty property, String oldValue, String newValue, Widget widget )
    {
        widget.onFontChanged( property, oldValue, newValue );
    }
}
