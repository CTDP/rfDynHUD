/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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

import org.jagatoo.util.Tools;

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
    public static interface ListPropertyValue
    {
        public String getForConfigFile();
        
        public boolean parse( String valueFromConfigFile );
    }
    
    private final E defaultValue;
    
    private L list;
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
     * Gets the data list.
     * 
     * @return the data list.
     */
    public final L getList()
    {
        return ( list );
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
     * Sets the new selected value for this property.
     * 
     * @param value the new value
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
        
        return ( false );
    }
    
    /**
     * Sets the new selected value for this property.
     * 
     * @param value the new value
     * 
     * @return changed?
     */
    public final boolean setSelectedValue( E value )
    {
        return ( setSelectedValue( value, false ) );
    }
    
    /**
     * Gets the currently selected element.
     * 
     * @return the currently selected element.
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
    public Object getValueForConfigurationFile()
    {
        Object value = super.getValueForConfigurationFile();
        
        if ( value instanceof ListPropertyValue )
            return ( ( (ListPropertyValue)value ).getForConfigFile() );
        
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        for ( E e : this.list )
        {
            if ( e instanceof ListPropertyValue )
            {
                if ( ( (ListPropertyValue)e ).parse( value ) )
                {
                    setValue( e );
                    
                    return;
                }
            }
            else if ( Tools.objectsEqual( value, e ) )
            {
                setValue( e );
                
                return;
            }
        }
    }
    
    public String getButton2Text()
    {
        return ( null );
    }
    
    public String getButton2Tooltip()
    {
        return ( null );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param list the data list
     * @param readonly read only property?
     * @param buttonText the text for the displayed button in the editor
     */
    public ListProperty( String name, String nameForDisplay, E defaultValue, L list, boolean readonly, String buttonText )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.LIST, buttonText, null );
        
        this.defaultValue = defaultValue;
        
        this.list = list;
        
        setSelectedValue( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param list the data list
     * @param readonly read only property?
     */
    public ListProperty( String name, String nameForDisplay, E defaultValue, L list, boolean readonly )
    {
        this( name, nameForDisplay, defaultValue, list, readonly, null );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param list the data list
     */
    public ListProperty( String name, String nameForDisplay, E defaultValue, L list )
    {
        this( name, nameForDisplay, defaultValue, list, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param list the data list
     * @param readonly read only property?
     */
    public ListProperty( String name, E defaultValue, L list, boolean readonly )
    {
        this( name, null, defaultValue, list, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param list the data list
     */
    public ListProperty( String name, E defaultValue, L list )
    {
        this( name, defaultValue, list, false );
    }
}
