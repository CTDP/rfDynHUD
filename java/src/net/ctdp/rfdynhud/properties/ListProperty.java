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

import java.util.Collection;

import net.ctdp.rfdynhud.util.Tools;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class ListProperty<E extends Object, L extends Collection<E>> extends Property
{
    private L list;
    private E value;
    
    public final L getList()
    {
        return ( list );
    }
    
    /**
     * 
     * @param oldValue
     * @param newValue
     */
    protected void onValueChanged( E oldValue, E newValue )
    {
    }
    
    public void setSelectedValue( E value )
    {
        if ( Tools.objectsEqual( value, this.value ) )
            return;
        
        E oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, value, widget );
    }
    
    public final E getSelectedValue()
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
        setSelectedValue( (E)value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( getSelectedValue() );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            for ( E e : this.list )
            {
                if ( Tools.objectsEqual( value, e ) )
                {
                    setValue( e );
                    
                    return ( true );
                }
            }
            
            return ( true );
        }
        
        return ( false );
    }
    
    public ListProperty( Widget widget, String propertyName, String nameForDisplay, E defaultValue, L list, boolean readonly, String buttonText )
    {
        super( widget, propertyName, nameForDisplay, readonly, PropertyEditorType.LIST, buttonText, null );
        
        this.value = defaultValue;
        this.list = list;
    }
    
    public ListProperty( Widget widget, String propertyName, String nameForDisplay, E defaultValue, L list, boolean readonly )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, list, readonly, null );
    }
    
    public ListProperty( Widget widget, String propertyName, String nameForDisplay, E defaultValue, L list )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, list, false );
    }
    
    public ListProperty( Widget widget, String propertyName, E defaultValue, L list, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, list, readonly );
    }
    
    public ListProperty( Widget widget, String propertyName, E defaultValue, L list )
    {
        this( widget, propertyName, defaultValue, list, false );
    }
}
