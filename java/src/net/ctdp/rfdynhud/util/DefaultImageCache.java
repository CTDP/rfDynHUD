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
    private static class Entry
    {
        private static int nextId = 1;
        
        private final ImageTemplate image;
        private final int id;
        private final long lastModified;
        
        public final int getSizeInBytes()
        {
            if ( image == null )
                return ( 0 );
            
            return ( image.getBaseWidth() * image.getBaseHeight() * ( image.hasAlpha() ? 4 : 3 ) );
        }
        
        public Entry( ImageTemplate image, long lastModified )
        {
            this.image = image;
            this.lastModified = lastModified;
            
            this.id = nextId++;
        }
    }
    
    private final HashMap<String, Entry> map = new HashMap<String, Entry>();
    
    private static final int ONE_MB = 1024 * 1024;
    
    private int maxSize = 10 * ONE_MB;
    private int size = 0;
    
    public final int getSize()
    {
        return ( size );
    }
    
    private void limitCacheSize()
    {
        if ( maxSize <= 0 )
            return;
        
        while ( size > maxSize )
        {
            Entry oldest = null;
            String oldestName = null;
            
            for ( String name : map.keySet() )
            {
                Entry entry = map.get( name );
                
                if ( ( oldest == null ) || ( oldest.id > entry.id ) )
                {
                    oldest = entry;
                    oldestName = name;
                }
            }
            
            remove( oldestName );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ImageTemplate add( String name, long lastModified, ImageTemplate image )
    {
        Entry entry = new Entry( image, lastModified );
        
        size += entry.getSizeInBytes();
        
        Entry old = map.put( name, entry );
        
        limitCacheSize();
        
        //Logger.log( "cache size: " + ( size / ONE_MB ) + " MB" );
        
        if ( old != null )
            return ( old.image );
        
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ImageTemplate remove( String name )
    {
        Entry entry = map.remove( name );
        
        if ( entry != null )
        {
            size -= entry.getSizeInBytes();
            
            //Logger.log( "cache size: " + ( size / ONE_MB ) + " MB" );
            
            return ( entry.image );
        }
        
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void check( String name, long lastModified )
    {
        Entry entry = map.get( name );
        if ( ( entry != null ) && ( entry.lastModified != lastModified ) )
        {
            remove( name );
        }
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
        Entry entry = map.get( name );
        
        if ( entry == null )
            return ( null );
        
        return ( entry.image );
    }
}
