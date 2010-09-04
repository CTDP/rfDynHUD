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
package net.ctdp.rfdynhud.util;

import java.util.HashMap;

import net.ctdp.rfdynhud.render.ImageTemplate;

/**
 * Default implementation of the image cache meant for game runtime.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DefaultImageCache implements ImageCache
{
    private final HashMap<String, ImageTemplate> map = new HashMap<String, ImageTemplate>();
    
    private int size = 0;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ImageTemplate add( String name, ImageTemplate image )
    {
        if ( image != null )
            size += image.getBaseWidth() * image.getBaseHeight() * ( image.hasAlpha() ? 4 : 3 );
        
        Logger.log( "cache size: " + ( size / ( 1024 * 1024 ) ) + " MB" );
        
        return ( map.put( name, image ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ImageTemplate remove( String name )
    {
        ImageTemplate image = map.remove( name );
        
        if ( image != null )
            size += image.getBaseWidth() * image.getBaseHeight() * ( image.hasAlpha() ? 4 : 3 );
        
        Logger.log( "cache size: " + ( size / ( 1024 * 1024 ) ) + " MB" );
        
        return ( image );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains( String name )
    {
        return ( map.containsKey( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ImageTemplate get( String name )
    {
        return ( map.get( name ) );
    }
}
