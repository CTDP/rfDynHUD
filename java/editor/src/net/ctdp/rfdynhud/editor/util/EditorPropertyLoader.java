/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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

import org.jagatoo.util.versioning.Version;

import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;

/**
 * Loads editor configuration files.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class EditorPropertyLoader implements PropertyLoader
{
    private String keyPrefix = null;
    
    private String currentKey = null;
    private String currentValue = null;
    
    private String effectiveKey = null;
    
    private Version sourceVersion = null;
    
    public void setKeyPrefix( String prefix )
    {
        this.keyPrefix = prefix;
        
        if ( keyPrefix == null )
            effectiveKey = currentKey;
        else
            effectiveKey = currentKey.substring( keyPrefix.length() );
    }
    
    public final String getKeyPrefix()
    {
        return ( keyPrefix );
    }
    
    public void setCurrentSetting( String key, String value )
    {
        this.currentKey = key;
        this.currentValue = value;
        
        setKeyPrefix( keyPrefix );
    }
    
    @Override
    public final String getCurrentKey()
    {
        return ( effectiveKey );
    }
    
    @Override
    public final String getCurrentValue()
    {
        return ( currentValue );
    }
    
    @Override
    public Version getSourceVersion()
    {
        return ( sourceVersion );
    }
    
    @Override
    public boolean loadProperty( Property property )
    {
        if ( property.isMatchingKey( effectiveKey ) )
        {
            property.loadValue( this, currentValue );
            
            return ( true );
        }
        
        return ( false );
    }
}
