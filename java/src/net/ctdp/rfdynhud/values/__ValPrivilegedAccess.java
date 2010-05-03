package net.ctdp.rfdynhud.values;

import net.ctdp.rfdynhud.widgets.widget.Widget;

public class __ValPrivilegedAccess
{
    public static final Position newWidgetPosition( RelativePositioning positioning, float x, float y, Size size, Widget widget )
    {
        return ( new Position( positioning, x, y, size, widget, true ) );
    }
    
    public static final Size newWidgetSize( float w, float h, Widget widget )
    {
        return ( new Size( w, h, widget, true ) );
    }
}
