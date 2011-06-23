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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.ini.IniLine;
import org.jagatoo.util.io.FileUtils;

/**
 * Model of mod information
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class _rf1_ModInfo extends ModInfo
{
    //private final _LiveGameDataObjectsFactory gdFactory;
    private final GameFileSystem fileSystem;
    private final ProfileInfo profileInfo;
    
    private final _rf1_VehicleRegistry vehicleRegistry;
    
    /**
     * 
     * @param line
     * @param keyLength
     * @return
     */
    private static String parseValuePart( String line, int keyLength )
    {
        IniLine iniLine = new IniLine();
        try
        {
            if ( !AbstractIniParser.parseLine( 0, null, line, "=", iniLine, null ) )
                return ( null );
            
            return ( iniLine.getValue() );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
            return ( null );
        }
    }
    
    private static String[] parseVehicleFilter( String line )
    {
        // 'line' is trimmed and starts with "Vehicle Filter".
        
        ArrayList<String> vehicleFilter = new ArrayList<String>();
        
        String value = parseValuePart( line, 14 );
        
        if ( value != null )
        {
            value = value.toLowerCase();
            
            if ( value.startsWith( "or:" ) )
                value = value.substring( 3 ).trim();
            
            String[] vf = value.split( " " );
            
            for ( int i = 0; i < vf.length; i++ )
            {
                String vf2 = vf[i].trim();
                
                if ( !vf2.equals( "" ) )
                {
                    if ( vf2.indexOf( ',' ) >= 0 )
                    {
                        String[] vf3 = vf2.split( "," );
                        for ( int j = 0; j < vf3.length; j++ )
                        {
                            vf3[j] = vf3[j].trim();
                            
                            if ( !vf3[j].equals( "" ) )
                                vehicleFilter.add( vf3[j] );
                        }
                    }
                    else
                    {
                        vehicleFilter.add( vf2 );
                    }
                }
            }
        }
        
        return ( vehicleFilter.toArray( new String[ vehicleFilter.size() ] ) );
    }
    
    private static File parseVehiclesDir( GameFileSystem fileSystem, String line )
    {
        // 'line' is trimmed and starts with "VehiclesDir".
        
        File vehiclesDir = null;
        
        String value = parseValuePart( line, 11 );
        
        if ( value != null )
        {
            try
            {
                vehiclesDir = new File( value );
                if ( !vehiclesDir.isAbsolute() )
                    vehiclesDir = new File( fileSystem.getGameFolder(), value );
                
                if ( vehiclesDir.exists() )
                    vehiclesDir = FileUtils.getCanonicalFile( vehiclesDir );
            }
            catch ( Throwable t )
            {
                RFDHLog.exception( t );
            }
        }
        
        return ( vehiclesDir );
    }
    
    private static int parseMaxOpponents( String line )
    {
        // 'line' is trimmed and starts with "Max Opponents".
        
        int maxOpponents = 0;
        
        String value = parseValuePart( line, 13 );
        
        if ( value != null )
        {
            try
            {
                int mo = Integer.parseInt( value );
                
                maxOpponents = Math.max( maxOpponents, mo );
            }
            catch ( NumberFormatException e )
            {
                RFDHLog.exception( e );
            }
        }
        
        return ( maxOpponents );
    }
    
    private static float parseRaceTime( String line )
    {
        // 'line' is trimmed and starts with "RaceTime".
        
        float raceTime = -1f;
        
        String value = parseValuePart( line, 8 );
        
        if ( value != null )
        {
            try
            {
                float rt = Float.parseFloat( value );
                
                raceTime = Math.max( raceTime, rt );
            }
            catch ( NumberFormatException e )
            {
                RFDHLog.exception( e );
            }
        }
        
        return ( raceTime );
    }
    
    private void parseRFM( File rfmFile )
    {
        maxOpponents = Integer.MAX_VALUE;
        raceDuration = -1f;
        
        BufferedReader br = null;
        
        try
        {
            br = new BufferedReader( new FileReader( rfmFile ) );
            
            String lastLine = null;
            String line;
            String[] groupStack = new String[ 16 ]; // should be enough
            int level = 0;
            while ( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                
                if ( line.length() == 0 )
                    continue;
                
                if ( line.startsWith( "//" ) || line.startsWith( "#" ) )
                    continue;
                
                int p = line.indexOf( '{' );
                if ( p >= 0 )
                {
                    groupStack[level++] = lastLine;
                    p++;
                }
                p = Math.max( 0, p );
                int p2 = line.indexOf( '}', p );
                if ( p2 >= 0 )
                {
                    groupStack[--level] = null;
                    p = p2 + 1;
                }
                p = Math.max( 0, p );
                p2 = line.indexOf( '{', p );
                if ( p2 >= 0 )
                {
                    groupStack[level++] = lastLine;
                    p = p2 + 1;
                }
                
                String lLine = line.toLowerCase();
                
                if ( ( level == 0 ) && lLine.startsWith( "vehicle filter" ) )
                {
                    vehicleFilter = parseVehicleFilter( line );
                }
                else if ( ( level == 1 ) && groupStack[0].equalsIgnoreCase( "ConfigOverrides" ) && lLine.startsWith( "vehiclesdir" ) )
                {
                    vehiclesDir = parseVehiclesDir( fileSystem, line );
                }
                else if ( lLine.startsWith( "max opponents" ) )
                {
                    maxOpponents = parseMaxOpponents( line );
                }
                else if ( lLine.startsWith( "racetime" ) && ( level == 1 ) && "DefaultScoring".equalsIgnoreCase( groupStack[0] ) )
                {
                    raceDuration = parseRaceTime( line ) * 60f;
                }
                
                lastLine = line;
            }
        }
        catch ( IOException e )
        {
            RFDHLog.exception( e );
        }
        finally
        {
            if ( br != null )
                try { br.close(); } catch ( IOException e ) {}
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateImpl()
    {
        this.modName = profileInfo.getModName();
        this.rfmFile = new File( new File( fileSystem.getGameFolder(), "rfm" ), modName + ".rfm" );
        this.vehiclesDir = new File( new File( fileSystem.getGameFolder(), "GameData" ), "Vehicles" );
        this.vehicleFilter = null;
        
        parseRFM( rfmFile );
        
        if ( vehiclesDir != null )
            vehicleRegistry.update( vehicleFilter, vehiclesDir );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VehicleInfo getVehicleInfoForDriver( VehicleScoringInfo vsi )
    {
        return ( vehicleRegistry.getVehicleForDriver( vsi.getVehicleName() ) );
    }
    
    /**
     * Creates a new ModInfo instance.
     * 
     * @param fileSystem
     * @param profileInfo
     * @param gdFactory
     */
    public _rf1_ModInfo( GameFileSystem fileSystem, ProfileInfo profileInfo, _LiveGameDataObjectsFactory gdFactory )
    {
        super();
        
        this.fileSystem = fileSystem;
        this.profileInfo = profileInfo;
        //this.gdFactory = gdFactory;
        
        this.vehicleRegistry = new _rf1_VehicleRegistry( gdFactory );
    }
    
    /**
     * Gets the RFM filenames of all installed mods.
     * 
     * @param fileSystem
     * 
     * @return the RFM filenames of all installed mods.
     */
    public static String[] getInstalledModNames( GameFileSystem fileSystem )
    {
        File[] rfms = new File( fileSystem.getGameFolder(), "rfm" ).listFiles();
        
        if ( rfms == null )
            return ( null );
        
        ArrayList<String> names = new ArrayList<String>();
        
        for ( File rfm : rfms )
        {
            String name = rfm.getName();
            if ( rfm.isFile() && name.toLowerCase().endsWith( ".rfm" ) )
            {
                if ( name.length() == 4 )
                    names.add( "" );
                else
                    names.add( name.substring( 0, name.length() - 4 ) );
            }
        }
        
        return ( names.toArray( new String[ names.size() ] ) );
    }
}
