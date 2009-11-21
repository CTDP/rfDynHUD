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
public class TexturedBorder
{
    private TextureImage2D texture;
    
    private int leftWidth;
    private int rightWidth;
    private int topHeight;
    private int bottomHeight;
    
    private int opaqueLeftWidth;
    private int opaqueRightWidth;
    private int opaqueTopHeight;
    private int opaqueBottomHeight;
    
    private int innerLeftWidth;
    private int innerRightWidth;
    private int innerTopHeight;
    private int innerBottomHeight;
    
    private int llUpperHeight = 0;
    private int llRightWidth = 0;
    private int lrLeftWidth = 0;
    private int lrUpperHeight = 0;
    private int urLowerHeight = 0;
    private int urLeftWidth = 0;
    private int ulRightWidth = 0;
    private int ulLowerHeight = 0;
    
    /**
     * Sets the border's texture.
     * 
     * @param color
     */
    public void setTexture( TextureImage2D texture )
    {
        if ( texture == null )
        {
            throw new NullPointerException( "texture parameter MUST NOT be null." );
        }
        
        this.texture = texture;
    }
    
    /**
     * Returns the border's texture.
     * 
     * @return the border's texture.
     */
    public final TextureImage2D getTexture()
    {
        return ( texture );
    }
    
    public void setBottomHeight( int bh )
    {
        this.bottomHeight = bh;
    }
    
    public final int getBottomHeight()
    {
        return ( bottomHeight );
    }
    
    public void setRightWidth( int rw )
    {
        this.rightWidth = rw;
    }
    
    public final int getRightWidth()
    {
        return ( rightWidth );
    }
    
    public void setTopHeight( int th )
    {
        this.topHeight = th;
    }
    
    public final int getTopHeight()
    {
        return ( topHeight );
    }
    
    public void setLeftWidth( int lw )
    {
        this.leftWidth = lw;
    }
    
    public final int getLeftWidth()
    {
        return ( leftWidth );
    }
    
    public void setOpaqueBottomHeight( int bh )
    {
        this.opaqueBottomHeight = bh;
    }
    
    public final int getOpaqueBottomHeight()
    {
        return ( opaqueBottomHeight );
    }
    
    public void setOpaqueRightWidth( int rw )
    {
        this.opaqueRightWidth = rw;
    }
    
    public final int getOpaqueRightWidth()
    {
        return ( opaqueRightWidth );
    }
    
    public void setOpaqueTopHeight( int th )
    {
        this.opaqueTopHeight = th;
    }
    
    public final int getOpaqueTopHeight()
    {
        return ( opaqueTopHeight );
    }
    
    public void setOpaqueLeftWidth( int lw )
    {
        this.opaqueLeftWidth = lw;
    }
    
    public final int getOpaqueLeftWidth()
    {
        return ( opaqueLeftWidth );
    }
    
    public void setInnerBottomHeight( int bh )
    {
        this.innerBottomHeight = bh;
    }
    
    public final int getInnerBottomHeight()
    {
        return ( innerBottomHeight );
    }
    
    public void setInnerRightWidth( int rw )
    {
        this.innerRightWidth = rw;
    }
    
    public final int getInnerRightWidth()
    {
        return ( innerRightWidth );
    }
    
    public void setInnerTopHeight( int th )
    {
        this.innerTopHeight = th;
    }
    
    public final int getInnerTopHeight()
    {
        return ( innerTopHeight );
    }
    
    public void setInnerLeftWidth( int lw )
    {
        this.innerLeftWidth = lw;
    }
    
    public final int getInnerLeftWidth()
    {
        return ( innerLeftWidth );
    }
    
    public void setLLupperHeight( int value )
    {
        this.llUpperHeight = value;
    }
    
    public final int getLLupperHeight()
    {
        return ( llUpperHeight );
    }
    
    public void setLLrightWidth( int value )
    {
        this.llRightWidth = value;
    }
    
    public final int getLLrightWidth()
    {
        return ( llRightWidth );
    }
    
    public void setLRleftWidth( int value )
    {
        this.lrLeftWidth = value;
    }
    
    public final int getLRleftWidth()
    {
        return ( lrLeftWidth );
    }
    
    public void setLRupperHeight( int value )
    {
        this.lrUpperHeight = value;
    }
    
    public final int getLRupperHeight()
    {
        return ( lrUpperHeight );
    }
    
    public void setURlowerHeight( int value )
    {
        this.urLowerHeight = value;
    }
    
    public final int getURlowerHeight()
    {
        return ( urLowerHeight );
    }
    
    public void setURleftWidth( int value )
    {
        this.urLeftWidth = value;
    }
    
    public final int getURleftWidth()
    {
        return ( urLeftWidth );
    }
    
    public void setULrightWidth( int value )
    {
        this.ulRightWidth = value;
    }
    
    public final int getULrightWidth()
    {
        return ( ulRightWidth );
    }
    
    public void setULlowerHeight( int value )
    {
        this.ulLowerHeight = value;
    }
    
    public final int getULlowerHeight()
    {
        return ( ulLowerHeight );
    }
    
