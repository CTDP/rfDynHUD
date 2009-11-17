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
    
    public final String getWidgetName()
    {
        return ( widgetName );
    }
    
    public final InputAction getAction()
    {
        return ( action );
    }
    
    public InputMapping( String widgetName, InputAction action )
    {
        this.widgetName = widgetName;
        this.action = action;
    }
}
