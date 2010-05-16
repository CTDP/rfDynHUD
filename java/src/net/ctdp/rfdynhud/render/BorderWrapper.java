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

import net.ctdp.rfdynhud.properties.IntProperty;


/**
 * A ColoredBorder is a Border implementation with no Textures but only a
 * color.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class BorderWrapper
{
    private final BorderRenderer renderer;
    private final BorderMeasures measures;
    
    private final IntProperty paddingTop;
    private final IntProperty paddingLeft;
    private final IntProperty paddingRight;
    private final IntProperty paddingBottom;
    
    public final BorderRenderer getRenderer()
    {
        return ( renderer );
    }
    
    /**
     * Returns whether this border has something to draw.
     * 
     * @return whether this border has something to draw.
     */
    public final boolean hasBorder()
    {
        return ( renderer != null );
    }
    
    public final int getTopHeight()
    {
        return ( ( measures != null ? measures.getTopHeight() : 0 ) + ( paddingTop != null ? paddingTop.getIntValue() : 0 ) );
    }
    
    public final int getLeftWidth()
    {
        return ( ( measures != null ? measures.getLeftWidth() : 0 ) + ( paddingLeft != null ? paddingLeft.getIntValue() : 0 ) );
    }
    
    public final int getRightWidth()
    {
        return ( ( measures != null ? measures.getRightWidth() : 0 ) + ( paddingRight != null ? paddingRight.getIntValue() : 0 ) );
    }
    
    public final int getBottomHeight()
    {
        return ( ( measures != null ? measures.getBottomHeight() : 0 ) + ( paddingBottom != null ? paddingBottom.getIntValue() : 0 ) );
    }
    
    public final int getOpaqueTopHeight()
    {
        return ( ( measures != null ? measures.getOpaqueTopHeight() : 0 ) + ( paddingTop != null ? paddingTop.getIntValue() : 0 ) );
    }
    
    public final int getOpaqueLeftWidth()
    {
        return ( ( measures != null ? measures.getOpaqueLeftWidth() : 0 ) + ( paddingLeft != null ? paddingLeft.getIntValue() : 0 ) );
    }
    
    public final int getOpaqueRightWidth()
    {
        return ( ( measures != null ? measures.getOpaqueRightWidth() : 0 ) + ( paddingRight != null ? paddingRight.getIntValue() : 0 ) );
    }
    
    public final int getOpaqueBottomHeight()
    {
        return ( ( measures != null ? measures.getOpaqueBottomHeight() : 0 ) + ( paddingBottom != null ? paddingBottom.getIntValue() : 0 ) );
    }
    
    public final int getInnerTopHeight()
    {
        return ( ( measures != null ? measures.getInnerTopHeight() : 0 ) + ( paddingTop != null ? paddingTop.getIntValue() : 0 ) );
    }
    
    public final int getInnerLeftWidth()
    {
        return ( ( measures != null ? measures.getInnerLeftWidth() : 0 ) + ( paddingLeft != null ? paddingLeft.getIntValue() : 0 ) );
    }
    
    public final int getInnerRightWidth()
    {
        return ( ( measures != null ? measures.getInnerRightWidth() : 0 ) + ( paddingRight != null ? paddingRight.getIntValue() : 0 ) );
    }
    
    public final int getInnerBottomHeight()
    {
        return ( ( measures != null ? measures.getInnerBottomHeight() : 0 ) + ( paddingBottom != null ? paddingBottom.getIntValue() : 0 ) );
    }
    
    public final int getPaddingTop()
    {
        return ( paddingTop != null ? paddingTop.getIntValue() : 0 );
    }
    
    public final int getPaddingLeft()
    {
        return ( paddingLeft != null ? paddingLeft.getIntValue() : 0 );
    }
    
    public final int getPaddingRight()
    {
        return ( paddingRight != null ? paddingRight.getIntValue() : 0 );
    }
    
    public final int getPaddingBottom()
    {
        return ( paddingBottom != null ? paddingBottom.getIntValue() : 0 );
    }
    
    public final int getWidgetWidth( int widthWithoutInner )
    {
        if ( measures == null )
            return ( widthWithoutInner );
        
        return ( widthWithoutInner + measures.getInnerLeftWidth() + measures.getInnerRightWidth() );
    }
    
    public final int getWidgetHeight( int heightWithoutInner )
    {
        if ( measures == null )
            return ( heightWithoutInner );
        
        return ( heightWithoutInner + measures.getInnerTopHeight() + measures.getInnerBottomHeight() );
    }
    
    public final int getLLupperHeight()
    {
        return ( measures != null ? measures.getLLupperHeight() : 0 );
    }
    
    public final int getLLrightWidth()
    {
        return ( measures != null ? measures.getLLrightWidth() : 0 );
    }
    
    public final int getLRleftWidth()
    {
        return ( measures != null ? measures.getLRleftWidth() : 0 );
    }
    
    public final int getLRupperHeight()
    {
        return ( measures != null ? measures.getLRupperHeight() : 0 );
    }
    
    public final int getURlowerHeight()
    {
        return ( measures != null ? measures.getURlowerHeight() : 0 );
    }
    
    public final int getURleftWidth()
    {
        return ( measures != null ? measures.getURleftWidth() : 0 );
    }
    
    public final int getULrightWidth()
    {
        return ( measures != null ? measures.getULrightWidth() : 0 );
    }
    
    public final int getULlowerHeight()
    {
        return ( measures != null ? measures.getULlowerHeight() : 0 );
    }
    
    public void drawBorder( Color backgroundColor, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( !hasBorder() )
            return;
        
        texture.getTextureCanvas().pushClip( offsetX, offsetY, width, height );
        
        try
        {
            renderer.drawBorder( backgroundColor, measures, texture, offsetX, offsetY, width, height );
        }
        finally
        {
            texture.getTextureCanvas().popClip();
        }
    }
    
    /**
     * Creates a new BorderWrapper encapsulating the given border.
     * 
     * @param renderer
     * @param measures
     */
    public BorderWrapper( BorderRenderer renderer, BorderMeasures measures, IntProperty paddingTop, IntProperty paddingLeft, IntProperty paddingRight, IntProperty paddingBottom )
    {
        this.renderer = renderer;
        this.measures = measures;
        
        this.paddingTop = paddingTop;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.paddingBottom = paddingBottom;
    }
}
