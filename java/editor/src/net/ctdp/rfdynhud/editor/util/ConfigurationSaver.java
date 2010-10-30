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
package net.ctdp.rfdynhud.editor.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FlatWidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.base.widget.AbstractAssembledWidget;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.__WPrivilegedAccess;

import org.jagatoo.util.ini.IniWriter;
import org.openmali.vecmath2.util.ColorUtils;

public class ConfigurationSaver
{
    private static HashSet<String> getUsedColorNames( WidgetsConfiguration widgetsConfig )
    {
        HashSet<String> result = new HashSet<String>();
        
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        
        for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
        {
            propsCont.clear();
            widgetsConfig.getWidget( i ).getProperties( propsCont, true );
            
            for ( Property prop : propsCont.getList() )
            {
                if ( prop instanceof ColorProperty )
                {
                    String colorKey = ( (ColorProperty)prop ).getColorKey();
                    
                    if ( widgetsConfig.getNamedColor( colorKey ) != null )
                        result.add( colorKey );
                }
                else if ( ( prop instanceof BackgroundProperty ) && ( (BackgroundProperty)prop ).getBackgroundType().isColor() )
                {
                    String colorKey = ( (BackgroundProperty)prop ).getColorProperty().getColorKey();
                    
                    if ( widgetsConfig.getNamedColor( colorKey ) != null )
                        result.add( colorKey );
                }
            }
        }
        
        return ( result );
    }
    
    private static HashSet<String> getUsedFontNames( WidgetsConfiguration widgetsConfig )
    {
        HashSet<String> result = new HashSet<String>();
        
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        
        for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
        {
            propsCont.clear();
            widgetsConfig.getWidget( i ).getProperties( propsCont, true );
            
            for ( Property prop : propsCont.getList() )
            {
                if ( prop instanceof FontProperty )
                {
                    String fontKey = ( (FontProperty)prop ).getFontKey();
                    
                    if ( widgetsConfig.getNamedFont( fontKey ) != null )
                        result.add( fontKey );
                }
            }
        }
        
        return ( result );
    }
    
    @SuppressWarnings( "unused" )
    private static HashSet<String> getUsedBorderAliases( WidgetsConfiguration widgetsConfig )
    {
        HashSet<String> result = new HashSet<String>();
        
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        
        for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
        {
            propsCont.clear();
            widgetsConfig.getWidget( i ).getProperties( propsCont, true );
            
            for ( Property prop : propsCont.getList() )
            {
                if ( prop instanceof BorderProperty )
                {
                    String borderAlias = ( (BorderProperty)prop ).getBorderAlias();
                    
                    if ( widgetsConfig.getBorderName( borderAlias ) != null )
                        result.add( borderAlias );
                }
            }
        }
        
        return ( result );
    }
    
    private static void saveWidget( Widget widget, IniWriter iniWriter, DefaultWidgetsConfigurationWriter confWriter ) throws IOException
    {
        if ( widget.getMasterWidget() != null )
            iniWriter.writeSetting( "<WidgetPart>", ( widget.getName() == null ? "" : widget.getName() ), "The Widget part's name." );
        
        iniWriter.writeSetting( "class", widget.getClass().getName(), "The Java class, that defines the Widget." );
        
        //confWriter.setKeyPrefix( keyPrefix );
        
        widget.saveProperties( confWriter );
        
        if ( widget instanceof AbstractAssembledWidget )
        {
            for ( int i = 0; i < ( (AbstractAssembledWidget)widget ).getNumParts(); i++ )
            {
                Widget part = ( (AbstractAssembledWidget)widget ).getPart( i );
                
                //saveWidget( part.getWidget(), iniWriter, ( keyPrefix == null ) ? part.getKeyPrefix() : keyPrefix + part.getKeyPrefix(), confWriter );
                saveWidget( part, iniWriter, confWriter );
            }
        }
        
        if ( widget.getMasterWidget() != null )
            iniWriter.writeSetting( "</WidgetPart>", ( widget.getName() == null ? "" : widget.getName() ) );
    }
    
