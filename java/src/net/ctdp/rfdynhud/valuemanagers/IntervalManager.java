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
package net.ctdp.rfdynhud.valuemanagers;

import net.ctdp.rfdynhud.properties.FactoredIntProperty;

/**
 * This manager invokes a method every defined time interval.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class IntervalManager implements ManagedValue
{
    private final FactoredIntProperty property;
    private final long interval;
    private long nextHitTime = -1L;
    private boolean hitState = false;
    private boolean stateChanged = false;
    
    public final long getInterval()
    {
        if ( property != null )
            return ( property.getFactoredValue() );
        
        return ( interval );
    }
    
    public final FactoredIntProperty getProperty()
    {
        return ( property );
    }
    
    public final boolean isUsed()
    {
        if ( property != null )
            return ( property.getIntValue() > 0L );
        
        return ( interval > 0L );
    }
    
    public void reset()
    {
        nextHitTime = -1L;
        hitState = false;
        stateChanged = false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init( long nanoTime )
    {
        reset();
    }
    
    /**
     * Gets the current interval state.
     * 
     * @return the current interval state.
     */
    public final boolean getState()
    {
        return ( hitState );
    }
    
    public final boolean getStateChanged()
    {
        return ( stateChanged );
    }
    
    /**
     * This method is invoked when the interval has been hit.
     * 
     * @param state the current state
     */
    protected void onIntervalHit( boolean state )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void update( long nanoTime, long frameCounter, boolean force )
    {
        stateChanged = false;
        
        if ( nextHitTime < 0L )
        {
            if ( property != null )
                nextHitTime = nanoTime + property.getFactoredValue();
            else
                nextHitTime = nanoTime + interval;
            hitState = true;
            stateChanged = true;
            
            onIntervalHit( hitState );
        }
        else if ( nanoTime >= nextHitTime )
        {
            if ( property != null )
                nextHitTime = nanoTime + property.getFactoredValue();
            else
                nextHitTime = nanoTime + interval;
            hitState = !hitState;
            stateChanged = true;
            
            onIntervalHit( hitState );
        }
    }
    
    public final void update( long nanoTime )
    {
        update( nanoTime, -1, false );
    }
    
    public IntervalManager( FactoredIntProperty intervalProperty )
    {
        if ( intervalProperty == null )
            throw new IllegalArgumentException( "intervalProperty must not be null." );
        
        this.property = intervalProperty;
        this.interval = -1L;
    }
    
    public IntervalManager( long intervalNanos )
    {
        if ( intervalNanos <= 0 )
            throw new IllegalArgumentException( "intervalNanos must be greater than 0." );
        
        this.property = null;
        this.interval = intervalNanos;
    }
}
