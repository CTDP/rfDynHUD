package net.ctdp.rfdynhud.gamedata;

import java.io.File;

import net.ctdp.rfdynhud.util.PluginINI;
import net.ctdp.rfdynhud.util.ResourceManager;
import net.ctdp.rfdynhud.util.__UtilHelper;

public class GameFileSystem
{
    private static final GameFileSystemInitializer INITIALIZER = GameFileSystemInitializer.getInstance();
    
    public static final File PLUGIN_FOLDER = __UtilHelper.PLUGIN_FOLDER;
    public static final String PLUGIN_PATH = PLUGIN_FOLDER.getAbsolutePath();
    public static final PluginINI PLUGIN_INI = new PluginINI();
    public static final File GAME_FOLDER = INITIALIZER.findGameFolder( PLUGIN_FOLDER );
    public static final String GAME_PATH = GAME_FOLDER.getAbsolutePath();
    public static final File CONFIG_FOLDER = PLUGIN_INI.getGeneralConfigFolder();
    public static final String CONFIG_PATH = CONFIG_FOLDER.getAbsolutePath();
    public static final File IMAGES_FOLDER = new File( new File( CONFIG_FOLDER, "data" ), "images" ).getAbsoluteFile();
    public static final File EDITOR_FOLDER = ResourceManager.isJarMode() ? new File( PLUGIN_FOLDER, "editor" ).getAbsoluteFile() : new File( __UtilHelper.stripDotDots( new File( "." ).getAbsolutePath() ), "data" ).getAbsoluteFile();
    public static final String EDITOR_PATH = EDITOR_FOLDER.getAbsolutePath();
    public static final File SCREENSHOTS_FOLDER = INITIALIZER.findGameScreenshotsFolder( GAME_FOLDER );
    
    public static File getPathFromGameConfigINI( File gameFolder, String setting, String fallback )
    {
        return ( INITIALIZER.getPathFromGameConfigINI( gameFolder, setting, fallback ) );
    }
}
