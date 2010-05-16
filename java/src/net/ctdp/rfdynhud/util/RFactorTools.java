package net.ctdp.rfdynhud.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

public class RFactorTools
{
    private static final FileFilter DIRECTORY_FILE_FILTER = new FileFilter()
    {
        public boolean accept( File pathname )
        {
            return ( pathname.isDirectory() );
        }
    };
    
    private static boolean isRoot( File folder )
    {
        return ( folder.getParent() == null );
    }
    
    private static File readDevRFactorFolder()
    {
        try
        {
            BufferedReader br = new BufferedReader( new FileReader( new File( "rfactor_folder.txt" ) ) );
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
    
    private static File findRFactorFolder( File pluginFolder )
    {
        if ( !ResourceManager.isJarMode() )
            return ( readDevRFactorFolder() );
        
        File f = pluginFolder.getParentFile();
        
        while ( !new File( f, "rFactor.exe" ).exists() && !isRoot( f ) )
            f = f.getParentFile();
        
        if ( new File( f, "rFactor.exe" ).exists() )
            return ( f.getAbsoluteFile() );
        
        if ( isRoot( pluginFolder ) )
            return ( pluginFolder.getAbsoluteFile() );
        
        return ( pluginFolder.getParentFile().getAbsoluteFile() );
    }
    
    private static File getPathFromRFConfigINI( File rFactorFolder, final String setting, String def )
    {
        File config = new File( rFactorFolder, "config.ini" );
        File fallback0 = new File( rFactorFolder, def ).getAbsoluteFile();
        File fallback;
        try
        {
            fallback = fallback0.getCanonicalFile();
        }
        catch ( IOException e )
        {
            fallback = fallback0;
        }
        
        if ( !config.exists() )
        {
            return ( fallback );
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
            return ( fallback );
        }
        catch ( IOException e1 )
        {
            return ( fallback );
        }
        
        if ( result[0] == null )
            return ( fallback );
        
        File f = new File( result[0] );
        if ( f.isAbsolute() )
            return ( f );
        
        f = new File( rFactorFolder, result[0] );
        try
        {
            return ( f.getCanonicalFile() );
        }
        catch ( IOException e )
        {
            return ( f.getAbsoluteFile() );
        }
    }
    
    public static final File PLUGIN_FOLDER = Helper.PLUGIN_FOLDER;
    public static final String PLUGIN_PATH = PLUGIN_FOLDER.getAbsolutePath();
    public static final PluginINI PLUGIN_INI = new PluginINI();
    public static final File RFACTOR_FOLDER = findRFactorFolder( PLUGIN_FOLDER );
    //public static final String RFACTOR_PATH = RFACTOR_FOLDER.getAbsolutePath();
    public static final File CONFIG_FOLDER = PLUGIN_INI.getGeneralConfigFolder();
    public static final String CONFIG_PATH = CONFIG_FOLDER.getAbsolutePath();
    public static final File IMAGES_FOLDER = new File( new File( CONFIG_FOLDER, "data" ), "images" ).getAbsoluteFile();
    public static final File EDITOR_FOLDER = ResourceManager.isJarMode() ? new File( PLUGIN_FOLDER, "editor" ).getAbsoluteFile() : new File( Helper.stripDotDots( new File( "." ).getAbsolutePath() ), "data" ).getAbsoluteFile();
    public static final String EDITOR_PATH = EDITOR_FOLDER.getAbsolutePath();
    
    private static File lastPLRFile = null;
    private static long lastPLRModified = -1L;
    
    public static enum MeasurementUnits
    {
        METRIC,
        IMPERIAL,
        ;
    }
    
    public static enum SpeedUnits
    {
        MPH,
        KPH,
        ;
    }
    
    private static String modName = null;
    private static String vehName = null;
    private static String lastUsedTrackFile = null;
    private static int trackRaceLaps = -1;
    private static Float multiRaceLength = null;
    private static Integer numReconLaps = null;
    private static Integer formationLapFlag = null;
    private static MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
    private static SpeedUnits speedUnits = SpeedUnits.KPH;
    
    private static int maxOpponents = -1;
    
    public static File getProfileFile( File profileFolder )
    {
        File plr = new File( profileFolder, profileFolder.getName() + ".PLR" );
        
        if ( !plr.exists() )
            return ( null );
        
        return ( plr );
    }
    
    public static File getUserDataFolder()
    {
        return ( getPathFromRFConfigINI( RFACTOR_FOLDER, "SaveDir", "UserData" ) );
    }
    
    public static File getUserScreenShotsFolder()
    {
        return ( new File( getUserDataFolder(), "ScreenShots" ) );
    }
    
    public static File getProfileFolder( File userDataFolder )
    {
        if ( !RFACTOR_FOLDER.exists() )
            return ( null );
        
        if ( userDataFolder == null )
            userDataFolder = getUserDataFolder();
        
        File[] profileCandidates = userDataFolder.listFiles( DIRECTORY_FILE_FILTER );
        
        if ( profileCandidates == null )
            return ( null );
        
        File profilePLR = null;
        for ( File p : profileCandidates )
        {
            File plr = getProfileFile( p );
            if ( plr != null )
            {
                if ( profilePLR == null )
                    profilePLR = plr;
                else if ( plr.lastModified() > profilePLR.lastModified() )
                    profilePLR = plr;
            }
        }
        
        if ( profilePLR == null )
            return ( null );
        
        return ( profilePLR.getParentFile() );
    }
    
    public static File getProfileFolder()
    {
        return ( getProfileFolder( null ) );
    }
    
    private static void updateProfileInformation( File userDataFolder, File profileFolder )
    {
        if ( profileFolder == null )
        {
            profileFolder = getProfileFolder( userDataFolder );
        }
        
        if ( profileFolder == null )
            return;
        
        File profilePLR = getProfileFile( profileFolder );
        
        if ( profilePLR == null )
            return;
        
        if ( ( lastPLRFile != null ) && profilePLR.equals( lastPLRFile ) && ( profilePLR.lastModified() <= lastPLRModified ) )
            return;
        
        modName = null;
        vehName = null;
        lastUsedTrackFile = null;
        multiRaceLength = null;
        numReconLaps = null;
        formationLapFlag = null;
        measurementUnits = MeasurementUnits.METRIC;
        speedUnits = SpeedUnits.KPH;
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( group == null )
                    {
                    }
                    else if ( group.equalsIgnoreCase( "SCENE" ) )
                    {
                        if ( key.equalsIgnoreCase( "Scene File" ) )
                        {
                            lastUsedTrackFile = value;
                        }
                    }
                    else if ( group.equalsIgnoreCase( "DRIVER" ) )
                    {
                        if ( key.equalsIgnoreCase( "Game Description" ) )
                        {
                            if ( value.toLowerCase().endsWith( ".rfm" ) )
                                modName = value.substring( 0, value.length() - 4 );
                            else
                                modName = value;
                        }
                        else if ( key.equalsIgnoreCase( "Vehicle File" ) )
                        {
                            vehName = value;
                        }
                    }
                    else if ( group.equalsIgnoreCase( "Game Options" ) )
                    {
                        if ( key.equalsIgnoreCase( "MULTI Race Length" ) )
                        {
                            multiRaceLength = Float.valueOf( value );
                        }
                        else if ( key.equalsIgnoreCase( "Measurement Units" ) )
                        {
                            try
                            {
                                int index = Integer.parseInt( value );
                                
                                measurementUnits = MeasurementUnits.values()[index];
                            }
                            catch ( Throwable t )
                            {
                                Logger.log( "Unable to parse \"Measurement Units\" from PLR file. Defaulting to METRIC." );
                                measurementUnits = MeasurementUnits.METRIC;
                            }
                        }
                        else if ( key.equalsIgnoreCase( "Speed Units" ) )
                        {
                            try
                            {
                                int index = Integer.parseInt( value );
                                
                                speedUnits = SpeedUnits.values()[index];
                            }
                            catch ( Throwable t )
                            {
                                Logger.log( "Unable to parse \"Speed Units\" from PLR file. Defaulting to KPH." );
                                speedUnits = SpeedUnits.KPH;
                            }
                        }
                    }
                    else if ( group.equalsIgnoreCase( "Race Conditions" ) )
                    {
                        if ( key.equalsIgnoreCase( "MULTI Reconnaissance" ) )
                        {
                            numReconLaps = Integer.valueOf( value );
                        }
                        else if ( key.equalsIgnoreCase( "MULTI Formation Lap" ) )
                        {
                            formationLapFlag = Integer.valueOf( value );
                        }
                    }
                    
                    return ( true );
                }
            }.parse( profilePLR );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            return;
        }
    }
    
    public static String getModName( File profileFolder )
    {
        updateProfileInformation( null, profileFolder );
        
        return ( modName );
    }
    
    public static String getPlayerVEHFile( File profileFolder )
    {
        updateProfileInformation( null, profileFolder );
        
        return ( vehName );
    }
    
    public static boolean isLastUsedTrackFileValid()
    {
        return ( lastUsedTrackFile != null );
    }
    
    public static String getPlainLastUsedTrackFile()
    {
        return ( lastUsedTrackFile );
    }
    
    public static File getLastUsedTrackFile()
    {
        if ( lastUsedTrackFile == null )
            return ( null );
        
        File f = new File( lastUsedTrackFile );
        
        if ( f.isAbsolute() )
            return ( f );
        
        return ( new File( RFACTOR_FOLDER, lastUsedTrackFile ) );
    }
    
    public static File getLastUsedTrackFile( File profileFolder )
    {
        updateProfileInformation( null, profileFolder );
        
        return ( getLastUsedTrackFile() );
    }
    
    public static Float getRaceLengthMultiplier()
    {
        return ( multiRaceLength );
    }
    
    public static Float getRaceLengthMultiplier( File profileFolder )
    {
        updateProfileInformation( null, profileFolder );
        
        return ( getRaceLengthMultiplier() );
    }
    
    public static Integer getNumReconLaps()
    {
        return ( numReconLaps );
    }
    
    public static Integer getNumReconLaps( File profileFolder )
    {
        updateProfileInformation( null, profileFolder );
        
        return ( getNumReconLaps() );
    }
    
    public static final Boolean getFormationLap()
    {
        if ( formationLapFlag == null )
            return ( null );
        
        // 0=standing start, 1=formation lap & standing start, 2=lap behind safety car & rolling start, 3=use track default, 4=fast rolling start
        switch ( formationLapFlag.intValue() )
        {
            case 0:
                return ( false );
            case 1:
                return ( true );
            case 2:
                return ( true );
            case 3:
                return ( null );
            case 4:
                return ( true );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    public static final Boolean getFormationLap( File profileFolder )
    {
        updateProfileInformation( null, profileFolder );
        
        return ( getFormationLap() );
    }
    
    public static MeasurementUnits getMeasurementUnits()
    {
        return ( measurementUnits );
    }
    
    public static MeasurementUnits getMeasurementUnits( File profileFolder )
    {
        updateProfileInformation( null, profileFolder );
        
        return ( getMeasurementUnits() );
    }
    
    public static SpeedUnits getSpeedUnits()
    {
        return ( speedUnits );
    }
    
    public static SpeedUnits getSpeedUnits( File profileFolder )
    {
        updateProfileInformation( null, profileFolder );
        
        return ( getSpeedUnits() );
    }
    
    public static int getMaxOpponents()
    {
        if ( maxOpponents < 0 )
        {
            maxOpponents = 0;
            
            String rfmName = getModName( null ) + ".rfm";
            
            BufferedReader br = null;
            
            try
            {
                br = new BufferedReader( new FileReader( new File( new File( RFACTOR_FOLDER, "rfm" ), rfmName ) ) );
                
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
                
                return ( maxOpponents );
            }
            finally
            {
                if ( br != null )
                    try { br.close(); } catch ( IOException e ) {}
            }
        }
        
        return ( maxOpponents );
    }
    
    public static File getCCHFile( File profileFolder )
    {
        if ( profileFolder == null )
            profileFolder = getProfileFolder();
        
        if ( profileFolder == null )
            return ( null );
        
        String modName = getModName( profileFolder );
        
        return ( new File( profileFolder, modName + ".cch" ) );
    }
    
    private static boolean checkGDB( File gdb, String trackname )
    {
        BufferedReader br = null;
        
        try
        {
            boolean trackFound = false;
            boolean raceLapsFound = false;
            
            trackRaceLaps = -1;
            
            br = new BufferedReader( new FileReader( gdb ) );
            
            String line = null;
            while ( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                if ( line.startsWith( "TrackName" ) )
                {
                    int idx = line.indexOf( '=', 9 );
                    if ( ( idx >= 0 ) && line.substring( idx + 1 ).trim().equals( trackname ) )
                    {
                        trackFound = true;
                    }
                }
                else if ( line.startsWith( "RaceLaps" ) )
                {
                    int idx = line.indexOf( '=', 8 );
                    if ( idx >= 0 )
                    {
                        try
                        {
                            trackRaceLaps = Integer.parseInt( line.substring( idx + 1 ).trim() );
                            raceLapsFound = true;
                        }
                        catch ( Throwable t )
                        {
                        }
                    }
                }
                
                if ( trackFound && raceLapsFound )
                    return ( true );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        finally
        {
            if ( br != null )
                try { br.close(); } catch ( Throwable t ) { Logger.log( t ); }
        }
        
        return ( false );
    }
    
    private static File searchTrackFolder( File parentDir, String trackname )
    {
        for ( File f : parentDir.listFiles() )
        {
            if ( f.isDirectory() )
            {
                File result = searchTrackFolder( f, trackname );
                if ( result != null )
                    return ( result );
            }
            else if ( f.getName().toLowerCase().endsWith( ".gdb" ) )
            {
                if ( checkGDB( f, trackname ) )
                    return ( f.getParentFile() );
            }
        }
        
        return ( null );
    }
    
    /**
     * <p>
     * Finds the folder from the GameData\Locations folder, in which a .gdb file
     * exists, that contains a line<br>
     *   TrackName = trackname
     * </p>
     * WARNING:<br>
     * This operation may take a long time.
     * 
     * @param trackname
     * 
     * @return the first matching folder (or null, if not found, but shouldn't happen).
     */
    public static File findTrackFolder( String trackname )
    {
        File locationsFolder = getPathFromRFConfigINI( RFACTOR_FOLDER, "TracksDir", "GameData\\Locations\\" );
        
        if ( ( locationsFolder == null ) || !locationsFolder.exists() )
            return ( null );
        
        return ( searchTrackFolder( locationsFolder, trackname ) );
    }
    
    /**
     * <p>
     * Gets the race laps from the current track reading it from the .gdb file
     * </p>
     * WARNING:<br>
     * This operation may take a long time.
     * 
     * @param trackname
     * 
     * @return the race laps from the current track reading it from the .gdb file
     */
    public static int updateTrackRaceLaps( String trackname )
    {
        findTrackFolder( trackname );
        
        return ( trackRaceLaps );
    }
    
    /**
     * Gets last read track race laps.
     * 
     * @return last read track race laps.
     */
    public static int getTrackRaceLaps()
    {
        return ( trackRaceLaps );
    }
    
    private static void readGDB( File gdb )
    {
        BufferedReader br = null;
        
        try
        {
            trackRaceLaps = -1;
            
            br = new BufferedReader( new FileReader( gdb ) );
            
            String line = null;
            while ( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                if ( line.startsWith( "RaceLaps" ) )
                {
                    int idx = line.indexOf( '=', 8 );
                    if ( idx >= 0 )
                    {
                        try
                        {
                            trackRaceLaps = Integer.parseInt( line.substring( idx + 1 ).trim() );
                        }
                        catch ( Throwable t )
                        {
                        }
                    }
                    
                    return;
                }
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        finally
        {
            if ( br != null )
                try { br.close(); } catch ( Throwable t ) { Logger.log( t ); }
        }
    }
    
    public static int getTrackRaceLaps( File trackFolder )
    {
        for ( File f : trackFolder.listFiles() )
        {
            if ( f.getName().toLowerCase().endsWith( ".gdb" ) )
            {
                readGDB( f );
                break;
            }
        }
        
        return ( trackRaceLaps );
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
    public static File findAIWFile( File trackFolder )
    {
        if ( trackFolder == null )
            return ( null );
        
        if ( !trackFolder.isDirectory() )
        {
            IllegalArgumentException iae = new IllegalArgumentException( "File \"" + trackFolder.getAbsolutePath() + "\" is not a valid directory and track data cannot be searched in it." );
            
            Logger.log( iae );
            
            throw iae;
        }
        
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
     * @param trackname
     * 
     * @return the AIW file for the given track.
     */
    public static File findAIWFile( String trackname )
    {
        File trackFolder = findTrackFolder( trackname );
                
        return ( findAIWFile( trackFolder ) );
    }
}
