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

import java.io.File;
import java.io.FileFilter;

import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Model of the current player's profile information
 * 
 * @author Marvin Froehlich
 */
public class ProfileInfo
{
    private static final FileFilter DIRECTORY_FILE_FILTER = new FileFilter()
    {
        @Override
        public boolean accept( File pathname )
        {
            return ( pathname.isDirectory() );
        }
    };
    
    /**
     * Model of measurement units (everything but speed)
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static enum MeasurementUnits
    {
        METRIC,
        IMPERIAL,
        ;
        
        public static final class Convert
        {
            public static final float ZERO_KELVIN = -273.15f;
            public static final float FAHRENHEIT_OFFSET = 32.0f;
            public static final float FAHRENHEIT_FACTOR = 1.8f;
            public static final float LITERS_TO_GALONS = 0.26417287f;
            
            private Convert()
            {
            }
        }
        
        /**
         * Converts the passed amount of fuel liers to the units selected in the PLR (liters, if you're a good boy).
         * 
         * @param liters
         * 
         * @return the amount converted to the selected units.
         */
        public final float getFuelAmountFromLiters( float liters )
        {
            if ( this == METRIC )
                return ( liters );
            
            return ( Convert.LITERS_TO_GALONS * liters );
        }
    }
    
    /**
     * Model of speed units
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static enum SpeedUnits
    {
        MPH,
        KPH,
        ;
        
        public static final class Convert
        {
            public static final float MPS_TO_MPH = 2.237f;
            public static final float MPS_TO_KPH = 3.6f; // 3600f / 1000f
            public static final float MPH_TO_MPS = 0.44704f;
            public static final float KPH_TO_MPS = 0.278f; // 3600f / 1000f
            public static final float KPH_TO_MPH = 0.62f;
            public static final float MPH_TO_KPH = 1.6099344f;
            
            private Convert()
            {
            }
        }
    }
    
    public static final File USERDATA_FOLDER = GameFileSystem.INSTANCE.getPathFromGameConfigINI( "SaveDir", "UserData" );
    
    private File profileFolder = null;
    private File plrFile = null;
    private long plrLastModified = -1L;
    
    private long updateId = 0L;
    
    private File lastUsedTrackFile = null;
    
    private String raceCastEmail = null; // The email you are registered with on racecast.rfactor.net
    private String raceCastPassword = null; // Your password on racecast.rfactor.net
    private File vehFile = null;
    private String teamName = null;
    private String nationality = null;
    private String birthDate = null;
    private String location = null;
    private String modName = null; // The current rFactor game file (*.RFM) to load
    private String helmet = null;
    private Integer uniqueID = null; // Helps to uniquely identify in multiplayer (along with name) if leaving and coming back
    private Integer startingDriver = null; // Zero-based index of starting driver (0=driver1, 1=driver2, 2=driver3, etc.)
    private Integer aiControlsDriver = null; // Bitfield defining which drivers the AI controls (0=none, 1=driver1, 2=driver2, 3=driver1+driver2, etc.)
    private Float driverHotswapDelay = null; // Delay in seconds between switching controls to AI or remote driver
    
    private Float multiRaceLength = null;
    private Boolean showCurrentLap = null;
    private Integer numReconLaps = null;
    private Integer formationLapFlag = null;
    private MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
    private SpeedUnits speedUnits = SpeedUnits.KPH;
    
    /**
     * Gets whether this information in this instance is valid for the current session.
     * This is false until rFactor is so kind to store the file.
     * 
     * @return whether this information in this instance is valid for the current session.
     */
    public final boolean isValid()
    {
        return ( plrFile != null );
    }
    
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
        
        raceCastEmail = null;
        raceCastPassword = null;
        vehFile = null;
        teamName = "N/A";
        nationality = null;
        birthDate = null;
        location = null;
        modName = "N/A";
        helmet = null;
        uniqueID = null;
        startingDriver = null;
        aiControlsDriver = null;
        driverHotswapDelay = null;
        
        lastUsedTrackFile = null;
        multiRaceLength = 1.0f;
        showCurrentLap = true;
        numReconLaps = 0;
        formationLapFlag = 1;
        measurementUnits = MeasurementUnits.METRIC;
        speedUnits = SpeedUnits.KPH;
    }
    
    boolean update()
    {
        final GameFileSystem fileSystem = GameFileSystem.INSTANCE;
        
        File plrFile = findPLRFile();
        
        if ( plrFile == null )
        {
            RFDHLog.error( "ERROR: No Profile with PLR file found under \"" + USERDATA_FOLDER.getAbsolutePath() + "\". Plugin unusable!" );
            
            reset();
            return ( false );
        }
        
        if ( ( this.plrFile != null ) && plrFile.equals( this.plrFile ) && ( plrFile.lastModified() == plrLastModified ) )
        {
            return ( true );
        }
        
        reset();
        
        RFDHLog.printlnEx( "INFO: Using PLR file \"" + plrFile.getAbsolutePath() + "\"" );
        
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
                                lastUsedTrackFile = new File( fileSystem.getGameFolder(), value );
                        }
                    }
                    else if ( group.equalsIgnoreCase( "DRIVER" ) )
                    {
                        if ( key.equalsIgnoreCase( "RaceCast Email" ) )
                        {
                            raceCastEmail = value;
                        }
                        else if ( key.equalsIgnoreCase( "RaceCast Password" ) )
                        {
                            raceCastPassword = value;
                        }
                        else if ( key.equalsIgnoreCase( "Vehicle File" ) )
                        {
                            vehFile = new File( GameFileSystem.INSTANCE.getGameFolder(), value );
                        }
                        else if ( key.equalsIgnoreCase( "Team" ) )
                        {
                            teamName = value;
                        }
                        else if ( key.equalsIgnoreCase( "Nationality" ) )
                        {
                            nationality = value;
                        }
                        else if ( key.equalsIgnoreCase( "Birth Date" ) )
                        {
                            birthDate = value;
                        }
                        else if ( key.equalsIgnoreCase( "Location" ) )
                        {
                            location = value;
                        }
                        else if ( key.equalsIgnoreCase( "Game Description" ) )
                        {
                            value = value.trim();
                            if ( value.toLowerCase().endsWith( ".rfm" ) )
                                modName = value.substring( 0, value.length() - 4 );
                            else
                                modName = value;
                        }
                        else if ( key.equalsIgnoreCase( "Helmet" ) )
                        {
                            helmet = value;
                        }
                        else if ( key.equalsIgnoreCase( "Unique ID" ) )
                        {
                            uniqueID = Integer.parseInt( value );
                        }
                        else if ( key.equalsIgnoreCase( "Starting Driver" ) )
                        {
                            startingDriver = Integer.parseInt( value );
                        }
                        else if ( key.equalsIgnoreCase( "AI Controls Driver" ) )
                        {
                            aiControlsDriver = Integer.parseInt( value );
                        }
                        else if ( key.equalsIgnoreCase( "Driver Hotswap Delay" ) )
                        {
                            driverHotswapDelay = Float.parseFloat( value );
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
                                RFDHLog.debug( "Unable to parse \"Show Extra Lap\" from PLR file. Defaulting to 'true'." );
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
                                RFDHLog.debug( "Unable to parse \"Measurement Units\" from PLR file. Defaulting to METRIC." );
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
                                RFDHLog.debug( "Unable to parse \"Speed Units\" from PLR file. Defaulting to KPH." );
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
            RFDHLog.exception( t );
        }
        
        updateId++;
        
        return ( true );
    }
    
    /**
     * This is incremented every time the info is updated.
     *  
     * @return the current update id.
     */
    public final long getUpdateId()
    {
        return ( updateId );
    }
    
    /**
     * Gets the folder, where rFactor stores profiles.
     * 
     * @return the folder, where rFactor stores profiles.
     */
    public final File getProfileFolder()
    {
        return ( profileFolder );
    }
    
    /**
     * Gets the used PLR file.
     * 
     * @return the used PLR file.
     */
    public final File getPLRFile()
    {
        return ( plrFile );
    }
    
    /**
     * Gets the email you are registered with on racecast.rfactor.net
     * 
     * @return the email you are registered with on racecast.rfactor.net
     */
    public final String getRaceCastEmail()
    {
        return ( raceCastEmail );
    }
    
    /**
     * Gets your password on racecast.rfactor.net
     * 
     * @return Your password on racecast.rfactor.net
     */
    public final String getRaceCastPassword()
    {
        return ( raceCastPassword );
    }
    
    /**
     * Gets the currently used vehicle file.
     * 
     * @return the currently used vehicle file.
     */
    public final File getVehicleFile()
    {
        return ( vehFile );
    }
    
    /**
     * Gets the currently used team's name.
     * 
     * @return the currently used team's name.
     */
    public final String getTeamName()
    {
        return ( teamName );
    }
    
    /**
     * Gets the player's nationality.
     * 
     * @return the player's nationality.
     */
    public final String getNationality()
    {
        return ( nationality );
    }
    
    /**
     * Gets the player's birth date. Should be in the format &quot;YYYY-MM-DD&quot;.
     * 
     * @return the player's birth date.
     */
    public final String getBirthDate()
    {
        return ( birthDate );
    }
    
    /**
     * Gets the player's location.
     * 
     * @return the player's location.
     */
    public final String getLocation()
    {
        return ( location );
    }
    
    /**
     * Gets the current mod's name.
     * 
     * @return the current mod's name.
     */
    final String getModName()
    {
        return ( modName );
    }
    
    /**
     * Gets the player's helmet.
     * 
     * @return the player's helmet.
     */
    public final String getHelmet()
    {
        return ( helmet );
    }
    
    /**
     * Helps to uniquely identify in multiplayer (along with name) if leaving and coming back
     * 
     * @return the player's unique ID.
     */
    public final Integer getUniqueID()
    {
        return ( uniqueID );
    }
    
    /**
     * Zero-based index of starting driver (0=driver1, 1=driver2, 2=driver3, etc.)
     * 
     * @return the starting driver.
     */
    public final Integer getStartingDriver()
    {
        return ( startingDriver );
    }
    
    /**
     * Bitfield defining which drivers the AI controls (0=none, 1=driver1, 2=driver2, 3=driver1+driver2, etc.)
     * 
     * @return the &quot;AI  Control Driver&quot; setting.
     */
    public final Integer getAIControlsDriver()
    {
        return ( aiControlsDriver );
    }
    
    /**
     * Delay in seconds between switching controls to AI or remote driver.
     * 
     * @return the driver hotswap delay in seconds.
     */
    public final Float getDriverHotswapDelay()
    {
        return ( driverHotswapDelay );
    }
    
    /**
     * Gets the last used scene file.
     * 
     * @return the last used scene file.
     */
    final File getLastUsedSceneFile()
    {
        return ( lastUsedTrackFile );
    }
    
    /**
     * Gets the current race length fraction.
     * 
     * @return the current race length fraction.
     */
    public final Float getRaceLengthMultiplier()
    {
        return ( multiRaceLength );
    }
    
    /**
     * Gets whether the current lap is to be displayed or the number of laps completed.
     * 
     * @return whether the current lap is to be displayed or the number of laps completed
     */
    public final Boolean getShowCurrentLap()
    {
        return ( showCurrentLap );
    }
    
    /**
     * Gets the number of configureed recon laps.
     * 
     * @return the number of configureed recon laps.
     */
    public final Integer getNumReconLaps()
    {
        return ( numReconLaps );
    }
    
    /**
     * Drive formation lap?
     * 
     * @return drive formation lap?
     */
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
    
    /**
     * Gets the selected measurement units. (Applies to everything but speed.)
     * 
     * @see #getSpeedUnits()
     * 
     * @return the selected measurement units.
     */
    public final MeasurementUnits getMeasurementUnits()
    {
        return ( measurementUnits );
    }
    
    /**
     * Gets the selected speed units.
     * 
     * @see #getMeasurementUnits()
     * 
     * @return the selected speed units.
     */
    public final SpeedUnits getSpeedUnits()
    {
        return ( speedUnits );
    }
    
    /**
     * Gets the currently used CCH file.
     * 
     * @return the currently used CCH file.
     */
    public final File getCCHFile()
    {
        if ( profileFolder == null )
            return ( null );
        
        if ( modName == null )
            return ( null );
        
        return ( new File( profileFolder, modName + ".cch" ) );
    }
    
    /**
     * Create a new instance.
     */
    public ProfileInfo()
    {
    }
}
