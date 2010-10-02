/**
 * Copyright (c) 2007-2009, OpenMaLi Project Group all rights reserved.
 * 
 * Portions based on the Sun's javax.vecmath interface, Copyright by Sun
 * Microsystems or Kenji Hiranabe's alternative GC-cheap implementation.
 * Many thanks to the developers.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of the 'OpenMaLi Project Group' nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
 * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE
 */
package org.openmali.types.twodee;

/**
 * A basic 2-dimensional rectangle.
 * 
 * @author Marvin Froehlich (aka Qudus)
 * @author Kevin Finley (aka Horati)
 */
public class Rect2i implements Positioned2i, Sized2i
{
    private static final Rect2iPool POOL = new Rect2iPool();
    
    private int left, top;
    private int width, height;
    
    protected boolean isDirty = true;
    
    /**
     * @return true, if this object has been modified since the last setClean() call.
     * 
     * @see #setClean()
     */
    public final boolean isDirty()
    {
        return ( isDirty );
    }
    
    /**
     * Marks this object as not dirty.
     */
    public void setClean()
    {
        this.isDirty = false;
    }
    
    /**
     * Sets the upper-left corner's coordinates.
     * 
     * @param left
     * @param top
     * 
     * @return true, if the location actually has changed
     */
    @Override
    public Rect2i setLocation( int left, int top )
    {
        if ( ( left == this.left ) && ( top == this.top ) )
            return ( this );
        
        this.left = left;
        this.top = top;
        
        this.isDirty = true;
        
        return ( this );
    }
    
    /*
     * Sets the upper-left corner's coordinates.
     * 
     * @param upperLeft
     * 
     * @return true, if the location actually has changed
     */
    /*
    public final Rect2i setLocation( Tuple2i upperLeft )
    {
        return ( setLocation( upperLeft.getX(), upperLeft.getY() ) );
    }
    */
    
    /*
     * @return the upper-left corner's coordinates
     */
    /*
    public final Tuple2i getLocation()
    {
        return ( new Tuple2i( left, top ) );
    }
    */
    
    /**
     * @return the upper-left corner's x-coordinate
     */
    @Override
    public final int getLeft()
    {
        return ( left );
    }
    
