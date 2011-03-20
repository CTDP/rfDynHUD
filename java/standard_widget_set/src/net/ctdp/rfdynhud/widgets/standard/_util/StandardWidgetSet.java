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

import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.util.StringMapping;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetSet;

public class StandardWidgetSet extends WidgetSet
{
    public static StandardWidgetSet INSTANCE = new StandardWidgetSet();
    
    public static final WidgetPackage WIDGET_PACKAGE = new WidgetPackage( INSTANCE, "CTDP/Standard", WidgetPackage.CTDP_ICON, INSTANCE.getIcon( "net/ctdp/rfdynhud/widgets/standard/widgets.png" ) );
    public static final WidgetPackage WIDGET_PACKAGE_TELEMETRY = new WidgetPackage( INSTANCE, "CTDP/Telemetry", WidgetPackage.CTDP_ICON, INSTANCE.getIcon( "net/ctdp/rfdynhud/widgets/standard/telemetry.png" ) );
    public static final WidgetPackage WIDGET_PACKAGE_TIMING = new WidgetPackage( INSTANCE, "CTDP/Timing", WidgetPackage.CTDP_ICON, INSTANCE.getIcon( "net/ctdp/rfdynhud/widgets/standard/timing.png" ) );
    public static final WidgetPackage WIDGET_PACKAGE_EXTRA = new WidgetPackage( INSTANCE, "CTDP/Extra", WidgetPackage.CTDP_ICON, WidgetPackage.EXTRA_ICON );
    
    public static final StringMapping STANDARD_FONT2 = new StringMapping( "StandardFont2", FontUtils.getFontString( "Dialog", Font.BOLD, 12, true, true ) );
    public static final StringMapping STANDARD_FONT3 = new StringMapping( "StandardFont3", FontUtils.getFontString( "Dialog", Font.BOLD, 11, true, true ) );
    public static final StringMapping SMALLER_FONT = new StringMapping( "SmallerFont", FontUtils.getFontString( "Dialog", Font.BOLD, 13, true, true ) );
    public static final StringMapping SMALLER_FONT3 = new StringMapping( "SmallerFont3", FontUtils.getFontString( "Dialog", Font.BOLD, 9, true, true ) );
    public static final StringMapping BIGGER_FONT = new StringMapping( "BiggerFont", FontUtils.getFontString( "Dialog", Font.PLAIN, 14, true, true ) );
    
    public static final StringMapping POSITION_ITEM_FONT = new StringMapping( "PositionItemFont", FontUtils.getFontString( "Verdana", Font.BOLD, 9, true, true ) );
    
    public static final StringMapping POSITION_ITEM_FONT_COLOR = new StringMapping( "PositionItemFontColor", "#000000" );
    public static final StringMapping POSITION_ITEM_COLOR_NORMAL = new StringMapping( "PositionItemColorNormal","#FFFFFFC0"  );
    public static final StringMapping POSITION_ITEM_COLOR_LEADER = new StringMapping( "PositionItemColorLeader", "#FF0000C0" );
    public static final StringMapping POSITION_ITEM_COLOR_ME = new StringMapping( "PositionItemColorMe", "#00FF00C0" );
    public static final StringMapping POSITION_ITEM_COLOR_NEXT_IN_FRONT = new StringMapping( "PositionItemColorNextInFront", "#0000FFC0" );
    public static final StringMapping POSITION_ITEM_COLOR_NEXT_BEHIND = new StringMapping( "PositionItemColorNextBehind", "#FFFF00C0" );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultBorderValue( String name )
    {
        return ( super.getDefaultBorderValue( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        if ( name.equals( STANDARD_FONT2.getKey() ) )
            return ( STANDARD_FONT2.getValue() );
        
        if ( name.equals( STANDARD_FONT3.getKey() ) )
            return ( STANDARD_FONT3.getValue() );
        
        if ( name.equals( SMALLER_FONT.getKey() ) )
            return ( SMALLER_FONT.getValue() );
        
        if ( name.equals( SMALLER_FONT3.getKey() ) )
            return ( SMALLER_FONT3.getValue() );
        
        if ( name.equals( BIGGER_FONT.getKey() ) )
            return ( BIGGER_FONT.getValue() );
        
        if ( name.equals( POSITION_ITEM_FONT.getKey() ) )
            return ( POSITION_ITEM_FONT.getValue() );
        
        return ( super.getDefaultNamedFontValue( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedColorValue( String name )
    {
        if ( name.equals( POSITION_ITEM_FONT_COLOR.getKey() ) )
            return ( POSITION_ITEM_FONT_COLOR.getValue() );
        
        if ( name.equals( POSITION_ITEM_COLOR_NORMAL.getKey() ) )
            return ( POSITION_ITEM_COLOR_NORMAL.getValue() );
        
        if ( name.equals( POSITION_ITEM_COLOR_LEADER.getKey() ) )
            return ( POSITION_ITEM_COLOR_LEADER.getValue() );
        
        if ( name.equals( POSITION_ITEM_COLOR_ME.getKey() ) )
            return ( POSITION_ITEM_COLOR_ME.getValue() );
        
        if ( name.equals( POSITION_ITEM_COLOR_NEXT_IN_FRONT.getKey() ) )
            return ( POSITION_ITEM_COLOR_NEXT_IN_FRONT.getValue() );
        
        if ( name.equals( POSITION_ITEM_COLOR_NEXT_BEHIND.getKey() ) )
            return ( POSITION_ITEM_COLOR_NEXT_BEHIND.getValue() );
        
        return ( super.getDefaultNamedColorValue( name ) );
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
    
    private StandardWidgetSet()
    {
        super( composeVersion( 1, 2, 1 ) );
    }
}
