package net.ctdp.rfdynhud.gamedata;

import java.io.File;
import java.io.IOException;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

class GameFileSystemRFactor extends GameFileSystem
{
    private static boolean isRoot( File folder )
    {
        return ( folder.getParent() == null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected File findGameFolder( File pluginFolder )
    {
        File f = pluginFolder.getParentFile();
        
        while ( !new File( f, "rFactor.exe" ).exists() && !isRoot( f ) )
            f = f.getParentFile();
        
        if ( new File( f, "rFactor.exe" ).exists() )
            return ( f.getAbsoluteFile() );
        
        if ( isRoot( pluginFolder ) )
            return ( pluginFolder.getAbsoluteFile() );
        
        return ( pluginFolder.getParentFile().getAbsoluteFile() );
    }
    
    private static File getFallbackGameConfigINIPath( File gameFolder, String fallback )
    {
        File fallback0 = new File( gameFolder, fallback ).getAbsoluteFile();
        
        try
        {
            return ( fallback0.getCanonicalFile() );
        }
        catch ( IOException e )
        {
            return ( fallback0 );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public File getPathFromGameConfigINI( final String setting, String fallback )
    {
        File gameFolder = getGameFolder();
        File config = new File( gameFolder, "config.ini" );
        
        if ( !config.exists() )
        {
            return ( getFallbackGameConfigINIPath( gameFolder, fallback ) );
        }
        
        final String[] result = { null };
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( "COMPONENTS".equalsIgnoreCase( group ) && setting.equalsIgnoreCase( key ) )
                    {
                        result[0] = value;
                        
                        return ( false );
                    }
                    
                    return ( true );
                }
            }.parse( config );
        }
        catch ( ParsingException e1 )
        {
            return ( getFallbackGameConfigINIPath( gameFolder, fallback ) );
        }
        catch ( IOException e1 )
        {
            return ( getFallbackGameConfigINIPath( gameFolder, fallback ) );
        }
        
        if ( result[0] == null )
            return ( getFallbackGameConfigINIPath( gameFolder, fallback ) );
        
        File f = new File( result[0] );
        if ( f.isAbsolute() )
            return ( f );
        
        f = new File( gameFolder, result[0] );
        try
        {
            return ( f.getCanonicalFile() );
        }
        catch ( IOException e )
        {
            return ( f.getAbsoluteFile() );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected File findGameScreenshotsFolder( File gameFolder )
    {
        return ( getPathFromGameConfigINI( "ScreenShotsDir", "UserData" + File.separator + "ScreenShots" ) );
    }
    
    public GameFileSystemRFactor()
    {
        super();
    }
}
