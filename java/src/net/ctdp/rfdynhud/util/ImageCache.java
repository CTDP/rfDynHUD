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

import net.ctdp.rfdynhud.render.ImageTemplate;

/**
 * An image cache caches images by their names.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public interface ImageCache
{
    /**
     * Adds an image to the cache.
     * 
     * @param name the name to cache by
     * @param lastModified the last modified timestamp
     * @param image the image to cache
     * 
     * @return the previously cached image.
     */
    public ImageTemplate add( String name, long lastModified, ImageTemplate image );
    
    /**
     * Removes an image from the cache.
     * 
     * @param name the name it is cached by
     * 
     * @return the cached image, if present.
     */
    public ImageTemplate remove( String name );
    
    /**
     * Checks, whether an image exists in the cache by the given name.
     * 
     * @param name the name to search for
     * 
     * @return <code>true</code>, if the image is present, <code>false</code> otherwise.
     */
    public boolean contains( String name );
    
    /**
     * Checks, whether an image is cached by the given name and matches the given 'lastModified' date.
     * 
     * @param name the name to search for
     * @param lastModified the last modified timestamp
     */
    public void check( String name, long lastModified );
    
    /**
     * Gets the cached image by the given name.
     * 
     * @param name the name to search for
     * 
     * @return the cached image or <code>null</code>, if not present.
     */
    public ImageTemplate get( String name );
}
