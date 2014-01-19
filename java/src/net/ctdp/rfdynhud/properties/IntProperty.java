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
 * The {@link IntProperty} serves for customizing a primitive int value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class IntProperty extends Property
{
    private final int defaultValue;
    
    private int value;
    
    private final int minValue;
    private final int maxValue;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getDefaultValue()
    {
        return ( defaultValue );
    }
    
    /**
     * Gets the minimum value.
     * 
     * @return the minimum value.
     */
    public final int getMinValue()
    {
        return ( minValue );
    }
    
    /**
     * Gets the maximum value.
     * 
     * @return the maximum value.
     */
    public final int getMaxValue()
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
    protected int fixValue( int value )
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
    protected void onValueChanged( Integer oldValue, int newValue )
    {
    }
    
    /**
     * Invoked when the value has been set.
     * 
     * @param value the new value
     */
    void onValueSet( int value )
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
    protected final boolean setIntValue( int value, boolean firstTime )
    {
        value = fixValue( value );
        
        if ( value == this.value )
            return ( false );
        
        Integer oldValue = firstTime ? null : this.value;
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
    public final boolean setIntValue( int value )
    {
        return ( setIntValue( value, false ) );
    }
    
    /**
     * Gets the current value.
     * 
     * @return the current value.
     */
    public final int getIntValue()
    {
        return ( value );
    }
    
    /**
     * Gets the current value as a float.
     * 
     * @return the current value as a float.
     */
    public final float getFloatValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setIntValue( ( (Number)value ).intValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getValue()
    {
        return ( getIntValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        setValue( Integer.parseInt( value ) );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param readonly read only property?
     * @param initialize
     */
    IntProperty( String name, String nameForDisplay, int defaultValue, int minValue, int maxValue, boolean readonly, boolean initialize )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.INTEGER, null, null );
        
        this.defaultValue = defaultValue;
        
        this.minValue = minValue;
        this.maxValue = maxValue;
        
        if ( initialize )
            setIntValue( defaultValue, true );
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
    public IntProperty( String name, String nameForDisplay, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        this( name, nameForDisplay, defaultValue, minValue, maxValue, readonly, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public IntProperty( String name, String nameForDisplay, int defaultValue, boolean readonly )
    {
        this( name, nameForDisplay, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public IntProperty( String name, String nameForDisplay, int defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public IntProperty( String name, int defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public IntProperty(  String name, int defaultValue )
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
    public IntProperty( String name, int defaultValue, int minValue, int maxValue )
    {
        this( name, null, defaultValue, minValue, maxValue, false );
    }
}
