package net.ctdp.rfdynhud.widgets.widget;

import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.values.RelativePositioning;
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
    
    public static final void onPropertyChanged( Property property, Object oldValue, Object newValue, Widget widget )
    {
        widget.onPropertyChanged( property, oldValue, newValue );
    }
    
    public static final void onPositionChanged( RelativePositioning oldPositioning, int oldX, int oldY, RelativePositioning newPositioning, int newX, int newY, Widget widget )
    {
        widget.onPositionChanged( oldPositioning, oldX, oldY, newPositioning, newX, newY );
    }
    
    public static final void onSizeChanged( int oldWidth, int oldHeight, int newWidth, int newHeight, Widget widget )
    {
        widget.onSizeChanged( oldWidth, oldHeight, newWidth, newHeight );
    }
    
    public static final boolean needsCompleteClear( Widget widget )
    {
        return ( widget.needsCompleteClear() );
    }
}
