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

import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * The {@link DelayProperty} serves for customizing a time delay.
 * A time delay is always measured in nano seconds internally.
 * This property provides the value in other units like milliseconds for easier use.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DelayProperty extends IntProperty
{
    public static enum DisplayUnits
    {
        MILLISECONDS,
        SECONDS,
        ;
    }
    
    private final DisplayUnits displayUnits;
    
    private long delayNanos;
    private float delaySeconds;
    
    /**
     * Gets the used {@link DisplayUnits}.
     * 
     * @return the used {@link DisplayUnits}.
     */
    public final DisplayUnits getDisplayUnits()
    {
        return ( displayUnits );
    }
    
    /**
     * This method is utilized to derive the delay value from the property value.
     * 
     * @param value the property value
     * @param displayUnits the used {@link DisplayUnits}
     * 
     * @return the derived delay value.
     */
    protected long deriveDelay( int value, DisplayUnits displayUnits )
    {
        switch ( displayUnits )
        {
            case MILLISECONDS:
                return ( value * 1000000L );
            case SECONDS:
                return ( value * 1000000000L );
        }
        
        throw new Error( "Unsupported DisplayUnits " + displayUnits );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setIntValue( int value )
    {
        if ( super.setIntValue( value ) )
        {
            this.delayNanos = deriveDelay( value, displayUnits );
            this.delaySeconds = (float)( delayNanos / 1000000000.0 );
            
            return ( true );
        }
        
        return ( false );
    }
    
    /**
     * The actual delay values in nanoseconds.
     * 
     * @return actual delay values in nanoseconds.
     */
    public final long getDelayNanos()
    {
        return ( delayNanos );
    }
    
    /**
     * The actual delay values in seconds.
     * 
     * @return actual delay values in seconds.
     */
    public final float getDelaySeconds()
    {
        return ( delaySeconds );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param readonly read only property?
     */
    public DelayProperty( Widget widget, String name, String nameForDisplay, DisplayUnits displayUnits, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        super( widget, name, nameForDisplay, defaultValue, minValue, maxValue, readonly );
        
        this.displayUnits = displayUnits;
        this.delayNanos = deriveDelay( getIntValue(), displayUnits );
        this.delaySeconds = (float)( delayNanos / 1000000000.0 );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public DelayProperty( Widget widget, String name, String nameForDisplay, DisplayUnits displayUnits, int defaultValue, boolean readonly )
    {
        this( widget, name, nameForDisplay, displayUnits, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, readonly );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     */
    public DelayProperty( Widget widget, String name, String nameForDisplay, DisplayUnits displayUnits, int defaultValue )
    {
        this( widget, name, nameForDisplay, displayUnits, defaultValue, false );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public DelayProperty( Widget widget, String name, DisplayUnits displayUnits, int defaultValue, boolean readonly )
    {
        this( widget, name, null, displayUnits, defaultValue, readonly );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     */
    public DelayProperty( Widget widget, String name, DisplayUnits displayUnits, int defaultValue )
    {
        this( widget, name, displayUnits, defaultValue, false );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     */
    public DelayProperty( Widget widget, String name, DisplayUnits displayUnits, int defaultValue, int minValue, int maxValue )
    {
        this( widget, name, null, displayUnits, defaultValue, minValue, maxValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param readonly read only property?
     */
    public DelayProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, DisplayUnits displayUnits, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        this( (Widget)null, name, nameForDisplay, displayUnits, defaultValue, minValue, maxValue, readonly );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public DelayProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, DisplayUnits displayUnits, int defaultValue, boolean readonly )
    {
        this( w2pf, name, nameForDisplay, displayUnits, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, readonly );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     */
    public DelayProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, DisplayUnits displayUnits, int defaultValue )
    {
        this( w2pf, name, nameForDisplay, displayUnits, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public DelayProperty( WidgetToPropertyForwarder w2pf, String name, DisplayUnits displayUnits, int defaultValue, boolean readonly )
    {
        this( w2pf, name, null, displayUnits, defaultValue, readonly );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     */
    public DelayProperty( WidgetToPropertyForwarder w2pf, String name, DisplayUnits displayUnits, int defaultValue )
    {
        this( w2pf, name, displayUnits, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param displayUnits the units to display the value in
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     */
    public DelayProperty( WidgetToPropertyForwarder w2pf, String name, DisplayUnits displayUnits, int defaultValue, int minValue, int maxValue )
    {
        this( w2pf, name, null, displayUnits, defaultValue, minValue, maxValue, false );
    }
}
