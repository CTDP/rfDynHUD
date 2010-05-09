package net.ctdp.rfdynhud.values;

/**
 * This class is a container for runtime values.
 * You can update the value every time, a Widget is redrawn
 * and compare it with the old (previous) value.
 * 
 * @author Marvin Froehlich
 */
public class LongValue
{
    public static final long DEFAULT_RESET_VALUE = -1L;
    public static final String N_A_VALUE = "N/A";
    
    private final long resetValue;
    private final ValidityTest validityTest;
    private final long validityCompareValue;
    private boolean oldValidity;
    private long oldValue;
    private long value;
    
    public final long getResetValue()
    {
        return ( resetValue );
    }
    
    public final ValidityTest getValdidityTest()
    {
        return ( validityTest );
    }
    
    public final long getValidityCompareValue()
    {
        return ( validityCompareValue );
    }
    
    public final long getOldValue()
    {
        return ( oldValue );
    }
    
    public final long getValue()
    {
        return ( value );
    }
    
    public final int getIntValue()
    {
        return ( (int)value );
    }
    
    public final float getFloatValue()
    {
        return ( (float)value );
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
    
    private final boolean update( long newValue, boolean setUnchanged )
    {
        this.oldValidity = isValid();
        
        if ( setUnchanged )
            this.oldValue = value;
        this.value = newValue;
        
        return ( hasChanged( false ) );
    }
    
    public final boolean update( long newValue )
    {
        return ( update( newValue, false ) );
    }
    
    public final void setUnchanged()
    {
        update( value, true );
    }
    
    public final LongValue reset( boolean resetOldValue )
    {
        this.value = resetValue;
        
        if ( resetOldValue )
            oldValue = resetValue;
        
        this.oldValidity = false;
        
        return ( this );
    }
    
    public final LongValue reset()
    {
        return ( reset( false ) );
    }
    
    public final boolean isValid()
    {
        switch ( validityTest )
        {
            case EQUALS:
                return ( value == validityCompareValue );
            case NOT_EQUALS:
                return ( value != validityCompareValue );
            case GREATER_THAN:
                return ( value > validityCompareValue );
            case GRATER_THAN_OR_EQUALS:
                return ( value >= validityCompareValue );
            case LESS_THAN:
                return ( value < validityCompareValue );
            case LESS_THAN_OR_EQUALS:
                return ( value <= validityCompareValue );
        }
        
        return ( false );
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
    
    public LongValue( long resetValue, ValidityTest validityTest, long validityCompareValue )
    {
        this.resetValue = resetValue;
        
        this.validityTest = validityTest;
        this.validityCompareValue = validityCompareValue;
        this.oldValidity = false;
        
        this.oldValue = resetValue;
        this.value = resetValue;
    }
    
    public LongValue( long resetValue )
    {
        this( resetValue, ValidityTest.GRATER_THAN_OR_EQUALS, 0 );
    }
    
    public LongValue( ValidityTest validityTest, long validityCompareValue )
    {
        this( DEFAULT_RESET_VALUE, validityTest, validityCompareValue );
    }
    
    public LongValue()
    {
        this( DEFAULT_RESET_VALUE );
    }
}
