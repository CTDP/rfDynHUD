package net.ctdp.rfdynhud.widgets._util;

import java.awt.geom.Rectangle2D;

import net.ctdp.rfdynhud.render.TextureImage2D;

import org.openmali.types.twodee.Rect2i;

/**
 * A {@link DrawnString} is an abstraction of a String to be drawn.
 * It handles alignment and clearing.
 * 
 * @author Marvin Froehlich
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
    
    private final Rect2i clearRect = new Rect2i( -1, -1, 0, 0 );
    
    private final String prefix;
    private final String postfix;
    
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
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int getWidth( String str, TextureImage2D texture )
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
        
        Rectangle2D bounds = texture.getStringBounds( s, font, fontAntiAliased );
        
        return ( (int)Math.round( bounds.getWidth() ) );
    }
    
    /**
     * Gets the drawn strings' minimum column-widths and the total width in pixels.
     * 
     * @param strs the strings to draw
     * @param padding padding per column
     * @param texture the texture to draw on
     * @param colWidths the array to write column widths to
     * 
     * @return the drawn strings' minimum total width in pixels.
     */
    public int getMinColWidths( String[] strs, int padding, TextureImage2D texture, int[] colWidths )
    {
        int total = 0;
        int w;
        String str;
        
        for ( int i = 0; i < strs.length; i++ )
        {
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
                if ( strs[i] == null )
                    str = "";
                else
                    str = strs[i];
            }
            
            w = (int)Math.round( texture.getStringBounds( str, font, fontAntiAliased ).getWidth() ) + padding;
            
            total += w;
            if ( colWidths != null )
                colWidths[i] = w;
        }
        
        return ( total );
    }
    
    /**
     * Gets the drawn strings' maximum column-widths and the total width in pixels.
     * 
     * @param strs the strings to draw
     * @param padding padding per column
     * @param texture the texture to draw on
     * @param colWidths the array to write column widths to
     * 
     * @return the drawn strings' maximum total width in pixels.
     */
    public int getMaxColWidths( String[] strs, int padding, TextureImage2D texture, int[] colWidths )
    {
        int total = 0;
        int w;
        String str;
        
        for ( int i = 0; i < strs.length; i++ )
        {
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
                if ( strs[i] == null )
                    str = "";
                else
                    str = strs[i];
            }
            
            w = (int)Math.round( texture.getStringBounds( str, font, fontAntiAliased ).getWidth() ) + padding;
            colWidths[i] = Math.max( colWidths[i], w );
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
    
    private int calcMaxHeight( java.awt.Font font, boolean antiAliased, TextureImage2D texture )
    {
        if ( maxHeight > 0 )
            return ( maxHeight );
        
        Rectangle2D bounds = texture.getStringBounds( MAX_HEIGHT_STRING, font, antiAliased );
        
        this.fontDescent = texture.getFontDescent( font );
        this.maxHeight = (int)( bounds.getHeight() + fontDescent );
        
        return ( maxHeight );
    }
    
    /**
     * Gets the last drawn string's height.<br />
     * This value will be invalid, if this {@link DrawnString} has not yet been drawn.
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
     * This value will be invalid, if this {@link DrawnString} has not yet been drawn.
     * 
     * @return the last drawn string's height.
     */
    public final int getMaxHeight( TextureImage2D texture, boolean includingDescent )
    {
        calcMaxHeight( font, fontAntiAliased, texture );
        
        return ( getMaxHeight( includingDescent ) );
    }
    
    private void clear( java.awt.Color clearColor, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, TextureImage2D texture, Rect2i dirtyRect )
    {
        if ( ( clearRect.getWidth() <= 0 ) || ( clearRect.getHeight() <= 0 ) )
            return;
        
        if ( ( clearColor == null ) && ( clearBackground == null ) )
            return;
        
        int x = clearRect.getLeft();
        int y = clearRect.getTop();
        int width = clearRect.getWidth();
        int height = clearRect.getHeight() - fontDescent;
        
        texture.getTextureCanvas().pushClip( x, y, width, height, true );
        if ( clearColor != null )
            texture.clear( clearColor, x, y, width, height, true, dirtyRect );
        if ( clearBackground != null )
            texture.clear( clearBackground, x - clearOffsetX, y - clearOffsetY, width, height, x, y, width, height, true, dirtyRect );
        texture.getTextureCanvas().popClip();
    }
    
    private int draw( int offsetX, int offsetY, String str, java.awt.Color clearColor, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, java.awt.Color fontColor, TextureImage2D texture )
    {
        if ( fontColor == null )
            fontColor = getFontColor();
        
        String totalString;
        Rectangle2D bounds = null;
        
        clear( clearColor, clearBackground, clearOffsetX, clearOffsetY, texture, null );
        
        if ( ( prefix != null ) && ( postfix != null ) )
            totalString = prefix + str + postfix;
        else if ( prefix != null )
            totalString = prefix + str;
        else if ( postfix != null )
            totalString = str + postfix;
        else
            totalString = str;
        
        bounds = texture.getStringBounds( totalString, font, fontAntiAliased );
        maxWidth = (int)bounds.getWidth();
        //maxHeight = (int)bounds.getHeight();
        calcMaxHeight( font, fontAntiAliased, texture );
        
        int x = this.getAbsX();
        if ( getAlignment() == Alignment.RIGHT )
            x -= (int)bounds.getWidth();
        else if ( getAlignment() == Alignment.CENTER )
            x -= (int)( bounds.getWidth() / 2.0 );
        
        int y = this.getAbsY() - ( isYAtBaseline() ? 0 : (int)bounds.getY() );
        
        texture.drawString( totalString, offsetX + x, offsetY + y, bounds, font, fontAntiAliased, fontColor, false, clearRect );
        
        texture.markDirty( clearRect );
        
        return ( (int)Math.round( bounds.getWidth() ) );
    }
    
    /**
     * Draws the specified String as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param str the string to draw
     * @param clearColor the color to use for clearing (null to skip clearing)
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int draw( int offsetX, int offsetY, String str, java.awt.Color clearColor, java.awt.Color fontColor, TextureImage2D texture )
    {
        return ( draw( offsetX, offsetY, str, clearColor, null, 0, 0, fontColor, texture ) );
    }
    
    /**
     * Draws the specified String as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param str the string to draw
     * @param clearColor the color to use for clearing (null to skip clearing)
     * @param texture the texture to draw on
     */
    public void draw( int offsetX, int offsetY, String str, java.awt.Color clearColor, TextureImage2D texture )
    {
        draw( offsetX, offsetY, str, clearColor, null, 0, 0, null, texture );
    }
    
    /**
     * Draws the specified String as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param str the string to draw
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int draw( int offsetX, int offsetY, String str, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, java.awt.Color fontColor, TextureImage2D texture )
    {
        return ( draw( offsetX, offsetY, str, null, clearBackground, clearOffsetX, clearOffsetY, fontColor, texture ) );
    }
    
    /**
     * Draws the specified String as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param str the string to draw
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * @param texture the texture to draw on
     */
    public void draw( int offsetX, int offsetY, String str, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, TextureImage2D texture )
    {
        draw( offsetX, offsetY, str, null, clearBackground, clearOffsetX, clearOffsetY, null, texture );
    }
    
    private int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color clearColor, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, java.awt.Color fontColor, java.awt.Color[] fontColors, TextureImage2D texture )
    {
        Rect2i dirtyRect = Rect2i.fromPool();
        
        clear( clearColor, clearBackground, clearOffsetX, clearOffsetY, texture, null );
        
        Rectangle2D bounds = null;
        maxWidth = 0;
        //maxHeight = 0;
        calcMaxHeight( font, fontAntiAliased, texture );
        
        final int ax = getAbsX();
        final int ay = getAbsY();
        
        int yOff = 0;
        String str;
        
        int totalWidth = 0;
        for ( int i = 0; i < colWidths.length; i++ )
            totalWidth += colWidths[i];
        
        boolean oneFCForAll = fontColor != null;
        
        for ( int i = 0; i < strs.length; i++ )
        {
            Alignment align = aligns == null ? getAlignment() : ( aligns[i] == null ? getAlignment() : aligns[i] );
            
            if ( ( i == 0 ) && ( prefix != null ) )
                if ( strs[i] == null )
                    str = prefix;
                else
                    str = prefix + strs[i];
            else if ( ( i == strs.length - 1 ) && ( postfix != null ) )
                if ( strs[i] == null )
                    str = postfix;
                else
                    str = strs[i] + postfix;
            else if ( strs[i] == null )
                str = "";
            else
                str = strs[i];
            
            //int cw = (int)Math.round( bounds.getWidth() );
            int cw = colWidths[i];
            
            bounds = texture.getStringBounds( str, font, fontAntiAliased );
            
            int x = ( ( getAlignment() == Alignment.RIGHT ) ? -totalWidth : ( getAlignment() == Alignment.CENTER ) ? -totalWidth / 2 : 0 ); 
            x += ax + maxWidth;
            if ( align == Alignment.RIGHT )
                x +=  cw - (int)bounds.getWidth() - padding;
            else if ( align == Alignment.CENTER )
                x += ( ( cw - padding ) / 2 ) - (int)( bounds.getWidth() / 2.0 );
            
            int y = ay - ( isYAtBaseline() ? 0 : (int)bounds.getY() );
            
            fontColor = oneFCForAll ? fontColor : ( ( fontColors == null ) ? this.fontColor : ( ( i >= fontColors.length ) ? this.fontColor : ( ( fontColors[i] == null ) ? this.fontColor : fontColors[i] ) ) );
            texture.drawString( str, offsetX + x, offsetY + y, bounds, font, fontAntiAliased, fontColor, false, dirtyRect );
            
            if ( i == 0 )
                clearRect.set( dirtyRect );
            else
                clearRect.combine( dirtyRect );
            
            maxWidth += cw;
            //maxHeight = Math.max( maxHeight, (int)Math.round( bounds.getHeight() ) );
            yOff = Math.min( yOff, (int)Math.round( bounds.getY() ) );
        }
        
        texture.markDirty( clearRect );
        
        Rect2i.toPool( dirtyRect );
        
        return ( maxWidth - colWidths[colWidths.length - 1] + (int)bounds.getWidth() );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param clearColor the color to use for clearing (null to skip clearing)
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, java.awt.Color clearColor, java.awt.Color fontColor, TextureImage2D texture )
    {
        return ( drawColumns( offsetX, offsetY, strs, null, 0, colWidths, clearColor, null, 0, 0, fontColor, (java.awt.Color[])null, texture ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param clearColor the color to use for clearing (null to skip clearing)
     * @param texture the texture to draw on
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, java.awt.Color clearColor, TextureImage2D texture )
    {
        drawColumns( offsetX, offsetY, strs, null, 0, colWidths, clearColor, null, 0, 0, null, (java.awt.Color[])null, texture );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, java.awt.Color fontColor, TextureImage2D texture )
    {
        return ( drawColumns( offsetX, offsetY, strs, null, 0, colWidths, null, clearBackground, clearOffsetX, clearOffsetY, fontColor, (java.awt.Color[])null, texture ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param strs the strings to draw
     * @param colWidths the column widths to use
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * @param texture the texture to draw on
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, int[] colWidths, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, TextureImage2D texture )
    {
        drawColumns( offsetX, offsetY, strs, null, 0, colWidths, null, clearBackground, clearOffsetX, clearOffsetY, null, (java.awt.Color[])null, texture );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param clearColor the color to use for clearing (null to skip clearing)
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color clearColor, java.awt.Color[] fontColors, TextureImage2D texture )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, clearColor, null, 0, 0, null, fontColors, texture ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param clearColor the color to use for clearing (null to skip clearing)
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color clearColor, java.awt.Color fontColor, TextureImage2D texture )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, clearColor, null, 0, 0, fontColor, (java.awt.Color[])null, texture ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param clearColor the color to use for clearing (null to skip clearing)
     * @param texture the texture to draw on
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, java.awt.Color clearColor, TextureImage2D texture )
    {
        drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, clearColor, null, 0, 0, null, (java.awt.Color[])null, texture );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, java.awt.Color[] fontColors, TextureImage2D texture )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, null, clearBackground, clearOffsetX, clearOffsetY, null, fontColors, texture ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * @param fontColor (null for predefined)
     * @param texture the texture to draw on
     * 
     * @return the drawn string's width in pixels.
     */
    public int drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, java.awt.Color fontColor, TextureImage2D texture )
    {
        return ( drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, null, clearBackground, clearOffsetX, clearOffsetY, fontColor, (java.awt.Color[])null, texture ) );
    }
    
    /**
     * Draws the specified Strings as configured in this class instance.
     * 
     * @param offsetX
     * @param offsetY
     * @param strs the strings to draw
     * @param aligns alignment per column (default is the {@link DrawnString}'s alignment)
     * @param padding the padding to honor when aligning right
     * @param colWidths the column widths to use
     * @param clearBackground the image to use for clearing (null to skip clearing)
     * @param clearOffsetX the x-offset of the clear texture on the background
     * @param clearOffsetY the y-offset of the clear texture on the background
     * @param texture the texture to draw on
     */
    public void drawColumns( int offsetX, int offsetY, String[] strs, Alignment[] aligns, int padding, int[] colWidths, TextureImage2D clearBackground, int clearOffsetX, int clearOffsetY, TextureImage2D texture )
    {
        drawColumns( offsetX, offsetY, strs, aligns, padding, colWidths, null, clearBackground, clearOffsetX, clearOffsetY, null, (java.awt.Color[])null, texture );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
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
    public DrawnString( DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
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
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     */
    public DrawnString( DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        this( xRelativeTo, yRelativeTo, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor, prefix, postfix );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     */
    public DrawnString( DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        this( xRelativeTo, yRelativeTo, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, null, null );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     */
    public DrawnString( DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        this( xRelativeTo, yRelativeTo, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
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
    public DrawnString( int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        this( null, null, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, prefix, postfix );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     */
    public DrawnString( int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        this( x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor, prefix, postfix );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     */
    public DrawnString( int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        this( x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, null, null );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     */
    public DrawnString( int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        this( x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor );
    }
}
