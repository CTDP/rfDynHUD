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
package net.ctdp.rfdynhud.render;

import java.awt.geom.Rectangle2D;

import net.ctdp.rfdynhud.widgets.base.widget.Widget;

import org.openmali.types.twodee.Rect2i;

/**
 * A {@link DrawnString} is an abstraction of a String to be drawn.
 * It handles alignment and clearing.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DrawnString
{
    public static enum Alignment
    {
        LEFT,
        CENTER,
        RIGHT,
        ;
    }
    
    private static final String MAX_HEIGHT_STRING = "yg0O9" + (char)196;
    
    private final Widget widget;
    
    private final String name;
    
    private final DrawnString xRelativeTo;
    private final DrawnString yRelativeTo;
    
    private final int x;
    private final int y;
    
    private final Alignment alignment;
    private final boolean y_at_baseline;
    
    private final java.awt.Font font;
    private final boolean fontAntiAliased;
    private final java.awt.Color fontColor;
    
    private int maxWidth = -1;
    private int maxHeight = -1;
    private int fontDescent = 0;
    
    public final Rect2i clearRect = new Rect2i( -1, -1, 0, 0 );
    
    private final String prefix;
    private final String postfix;
    
    public final String getName()
    {
        return ( name );
    }
    
    public void resetClearRect()
    {
        clearRect.set( -1, -1, 0, 0 );
    }
    
    public final DrawnString getXRelativeTo()
    {
        return ( xRelativeTo );
    }
    
    public final DrawnString getYRelativeTo()
    {
        return ( yRelativeTo );
    }
    
    /**
     * Gets the x-location of the String.
     * 
     * @see #getAlignment()
     * 
     * @return the x-location of the String.
     */
    public final int getX()
    {
        return ( x );
    }
    
    /**
     * Gets the y-location of the String.
     * 
     * @see #isYAtBaseline()
     * 
     * @return the y-location of the String.
     */
    public final int getY()
    {
        return ( y );
    }
    
    /**
     * Gets the x-location of the String with respect to "relativeTo".
     * 
     * @see #getAlignment()
     * 
     * @return the x-location of the String.
     */
    public final int getAbsX()
    {
        if ( xRelativeTo != null )
        {
            if ( getAlignment() == Alignment.CENTER )
            {
                if ( xRelativeTo.getAlignment() == Alignment.LEFT )
                    return ( xRelativeTo.getAbsX() + ( xRelativeTo.maxWidth / 2 ) + x );
                
                if ( xRelativeTo.getAlignment() == Alignment.CENTER )
                    return ( xRelativeTo.getAbsX() + x );
                
                //if ( xRelativeTo.getAlignment() == Alignment.RIGHT )
                    return ( xRelativeTo.getAbsX() - ( xRelativeTo.maxWidth / 2 ) + x );
            }
            
            if ( xRelativeTo.getAlignment() == Alignment.CENTER )
                return ( xRelativeTo.getAbsX() + ( xRelativeTo.maxWidth / 2 ) + x );
            
            if ( xRelativeTo.getAlignment() == Alignment.RIGHT )
                return ( xRelativeTo.getAbsX() + x );
            
            return ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + x );
        }
        
        return ( x );
    }
    
    /**
     * Gets the y-location of the String with respect to "relativeTo".
     * 
     * @see #isYAtBaseline()
     * 
     * @return the y-location of the String.
     */
    public final int getAbsY()
    {
        if ( yRelativeTo != null )
            return ( yRelativeTo.getAbsY() + yRelativeTo.maxHeight - fontDescent + y );
        
        return ( y );
    }
    
    /**
     * Gets the alignment affecting the x-location.
     * 
     * @return the alignment affecting the x-location.
     */
    public final Alignment getAlignment()
    {
        return ( alignment );
    }
    
    /**
     * Gets, whether the y-coordinate indicates the String's baseline position or the top-bound.
     * 
     * @return whether the y-coordinate indicates the String's baseline position or the top-bound.
     */
    public final boolean isYAtBaseline()
    {
        return ( y_at_baseline );
    }
    
    /**
     * Gets the used Font.
     * 
     * @return the used Font.
     */
    public final java.awt.Font getFont()
    {
        return ( font );
    }
    
    /**
     * Gets the used Font-Color.
     * 
     * @return the used Font-Color.
     */
    public final java.awt.Color getFontColor()
    {
        return ( fontColor );
    }
    
    /**
     * Gets the prefix.
     * 
     * @return the prefix.
     */
    public final String getPrefix()
    {
        return ( prefix );
    }
    
    /**
     * Gets the postfix.
     * 
     * @return the postfix.
     */
    public final String getPostfix()
    {
        return ( postfix );
    }
    
    /**
     * Gets the drawn string's width in pixels.
     * 
     * @param str the string to draw
     * 
     * @return the drawn string's width in pixels.
     */
    public int getWidth( String str )
    {
        String s;
        if ( ( prefix != null ) && ( postfix != null ) )
            s = prefix + str + postfix;
        else if ( prefix != null )
            s = prefix + str;
        else if ( postfix != null )
            s = str + postfix;
        else
            s = str;
        
        Rectangle2D bounds = TextureImage2D.getStringBounds( s, font, fontAntiAliased );
        
        return ( (int)Math.round( bounds.getWidth() ) );
    }
    
    /**
     * Gets the drawn strings' minimum column-widths and the total width in pixels.
     * 
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding padding per column
     * @param colWidths the array to write column widths to
     * 
     * @return the drawn strings' minimum total width in pixels.
     */
    public int getMinColWidths( String[] strs, Alignment[] aligns, int padding, int[] colWidths )
    {
        int total = 0;
        int w;
        String str;
        
        for ( int i = 0; i < strs.length; i++ )
        {
            Alignment align = aligns == null ? getAlignment() : ( aligns[i] == null ? getAlignment() : aligns[i] );
            
            if ( ( i == 0 ) && ( prefix != null ) )
            {
                if ( strs[i] == null )
                    str = prefix;
                else
                    str = prefix + strs[i];
            }
            else if ( ( i == strs.length - 1 ) && ( postfix != null ) )
            {
                if ( strs[i] == null )
                    str = postfix;
                else
                    str = strs[i] + postfix;
            }
            else
            {
                str = strs[i];
            }
            
            w = 0;
            
            if ( str != null )
            {
                int pad = padding;
                if ( ( i == 0 ) && ( align == Alignment.LEFT ) )
                    pad = 0;
                else if ( ( i == strs.length - 1 ) && ( align == Alignment.RIGHT ) )
                    pad = 0;
                
                w = (int)Math.round( TextureImage2D.getStringBounds( str, font, fontAntiAliased ).getWidth() ) + pad;
                
                total += w;
            }
            
            if ( colWidths != null )
                colWidths[i] = w;
        }
        
        return ( total );
    }
    
    /**
     * Gets the drawn strings' maximum column-widths and the total width in pixels.
     * 
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding padding per column
     * @param colWidths the array to write column widths to
     * 
     * @return the drawn strings' maximum total width in pixels.
     */
    public int getMaxColWidths( String[] strs, Alignment[] aligns, int padding, int[] colWidths )
    {
        int total = 0;
        int w;
        String str;
        
        for ( int i = 0; i < strs.length; i++ )
        {
            Alignment align = aligns == null ? getAlignment() : ( aligns[i] == null ? getAlignment() : aligns[i] );
            
            if ( ( i == 0 ) && ( prefix != null ) )
            {
                if ( strs[i] == null )
                    str = prefix;
                else
                    str = prefix + strs[i];
            }
            else if ( ( i == strs.length - 1 ) && ( postfix != null ) )
            {
                if ( strs[i] == null )
                    str = postfix;
                else
                    str = strs[i] + postfix;
            }
            else
            {
                str = strs[i];
            }
            
            if ( str != null )
            {
                int pad = padding;
                if ( ( i == 0 ) && ( align == Alignment.LEFT ) )
                    pad = 0;
                else if ( ( i == strs.length - 1 ) && ( align == Alignment.RIGHT ) )
                    pad = 0;
                
                w = (int)Math.round( TextureImage2D.getStringBounds( str, font, fontAntiAliased ).getWidth() ) + pad;
                
                colWidths[i] = Math.max( colWidths[i], w );
            }
            
            total += colWidths[i];
        }
        
        return ( total );
    }
    
    /**
     * Gets the last drawn string's width.<br />
     * This value will be invalid, if this {@link DrawnString} has not yet been drawn.
     * 
     * @return the last drawn string's width.
     */
    public final int getLastWidth()
    {
        return ( maxWidth );
    }
    
    private int calcMaxHeight( java.awt.Font font, boolean antiAliased )
    {
        if ( maxHeight > 0 )
            return ( maxHeight );
        
        Rectangle2D bounds = TextureImage2D.getStringBounds( MAX_HEIGHT_STRING, font, antiAliased );
        
        this.fontDescent = TextureImage2D.getFontDescent( font );
        this.maxHeight = (int)( bounds.getHeight() + fontDescent );
        
        return ( maxHeight );
    }
    
    /**
     * Gets the last drawn string's height.<br />
     * This value will be invalid, if this {@link DrawnString} has not yet been drawn.
     * 
     * @param includingDescent include font descent?
     * 
     * @return the last drawn string's height.
     */
    public final int getMaxHeight( boolean includingDescent )
    {
        if ( includingDescent )
            return ( maxHeight );
        
        return ( maxHeight - fontDescent );
    }
    
    /**
     * Gets the last drawn string's height.<br />
     * 
     * @param includingDescent include font descent?
     * 
     * @return the last drawn string's height.
     */
    public final int calcMaxHeight( boolean includingDescent )
    {
        calcMaxHeight( font, fontAntiAliased );
        
        return ( getMaxHeight( includingDescent ) );
    }
    
    private void clear( int offsetX, int offsetY, java.awt.Color clearColor, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, TextureImage2D texture, Rect2i dirtyRect )
    {
        if ( ( clearRect.getWidth() <= 0 ) || ( clearRect.getHeight() <= 0 ) )
            return;
        
        final int x = clearRect.getLeft();
        final int y = clearRect.getTop();
        final int width = clearRect.getWidth();
        //final int height = clearRect.getHeight() - fontDescent;
        final int height = clearRect.getHeight();
        
        if ( ( clearColor == null ) && ( clearBackground == null ) )
        {
            widget.clearBackgroundRegion( texture, offsetX, offsetY, x - offsetX, y - offsetY, width, height, true, dirtyRect );
        }
        else
        {
            texture.getTextureCanvas().pushClip( x, y, width, height, true );
            
            try
            {
                if ( clearColor != null )
                {
                    texture.clear( clearColor, x, y, width, height, true, dirtyRect );
                }
                
                if ( clearBackground != null )
                {
                    int x1_ = x - clearOffsetX;
                    int y1_ = y - clearOffsetY;
                    int x2_ = x1_ + width - 1;
                    int y2_ = y1_ + height - 1;
                    
                    x1_ = Math.max( x1_, 0 );
                    y1_ = Math.max( y1_, 0 );
                    x2_ = Math.min( x2_, clearBackground.getWidth() - 1 );
                    y2_ = Math.min( y2_, clearBackground.getHeight() - 1 );
                    
                    int w_ = x2_ - x1_ + 1;
                    int h_ = y2_ - y1_ + 1;
                    
                    if ( ( w_ > 0 ) && ( h_ > 0 ) )
                    {
                        int x_ = x + ( x1_ - x + clearOffsetX );
                        int y_ = y + ( y1_ - y + clearOffsetY );
                        
                        texture.clear( clearBackground, x1_, y1_, w_, h_, x_, y_, w_, h_, true, dirtyRect );
                    }
                    //texture.clear( clearBackground, x1_, y1_, width, height, x, y, width, height, true, dirtyRect );
                }
            }
            finally
            {
                texture.getTextureCanvas().popClip();
            }
        }
        
        clearRect.setSize( 0, 0 );
    }
    
    private int draw( int offsetX, int offsetY, String str, java.awt.Color fontColor, TextureImage2D texture, boolean clearBefore, java.awt.Color clearColor, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY )
    {
        if ( fontColor == null )
            fontColor = getFontColor();
        
        String totalString;
        Rectangle2D bounds = null;
        
        if ( clearBefore )
            clear( offsetX, offsetY, clearColor, clearBackground, clearOffsetX, clearOffsetY, texture, null );
        
        if ( ( prefix != null ) && ( postfix != null ) )
            totalString = prefix + str + postfix;
        else if ( prefix != null )
            totalString = prefix + str;
        else if ( postfix != null )
            totalString = str + postfix;
        else
            totalString = str;
        
        bounds = TextureImage2D.getStringBounds( totalString, font, fontAntiAliased );
        maxWidth = (int)bounds.getWidth();
        //maxHeight = (int)bounds.getHeight();
        calcMaxHeight( font, fontAntiAliased );
        
        int x = this.getAbsX();
        if ( getAlignment() == Alignment.RIGHT )
            x -= (int)bounds.getWidth();
        else if ( getAlignment() == Alignment.CENTER )
            x -= (int)( bounds.getWidth() / 2.0 );
        
        int y = this.getAbsY() - ( isYAtBaseline() ? 0 : (int)bounds.getY() );
        
        texture.drawString( totalString, offsetX + x, offsetY + y, bounds, font, fontAntiAliased, fontColor, false, clearRect );
        
        if ( ( clearRect.getWidth() > 0 ) && ( clearRect.getHeight() > 0 ) )
            texture.markDirty( clearRect, null );
        
        return ( (int)Math.round( bounds.getWidth() ) );
    }
    
    private static final boolean getClearFlag( java.awt.Color clearColor, TextureImage2D clearBackground )
    {
        return ( ( clearColor != null ) || ( clearBackground != null ) );
    }
    
    /**
     * Draws the specified String as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param str the string to draw
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * @param clearColor the color to use for clearing (null to skip clearing)
     * 
     * @return the drawn string's width in pixels.
     */
    public int draw( int offsetX, int offsetY, String str, java.awt.Color fontColor, TextureImage2D texture, java.awt.Color clearColor )
    {
        return ( draw( offsetX, offsetY, str, fontColor, texture, getClearFlag( clearColor, null ), clearColor, null, 0, 0 ) );
    }
    
    /**
     * Draws the specified String as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param str the string to draw
     * @param texture the texture to draw on
     * @param clearColor the color to use for clearing (null to skip clearing)
     */
    public void draw( int offsetX, int offsetY, String str, TextureImage2D texture, java.awt.Color clearColor )
    {
        draw( offsetX, offsetY, str, null, texture, getClearFlag( clearColor, null ), clearColor, null, 0, 0 );
    }
    
    /**
     * Draws the specified String as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param str the string to draw
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * 
     * @return the drawn string's width in pixels.
     */
    public int draw( int offsetX, int offsetY, String str, java.awt.Color fontColor, TextureImage2D texture, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY )
    {
        return ( draw( offsetX, offsetY, str, fontColor, texture, getClearFlag( null, clearBackground ), null, clearBackground, clearOffsetX, clearOffsetY ) );
    }
    
    /**
     * Draws the specified String as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param str the string to draw
     * @param texture the texture to draw on
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     */
    public void draw( int offsetX, int offsetY, String str, TextureImage2D texture, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY )
    {
        draw( offsetX, offsetY, str, null, texture, getClearFlag( null, clearBackground ), null, clearBackground, clearOffsetX, clearOffsetY );
    }
    
    /**
     * Draws the specified String as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param str the string to draw
     * @param texture the texture to draw on
     * @param clearBackground the image to use for clearing (null to skip clearing)
     */
    public void draw( int offsetX, int offsetY, String str, TextureImage2D texture, TextureImage2D clearBackground )
    {
        draw( offsetX, offsetY, str, null, texture, getClearFlag( null, clearBackground ), null, clearBackground, offsetX, offsetY );
    }
    
    /**
     * Draws the specified String as configured in this class instance. This method doesn't clear the area before!
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param str the string to draw
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * @param clearBefore clear the area before drawing?
     */
    public void draw( int offsetX, int offsetY, String str, java.awt.Color fontColor, TextureImage2D texture, boolean clearBefore )
    {
        draw( offsetX, offsetY, str, fontColor, texture, clearBefore, null, null, 0, 0 );
    }
    
    /**
     * Draws the specified String as configured in this class instance. This method doesn't clear the area before!
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param str the string to draw
     * @param texture the texture to draw on
     * @param clearBefore clear the area before drawing?
     */
    public void draw( int offsetX, int offsetY, String str, TextureImage2D texture, boolean clearBefore )
    {
        draw( offsetX, offsetY, str, null, texture, clearBefore, null, null, 0, 0 );
    }
    
    /**
     * Draws the specified String as configured in this class instance. This method doesn't clear the area before!
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param str the string to draw
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     */
    public void draw( int offsetX, int offsetY, String str, java.awt.Color fontColor, TextureImage2D texture )
    {
        draw( offsetX, offsetY, str, fontColor, texture, true, null, null, 0, 0 );
    }
    
    /**
     * Draws the specified String as configured in this class instance. This method doesn't clear the area before!
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param str the string to draw
     * @param texture the texture to draw on
     */
    public void draw( int offsetX, int offsetY, String str, TextureImage2D texture )
    {
        draw( offsetX, offsetY, str, null, texture, true, null, null, 0, 0 );
    }
    
    private int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color fontColor, java.awt.Color[] fontColors, TextureImage2D texture, boolean clearBefore, java.awt.Color clearColor, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY )
    {
        Rect2i dirtyRect = Rect2i.fromPool();
        
        if ( clearBefore )
            clear( offsetX, offsetY, clearColor, clearBackground, clearOffsetX, clearOffsetY, texture, null );
        
        clearRect.setSize( 0, 0 );
        
        Rectangle2D bounds = null;
        int xOff = 0;
        maxWidth = 0;
        //maxHeight = 0;
        int lastDrawnColWidth = -1;
        calcMaxHeight( font, fontAntiAliased );
        
        final int ax = getAbsX();
        final int ay = getAbsY();
        
        //int yOff = 0;
        String str;
        
        int totalWidth = 0;
        for ( int i = 0; i < colWidths.length; i++ )
            totalWidth += colWidths[i];
        
        boolean oneFCForAll = fontColor != null;
        
        for ( int i = 0; i < strs.length; i++ )
        {
            Alignment align = aligns == null ? getAlignment() : ( aligns[i] == null ? getAlignment() : aligns[i] );
            
            if ( ( i == 0 ) && ( prefix != null ) )
            {
                if ( strs[i] == null )
                    str = prefix;
                else
                    str = prefix + strs[i];
            }
            else if ( ( i == strs.length - 1 ) && ( postfix != null ) )
            {
                if ( strs[i] == null )
                    str = postfix;
                else
                    str = strs[i] + postfix;
            }
            else
            {
                str = strs[i];
            }
            
            //int cw = (int)Math.round( bounds.getWidth() );
            int cw = colWidths[i];
            
            if ( str != null )
            {
                bounds = TextureImage2D.getStringBounds( str, font, fontAntiAliased );
                
                if ( str.length() > 0 )
                {
                    int x = ( ( getAlignment() == Alignment.RIGHT ) ? -totalWidth : ( getAlignment() == Alignment.CENTER ) ? -totalWidth / 2 : 0 ); 
                    x += ax + xOff;
                    if ( align == Alignment.RIGHT )
                        x +=  cw - (int)bounds.getWidth() - ( ( i < strs.length - 1 ) ? padding : 0 );
                    else if ( align == Alignment.CENTER )
                        x += ( ( cw - ( ( i > 0 ) ? padding : 0 ) ) / 2 ) - (int)( bounds.getWidth() / 2.0 );
                    
                    int y = ay - ( isYAtBaseline() ? 0 : (int)bounds.getY() );
                    
                    fontColor = oneFCForAll ? fontColor : ( ( fontColors == null ) ? this.fontColor : ( ( i >= fontColors.length ) ? this.fontColor : ( ( fontColors[i] == null ) ? this.fontColor : fontColors[i] ) ) );
                    texture.drawString( str, offsetX + x, offsetY + y, bounds, font, fontAntiAliased, fontColor, false, dirtyRect );
                    
                    if ( ( dirtyRect.getWidth() > 0 ) && ( dirtyRect.getHeight() > 0 ) )
                    {
                        if ( i == 0 )
                            clearRect.set( dirtyRect );
                        else
                            clearRect.combine( dirtyRect );
                    }
                }
                
                maxWidth += cw;
                xOff += cw;
                //maxHeight = Math.max( maxHeight, (int)Math.round( bounds.getHeight() ) );
                lastDrawnColWidth = cw;
                //yOff = Math.min( yOff, (int)Math.round( bounds.getY() ) );
            }
            else
            {
                xOff += cw;
            }
        }
        
        if ( ( clearRect.getWidth() > 0 ) && ( clearRect.getHeight() > 0 ) )
            texture.markDirty( clearRect, null );
        
        Rect2i.toPool( dirtyRect );
        
        if ( bounds == null )
            return ( maxWidth );
        
        return ( maxWidth - lastDrawnColWidth + (int)bounds.getWidth() );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * @param clearColor the color to use for clearing (null to skip clearing)
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, java.awt.Color fontColor, TextureImage2D texture, java.awt.Color clearColor )
    {
        return ( drawColumns( offsetX, offsetY, strs, null, 0, colWidths, fontColor, (java.awt.Color[])null, texture, getClearFlag( clearColor, null ), clearColor, null, 0, 0 ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param texture the texture to draw on
     * @param clearColor the color to use for clearing (null to skip clearing)
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, TextureImage2D texture, java.awt.Color clearColor )
    {
        drawColumns( offsetX, offsetY, strs, null, 0, colWidths, null, (java.awt.Color[])null, texture, getClearFlag( clearColor, null ), clearColor, null, 0, 0 );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, java.awt.Color fontColor, TextureImage2D texture, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY )
    {
        return ( drawColumns( offsetX, offsetY, strs, null, 0, colWidths, fontColor, (java.awt.Color[])null, texture, getClearFlag( null, clearBackground ), null, clearBackground, clearOffsetX, clearOffsetY ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param texture the texture to draw on
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, TextureImage2D texture, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY )
    {
        drawColumns( offsetX, offsetY, strs, null, 0, colWidths, null, (java.awt.Color[])null, texture, getClearFlag( null, clearBackground ), null, clearBackground, clearOffsetX, clearOffsetY );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param fontColors (null for predefined)
     * @param texture the texture to draw on
     * @param clearColor the color to use for clearing (null to skip clearing)
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color[] fontColors, TextureImage2D texture, java.awt.Color clearColor )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, null, fontColors, texture, getClearFlag( clearColor, null ), clearColor, null, 0, 0 ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * @param clearColor the color to use for clearing (null to skip clearing)
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color fontColor, TextureImage2D texture, java.awt.Color clearColor )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, fontColor, (java.awt.Color[])null, texture, getClearFlag( clearColor, null ), clearColor, null, 0, 0 ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param texture the texture to draw on
     * @param clearColor the color to use for clearing (null to skip clearing)
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, TextureImage2D texture, java.awt.Color clearColor )
    {
        drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, null, (java.awt.Color[])null, texture, getClearFlag( clearColor, null ), clearColor, null, 0, 0 );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param fontColors (null for predefined)
     * @param texture the texture to draw on
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color[] fontColors, TextureImage2D texture, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, null, fontColors, texture, getClearFlag( null, clearBackground ), null, clearBackground, clearOffsetX, clearOffsetY ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color fontColor, TextureImage2D texture, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, fontColor, (java.awt.Color[])null, texture, getClearFlag( null, clearBackground ), null, clearBackground, clearOffsetX, clearOffsetY ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance
     * and clears the previously used area with the {@link Widget}'s background, if necessary.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * @param clearBefore clear the area before drawing?
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, java.awt.Color fontColor, TextureImage2D texture, boolean clearBefore )
    {
        return ( drawColumns( offsetX, offsetY, strs, null, 0, colWidths, fontColor, (java.awt.Color[])null, texture, clearBefore, null, null, 0, 0 ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance
     * and clears the previously used area with the {@link Widget}'s background, if necessary.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param texture the texture to draw on
     * @param clearBefore clear the area before drawing?
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, TextureImage2D texture, boolean clearBefore )
    {
        drawColumns( offsetX, offsetY, strs, null, 0, colWidths, null, (java.awt.Color[])null, texture, clearBefore, null, null, 0, 0 );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance
     * and clears the previously used area with the {@link Widget}'s background, if necessary.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param fontColors (null for predefined)
     * @param texture the texture to draw on
     * @param clearBefore clear the area before drawing?
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color[] fontColors, TextureImage2D texture, boolean clearBefore )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, null, fontColors, texture, clearBefore, null, null, 0, 0 ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance
     * and clears the previously used area with the {@link Widget}'s background, if necessary.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * @param clearBefore clear the area before drawing?
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color fontColor, TextureImage2D texture, boolean clearBefore )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, fontColor, (java.awt.Color[])null, texture, clearBefore, null, null, 0, 0 ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance
     * and clears the previously used area with the {@link Widget}'s background, if necessary.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param texture the texture to draw on
     * @param clearBefore clear the area before drawing?
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, TextureImage2D texture, boolean clearBefore )
    {
        drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, null, (java.awt.Color[])null, texture, clearBefore, null, null, 0, 0 );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance
     * and clears the previously used area with the {@link Widget}'s background, if necessary.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, java.awt.Color fontColor, TextureImage2D texture )
    {
        return ( drawColumns( offsetX, offsetY, strs, null, 0, colWidths, fontColor, (java.awt.Color[])null, texture, true, null, null, 0, 0 ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance
     * and clears the previously used area with the {@link Widget}'s background, if necessary.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param texture the texture to draw on
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, TextureImage2D texture )
    {
        drawColumns( offsetX, offsetY, strs, null, 0, colWidths, null, (java.awt.Color[])null, texture, true, null, null, 0, 0 );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance
     * and clears the previously used area with the {@link Widget}'s background, if necessary.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param fontColors (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color[] fontColors, TextureImage2D texture )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, null, fontColors, texture, true, null, null, 0, 0 ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance
     * and clears the previously used area with the {@link Widget}'s background, if necessary.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color fontColor, TextureImage2D texture )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, fontColor, (java.awt.Color[])null, texture, true, null, null, 0, 0 ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance
     * and clears the previously used area with the {@link Widget}'s background, if necessary.
     * 
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param texture the texture to draw on
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, TextureImage2D texture )
    {
        drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, null, (java.awt.Color[])null, texture, true, null, null, 0, 0 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( getClass().getSimpleName() + "( \"" + getName() + "\" )" );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param name
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     */
    DrawnString( Widget widget, String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        this.widget = widget;
        
        this.name = name;
        
        this.xRelativeTo = xRelativeTo;
        this.yRelativeTo = yRelativeTo;
        
        this.x = x;
        this.y = y;
        
        this.alignment = alignment;
        this.y_at_baseline = y_at_baseline;
        
        this.font = font;
        this.fontAntiAliased = fontAntiAliased;
        this.fontColor = fontColor;
        
        this.prefix = prefix;
        this.postfix = postfix;
    }
}
