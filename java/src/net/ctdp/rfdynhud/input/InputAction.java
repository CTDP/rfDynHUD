package net.ctdp.rfdynhud.input;

/**
 * This is a simple abstraction of an input action.
 * 
 * @author Marvin Froehlich
 */
public class InputAction implements Comparable<InputAction>
{
    public static final int MODIFIER_MASK_SHIFT = 1;
    public static final int MODIFIER_MASK_CTRL  = 2;
    public static final int MODIFIER_MASK_LALT  = 4;
    public static final int MODIFIER_MASK_RALT  = 8;
    public static final int MODIFIER_MASK_LMETA = 16;
    public static final int MODIFIER_MASK_RMETA = 32;
    
    private static int nextId = 1;
    
    private final int id;
    private final String name;
    private final Boolean acceptedState;
    private final boolean isWidgetAction;
    private final InputActionConsumer consumer;
    
    public final int getID()
    {
        return ( id );
    }
    
    public final String getName()
    {
        return ( name );
    }
    
    public final Boolean getAcceptedState()
    {
        return ( acceptedState );
    }
    
    public final boolean isWidgetAction()
    {
        return ( isWidgetAction );
    }
    
    public final InputActionConsumer getConsumer()
    {
        return ( consumer );
    }
    
    @Override
    public int compareTo( InputAction o )
    {
        if ( o == null )
            return ( +1 );
        
        if ( o.id == this.id )
            return ( 0 );
        
        return ( String.CASE_INSENSITIVE_ORDER.compare( this.name, o.name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( o == this )
            return ( true );
        
        if ( !( o instanceof InputAction ) )
            return ( false );
        
        return ( this.id == ( (InputAction)o ).getID() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ( id );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( getName() );
    }
    
    public String toString2()
    {
        return ( getName() + " (ID=" + getID() + ")" );
    }
    
    InputAction( String name, Boolean acceptedState, boolean isWidgetAction, InputActionConsumer consumer )
    {
        this.id = nextId++;
        
        this.name = name;
        this.acceptedState = acceptedState;
        this.isWidgetAction = isWidgetAction;
        this.consumer = consumer;
    }
    
    /**
     * Creates a new InputAction.
     * 
     * @param name the action's name as displayed in the editor
     * @param acceptedState null to accept any state, true or false to accept only this state
     */
    public InputAction( String name, Boolean acceptedState )
    {
        this( name, acceptedState, true, null );
    }
    
    /**
     * Creates a new InputAction.
     * It will only accept true state.
     * 
     * @param name the action's name as displayed in the editor
     */
    public InputAction( String name )
    {
        this( name, true, true, null );
    }
}
