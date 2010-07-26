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
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class FloatProperty extends Property
{
    private float value;
    
    private final float minValue;
    private final float maxValue;
    
    public final float getMinValue()
    {
        return ( minValue );
    }
    
    public final float getMaxValue()
    {
        return ( maxValue );
    }
    
    protected float fixValue( float value )
    {
        return ( Math.max( minValue, Math.min( value, maxValue ) ) );
    }
    
    /**
     * 
     * @param oldValue
     * @param newValue
     */
    protected void onValueChanged( float oldValue, float newValue )
    {
    }
    
    public void setFloatValue( float value )
    {
        value = fixValue( value );
        
        if ( value == this.value )
            return;
        
        float oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, value, widget );
    }
    
    public final float getFloatValue()
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setFloatValue( ( (Number)value ).floatValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( getFloatValue() );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            setValue( Float.parseFloat( value ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    public FloatProperty( Widget widget, String propertyName, String nameForDisplay, float defaultValue, float minValue, float maxValue, boolean readonly )
    {
        super( widget, propertyName, nameForDisplay, readonly, PropertyEditorType.FLOAT, null, null );
        
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public FloatProperty( Widget widget, String propertyName, String nameForDisplay, float defaultValue, boolean readonly )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, -Float.MAX_VALUE, +Float.MAX_VALUE, readonly );
    }
    
    public FloatProperty( Widget widget, String propertyName, String nameForDisplay, float defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public FloatProperty( Widget widget, String propertyName, float defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public FloatProperty( Widget widget, String propertyName, float defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
    
    public FloatProperty( Widget widget, String propertyName, float defaultValue, float minValue, float maxValue )
    {
        this( widget, propertyName, propertyName, defaultValue, minValue, maxValue, false );
    }
}
