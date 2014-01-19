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
package net.ctdp.rfdynhud.util;

import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.properties.DelayProperty;

/**
 * This class is a simple utility, that counts a given amount of time and reports the current state.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Delay
{
    public static enum State
    {
        STANDBY,
        DELAYING,
        TIME_UP,
    }
    
    private int lastKnownSessionID = -1234;
    
    private long t0 = -1L;
    private long t1 = -1L;
    
    public void reset()
    {
        lastKnownSessionID = -1234;
        
        t0 = -1L;
        t1 = -1L;
    }
    
    /**
     * Starts the delay. It will end after the given nanos have passed.
     * 
     * @param scoringInfo the ScoringInfo to take the start (current) time from
     * @param delayNanos the nanos, this delay will run for
     */
    public void start( ScoringInfo scoringInfo, long delayNanos )
    {
        t0 = scoringInfo.getSessionNanos();
        t1 = t0 + delayNanos;
        lastKnownSessionID = scoringInfo.getSessionId();
    }
    
    /**
     * Starts the delay. It will end after the given seconds have passed.
     * 
     * @param scoringInfo the ScoringInfo to take the start (current) time from
     * @param delaySeconds the seconds, this delay will run for
     */
    public final void start( ScoringInfo scoringInfo, float delaySeconds )
    {
        start( scoringInfo, (long)( (double)delaySeconds * 1000000000L ) );
    }
    
    /**
     * Starts the delay. It will end after the given seconds have passed.
     * 
     * @param scoringInfo the ScoringInfo to take the start (current) time from
     * @param delay the property to take the delay from
     */
    public final void start( ScoringInfo scoringInfo, DelayProperty delay )
    {
        start( scoringInfo, delay.getDelayNanos() );
    }
    
    /**
     * Gets the {@link Delay}'s current state.
     * 
     * @param scoringInfo
     * 
     * @return the {@link Delay}'s current state.
     */
    public final State getState( ScoringInfo scoringInfo )
    {
        if ( scoringInfo.getSessionId() == lastKnownSessionID )
        {
            if ( scoringInfo.getSessionNanos() < t1 )
                return ( State.DELAYING );
            
            return ( State.TIME_UP );
        }
        
        return ( State.STANDBY );
    }
    
    /**
     * Checks, whether the time's up for this {@link Delay}.
     * 
     * @param scoringInfo
     * 
     * @return whether the time's up for this {@link Delay}.
     */
    public final boolean isTimeUp( ScoringInfo scoringInfo )
    {
        return ( getState( scoringInfo ) == State.TIME_UP );
    }
    
    public Delay()
    {
    }
}
