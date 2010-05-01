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

import java.awt.Color;


/**
 * A ColoredBorder is a Border implementation with no Textures but only a
 * color.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class ImageBorderRenderer extends BorderRenderer
{
    private final String imageFilename;
    private TextureImage2D borderTexture;
    
    public final String getImageFilename()
    {
        return ( imageFilename );
    }
    
    /**
     * Returns the border's texture.
     * 
     * @return the border's texture.
     */
    public final TextureImage2D getTexture()
    {
        return ( borderTexture );
    }
    
    public static void drawBorderFromTexture( TextureImage2D borderTexture, BorderMeasures measures, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        TextureImage2D image = borderTexture;
        
        int srcW = borderTexture.getWidth();
        int srcH = borderTexture.getHeight();
        
        int srcBottomH = measures.getBottomHeight();
        int srcRightW = measures.getRightWidth();
        int srcTopH = measures.getTopHeight();
        int srcLeftW = measures.getLeftWidth();
        
        int ll_upper = measures.getLLupperHeight();
        int ll_right = measures.getLLrightWidth();
        int lr_left = measures.getLRleftWidth();
        int lr_upper = measures.getLRupperHeight();
        int ur_lower = measures.getURlowerHeight();
        int ur_left = measures.getURleftWidth();
        int ul_right = measures.getULrightWidth();
        int ul_lower = measures.getULlowerHeight();
        
        int srcRightLeft = srcW - srcRightW;
        int srcBottomTop = srcH - srcBottomH;
        int trgRightLeft = width - srcRightW;
        int trgBottomTop = height - srcBottomH;
        
        final boolean markDirty = false;
        
        // render corners...
        if ( ( srcLeftW > 0 ) && ( srcBottomH > 0 ) )
            texture.clear( image, 0, srcBottomTop, srcLeftW, srcBottomH, offsetX + 0, offsetY + trgBottomTop, markDirty, null );
        if ( ( srcRightW > 0 ) && ( srcBottomH > 0 ) )
            texture.clear( image, srcRightLeft, srcBottomTop, srcRightW, srcBottomH, offsetX + trgRightLeft, offsetY + trgBottomTop, markDirty, null );
        if ( ( srcRightW > 0 ) && ( srcTopH > 0 ) )
            texture.clear( image, srcRightLeft, 0, srcRightW, srcTopH, offsetX + trgRightLeft, offsetY, markDirty, null );
        if ( ( srcLeftW > 0 ) && ( srcTopH > 0 ) )
            texture.clear( image, 0, 0, srcLeftW, srcTopH, offsetX, offsetY, markDirty, null );
        
        // render extended corners...
        if ( ll_right > 0 )
            texture.clear( image, srcLeftW, srcBottomTop, ll_right, srcBottomH, offsetX + srcLeftW, offsetY + trgBottomTop, ll_right, srcBottomH, markDirty, null );
        if ( lr_left > 0 )
            texture.clear( image, srcRightLeft - lr_left, srcBottomTop, lr_left, srcBottomH, offsetX + trgRightLeft - lr_left, offsetY + trgBottomTop, lr_left, srcBottomH, markDirty, null );
        if ( lr_upper > 0 )
            texture.clear( image, srcRightLeft, srcBottomTop - lr_upper, srcRightW, lr_upper, offsetX + trgRightLeft, offsetY + trgBottomTop - lr_upper, srcRightW, lr_upper, markDirty, null );
        if ( ur_lower > 0 )
            texture.clear( image, srcRightLeft, srcTopH, srcRightW, ul_lower, offsetX + trgRightLeft, offsetY + srcTopH, srcRightW, ul_lower, markDirty, null );
        if ( ur_left > 0 )
            texture.clear( image, srcRightLeft - ur_left, 0, ur_left, srcTopH, offsetX + trgRightLeft - ur_left, offsetY, ur_left, srcTopH, markDirty, null );
        if ( ul_right > 0 )
            texture.clear( image, srcLeftW, 0, ul_right, srcTopH, offsetX + srcLeftW, offsetY, ul_right, srcTopH, markDirty, null );
        if ( ul_lower > 0 )
            texture.clear( image, 0, srcTopH, srcLeftW, ul_lower, offsetX, offsetY + srcTopH, srcLeftW, ul_lower, markDirty, null );
        if ( ll_upper > 0 )
            texture.clear( image, 0, srcBottomTop - ll_upper, srcLeftW, ll_upper, offsetX, offsetY + trgBottomTop - ll_upper, srcLeftW, ll_upper, markDirty, null );
        
        // render edges...
        if ( srcBottomH > 0 )
            texture.clear( image, srcLeftW + ll_right, srcBottomTop, srcW - srcLeftW - ll_right - lr_left - srcRightW, srcBottomH, offsetX + srcLeftW + ll_right, offsetY + trgBottomTop, width - srcLeftW - ll_right - lr_left - srcRightW, srcBottomH, markDirty, null );
        if ( srcRightW > 0 )
            texture.clear( image, srcRightLeft, srcTopH + ur_lower, srcRightW, srcH - srcTopH - ur_lower - lr_upper - srcBottomH, offsetX + trgRightLeft, offsetY + srcTopH + ur_lower, srcRightW, height - srcTopH - ur_lower - lr_upper - srcBottomH, markDirty, null );
        if ( srcTopH > 0 )
            texture.clear( image, srcLeftW + ul_right, 0, srcW - ul_right - ur_left - srcLeftW - srcRightW, srcTopH, offsetX + srcLeftW + ul_right, offsetY, width - srcLeftW - ul_right - ur_left - srcRightW, srcTopH, markDirty, null );
        if ( srcLeftW > 0 )
            texture.clear( image, 0, srcTopH + ul_lower, srcLeftW, srcH - srcTopH - ul_lower - ll_upper - srcBottomH, offsetX, offsetY + srcTopH + ul_lower, srcLeftW, height - srcTopH - ul_lower - ll_upper - srcBottomH, markDirty, null );
    }
    
    @Override
    public void drawBorder( Color backgroundColor, BorderMeasures measures, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        drawBorderFromTexture( borderTexture, measures, texture, offsetX, offsetY, width, height );
    }
    
    public ImageBorderRenderer( String imageFilename, TextureImage2D image )
    {
        this.imageFilename = imageFilename;
        this.borderTexture = image;
    }
}
