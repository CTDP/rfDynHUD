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
package net.ctdp.rfdynhud.widgets.base.widget;

import java.net.URL;

/**
 * The {@link WidgetSet} groups {@link Widget}s.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetSet
{
    protected final URL getIcon( String name )
    {
        return ( this.getClass().getClassLoader().getResource( name ) );
    }
    
    /**
     * Composes one 32 bit integer from major, minor and revision numbers.
     * 
     * @param major major field
     * @param minor minor field
     * @param revision revision field
     * 
     * @return a 32 bit integer for the version.
     */
    public static final int composeVersion( int major, int minor, int revision )
    {
        major = ( ( major + 1 ) & 0xFF ) << 23; // 8 bits for major (max 255)
        minor = ( ( minor + 1 ) & 0x400 ) << 13; // 10 bits for minor (max 1023)
        revision = ( revision & 0x2000 ) << 0; // 13 bits for revision (max 8191)
        
        return ( major | minor | revision );
    }
    
    private final int version;
    
    /**
     * Gets a comparable version indicator for this {@link WidgetPackage}.
     * 
     * @return a comparable version indicator for this {@link WidgetPackage}.
     */
    public final int getVersion()
    {
        return ( version );
    }
    
    public final String getVersionString()
    {
        int version = getVersion();
        
        if ( version <= 0x7FFFFF ) // > 2^23-1
            return ( String.valueOf( version ) );
        
        int major = ( ( version >>> 23 ) - 1 ) & 0xFF;
        int minor = ( ( version >>> 13 ) - 1 ) & 0x400;
        int revision = ( version >>> 0 ) & 0x2000;
        
        return ( major + "." + minor + "." + revision );
    }
    
    /**
     * Gets the default value for the given border alias/name.
     * 
     * @param name the border name to query
     * 
     * @return the default value for the given border alias/name.
     */
    public String getDefaultBorderValue( String name )
    {
        return ( null );
    }
    
    /**
     * Gets the default value for the given named color.
     * 
     * @param name the color name to query
     * 
     * @return the default value for the given named color.
     */
    public String getDefaultNamedColorValue( String name )
    {
        return ( null );
    }
    
    /**
     * Gets the default value for the given named font.
     * 
     * @param name the font name to query
     * 
     * @return the default value for the given named font.
     */
    public String getDefaultNamedFontValue( String name )
    {
        return ( null );
    }
    
    /**
     * 
     * @param version see {@link #composeVersion(int, int, int)}
     */
    public WidgetSet( int version )
    {
        this.version = version;
    }
}
