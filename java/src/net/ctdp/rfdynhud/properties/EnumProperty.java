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
     * Sets the selected value.
     * 
     * @param value the new value
     * @param firstTime
     * 
     * @return changed?
     */
    protected final boolean setEnumValue( E value, boolean firstTime )
    {
        if ( value == this.value )
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
     * Sets the selected value.
     * 
     * @param value the new value
     * 
     * @return changed?
     */
    public final boolean setEnumValue( E value )
    {
        return ( setEnumValue( value, false ) );
    }
    
    /**
     * Gets the currently selected value.
     * 
     * @return the currently selected value.
     */
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
    public void loadValue( PropertyLoader loader, String value )
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
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public EnumProperty( String name, String nameForDisplay, E defaultValue, boolean readonly )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.ENUM, null, null );
        
        setEnumValue( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public EnumProperty( String name, String nameForDisplay, E defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public EnumProperty( String name, E defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public EnumProperty( String name, E defaultValue )
    {
        this( name, defaultValue, false );
    }
}
