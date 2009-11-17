package net.ctdp.rfdynhud.editor.util;

import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.ini.IniWriter;

public class ConfigurationSaver
{
    public static void saveConfiguration( WidgetsConfiguration widgetsConfig, File out ) throws IOException
    {
        final IniWriter writer = new IniWriter( out );
        writer.setMinEqualSignPosition( 25 );
        writer.setMinCommentPosition( 45 );
        
        writer.writeGroup( "NamedColors" );
        for ( String name : widgetsConfig.getColorNames() )
        {
            writer.writeSetting( name, widgetsConfig.getNamedColor( name ) );
        }
        
        writer.writeGroup( "NamedFonts" );
        for ( String name : widgetsConfig.getFontNames() )
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
