package net.ctdp.rfdynhud.etv2010.widgets._util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;

import org.openmali.types.twodee.Rect2i;

public class ETVUtils
{
    public static final String WIDGET_PACKAGE = "CTDP/Ecclestone TV 2010";
    
    public static final int TRIANGLE_WIDTH = 15;
    private static final boolean AA_TRIANGLE = true;
    
    public static final String ETV_STYLE_CAPTION_BACKGROUND_COLOR = "ETVCaptionBackgroundColor";
    public static final String ETV_STYLE_CAPTION_BACKGROUND_COLOR_1ST = "ETVCaptionBackgroundColor1st";
    public static final String ETV_STYLE_CAPTION_FONT_COLOR = "ETVCaptionFontColor";
    public static final String ETV_STYLE_DATA_BACKGROUND_COLOR = "ETVDataBackgroundColor";
    public static final String ETV_STYLE_DATA_FONT_COLOR = "ETVDataFontColor";
    public static final String ETV_STYLE_FONT = "ETVFont";
    
    public static String getDefaultNamedColorValue( String name )
    {
        if ( name.equals( ETV_STYLE_CAPTION_BACKGROUND_COLOR ) )
            return ( "#787878" );
        
        if ( name.equals( ETV_STYLE_CAPTION_BACKGROUND_COLOR_1ST ) )
            return ( "#FF0000" );
        
        if ( name.equals( ETV_STYLE_CAPTION_FONT_COLOR ) )
            return ( "#FFFFFF" );
        
        if ( name.equals( ETV_STYLE_DATA_BACKGROUND_COLOR ) )
            return ( "#000000" );
        
        if ( name.equals( ETV_STYLE_DATA_FONT_COLOR ) )
            return ( "#FFFFFF" );
        
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
    
    public static final int getLabeledDataDataLeft( int width, Rectangle2D captionBounds )
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
    
    public static void drawDataBackground( int x, int y, int width, int height, Color dataBgColor, TextureImage2D texture, boolean clearBefore )
    {
        if ( clearBefore )
            texture.clear( x, y, width, height, true, null );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        texCanvas.setColor( dataBgColor );
        
        texCanvas.setAntialiazingEnabled( AA_TRIANGLE );
        
        int[] xPoints = new int[] { x, x + TRIANGLE_WIDTH, x + TRIANGLE_WIDTH, x };
        int[] yPoints = new int[] { y + height, y + height, y + 0, y + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
        
        texCanvas.setAntialiazingEnabled( false );
        
        //int lapAreaWidth = width - 4 * TRIANGLE_WIDTH - capWidth;
        Rect2i rect = new Rect2i( x + TRIANGLE_WIDTH, y + 0, width - 2 * TRIANGLE_WIDTH, y + height );
        
        texCanvas.fillRect( rect );
        //texCanvas.drawRect( rect );
        
        texCanvas.setAntialiazingEnabled( AA_TRIANGLE );
        
        xPoints = new int[] { x + width - TRIANGLE_WIDTH, x + width, x + width - TRIANGLE_WIDTH, x + width - TRIANGLE_WIDTH };
        yPoints = new int[] { y + height, y + 0, y + 0, y + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
    }
    
    public static void drawLabeledDataBackground( int x, int y, int width, int height, String caption, Font font, Color captionBgColor, Color dataBgColor, TextureImage2D texture, boolean clearBefore )
    {
        if ( clearBefore )
            texture.clear( x, y, width, height, true, null );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        texCanvas.setFont( font );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D capBounds = metrics.getStringBounds( caption, texCanvas );
        
        texCanvas.setColor( captionBgColor );
        
        texCanvas.setAntialiazingEnabled( AA_TRIANGLE );
        
        int[] xPoints = new int[] { x + 0, x + TRIANGLE_WIDTH, x + TRIANGLE_WIDTH, x + 0 };
        int[] yPoints = new int[] { y + height, y + height, y + 0, y + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
        
        texCanvas.setAntialiazingEnabled( false );
        
        int capWidth = (int)Math.ceil( capBounds.getWidth() );
        Rect2i rect = new Rect2i( x + TRIANGLE_WIDTH, y + 0, capWidth, y + height );
        
        texCanvas.fillRect( rect );
        //texCanvas.drawRect( rect );
        
        texCanvas.setAntialiazingEnabled( AA_TRIANGLE );
        
        xPoints = new int[] { x + TRIANGLE_WIDTH + capWidth, x + TRIANGLE_WIDTH + capWidth + TRIANGLE_WIDTH, x + TRIANGLE_WIDTH + capWidth, x + TRIANGLE_WIDTH + capWidth };
        yPoints = new int[] { y + height, y + 0, y + 0, y + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
        
        drawDataBackground( x + TRIANGLE_WIDTH + capWidth, y, width - TRIANGLE_WIDTH - capWidth, height, dataBgColor, texture, false );
    }
}
