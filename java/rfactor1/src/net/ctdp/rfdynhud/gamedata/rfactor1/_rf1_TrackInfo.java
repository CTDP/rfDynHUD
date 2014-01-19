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

import net.ctdp.rfdynhud.gamedata.Track;
import net.ctdp.rfdynhud.gamedata.TrackInfo;
import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.strings.StringUtils;

/**
 * Model of the currently used track
 * 
 * @author Marvin Froehlich
 */
class _rf1_TrackInfo extends TrackInfo
{
    private final _rf1_ProfileInfo profileInfo;
    
    private File trackFolder = null;
    private String trackName = null;
    private int raceLaps = -1;
    private File aiwFile = null;
    private Track lastTrack = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid()
    {
        if ( getTrackFolder() == null )
            return ( false );
        
        if ( getSceneFile() == null )
            return ( false );
        
        if ( !getSceneFile().exists() )
            return ( false );
        
        return ( true );
    }
    
    private static Object[] checkGDB( File gdb, String trackname )
    {
        Object[] result = new Object[] { "N/A", -1 };
        
        BufferedReader br = null;
        
        try
        {
            boolean trackFound = false;
            boolean raceLapsFound = false;
            
            br = new BufferedReader( new FileReader( gdb ) );
            
            String line = null;
            
            while ( ( line = br.readLine() ) != null )
            {
                int offset = StringUtils.findFirstNonWhitespace( line );
                
                if ( offset < 0 )
                    continue;
                
                if ( StringUtils.startsWithIgnoreCase( line, "TrackName", offset ) )
                {
                    int idx = line.indexOf( '=', offset + 9 );
                    if ( idx >= 0 )
                    {
                        String tn = line.substring( idx + 1 ).trim();
                        
                        if ( ( trackname == null ) || tn.equals( trackname ) )
                        {
                            result[0] = tn;
                            trackFound = true;
                        }
                    }
                }
                else if ( StringUtils.startsWithIgnoreCase( line, "RaceLaps", offset ) )
                {
                    int idx = line.indexOf( '=', offset + 8 );
                    if ( idx >= 0 )
                    {
                        try
                        {
                            result[1] = Integer.parseInt( line.substring( idx + 1 ).trim() );
                            raceLapsFound = true;
                        }
                        catch ( Throwable t )
                        {
                        }
                    }
                }
                
                if ( trackFound && raceLapsFound )
                    return ( result );
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        finally
        {
            if ( br != null )
                try { br.close(); } catch ( Throwable t ) {}
        }
        
        return ( result );
    }
    
    /*
    private static Object[] searchTrackFolder( File parentDir, String trackname )
    {
        for ( File f : parentDir.listFiles() )
        {
            if ( f.isDirectory() )
            {
                Object[] result = searchTrackFolder( f, trackname );
                if ( result != null )
                    return ( result );
            }
            else if ( f.getName().toLowerCase().endsWith( ".gdb" ) )
            {
                Object[] result = checkGDB( f, trackname );
                if ( result != null )
                    return ( new Object[] { f.getParentFile(), (Integer)result[1] } );
            }
        }
        
        return ( null );
    }
    */
    
    private void readTrackHeader( File trackFolder, String trackname )
    {
        if ( ( trackFolder == null ) || !trackFolder.exists() )
        {
            RFDHLog.exception( "WARNING: Track folder not found: " + trackFolder );
            
            this.trackName = "N/A";
            this.raceLaps = -1;
            
            return;
        }
        
        for ( File f : trackFolder.listFiles() )
        {
            if ( f.isFile() && f.getName().toLowerCase().endsWith( ".gdb" ) )
            {
                Object[] result = checkGDB( f, trackname );
                if ( result != null )
                {
                    this.trackName = (String)result[0];
                    this.raceLaps = (Integer)result[1];
                    return;
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset()
    {
        super.reset();
        
        this.trackFolder = null;
        this.trackName = null;
        this.raceLaps = -1;
        this.aiwFile = null;
        this.lastTrack = null;
    }
    
    /**
     * <p>
     * Finds the folder from the GameData\Locations folder, in which a .gdb file
     * exists, that contains a line<br>
     *   TrackName = trackname
     * </p>
     * WARNING:<br>
     * This operation may take a long time.
     */
    @Override
    protected void updateImpl()
    {
        File sceneFile = profileInfo.getLastUsedSceneFile();
        
        if ( sceneFile == null )
        {
            reset();
            return;
        }
        
        File trackFolder = sceneFile.getParentFile();
        
        if ( trackFolder.equals( this.trackFolder ) )
            return;
        
        this.trackFolder = trackFolder;
        
        this.trackName = null;
        this.raceLaps = -1;
        
        this.aiwFile = null;
        this.lastTrack = null;
    }
    
    /**
     * Gets the track's folder.
     * 
     * @return the track's folder.
     */
    public final File getTrackFolder()
    {
        return ( trackFolder );
    }
    
    /**
     * Gets the track's scene file.
     * 
     * @return the track's scene file.
     */
    public final File getSceneFile()
    {
        return ( profileInfo.getLastUsedSceneFile() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getTrackName()
    {
        if ( trackName == null )
        {
            readTrackHeader( getTrackFolder(), null );
        }
        
        return ( trackName );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getRaceLaps()
    {
        if ( raceLaps < 0 )
        {
            readTrackHeader( getTrackFolder(), null );
        }
        
        return ( raceLaps );
    }
    
    /**
     * <p>
     * Finds the AIW file for the given track.
     * </p>
     * WARNING:<br>
     * This operation may take a long time.
     * 
     * @param trackFolder
     * 
     * @return the AIW file for the given track.
     */
    private static File findAIWFile( File trackFolder )
    {
        if ( trackFolder == null )
            return ( null );
        
        File aiw = null;
        for ( File f : trackFolder.listFiles() )
        {
            if ( !f.isDirectory() && f.getName().toUpperCase().endsWith( ".AIW" ) )
            {
                aiw = f;
                break;
            }
        }
        
        return ( aiw );
    }
    
    /**
     * <p>
     * Finds the AIW file for the given track.
     * </p>
     * WARNING:<br>
     * This operation may take a long time.
     * 
     * @return the AIW file for the given track.
     */
    public File getAIWFile()
    {
        if ( aiwFile == null )
        {
            aiwFile = findAIWFile( getTrackFolder() );
        }
        
        return ( aiwFile );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final Track getTrack()
    {
        if ( lastTrack == null )
        {
            File aiw = getAIWFile();
            
            if ( aiw != null )
            {
                this.aiwFile = aiw;
                try
                {
                    this.lastTrack = _rf1_AIWParser.parseTrackFromAIW( aiw );
                }
                catch ( IOException e )
                {
                    RFDHLog.exception( e );
                    this.lastTrack = null;
                }
            }
        }
        
        return ( lastTrack );
    }
    
    /**
     * Create a new instance.
     * 
     * @param profileInfo
     */
    public _rf1_TrackInfo( _rf1_ProfileInfo profileInfo )
    {
        super( profileInfo );
        
        this.profileInfo = profileInfo;
    }
}
