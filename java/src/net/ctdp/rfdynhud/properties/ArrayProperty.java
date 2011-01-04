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

import org.jagatoo.util.Tools;

/**
 * The {@link ArrayProperty} serves for customizing a value from an array.
 * 
 * @author Marvin Froehlich (CTDP)
 * 
 * @param <E> the array element type
 */
public class ArrayProperty<E extends Object> extends Property
{
    private final E defaultValue;
    
    private E[] array;
    private E value;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public E getDefaultValue()
    {
        return ( defaultValue );
    }
    
    /**
     * Gets the data array.
     * 
     * @return the data array.
     */
    public final E[] getArray()
    {
        return ( array );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onKeeperSet()
    {
        super.onKeeperSet();
        
        onValueChanged( null, getValue() );
    }
    
    /**
     * Invoked when the value has changed.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( E oldValue, E newValue )
    {
    }
    
    /**
     * Invoked when the value has been set.
     * 
     * @param value the new value
     */
    void onValueSet( E value )
    {
    }
    
    /**
     * Sets the currently selected value.
     * 
     * @param value
     * @param firstTime
     * 
     * @return changed?
     */
    protected final boolean setSelectedValue( E value, boolean firstTime )
    {
        if ( Tools.objectsEqual( value, this.value ) )
            return ( false );
        
        E oldValue = firstTime ? null : this.value;
        this.value = value;
        
        onValueSet( this.value );
        
        if ( !firstTime )
        {
            triggerKeepersOnPropertyChanged( oldValue, value );
            onValueChanged( oldValue, value );
        }
        
        return ( true );
    }
    
    /**
     * Sets the currently selected value.
     * 
     * @param value
     * 
     * @return changed?
     */
    public final boolean setSelectedValue( E value )
    {
        return ( setSelectedValue( value, false ) );
    }
    
    /**
     * Gets the currently selected value.
     * 
     * @return the currently selected value.
     */
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
    public void loadValue( PropertyLoader loader, String value )
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
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param array the data array
     * @param readonly read only property?
     * @param buttonText the button text
     */
    public ArrayProperty( String name, String nameForDisplay, E defaultValue, E[] array, boolean readonly, String buttonText )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.ARRAY, buttonText, null );
        
        this.defaultValue = defaultValue;
        this.array = array;
        
        setSelectedValue( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param array the data array
     * @param readonly read only property?
     */
    public ArrayProperty( String name, String nameForDisplay, E defaultValue, E[] array, boolean readonly )
    {
        this( name, nameForDisplay, defaultValue, array, readonly, null );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param array the data array
     */
    public ArrayProperty( String name, String nameForDisplay, E defaultValue, E[] array )
    {
        this( name, nameForDisplay, defaultValue, array, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param array the data array
     * @param readonly read only property?
     */
    public ArrayProperty( String name, E defaultValue, E[] array, boolean readonly )
    {
        this( name, null, defaultValue, array, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param array the data array
     */
    public ArrayProperty( String name, E defaultValue, E[] array )
    {
        this( name, defaultValue, array, false );
    }
}
