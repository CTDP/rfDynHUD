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

public class IntProperty extends Property
{
    private int value;
    
    private final int minValue;
    private final int maxValue;
    
    public final int getMinValue()
    {
        return ( minValue );
    }
    
    public final int getMaxValue()
    {
        return ( maxValue );
    }
    
    protected int fixValue( int value )
    {
        return ( Math.max( minValue, Math.min( value, maxValue ) ) );
    }
    
    /**
     * 
     * @param oldValue
     * @param newValue
     */
    protected void onValueChanged( int oldValue, int newValue )
    {
    }
    
    public void setIntValue( int value )
    {
        value = fixValue( value );
        
        if ( value == this.value )
            return;
        
        int oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, value, widget );
    }
    
    public final int getIntValue()
    {
        return ( value );
    }
    
    public final float getFloatValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setIntValue( ( (Number)value ).intValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( getIntValue() );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            setValue( Integer.parseInt( value ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    public IntProperty( Widget widget, String propertyName, String nameForDisplay, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        super( widget, propertyName, nameForDisplay, readonly, PropertyEditorType.INTEGER, null, null );
        
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public IntProperty( Widget widget, String propertyName, String nameForDisplay, int defaultValue, boolean readonly )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, readonly );
    }
    
    public IntProperty( Widget widget, String propertyName, String nameForDisplay, int defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public IntProperty( Widget widget, String propertyName, int defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public IntProperty( Widget widget, String propertyName, int defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
    
    public IntProperty( Widget widget, String propertyName, int defaultValue, int minValue, int maxValue )
    {
        this( widget, propertyName, propertyName, defaultValue, minValue, maxValue, false );
    }
}
