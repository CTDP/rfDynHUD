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

import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * The {@link TimeProperty} serves for customizing a time value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TimeProperty extends Property
{
    private static final long ONE_SECOND = 1000000000L;
    
    private long value;
    
    /**
     * Invoked when the value has changed.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( long oldValue, long newValue )
    {
    }
    
    /**
     * Sets the property's value.
     * 
     * @param nanos the time in nano seconds
     * @param triggerOnChange
     * 
     * @return changed?
     */
    private boolean setNanoValue( long nanos, boolean triggerOnChange )
    {
        if ( nanos == this.value )
            return ( false );
        
        long oldValue = this.value;
        
        this.value = nanos;
        
        if ( triggerOnChange )
        {
            if ( widget != null )
                widget.forceAndSetDirty( true );
            
            triggerCommonOnValueChanged( oldValue, value );
            if ( getTriggerOnValueChangedBeforeAttachedToConfig() || ( ( getWidget() != null ) && ( getWidget().getConfiguration() != null ) ) )
                onValueChanged( oldValue, value );
        }
        
        return ( true );
    }
    
    /**
     * Sets the property's value.
     * 
     * @param nanos the time in nano seconds
     * 
     * @return changed?
     */
    public boolean setNanoValue( long nanos )
    {
        return ( setNanoValue( nanos, true ) );
    }
    
    /**
     * Gets the current value as nano seconds.
     * 
     * @return the current value as nano seconds.
     */
    public final long getNanoValue()
    {
        return ( value );
    }
    
    /**
     * Sets the property's value.
     * 
     * @param time the new value in the format &quot;00:00:00&quot;.
     * @param triggerOnChange
     * 
     * @return changed?
     */
    private boolean setTimeValue( String time, boolean triggerOnChange )
    {
        long oldValue = this.value;
        
        if ( time != null )
            time = time.trim();
        
        if ( ( time == null ) || ( time.length() == 0 ) )
        {
            this.value = 0L;
        }
        else
        {
            long nanos = 0L;
            
            int p0 = time.lastIndexOf( ':' );
            
            if ( p0 == -1 )
            {
                // seconds
                nanos = Long.parseLong( time ) * ONE_SECOND;
            }
            else
            {
                if ( p0 == time.length() - 1 )
                    // seconds
                    nanos = 0L;
                else
                    // seconds
                    nanos = Long.parseLong( time.substring( p0 + 1 ) ) * ONE_SECOND;
                
                if ( p0 > 0 )
                {
                    int p1 = p0;
                    p0 = time.lastIndexOf( ':', p1 - 1 );
                    
                    if ( p0 < 0 )
                    {
                        // minutes
                        nanos += Long.parseLong( time.substring( 0, p1 ) ) * ONE_SECOND * 60L;
                    }
                    else
                    {
                        // minutes
                        nanos += Long.parseLong( time.substring( p0 + 1, p1 ) ) * ONE_SECOND * 60L;
                        
                        // hours
                        nanos += Long.parseLong( time.substring( 0, p0 ) ) * ONE_SECOND * 3600L;
                    }
                }
            }
            
            this.value = nanos;
        }
        
        if ( this.value == oldValue )
            return ( false );
        
        if ( triggerOnChange )
        {
            if ( widget != null )
                widget.forceAndSetDirty( true );
            
            triggerCommonOnValueChanged( oldValue, value );
            if ( getTriggerOnValueChangedBeforeAttachedToConfig() || ( ( getWidget() != null ) && ( getWidget().getConfiguration() != null ) ) )
                onValueChanged( oldValue, value );
        }
        
        return ( true );
    }
    
    /**
     * Sets the property's value.
     * 
     * @param time the new value in the format &quot;00:00:00&quot;.
     * 
     * @return changed?
     */
    public boolean setTimeValue( String time )
    {
        return ( setTimeValue( time, true ) );
    }
    
    /**
     * Gets the current value as a time string.
     * 
     * @return the current value as a time string.
     */
    public final String getTimeValue()
    {
        long seconds = ( value / ONE_SECOND ) % 60L;
        String time = NumberUtil.pad2( (int)seconds );
        
        long rest = value - ( seconds * ONE_SECOND );
        
        //if ( rest > 0L )
        {
            time = ":" + time;
            
            long minutes = ( rest / ( ONE_SECOND * 60L ) ) % 60L;
            time = NumberUtil.pad2( (int)minutes ) + time;
            
            rest -= minutes * 60L * ONE_SECOND;
            
            //if ( rest > 0L )
            {
                time = ":" + time;
                
                long hours = rest / ( ONE_SECOND * 3600L );
                time = NumberUtil.pad2( (int)hours ) + time;
            }
        }
        
        return ( time );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setTimeValue( String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue()
    {
        return ( getTimeValue() );
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
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public TimeProperty( Widget widget, String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( widget, name, nameForDisplay, readonly, PropertyEditorType.TIME, null, null );
        
        setTimeValue( defaultValue, false );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public TimeProperty( Widget widget, String name, String nameForDisplay, String defaultValue )
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
    public TimeProperty( Widget widget, String name, String defaultValue, boolean readonly )
    {
        this( widget, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public TimeProperty( Widget widget, String name, String defaultValue )
    {
        this( widget, name, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public TimeProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( (Widget)null, name, nameForDisplay, defaultValue, readonly );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public TimeProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue )
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
    public TimeProperty( WidgetToPropertyForwarder w2pf, String name, String defaultValue, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public TimeProperty( WidgetToPropertyForwarder w2pf, String name, String defaultValue )
    {
        this( w2pf, name, defaultValue, false );
    }
}
