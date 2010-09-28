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
 * The {@link FactoredFloatProperty} serves for customizing a primitive float value multiplied by a factor.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class FactoredFloatProperty extends FloatProperty
{
    private final float factor;
    
    private float factoredValue;
    
    /**
     * Gets the factor.
     * 
     * @return the factor.
     */
    public final float getFactor()
    {
        return ( factor );
    }
    
    /**
     * This method is utilized to derive the factored value from the property value.
     * 
     * @param value the property value
     * @param factor the factor
     * 
     * @return the derived value.
     */
    protected float deriveValue( float value, float factor )
    {
        return ( value * factor );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setFloatValue( float value )
    {
        if ( super.setFloatValue( value ) )
        {
            this.factoredValue = deriveValue( value, factor );
            
            return ( true );
        }
        
        return ( false );
    }
    
    /**
     * Gets the value multiplied by the factor.
     * 
     * @return the value multiplied by the factor.
     */
    public final float getFactoredValue()
    {
        return ( factoredValue );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param factor the factor
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param readonly
     */
    public FactoredFloatProperty( Widget widget, String name, String nameForDisplay, float factor, float defaultValue, float minValue, float maxValue, boolean readonly )
    {
        super( widget, name, nameForDisplay, defaultValue, minValue, maxValue, readonly );
        
        this.factor = factor;
        
        this.factoredValue = deriveValue( getIntValue(), factor );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param factor the factor
     * @param defaultValue
     * @param minValue
     * @param maxValue
     */
    public FactoredFloatProperty( Widget widget, String name, String nameForDisplay, float factor, float defaultValue, float minValue, float maxValue )
    {
        this( widget, name, nameForDisplay, factor, defaultValue, minValue, maxValue, false );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param factor the factor
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param readonly
     */
    public FactoredFloatProperty( Widget widget, String name, float factor, int defaultValue, float minValue, float maxValue, boolean readonly )
    {
        this( widget, name, null, factor, defaultValue, minValue, maxValue, readonly );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param factor the factor
     * @param defaultValue
     * @param minValue
     * @param maxValue
     */
    public FactoredFloatProperty( Widget widget, String name, float factor, float defaultValue, float minValue, float maxValue )
    {
        this( widget, name, null, factor, defaultValue, minValue, maxValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param factor the factor
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param readonly
     */
    public FactoredFloatProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, float factor, float defaultValue, float minValue, float maxValue, boolean readonly )
    {
        this( (Widget)null, name, nameForDisplay, factor, defaultValue, minValue, maxValue, readonly );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param factor the factor
     * @param defaultValue
     * @param minValue
     * @param maxValue
     */
    public FactoredFloatProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, float factor, float defaultValue, float minValue, float maxValue )
    {
        this( w2pf, name, nameForDisplay, factor, defaultValue, minValue, maxValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param factor the factor
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param readonly
     */
    public FactoredFloatProperty( WidgetToPropertyForwarder w2pf, String name, float factor, float defaultValue, float minValue, float maxValue, boolean readonly )
    {
        this( w2pf, name, null, factor, defaultValue, minValue, maxValue, readonly );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param factor the factor
     * @param defaultValue
     * @param minValue
     * @param maxValue
     */
    public FactoredFloatProperty( WidgetToPropertyForwarder w2pf, String name, float factor, float defaultValue, float minValue, float maxValue )
    {
        this( w2pf, name, null, factor, defaultValue, minValue, maxValue, false );
    }
}
