package net.ctdp.rfdynhud.gamedata;

import java.io.File;
import java.io.FileFilter;

import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.RFactorFileSystem;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

public class ProfileInfo
{
    private static final FileFilter DIRECTORY_FILE_FILTER = new FileFilter()
    {
        public boolean accept( File pathname )
        {
            return ( pathname.isDirectory() );
        }
    };
    
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
    
    public static final File USERDATA_FOLDER = RFactorFileSystem.getPathFromRFConfigINI( RFactorFileSystem.RFACTOR_FOLDER, "SaveDir", "UserData" );
    
    private File profileFolder = null;
    private File plrFile = null;
    private long plrLastModified = -1L;
    
    private String modName = null;
    private String vehName = null;
    private File lastUsedTrackFile = null;
    private Float multiRaceLength = null;
    private Boolean showCurrentLap = null;
    private Integer numReconLaps = null;
    private Integer formationLapFlag = null;
    private MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
    private SpeedUnits speedUnits = SpeedUnits.KPH;
    
    private static File findPLRFile()
    {
        File[] profileCandidates = USERDATA_FOLDER.listFiles( DIRECTORY_FILE_FILTER );
        
        if ( profileCandidates == null )
            return ( null );
        
        File plrFile = null;
        for ( File p : profileCandidates )
        {
            File plr = new File( p, p.getName() + ".PLR" );
            if ( plr.exists() && plr.isFile() )
            {
                if ( plrFile == null )
                    plrFile = plr;
                else if ( plr.lastModified() > plrFile.lastModified() )
                    plrFile = plr;
            }
        }
        
        return ( plrFile );
    }
    
    private void reset()
    {
        profileFolder = null;
        plrFile = null;
        
        modName = null;
        vehName = null;
        lastUsedTrackFile = null;
        multiRaceLength = null;
        showCurrentLap = null;
        numReconLaps = null;
        formationLapFlag = null;
        measurementUnits = MeasurementUnits.METRIC;
        speedUnits = SpeedUnits.KPH;
    }
    
    void update()
    {
        File plrFile = findPLRFile();
        
        if ( plrFile == null )
        {
            reset();
            return;
        }
        
        if ( ( this.plrFile != null ) && plrFile.equals( this.plrFile ) && ( plrFile.lastModified() == plrLastModified ) )
        {
            return;
        }
        
        reset();
        
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
                            lastUsedTrackFile = new File( value );
                            
                            if ( !lastUsedTrackFile.isAbsolute() )
                                lastUsedTrackFile = new File( RFactorFileSystem.RFACTOR_FOLDER, value );
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
                        else if ( key.equalsIgnoreCase( "Show Extra Lap" ) )
                        {
                            try
                            {
                                int index = Integer.parseInt( value );
                                
                                showCurrentLap = ( index != 0 );
                            }
                            catch ( Throwable t )
                            {
                                Logger.log( "Unable to parse \"Show Extra Lap\" from PLR file. Defaulting to 'true'." );
                                showCurrentLap = true;
                            }
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
            }.parse( plrFile );
            
            this.plrFile = plrFile;
            this.profileFolder = plrFile.getParentFile();
            this.plrLastModified = plrFile.lastModified();
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    public final File getProfileFolder()
    {
        return ( profileFolder );
    }
    
    public final File getPLRFile()
    {
        return ( plrFile );
    }
    
    final String getModName()
    {
        return ( modName );
    }
    
    public final String getPlayerVEHFile()
    {
        return ( vehName );
    }
    
    final File getLastUsedSceneFile()
    {
        return ( lastUsedTrackFile );
    }
    
    public final Float getRaceLengthMultiplier()
    {
        return ( multiRaceLength );
    }
    
    public final Boolean getShowCurrentLap()
    {
        return ( showCurrentLap );
    }
    
    public final Integer getNumReconLaps()
    {
        return ( numReconLaps );
    }
    
    public final Boolean getFormationLap()
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
    
    public final MeasurementUnits getMeasurementUnits()
    {
        return ( measurementUnits );
    }
    
    public final SpeedUnits getSpeedUnits()
    {
        return ( speedUnits );
    }
    
    public final File getCCHFile()
    {
        if ( profileFolder == null )
            return ( null );
        
        if ( modName == null )
            return ( null );
        
        return ( new File( profileFolder, modName + ".cch" ) );
    }
    
    public ProfileInfo()
    {
    }
}
