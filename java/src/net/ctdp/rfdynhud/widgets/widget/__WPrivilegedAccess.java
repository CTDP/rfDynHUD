package net.ctdp.rfdynhud.widgets.widget;

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
}