    public void drawBorder( Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        TextureImage2D image = texture;
        
        int srcW = texture.getWidth();
        int srcH = texture.getHeight();
        
        int srcBottomH = getBottomHeight();
        int srcRightW = getRightWidth();
        int srcTopH = getTopHeight();
        int srcLeftW = getLeftWidth();
        
        int ll_upper = getLLupperHeight();
        int ll_right = getLLrightWidth();
        int lr_left = getLRleftWidth();
        int lr_upper = getLRupperHeight();
        int ur_lower = getURlowerHeight();
        int ur_left = getURleftWidth();
        int ul_right = getULrightWidth();
        int ul_lower = getULlowerHeight();
        
        int srcRightLeft = srcW - srcRightW;
        int srcBottomTop = srcH - srcBottomH;
        int trgRightLeft = width - srcRightW;
        int trgBottomTop = height - srcBottomH;
        
        final TextureImage2D ti = texCanvas.getImage();
        
        final boolean markDirty = false;
        
        // render corners...
        if ( ( srcLeftW > 0 ) && ( srcBottomH > 0 ) )
            ti.clear( image, 0, srcBottomTop, srcLeftW, srcBottomH, offsetX + 0, offsetY + trgBottomTop, markDirty, null );
        if ( ( srcRightW > 0 ) && ( srcBottomH > 0 ) )
            ti.clear( image, srcRightLeft, srcBottomTop, srcRightW, srcBottomH, offsetX + trgRightLeft, offsetY + trgBottomTop, markDirty, null );
        if ( ( srcRightW > 0 ) && ( srcTopH > 0 ) )
            ti.clear( image, srcRightLeft, 0, srcRightW, srcTopH, offsetX + trgRightLeft, offsetY, markDirty, null );
        if ( ( srcLeftW > 0 ) && ( srcTopH > 0 ) )
            ti.clear( image, 0, 0, srcLeftW, srcTopH, offsetX, offsetY, markDirty, null );
        
        // render extended corners...
        if ( ll_right > 0 )
            ti.clear( image, srcLeftW, srcBottomTop, ll_right, srcBottomH, offsetX + srcLeftW, offsetY + trgBottomTop, ll_right, srcBottomH, markDirty, null );
        if ( lr_left > 0 )
            ti.clear( image, srcRightLeft - lr_left, srcBottomTop, lr_left, srcBottomH, offsetX + trgRightLeft - lr_left, offsetY + trgBottomTop, lr_left, srcBottomH, markDirty, null );
        if ( lr_upper > 0 )
            ti.clear( image, srcRightLeft, srcBottomTop - lr_upper, srcRightW, lr_upper, offsetX + trgRightLeft, offsetY + trgBottomTop - lr_upper, srcRightW, lr_upper, markDirty, null );
        if ( ur_lower > 0 )
            ti.clear( image, srcRightLeft, srcTopH, srcRightW, ul_lower, offsetX + trgRightLeft, offsetY + srcTopH, srcRightW, ul_lower, markDirty, null );
        if ( ur_left > 0 )
            ti.clear( image, srcRightLeft - ur_left, 0, ur_left, srcTopH, offsetX + trgRightLeft - ur_left, offsetY, ur_left, srcTopH, markDirty, null );
        if ( ul_right > 0 )
            ti.clear( image, srcLeftW, 0, ul_right, srcTopH, offsetX + srcLeftW, offsetY, ul_right, srcTopH, markDirty, null );
        if ( ul_lower > 0 )
            ti.clear( image, 0, srcTopH, srcLeftW, ul_lower, offsetX, offsetY + srcTopH, srcLeftW, ul_lower, markDirty, null );
        if ( ll_upper > 0 )
            ti.clear( image, 0, srcBottomTop - ll_upper, srcLeftW, ll_upper, offsetX, offsetY + trgBottomTop - ll_upper, srcLeftW, ll_upper, markDirty, null );
        
        // render edges...
        if ( srcBottomH > 0 )
            ti.clear( image, srcLeftW + ll_right, srcBottomTop, srcW - srcLeftW - ll_right - lr_left - srcRightW, srcBottomH, offsetX + srcLeftW + ll_right, offsetY + trgBottomTop, width - srcLeftW - ll_right - lr_left - srcRightW, srcBottomH, markDirty, null );
        if ( srcRightW > 0 )
            ti.clear( image, srcRightLeft, srcTopH + ur_lower, srcRightW, srcH - srcTopH - ur_lower - lr_upper - srcBottomH, offsetX + trgRightLeft, offsetY + srcTopH + ur_lower, srcRightW, height - srcTopH - ur_lower - lr_upper - srcBottomH, markDirty, null );
        if ( srcTopH > 0 )
            ti.clear( image, srcLeftW + ul_right, 0, srcW - ul_right - ur_left - srcLeftW - srcRightW, srcTopH, offsetX + srcLeftW + ul_right, offsetY, width - srcLeftW - ul_right - ur_left - srcRightW, srcTopH, markDirty, null );
        if ( srcLeftW > 0 )
            ti.clear( image, 0, srcTopH + ul_lower, srcLeftW, srcH - srcTopH - ul_lower - ll_upper - srcBottomH, offsetX, offsetY + srcTopH + ul_lower, srcLeftW, height - srcTopH - ul_lower - ll_upper - srcBottomH, markDirty, null );
    }
    
    /**
     * Creates a new TexturedBorder with the given side widths.
     * 
     * @param texture
     * @param bottomHeight
     * @param rightWidth
     * @param topHeight
     * @param leftWidth
     */
    public TexturedBorder( TextureImage2D texture, int bottomHeight, int rightWidth, int topHeight, int leftWidth )
    {
        this.texture = texture;
        
        this.bottomHeight = bottomHeight;
        this.rightWidth = rightWidth;
        this.topHeight = topHeight;
        this.leftWidth = leftWidth;
    }
    
    /**
     * Creates a new TexturedBorder with all sides of the same width.
     * 
     * @param texture
     * @param width
     */
    public TexturedBorder( TextureImage2D texture, int width )
    {
        this( texture, width, width, width, width );
    }
}
