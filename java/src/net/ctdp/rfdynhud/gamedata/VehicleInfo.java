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
    private String teamName = null;
    private String teamNameCleaned = null;
    String pitGroup = null;
    String driverName = null;
    String driverDescription = null;
    String engineName = null;
    String manufacturer = null;
    String classes = null;
    
    String fullTeamName = null;
    String teamFounded = null;
    String teamHeadquarters = null;
    String teamStarts = "N/A";
    String teamPoles = "N/A";
    String teamWins = "N/A";
    String teamWorldChampionships = "N/A";
    
    String category = null;
    
    void reset()
    {
        carNumber = -1;
        teamName = null;
        teamNameCleaned = null;
        pitGroup = null;
        driverName = null;
        driverDescription = null;
        engineName = null;
        manufacturer = null;
        classes = null;
        
        fullTeamName = null;
        teamFounded = null;
        teamHeadquarters = null;
        teamStarts = "N/A";
        teamPoles = "N/A";
        teamWins = "N/A";
        teamWorldChampionships = "N/A";
        
        category = null;
    }
    
    public final int getCarNumber()
    {
        return ( carNumber );
    }
    
    void setTeamName( String teamName )
    {
        this.teamName = teamName;
        
        int p0 = 0;
        char ch = this.teamName.charAt( p0 );
        while ( ( p0 < this.teamName.length() ) && Character.isWhitespace( ch ) || ( ch >= '0' && ch <= '9' ) )
            ch = this.teamName.charAt( ++p0 );
        
        if ( p0 == teamName.length() )
            this.teamNameCleaned = this.teamName;
        else
            this.teamNameCleaned = this.teamName.substring( p0 );
    }
    
    public final String getTeamName()
    {
        return ( teamName );
    }
    
    /**
     * Since &quot;TeamName&quot; in the .VEH file is used for sorting, we trim a numeric prefix from the name here.
     * 
     * @return the cleaned team name.
     */
    public final String getTeamNameCleaned()
    {
        return ( teamNameCleaned );
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
    
    public final String getTeamStarts()
    {
        return ( teamStarts );
    }
    
    public final String getTeamPoles()
    {
        return ( teamPoles );
    }
    
    public final String getTeamWins()
    {
        return ( teamWins );
    }
    
    public final String getTeamWorldChampionships()
    {
        return ( teamWorldChampionships );
    }
    
    public final String getCategory()
    {
        return ( category );
    }
}
