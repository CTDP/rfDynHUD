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
package net.ctdp.rfdynhud.widgets.standard._util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.net.URL;

import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;

public class StandardWidgetSet
{
    private static final URL getIcon( String name )
    {
        return ( StandardWidgetSet.class.getClassLoader().getResource( name ) );
    }
    
    private static int VERSION = WidgetPackage.composeVersion( 1, 2, 0 );
    public static final WidgetPackage WIDGET_PACKAGE = new WidgetPackage( "CTDP/Standard", VERSION, WidgetPackage.CTDP_ICON, getIcon( "net/ctdp/rfdynhud/widgets/standard/widgets.png" ) );
    public static final WidgetPackage WIDGET_PACKAGE_TELEMETRY = new WidgetPackage( "CTDP/Telemetry", VERSION, WidgetPackage.CTDP_ICON, getIcon( "net/ctdp/rfdynhud/widgets/standard/telemetry.png" ) );
    public static final WidgetPackage WIDGET_PACKAGE_TIMING = new WidgetPackage( "CTDP/Timing", VERSION, WidgetPackage.CTDP_ICON, getIcon( "net/ctdp/rfdynhud/widgets/standard/timing.png" ) );
    
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
    
    public static java.awt.Dimension getPositionItemSize( int radius, LabelPositioning namePositioning, Font nameFont, boolean nameFontAntialiased )
    {
        int width = radius + radius;
        int height = radius + radius;
        
        if ( ( namePositioning != null ) && ( nameFont != null ) )
        {
            Rectangle2D nameBounds = TextureImage2D.getStringBounds( "WWW", nameFont, nameFontAntialiased );
            
            switch ( namePositioning )
            {
                case ABOVE:
                case BELOW:
                    width = Math.max( width, (int)nameBounds.getWidth() );
                    height += nameBounds.getHeight();
                    break;
                case BELOW_RIGHT:
                    int nxy = radius + (int)Math.sqrt( ( radius + 7 ) * ( radius + 7 ) / 2 );
                    
                    width += radius + radius - nxy + nameBounds.getWidth() + 2;
                    height += radius + radius - nxy + nameBounds.getHeight();
                    break;
            }
        }
        
        return ( new java.awt.Dimension( Math.max( 1, width ), Math.max( 1, height ) ) );
    }
    
    public static int drawPositionItem( TextureImage2D texture, int offsetX, int offsetY, int radius, int place, Color backgroundColor, int blackBorderWidth, Font numberFont, boolean numberFontAntialiased, Color numberFontColor, LabelPositioning namePositioning, String driverName, Font nameFont, boolean nameFontAntialiased, Color nameFontColor )
    {
        int width = radius + radius;
        int height = radius + radius;
        
        Rectangle2D nameBounds = null;
        int nx = 0;
        int ny = 0;
        int circleOffsetX = 0;
        int circleOffsetY = 0;
        if ( ( namePositioning != null ) && ( driverName != null ) && ( nameFont != null ) && ( nameFontColor != null ) )
        {
            nameBounds = TextureImage2D.getStringBounds( "WWW", nameFont, nameFontAntialiased );
            switch ( namePositioning )
            {
                case ABOVE:
                    circleOffsetX = Math.max( 0, ( (int)nameBounds.getWidth() - width ) / 2 );
                    circleOffsetY = (int)nameBounds.getHeight();
                    width = Math.max( width, (int)nameBounds.getWidth() );
                    height += nameBounds.getHeight();
                    ny = 0;
                    break;
                case BELOW:
                    circleOffsetX = Math.max( 0, ( (int)nameBounds.getWidth() - width ) / 2 );
                    circleOffsetY = 0;
                    width = Math.max( width, (int)nameBounds.getWidth() );
                    height += nameBounds.getHeight();
                    ny = radius + radius + 0;
                    break;
                case BELOW_RIGHT:
                    nx = radius + (int)Math.sqrt( ( radius + 7 ) * ( radius + 7 ) / 2 );
                    ny = nx;
                    
                    width += radius + radius - nx + nameBounds.getWidth();
                    height += radius + radius - ny + nameBounds.getHeight();
                    break;
            }
        }
        
        texture.clear( offsetX, offsetY, width, height, true, null );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        if ( backgroundColor != null )
        {
            texCanvas.setColor( backgroundColor );
        }
        
        texCanvas.setAntialiazingEnabled( true );
        texCanvas.fillArc( circleOffsetX, circleOffsetY, radius + radius, radius + radius, 0, 360 );
        
        if ( blackBorderWidth > 0 )
        {
            Stroke oldStroke = texCanvas.getStroke();
            Color oldColor = texCanvas.getColor();
            
            texCanvas.setStroke( new BasicStroke( blackBorderWidth ) );
            texCanvas.setColor( Color.BLACK );
            
            texCanvas.drawArc( circleOffsetX, circleOffsetY, radius + radius - 1, radius + radius - 1, 0, 360 );
            
            texCanvas.setColor( oldColor );
            texCanvas.setStroke( oldStroke );
        }
        
        if ( ( place > 0 ) && ( numberFont != null ) && ( numberFontColor != null ) )
        {
            String posStr = String.valueOf( place );
            Rectangle2D bounds = TextureImage2D.getStringBounds( posStr, numberFont, numberFontAntialiased );
            float fw = (float)bounds.getWidth();
            float fh = TextureImage2D.getFontAscent( numberFont ) - TextureImage2D.getFontDescent( numberFont );
            
            texture.drawString( posStr, circleOffsetX + radius - (int)( fw / 2 ), circleOffsetY + radius + (int)( fh / 2 ), bounds, numberFont, numberFontAntialiased, numberFontColor, false, null );
        }
        
        if ( ( namePositioning != null ) && ( driverName != null ) && ( nameFont != null ) && ( nameFontColor != null ) )
        {
            if ( ( namePositioning == LabelPositioning.ABOVE ) || ( namePositioning == LabelPositioning.BELOW ) )
            {
                nameBounds = TextureImage2D.getStringBounds( driverName, nameFont, nameFontAntialiased );
                nx = ( width - (int)nameBounds.getWidth() ) / 2;
                ny -= nameBounds.getY();
            }
            
            texture.drawString( driverName, nx, ny, nameBounds, nameFont, nameFontAntialiased, nameFontColor, false, null );
        }
        
        return ( circleOffsetY );
    }
}
