package net.ctdp.rfdynhud.editor.util;

import java.io.IOException;

import org.jagatoo.util.ini.IniWriter;

import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;

public class DefaultWidgetsConfigurationWriter implements WidgetsConfigurationWriter
{
    private final IniWriter writer;
    
    @Override
    public void writeProperty( String key, Object value, String comment ) throws IOException
    {
        writer.writeSetting( key, value, comment );
    }
    
    @Override
    public void writeProperty( String key, Object value, Boolean quoteValue, String comment ) throws IOException
    {
        writer.writeSetting( key, value, quoteValue, comment );
    }
    
    @Override
    public void writeProperty( Property property, Boolean quoteValue, String comment ) throws IOException
    {
        writer.writeSetting( property.getPropertyName(), property.getValue(), quoteValue, comment );
    }
    
    @Override
    public void writeProperty( Property property, String comment ) throws IOException
    {
        writer.writeSetting( property.getPropertyName(), property.getValue(), comment );
    }
    
    public DefaultWidgetsConfigurationWriter( IniWriter writer )
    {
        this.writer = writer;
    }
}
