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

/**
 * The {@link ListProperty} serves for customizing a value from a list.
 * 
 * @author Marvin Froehlich (CTDP)
 * 
 * @param <E> the list element type
 * @param <L> the list type
 */
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
    public E getValue()
    {
        return ( getSelectedValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( String value )
    {
        for ( E e : this.list )
        {
            if ( Tools.objectsEqual( value, e ) )
            {
                setValue( e );
                
                return;
            }
        }
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param list
     * @param readonly
     * @param buttonText
     */
    public ListProperty( Widget widget, String name, String nameForDisplay, E defaultValue, L list, boolean readonly, String buttonText )
    {
        super( widget, name, nameForDisplay, readonly, PropertyEditorType.LIST, buttonText, null );
        
        this.value = defaultValue;
        this.list = list;
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param list
     * @param readonly
     */
    public ListProperty( Widget widget, String name, String nameForDisplay, E defaultValue, L list, boolean readonly )
    {
        this( widget, name, nameForDisplay, defaultValue, list, readonly, null );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param list
     */
    public ListProperty( Widget widget, String name, String nameForDisplay, E defaultValue, L list )
    {
        this( widget, name, nameForDisplay, defaultValue, list, false );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param list
     * @param readonly
     */
    public ListProperty( Widget widget, String name, E defaultValue, L list, boolean readonly )
    {
        this( widget, name, null, defaultValue, list, readonly );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param list
     */
    public ListProperty( Widget widget, String name, E defaultValue, L list )
    {
        this( widget, name, defaultValue, list, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param list
     * @param readonly
     * @param buttonText
     */
    public ListProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue, L list, boolean readonly, String buttonText )
    {
        this( (Widget)null, name, nameForDisplay, defaultValue, list, readonly, buttonText );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param list
     * @param readonly
     */
    public ListProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue, L list, boolean readonly )
    {
        this( w2pf, name, nameForDisplay, defaultValue, list, readonly, null );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param list
     */
    public ListProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue, L list )
    {
        this( w2pf, name, nameForDisplay, defaultValue, list, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param list
     * @param readonly
     */
    public ListProperty( WidgetToPropertyForwarder w2pf, String name, E defaultValue, L list, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, list, readonly );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param list
     */
    public ListProperty( WidgetToPropertyForwarder w2pf, String name, E defaultValue, L list )
    {
        this( w2pf, name, defaultValue, list, false );
    }
}
