package net.ctdp.rfdynhud.widgets._util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;

public class StandardWidgetSet
{
    public static final String WIDGET_PACKAGE = "";
    
    public static final String POSITION_ITEM_FONT_COLOR_NAME = "PositionItemFontColor";
    public static final String POSITION_ITEM_COLOR_NORMAL = "PositionItemColorNormal";
    public static final String POSITION_ITEM_COLOR_LEADER = "PositionItemColorLeader";
    public static final String POSITION_ITEM_COLOR_ME = "PositionItemColorMe";
    public static final String POSITION_ITEM_COLOR_NEXT_IN_FRONT = "PositionItemColorNextInFront";
    public static final String POSITION_ITEM_COLOR_NEXT_BEHIND = "PositionItemColorNextBehind";
    
    public static final String POSITION_ITEM_FONT_NAME = "PositionItemFont";
    
    public static String getDefaultNamedColorValue( String name )
    {
        if ( name.equals( POSITION_ITEM_FONT_COLOR_NAME ) )
            return ( "#000000" );
        
        if ( name.equals( POSITION_ITEM_COLOR_NORMAL ) )
            return ( "#FFFFFFC0" );
        
        if ( name.equals( POSITION_ITEM_COLOR_LEADER ) )
            return ( "#FF0000C0" );
        
        if ( name.equals( POSITION_ITEM_COLOR_ME ) )
            return ( "#00FF00C0" );
        
        if ( name.equals( POSITION_ITEM_COLOR_NEXT_IN_FRONT ) )
            return ( "#0000FFC0" );
        
        if ( name.equals( POSITION_ITEM_COLOR_NEXT_BEHIND ) )
            return ( "#FFFF00C0" );
        
        return ( null );
    }
    
    public static String getDefaultNamedFontValue( String name )
    {
        if ( name.equals( POSITION_ITEM_FONT_NAME ) )
            return ( "Verdana-BOLD-9va" );
        
        return ( null );
    }
    
    public static void drawPositionItem( TextureImage2D texture, int offsetX, int offsetY, int radius, int place, Color backgroundColor, boolean blackBorder, Font font, boolean fontAntialiased, Color fontColor )
    {
        int width = radius + radius;
        int height = radius + radius;
        
        texture.clear( offsetX, offsetY, width, height, true, null );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        if ( backgroundColor != null )
        {
            texCanvas.setColor( backgroundColor );
        }
        
        texCanvas.setAntialiazingEnabled( true );
        texCanvas.fillArc( 0, 0, width, height, 0, 360 );
        
        if ( blackBorder )
        {
            Stroke oldStroke = texCanvas.getStroke();
            Color oldColor = texCanvas.getColor();
            
            texCanvas.setStroke( new BasicStroke( 2 ) );
            texCanvas.setColor( Color.BLACK );
            
            texCanvas.drawArc( 0, 0, width - 1, height - 1, 0, 360 );
            
            texCanvas.setColor( oldColor );
            texCanvas.setStroke( oldStroke );
        }
        
        if ( ( place > 0 ) && ( font != null ) && ( fontColor != null ) )
        {
            String posStr = String.valueOf( place );
            Rectangle2D bounds = texture.getStringBounds( posStr, font, fontAntialiased );
            float fw = (float)bounds.getWidth();
            float fh = (float)( texture.getFontAscent( font ) - texture.getFontDescent( font ) );
            
            texture.drawString( posStr, radius - (int)( fw / 2 ), radius + (int)( fh / 2 ), bounds, font, fontAntialiased, fontColor, false, null );
        }
    }
}
