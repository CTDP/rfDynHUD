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
package net.ctdp.rfdynhud.render;

import java.awt.Color;

import net.ctdp.rfdynhud.properties.IntProperty;


/**
 * A ColoredBorder is a Border implementation with no Textures but only a
 * color.
 * 
 * @author Marvin Froehlich (CTDP) (aka Qudus)
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
    
    public final int getInnerTopHeightWOPadding()
    {
        return ( ( measures != null ? measures.getInnerTopHeight() : 0 ) );
    }
    
    public final int getInnerLeftWidthWOPadding()
    {
        return ( ( measures != null ? measures.getInnerLeftWidth() : 0 ) );
    }
    
    public final int getInnerRightWidthWOPadding()
    {
        return ( ( measures != null ? measures.getInnerRightWidth() : 0 ) );
    }
    
    public final int getInnerBottomHeightWOPadding()
    {
        return ( ( measures != null ? measures.getInnerBottomHeight() : 0 ) );
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
     * @param renderer the renderer
     * @param measures the measures
     * @param paddingTop top padding property
     * @param paddingLeft left padding property
     * @param paddingRight right padding property
     * @param paddingBottom bottom padding property
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
