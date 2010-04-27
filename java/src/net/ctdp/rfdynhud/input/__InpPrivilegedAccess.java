package net.ctdp.rfdynhud.input;

import java.net.URL;

public class __InpPrivilegedAccess
{
    public static final InputAction createInputAction( String name, Boolean acceptedState, boolean isWidgetAction, InputActionConsumer consumer, URL doc )
    {
        return ( new InputAction( name, acceptedState, isWidgetAction, consumer, doc ) );
    }
}
