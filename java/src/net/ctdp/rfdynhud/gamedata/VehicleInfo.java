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

/**
 * Keeps all descriptive information read from the .VEH file.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class VehicleInfo
{
    int carNumber = -1;
    String teamName = null;
    String pitGroup = null;
    String driverName = null;
    String driverDescription = null;
    String engineName = null;
    String manufacturer = null;
    String classes = null;
    
    String fullTeamName = null;
    String teamFounded = null;
    String teamHeadquarters = null;
    int teamStarts = -1;
    int teamPoles = -1;
    int teamWins = -1;
    int teamWorldChampionships = -1;
    
    String category = null;
    
    void reset()
    {
        carNumber = -1;
        teamName = null;
        pitGroup = null;
        driverName = null;
        driverDescription = null;
        engineName = null;
        manufacturer = null;
        classes = null;
        
        fullTeamName = null;
        teamFounded = null;
        teamHeadquarters = null;
        teamStarts = -1;
        teamPoles = -1;
        teamWins = -1;
        teamWorldChampionships = -1;
        
        category = null;
    }
    
    public final int getCarNumber()
    {
        return ( carNumber );
    }
    
    public final String getTeamName()
    {
        return ( teamName );
    }
    
    public final String getPitGroup()
    {
        return ( pitGroup );
    }
    
    public final String getDriverName()
    {
        return ( driverName );
    }
    
    public final String getDriverDescription()
    {
        return ( driverDescription );
    }
    
    public final String getEngineName()
    {
        return ( engineName );
    }
    
    public final String getManufacturer()
    {
        return ( manufacturer );
    }
    
    public final String getClasses()
    {
        return ( classes );
    }
    
    public final String getFullTeamName()
    {
        return ( fullTeamName );
    }
    
    public final String getTeamFounded()
    {
        return ( teamFounded );
    }
    
    public final String getTeamHeadquarters()
    {
        return ( teamHeadquarters );
    }
    
    public final int getTeamStarts()
    {
        return ( teamStarts );
    }
    
    public final int getTeamPoles()
    {
        return ( teamPoles );
    }
    
    public final int getTeamWins()
    {
        return ( teamWins );
    }
    
    public final int getTeamWorldChampionships()
    {
        return ( teamWorldChampionships );
    }
    
    public final String getCategory()
    {
        return ( category );
    }
}