    /**
     * @return the upper-left corner's y-coordinate
     */
    @Override
    public final int getTop()
    {
        return ( top );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Rect2i setSize( int width, int height )
    {
        if ( ( width == this.width ) && ( height == this.height ) )
            return ( this );
        
        this.width = width;
        this.height = height;
        
        this.isDirty = true;
        
        return ( this );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void setWidth( int width )
    {
        setSize( width, this.height );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void setHeight( int height )
    {
        setSize( this.width, height );
    }
    
    /**
     * @return the rectangle's width
     */
    @Override
    public final int getWidth()
    {
        return ( width );
    }
    
    /**
     * @return the rectangle's height
     */
    @Override
    public final int getHeight()
    {
        return ( height );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getAspect()
    {
        if ( getHeight() != 0 )
            return ( (float)getWidth() / (float)getHeight() );
        
        return ( 0f );
    }
    
    /**
     * @return the area-size of this rectangle.
     */
    public final int getArea()
    {
        return ( getWidth() * getHeight() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final Rect2i setSize( Sized2iRO size )
    {
        return ( setSize( size.getWidth(), size.getHeight() ) );
    }
    
    /*
     * {@inheritDoc}
     */
    /*
    public final Rect2i setSize( Tuple2i size )
    {
        return ( setSize( size.getX(), size.getY() ) );
    }
    */
    
    /**
     * Sets this rectangle's coordinates to the given rectangle's ones.
     * 
     * @param left the upper-left corner's x-coordinate
     * @param top the upper-left corner's y-coordinate
     * @param width the rectangle's width
     * @param height the rectangle's height
     * 
     * @return this instance
     */
    public final Rect2i set( int left, int top, int width, int height )
    {
        setLocation( left, top );
        setSize( width, height );
        
        return ( this );
    }
    
    /**
     * Sets this rectangle's coordinates to the given rectangle's ones.
     * 
     * @param pos
     * @param size
     * 
     * @return this instance
     */
    public final Rect2i set( Positioned2iRO pos, Sized2iRO size )
    {
        setLocation( pos.getLeft(), pos.getTop() );
        setSize( size.getWidth(), size.getHeight() );
        
        return ( this );
    }
    
    /**
     * Sets this rectangle's coordinates to the given rectangle's ones.
     * 
     * @param rect
     * 
     * @return this instance
     */
    public final Rect2i set( Rect2i rect )
    {
        setLocation( rect.getLeft(), rect.getTop() );
        setSize( rect.getWidth(), rect.getHeight() );
        
        return ( this );
    }
    
    /**
     * Checks, whether this rectangle is completely covered by the given one.
     * 
     * @param left
     * @param top
     * @param width
     * @param height
     * 
     * @return true, if the given rectangle completely covers this rectangle.
     */
    public final boolean isCoveredBy( int left, int top, int width, int height )
    {
        return ( ( this.left >= left ) &&
                ( ( this.left + this.width ) <= ( left + width ) ) &&
                ( this.top >= top ) &&
                ( ( this.top + this.height ) <= ( top + height ) )
              );
    }
    
    /**
     * Checks, whether this rectangle is completely covered by the given one.
     * 
     * @param rect
     * 
     * @return true, if the given rectangle completely covers this rectangle.
     */
    public final boolean isCoveredBy( Rect2i rect )
    {
        return ( isCoveredBy( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() ) );
    }
    
    /**
     * Checks, whether this rectangle completely covers the given one.
     * 
     * @param left
     * @param top
     * @param width
     * @param height
     * 
     * @return true, if this rectangle completely covers the given rectangle.
     */
    public final boolean covers( int left, int top, int width, int height )
    {
        return ( ( left >= this.left ) &&
                ( ( left + width ) <= ( this.left + this.width ) ) &&
                ( top >= this.top ) &&
                ( ( top + height ) <= ( this.top + this.height ) )
              );
    }
    
    /**
     * Checks, whether this rectangle completely covers the given one.
     * 
     * @param rect
     * 
     * @return true, if this rectangle completely covers the given rectangle.
     */
    public final boolean covers( Rect2i rect )
    {
        return ( covers( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() ) );
    }
    
    /**
     * @return the sum of area-size of the parts of this rectangle,
     * that overlap the given one.
     * 
     * @param left
     * @param top
     * @param width
     * @param height
     */
    public final int getMatchFactor( int left, int top, int width, int height )
    {
        /*
         * Actually this algorithm is precise in no way.
         * But it will suffice to compare two match-factors.
         * And it is fast.
         */
        
        int l = Math.min( left, this.left );
        int t = Math.min( top, this.top );
        
        int w = Math.max( left + width, this.left + this.width ) - l;
        int h = Math.max( top + height, this.top + this.height ) - t;
        
        int a = w * h;
        
        return ( a - ( width * height ) );
    }
    
    /**
     * @return the sum of area-size of the parts of this rectangle,
     * that overlap the given one.
     * 
     * @param rect
     */
    public final int getMatchFactor( Rect2i rect )
    {
        return ( getMatchFactor( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() ) );
    }
    
    /**
     * Checks, if the given rectangle intersects this rectangle.
     * 
     * @param left
     * @param top
     * @param width
     * @param height
     * 
     * @return true, if the two rectangles intersect.
     */
    public final boolean intersects( int left, int top, int width, int height )
    {
        if ( this.left + this.width < left )
            return ( false );
        
        if ( left + width < this.left )
            return ( false );
        
        if ( this.top + this.height < top )
            return ( false );
        
        if ( top + height < this.top )
            return ( false );
        
        return ( true );
    }
    
    /**
     * Checks, if the given rectangle intersects this rectangle.
     * 
     * @param rect
     * 
     * @return true, if the two rectangles intersect.
     */
    public final boolean intersects( Rect2i rect )
    {
        return ( intersects( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() ) );
    }
    
    /**
     * Combines this Rectangle with the given one and writes the result
     * into this instance.
     * 
     * @param left
     * @param top
     * @param width
     * @param height
     * 
     * @return this instance.
     */
    public Rect2i combine( int left, int top, int width, int height )
    {
        int newLeft = Math.min( this.left, left );
        int newTop = Math.min( this.top, top );
        int newWidth = Math.max( this.left + this.width - newLeft, left + width - newLeft );
        int newHeight = Math.max( this.top + this.height - newTop, top + height - newTop );
        
        set( newLeft, newTop, newWidth, newHeight );
        
        return ( this );
    }
    
    /**
     * Combines this Rectangle with the given one and writes the result
     * into this instance.
     * 
     * @param rect
     * 
     * @return this instance.
     */
    public final Rect2i combine( Rect2i rect )
    {
        return ( combine( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() ) );
    }
    
    /**
     * Clamps this rectangle by the given one and writes the result into this instance.
     * 
     * @param left
     * @param top
     * @param width
     * @param height
     * 
     * @return this instance.
     */
    public Rect2i clamp( int left, int top, int width, int height )
    {
        int clipX0 = this.left;
        int clipY0 = this.top;
        int clipX1 = clipX0 + this.width - 1;
        int clipY1 = clipY0 + this.height - 1;
        
        if ( clipX0 < left )
            clipX0 = left;
        
        if ( clipY0 < top )
            clipY0 = top;
        
        if ( clipX1 < clipX0 )
            clipX1 = clipX0;
        
        if ( clipY1 < clipY0 )
            clipY1 = clipY0;
        
        if ( clipX1 > left + width - 1 )
            clipX1 = left + width - 1;
        
        if ( clipY1 > top + height - 1 )
            clipY1 = top + height - 1;
        
        if ( clipX0 > clipX1 )
            clipX0 = clipX1;
        
        if ( clipY0 > clipY1 )
            clipY0 = clipY1;
        
        this.set( clipX0, clipY0, clipX1 - clipX0 + 1, clipY1 - clipY0 + 1 );
        
        return ( this );
    }
    
    /**
     * Clamps this rectangle by the given one and writes the result into this instance.
     * 
     * @param rect
     * 
     * @return this instance.
     */
    public final Rect2i clamp( Rect2i rect )
    {
        return ( clamp( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() ) );
    }
    
    public boolean equals( Rect2i rect )
    {
        if ( rect == null )
            return ( false );
        
        if ( rect == this )
            return ( true );
        
        return ( ( rect.getLeft() == this.getLeft() ) && ( rect.getTop() == this.getTop() ) && ( rect.getWidth() == this.getWidth() ) && ( rect.getHeight() == this.getHeight() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( o == null )
            return ( false );
        
        if ( !( o instanceof Rect2i ) )
            return ( false );
        
        return ( equals( (Rect2i)o ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( this.getClass().getSimpleName() + "( left = " + getLeft() + ", top = " + getTop() + ", width = " + getWidth() + ", height = " + getHeight() + ", aspect = " + getAspect() + " )" );
    }
    
    /**
     * Creates a new 2-dimensional rectangle.
     * 
     * @param left the upper-left corner's x-coordinate
     * @param top the upper-left corner's y-coordinate
     * @param width the rectangle's width
     * @param height the rectangle's height
     */
    public Rect2i( int left, int top, int width, int height )
    {
        this.left = left;
        this.top = top;
        
        this.width = width;
        this.height = height;
    }
    
    /**
     * Creates a new 2-dimensional rectangle and copies the template's coordinates.
     * 
     * @param template
     */
    public Rect2i( Rect2i template )
    {
        this( template.getLeft(), template.getTop(), template.getWidth(), template.getHeight() );
    }
    
    /**
     * Creates a new 2-dimensional rectangle with zero position and size.
     */
    public Rect2i()
    {
        this( 0, 0, 0, 0 );
    }
    
    public static final Rect2i fromPool()
    {
        return ( POOL.alloc() );
    }
    
    public static final Rect2i fromPool( int left, int top, int width, int height )
    {
        Rect2i inst = POOL.alloc();
        
        inst.set( left, top, width, height );
        
        return ( inst );
    }
    
    public static final void toPool( Rect2i rect )
    {
        POOL.free( rect );
    }
}
