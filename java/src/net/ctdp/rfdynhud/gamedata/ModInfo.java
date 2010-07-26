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
    
    private static int readMaxOpponents( File rfmFile )
    {
        int maxOpponents = 0;
        
        BufferedReader br = null;
        
        try
        {
            br = new BufferedReader( new FileReader( rfmFile ) );
            
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                
                if ( line.toLowerCase().startsWith( "max opponents" ) )
                {
                    int p = line.indexOf( '=' );
                    
                    if ( p >= 0 )
                    {
                        String s = line.substring( p + 1 ).trim();
                        
                        p = s.indexOf( ' ' );
                        
                        if ( p > 0 )
                            s = s.substring( 0, p ).trim();
                        
                        p = s.indexOf( '\t' );
                        
                        if ( p > 0 )
                            s = s.substring( 0, p ).trim();
                        
                        try
                        {
                            int mo = Integer.parseInt( s );
                            
                            maxOpponents = Math.max( maxOpponents, mo );
                        }
                        catch ( NumberFormatException e )
                        {
                            Logger.log( e );
                        }
                    }
                }
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
        
        return ( maxOpponents );
    }
    
    void update()
    {
        this.modName = profileInfo.getModName();
        this.rfmFile = new File( new File( GameFileSystem.INSTANCE.getGameFolder(), "rfm" ), modName + ".rfm" );
        
        maxOpponents = readMaxOpponents( rfmFile );
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
    
    public ModInfo( ProfileInfo profileInfo )
    {
        this.profileInfo = profileInfo;
    }
}
