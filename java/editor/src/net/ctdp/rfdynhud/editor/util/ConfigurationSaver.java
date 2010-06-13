package net.ctdp.rfdynhud.editor.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FlatWidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.ini.IniWriter;

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
                    String borderAlias = ( (BorderProperty)prop ).getBorderName();
                    
                    if ( widgetsConfig.getBorderName( borderAlias ) != null )
                        result.add( borderAlias );
                }
            }
        }
        
        return ( result );
    }
    
    public static void saveConfiguration( WidgetsConfiguration widgetsConfig, String designResultion, int gridOffsetX, int gridOffsetY, int gridSizeX, int gridSizeY, File out ) throws IOException
    {
        final IniWriter writer = new IniWriter( out );
        writer.setMinEqualSignPosition( 25 );
        writer.setMinCommentPosition( 45 );
        
        writer.writeGroup( "Meta" );
        writer.writeSetting( "rfDynHUD_Version", RFDynHUD.VERSION.toString() );
        writer.writeSetting( "Design_Resolution", designResultion );
        writer.writeSetting( "Design_Grid", "(" + gridOffsetX + "," + gridOffsetY + ";" + gridSizeX + "," + gridSizeY + ")" );
        
        writer.writeGroup( "Global" );
        widgetsConfig.saveProperties( new DefaultWidgetsConfigurationWriter( writer ) );
        
        writer.writeGroup( "NamedColors" );
        HashSet<String> usedColorNames = getUsedColorNames( widgetsConfig );
        ArrayList<String> colorNames = new ArrayList<String>( widgetsConfig.getColorNames() );
        Collections.sort( colorNames, String.CASE_INSENSITIVE_ORDER );
        for ( String name : colorNames )
        {
            if ( usedColorNames.contains( name ) )
                writer.writeSetting( name, widgetsConfig.getNamedColor( name ) );
        }
        
        writer.writeGroup( "NamedFonts" );
        HashSet<String> usedFontNames = getUsedFontNames( widgetsConfig );
        ArrayList<String> fontNames = new ArrayList<String>( widgetsConfig.getFontNames() );
        Collections.sort( fontNames, String.CASE_INSENSITIVE_ORDER );
        for ( String name : fontNames )
        {
            if ( usedFontNames.contains( name ) )
                writer.writeSetting( name, widgetsConfig.getNamedFontString( name ) );
        }
        
        writer.writeGroup( "BorderAliases" );
        //HashSet<String> usedBorderAliases = getUsedBorderAliases( widgetsConfig );
        ArrayList<String> borderAliases = new ArrayList<String>( widgetsConfig.getBorderAliases() );
        Collections.sort( borderAliases, String.CASE_INSENSITIVE_ORDER );
        for ( String alias : borderAliases )
        {
            writer.writeSetting( alias, widgetsConfig.getBorderName( alias ) );
        }
        
        WidgetsConfigurationWriter confWriter = new DefaultWidgetsConfigurationWriter( writer );
        
        __WCPrivilegedAccess.sortWidgets( widgetsConfig );
        
        int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            writer.writeGroup( "Widget::" + ( widget.getName() == null ? "" : widget.getName() ) );
            
            writer.writeSetting( "class", widget.getClass().getName(), "The Java class, that defines the Widget." );
            
            widgetsConfig.getWidget( i ).saveProperties( confWriter );
        }
        
        writer.close();
    }
}
