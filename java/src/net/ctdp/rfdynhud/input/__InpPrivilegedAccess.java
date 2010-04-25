package net.ctdp.rfdynhud.input;

public class __InpPrivilegedAccess
{
    public static final InputAction createInputAction( String name, Boolean acceptedState, boolean isWidgetAction, InputActionConsumer consumer )
    {
        return ( new InputAction( name, acceptedState, isWidgetAction, consumer ) );
    }
}
