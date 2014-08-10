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
package net.ctdp.rfdynhud.gamedata;

import java.io.File;

import net.ctdp.rfdynhud.input.InputMappings;

/**
 * Model of the current player's profile information
 * 
 * @author Marvin Froehlich
 */
public abstract class ProfileInfo
{
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
        
        public static final String TEMPERATURE_UNIT_GLYPHS_KELVIN = "�K";
        public static final String TEMPERATURE_UNIT_GLYPHS_CELSIUS = "�C";
        public static final String TEMPERATURE_UNIT_GLYPHS_FAHRENHEIT = "�F";
        
        public static final String SPACE_UNIT_GLYPHS_LITERS = "L";
        public static final String SPACE_UNIT_GLYPHS_GALLONS = "gal";
        
        public static final String PRESSURE_UNIT_GLYPHS_KPA = "kpa";
        public static final String PRESSURE_UNIT_GLYPHS_PSI = "psi";
        
        public final String getTemperatureUnitGlyphs()
        {
            if ( this == IMPERIAL )
                return ( TEMPERATURE_UNIT_GLYPHS_FAHRENHEIT );
            
            return ( TEMPERATURE_UNIT_GLYPHS_CELSIUS );
        }
        
        public final String getSpaceUnitGlyphs()
        {
            if ( this == IMPERIAL )
                return ( SPACE_UNIT_GLYPHS_GALLONS );
            
            return ( SPACE_UNIT_GLYPHS_LITERS );
        }
        
        public final String getPressureUnitGlyphs()
        {
            if ( this == IMPERIAL )
                return ( PRESSURE_UNIT_GLYPHS_PSI );
            
            return ( PRESSURE_UNIT_GLYPHS_KPA );
        }
        
        public static final class Convert
        {
            /**
             * Zero degrees Kelvin in Celsius
             */
            public static final float ZERO_KELVIN = -273.15f;
            
            /**
             * Offset for Celsius to Fahrenheit conversion. You will also need to factor it by {@link #FAHRENHEIT_FACTOR}.
             * Better use {@link #celsius2Fahrehheit(float)}.
             */
            public static final float FAHRENHEIT_OFFSET = 32.0f;
            
            /**
             * Factor for Celsius to Fahrenheit conversion. You will also need to offset it by {@link #FAHRENHEIT_OFFSET}.
             * Better use {@link #celsius2Fahrehheit(float)}.
             */
            public static final float FAHRENHEIT_FACTOR = 1.8f;
            
            /**
             * Liters (l) to Gallons (gal)
             */
            public static final float LITERS_TO_GALONS = 0.26417287f;
            
            /**
             * Kilopascal (kPa) to PSI
             */
            public static final float KPA_TO_PSI = 0.14503773773375085503535504944423f;
            
            /**
             * Kilometers (km) to Miles (mi)
             */
            public static final float KM_TO_MI = 0.621371192f;
            
            /**
             * Miles (mi) to Kilometers (km)
             */
            public static final float MI_TO_KM = 1.609344f;
            
            /**
             * Centimenter (cm) to Inch (in)
             */
            public static final float CM_TO_INCH = 0.3937007874015748031496062992126f;
            
            /**
             * Millimeter (mm) to Inch (in)
             */
            public static final float MM_TO_INCH = CM_TO_INCH / 10f;
            
            /**
             * Meter (m) to Inch (in)
             */
            public static final float M_TO_INCH = CM_TO_INCH * 10f;
            
            /**
             * Newton (N) to pound force (LBS)
             */
            public static final float N_TO_LBS = 5.71014715f;
            
            /**
             * Converts a value in &quot;degrees Celsius&quot; to &quot;degrees Fahreheit&quot;.<br />
             * Math: fahrenheit = {@link #FAHRENHEIT_OFFSET} + celsius * {@link #FAHRENHEIT_FACTOR}
             * 
             * @param celsius the value in Celsius.
             * 
             * @return the value in Fahrenheit.
             */
            public static final float celsius2Fahrehheit( float celsius )
            {
                return ( FAHRENHEIT_OFFSET + celsius * FAHRENHEIT_FACTOR );
            }
            
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
        MIH,
        KMH,
        ;
        
        /**
         * @deprecated replaced by {@link #MIH};
         */
        @Deprecated
        public static final SpeedUnits MPH = MIH;
        
        /**
         * @deprecated replaced by {@link #KMH};
         */
        @Deprecated
        public static final SpeedUnits KPH = KMH;
        
        public static final String SPEED_UNIT_KMH = "km/h";
        public static final String SPEED_UNIT_MIH = "mi/h";
        
        public final String getSpeedUnitGlyphs()
        {
            if ( this == MIH )
                return ( SPEED_UNIT_MIH );
            
            return ( SPEED_UNIT_KMH );
        }
        
        public static final class Convert
        {
            /**
             * meters per second (m/s) to miles per hour (mi/h)
             */
            public static final float MS_TO_MIH = 2.237f;
            
