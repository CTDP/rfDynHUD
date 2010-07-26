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
package net.ctdp.rfdynhud.etv2010.widgets._util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

import org.openmali.types.twodee.Rect2i;

public class ETVUtils
{
    public static final WidgetPackage WIDGET_PACKAGE = new WidgetPackage( "CTDP/Ecclestone TV 2010", ETVUtils.class.getClassLoader().getResource( "net/ctdp/rfdynhud/etv2010/widgets/ctdp.png" ), ETVUtils.class.getClassLoader().getResource( "net/ctdp/rfdynhud/etv2010/widgets/etv2010.png" ) );
    
    public static final int TRIANGLE_WIDTH = 16;
    private static final boolean AA_TRIANGLE = true;
    public static final int ITEM_GAP = 3;
    
    public static final String ETV_STYLE_CAPTION_BACKGROUND_COLOR = "ETVCaptionBackgroundColor";
    public static final String ETV_STYLE_CAPTION_BACKGROUND_COLOR_1ST = "ETVCaptionBackgroundColor1st";
    public static final String ETV_STYLE_CAPTION_FONT_COLOR = "ETVCaptionFontColor";
    public static final String ETV_STYLE_DATA_BACKGROUND_COLOR_1ST = "ETVDataBackgroundColor1st";
    public static final String ETV_STYLE_DATA_BACKGROUND_COLOR = "ETVDataBackgroundColor";
    public static final String ETV_STYLE_DATA_BACKGROUND_COLOR_FASTEST = "ETVDataBackgroundColorFastest";
    public static final String ETV_STYLE_DATA_BACKGROUND_COLOR_FASTER = "ETVDataBackgroundColorFaster";
    public static final String ETV_STYLE_DATA_BACKGROUND_COLOR_SLOWER = "ETVDataBackgroundColorSlower";
    public static final String ETV_STYLE_DATA_FONT_COLOR = "ETVDataFontColor";
    public static final String ETV_STYLE_DATA_FONT_COLOR_FASTEST = "ETVDataFontColorFasterst";
    public static final String ETV_STYLE_DATA_FONT_COLOR_FASTER = "ETVDataFontColorFaster";
    public static final String ETV_STYLE_DATA_FONT_COLOR_SLOWER = "ETVDataFontColorSlower";
    public static final String ETV_STYLE_FONT = "ETVFont";
    
    public static String getDefaultNamedColorValue( String name )
    {
        if ( name.equals( ETV_STYLE_CAPTION_BACKGROUND_COLOR ) )
            return ( "#787878" );
        
        if ( name.equals( ETV_STYLE_CAPTION_BACKGROUND_COLOR_1ST ) )
            return ( "#B10000" );
        
        if ( name.equals( ETV_STYLE_CAPTION_FONT_COLOR ) )
            return ( "#FFFFFF" );
        
        if ( name.equals( ETV_STYLE_DATA_BACKGROUND_COLOR_1ST ) )
            return ( "#230000" );
        
        if ( name.equals( ETV_STYLE_DATA_BACKGROUND_COLOR ) )
            return ( "#000000" );
        
        if ( name.equals( ETV_STYLE_DATA_BACKGROUND_COLOR_FASTEST ) )
            return ( "#C000D2" );
        
        if ( name.equals( ETV_STYLE_DATA_BACKGROUND_COLOR_FASTER ) )
            return ( "#008800" );
        
        if ( name.equals( ETV_STYLE_DATA_BACKGROUND_COLOR_SLOWER ) )
            return ( "#BAB802" );
        
        if ( name.equals( ETV_STYLE_DATA_FONT_COLOR ) )
            return ( "#FFFFFF" );
        
        if ( name.equals( ETV_STYLE_DATA_FONT_COLOR_FASTEST ) )
            return ( "#000000" );
        
        if ( name.equals( ETV_STYLE_DATA_FONT_COLOR_FASTER ) )
            return ( "#FFFFFF" );
        
        if ( name.equals( ETV_STYLE_DATA_FONT_COLOR_SLOWER ) )
            return ( "#000000" );
        
        return ( null );
    }
    
    public static String getDefaultNamedFontValue( String name )
    {
        if ( name.equals( ETV_STYLE_FONT ) )
            return ( "Verdana-PLAIN-16va" );
        
        return ( null );
    }
    
    public static final int getLabeledDataDataWidth( int width, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        int dataAreaWidth = width - 3 * TRIANGLE_WIDTH - capWidth;
        
        return ( dataAreaWidth );
    }
    
