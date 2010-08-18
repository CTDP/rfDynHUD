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

/**
 * The {@link EnumProperty} serves for customizing a value from an enum.
 * 
 * @author Marvin Froehlich (CTDP)
 * 
 * @param <E> the enum type
 */
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
        
        onValueChanged();
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
    public E getValue()
    {
        return ( getEnumValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( String value )
    {
        for ( Enum<?> e : this.value.getClass().getEnumConstants() )
        {
            if ( e.name().equals( value ) )
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
     * @param readonly
     */
    public EnumProperty( Widget widget, String name, String nameForDisplay, E defaultValue, boolean readonly )
    {
        super( widget, name, nameForDisplay, readonly, PropertyEditorType.ENUM, null, null );
        
        this.value = defaultValue;
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public EnumProperty( Widget widget, String name, String nameForDisplay, E defaultValue )
    {
        this( widget, name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param readonly
     */
    public EnumProperty( Widget widget, String name, E defaultValue, boolean readonly )
    {
        this( widget, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public EnumProperty( Widget widget, String name, E defaultValue )
    {
        this( widget, name, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly
     */
    public EnumProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue, boolean readonly )
    {
        this( (Widget)null, name, nameForDisplay, defaultValue, readonly );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public EnumProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue )
    {
        this( w2pf, name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param readonly
     */
    public EnumProperty( WidgetToPropertyForwarder w2pf, String name, E defaultValue, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public EnumProperty( WidgetToPropertyForwarder w2pf, String name, E defaultValue )
    {
        this( w2pf, name, defaultValue, false );
    }
}
