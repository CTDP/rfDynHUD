package net.ctdp.rfdynhud.values;

import net.ctdp.rfdynhud.widgets.widget.Widget;

public class __ValPrivilegedAccess
{
    public static final Position newWidgetPosition( RelativePositioning positioning, float x, boolean xPercent, float y, boolean yPercent, Size size, Widget widget )
    {
        return ( new Position( positioning, x, xPercent, y, yPercent, size, widget, true ) );
    }
    
    public static final Size newWidgetSize( float width, boolean widthPercent, float height, boolean heightPercent, Widget widget )
    {
        return ( new Size( width, widthPercent, height, heightPercent, widget, true ) );
    }
}
