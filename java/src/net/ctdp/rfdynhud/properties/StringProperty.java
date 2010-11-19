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
 * The {@link StringProperty} serves for customizing a simple String value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class StringProperty extends Property
{
    private final boolean forceTrimOnSet;
    private String value;
    
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
     * Invoked when the property's value has changed.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    /**
     * Invoked when the property's value has been set.
     * 
     * @param value the new value
     */
    void onValueSet( String value )
    {
    }
    
    /**
     * Sets the property's new value.
     * 
     * @param value the new value
     * @param firstTime
     * 
     * @return changed?
     */
    protected final boolean setStringValue( String value, boolean firstTime )
    {
        if ( forceTrimOnSet && ( value != null ) )
            value = value.trim();
        
        if ( ( ( value == null ) && ( this.value == null ) ) || ( ( value != null ) && value.equals( this.value ) ) )
            return ( false );
        
        String oldValue = firstTime ? null : this.value;
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
     * Sets the property's new value.
     * 
     * @param value the new value
     * 
     * @return changed?
     */
    public final boolean setStringValue( String value )
    {
        return ( setStringValue( value, false ) );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final String getStringValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setStringValue( String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue()
    {
        return ( getStringValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        setValue( value );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param forceTrimOnSet trim value when set
     * @param readonly read only property?
     * @param propEdType property editor type
     */
    protected StringProperty( String name, String nameForDisplay, String defaultValue, boolean forceTrimOnSet, boolean readonly, PropertyEditorType propEdType )
    {
        super( name, nameForDisplay, readonly, propEdType, null, null );
        
        this.forceTrimOnSet = forceTrimOnSet;
        
        setStringValue( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public StringProperty( String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( name, nameForDisplay, defaultValue, false, readonly, PropertyEditorType.STRING );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public StringProperty( String name, String nameForDisplay, String defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public StringProperty( String name, String defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public StringProperty( String name, String defaultValue )
    {
        this( name, defaultValue, false );
    }
}
