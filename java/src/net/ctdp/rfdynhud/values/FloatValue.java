/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.values;

/**
 * This class is a container for runtime values.
 * You can update the value every time, a Widget is redrawn
 * and compare it with the old (previous) value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class FloatValue
{
    public static final float DEFAULT_RESET_VALUE = -1f;
    public static final float DEFAULT_COMPARE_PRECISION = 0.001f;
    public static final String N_A_VALUE = "N/A";
    
    private final float resetValue;
    private final float comparePrecision;
    private final ValidityTest validityTest;
    private final float validityCompareValue;
    private boolean oldValidity;
    private float oldValue;
    private float value;
    private boolean isResetValue;
    
    public final float getResetValue()
    {
        return ( resetValue );
    }
    
    public final float getComparePrecision()
    {
        return ( comparePrecision );
    }
    
    public final ValidityTest getValdidityTest()
    {
        return ( validityTest );
    }
    
    public final float getValidityCompareValue()
    {
        return ( validityCompareValue );
    }
    
    public final float getOldValue()
    {
        return ( oldValue );
    }
    
    public final float getValue()
    {
        return ( value );
    }
    
    public final int getIntValue( boolean round )
    {
        if ( round )
            return ( Math.round( value ) );
        
        return ( (int)value );
    }
    
    public final int getIntValue()
    {
        return ( getIntValue( false ) );
    }
    
    public final boolean hasChanged( boolean setUnchanged )
    {
        boolean result = ( Math.abs( oldValue - value ) > comparePrecision );
        
        if ( result && setUnchanged )
            update( value, true );
        
        return ( result );
    }
    
    public final boolean hasChanged()
    {
        return ( hasChanged( true ) );
    }
    
    private final boolean update( float newValue, boolean setUnchanged )
    {
        this.oldValidity = isValid();
        
        if ( setUnchanged )
            this.oldValue = value;
        this.value = newValue;
        
        this.isResetValue = false;
        
        return ( hasChanged( false ) );
    }
    
    public final boolean update( float newValue )
    {
        return ( update( newValue, false ) );
    }
    
    public final void setUnchanged()
    {
        update( value, true );
    }
    
    public final FloatValue reset( boolean resetOldValue )
    {
        this.value = resetValue;
        this.isResetValue = true;
        
        if ( resetOldValue )
            oldValue = resetValue;
        
        this.oldValidity = false;
        
        return ( this );
    }
    
    public final FloatValue reset()
    {
        return ( reset( false ) );
    }
    
    public final boolean isValid()
    {
        switch ( validityTest )
        {
            case EQUALS:
                return ( Math.abs( value - validityCompareValue ) <= comparePrecision );
            case NOT_EQUALS:
                return ( Math.abs( value - validityCompareValue ) > comparePrecision );
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
    
    public final String getValueAsStringWithSign()
    {
        if ( isResetValue )
            return ( N_A_VALUE );
        
        if ( value > 0f )
            return ( "+" + String.valueOf( value ) );
        
        if ( value == -0f )
            return ( "0" ); // avoid "-0"
        
        return ( String.valueOf( value ) );
    }
    
    public final String getValueAsStringWithSign( boolean round )
    {
        if ( isResetValue )
            return ( N_A_VALUE );
        
        if ( round )
        {
            int value_ = Math.round( value );
            
            if ( value_ == -0 )
                return ( "0" ); // avoid "-0"
            
            if ( value_ > 0 )
                return ( "+" + String.valueOf( value_ ) );
            
            return ( String.valueOf( value_ ) );
        }
        
        if ( value == -0f )
            return ( "0" ); // avoid "-0"
        
        if ( value > 0f )
            return ( "+" + String.valueOf( value ) );
        
        return ( String.valueOf( value ) );
    }
    
    public final String getValueAsStringWithSign( float factor, boolean round )
    {
        if ( isResetValue )
            return ( N_A_VALUE );
        
        if ( round )
        {
            int value_ = Math.round( value * factor );
            
            if ( value_ == -0 )
                return ( "0" ); // avoid "-0"
            
            if ( value_ > 0 )
                return ( "+" + String.valueOf( value_ ) );
            
            return ( String.valueOf( value_ ) );
        }
        
        float value_ = value * factor;
        
        if ( value_ == -0f )
            return ( "0" ); // avoid "-0"
        
        if ( value_ > 0f )
            return ( "+" + String.valueOf( value_ ) );
        
        return ( String.valueOf( value_ ) );
    }
    
    public final String getValueAsStringWithSign( int precision )
    {
        if ( isResetValue )
            return ( N_A_VALUE );
        
        if ( precision < 0 )
            return ( "ERROR" );
        
        float value_ = value;
        if ( value_ == -0f )
            value_ = 0f;
        
        String sign = ( value_ > 0f ) ? "+" : "";
        
        switch ( precision )
        {
            case 0:
                return ( sign + String.valueOf( Math.round( value ) ) );
            case 1:
                return ( sign + String.valueOf( Math.round( value * 10f ) / 10f ) );
            case 2:
                return ( sign + String.valueOf( Math.round( value * 100f ) / 100f ) );
            case 3:
                return ( sign + String.valueOf( Math.round( value * 1000f ) / 1000f ) );
            case 4:
                return ( sign + String.valueOf( Math.round( value * 10000f ) / 10000f ) );
            case 5:
                return ( sign + String.valueOf( Math.round( value * 100000f ) / 100000f ) );
            case 6:
                return ( sign + String.valueOf( Math.round( value * 1000000f ) / 1000000f ) );
        }
        
        return ( sign + String.valueOf( value ) );
    }
    
    public final String getValueAsString()
    {
        if ( isResetValue )
            return ( N_A_VALUE );
        
        if ( value == -0f )
            return ( "0" ); // avoid "-0"
        
        return ( String.valueOf( value ) );
    }
    
    public final String getValueAsString( boolean round )
    {
        if ( isResetValue )
            return ( N_A_VALUE );
        
        if ( round )
            return ( String.valueOf( Math.round( value ) ) );
        
        return ( String.valueOf( value ) );
    }
    
    public final String getValueAsString( float factor, boolean round )
    {
        if ( isResetValue )
            return ( N_A_VALUE );
        
        if ( round )
            return ( String.valueOf( Math.round( value * factor ) ) );
        
        return ( String.valueOf( value * factor ) );
    }
    
    public final String getValueAsString( int precision )
    {
        if ( isResetValue )
            return ( N_A_VALUE );
        
        if ( precision < 0 )
            return ( "ERROR" );
        
        switch ( precision )
        {
            case 0:
                return ( String.valueOf( Math.round( value ) ) );
            case 1:
                return ( String.valueOf( Math.round( value * 10f ) / 10f ) );
            case 2:
                return ( String.valueOf( Math.round( value * 100f ) / 100f ) );
            case 3:
                return ( String.valueOf( Math.round( value * 1000f ) / 1000f ) );
            case 4:
                return ( String.valueOf( Math.round( value * 10000f ) / 10000f ) );
            case 5:
                return ( String.valueOf( Math.round( value * 100000f ) / 100000f ) );
            case 6:
                return ( String.valueOf( Math.round( value * 1000000f ) / 1000000f ) );
        }
        
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
    
    public FloatValue( float resetValue, float comparePrecision, ValidityTest validityTest, float validityCompareValue )
    {
        this.resetValue = resetValue;
        
        this.comparePrecision = comparePrecision;
        
        this.validityTest = validityTest;
        this.validityCompareValue = validityCompareValue;
        this.oldValidity = false;
        
        this.oldValue = resetValue;
        this.value = resetValue;
        
        this.isResetValue = true;
    }
    
    public FloatValue( float resetValue, float comparePrecision )
    {
        this( resetValue, comparePrecision, ValidityTest.GRATER_THAN_OR_EQUALS, 0f );
    }
    
    public FloatValue( float resetValue, ValidityTest validityTest, float validityCompareValue )
    {
        this( resetValue, DEFAULT_COMPARE_PRECISION, validityTest, validityCompareValue );
    }
    
    public FloatValue( float resetValue )
    {
        this( resetValue, DEFAULT_COMPARE_PRECISION );
    }
    
    public FloatValue( ValidityTest validityTest, float validityCompareValue )
    {
        this( DEFAULT_RESET_VALUE, DEFAULT_COMPARE_PRECISION, validityTest, validityCompareValue );
    }
    
    public FloatValue()
    {
        this( DEFAULT_RESET_VALUE, DEFAULT_COMPARE_PRECISION );
    }
}
