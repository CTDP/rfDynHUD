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
package net.ctdp.rfdynhud.util;

/**
 * This is a simple utility class, that maps one string to another one.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class StringMapping
{
    private final String key;
    private final String value;
    
    /**
     * Gets the key-String.
     * 
     * @return the key-String.
     */
    public final String getKey()
    {
        return ( key );
    }
    
    /**
     * Gets the value-String.
     * 
     * @return the value-String.
     */
    public final String getValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ( key.hashCode() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( o == this )
            return ( true );
        
        if ( !( o instanceof StringMapping ) )
            return ( false );
        
        StringMapping sm = (StringMapping)o;
        
        return ( this.key.equals( sm.key ) && this.value.equals( sm.value ) );
    }
    
    public StringMapping( String key, String value )
    {
        this.key = key;
        this.value = value;
    }
}
