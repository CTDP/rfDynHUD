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

/**
 * A {@link Clock} sets the {@link #c()} member to true in implementation specific intervals
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class Clock implements ManagedValue
{
    public static final long ONE_SECOND_NANOS = 1000000000L;
    
    private boolean c = false;
    private long ticks = 0L;
    
    /**
     * Implementation specific code for initialization.
     * 
     * @param nanoTime the starting time stamp
     */
    protected abstract void initImpl( long nanoTime );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void init( long nanoTime )
    {
        c = false;
        ticks = 0L;
        
        initImpl( nanoTime );
    }
    
    private void setC( boolean c )
    {
        this.c = c;
        
        if ( this.c )
            ticks++;
    }
    
    /**
     * Gets the current clock flag.
     * 
     * @return <code>true</code>, if the flag is set, <code>false</code> otherwise.
     */
    public final boolean c()
    {
        return ( c );
    }
    
    /**
     * Gets a 'multiplied' clock flag.
     * 
     * @param step the number of clock ticks to wait for the next 'true'
     * 
     * @return <code>true</code>, if the current flag is set, and ( ticks % step ) is 0.
     */
    public final boolean c( int step )
    {
        return ( c && ( ( ticks % step ) == 0L ) );
    }
    
    /**
     * This is a shortcut for {@link #c(int)} with a step value of 2.
     * 
     * @return <code>true</code> every 2 clock ticks, <code>false</code> otherwise.
     */
    public final boolean c2()
    {
        return ( c && ( ( ticks % 2 ) == 0L ) );
    }
    
    /**
     * This is a shortcut for {@link #c(int)} with a step value of 3.
     * 
     * @return <code>true</code> every 3 clock ticks, <code>false</code> otherwise.
     */
    public final boolean c3()
    {
        return ( c && ( ( ticks % 3 ) == 0L ) );
    }
    
    /**
     * Gets the number of <code>true</code> situations since the last call to {@link #init(long)}.
     * 
     * @return the number of true situations since the last call to {@link #init(long)}.
     */
    public final long getTicks()
    {
        return ( ticks );
    }
    
    /**
     * Implementation of the update method.
     * 
     * @param nanoTime the current timestamp in nano seconds
     * @param frameCounter the current frame index
     * @param force force clock to <code>true</code>.
     * 
     * @return <code>true</code> to set the {@link Clock} flag to <code>true</code>.
     */
    protected abstract boolean updateImpl( long nanoTime, long frameCounter, boolean force );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void update( long nanoTime, long frameCounter, boolean force )
    {
        setC( updateImpl( nanoTime, frameCounter, force ) );
    }
    
    protected Clock()
    {
    }
}
