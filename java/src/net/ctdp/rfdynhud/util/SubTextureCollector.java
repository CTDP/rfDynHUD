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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import net.ctdp.rfdynhud.render.TransformableTexture;

/**
 * Sub textures ({@link TransformableTexture}s) are collected here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SubTextureCollector
{
    private final ArrayList<TransformableTexture> list = new ArrayList<TransformableTexture>();
    
    /**
     * Adds a {@link TransformableTexture} to the end of the list.
     * 
     * @param subTexture
     */
    public void add( TransformableTexture subTexture )
    {
        list.add( subTexture );
    }
    
    /**
     * Gets the number of sub textures in the list.
     * 
     * @return the number of sub textures in the list.
     */
    public final int getNumberOf()
    {
        return ( list.size() );
    }
    
    /**
     * Gets the index-th sub texture from the list.
     * 
     * @param index
     * 
     * @return the index-th sub texture from the list.
     */
    public final TransformableTexture get( int index )
    {
        return ( list.get( index ) );
    }
    
    private static final Comparator<TransformableTexture> LOCAL_Z_INDEX_COMPARATOR = new Comparator<TransformableTexture>()
    {
        @Override
        public int compare( TransformableTexture tt1, TransformableTexture tt2 )
        {
            if ( tt1.getLocalZIndex() < tt2.getLocalZIndex() )
                return ( -1 );
            
            if ( tt1.getLocalZIndex() > tt2.getLocalZIndex() )
                return ( +1 );
            
            return ( 0 );
        }
    };
    
    final TransformableTexture[] getArray( boolean sort )
    {
        if ( list.size() == 0 )
            return ( null );
        
        TransformableTexture[] array = new TransformableTexture[ list.size() ];
        array = list.toArray( array );
        
        if ( sort )
            Arrays.sort( array, LOCAL_Z_INDEX_COMPARATOR );
        
        return ( array );
    }
}
