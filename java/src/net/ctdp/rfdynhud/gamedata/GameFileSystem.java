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

import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.PluginINI;
import net.ctdp.rfdynhud.util.ResourceManager;
import net.ctdp.rfdynhud.util.__UtilHelper;

/**
 * Model of the game's (e.g. rFactor) filesystem
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class GameFileSystem
{
    private final File pluginFolder = __UtilHelper.PLUGIN_FOLDER;
    private final String pluginPath = pluginFolder.getAbsolutePath();
    private final PluginINI pluginINI = new PluginINI( pluginFolder );
    private final File gameFolder;
    private final String gamePath;
    private final File locationsFolder;
    private final String locationsPath;
    private final File configFolder;
    private final String configPath;
    private final File subPluginsFolder;
    private final String subPluginsPath;
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
    
    /**
     * Called once at instantiation time to initialize the game's root folder.
     * 
     * @param pluginFolder the plugin's main folder
     * 
     * @return the game's root folder.
     */
    protected abstract File findGameFolder( File pluginFolder );
    
    /**
     * Called once at instantiation time to initialize the game's &quot;Locations&quot; folder.
     * 
     * @param gameFolder the game's root folder
     * 
     * @return the game's &quot;Locations&quot; folder.
     */
    protected abstract File findLocationsFolder( File gameFolder );
    
    /**
     * Called once at instantiation time to initialize the plugin's config folder.
     * 
     * @param pluginFolder the plugin's main folder
     * @param pluginINI an interface to the main plugin's ini file
     * 
     * @return the plugin's config folder.
     */
    protected File findConfigFolder( File pluginFolder, PluginINI pluginINI )
    {
        return ( pluginINI.getGeneralConfigFolder() );
    }
    
    /**
     * Called once at instantiation time to initialize the sub plugins' folder.
     * 
     * @param pluginFolder the plugin's main folder
     * 
     * @return the sub plugins' folder.
     */
    protected File findSubPluginsFolder( File pluginFolder )
    {
        return ( new File( pluginFolder, "plugins" ).getAbsoluteFile() );
    }
    
    /**
     * Called once at instantiation time to initialize the plugin's cache folder.
     * 
     * @param pluginFolder the plugin's main folder
     * @param pluginINI an interface to the main plugin's ini file
     * 
     * @return the plugin's cache folder.
     */
    protected File findCacheFolder( File pluginFolder, PluginINI pluginINI )
    {
        return ( pluginINI.getGeneralCacheFolder() );
    }
    
    /**
     * Called once at instantiation time to initialize the plugin's border folder.
     * 
     * @param pluginFolder the plugin's main folder
     * @param pluginINI an interface to the main plugin's ini file
     * @param configFolder the plugin's config folder
     * 
     * @return the plugin's border folder.
     */
    protected File findBordersFolder( File pluginFolder, PluginINI pluginINI, File configFolder )
    {
        return ( new File( new File( configFolder, "data" ), "borders" ).getAbsoluteFile() );
    }
    
    /**
     * Called once at instantiation time to initialize the plugin's images folder.
     * 
     * @param pluginFolder the plugin's main folder
     * @param pluginINI an interface to the main plugin's ini file
     * @param configFolder the plugin's config folder
     * 
     * @return the plugin's image's folder.
     */
    protected File findImagesFolder( File pluginFolder, PluginINI pluginINI, File configFolder )
    {
        return ( new File( new File( configFolder, "data" ), "images" ).getAbsoluteFile() );
    }
    
    /**
     * Called once at instantiation time to initialize the plugin's editor folder.
     * 
     * @param pluginFolder the plugin's main folder
     * @param pluginINI an interface to the main plugin's ini file
     * 
     * @return the plugin's editor folder.
     */
    protected File findEditorFolder( File pluginFolder, PluginINI pluginINI )
    {
        if ( ResourceManager.isJarMode() )
            return ( new File( pluginFolder, "editor" ).getAbsoluteFile() );
        
        return ( new File( new File( __UtilHelper.stripDotDots( new File( "." ).getAbsolutePath() ), "yyy_data" ), "editor" ).getAbsoluteFile() );
    }
    
    /**
     * Gets an absolute path from the game's config ini file.
     * 
     * @param setting the setting to query
     * @param fallback the fallback value, if the setting couldn't be read
     * 
     * @return the path as a File object.
     */
    public abstract File getPathFromGameConfigINI( String setting, String fallback );
    
    /**
     * Called once at instantiation time to initialize the game's screenshots folder.
     * 
     * @param gameFolder the game's root folder
     * 
     * @return the game's screenshots folder.
     */
    protected abstract File findGameScreenshotsFolder( File gameFolder );
    
    
    /**
     * Gets the plugin's main folder.
     * 
     * @return the plugin's main folder.
     */
    public final File getPluginFolder()
    {
        return ( pluginFolder );
    }
    
    /**
     * Gets the plugin's main folder.
     * 
     * @return the plugin's main folder.
     */
    public final String getPluginPath()
    {
        return ( pluginPath );
    }
    
    /**
     * Gets an abstraction of the plugin's main ini file.
     * 
     * @return an abstraction of the plugin's main ini file.
     */
    public final PluginINI getPluginINI()
    {
        return ( pluginINI );
    }
    
    /**
     * Gets the game's root folder.
     * 
     * @return the game's root folder.
     */
    public final File getGameFolder()
    {
        return ( gameFolder );
    }
    
    /**
     * Gets the game's root folder.
     * 
     * @return the game's root folder.
     */
    public final String getGamePath()
    {
        return ( gamePath );
    }
    
    /**
     * Gets the game's &quot;Locations&quot; folder.
     * 
     * @return the game's &quot;Locations&quot; folder.
     */
    public final File getLocationsFolder()
    {
        return ( locationsFolder );
    }
    
    /**
     * Gets the game's &quot;Locations&quot; folder.
     * 
     * @return the game's &quot;Locations&quot; folder.
     */
    public final String getLocationsPath()
    {
        return ( locationsPath );
    }
    
    /**
     * Locates the current vehicle setup file.
     * 
     * @param gameData the live game data
     * 
     * @return the setup file (where it should be, may not exist).
     */
    public abstract File locateSetupFile( LiveGameData gameData );
    
    /**
     * Gets the plugin's config folder.
     * 
     * @return the plugin's config folder.
     */
    public final File getConfigFolder()
    {
        return ( configFolder );
    }
    
    /**
     * Gets the plugin's config folder.
     * 
     * @return the plugin's config folder.
     */
    public final String getConfigPath()
    {
        return ( configPath );
    }
    
    /**
     * Gets sub plugins' folder.
     * 
     * @return sub plugins' folder.
     */
    public final File getSubPluginsFolder()
    {
        return ( subPluginsFolder );
    }
    
    /**
     * Gets sub plugins' folder.
     * 
     * @return sub plugins' folder.
     */
    public final String getSubPluginsPath()
    {
        return ( subPluginsPath );
    }
    
    /**
     * Gets the plugin's cache folder.
     * 
     * @return the plugin's cache folder.
     */
    public final File getCacheFolder()
    {
        return ( cacheFolder );
    }
    
    /**
     * Gets the plugin's cache folder.
     * 
     * @return the plugin's cache folder.
     */
    public final String getCachePath()
    {
        return ( cachePath );
    }
    
    /**
     * Gets the plugin's borders folder.
     * 
     * @return the plugin's borders folder.
     */
    public final File getBordersFolder()
    {
        return ( bordersFolder );
    }
    
    /**
     * Gets the plugin's borders folder.
     * 
     * @return the plugin's borders folder.
     */
    public final String getBordersPath()
    {
        return ( bordersPath );
    }
    
    /**
     * Gets the plugin's images folder.
     * 
     * @return the plugin's images folder.
     */
    public final File getImagesFolder()
    {
        return ( imagesFolder );
    }
    
    /**
     * Gets the plugin's images folder.
     * 
     * @return the plugin's images folder.
     */
    public final String getImagesPath()
    {
        return ( imagesPath );
    }
    
    /**
     * Gets the plugin's editor folder.
     * 
     * @return the plugin's editor folder.
     */
    public final File getEditorFolder()
    {
        return ( editorFolder );
    }
    
    /**
     * Gets the plugin's editor folder.
     * 
     * @return the plugin's editor folder.
     */
    public final String getEditorPath()
    {
        return ( editorPath );
    }
    
    /**
     * Gets the game's screenshots folder.
     * 
     * @return the game's screenshots folder.
     */
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
        
        this.locationsFolder = findLocationsFolder( gameFolder );
        this.locationsPath = locationsFolder.getAbsolutePath();
        
        this.configFolder = findConfigFolder( pluginFolder, pluginINI );
        this.configPath = configFolder.getAbsolutePath();
        
        this.bordersFolder = findBordersFolder( pluginFolder, pluginINI, configFolder );
        this.bordersPath = bordersFolder.getAbsolutePath();
        
        this.subPluginsFolder = findSubPluginsFolder( pluginFolder );
        this.subPluginsPath = ( subPluginsFolder == null ) ? null : subPluginsFolder.getAbsolutePath();
        
        this.cacheFolder = findCacheFolder( pluginFolder, pluginINI );
        this.cachePath = ( cacheFolder == null ) ? null : cacheFolder.getAbsolutePath();
        
        this.imagesFolder = findImagesFolder( pluginFolder, pluginINI, configFolder );
        this.imagesPath = imagesFolder.getAbsolutePath();
        
        this.editorFolder = findEditorFolder( pluginFolder, pluginINI );
        this.editorPath = editorFolder.getAbsolutePath();
        
        this.gameScreenshotsFolder = findGameScreenshotsFolder( gameFolder );
    }
    
    private static GameFileSystem createInstance( SupportedGames gameId )
    {
        if ( gameId == SupportedGames.rFactor )
            return ( new GameFileSystemRFactor() );
        
        throw new Error( "Unsupported game: " + gameId );
    }
    
    /**
     * This is the public singleton instance of this class.
     */
    public static final GameFileSystem INSTANCE = GameFileSystem.createInstance( __GameIDHelper.gameId );
}
