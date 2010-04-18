package net.ctdp.rfdynhud.input;

/**
 * The {@link InputMapping} keeps information about a single mapped input component to a Widget.
 * 
 * @author Marvin Froehlich
 */
public class InputMapping
{
    private final String widgetName;
    private final InputAction action;
    private final String deviceComponent;
    
    public final String getWidgetName()
    {
        return ( widgetName );
    }
    
    public final InputAction getAction()
    {
        return ( action );
    }
    
    public final String getDeviceComponent()
    {
        return ( deviceComponent );
    }
    
    public InputMapping( String widgetName, InputAction action, String deviceComponent )
    {
        this.widgetName = widgetName;
        this.action = action;
        this.deviceComponent = deviceComponent;
    }
}
