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
package net.ctdp.rfdynhud.editor.hiergrid;

import java.util.ArrayList;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public class FlaggedList extends ArrayList< Object >
{
    private static final long serialVersionUID = -2178071328264336996L;
    
    private final String name;
    private boolean expandFlag;
    
    public final String getName()
    {
        return ( name );
    }
    
    public void setExpandFlag( boolean flag )
    {
        this.expandFlag = flag;
    }
    
    public final boolean getExpandFlag()
    {
        return ( expandFlag );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( "FlaggedList \"" + getName() + "\"" );
    }
    
    public FlaggedList( String name, boolean expandFlag, int initialCapacity )
    {
        super( initialCapacity );
        
        this.name = name;
        this.expandFlag = expandFlag;
    }
    
    public FlaggedList( String name, boolean expandFlag )
    {
        this( name, expandFlag, 16 );
    }
    
    public FlaggedList( String name, int initialCapacity )
    {
        this( name, false, initialCapacity );
    }
    
    public FlaggedList( String name )
    {
        this( name, false );
    }
}