    public static void saveConfiguration( WidgetsConfiguration widgetsConfig, String designResultion, int gridOffsetX, int gridOffsetY, int gridSizeX, int gridSizeY, File out ) throws IOException
    {
        final IniWriter iniWriter = new IniWriter( out )
        {
            @Override
            protected String getObjectValue( Object value, Boolean quoteValue )
            {
                if ( value instanceof java.awt.Font )
                    return ( FontUtils.getFontString( (java.awt.Font)value, false, false ) );
                
                if ( value instanceof java.awt.Color )
                    return ( ColorUtils.colorToHex( (java.awt.Color)value ) );
                
                return ( super.getObjectValue( value, quoteValue ) );
            }
        };
        iniWriter.setMinEqualSignPosition( 27 );
        iniWriter.setMinCommentPosition( 55 );
        
        iniWriter.writeGroup( "Meta" );
        iniWriter.writeComment( "The data in this section is only for informational purposes" );
        iniWriter.writeComment( "and will not be used when the config is loaded." );
        iniWriter.writeComment( "Modifications here may not change anything." );
        iniWriter.writeSetting( "rfDynHUD_Version", RFDynHUD.VERSION.toString() );
        iniWriter.writeSetting( "Design_Resolution", designResultion );
        iniWriter.writeSetting( "Design_Grid", "(" + gridOffsetX + "," + gridOffsetY + ";" + gridSizeX + "," + gridSizeY + ")" );
        
        iniWriter.writeGroup( "Global" );
        widgetsConfig.saveProperties( new DefaultWidgetsConfigurationWriter( iniWriter ) );
        
        iniWriter.writeGroup( "NamedColors" );
        HashSet<String> usedColorNames = getUsedColorNames( widgetsConfig );
        ArrayList<String> colorNames = new ArrayList<String>( widgetsConfig.getColorNames() );
        Collections.sort( colorNames, String.CASE_INSENSITIVE_ORDER );
        for ( String name : colorNames )
        {
            if ( usedColorNames.contains( name ) )
                iniWriter.writeSetting( name, widgetsConfig.getNamedColor( name ) );
        }
        
        iniWriter.writeGroup( "NamedFonts" );
        HashSet<String> usedFontNames = getUsedFontNames( widgetsConfig );
        ArrayList<String> fontNames = new ArrayList<String>( widgetsConfig.getFontNames() );
        Collections.sort( fontNames, String.CASE_INSENSITIVE_ORDER );
        for ( String name : fontNames )
        {
            if ( usedFontNames.contains( name ) )
                iniWriter.writeSetting( name, widgetsConfig.getNamedFontString( name ) );
        }
        
        iniWriter.writeGroup( "BorderAliases" );
        //HashSet<String> usedBorderAliases = getUsedBorderAliases( widgetsConfig );
        ArrayList<String> borderAliases = new ArrayList<String>( widgetsConfig.getBorderAliases() );
        Collections.sort( borderAliases, String.CASE_INSENSITIVE_ORDER );
        for ( String alias : borderAliases )
        {
            iniWriter.writeSetting( alias, widgetsConfig.getBorderName( alias ) );
        }
        
        DefaultWidgetsConfigurationWriter confWriter = new DefaultWidgetsConfigurationWriter( iniWriter );
        
        __WCPrivilegedAccess.sortWidgets( widgetsConfig );
        
        int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            if ( widget instanceof AbstractAssembledWidget )
            {
                __WPrivilegedAccess.sortWidgetParts( (AbstractAssembledWidget)widget );
            }
            
            iniWriter.writeGroup( "Widget::" + ( widget.getName() == null ? "" : widget.getName() ) );
            
            saveWidget( widget, iniWriter, confWriter );
        }
        
        iniWriter.close();
    }
}
