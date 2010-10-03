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
    private long nextHitTime = -1L;
    private boolean hitState = false;
    
    public final FactoredIntProperty getProperty()
    {
        return ( property );
    }
    
    public final boolean isUsed()
    {
        return ( property.getIntValue() > 0 );
    }
    
    public void reset()
    {
        nextHitTime = -1L;
        hitState = false;
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
        if ( nextHitTime < 0L )
        {
            nextHitTime = nanoTime + property.getFactoredValue();
            hitState = true;
            
            onIntervalHit( hitState );
        }
        else if ( nanoTime >= nextHitTime )
        {
            nextHitTime = nanoTime + property.getFactoredValue();
            hitState = !hitState;
            
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
    }
}
