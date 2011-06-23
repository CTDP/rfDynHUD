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
package net.ctdp.rfdynhud.plugins.director;

import java.io.File;

import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.util.ConfigurationCandidatesIterator;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class NullingConfigurationCandidatesIterator extends ConfigurationCandidatesIterator
{
    private boolean nulling = false;
    
    public void setNulling( boolean b )
    {
        this.nulling = b;
    }
    
    @Override
    public void collectCandidates( File configFolder, boolean smallMonitor, boolean bigMonitor, boolean isInGarage, String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        if ( !nulling )
            super.collectCandidates( configFolder, smallMonitor, bigMonitor, isInGarage, modName, vehicleClass, vehicleName, sessionType );
    }
}
