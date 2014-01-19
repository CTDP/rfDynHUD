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


/**
 * The {@link BooleanProperty} serves for customizing a primitive boolean value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BooleanProperty extends Property
{
    private final boolean defaultValue;
    private boolean value;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getDefaultValue()
    {
        return ( defaultValue );
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
    protected void onValueChanged( Boolean oldValue, boolean newValue )
    {
    }
    
    /**
     * Invoked when the value has been set.
     * 
     * @param value the new value
     */
    void onValueSet( boolean value )
    {
    }
    
    /**
     * Sets the property's value.
     * 
     * @param value the new value
     * @param firstTime
     * 
     * @return changed?
     */
    protected final boolean setBooleanValue( boolean value, boolean firstTime )
    {
        if ( value == this.value )
            return ( false );
        
        Boolean oldValue = firstTime ? null : this.value;
        this.value = value;
        
        onValueSet( this.value );
        
        if ( !firstTime )
        {
            triggerKeepersOnPropertyChanged( !value, value );
            onValueChanged( oldValue, value );
        }
        
        return ( true );
    }
    
    /**
     * Sets the property's value.
     * 
     * @param value the new value
     * 
     * @return changed?
     */
    public final boolean setBooleanValue( boolean value )
    {
        return ( setBooleanValue( value, false ) );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final boolean getBooleanValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setBooleanValue( ( (Boolean)value ).booleanValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getValue()
    {
        return ( getBooleanValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        setValue( Boolean.parseBoolean( value ) );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public BooleanProperty( String name, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.defaultValue = defaultValue;
        
        setBooleanValue( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public BooleanProperty( String name, String nameForDisplay, boolean defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public BooleanProperty( String name, boolean defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public BooleanProperty( String name, boolean defaultValue )
    {
        this( name, defaultValue, false );
    }
}
