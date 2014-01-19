/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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
package net.ctdp.rfdynhud.properties;


/**
 * The {@link FactoredIntProperty} serves for customizing a primitive int value multiplied or divided by a factor.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class FactoredIntProperty extends IntProperty
{
    private final int factor;
    private final int divisor;
    
    private long factoredValue;
    
    /**
     * Gets the factor.
     * This is zero, if divisor is used.
     * @see #getDivisor()
     * 
     * @return the factor.
     */
    public final int getFactor()
    {
        return ( factor );
    }
    
    /**
     * Gets the divisor.
     * This is zero, if factor is used.
     * @see #getFactor()
     * 
     * @return the divisor.
     */
    public final int getDivisor()
    {
        return ( divisor );
    }
    
    /**
     * This method is utilized to derive the factored value from the property value.
     * 
     * @param value the property value
     * @param factor the factor (zero, if divisor is used)
     * @param divisor the divisor (zero, if factor is used)
     * 
     * @return the derived value.
     */
    protected long deriveValue( int value, int factor, int divisor )
    {
        if ( divisor == 0 )
            return ( (long)value * (long)factor );
        
        return ( (long)value / (long)divisor );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    void onValueSet( int value )
    {
        super.onValueSet( value );
        
        this.factoredValue = deriveValue( value, factor, divisor );
    }
    
    /**
     * Gets the value multiplied by the factor or divided by the divisor.
     * 
     * @return the value multiplied by the factor or divided by the divisor.
     */
    public final long getFactoredValue()
    {
        return ( factoredValue );
    }
    
    /**
     * Gets the value multiplied by the factor or divided by the divisor.
     * 
     * @return the value multiplied by the factor or divided by the divisor.
     */
    public final double getFactoredDoubleValue()
    {
        return ( factoredValue );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param readonly read only property?
     */
    public FactoredIntProperty( String name, String nameForDisplay, int factor, int divisor, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        super( name, nameForDisplay, defaultValue, minValue, maxValue, readonly, false );
        
        if ( ( factor == 0 ) && ( divisor == 0 ) )
            throw new IllegalArgumentException( "factor and divisor cannot be both zero." );
        
        this.factor = factor;
        this.divisor = divisor;
        
        setIntValue( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     */
    public FactoredIntProperty( String name, String nameForDisplay, int factor, int divisor, int defaultValue, int minValue, int maxValue )
    {
        this( name, nameForDisplay, factor, divisor, defaultValue, minValue, maxValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param readonly read only property?
     */
    public FactoredIntProperty( String name, int factor, int divisor, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        this( name, null, factor, divisor, defaultValue, minValue, maxValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     */
    public FactoredIntProperty( String name, int factor, int divisor, int defaultValue, int minValue, int maxValue )
    {
        this( name, null, factor, divisor, defaultValue, minValue, maxValue, false );
    }
}
