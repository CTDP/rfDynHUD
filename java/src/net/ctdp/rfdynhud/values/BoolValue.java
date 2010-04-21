package net.ctdp.rfdynhud.values;

/**
 * This class is a container for runtime values.
 * You can update the value every time, a Widget is redrawn
 * and compare it with the old (previous) value.
 * 
 * @author Marvin Froehlich
 */
public class BoolValue
{
    public static final Boolean DEFAULT_RESET_VALUE = null;
    public static final String N_A_VALUE = "N/A";
    
    private final Boolean resetValue;
    private boolean oldValidity = false;
    private Boolean oldValue;
    private Boolean value;
    
    public final Boolean getResetValue()
    {
        return ( resetValue );
    }
    
    public final Boolean getOldValue()
    {
        return ( oldValue );
    }
    
    public final Boolean getValue()
    {
        return ( value );
    }
    
    private final boolean hasChanged( boolean setUnchanged )
    {
        boolean result = ( value != oldValue );
        
        if ( result && setUnchanged )
            update( value, true );
        
        return ( result );
    }
    
    public final boolean hasChanged()
    {
        return ( hasChanged( true ) );
    }
    
    private final boolean update( boolean newValue, boolean setUnchanged )
    {
        this.oldValidity = isValid();
        
        if ( setUnchanged )
            this.oldValue = value;
        this.value = newValue;
        
        return ( hasChanged( false ) );
    }
    
    public final boolean update( boolean newValue )
    {
        return ( update( newValue, false ) );
    }
    
    public final void setUnchanged()
    {
        update( value, true );
    }
    
    public final BoolValue reset()
    {
        this.value = resetValue;
        
        this.oldValidity = false;
        
        return ( this );
    }
    
    public final boolean isValid()
    {
        return ( value != null );
    }
    
    public final boolean hasValidityChanged()
    {
        return ( isValid() != oldValidity );
    }
    
    public final String getValueAsString()
    {
        if ( value == resetValue )
            return ( N_A_VALUE );
        
        return ( String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( getValueAsString() );
    }
    
    public BoolValue( Boolean resetValue )
    {
        this.resetValue = resetValue;
        
        this.oldValidity = false;
        
        this.oldValue = resetValue;
        this.value = resetValue;
    }
    
    public BoolValue()
    {
        this( DEFAULT_RESET_VALUE );
    }
}
