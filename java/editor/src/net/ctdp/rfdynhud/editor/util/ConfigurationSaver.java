package net.ctdp.rfdynhud.editor.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.ini.IniWriter;

public class ConfigurationSaver
{
    public static void saveConfiguration( WidgetsConfiguration widgetsConfig, String designResultion, File out ) throws IOException
    {
        final IniWriter writer = new IniWriter( out );
        writer.setMinEqualSignPosition( 25 );
        writer.setMinCommentPosition( 45 );
        
        writer.writeGroup( "Meta" );
        writer.writeSetting( "rfDynHUD_Version", RFDynHUD.VERSION.toString() );
        if ( designResultion != null )
            writer.writeSetting( "Design_Resolution", designResultion );
        
        writer.writeGroup( "NamedColors" );
        ArrayList<String> colorNames = new ArrayList<String>( widgetsConfig.getColorNames() );
        Collections.sort( colorNames, String.CASE_INSENSITIVE_ORDER );
        for ( String name : colorNames )
        {
            writer.writeSetting( name, widgetsConfig.getNamedColor( name ) );
        }
        
        writer.writeGroup( "NamedFonts" );
        ArrayList<String> fontNames = new ArrayList<String>( widgetsConfig.getFontNames() );
        Collections.sort( fontNames, String.CASE_INSENSITIVE_ORDER );
        for ( String name : fontNames )
        {
            writer.writeSetting( name, widgetsConfig.getNamedFontString( name ) );
        }
        
        writer.writeGroup( "BorderAliases" );
        for ( String alias : widgetsConfig.getBorderAliases() )
        {
            writer.writeSetting( alias, widgetsConfig.getBorderName( alias ) );
        }
        
        final WidgetsConfigurationWriter confWriter = new WidgetsConfigurationWriter()
        {
            public void writeProperty( String key, Object value, String comment ) throws IOException
            {
                writer.writeSetting( key, value, comment );
            }
            
            public void writeProperty( String key, Object value, Boolean quoteValue, String comment ) throws IOException
            {
                writer.writeSetting( key, value, quoteValue, comment );
            }
            
            public void writeProperty( Property property, Boolean quoteValue, String comment ) throws IOException
            {
                writer.writeSetting( property.getPropertyName(), property.getValue(), quoteValue, comment );
            }
            
            public void writeProperty( Property property, String comment ) throws IOException
            {
                writer.writeSetting( property.getPropertyName(), property.getValue(), comment );
            }
        };
        
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
