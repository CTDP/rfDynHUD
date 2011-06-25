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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.SpeedUnits;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits.Convert;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.Engine;
import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * Our world coordinate system is left-handed, with +y pointing up.
 * The local vehicle coordinate system is as follows:
 *   +x points out the left side of the car (from the driver's perspective)
 *   +y points out the roof
 *   +z points out the back of the car
 * Rotations are as follows:
 *   +x pitches up
 *   +y yaws to the right
 *   +z rolls to the right
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TelemetryData
{
    final _TelemetryDataCapsule data;
    
    private boolean updatedInTimeScope = false;
    private long lastUpdateTimestamp = -1L;
    private long updateTimestamp = -1L;
    
    private final LiveGameData gameData;
    
    private float engineRPM = -1f;
    private float engineBaseMaxRPM = 1000.12345f;
    private float engineMaxRPM = 1000.12345f;
    
    private int engineBoostMapping = 5;
    private boolean tempBoostFlag = false;
    
    float engineLifetime = 0.0f;
    float brakeDiscThicknessFL = 0.0f;
    float brakeDiscThicknessFR = 0.0f;
    float brakeDiscThicknessRL = 0.0f;
    float brakeDiscThicknessRR = 0.0f;
    
    private float fuelLoad = -1f;
    
    float fuelUsageLastLap = -1f;
    float fuelUsageAverage = -1f;
    
    public static interface TelemetryDataUpdateListener extends LiveGameData.GameDataUpdateListener
    {
        public void onTelemetryDataUpdated( LiveGameData gameData, boolean isEditorMode );
    }
    
    private TelemetryDataUpdateListener[] updateListeners = null;
    
    public void registerListener( TelemetryDataUpdateListener l )
    {
        if ( updateListeners == null )
        {
            updateListeners = new TelemetryDataUpdateListener[] { l };
        }
        else
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                if ( updateListeners[i] == l )
                    return;
            }
            
            TelemetryDataUpdateListener[] tmp = new TelemetryDataUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
        
        gameData.registerDataUpdateListener( l );
    }
    
    public void unregisterListener( TelemetryDataUpdateListener l )
    {
        if ( updateListeners == null )
            return;
        
        int index = -1;
        for ( int i = 0; i < updateListeners.length; i++ )
        {
            if ( updateListeners[i] == l )
            {
                index = i;
                break;
            }
        }
        
        if ( index < 0 )
            return;
        
        if ( updateListeners.length == 1 )
        {
            updateListeners = null;
            return;
        }
        
        TelemetryDataUpdateListener[] tmp = new TelemetryDataUpdateListener[ updateListeners.length - 1 ];
        if ( index > 0 )
            System.arraycopy( updateListeners, 0, tmp, 0, index );
        if ( index < updateListeners.length - 1 )
            System.arraycopy( updateListeners, index + 1, tmp, index, updateListeners.length - index - 1 );
        updateListeners = tmp;
        
        gameData.unregisterDataUpdateListener( l );
    }
    
    /**
     * 
     * @param isEditorMode
     */
    void onSessionStarted( boolean isEditorMode )
    {
        this.updatedInTimeScope = false;
        this.updateTimestamp = -1L;
    }
    
    void onSessionEnded()
    {
        this.updatedInTimeScope = false;
    }
    
    void onRealtimeEntered()
    {
        this.updatedInTimeScope = true;
    }
    
    void onRealtimeExited()
    {
        this.updatedInTimeScope = false;
    }
    
    void prepareDataUpdate()
    {
    }
    
    void applyEditorPresets( EditorPresets editorPresets )
    {
        if ( editorPresets == null )
            return;
        
        VehicleScoringInfo playerVSI = gameData.getScoringInfo().getPlayersVehicleScoringInfo();
        
        setEngineBoostMapping( editorPresets.getEngineBoost() );
        playerVSI.engineBoostMapping = editorPresets.getEngineBoost();
        
        this.engineRPM = editorPresets.getEngineRPM();
        playerVSI.engineRPM = editorPresets.getEngineRPM();
        
        this.engineLifetime = editorPresets.getEngineLifetime();
        
        this.brakeDiscThicknessFL = editorPresets.getBrakeDiscThicknessFL();
        this.brakeDiscThicknessFR = editorPresets.getBrakeDiscThicknessFR();
        this.brakeDiscThicknessRL = editorPresets.getBrakeDiscThicknessRL();
        this.brakeDiscThicknessRR = editorPresets.getBrakeDiscThicknessRR();
        
        this.fuelLoad = editorPresets.getFuelLoad();
    }
    
    void onDataUpdated( boolean isEditorMode )
    {
        try
        {
            this.updatedInTimeScope = gameData.isInRealtimeMode();
            data.onDataUpdated();
            this.lastUpdateTimestamp = updateTimestamp;
            this.updateTimestamp = System.nanoTime();
            
            float bmr = data.getEngineMaxRPM();
            bmr = gameData.getPhysics().getEngine().getRevLimitRange().clampValue( bmr );
            
            if ( bmr != engineBaseMaxRPM )
            {
                // the car is controlled by the player but not the AI
                this.engineBaseMaxRPM = bmr;
            }
            
            if ( updateListeners != null )
            {
                for ( int i = 0; i < updateListeners.length; i++ )
                {
                    try
                    {
                        updateListeners[i].onTelemetryDataUpdated( gameData, isEditorMode );
                    }
                    catch ( Throwable t )
                    {
                        RFDHLog.exception( t );
                    }
                }
            }
            
            VehicleScoringInfo playerVSI = gameData.getScoringInfo().getPlayersVehicleScoringInfo();
            
            if ( playerVSI != null ) // This can happen when scoring info has not yet been updated!
            {
                playerVSI.engineRPM = getEngineRPM();
                playerVSI.engineMaxRPM = getEngineBaseMaxRPM();
                playerVSI.engineBoostMapping = getEngineBoostMapping();
                playerVSI.gear = getCurrentGear();
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    public void readFromStream( InputStream in ) throws IOException
    {
        data.loadFromStream( in );
        
        onDataUpdated( false );
    }
    
    public void writeToStream( OutputStream out ) throws IOException
    {
        data.writeToStream( out );
    }
    
    boolean checkGamePaused( long timestamp )
    {
        // TelemetryData are updated at 90Hz = 11ms. So 100ms should be a safe value to check against.
        
        return ( timestamp - updateTimestamp > 100000000L );
    }
    
    /**
     * The system timestamp (in nanos) of the last {@link TelemetryData} update.
     * 
     * @return system timestamp (in nanos) of the last {@link TelemetryData} update.
     */
    public final long getUpdateTimestamp()
    {
        return ( updateTimestamp );
    }
    
    /**
     * The delta time between the last two {@link TelemetryData} updates (using system timing) (in nanos).
     * 
     * @return delta time between the last two {@link TelemetryData} updates (using system timing).
     */
    public final long getDeltaUpdateTime()
    {
        if ( lastUpdateTimestamp == -1L )
            return ( 0L );
        
        if ( updateTimestamp == -1L )
            return ( -1L );
        
        return ( updateTimestamp - lastUpdateTimestamp );
    }
    
    /**
     * Gets, whether the last update of these data has been done while in running session resp. realtime mode.
     * @return whether the last update of these data has been done while in running session resp. realtime mode.
     */
    public final boolean isUpdatedInTimeScope()
    {
        return ( updatedInTimeScope );
    }
    
    /**
     * This is incremented every time the info is updated.
     *  
     * @return the current update id.
     */
    public final long getUpdateId()
    {
        return ( data.getUpdateId() );
    }
    
    void loadFromStream( InputStream in, boolean isEditorMode ) throws IOException
    {
        prepareDataUpdate();
        
        data.loadFromStream( in );
        
        onDataUpdated( isEditorMode );
    }
    
    void setEngineBoostMapping( int boost )
    {
        this.engineBoostMapping = boost;
        this.tempBoostFlag = false;
        this.engineMaxRPM = gameData.getPhysics().getEngine().getMaxRPM( engineBaseMaxRPM );
    }
    
    void incEngineBoostMapping( Engine engine )
    {
        this.engineBoostMapping = Math.min( engineBoostMapping + 1, (int)engine.getBoostRange().getMaxValue() );
    }
    
    void decEngineBoostMapping( Engine engine )
    {
        this.engineBoostMapping = Math.max( (int)engine.getBoostRange().getMinValue(), engineBoostMapping - 1 );
    }
    
    void setTempBoostFlag( boolean tempBoostFlag )
    {
        this.tempBoostFlag = tempBoostFlag;
    }
    
    /**
     * Is temporary boost enabled?
     * 
     * @return whether temporary boost is enabled.
     */
    public final boolean getTemporaryBoostFlag()
    {
        return ( tempBoostFlag );
    }
    
    /**
     * Gets the currently selected engine boost mapping.
     * 
     * @return the currently selected engine boost mapping.
     */
    public final int getEngineBoostMapping()
    {
        return ( engineBoostMapping );
    }
    
    /**
     * If temp boost is activated, this returns the maximum (highest valued) boost mapping,
     * otherwise it returns the result of {@link #getEngineBoostMapping()}.
     * 
     * @return the effective engine boost mapping.
     */
    public final int getEffectiveEngineBoostMapping()
    {
        if ( tempBoostFlag )
            return ( (int)gameData.getPhysics().getEngine().getBoostRange().getMaxValue() );
        
        return ( engineBoostMapping );
    }
    
    /**
     * Gets the currently remaining engine's lifetime in seconds.
     * When you enter the cockpit the value will be the result of
     * gameData.getPhysics().getEngine().getSafeLifetimeTotal( gameData.getScoringInfo().getRaceLengthPercentage() );
     * 
     * @return the currently remaining engine's lifetime in seconds.
     */
    public final float getEngineLifetime()
    {
        return ( engineLifetime );
    }
    
    /**
     * Gets the current brake disc thickness in meters.
     * 
     * @param wheel
     * 
     * @return the current brake disc thickness in meters.
     */
    public final float getBrakeDiscThicknessM( Wheel wheel )
    {
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( brakeDiscThicknessFL );
            case FRONT_RIGHT:
                return ( brakeDiscThicknessFR );
            case REAR_LEFT:
                return ( brakeDiscThicknessRL );
            case REAR_RIGHT:
                return ( brakeDiscThicknessRR );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * Gets the current brake disc thickness in inch.
     * 
     * @param wheel
     * 
     * @return the current brake disc thickness in inch.
     */
    public final float getBrakeDiscThicknessIn( Wheel wheel )
    {
        return ( getBrakeDiscThicknessM( wheel ) * Convert.M_TO_INCH );
    }
    
    /**
     * Gets the current brake disc thickness in the units selected in the PLR.
     * 
     * @param wheel
     * 
     * @return the current brake disc thickness in the selected units.
     */
    public final float getBrakeDiscThickness( Wheel wheel )
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getBrakeDiscThicknessIn( wheel ) );
        
        return ( getBrakeDiscThicknessM( wheel ) );
    }
    
    /**
     * Gets the fuel usage of the last (timed) lap in liters.
     * 
     * @return the fuel usage of the last (timed) lap in liters.
     */
    public final float getFuelUsageLastLapL()
    {
        return ( fuelUsageLastLap );
    }
    
    /**
     * Gets the fuel usage of the last (timed) lap in gallons.
     * 
     * @return the fuel usage of the last (timed) lap in gallons.
     */
    public final float getFuelUsageLastLapGal()
    {
        return ( fuelUsageLastLap * Convert.LITERS_TO_GALONS );
    }
    
    /**
     * Gets the fuel usage of the last (timed) lap in the units selected in the PLR.
     * 
     * @return the fuel usage of the last (timed) lap in the selected units.
     */
    public final float getFuelUsageLastLap()
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getFuelUsageLastLapGal() );
        
        return ( getFuelUsageLastLapL() );
    }
    
    /**
     * Gets the average fuel usage of all recorded (timed) laps in liters.
     * 
     * @return the average fuel usage of all recorded (timed) laps in liters.
     */
    public final float getFuelUsageAverageL()
    {
        return ( fuelUsageAverage );
    }
    
    /**
     * Gets the average fuel usage of all recorded (timed) laps in gallons.
     * 
     * @return the average fuel usage of all recorded (timed) laps in gallons.
     */
    public final float getFuelUsageAverageGal()
    {
        return ( fuelUsageAverage * Convert.LITERS_TO_GALONS );
    }
    
    /**
     * Gets the average fuel usage of all recorded (timed) laps in the units selected in the PLR.
     * 
     * @return the average fuel usage of all recorded (timed) laps in the units selected in the PLR.
     */
    public final float getFuelUsageAverage()
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getFuelUsageAverageGal() );
        
        return ( getFuelUsageAverageL() );
    }
    
    /**
     * Gets the time since last update (seconds).
     * 
     * @return the time since last update (seconds).
     */
    public final float getDeltaTime()
    {
        return ( data.getDeltaTime() );
    }
    
    /*
     * Gets the current lap number.
     * 
     * @return the current lap number.
     */
    /*
    public final int getCurrentLapNumber()
    {
        return ( data.getCurrentLapNumber() );
    }
    */
    
    /*
     * Gets the time this lap was started at.
     * 
     * @return the time this lap was started at.
     */
    /*
    public final float getLapStartTime()
    {
        return ( data.getLapStartET() );
    }
    */
    
    /*
     * Gets the current vehicle name.
     * 
     * @return the current vehicle name.
     */
    /*
    public final String getVehicleName()
    {
        return ( data.getVehicleName() );
    }
    */
    
    /*
     * Gets the current track name.
     * 
     * @return the current track name.
     */
    /*
    public final String getTrackName()
    {
        return ( data.getTrackName() );
    }
    */
    
    /**
     * Gets world position in meters.
     * 
     * @param position output buffer
     * 
     * @return the output buffer back again.
     */
    public final TelemVect3 getPosition( TelemVect3 position )
    {
        return ( data.getPosition( position ) );
    }
    
    /**
     * Gets world position in meters.
     * 
     * @return world position in meters.
     */
    public final float getPositionX()
    {
        return ( data.getPositionX() );
    }
    
    /**
     * Gets world position in meters.
     * 
     * @return world position in meters.
     */
    public final float getPositionY()
    {
        return ( data.getPositionY() );
    }
    
    /**
     * Gets world position in meters.
     * 
     * @return world position in meters.
     */
    public final float getPositionZ()
    {
        return ( data.getPositionZ() );
    }
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
     * 
     * @param localVel output buffer
     * 
     * @return the output buffer back again.
     */
    public final TelemVect3 getLocalVelocity( TelemVect3 localVel )
    {
        return ( data.getLocalVelocity( localVel ) );
    }
    
    /**
     * Gets the velocity (meters/sec).
     * 
     * @return the velocity (meters/sec).
     */
    public final float getScalarVelocityMPS()
    {
        return ( data.getScalarVelocity() );
    }
    
    /**
     * Gets the velocity (mph).
     * 
     * @return the velocity (mph).
     */
    public final float getScalarVelocityMPH()
    {
        float mps = getScalarVelocityMPS();
        
        return ( mps * SpeedUnits.Convert.MPS_TO_MPH );
    }
    
    /**
     * Gets the velocity (km/h).
     * 
     * @return the velocity (km/h).
     */
    public final float getScalarVelocityKPH()
    {
        float mps = getScalarVelocityMPS();
        
        return ( mps * SpeedUnits.Convert.MPS_TO_KPH );
    }
    
    /**
     * Gets the velocity in the units selected in the PLR.
     * 
     * @return the velocity in the units selected in the PLR.
     */
    public final float getScalarVelocity()
    {
        if ( gameData.getProfileInfo().getSpeedUnits() == SpeedUnits.MPH )
            return ( getScalarVelocityMPH() );
        
        return ( getScalarVelocityKPH() );
    }
    
    /**
     * Gets the acceleration (meters/sec^2) in local vehicle coordinates.
     * 
     * @param localAccel output buffer
     * 
     * @return the output buffer back again.
     */
    public final TelemVect3 getLocalAcceleration( TelemVect3 localAccel )
    {
        return ( data.getLocalAcceleration( localAccel ) );
    }
    
    /**
     * Gets longitudinal acceleration (meters/sec^2).
     * 
     * @return longitudinal acceleration (meters/sec^2).
     */
    public final float getLongitudinalAcceleration()
    {
        return ( data.getLongitudinalAcceleration() );
    }
    
    /**
     * Gets the lateral acceleration (meters/sec^2).
     * 
     * @return the lateral acceleration (meters/sec^2).
     */
    public final float getLateralAcceleration()
    {
        return ( data.getLateralAcceleration() );
    }
    
    /**
     * top row of orientation matrix
     * 
     * (also converts local vehicle vectors into world X using dot product)
     * 
     * @param oriX output buffer
     * 
     * @return the output buffer back again.
     */
    public final TelemVect3 getOrientationX( TelemVect3 oriX )
    {
        return ( data.getOrientationX( oriX ) );
    }
    
    /**
     * mid row of orientation matrix
     * 
     * (also converts local vehicle vectors into world Y using dot product)
     * 
     * @param oriY output buffer
     * 
     * @return the output buffer back again.
     */
    public final TelemVect3 getOrientationY( TelemVect3 oriY )
    {
        return ( data.getOrientationY( oriY ) );
    }
    
    /**
     * bot row of orientation matrix
     * 
     * (also converts local vehicle vectors into world Z using dot product)
     * 
     * @param oriZ output buffer
     * 
     * @return the output buffer back again.
     */
    public final TelemVect3 getOrientationZ( TelemVect3 oriZ )
    {
        return ( data.getOrientationZ( oriZ ) );
    }
    
    /**
     * rotation (radians/sec) in local vehicle coordinates
     * 
     * @param localRot output buffer
     * 
     * @return the output buffer back again.
     */
    public final TelemVect3 getLocalRotation( TelemVect3 localRot )
    {
        return ( data.getLocalRotation( localRot ) );
    }
    
    /**
     * rotational acceleration (radians/sec^2) in local vehicle coordinates
     * 
     * @param localRotAccel output buffer
     * 
     * @return the output buffer back again.
     */
    public final TelemVect3 getLocalRotationalAcceleration( TelemVect3 localRotAccel )
    {
        return ( data.getLocalRotationalAcceleration( localRotAccel ) );
    }
    
    /**
     * Gets the current gear (-1=reverse, 0=neutral, 1+=forward gears).
     * 
     * @return the current gear.
     */
    public final short getCurrentGear()
    {
        return ( data.getCurrentGear() );
    }
    
    /**
     * Gets the current engine RPM.
     * 
     * @return the current engine RPM.
     */
    public final float getEngineRPM()
    {
        if ( engineRPM >= 0 )
            return ( engineRPM );
        
        return ( data.getEngineRPM() );
    }
    
    /**
     * Gets the current engine water temperature in Celsius.
     * 
     * @return the current engine water temperature in Celsius.
     */
    public final float getEngineWaterTemperatureC()
    {
        return ( data.getEngineWaterTemperature() );
    }
    
    /**
     * Gets the current engine water temperature in Fahrenheit.
     * 
     * @return the current engine water temperature in Fahrenheit.
     */
    public final float getEngineWaterTemperatureF()
    {
        return ( Convert.celsius2Fahrehheit( getEngineWaterTemperatureC() ) );
    }
    
    /**
     * Gets the current engine water temperature in the units selected in the PLR.
     * 
     * @return the current engine water temperature in the units selected in the PLR.
     */
    public final float getEngineWaterTemperature()
    {
        switch ( gameData.getProfileInfo().getMeasurementUnits() )
        {
            case IMPERIAL:
                return ( getEngineWaterTemperatureF() );
            case METRIC:
            default:
                return ( getEngineWaterTemperatureC() );
        }
    }
    
    /**
     * Gets the current engine oil temperature in Celsius.
     * 
     * @return the current engine oil temperature in Celsius.
     */
    public final float getEngineOilTemperatureC()
    {
        return ( data.getEngineOilTemperature() );
    }
    
    /**
     * Gets the current engine oil temperature in Fahrenheit.
     * 
     * @return the current engine oil temperature in Fahrenheit.
     */
    public final float getEngineOilTemperatureF()
    {
        return ( Convert.celsius2Fahrehheit( getEngineOilTemperatureC() ) );
    }
    
    /**
     * Gets the current engine oil temperature in the units selected in the PLR.
     * 
     * @return the current engine oil temperature in the units selected in the PLR.
     */
    public final float getEngineOilTemperature()
    {
        switch ( gameData.getProfileInfo().getMeasurementUnits() )
        {
            case IMPERIAL:
                return ( getEngineOilTemperatureF() );
            case METRIC:
            default:
                return ( getEngineOilTemperatureC() );
        }
    }
    
    /**
     * Gets the current clutch RPM.
     * 
     * @return the current clutch RPM.
     */
    public final float getClutchRPM()
    {
        return ( data.getClutchRPM() );
    }
    
    /**
     * Get the current unfiltered throttle application [0.0,1.0].
     * 
     * @return the current unfiltered throttle application [0.0,1.0].
     */
    public final float getUnfilteredThrottle()
    {
        return ( data.getUnfilteredThrottle() );
    }
    
    /**
     * Get the current unfiltered brake application [0.0,1.0].
     * 
     * @return the current unfiltered brake application [0.0,1.0].
     */
    public final float getUnfilteredBrake()
    {
        return ( data.getUnfilteredBrake() );
    }
    
    /**
     * Get the current unfiltered clutch application [0.0,1.0].
     * 
     * @return the current unfiltered clutch application [0.0,1.0].
     */
    public final float getUnfilteredClutch()
    {
        return ( data.getUnfilteredClutch() );
    }
    
    /**
     * Get the current unfiltered steering application [-1.0,1.0] (left to right).
     * 
     * @return the current unfiltered steering application [-1.0,1.0] (left to right).
     */
    public final float getUnfilteredSteering()
    {
        return ( data.getUnfilteredSteering() );
    }
    
    /**
     * Gets the force on steering arms.
     * 
     * @return the force on steering arms.
     */
    public final float getSteeringArmForce()
    {
        return ( data.getSteeringArmForce() );
    }
    
    /**
     * Gets the amount of fuel (liters).
     * 
     * @return the amount of fuel (liters).
     */
    public final float getFuelL()
    {
        if ( fuelLoad >= 0f )
            return ( fuelLoad );
        
        return ( data.getFuel() );
    }
    
    /**
     * Gets the amount of fuel (galons).
     * 
     * @return the amount of fuel (galons).
     */
    public final float getFuelGal()
    {
        if ( fuelLoad >= 0f )
            return ( fuelLoad * Convert.LITERS_TO_GALONS );
        
        return ( getFuelL() * Convert.LITERS_TO_GALONS );
    }
    
    /**
     * Gets the amount of fuel (units selected in the PLR).
     * 
     * @return the amount of fuel (units selected in the PLR).
     */
    public final float getFuel()
    {
        switch ( gameData.getProfileInfo().getMeasurementUnits() )
        {
            case IMPERIAL:
                return ( getFuelGal() );
            case METRIC:
            default:
                return ( getFuelL() );
        }
    }
    
    /**
     * Gets the current engine rev limit (base as reported by plugin interface).
     * 
     * @return the current engine rev limit (base as reported by plugin interface).
     */
    public final float getEngineBaseMaxRPM()
    {
        return ( engineBaseMaxRPM );
    }
    
    /**
     * Gets the engine rev limit with max boost.
     * 
     * @return the engine rev limit with max boost.
     */
    public final float getEngineMaxRPM()
    {
        return ( engineMaxRPM );
    }
    
    /*
     * Gets the number of scheduled pitstops.
     * 
     * @return the number of scheduled pitstops.
     */
    /*
    public short getNumberOfScheduledPitstops()
    {
        return ( data.getNumberOfScheduledPitstops() );
    }
    */
    
    /**
     * Gets whether overheating icon is shown.
     * 
     * @return whether overheating icon is shown.
     */
    public final boolean isOverheating()
    {
        return ( data.isOverheating() );
    }
    
    /**
     * Gets whether any parts (besides wheels) have been detached.
     * 
     * @return whether any parts (besides wheels) have been detached
     */
    public final boolean isAnythingDetached()
    {
        return ( data.isAnythingDetached() );
    }
    
    /**
     * Gets dent severity at 8 locations around the car (0=none, 1=some, 2=more).
     * 
     * @return dent severity at 8 locations around the car.
     */
    public final short[] getDentSevirity()
    {
        return ( data.getDentSevirity() );
    }
    
    /**
     * Gets the time of last impact.
     * 
     * @return the time of last impact.
     */
    public final float getLastImpactTime()
    {
        return ( data.getLastImpactTime() );
    }
    
    /**
     * Gets the magnitude of last impact.
     * 
     * @return the magnitude of last impact.
     */
    public final float getLastImpactMagnitude()
    {
        return ( data.getLastImpactMagnitude() );
    }
    
    /**
     * location of last impact
     * 
     * @param lastImpactPos output buffer
     * 
     * @return the output buffer back again.
     */
    public final TelemVect3 getLastImpactPosition( TelemVect3 lastImpactPos )
    {
        return ( data.getLastImpactPosition( lastImpactPos ) );
    }
    
    /**
     * Gets the curent wheel rotation in radians/sec.
     * 
     * @param wheel the queried wheel
     * 
     * @return the curent wheel rotation in radians/sec.
     */
    public final float getWheelRotation( Wheel wheel )
    {
        return ( data.getWheelRotation( wheel ) );
    }
    
    /**
     * Gets the current suspension deflection in meters.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current suspension deflection in meters.
     */
    public final float getWheelSuspensionDeflectionM( Wheel wheel )
    {
        return ( data.getWheelSuspensionDeflection( wheel ) );
    }
    
    /**
     * Gets the current suspension deflection in inches.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current suspension deflection in inches.
     */
    public final float getWheelSuspensionDeflectionIn( Wheel wheel )
    {
        return ( data.getWheelSuspensionDeflection( wheel ) * Convert.M_TO_INCH );
    }
    
    /**
     * Gets the current suspension deflection in the units selected in the PLR.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current suspension deflection in the units selected in the PLR.
     */
    public final float getWheelSuspensionDeflection( Wheel wheel )
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getWheelSuspensionDeflectionIn( wheel ) );
        
        return ( getWheelSuspensionDeflectionM( wheel ) );
    }
    
    /**
     * Gets the current ride height in meters.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current ride height in meters.
     */
    public final float getRideHeightM( Wheel wheel )
    {
        return ( data.getRideHeight( wheel ) );
    }
    
    /**
     * Gets the current ride height in inches.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current ride height in inches.
     */
    public final float getRideHeightIn( Wheel wheel )
    {
        return ( data.getRideHeight( wheel ) * Convert.M_TO_INCH );
    }
    
    /**
     * Gets the current ride height in the units selected in the PLR.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current ride height in the units selected in the PLR
     */
    public final float getRideHeight( Wheel wheel )
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getRideHeightIn( wheel ) );
        
        return ( getRideHeightM( wheel ) );
    }
    
    /**
     * Gets the current tire load in Newtons.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current tire load in Newtons.
     */
    public final float getTireLoadN( Wheel wheel )
    {
        return ( data.getTireLoad( wheel ) );
    }
    
    /**
     * Gets the current tire load in LBS.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current tire load in LBS.
     */
    public final float getTireLoadLBS( Wheel wheel )
    {
        return ( data.getTireLoad( wheel ) * Convert.N_TO_LBS );
    }
    
    /**
     * Gets the current tire load in the units selected in the PLR.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current tire load in the selected units.
     */
    public final float getTireLoad( Wheel wheel )
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getTireLoadLBS( wheel ) );
        
        return ( getTireLoadN( wheel ) );
    }
    
    /**
     * Gets the current lateral force in Newtons.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current lateral force in Newtons.
     */
    public final float getLateralForce( Wheel wheel )
    {
        return ( data.getLateralForce( wheel ) );
    }
    
    /**
     * Gets an approximation of what fraction of the contact patch is sliding.
     * 
     * @param wheel the queried wheel
     * 
     * @return an approximation of what fraction of the contact patch is sliding.
     */
    public final float getGripFraction( Wheel wheel )
    {
        return ( data.getGripFraction( wheel ) );
    }
    
    /**
     * Gets the current brake temperature in Kelvin.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current brake temperature in Kelvin.
     */
    public final float getBrakeTemperatureK( Wheel wheel )
    {
        return ( data.getBrakeTemperature( wheel ) );
    }
    
    /**
     * Gets the current brake temperature in Celsius.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current brake temperature in Celsius.
     */
    public final float getBrakeTemperatureC( Wheel wheel )
    {
        // float mBrakeTemp
        
        return ( getBrakeTemperatureK( wheel ) + Convert.ZERO_KELVIN );
    }
    
    /**
     * Gets the current brake temperature in Fahrenheit.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current brake temperature in Fahrenheit.
     */
    public final float getBrakeTemperatureF( Wheel wheel )
    {
        // float mBrakeTemp
        
        return ( Convert.celsius2Fahrehheit( getBrakeTemperatureC( wheel ) ) );
    }
    
    /**
     * Gets the current brake temperature in the units selected in the PLR.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current brake temperature in the units selected in the PLR.
     */
    public final float getBrakeTemperature( Wheel wheel )
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getBrakeTemperatureF( wheel ) );
        
        return ( getBrakeTemperatureC( wheel ) );
    }
    
    /**
     * Gets the current tire pressure in kPa.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current tire pressure in kPa.
     */
    public final float getTirePressureKPa( Wheel wheel )
    {
        return ( data.getTirePressure( wheel ) );
    }
    
    /**
     * Gets the current tire pressure in PSI.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current tire pressure in PSI.
     */
    public final float getTirePressurePSI( Wheel wheel )
    {
        return ( data.getTirePressure( wheel ) * Convert.KPA_TO_PSI );
    }
    
    /**
     * Gets the current tire pressure in the units selected in the PLR.
     * 
     * @param wheel the queried wheel
     * 
     * @return the current tire pressure in the selected units.
     */
    public final float getTirePressure( Wheel wheel )
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getTirePressurePSI( wheel ) );
        
        return ( getTirePressureKPa( wheel ) );
    }
    
    /**
     * Gets the current tire temperature in Celsius.
     * 
     * @param wheel the queried wheel
     * @param part the wheel part
     * 
     * @return the current tire temperature in Celsius.
     */
    public final float getTireTemperatureC( Wheel wheel, WheelPart part )
    {
        return ( data.getTireTemperature( wheel, part ) + Convert.ZERO_KELVIN );
    }
    
    /**
     * Gets the current tire temperature in Fahrenheit.
     * 
     * @param wheel the queried wheel
     * @param part the wheel part
     * 
     * @return the current tire temperature in Fahrenheit.
     */
    public final float getTireTemperatureF( Wheel wheel, WheelPart part )
    {
        return ( Convert.celsius2Fahrehheit( getTireTemperatureC( wheel, part ) ) );
    }
    
    /**
     * Gets the current tire temperature in the units selected in the PLR (Celsius or Fahrenheit).
     * 
     * @param wheel the queried wheel
     * @param part the wheel part
     * 
     * @return the current tire temperature in the units selected in the PLR.
     */
    public final float getTireTemperature( Wheel wheel, WheelPart part )
    {
        switch ( gameData.getProfileInfo().getMeasurementUnits() )
        {
            case IMPERIAL:
                return ( getTireTemperatureF( wheel, part ) );
            case METRIC:
            default:
                return ( getTireTemperatureC( wheel, part ) );
        }
    }
    
    /**
     * Gets the current tire temperature in Celsius : ( (INSIDE + CENTER + OUTSIDE) / 3 )
     * 
     * @param wheel the queried wheel
     * 
     * @return the current tire temperature in Celsius.
     */
    public final float getTireTemperatureC( Wheel wheel )
    {
        float inside = getTireTemperatureC( wheel, WheelPart.INSIDE );
        float center = getTireTemperatureC( wheel, WheelPart.CENTER );
        float outside = getTireTemperatureC( wheel, WheelPart.OUTSIDE );
        
        return ( ( inside + center + outside ) / 3f );
    }
    
    /**
     * Gets the current tire temperature in Fahrenheit : ( (INSIDE + CENTER + OUTSIDE) / 3 )
     * 
     * @param wheel the queried wheel
     * 
     * @return the current tire temperature in Fahrenheit.
     */
    public final float getTireTemperatureF( Wheel wheel )
    {
        float inside = getTireTemperatureF( wheel, WheelPart.INSIDE );
        float center = getTireTemperatureF( wheel, WheelPart.CENTER );
        float outside = getTireTemperatureF( wheel, WheelPart.OUTSIDE );
        
        return ( ( inside + center + outside ) / 3f );
    }
    
    /**
     * Gets the current tire temperature in the units selected in the PLR (Celsius or Fahrenheit) : ( (INSIDE + CENTER + OUTSIDE) / 3 )
     * 
     * @param wheel the queried wheel
     * 
     * @return the current tire temperature in the units selected in the PLR
     */
    public final float getTireTemperature( Wheel wheel )
    {
        switch ( gameData.getProfileInfo().getMeasurementUnits() )
        {
            case IMPERIAL:
                return ( getTireTemperatureF( wheel ) );
            case METRIC:
            default:
                return ( getTireTemperatureC( wheel ) );
        }
    }
    
    /**
     * Gets the hottest wheel.
     * 
     * @return the hottest wheel.
     */
    public final Wheel getHottestWheel()
    {
        Wheel wheel = Wheel.FRONT_LEFT;
        float maxTemp = getTireTemperatureC( Wheel.FRONT_LEFT );
        
        float t = getTireTemperatureC( Wheel.FRONT_RIGHT );
        if ( t > maxTemp )
        {
            maxTemp = t;
            wheel = Wheel.FRONT_RIGHT;
        }
        
        t = getTireTemperatureC( Wheel.REAR_LEFT );
        if ( t > maxTemp )
        {
            maxTemp = t;
            wheel = Wheel.REAR_LEFT;
        }
        
        t = getTireTemperatureC( Wheel.REAR_RIGHT );
        if ( t > maxTemp )
        {
            maxTemp = t;
            wheel = Wheel.REAR_RIGHT;
        }
        
        return ( wheel );
    }
    
    // TelemWheelV2
    
    /**
     * Gets current tire wear (0.0-1.0, fraction of maximum) ... this is not necessarily proportional with grip loss.
     * 
     * @param wheel the queried wheel
     * 
     * @return current tire wear.
     */
    public final float getTireWear( Wheel wheel )
    {
        return ( data.getTireWear( wheel ) );
    }
    
    /**
     * Gets the material prefixes from the TDF file.
     * 
     * @param wheel the queried wheel
     * 
     * @return the material prefixes from the TDF file.
     */
    public final String getTerrainName( Wheel wheel )
    {
        return ( data.getTerrainName( wheel ) );
    }
    
    /**
     * Gets surface type under the tire.
     * 
     * @param wheel the queried wheel
     * 
     * @return surface type under the tire.
     */
    public final SurfaceType getSurfaceType( Wheel wheel )
    {
        return ( data.getSurfaceType( wheel ) );
    }
    
    /**
     * Gets whether tire is flat.
     * 
     * @param wheel the queried wheel
     * 
     * @return whether tire is flat.
     */
    public final boolean isWheelFlat( Wheel wheel )
    {
        return ( data.isWheelFlat( wheel ) );
    }
    
    /**
     * Gets whether wheel is detached.
     * 
     * @param wheel the queried wheel
     * 
     * @return whether wheel is detached.
     */
    public final boolean isWheelDetached( Wheel wheel )
    {
        return ( data.isWheelDetached( wheel ) );
    }
    
    // future use
    
    // unsigned char mExpansion[32];
    
    TelemetryData( LiveGameData gameData, _LiveGameDataObjectsFactory gdFactory )
    {
        this.gameData = gameData;
        this.data = gdFactory.newTelemetryDataCapsule( gameData );
        
        registerListener( TopspeedRecorder.MASTER_TOPSPEED_RECORDER );
        registerListener( LifetimeManager.INSTANCE );
    }
}
