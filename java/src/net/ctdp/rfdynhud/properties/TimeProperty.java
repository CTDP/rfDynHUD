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

/**
 * The {@link TimeProperty} serves for customizing a time value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TimeProperty extends Property
{
    private static final long ONE_SECOND = 1000000000L;
    
    private final long defaultValue;
    
    private long value;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Long getDefaultValue()
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
        
        onValueChanged( null, getNanoValue() );
    }
    
    /**
     * Invoked when the value has changed.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( Long oldValue, long newValue )
    {
    }
    
    /**
     * 
     * @param nanos
     */
    void onValueSet( long nanos )
    {
    }
    
    /**
     * Sets the property's value.
     * 
     * @param nanos the time in nano seconds
     * @param firstTime
     * 
     * @return changed?
     */
    protected final boolean setNanoValue( long nanos, boolean firstTime )
    {
        if ( nanos == this.value )
            return ( false );
        
        Long oldValue = firstTime ? null : this.value;
        
        this.value = nanos;
        
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
     * @param nanos the time in nano seconds
     * 
     * @return changed?
     */
    public final boolean setNanoValue( long nanos )
    {
        return ( setNanoValue( nanos, false ) );
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
    
    private static long parseTimeValue( String time )
    {
        if ( time != null )
            time = time.trim();
        
        if ( ( time == null ) || ( time.length() == 0 ) )
            return ( 0L );
        
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
        
        return ( nanos );
    }
    
    /**
     * Sets the property's value.
     * 
     * @param time the new value in the format &quot;00:00:00&quot;.
     * 
     * @return changed?
     */
    public final boolean setTimeValue( String time )
    {
        return ( setNanoValue( parseTimeValue( time ), false ) );
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
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public TimeProperty( String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.TIME, null, null );
        
        long time = parseTimeValue( defaultValue );
        
        this.defaultValue = time;
        
        setNanoValue( time, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public TimeProperty( String name, String nameForDisplay, String defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public TimeProperty( String name, String defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public TimeProperty( String name, String defaultValue )
    {
        this( name, defaultValue, false );
    }
}
