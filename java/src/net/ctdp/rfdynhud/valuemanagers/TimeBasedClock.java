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
package net.ctdp.rfdynhud.valuemanagers;


/**
 * The {@link TimeBasedClock} calculates frame gaps based on a given time delay to know, when to set the clock flag and when to reset.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TimeBasedClock extends Clock
{
    public static final long DEFAULT_MIN_FRAMES = 3L;
    
    private final long delay;
    private final long minFrames;
    
    private long measureStart = -1L;
    private long measureEnd = -1L;
    private long measureFrameCounter = 0;
    
    //private long nextClockTime1 = -1L;
    //private long nextClockTime2 = -1L;
    
    private long clockFrames = 10;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initImpl( long nanoTime )
    {
        measureEnd = -1L;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean updateImpl( long nanoTime, long frameCounter, boolean force )
    {
        if ( measureEnd == -1L )
        {
            measureStart = nanoTime;
            measureEnd = nanoTime + ONE_SECOND_NANOS;
            measureFrameCounter = 0L;
        }
        else if ( nanoTime >= measureEnd )
        {
            long dt = nanoTime - measureStart;
            clockFrames = Math.max( minFrames, measureFrameCounter * delay / dt );
            //Logger.log( "Clock: " + clockFrames + " ( " + measureFrameCounter + ", " + delay + ", " + dt + " )" );
            
            measureStart = nanoTime;
            measureEnd = nanoTime + ONE_SECOND_NANOS;
            measureFrameCounter = 0L;
        }
        
        measureFrameCounter++;
        
        return ( force || ( ( frameCounter % clockFrames ) == 0L ) );
    }
    
    public TimeBasedClock( long delay, long minFrames )
    {
        this.delay = delay;
        this.minFrames = minFrames;
    }
    
    public TimeBasedClock( long delay )
    {
        this( delay, DEFAULT_MIN_FRAMES );
    }
}
