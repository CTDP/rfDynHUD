package net.ctdp.rfdynhud.gamedata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.ResourceManager;

abstract class GameFileSystemInitializer
{
    private static File readDevGameFolder()
    {
        try
        {
            BufferedReader br = new BufferedReader( new FileReader( new File( "game_folder.txt" ) ) );
            String line = br.readLine();
            br.close();
            
            return ( new File( line ).getAbsoluteFile() );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( null );
        }
    }
    
    protected abstract File findGameFolderImpl( File pluginFolder );
    
    public final File findGameFolder( File pluginFolder )
    {
        if ( !ResourceManager.isJarMode() )
            return ( readDevGameFolder() );
        
        return ( findGameFolderImpl( pluginFolder ) );
    }
    
    protected abstract File getPathFromGameConfigINIImpl( File gameFolder, String setting, String fallback );
    
    public final File getPathFromGameConfigINI( File gameFolder, String setting, String fallback )
    {
        return ( getPathFromGameConfigINIImpl( gameFolder, setting, fallback ) );
    }
    
    protected abstract File findGameScreenshotsFolderImpl( File gameFolder );
    
    public final File findGameScreenshotsFolder( File gameFolder )
    {
        return ( findGameScreenshotsFolderImpl( gameFolder ) );
    }
    
    
    public GameFileSystemInitializer()
    {
    }
    
    private static GameFileSystemInitializer instance = null;
    
    public static GameFileSystemInitializer getInstance()
    {
        if ( instance == null )
        {
            // TODO: Decide somehow, which game is being used.
            
            instance = new GameFileSystemInitializerRFactor();
        }
        
        return ( instance );
    }
}
