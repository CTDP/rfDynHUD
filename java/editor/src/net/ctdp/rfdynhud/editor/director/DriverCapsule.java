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
package net.ctdp.rfdynhud.editor.director;

/**
 * Driver capsule.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DriverCapsule
{
    public static final DriverCapsule DEFAULT_DRIVER = new DriverCapsule( "(Auto)", -1 );
    
    private final String name;
    private final int id;
    
    public final String getName()
    {
        return ( name );
    }
    
    public final int getId()
    {
        return ( id );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( name );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( !( o instanceof DriverCapsule ) )
            return ( false );
        
        return ( ( (DriverCapsule)o ).id == this.id );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ( id );
    }
    
    public DriverCapsule( String name, int id )
    {
        this.name = name;
        this.id = id;
    }
}
