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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.ModInfo;
import net.ctdp.rfdynhud.gamedata.ProfileInfo;
import net.ctdp.rfdynhud.gamedata.VehicleInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
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
    
    private final _rf1_VehicleRegistry vehicleRegistry;
    
    private File rfmFile = null;
    private File vehiclesDir = null;
    private String[] vehicleFilter = null;
    
    /**
     * Gets the mod's RFM file.
     * 
     * @return the mod's RFM file.
     */
    public final File getRFMFile()
    {
        return ( rfmFile );
    }
    
    /**
     * Gets the vehicle filter.
     * 
     * @return the vehicle filter.
     */
    public final String[] getVehicleFilter()
    {
        return ( vehicleFilter );
    }
    
    /**
     * Gets the folder, where to search for .VEH files.
     * 
     * @return the folder, where to search for .VEH files.
     */
    public final File getVehiclesFolder()
    {
        return ( vehiclesDir );
    }
    
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
        maxOpponents = 256;
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
        this.rfmFile = new File( new File( fileSystem.getGameFolder(), "rfm" ), getName() + ".rfm" );
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
     * {@inheritDoc}
     */
    @Override
    public String[] getInstalledModNames()
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
    
    /**
     * Creates a new ModInfo instance.
     * 
     * @param fileSystem
     * @param profileInfo
     */
    public _rf1_ModInfo( GameFileSystem fileSystem, ProfileInfo profileInfo )
    {
        super( profileInfo );
        
        this.fileSystem = fileSystem;
        
        this.vehicleRegistry = new _rf1_VehicleRegistry();
    }
}
