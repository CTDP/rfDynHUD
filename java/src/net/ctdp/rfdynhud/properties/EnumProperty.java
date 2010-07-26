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

public class EnumProperty<E extends Enum<E>> extends Property
{
    private E value;
    
    /**
     * 
     * @param oldValue
     * @param newValue
     */
    protected void onValueChanged( E oldValue, E newValue )
    {
    }
    
    public void setEnumValue( E value )
    {
        if ( value == this.value )
            return;
        
        E oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, value, widget );
    }
    
    public final E getEnumValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public void setValue( Object value )
    {
        setEnumValue( (E)value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( getEnumValue() );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            for ( Enum<?> e : this.value.getClass().getEnumConstants() )
            {
                if ( e.name().equals( value ) )
                {
                    setValue( e );
                    
                    return ( true );
                }
            }
            
            return ( true );
        }
        
        return ( false );
    }
    
    public EnumProperty( Widget widget, String propertyName, String nameForDisplay, E defaultValue, boolean readonly )
    {
        super( widget, propertyName, nameForDisplay, readonly, PropertyEditorType.ENUM, null, null );
        
        this.value = defaultValue;
    }
    
    public EnumProperty( Widget widget, String propertyName, String nameForDisplay, E defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public EnumProperty( Widget widget, String propertyName, E defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public EnumProperty( Widget widget, String propertyName, E defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
}
