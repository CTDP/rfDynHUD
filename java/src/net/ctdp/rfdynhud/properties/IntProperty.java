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

/**
 * The {@link IntProperty} serves for customizing a primitive int value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class IntProperty extends Property
{
    private int value;
    
    private final int minValue;
    private final int maxValue;
    
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
     * Invoked when the value has changed.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( int oldValue, int newValue )
    {
    }
    
    /**
     * Sets the property's value.
     * 
     * @param value the new value
     * 
     * @return changed?
     */
    public boolean setIntValue( int value )
    {
        value = fixValue( value );
        
        if ( value == this.value )
            return ( false );
        
        int oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty( true );
        
        triggerCommonOnValueChanged( oldValue, value );
        if ( getTriggerOnValueChangedBeforeAttachedToConfig() || ( ( getWidget() != null ) && ( getWidget().getConfiguration() != null ) ) )
            onValueChanged( oldValue, value );
        
        return ( true );
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
    public void loadValue( String value )
    {
        setValue( Integer.parseInt( value ) );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param readonly read only property?
     */
    public IntProperty( Widget widget, String name, String nameForDisplay, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        super( widget, name, nameForDisplay, readonly, PropertyEditorType.INTEGER, null, null );
        
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public IntProperty( Widget widget, String name, String nameForDisplay, int defaultValue, boolean readonly )
    {
        this( widget, name, nameForDisplay, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, readonly );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public IntProperty( Widget widget, String name, String nameForDisplay, int defaultValue )
    {
        this( widget, name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public IntProperty( Widget widget, String name, int defaultValue, boolean readonly )
    {
        this( widget, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public IntProperty( Widget widget, String name, int defaultValue )
    {
        this( widget, name, defaultValue, false );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     */
    public IntProperty( Widget widget, String name, int defaultValue, int minValue, int maxValue )
    {
        this( widget, name, null, defaultValue, minValue, maxValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param readonly read only property?
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        this( (Widget)null, name, nameForDisplay, defaultValue, minValue, maxValue, readonly );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, int defaultValue, boolean readonly )
    {
        this( w2pf, name, nameForDisplay, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, readonly );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, int defaultValue )
    {
        this( w2pf, name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, int defaultValue, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, int defaultValue )
    {
        this( w2pf, name, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, int defaultValue, int minValue, int maxValue )
    {
        this( w2pf, name, null, defaultValue, minValue, maxValue, false );
    }
}
