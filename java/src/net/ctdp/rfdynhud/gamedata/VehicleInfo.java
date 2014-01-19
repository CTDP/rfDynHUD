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
package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;

/**
 * Keeps all descriptive information read from the .VEH file.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class VehicleInfo
{
    private int carNumber = -1;
    private String teamName = "N/A";//null;
    private String teamNameCleaned = "N/A";//null;
    private String pitGroup = "N/A";//null;
    private String driverName = "N/A";//null;
    private String driverDescription = "N/A";//null;
    private String engineName = "N/A";//null;
    private String manufacturer = "N/A";//null;
    private String classes = "N/A";//null;
    
    private String fullTeamName = "N/A";//null;
    private String teamFounded = "N/A";//null;
    private String teamHeadquarters = "N/A";//null;
    private String teamStarts = "N/A";
    private String teamPoles = "N/A";
    private String teamWins = "N/A";
    private String teamWorldChampionships = "N/A";
    
    private String category = "N/A";//null;
    
    protected void reset()
    {
        carNumber = -1;
        teamName = "N/A";//null;
        teamNameCleaned = "N/A";//null;
        pitGroup = "N/A";//null;
        driverName = "N/A";//null;
        driverDescription = "N/A";//null;
        engineName = "N/A";//null;
        manufacturer = "N/A";//null;
        classes = "N/A";//null;
        
        fullTeamName = "N/A";//null;
        teamFounded = "N/A";//null;
        teamHeadquarters = "N/A";//null;
        teamStarts = "N/A";
        teamPoles = "N/A";
        teamWins = "N/A";
        teamWorldChampionships = "N/A";
        
        category = "N/A";//null;
    }
    
    protected abstract void reload( LiveGameData gameData ) throws IOException;
    
    protected void setCarNumber( int carNumber )
    {
        this.carNumber = carNumber;
    }
    
    public final int getCarNumber()
    {
        return ( carNumber );
    }
    
    protected void setTeamName( String teamName )
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
    
    protected void setPitGroup( String pitGroup )
    {
        this.pitGroup = pitGroup;
    }
    
    public final String getPitGroup()
    {
        return ( pitGroup );
    }
    
    protected void setDriverName( String driverName )
    {
        this.driverName = driverName;
    }
    
    public final String getDriverName()
    {
        return ( driverName );
    }
    
    protected void setDriverDescription( String driverDescription )
    {
        this.driverDescription = driverDescription;
    }
    
    public final String getDriverDescription()
    {
        return ( driverDescription );
    }
    
    protected void setEngineName( String engineName )
    {
        this.engineName = engineName;
    }
    
    public final String getEngineName()
    {
        return ( engineName );
    }
    
    protected void setManufacturer( String manufacturer )
    {
        this.manufacturer = manufacturer;
    }
    
    public final String getManufacturer()
    {
        return ( manufacturer );
    }
    
    protected void setClasses( String classes )
    {
        this.classes = classes;
    }
    
    public final String getClasses()
    {
        return ( classes );
    }
    
    protected void setFullTeamName( String fullTeamName )
    {
        this.fullTeamName = fullTeamName;
    }
    
    public final String getFullTeamName()
    {
        return ( fullTeamName );
    }
    
    protected void setTeamFounded( String teamFounded )
    {
        this.teamFounded = teamFounded;
    }
    
    public final String getTeamFounded()
    {
        return ( teamFounded );
    }
    
    protected void setTeamHeadquarters( String teamHeadquarters )
    {
        this.teamHeadquarters = teamHeadquarters;
    }
    
    public final String getTeamHeadquarters()
    {
        return ( teamHeadquarters );
    }
    
    protected void setTeamStarts( String teamStarts )
    {
        this.teamStarts = teamStarts;
    }
    
    public final String getTeamStarts()
    {
        return ( teamStarts );
    }
    
    protected void setTeamPoles( String teamPoles )
    {
        this.teamPoles = teamPoles;
    }
    
    public final String getTeamPoles()
    {
        return ( teamPoles );
    }
    
    protected void setTeamWins( String teamWins )
    {
        this.teamWins = teamWins;
    }
    
    public final String getTeamWins()
    {
        return ( teamWins );
    }
    
    protected void setTeamWorldChampionships( String teamWorldChampionships )
    {
        this.teamWorldChampionships = teamWorldChampionships;
    }
    
    public final String getTeamWorldChampionships()
    {
        return ( teamWorldChampionships );
    }
    
    protected void setCategory( String category )
    {
        this.category = category;
    }
    
    public final String getCategory()
    {
        return ( category );
    }
}
