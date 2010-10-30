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
import java.awt.geom.Rectangle2D;

import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;

import org.openmali.types.twodee.Rect2i;

public class ETVUtils
{
    public static final WidgetPackage WIDGET_PACKAGE = new WidgetPackage( "CTDP/Ecclestone TV 2010", WidgetPackage.composeVersion( 1, 1, 0 ), ETVUtils.class.getClassLoader().getResource( "net/ctdp/rfdynhud/etv2010/widgets/ctdp.png" ), ETVUtils.class.getClassLoader().getResource( "net/ctdp/rfdynhud/etv2010/widgets/etv2010.png" ) );
    
    private static final boolean AA_TRIANGLE = true;
    
    public static final String ETV_CAPTION_BACKGROUND_COLOR = "ETVCaptionBackgroundColor";
    public static final String ETV_CAPTION_BACKGROUND_COLOR_1ST = "ETVCaptionBackgroundColor1st";
    public static final String ETV_CAPTION_FONT_COLOR = "ETVCaptionFontColor";
    public static final String ETV_DATA_BACKGROUND_COLOR_1ST = "ETVDataBackgroundColor1st";
    public static final String ETV_DATA_BACKGROUND_COLOR = "ETVDataBackgroundColor";
    public static final String ETV_DATA_BACKGROUND_COLOR_FASTEST = "ETVDataBackgroundColorFastest";
    public static final String ETV_DATA_BACKGROUND_COLOR_FASTER = "ETVDataBackgroundColorFaster";
    public static final String ETV_DATA_BACKGROUND_COLOR_SLOWER = "ETVDataBackgroundColorSlower";
    public static final String ETV_DATA_FONT_COLOR = "ETVDataFontColor";
    public static final String ETV_DATA_FONT_COLOR_FASTEST = "ETVDataFontColorFasterst";
    public static final String ETV_DATA_FONT_COLOR_FASTER = "ETVDataFontColorFaster";
    public static final String ETV_DATA_FONT_COLOR_SLOWER = "ETVDataFontColorSlower";
    public static final String ETV_FONT = "ETVFont";
    public static final String ETV_VELOCITY_FONT = "ETVVelocityFont";
    public static final String ETV_REV_MARKERS_FONT = "ETVRevMarkersFont";
    public static final String ETV_GEAR_FONT = "ETVGearFont";
    public static final String ETV_CONTROLS_LABEL_FONT = "ETVControlsLabelFont";
    
    public static String getDefaultNamedColorValue( String name )
    {
        if ( name.equals( ETV_CAPTION_BACKGROUND_COLOR ) )
            return ( "#787878" );
        
        if ( name.equals( ETV_CAPTION_BACKGROUND_COLOR_1ST ) )
            return ( "#B10000" );
        
        if ( name.equals( ETV_CAPTION_FONT_COLOR ) )
            return ( "#FFFFFF" );
        
        if ( name.equals( ETV_DATA_BACKGROUND_COLOR_1ST ) )
            return ( "#230000" );
        
        if ( name.equals( ETV_DATA_BACKGROUND_COLOR ) )
            return ( "#000000" );
        
        if ( name.equals( ETV_DATA_BACKGROUND_COLOR_FASTEST ) )
            return ( "#C000D2" );
        
        if ( name.equals( ETV_DATA_BACKGROUND_COLOR_FASTER ) )
            return ( "#008800" );
        
        if ( name.equals( ETV_DATA_BACKGROUND_COLOR_SLOWER ) )
            return ( "#BAB802" );
        
        if ( name.equals( ETV_DATA_FONT_COLOR ) )
            return ( "#FFFFFF" );
        
        if ( name.equals( ETV_DATA_FONT_COLOR_FASTEST ) )
            return ( "#000000" );
        
        if ( name.equals( ETV_DATA_FONT_COLOR_FASTER ) )
            return ( "#FFFFFF" );
        
        if ( name.equals( ETV_DATA_FONT_COLOR_SLOWER ) )
            return ( "#000000" );
        
        return ( null );
    }
    
    public static String getDefaultNamedFontValue( String name )
    {
        if ( name.equals( ETV_FONT ) )
            return ( "DokChampa-BOLD-16va" );
        
        if ( name.equals( ETV_VELOCITY_FONT ) )
            return ( "DokChampa-BOLD-18va" );
        
        if ( name.equals( ETV_REV_MARKERS_FONT ) )
            return ( "DokChampa-BOLD-18va" );
        
        if ( name.equals( ETV_GEAR_FONT ) )
            return ( "DokChampa-BOLD-30va" );
        
        if ( name.equals( ETV_CONTROLS_LABEL_FONT ) )
            return ( "DokChampa-BOLD-18va" );
        
        return ( null );
    }
    
    public static final int getTriangleWidth( int itemHeight )
    {
        return ( Math.round( itemHeight / 2f ) );
    }
    
