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
    
    ModInfo( ProfileInfo profileInfo )
    {
        this.profileInfo = profileInfo;
    }
}
