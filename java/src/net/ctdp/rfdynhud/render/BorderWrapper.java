/**
 * Copyright (c) 2003-2009, Xith3D Project Group all rights reserved.
 * 
 * Portions based on the Java3D interface, Copyright by Sun Microsystems.
 * Many thanks to the developers of Java3D and Sun Microsystems for their
 * innovation and design.
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
 * Neither the name of the 'Xith3D Project Group' nor the names of its
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
package net.ctdp.rfdynhud.render;


/**
 * A ColoredBorder is a Border implementation with no Textures but only a
 * color.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class BorderWrapper
{
    private final TexturedBorder border;
    
    /**
     * Returns the border.
     * 
     * @return the border.
     */
    public final TexturedBorder getBorder()
    {
        return ( border );
    }
    
    public final int getBottomHeight()
    {
        return ( border != null ? border.getBottomHeight() : 0 );
    }
    
    public final int getRightWidth()
    {
        return ( border != null ? border.getRightWidth() : 0 );
    }
    
    public final int getTopHeight()
    {
        return ( border != null ? border.getTopHeight() : 0 );
    }
    
    public final int getLeftWidth()
    {
        return ( border != null ? border.getLeftWidth() : 0 );
    }
    
    public final int getOpaqueBottomHeight()
    {
        return ( border != null ? border.getOpaqueBottomHeight() : 0 );
    }
    
    public final int getOpaqueRightWidth()
    {
        return ( border != null ? border.getOpaqueRightWidth() : 0 );
    }
    
    public final int getOpaqueTopHeight()
    {
        return ( border != null ? border.getOpaqueTopHeight() : 0 );
    }
    
    public final int getOpaqueLeftWidth()
    {
        return ( border != null ? border.getOpaqueLeftWidth() : 0 );
    }
    
    public final int getInnerBottomHeight()
    {
        return ( border != null ? border.getInnerBottomHeight() : 0 );
    }
    
    public final int getInnerRightWidth()
    {
        return ( border != null ? border.getInnerRightWidth() : 0 );
    }
    
    public final int getInnerTopHeight()
    {
        return ( border != null ? border.getInnerTopHeight() : 0 );
    }
    
    public final int getInnerLeftWidth()
    {
        return ( border != null ? border.getInnerLeftWidth() : 0 );
    }
    
    public final int getPaddingLeft()
    {
        if ( border == null )
            return ( 0 );
        
        return ( border.getLeftWidth() - border.getOpaqueLeftWidth() );
    }
    
    public final int getPaddingTop()
    {
        if ( border == null )
            return ( 0 );
        
        return ( border.getTopHeight() - border.getOpaqueTopHeight() );
    }
    
    public final int getPaddingBottom()
    {
        if ( border == null )
            return ( 0 );
        
        return ( border.getBottomHeight() - border.getOpaqueBottomHeight() );
    }
    
    public final int getPaddingRight()
    {
        if ( border == null )
            return ( 0 );
        
        return ( border.getRightWidth() - border.getOpaqueRightWidth() );
    }
    
    public final int getWidgetWidth( int widthWithoutInner )
    {
        if ( border == null )
            return ( widthWithoutInner );
        
        return ( widthWithoutInner + border.getInnerLeftWidth() + border.getInnerRightWidth() );
    }
    
    public final int getWidgetHeight( int heightWithoutInner )
    {
        if ( border == null )
            return ( heightWithoutInner );
        
        return ( heightWithoutInner + border.getInnerTopHeight() + border.getInnerBottomHeight() );
    }
    
    public final int getLLupperHeight()
    {
        return ( border != null ? border.getLLupperHeight() : 0 );
    }
    
    public final int getLLrightWidth()
    {
        return ( border != null ? border.getLLrightWidth() : 0 );
    }
    
    public final int getLRleftWidth()
    {
        return ( border != null ? border.getLRleftWidth() : 0 );
    }
    
    public final int getLRupperHeight()
    {
        return ( border != null ? border.getLRupperHeight() : 0 );
    }
    
    public final int getURlowerHeight()
    {
        return ( border != null ? border.getURlowerHeight() : 0 );
    }
    
    public final int getURleftWidth()
    {
        return ( border != null ? border.getURleftWidth() : 0 );
    }
    
    public final int getULrightWidth()
    {
        return ( border != null ? border.getULrightWidth() : 0 );
    }
    
    public final int getULlowerHeight()
    {
        return ( border != null ? border.getULlowerHeight() : 0 );
    }
    
    /**
     * Creates a new BorderWrapper encapsulating the given border.
     * 
     * @param border
     */
    public BorderWrapper( TexturedBorder border )
    {
        this.border = border;
    }
}
