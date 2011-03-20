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
package net.ctdp.rfdynhud.widgets.etv2010._util;

import java.awt.Font;

import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.util.StringMapping;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetSet;

public class ETVWidgetSet extends WidgetSet
{
    public static final ETVWidgetSet INSTANCE = new ETVWidgetSet();
    
    public static final WidgetPackage WIDGET_PACKAGE = new WidgetPackage( INSTANCE, "CTDP/Ecclestone TV 2010", WidgetPackage.CTDP_ICON, INSTANCE.getIcon( "net/ctdp/rfdynhud/widgets/etv2010/etv2010.png" ) );
    
    public static final StringMapping ETV_CAPTION_BACKGROUND_COLOR = new StringMapping( "ETVCaptionBackgroundColor", "#787878" );
    public static final StringMapping ETV_CAPTION_BACKGROUND_COLOR_1ST = new StringMapping( "ETVCaptionBackgroundColor1st", "#B10000" );
    public static final StringMapping ETV_CAPTION_FONT_COLOR = new StringMapping( "ETVCaptionFontColor", "#FFFFFF" );
    public static final StringMapping ETV_DATA_BACKGROUND_COLOR_1ST = new StringMapping( "ETVDataBackgroundColor1st", "#230000" );
    public static final StringMapping ETV_DATA_BACKGROUND_COLOR = new StringMapping( "ETVDataBackgroundColor", "#000000" );
    public static final StringMapping ETV_DATA_BACKGROUND_COLOR_FASTEST = new StringMapping( "ETVDataBackgroundColorFastest", "#C000D2" );
    public static final StringMapping ETV_DATA_BACKGROUND_COLOR_FASTER = new StringMapping( "ETVDataBackgroundColorFaster", "#008800" );
    public static final StringMapping ETV_DATA_BACKGROUND_COLOR_SLOWER = new StringMapping( "ETVDataBackgroundColorSlower", "#BAB802" );
    public static final StringMapping ETV_DATA_FONT_COLOR = new StringMapping( "ETVDataFontColor", "#FFFFFF" );
    public static final StringMapping ETV_DATA_FONT_COLOR_FASTEST = new StringMapping( "ETVDataFontColorFasterst", "#000000" );
    public static final StringMapping ETV_DATA_FONT_COLOR_FASTER = new StringMapping( "ETVDataFontColorFaster", "#FFFFFF" );
    public static final StringMapping ETV_DATA_FONT_COLOR_SLOWER = new StringMapping( "ETVDataFontColorSlower", "#000000" );
    
    public static final StringMapping ETV_FONT = new StringMapping( "ETVFont", FontUtils.getFontString( "DokChampa", Font.BOLD, 16, true, true ) );
    public static final StringMapping ETV_VELOCITY_FONT = new StringMapping( "ETVVelocityFont", FontUtils.getFontString( "DokChampa", Font.BOLD, 18, true, true ) );
    public static final StringMapping ETV_REV_MARKERS_FONT = new StringMapping( "ETVRevMarkersFont", FontUtils.getFontString( "DokChampa", Font.BOLD, 18, true, true ) );
    public static final StringMapping ETV_GEAR_FONT = new StringMapping( "ETVGearFont", FontUtils.getFontString( "DokChampa", Font.BOLD, 30, true, true ) );
    public static final StringMapping ETV_CONTROLS_LABEL_FONT = new StringMapping( "ETVControlsLabelFont", FontUtils.getFontString( "DokChampa", Font.BOLD, 18, true, true ) );
    
    @Override
    public String getDefaultNamedColorValue( String name )
    {
        if ( name.equals( ETV_CAPTION_BACKGROUND_COLOR.getKey() ) )
            return ( ETV_CAPTION_BACKGROUND_COLOR.getValue() );
        
        if ( name.equals( ETV_CAPTION_BACKGROUND_COLOR_1ST.getKey() ) )
            return ( ETV_CAPTION_BACKGROUND_COLOR_1ST.getValue() );
        
        if ( name.equals( ETV_CAPTION_FONT_COLOR.getKey() ) )
            return ( ETV_CAPTION_FONT_COLOR.getValue() );
        
        if ( name.equals( ETV_DATA_BACKGROUND_COLOR_1ST.getKey() ) )
            return ( ETV_DATA_BACKGROUND_COLOR_1ST.getValue() );
        
        if ( name.equals( ETV_DATA_BACKGROUND_COLOR.getKey() ) )
            return ( ETV_DATA_BACKGROUND_COLOR.getValue() );
        
        if ( name.equals( ETV_DATA_BACKGROUND_COLOR_FASTEST.getKey() ) )
            return ( ETV_DATA_BACKGROUND_COLOR_FASTEST.getValue() );
        
        if ( name.equals( ETV_DATA_BACKGROUND_COLOR_FASTER.getKey() ) )
            return ( ETV_DATA_BACKGROUND_COLOR_FASTER.getValue() );
        
        if ( name.equals( ETV_DATA_BACKGROUND_COLOR_SLOWER.getKey() ) )
            return ( ETV_DATA_BACKGROUND_COLOR_SLOWER.getValue() );
        
        if ( name.equals( ETV_DATA_FONT_COLOR.getKey() ) )
            return ( ETV_DATA_FONT_COLOR.getValue() );
        
        if ( name.equals( ETV_DATA_FONT_COLOR_FASTEST.getKey() ) )
            return ( ETV_DATA_FONT_COLOR_FASTEST.getValue() );
        
        if ( name.equals( ETV_DATA_FONT_COLOR_FASTER.getKey() ) )
            return ( ETV_DATA_FONT_COLOR_FASTER.getValue() );
        
        if ( name.equals( ETV_DATA_FONT_COLOR_SLOWER.getKey() ) )
            return ( ETV_DATA_FONT_COLOR_SLOWER.getValue() );
        
        return ( super.getDefaultNamedColorValue( name ) );
    }
    
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        if ( name.equals( ETV_FONT.getKey() ) )
            return ( ETV_FONT.getValue() );
        
        if ( name.equals( ETV_VELOCITY_FONT.getKey() ) )
            return ( ETV_VELOCITY_FONT.getValue() );
        
        if ( name.equals( ETV_REV_MARKERS_FONT.getKey() ) )
            return ( ETV_REV_MARKERS_FONT.getValue() );
        
        if ( name.equals( ETV_GEAR_FONT.getKey() ) )
            return ( ETV_GEAR_FONT.getValue() );
        
        if ( name.equals( ETV_CONTROLS_LABEL_FONT.getKey() ) )
            return ( ETV_CONTROLS_LABEL_FONT.getValue() );
        
        return ( super.getDefaultNamedFontValue( name ) );
    }
    
    private ETVWidgetSet()
    {
        super( composeVersion( 1, 2, 1 ) );
    }
}
