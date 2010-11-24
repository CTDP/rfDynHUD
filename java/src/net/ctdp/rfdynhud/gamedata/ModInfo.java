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

import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.ini.IniLine;
import org.jagatoo.util.io.FileUtils;

import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * Model of mod information
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ModInfo
{
    private final ProfileInfo profileInfo;
    
    private String modName = null;
    private File rfmFile = null;
    private File vehiclesDir = null;
    private String[] vehicleFilter = null;
    private int maxOpponents = -1;
    private float raceDuration = -1f;
    
    private final VehicleRegistry vehicleRegistry = new VehicleRegistry();
    
    final VehicleRegistry getVehicleRegistry()
    {
        return ( vehicleRegistry );
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
        
        String[] vehicleFilter = null;
        
        String value = parseValuePart( line, 14 );
        
        if ( value != null )
        {
            value = value.toLowerCase();
            
            if ( value.startsWith( "or:" ) )
                value = value.substring( 3 ).trim();
            
            vehicleFilter = value.split( " " );
            
            for ( int i = 0; i < vehicleFilter.length; i++ )
            {
                vehicleFilter[i] = vehicleFilter[i].trim();
            }
        }
        
        if ( vehicleFilter != null )
        {
            int n = 0;
            for ( int i = 0; i < vehicleFilter.length; i++ )
            {
                if ( !vehicleFilter[i].equals( "" ) )
                    n++;
            }
            
            if ( n == 0 )
            {
                vehicleFilter = null;
            }
            else if ( n < vehicleFilter.length )
            {
                String[] tmp = new String[ n ];
                int j = 0;
                for ( int i = 0; i < vehicleFilter.length; i++ )
                {
                    if ( !vehicleFilter[i].equals( "" ) )
                        tmp[j++] = vehicleFilter[i];
                }
                
                vehicleFilter = tmp;
            }
        }
        
        return ( vehicleFilter );
    }
    
    private static File parseVehiclesDir( String line )
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
                    vehiclesDir = new File( GameFileSystem.INSTANCE.getGameFolder(), value );
                
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
        maxOpponents = 0;
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
                    vehiclesDir = parseVehiclesDir( line );
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
    
    void update()
    {
        this.modName = profileInfo.getModName();
        this.rfmFile = new File( new File( GameFileSystem.INSTANCE.getGameFolder(), "rfm" ), modName + ".rfm" );
        this.vehiclesDir = new File( new File( GameFileSystem.INSTANCE.getGameFolder(), "GameData" ), "Vehicles" );
        this.vehicleFilter = null;
        
        parseRFM( rfmFile );
        
        if ( vehiclesDir != null )
            vehicleRegistry.update( vehicleFilter, vehiclesDir );
    }
    
    /**
     * Gets the current mod's name.
     * 
     * @return the current mod's name.
     */
    public final String getName()
    {
        return ( modName );
    }
    
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
     * Gets the 'max opponents' setting from the mod's RFM.
     * 
     * @return the 'max opponents' setting from the mod's RFM.
     */
    public final int getMaxOpponents()
    {
        return ( maxOpponents );
    }
    
    /**
     * Gets the race duration in seconds.
     * 
     * @return the race duration in seconds.
     */
    public final float getRaceDuration()
    {
        return ( raceDuration );
    }
    
    /**
     * Creates a new ModInfo instance.
     * 
     * @param profileInfo
     */
    public ModInfo( ProfileInfo profileInfo )
    {
        this.profileInfo = profileInfo;
    }
}
