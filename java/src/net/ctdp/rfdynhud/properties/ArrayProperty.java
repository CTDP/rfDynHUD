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

import net.ctdp.rfdynhud.util.Tools;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

/**
 * The {@link ArrayProperty} serves for customizing a value from an array.
 * 
 * @author Marvin Froehlich (CTDP)
 * 
 * @param <E> the array element type
 */
public class ArrayProperty<E extends Object> extends Property
{
    private E[] array;
    private E value;
    
    public final E[] getArray()
    {
        return ( array );
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
            widget.forceAndSetDirty( true );
        
        onValueChanged();
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
        for ( E e : this.array )
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
     * @param array
     * @param readonly
     * @param buttonText
     */
    public ArrayProperty( Widget widget, String name, String nameForDisplay, E defaultValue, E[] array, boolean readonly, String buttonText )
    {
        super( widget, name, nameForDisplay, readonly, PropertyEditorType.ARRAY, buttonText, null );
        
        this.value = defaultValue;
        this.array = array;
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param array
     * @param readonly
     */
    public ArrayProperty( Widget widget, String name, String nameForDisplay, E defaultValue, E[] array, boolean readonly )
    {
        this( widget, name, nameForDisplay, defaultValue, array, readonly, null );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param array
     */
    public ArrayProperty( Widget widget, String name, String nameForDisplay, E defaultValue, E[] array )
    {
        this( widget, name, nameForDisplay, defaultValue, array, false );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param array
     * @param readonly
     */
    public ArrayProperty( Widget widget, String name, E defaultValue, E[] array, boolean readonly )
    {
        this( widget, name, null, defaultValue, array, readonly );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param array
     */
    public ArrayProperty( Widget widget, String name, E defaultValue, E[] array )
    {
        this( widget, name, defaultValue, array, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param array
     * @param readonly
     * @param buttonText
     */
    public ArrayProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue, E[] array, boolean readonly, String buttonText )
    {
        super( w2pf, name, nameForDisplay, readonly, PropertyEditorType.ARRAY, buttonText, null );
        
        this.value = defaultValue;
        this.array = array;
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param array
     * @param readonly
     */
    public ArrayProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue, E[] array, boolean readonly )
    {
        this( w2pf, name, nameForDisplay, defaultValue, array, readonly, null );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param array
     */
    public ArrayProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue, E[] array )
    {
        this( w2pf, name, nameForDisplay, defaultValue, array, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param array
     * @param readonly
     */
    public ArrayProperty( WidgetToPropertyForwarder w2pf, String name, E defaultValue, E[] array, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, array, readonly );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param array
     */
    public ArrayProperty( WidgetToPropertyForwarder w2pf, String name, E defaultValue, E[] array )
    {
        this( w2pf, name, defaultValue, array, false );
    }
}