            /**
             * @deprecated replaced by {@link #MS_TO_MIH};
             */
            @Deprecated
            public static final float MPS_TO_MPH = MS_TO_MIH;
            
            /**
             * meters per second (m/s) to kilometers per hour (km/h)
             */
            public static final float MS_TO_KMH = 3.6f; // 3600f / 1000f
            
            /**
             * @deprecated replaced by {@link #MS_TO_KMH};
             */
            @Deprecated
            public static final float MPS_TO_KPH = MS_TO_KMH;
            
            /**
             * miles per hour (mi/h) to meters per second (m/s)
             */
            public static final float MIH_TO_MS = 0.44704f;
            
            /**
             * @deprecated replaced by {@link #MIH_TO_MS};
             */
            @Deprecated
            public static final float MPH_TO_MPS = MIH_TO_MS;
            
            /**
             * kilometers per hour (km/h) miles per hour (mi/h)
             */
            public static final float KMH_TO_MS = 0.278f; // 3600f / 1000f
            
            /**
             * @deprecated replaced by {@link #KMH_TO_MS};
             */
            @Deprecated
            public static final float KPH_TO_MPS = KMH_TO_MS;
            
            /**
             * kilometers per hour (km/h) to miles per hour (mi/h)
             */
            public static final float KMH_TO_MIH = 0.62f;
            
            /**
             * @deprecated replaced by {@link #KMH_TO_MIH};
             */
            @Deprecated
            public static final float KPH_TO_MPH = KMH_TO_MIH;
            
            /**
             * miles per hour (mi/h) to kilometers per hour (km/h)
             */
            public static final float MIH_TO_KMH = 1.6099344f;
            
            /**
             * @deprecated replaced by {@link #MIH_TO_KMH};
             */
            @Deprecated
            public static final float MPH_TO_KPH = MIH_TO_KMH;
            
            private Convert()
            {
            }
        }
    }
    
    private long updateId = 0L;
    
    protected String teamName = null;
    protected String nationality = null;
    protected String birthDate = null;
    protected String location = null;
    protected String modName = null; // The current rFactor game file (*.RFM) to load
    protected String helmet = null;
    protected Integer uniqueID = null; // Helps to uniquely identify in multiplayer (along with name) if leaving and coming back
    protected Integer startingDriver = null; // Zero-based index of starting driver (0=driver1, 1=driver2, 2=driver3, etc.)
    protected Integer aiControlsDriver = null; // Bitfield defining which drivers the AI controls (0=none, 1=driver1, 2=driver2, 3=driver1+driver2, etc.)
    protected Float driverHotswapDelay = null; // Delay in seconds between switching controls to AI or remote driver
    
    protected Float multiRaceLength = null;
    protected Boolean showCurrentLap = null;
    protected Integer numReconLaps = null;
    protected MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
    protected SpeedUnits speedUnits = SpeedUnits.KPH;
    
    /**
     * Gets whether this information in this instance is valid for the current session.
     * This is false until rFactor is so kind to store the file.
     * 
     * @return whether this information in this instance is valid for the current session.
     */
    public abstract boolean isValid();
    
    protected void reset()
    {
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
        
        multiRaceLength = 1.0f;
        showCurrentLap = true;
        numReconLaps = 0;
        measurementUnits = MeasurementUnits.METRIC;
        speedUnits = SpeedUnits.KMH;
    }
    
    /**
     * Updates the information from the game.
     * 
     * @return whether anything has been updated.
     */
    protected abstract boolean updateImpl();
    
    /**
     * Updates the information from the game.
     * 
     * @return whether anything has been updated.
     */
    public final boolean update()
    {
        boolean result = updateImpl();
        
        if ( result )
            updateId++;
        
        return ( result );
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
    public abstract File getProfileFolder();
    
    /**
     * Gets the used PLR file.
     * 
     * @return the used PLR file.
     */
    public abstract File getProfileFile();
    
    /**
     * Validates the current input mappings and returns an array of warning messages, if any, <code>null</code> otherwise.
     * 
     * @param mappings
     * 
     * @return an array of warning messages, if any, <code>null</code> otherwise.
     */
    public abstract String[] validateInputBindings( InputMappings mappings );
    
    /**
     * @deprecated only valid for rf1
     * @return
     */
    @Deprecated
    public String getRaceCastEmail()
    {
        return ( null );
    }
    
    /**
     * @deprecated only valid for rf1
     * @return
     */
    @Deprecated
    public String getRaceCastPassword()
    {
        return ( null );
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
    
    /*
     * Helps to uniquely identify in multiplayer (along with name) if leaving and coming back
     * 
     * @return the player's unique ID.
     */
    /*
    public final Integer getUniqueID()
    {
        return ( uniqueID );
    }
    */
    
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
    public abstract Boolean getFormationLap();
    
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
     * Create a new instance.
     */
    protected ProfileInfo()
    {
    }
}
