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
package net.ctdp.rfdynhud.gamedata.rfactor1;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.ctdp.rfdynhud.gamedata.VehicleInfo;
import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Parses {@link VehicleInfo} from a .VEH file.
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf1_VehicleInfoParser
{
    private final String filename;
    private final _rf1_VehicleInfo info;
    
    private static final Throwable getRootCause( Throwable t )
    {
        if ( t.getCause() == null )
            return ( t );
        
        return ( getRootCause( t.getCause() ) );
    }
    
    private class _ParserImpl extends AbstractIniParser
    {
        @Override
        protected boolean acceptMissingTrailingQuote()
        {
            return ( true );
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
                        info.setCarNumber( Integer.parseInt( value ) );
                    }
                    else if ( key.equalsIgnoreCase( "Team" ) )
                    {
                        info.setTeamName( value );
                    }
                    else if ( key.equalsIgnoreCase( "PitGroup" ) )
                    {
                        info.setPitGroup( value );
                    }
                    else if ( key.equalsIgnoreCase( "Driver" ) )
                    {
                        info.setDriverName( value );
                    }
                    else if ( key.equalsIgnoreCase( "Description" ) )
                    {
                        info.setDriverDescription( value );
                    }
                    else if ( key.equalsIgnoreCase( "Engine" ) )
                    {
                        info.setEngineName( value );
                    }
                    else if ( key.equalsIgnoreCase( "Manufacturer" ) )
                    {
                        info.setManufacturer( value );
                    }
                    else if ( key.equalsIgnoreCase( "Classes" ) )
                    {
                        info.setClasses( value );
                    }
                    else if ( key.equalsIgnoreCase( "FullTeamName" ) )
                    {
                        info.setFullTeamName( value );
                    }
                    else if ( key.equalsIgnoreCase( "TeamFounded" ) )
                    {
                        info.setTeamFounded( value );
                    }
                    else if ( key.equalsIgnoreCase( "TeamHeadquarters" ) )
                    {
                        info.setTeamHeadquarters( value );
                    }
                    else if ( key.equalsIgnoreCase( "TeamStarts" ) )
                    {
                        if ( value.length() > 0 )
                            info.setTeamStarts( value );
                    }
                    else if ( key.equalsIgnoreCase( "TeamPoles" ) )
                    {
                        if ( value.length() > 0 )
                            info.setTeamPoles( value );
                    }
                    else if ( key.equalsIgnoreCase( "TeamWins" ) )
                    {
                        if ( value.length() > 0 )
                            info.setTeamWins( value );
                    }
                    else if ( key.equalsIgnoreCase( "TeamWorldChampionships" ) )
                    {
                        if ( value.length() > 0 )
                            info.setTeamWorldChampionships( value );
                    }
                    else if ( key.equalsIgnoreCase( "Category" ) )
                    {
                        info.setCategory( value );
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
    }
    
    /**
     * Parses the given file.
     * 
     * @param file
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public void parse( File file ) throws IOException, ParsingException
    {
        new _ParserImpl().parse( file );
    }
    
    /**
     * Parses the given file.
     * 
     * @param url
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public void parse( URL url ) throws IOException, ParsingException
    {
        new _ParserImpl().parse( url );
    }
    
    public _rf1_VehicleInfoParser( String filename, _rf1_VehicleInfo info )
    {
        this.filename = filename;
        this.info = info;
    }
}
