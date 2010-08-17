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
package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link FactoredIntProperty} serves for customizing a primitive int value multiplied or divided by a factor.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class FactoredIntProperty extends IntProperty
{
    private final int factor;
    private final int divisor;
    
    private int factoredValue;
    
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
    protected int deriveValue( int value, int factor, int divisor )
    {
        if ( divisor == 0 )
            return ( value * factor );
        
        return ( value / divisor );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onValueChanged( int oldValue, int newValue )
    {
        super.onValueChanged( oldValue, newValue );
        
        this.factoredValue = deriveValue( newValue, factor, divisor );
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
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param readonly
     */
    public FactoredIntProperty( Widget widget, String name, String nameForDisplay, int factor, int divisor, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        super( widget, name, nameForDisplay, defaultValue, minValue, maxValue, readonly );
        
        if ( ( factor == 0 ) && ( divisor == 0 ) )
            throw new IllegalArgumentException( "factor and divisor cannot be both zero." );
        
        this.factor = factor;
        this.divisor = divisor;
        
        this.factoredValue = deriveValue( getIntValue(), factor, divisor );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue
     * @param minValue
     * @param maxValue
     */
    public FactoredIntProperty( Widget widget, String name, String nameForDisplay, int factor, int divisor, int defaultValue, int minValue, int maxValue )
    {
        this( widget, name, nameForDisplay, factor, divisor, defaultValue, minValue, maxValue, false );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param readonly
     */
    public FactoredIntProperty( Widget widget, String name, int factor, int divisor, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        this( widget, name, null, factor, divisor, defaultValue, minValue, maxValue, readonly );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue
     * @param minValue
     * @param maxValue
     */
    public FactoredIntProperty( Widget widget, String name, int factor, int divisor, int defaultValue, int minValue, int maxValue )
    {
        this( widget, name, null, factor, divisor, defaultValue, minValue, maxValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param readonly
     */
    public FactoredIntProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, int factor, int divisor, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        this( (Widget)null, name, nameForDisplay, factor, divisor, defaultValue, minValue, maxValue, readonly );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue
     * @param minValue
     * @param maxValue
     */
    public FactoredIntProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, int factor, int divisor, int defaultValue, int minValue, int maxValue )
    {
        this( w2pf, name, nameForDisplay, factor, divisor, defaultValue, minValue, maxValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param readonly
     */
    public FactoredIntProperty( WidgetToPropertyForwarder w2pf, String name, int factor, int divisor, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        this( w2pf, name, null, factor, divisor, defaultValue, minValue, maxValue, readonly );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param factor the factor (0 for divisor usage)
     * @param divisor the divisor (0 for factor usage)
     * @param defaultValue
     * @param minValue
     * @param maxValue
     */
    public FactoredIntProperty( WidgetToPropertyForwarder w2pf, String name, int factor, int divisor, int defaultValue, int minValue, int maxValue )
    {
        this( w2pf, name, null, factor, divisor, defaultValue, minValue, maxValue, false );
    }
}