    public static final int getLabeledDataDataLeft( Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( TRIANGLE_WIDTH + capWidth + TRIANGLE_WIDTH );
    }
    
    public static final int getLabeledDataDataCenter( int width, Rectangle2D captionBounds )
    {
        int dataAreaWidth = getLabeledDataDataWidth( width, captionBounds );
        int dataAreaCenter = width - TRIANGLE_WIDTH - dataAreaWidth / 2;
        
        return ( dataAreaCenter );
    }
    
    public static final int getLabeledDataDataRight( int width )
    {
        return ( width - TRIANGLE_WIDTH );
    }
    
    public static final int getLabeledDataVMiddle( int height, Rectangle2D captionBounds )
    {
        int capHeight = (int)Math.ceil( captionBounds.getHeight() );
        int vMiddle = ( height - capHeight ) / 2;
        
        return ( vMiddle );
    }
    
    public static void drawDataBackground( int x, int y, int width, int height, int triangleWidthFactor, Color dataBgColor, TextureImage2D texture, boolean clearBefore )
    {
        if ( clearBefore )
            texture.clear( x, y, width, height, true, null );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        texCanvas.setColor( dataBgColor );
        
        texCanvas.setAntialiazingEnabled( AA_TRIANGLE );
        
        final int triangWidth = TRIANGLE_WIDTH * triangleWidthFactor;
        
        int[] xPoints = new int[] { x, x + triangWidth, x + triangWidth, x };
        int[] yPoints = new int[] { y + height, y + height, y + 0, y + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
        
        texCanvas.setAntialiazingEnabled( false );
        
        //int lapAreaWidth = width - 4 * triangWidth - capWidth;
        Rect2i rect = new Rect2i( x + triangWidth, y + 0, width - 2 * triangWidth, height );
        
        texCanvas.fillRect( rect );
        //texCanvas.drawRect( rect );
        
        texCanvas.setAntialiazingEnabled( AA_TRIANGLE );
        
        xPoints = new int[] { x + width - triangWidth, x + width, x + width - triangWidth, x + width - triangWidth };
        yPoints = new int[] { y + height, y + 0, y + 0, y + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
    }
    
    public static void drawDataBackground( int x, int y, int width, int height, Color dataBgColor, TextureImage2D texture, boolean clearBefore )
    {
        drawDataBackground( x, y, width, height, 1, dataBgColor, texture, clearBefore );
    }
    
    public static void drawLabeledDataBackground( int x, int y, int width, int height, int triangleWidthFactor, String caption, Font font, Color captionBgColor, Color dataBgColor, TextureImage2D texture, boolean clearBefore )
    {
        if ( clearBefore )
            texture.clear( x, y, width, height, true, null );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        texCanvas.setFont( font );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D capBounds = metrics.getStringBounds( caption, texCanvas );
        
        texCanvas.setColor( captionBgColor );
        
        texCanvas.setAntialiazingEnabled( AA_TRIANGLE );
        
        final int triangWidth = TRIANGLE_WIDTH * triangleWidthFactor;
        
        int[] xPoints = new int[] { x + 0, x + triangWidth, x + triangWidth, x + 0 };
        int[] yPoints = new int[] { y + height, y + height, y + 0, y + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
        
        texCanvas.setAntialiazingEnabled( false );
        
        int capWidth = (int)Math.ceil( capBounds.getWidth() );
        Rect2i rect = new Rect2i( x + triangWidth, y + 0, capWidth, height );
        
        texCanvas.fillRect( rect );
        //texCanvas.drawRect( rect );
        
        texCanvas.setAntialiazingEnabled( AA_TRIANGLE );
        
        xPoints = new int[] { x + triangWidth + capWidth, x + triangWidth + capWidth + triangWidth, x + triangWidth + capWidth, x + triangWidth + capWidth };
        yPoints = new int[] { y + height, y + 0, y + 0, y + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
        
        drawDataBackground( x + triangWidth + capWidth, y, width - triangWidth - capWidth, height, dataBgColor, texture, false );
    }
    
    public static void drawLabeledDataBackground( int x, int y, int width, int height, String caption, Font font, Color captionBgColor, Color dataBgColor, TextureImage2D texture, boolean clearBefore )
    {
        drawLabeledDataBackground( x, y, width, height, 1, caption, font, captionBgColor, dataBgColor, texture, clearBefore );
    }
}
