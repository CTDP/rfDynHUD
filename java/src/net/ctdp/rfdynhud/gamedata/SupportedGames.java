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
package net.ctdp.rfdynhud.gamedata;

import org.jagatoo.util.versioning.Version;

/**
 * This is an enumeration of supprted games.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public enum SupportedGames
{
    rFactor( new Version( 1, 255, 0, "F", 0  ) ),
    ;
    
    private final Version lastKnownVersion;
    
    /**
     * Gets the last known and fully supported version. Later versions may cause problems.
     * 
     * @return the last known and fully supported version.
     */
    public final Version getLastKnownVersion()
    {
        return ( lastKnownVersion );
    }
    
    private SupportedGames( Version lastKnownVersion )
    {
        this.lastKnownVersion = lastKnownVersion;
    }
}
