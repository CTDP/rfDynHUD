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
 * Interpolates a float value along time.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ValueInterpolater implements ManagedValue
{
    public static enum BoundaryType
    {
        CIRCULAR,
        BIDIRECTIONAL,
        ;
    }
    
    private final BoundaryType boundaryType;
    private final float minValue;
    private final float maxValue;
    private final float startValue;
    private final float speed;
    private final long speedNanos;
    private float value;
    private long startTime = 0L;
    
    /**
     * Gets the used {@link BoundaryType}.
     * 
     * @return the used {@link BoundaryType}.
     */
    public final BoundaryType getBoundaryType()
    {
        return ( boundaryType );
    }
    
    /**
     * Gets the minimum value.
     * 
     * @return the minimum value.
     */
    public final float getMinValue()
    {
        return ( minValue );
    }
    
    /**
     * Gets the maxmum value.
     * 
     * @return the maxmum value.
     */
    public final float getMaxValue()
    {
        return ( maxValue );
    }
    
    /**
     * Gets the start value (set by the {@link #init(long)} method).
     * 
     * @return the start value.
     */
    public final float getStartValue()
    {
        return ( startValue );
    }
    
    /**
     * Gets the speed.
     * This is the time in seconds for moving from minValue to maxValue.
     * 
     * @return the speed in seconds.
     */
    public final float getSpeed()
    {
        return ( speed );
    }
    
    /**
     * Sets the current value.
     * 
     * @param value
     */
    private void setValue( float value )
    {
        this.value = value;
    }
    
    /**
     * Gets the current value.
     * 
     * @return the interpolated value
     */
    public final float getValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init( long nanoTime )
    {
        setValue( getStartValue() );
        this.startTime = nanoTime;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void update( long nanoTime, long frameCounter, boolean force )
    {
        if ( boundaryType == BoundaryType.CIRCULAR )
        {
            long t = ( nanoTime - startTime ) % speedNanos;
            float tf = (float)( t / 1000000000.0 );
            float nt = tf / speed;
            
            this.value = minValue + ( maxValue - minValue ) * nt;
        }
        else if ( boundaryType == BoundaryType.BIDIRECTIONAL )
        {
            long t = ( nanoTime - startTime );
            float n = (float)( t / (double)speedNanos );
            int m = (int)( n % 2.0f );
            
            if ( m == 0 )
                t = t % speedNanos;
            else
                t = speedNanos - ( t % speedNanos );
            
            float tf = (float)( t / 1000000000.0 );
            float nt = tf / speed;
            
            this.value = minValue + ( maxValue - minValue ) * nt;
        }
    }
    
    public final void update( long nanoTime )
    {
        update( nanoTime, -1, false );
    }
    
    public ValueInterpolater( BoundaryType boundaryType, float minValue, float maxValue, float startValue, float speed )
    {
        if ( boundaryType == null )
            throw new IllegalArgumentException( "boundaryType must not be null." );
        
        this.boundaryType = boundaryType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.startValue = startValue;
        this.speed = speed;
        this.speedNanos = (long)( 1000000000.0 * speed );
    }
}
