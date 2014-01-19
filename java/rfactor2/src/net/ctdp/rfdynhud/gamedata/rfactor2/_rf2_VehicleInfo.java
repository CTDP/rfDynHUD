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
package net.ctdp.rfdynhud.gamedata.rfactor2;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.VehicleInfo;

class _rf2_VehicleInfo extends VehicleInfo
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reload( LiveGameData gameData ) throws IOException
    {
        /*
        reset();
        
        java.io.File playerVEHFile = ( (_rf2_ProfileInfo)gameData.getProfileInfo() ).getVehicleFile();
        
        if ( ( playerVEHFile != null ) && playerVEHFile.exists() )
        {
            net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess.getGameDataObjectsFactory( gameData ).parseVehicleInfo( playerVEHFile, playerVEHFile.getAbsolutePath(), gameData.getVehicleInfo() );
        }
        */
    }
    
    @Override
    protected void setCarNumber( int carNumber )
    {
        super.setCarNumber( carNumber );
    }
    
    @Override
    protected void setTeamName( String teamName )
    {
        super.setTeamName( teamName );
    }
    
    @Override
    protected void setPitGroup( String pitGroup )
    {
        super.setPitGroup( pitGroup );
    }
    
    @Override
    protected void setDriverName( String driverName )
    {
        super.setDriverName( driverName );
    }
    
    @Override
    protected void setDriverDescription( String driverDescription )
    {
        super.setDriverDescription( driverDescription );
    }
    
    @Override
    protected void setEngineName( String engineName )
    {
        super.setEngineName( engineName );
    }
    
    @Override
    protected void setManufacturer( String manufacturer )
    {
        super.setManufacturer( manufacturer );
    }
    
    @Override
    protected void setClasses( String classes )
    {
        super.setClasses( classes );
    }
    
    @Override
    protected void setFullTeamName( String fullTeamName )
    {
        super.setFullTeamName( fullTeamName );
    }
    
    @Override
    protected void setTeamFounded( String teamFounded )
    {
        super.setTeamFounded( teamFounded );
    }
    
    @Override
    protected void setTeamHeadquarters( String teamHeadquarters )
    {
        super.setTeamHeadquarters( teamHeadquarters );
    }
    
    @Override
    protected void setTeamStarts( String teamStarts )
    {
        super.setTeamStarts( teamStarts );
    }
    
    @Override
    protected void setTeamPoles( String teamPoles )
    {
        super.setTeamPoles( teamPoles );
    }
    
    @Override
    protected void setTeamWins( String teamWins )
    {
        super.setTeamWins( teamWins );
    }
    
    @Override
    protected void setTeamWorldChampionships( String teamWorldChampionships )
    {
        super.setTeamWorldChampionships( teamWorldChampionships );
    }
    
    @Override
    protected void setCategory( String category )
    {
        super.setCategory( category );
    }
}
