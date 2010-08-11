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
        writer.writeSetting( property.getName(), property.getValue(), quoteValue, comment );
    }
    
    @Override
    public void writeProperty( Property property, String comment ) throws IOException
    {
        writer.writeSetting( property.getName(), property.getValue(), comment );
    }
    
    public DefaultWidgetsConfigurationWriter( IniWriter writer )
    {
        this.writer = writer;
    }
}
