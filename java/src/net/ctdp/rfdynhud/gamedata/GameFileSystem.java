package net.ctdp.rfdynhud.gamedata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.PluginINI;
import net.ctdp.rfdynhud.util.ResourceManager;
import net.ctdp.rfdynhud.util.__UtilHelper;

public abstract class GameFileSystem
{
    private final File pluginFolder = __UtilHelper.PLUGIN_FOLDER;
    private final String pluginPath = pluginFolder.getAbsolutePath();
    private final PluginINI pluginINI = new PluginINI( pluginFolder );
    private final File gameFolder;
    private final String gamePath;
    private final File configFolder;
    private final String configPath;
    private final File cacheFolder;
    private final String cachePath;
    private final File bordersFolder;
    private final String bordersPath;
    private final File imagesFolder;
    private final String imagesPath;
    private final File editorFolder;
    private final String editorPath;
    private final File gameScreenshotsFolder;
    
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
    
    protected abstract File findGameFolder( File pluginFolder );
    
    /**
     * 
     * @param pluginFolder
     * @param pluginINI
     * @return
     */
    protected File findConfigFolder( File pluginFolder, PluginINI pluginINI )
    {
        return ( pluginINI.getGeneralConfigFolder() );
    }
    
    /**
     * 
     * @param pluginFolder
     * @param pluginINI
     * @return
     */
    protected File findCacheFolder( File pluginFolder, PluginINI pluginINI )
    {
        return ( pluginINI.getGeneralCacheFolder() );
    }
    
    /**
     * 
     * @param pluginFolder
     * @param pluginINI
     * @param configFolder
     * @return
     */
    protected File findBordersFolder( File pluginFolder, PluginINI pluginINI, File configFolder )
    {
        return ( new File( new File( configFolder, "data" ), "borders" ).getAbsoluteFile() );
    }
    
    /**
     * 
     * @param pluginFolder
     * @param pluginINI
     * @param configFolder
     * @return
     */
    protected File findImagesFolder( File pluginFolder, PluginINI pluginINI, File configFolder )
    {
        return ( new File( new File( configFolder, "data" ), "images" ).getAbsoluteFile() );
    }
    
    /**
     * 
     * @param pluginFolder
     * @param pluginINI
     * @return
     */
    protected File findEditorFolder( File pluginFolder, PluginINI pluginINI )
    {
        if ( ResourceManager.isJarMode() )
            return ( new File( pluginFolder, "editor" ).getAbsoluteFile() );
        
        return ( new File( __UtilHelper.stripDotDots( new File( "." ).getAbsolutePath() ), "data" ).getAbsoluteFile() );
    }
    
    public abstract File getPathFromGameConfigINI( String setting, String fallback );
    
    protected abstract File findGameScreenshotsFolder( File gameFolder );
    
    
    public final File getPluginFolder()
    {
        return ( pluginFolder );
    }
    
    public final String getPluginPath()
    {
        return ( pluginPath );
    }
    
    public final PluginINI getPluginINI()
    {
        return ( pluginINI );
    }
    
    public final File getGameFolder()
    {
        return ( gameFolder );
    }
    
    public final String getGamePath()
    {
        return ( gamePath );
    }
    
    public final File getConfigFolder()
    {
        return ( configFolder );
    }
    
    public final String getConfigPath()
    {
        return ( configPath );
    }
    
    public final File getCacheFolder()
    {
        return ( cacheFolder );
    }
    
    public final String getCachePath()
    {
        return ( cachePath );
    }
    
    public final File getBordersFolder()
    {
        return ( bordersFolder );
    }
    
    public final String getBordersPath()
    {
        return ( bordersPath );
    }
    
    public final File getImagesFolder()
    {
        return ( imagesFolder );
    }
    
    public final String getImagesPath()
    {
        return ( imagesPath );
    }
    
    public final File getEditorFolder()
    {
        return ( editorFolder );
    }
    
    public final String getEditorPath()
    {
        return ( editorPath );
    }
    
    public final File getGameScreenshotsFolder()
    {
        return ( gameScreenshotsFolder );
    }
    
    protected GameFileSystem()
    {
        final boolean isJarMode = ResourceManager.isJarMode();
        
        if ( isJarMode )
            this.gameFolder = findGameFolder( pluginFolder );
        else
            this.gameFolder = readDevGameFolder();
        
        this.gamePath = gameFolder.getAbsolutePath();
        
        this.configFolder = findConfigFolder( pluginFolder, pluginINI );
        this.configPath = configFolder.getAbsolutePath();
        
        this.bordersFolder = findBordersFolder( pluginFolder, pluginINI, configFolder );
        this.bordersPath = bordersFolder.getAbsolutePath();
        
        this.cacheFolder = findCacheFolder( pluginFolder, pluginINI );
        this.cachePath = ( cacheFolder == null ) ? null : cacheFolder.getAbsolutePath();
        
        this.imagesFolder = findImagesFolder( pluginFolder, pluginINI, configFolder );
        this.imagesPath = imagesFolder.getAbsolutePath();
        
        this.editorFolder = findEditorFolder( pluginFolder, pluginINI );
        this.editorPath = editorFolder.getAbsolutePath();
        
        this.gameScreenshotsFolder = findGameScreenshotsFolder( gameFolder );
    }
    
    private static GameFileSystem createInstance()
    {
        // TODO: Decide somehow, which game is being used.
        
        return ( new GameFileSystemRFactor() );
    }
    
    public static final GameFileSystem INSTANCE = GameFileSystem.createInstance();
}
