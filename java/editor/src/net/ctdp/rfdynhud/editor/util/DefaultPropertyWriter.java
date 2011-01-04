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

import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.PropertyWriter;

/**
 * Default implementation of a {@link PropertyWriter}.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DefaultPropertyWriter extends PropertyWriter
{
    public static final Object DEFAULT_PLACEHOLDER = ConfigurationLoader.DEFAULT_PLACEHOLDER;
    
    private final boolean handleDefaults;
    
    private final IniWriter writer;
    
    private String keyPrefix = null;
    
    public final IniWriter getIniWriter()
    {
        return ( writer );
    }
    
    public void setKeyPrefix( String prefix )
    {
        this.keyPrefix = prefix;
    }
    
    public final String getKeyPrefix()
    {
        return ( keyPrefix );
    }
    
    @Override
    public void writeProperty( String key, Object value, boolean isDefaultValue, String comment ) throws IOException
    {
        if ( handleDefaults && isDefaultValue )
            value = DEFAULT_PLACEHOLDER;
        
        if ( keyPrefix == null )
            writer.writeSetting( key, value, comment );
        else
            writer.writeSetting( keyPrefix + key, value, comment );
    }
    
    @Override
    public void writeProperty( String key, Object value, boolean isDefaultValue, Boolean quoteValue, String comment ) throws IOException
    {
        if ( handleDefaults && isDefaultValue )
            value = DEFAULT_PLACEHOLDER;
        
        if ( keyPrefix == null )
            writer.writeSetting( key, value, quoteValue, comment );
        else
            writer.writeSetting( keyPrefix + key, value, quoteValue, comment );
    }
    
    public DefaultPropertyWriter( IniWriter writer, String keyPrefix, boolean handleDefaults )
    {
        this.writer = writer;
        this.keyPrefix = keyPrefix;
        this.handleDefaults = handleDefaults;
    }
    
    public DefaultPropertyWriter( IniWriter writer, boolean handleDefaults )
    {
        this( writer, null, handleDefaults );
    }
}
