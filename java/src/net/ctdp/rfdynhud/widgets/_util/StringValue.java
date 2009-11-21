package net.ctdp.rfdynhud.widgets._util;

/**
 * This class is a container for runtime values.
 * You can update the value every time, a Widget is redrawn
 * and compare it with the old (previous) value.
 * 
 * @author Marvin Froehlich
 */
public class StringValue
{
    public static final String DEFAULT_RESET_VALUE = null;
    public static final String N_A_VALUE = "N/A";
    
    private final String resetValue;
    private String oldValue;
    private String value;
    
    public final String getResetValue()
    {
        return ( resetValue );
    }
    
    public final String getOldValue()
    {
        return ( oldValue );
    }
    
    public final String getValue()
    {
        return ( value );
    }
    
    public final boolean hasChanged( boolean setUnchanged )
    {
        boolean result = ( value == null ) ? ( oldValue != null ) : !value.equals( oldValue );
        
        if ( result && setUnchanged )
            update( value, true );
        
        return ( result );
    }
    
    public final boolean hasChanged()
    {
        return ( hasChanged( true ) );
    }
    
    private final boolean update( String newValue, boolean forceSetUnchanged )
    {
        if ( forceSetUnchanged || !hasChanged( false ) )
            this.oldValue = value;
        this.value = newValue;
        
        return ( hasChanged( false ) );
    }
    
    public final boolean update( String newValue )
    {
        return ( update( newValue, false ) );
    }
    
    public final void setUnchanged()
    {
        update( value, true );
    }
    
    public final StringValue reset()
    {
        this.value = resetValue;
        
        return ( this );
    }
    
    public final boolean isValid()
    {
        return ( ( value != null ) && !value.equals( resetValue ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( getValue() );
    }
    
    public StringValue( String resetValue )
    {
        this.resetValue = resetValue;
        
        this.oldValue = resetValue;
        this.value = resetValue;
    }
    
    public StringValue()
    {
        this( DEFAULT_RESET_VALUE );
    }
}
