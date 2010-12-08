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

import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Parses {@link VehicleInfo} from a .VEH file.
 * 
 * @author Marvin Froehlich (CTDP)
 */
class VehicleInfoParser extends AbstractIniParser
{
    private final String filename;
    private final VehicleInfo info;
    
    @Override
    protected boolean acceptMissingTrailingQuote()
    {
        return ( true );
    }
    
    private static final Throwable getRootCause( Throwable t )
    {
        if ( t.getCause() == null )
            return ( t );
        
        return ( getRootCause( t.getCause() ) );
    }
    
    @Override
    protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
    {
        try
        {
            if ( group == null )
            {
                if ( key.equalsIgnoreCase( "Number" ) )
                {
                    info.carNumber = Integer.parseInt( value );
                }
                else if ( key.equalsIgnoreCase( "Team" ) )
                {
                    info.teamName = value;
                }
                else if ( key.equalsIgnoreCase( "PitGroup" ) )
                {
                    info.pitGroup = value;
                }
                else if ( key.equalsIgnoreCase( "Driver" ) )
                {
                    info.driverName = value;
                }
                else if ( key.equalsIgnoreCase( "Description" ) )
                {
                    info.driverDescription = value;
                }
                else if ( key.equalsIgnoreCase( "Engine" ) )
                {
                    info.engineName = value;
                }
                else if ( key.equalsIgnoreCase( "Manufacturer" ) )
                {
                    info.manufacturer = value;
                }
                else if ( key.equalsIgnoreCase( "Classes" ) )
                {
                    info.classes = value;
                }
                else if ( key.equalsIgnoreCase( "FullTeamName" ) )
                {
                    info.fullTeamName = value;
                }
                else if ( key.equalsIgnoreCase( "TeamFounded" ) )
                {
                    info.teamFounded = value;
                }
                else if ( key.equalsIgnoreCase( "TeamHeadquarters" ) )
                {
                    info.teamHeadquarters = value;
                }
                else if ( key.equalsIgnoreCase( "TeamStarts" ) )
                {
                    if ( value.length() > 0 )
                        info.teamStarts = Integer.parseInt( value );
                }
                else if ( key.equalsIgnoreCase( "TeamPoles" ) )
                {
                    if ( value.length() > 0 )
                        info.teamPoles = Integer.parseInt( value );
                }
                else if ( key.equalsIgnoreCase( "TeamWins" ) )
                {
                    if ( value.length() > 0 )
                        info.teamWins = Integer.parseInt( value );
                }
                else if ( key.equalsIgnoreCase( "TeamWorldChampionships" ) )
                {
                    if ( value.length() > 0 )
                        info.teamWorldChampionships = Integer.parseInt( value );
                }
                else if ( key.equalsIgnoreCase( "Category" ) )
                {
                    info.category = value;
                }
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( "WARNING: Parsing exception in VEH file \"" + filename + "\" in line #" + lineNr + "(" + getRootCause( t ).getClass().getSimpleName() + "). Message: " + t.getMessage() );
        }
        
        return ( true );
    }
    
    @Override
    protected boolean handleParsingException( int lineNr, String group, String line, Throwable t ) throws ParsingException
    {
        RFDHLog.exception( "Warning: Unable to parse the line #" + lineNr + " from engine physics \"" + filename + "\"." );
        RFDHLog.exception( "Line was \"" + line + "\". Exception follows." );
        RFDHLog.exception( t );
        
        return ( true );
    }
    
    public VehicleInfoParser( String filename, VehicleInfo info )
    {
        this.filename = filename;
        this.info = info;
    }
}
