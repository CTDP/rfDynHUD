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

import net.ctdp.rfdynhud.util.Logger;

public class ModInfo
{
    private final ProfileInfo profileInfo;
    
    private String modName = null;
    private File rfmFile = null;
    private int maxOpponents = -1;
    private float raceDuration = -1f;
    
    private static String parseValuePart( String line, int keyLength )
    {
        int p = line.indexOf( '=', keyLength );
        
        if ( p >= 0 )
        {
            String s = line.substring( p + 1 ).trim();
            
            p = s.indexOf( ' ' );
            
            if ( p > 0 )
                s = s.substring( 0, p ).trim();
            
            p = s.indexOf( '\t' );
            
            if ( p > 0 )
                s = s.substring( 0, p ).trim();
            
            return ( s );
        }
        
        return ( null );
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
                Logger.log( e );
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
                Logger.log( e );
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
                
                if ( lLine.startsWith( "max opponents" ) )
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
            Logger.log( e );
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
        
        parseRFM( rfmFile );
    }
    
    public final String getName()
    {
        return ( modName );
    }
    
    public final File getRFMFile()
    {
        return ( rfmFile );
    }
    
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
    
    public ModInfo( ProfileInfo profileInfo )
    {
        this.profileInfo = profileInfo;
    }
}
