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
 * The {@link FloatProperty} serves for customizing a primitive float value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class FloatProperty extends Property
{
    private final float defaultValue;
    
    private float value;
    
    private final float minValue;
    private final float maxValue;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Float getDefaultValue()
    {
        return ( defaultValue );
    }
    
    /**
     * Gets the minimum value.
     * 
     * @return the minimum value.
     */
    public final float getMinValue()
    {
        return ( minValue );
    }
    
    /**
     * Gets the maximum value.
     * 
     * @return the maximum value.
     */
    public final float getMaxValue()
    {
        return ( maxValue );
    }
    
    /**
     * Fixes the value to stick to the limits.
     * 
     * @param value the unfixed value
     * 
     * @return the fixed value.
     */
    protected float fixValue( float value )
    {
        return ( Math.max( minValue, Math.min( value, maxValue ) ) );
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
    protected void onValueChanged( Float oldValue, float newValue )
    {
    }
    
    /**
     * Invoked when the value has been set.
     * 
     * @param value the new value
     */
    void onValueSet( float value )
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
    protected final boolean setFloatValue( float value, boolean firstTime )
    {
        value = fixValue( value );
        
        if ( value == this.value )
            return ( false );
        
        Float oldValue = firstTime ? null : this.value;
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
     * Sets the property's value.
     * 
     * @param value the new value
     * 
     * @return changed?
     */
    public final boolean setFloatValue( float value )
    {
        return ( setFloatValue( value, false ) );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final float getFloatValue()
    {
        return ( value );
    }
    
    /**
     * Gets the property's current value as an int.
     * 
     * @param round if <code>true</code> the value is rounded, otherwise it is floored.
     * 
     * @return the property's current value as an int.
     */
    public final int getIntValue( boolean round )
    {
        if ( round )
            return ( Math.round( value ) );
        
        return ( (int)value );
    }
    
    /**
     * Gets the property's current value as a floored int.
     * 
     * @return the property's current value as a floored int.
     */
    public final int getIntValue()
    {
        return ( getIntValue( false ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setFloatValue( ( (Number)value ).floatValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Float getValue()
    {
        return ( getFloatValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        setValue( Float.parseFloat( value ) );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param readonly read only property?
     * @param pet
     * @param initialize
     */
    FloatProperty( String name, String nameForDisplay, float defaultValue, float minValue, float maxValue, boolean readonly, PropertyEditorType pet, boolean initialize )
    {
        super( name, nameForDisplay, readonly, pet, null, null );
        
        this.defaultValue = defaultValue;
        
        this.minValue = minValue;
        this.maxValue = maxValue;
        
        if ( initialize )
            setFloatValue( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param readonly read only property?
     */
    public FloatProperty( String name, String nameForDisplay, float defaultValue, float minValue, float maxValue, boolean readonly )
    {
        this( name, nameForDisplay, defaultValue, minValue, maxValue, readonly, PropertyEditorType.FLOAT, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public FloatProperty( String name, String nameForDisplay, float defaultValue, boolean readonly )
    {
        this( name, nameForDisplay, defaultValue, -Float.MAX_VALUE, +Float.MAX_VALUE, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public FloatProperty( String name, String nameForDisplay, float defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public FloatProperty( String name, float defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public FloatProperty( String name, float defaultValue )
    {
        this( name, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     */
    public FloatProperty( String name, float defaultValue, float minValue, float maxValue )
    {
        this( name, null, defaultValue, minValue, maxValue, false );
    }
}