    public static final int getLabeledDataCaptionLeft( int height )
    {
        return ( getTriangleWidth( height ) );
    }
    
    public static final int getLabeledDataCaptionCenter( int height, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getTriangleWidth( height ) + ( capWidth / 2 ) );
    }
    
    public static final int getLabeledDataCaptionRight( int height, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getTriangleWidth( height ) + capWidth );
    }
    
    public static final int getLabeledDataDataWidth( int width, int height, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        int dataAreaWidth = width - 3 * getTriangleWidth( height ) - capWidth;
        
        return ( dataAreaWidth );
    }
    
    public static final int getLabeledDataDataLeft( int height, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        int triangWidth = getTriangleWidth( height );
        
        return ( triangWidth + capWidth + triangWidth );
    }
    
    public static final int getLabeledDataDataCenter( int width, int height, Rectangle2D captionBounds )
    {
        int dataAreaWidth = getLabeledDataDataWidth( width, height, captionBounds );
        int dataAreaCenter = width - getTriangleWidth( height ) - dataAreaWidth / 2;
        
        return ( dataAreaCenter );
    }
    
    public static final int getLabeledDataDataRight( int width, int height )
    {
        return ( width - getTriangleWidth( height ) );
    }
    
    public static final int getLabeledDataVMiddle( int height, Rectangle2D captionBounds )
    {
        int capHeight = (int)Math.ceil( captionBounds.getHeight() );
        int vMiddle = ( height - capHeight ) / 2;
        
        return ( vMiddle );
    }
    
    public static void drawBigPositionBackgroundI( int x, int y, int width, int height, ETVImages images, boolean first, TextureImage2D texture, boolean clearBefore )
    {
        if ( clearBefore )
            texture.clear( x, y, width, height, true, null );
        
        final ImageTemplate it = images.getBigPositionImage( first );
        final float scale = (float)height / (float)it.getBaseHeight();
        final int hs = it.getBaseHeight();
        
        int xs = 0;
        int xd = x;
        
        int border_left = images.getBigPositionBorderLeft();
        int border_left_ = Math.round( border_left * scale );
        
        it.drawScaled( xs, 0, border_left, hs, xd, y, border_left_, height, texture, false );
        
        xs += border_left;
        xd += border_left_;
        
        int border_right = images.getBigPositionBorderRight();
        int border_right_ = Math.round( border_right * scale );
        
        int data_width = it.getBaseWidth() - xs - border_right;
        int data_width_ = width - xd + x - border_right_;
        
        it.drawScaled( xs, 0, data_width, hs, xd, y, data_width_, height, texture, false );
        
        xs += data_width;
        xd += data_width_;
        
        it.drawScaled( xs, 0, border_right, hs, xd, y, border_right_, height, texture, false );
    }
    
    public static void drawDataBackground( int x, int y, int width, int height, Color dataBgColor, TextureImage2D texture, boolean clearBefore )
    {
        if ( clearBefore )
            texture.clear( x, y, width, height, true, null );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        texCanvas.setColor( dataBgColor );
        
        texCanvas.setAntialiazingEnabled( AA_TRIANGLE );
        
        final int triangWidth = getTriangleWidth( height );
        
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
    
    public static void drawDataBackgroundI( int x, int y, int width, int height, ETVImages images, ETVImages.BGType type, TextureImage2D texture, boolean clearBefore )
    {
        if ( clearBefore )
            texture.clear( x, y, width, height, true, null );
        
        final ImageTemplate it = images.getDataImage( type );
        final float scale = (float)height / (float)it.getBaseHeight();
        final int hs = it.getBaseHeight();
        
        int xs = 0;
        int xd = x;
        
        int border_left = images.getDataBorderLeft();
        int border_left_ = Math.round( border_left * scale );
        
        it.drawScaled( xs, 0, border_left, hs, xd, y, border_left_, height, texture, false );
        
        xs += border_left;
        xd += border_left_;
        
        int border_right = images.getDataBorderRight();
        int border_right_ = Math.round( border_right * scale );
        
        int data_width = it.getBaseWidth() - xs - border_right;
        int data_width_ = width - xd + x - border_right_;
        
        it.drawScaled( xs, 0, data_width, hs, xd, y, data_width_, height, texture, false );
        
        xs += data_width;
        xd += data_width_;
        
        it.drawScaled( xs, 0, border_right, hs, xd, y, border_right_, height, texture, false );
    }
    
    public static void drawLabeledDataBackground( int x, int y, int width, int height, int triangleWidthFactor, String caption, FontProperty font, Color captionBgColor, Color dataBgColor, TextureImage2D texture, boolean clearBefore )
    {
        if ( clearBefore )
            texture.clear( x, y, width, height, true, null );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        Rectangle2D capBounds = TextureImage2D.getStringBounds( caption, font );
        int capWidth = (int)Math.ceil( capBounds.getWidth() );
        
        texCanvas.setColor( captionBgColor );
        
        texCanvas.setAntialiazingEnabled( AA_TRIANGLE );
        
        final int triangWidth = getTriangleWidth( height ) * triangleWidthFactor;
        
        int[] xPoints = new int[] { x + 0, x + triangWidth, x + triangWidth, x + 0 };
        int[] yPoints = new int[] { y + height, y + height, y + 0, y + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
        
        texCanvas.setAntialiazingEnabled( false );
        
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
    
    public static void drawLabeledDataBackground( int x, int y, int width, int height, String caption, FontProperty font, Color captionBgColor, Color dataBgColor, TextureImage2D texture, boolean clearBefore )
    {
        drawLabeledDataBackground( x, y, width, height, 1, caption, font, captionBgColor, dataBgColor, texture, clearBefore );
    }
    
    public static void drawLabeledDataBackgroundI( int x, int y, int width, int height, String caption, FontProperty font, ETVImages images, ETVImages.BGType type, TextureImage2D texture, boolean clearBefore )
    {
        if ( clearBefore )
            texture.clear( x, y, width, height, true, null );
        
        Rectangle2D capBounds = TextureImage2D.getStringBounds( caption, font );
        int capWidth = (int)Math.ceil( capBounds.getWidth() );
        
        final ImageTemplate it = images.getLabeledDataImage( type );
        final float scale = (float)height / (float)it.getBaseHeight();
        final int hs = it.getBaseHeight();
        
        int xs = 0;
        int xd = x;
        
        int label_border_left = images.getLabeledDataLabelBorderLeft();
        int label_border_left_ = Math.round( label_border_left * scale );
        
        it.drawScaled( xs, 0, label_border_left, hs, xd, y, label_border_left_, height, texture, false );
        
        xs += label_border_left;
        xd += label_border_left_;
        
        int label_width = images.getLabeledDataLabelWidth();
        int label_width_ = capWidth;
        
        it.drawScaled( xs, 0, label_width, hs, xd, y, label_width_, height, texture, false );
        
        xs += label_width;
        xd += label_width_;
        
        int separator_width = images.getLabeledDataSeparatorWidth();
        int separator_width_ = Math.round( separator_width * scale );
        
        it.drawScaled( xs, 0, separator_width, hs, xd, y, separator_width_, height, texture, false );
        
        xs += separator_width;
        xd += separator_width_;
        
        int border_right = images.getLabeledDataDataBorderRight();
        int border_right_ = Math.round( border_right * scale );
        
        int data_width = it.getBaseWidth() - xs - border_right;
        int data_width_ = width - xd + x - border_right_;
        
        it.drawScaled( xs, 0, data_width, hs, xd, y, data_width_, height, texture, false );
        
        xs += data_width;
        xd += data_width_;
        
        it.drawScaled( xs, 0, border_right, hs, xd, y, border_right_, height, texture, false );
    }
    
    public static void drawLabeledCompareBackgroundI( int x, int y, int width, int height, String caption, FontProperty font, ETVImages images, ETVImages.BGType type, TextureImage2D texture, boolean clearBefore )
    {
        if ( clearBefore )
            texture.clear( x, y, width, height, true, null );
        
        Rectangle2D capBounds = TextureImage2D.getStringBounds( caption, font );
        int capWidth = (int)Math.ceil( capBounds.getWidth() );
        
        final ImageTemplate it = images.getCompareImage( type );
        final float scale = (float)height / (float)it.getBaseHeight();
        final int hs = it.getBaseHeight();
        
        int xs = 0;
        int xd = x;
        
        int label_border_left = images.getComparePositionBorderLeft();
        int label_border_left_ = Math.round( label_border_left * scale );
        
        it.drawScaled( xs, 0, label_border_left, hs, xd, y, label_border_left_, height, texture, false );
        
        xs += label_border_left;
        xd += label_border_left_;
        
        int label_width = images.getComparePositionWidth();
        int label_width_ = capWidth;
        
        it.drawScaled( xs, 0, label_width, hs, xd, y, label_width_, height, texture, false );
        
        xs += label_width;
        xd += label_width_;
        
        int separator_width = images.getCompareSeparatorWidth();
        int separator_width_ = Math.round( separator_width * scale );
        
        it.drawScaled( xs, 0, separator_width, hs, xd, y, separator_width_, height, texture, false );
        
        xs += separator_width;
        xd += separator_width_;
        
        int border_right = images.getCompareDataBorderRight();
        int border_right_ = Math.round( border_right * scale );
        
        int data_width = it.getBaseWidth() - xs - border_right;
        int data_width_ = width - xd + x - border_right_;
        
        it.drawScaled( xs, 0, data_width, hs, xd, y, data_width_, height, texture, false );
        
        xs += data_width;
        xd += data_width_;
        
        it.drawScaled( xs, 0, border_right, hs, xd, y, border_right_, height, texture, false );
    }
}
