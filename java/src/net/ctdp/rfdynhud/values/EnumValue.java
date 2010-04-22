package net.ctdp.rfdynhud.values;

/**
 * This class is a container for runtime values.
 * You can update the value every time, a Widget is redrawn
 * and compare it with the old (previous) value.
 * 
 * @author Marvin Froehlich
 */
public class EnumValue<E extends Enum<E>>
{
    public static final String N_A_VALUE = "N/A";
    
    private final E resetValue;
    private E oldValue;
    private E value;
    
    public final E getResetValue()
    {
        return ( resetValue );
    }
    
    public final E getOldValue()
    {
        return ( oldValue );
    }
    
    public final E getValue()
    {
        return ( value );
    }
    
    public final boolean hasChanged( boolean setUnchanged )
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
    
    private final boolean update( E newValue, boolean setUnchanged )
    {
        if ( setUnchanged )
            this.oldValue = value;
        this.value = newValue;
        
        return ( hasChanged( false ) );
    }
    
    public final boolean update( E newValue )
    {
        return ( update( newValue, false ) );
    }
    
    public final void setUnchanged()
    {
        update( value, true );
    }
    
    public final EnumValue<E> reset()
    {
        this.value = resetValue;
        
        return ( this );
    }
    
    public final boolean isValid()
    {
        return ( value != null );
    }
    
    public final String getValueAsString()
    {
        return ( value.name() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( value.toString() );
    }
    
    public EnumValue( E resetValue )
    {
        this.resetValue = resetValue;
        
        this.oldValue = resetValue;
        this.value = resetValue;
    }
    
    public EnumValue()
    {
        this( null );
    }
}
