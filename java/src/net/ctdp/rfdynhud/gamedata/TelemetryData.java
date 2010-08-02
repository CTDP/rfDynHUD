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

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.SpeedUnits;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.Engine;
import net.ctdp.rfdynhud.util.Logger;

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
    static final float ZERO_KELVIN = -273.15f;
    static final float FAHRENHEIT_OFFSET = 32.0f;
    static final float FAHRENHEIT_FACTOR = 1.8f;
    static final float MPS_TO_MPH = 2.237f;
    static final float MPS_TO_KPH = 3.6f; // 3600f / 1000f
    static final float LITERS_TO_GALONS = 0.26417287f;
    
    final TelemetryDataCapsule data = new TelemetryDataCapsule();
    
    private boolean updatedInTimeScope = false;
    private long updateId = 0L;
    private long lastUpdateTimestamp = -1L;
    private long updateTimestamp = -1L;
    
    private final LiveGameData gameData;
    
    private final GameEventsManager eventsManager;
    
    private float engineBaseMaxRPM = 1000.12345f;
    private float engineMaxRPM = 1000.12345f;
    
    private int engineBoostMapping = 5;
    private boolean tempBoostFlag = false;
    
    float engineLifetime = 0.0f;
    float brakeDiscThicknessFL = 0.0f;
    float brakeDiscThicknessFR = 0.0f;
    float brakeDiscThicknessRL = 0.0f;
    float brakeDiscThicknessRR = 0.0f;
    
    float fuelUsageLastLap = -1f;
    float fuelUsageAverage = -1f;
    
    public static interface TelemetryDataUpdateListener extends LiveGameData.GameDataUpdateListener
    {
        public void onTelemetryDataUpdated( LiveGameData gameData, EditorPresets editorPresets );
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
        
        gameData.registerListener( l );
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
        
        gameData.unregisterListener( l );
    }
    
    /**
     * 
     * @param editorPresets
     */
    void onSessionStarted( EditorPresets editorPresets )
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
        
        setEngineBoostMapping( editorPresets.getEngineBoost() );
    }
    
    void onDataUpdated( EditorPresets editorPresets )
    {
        try
        {
            this.updatedInTimeScope = gameData.isInRealtimeMode();
            this.updateId++;
            this.lastUpdateTimestamp = updateTimestamp;
            this.updateTimestamp = System.nanoTime();
            
            float bmr = data.getEngineMaxRPM();
            bmr = gameData.getPhysics().getEngine().getRevLimitRange().limitValue( bmr );
            
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
                        updateListeners[i].onTelemetryDataUpdated( gameData, editorPresets );
                    }
                    catch ( Throwable t )
                    {
                        Logger.log( t );
                    }
                }
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
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
    
    public final long getUpdateId()
    {
        return ( updateId );
    }
    
    void loadFromStream( InputStream in, EditorPresets editorPresets ) throws IOException
    {
        prepareDataUpdate();
        
        data.loadFromStream( in );
        
        onDataUpdated( editorPresets );
    }
    
    void setEngineBoostMapping( int boost )
    {
        this.engineBoostMapping = boost;
        this.tempBoostFlag = false;
        this.engineMaxRPM = gameData.getPhysics().getEngine().getMaxRPM( engineBaseMaxRPM );
    }
    
    void incEngineBoostMapping( Engine engine )
    {
        int oldValue = this.engineBoostMapping;
        this.engineBoostMapping = Math.min( engineBoostMapping + 1, (int)engine.getBoostRange().getMaxValue() );
        
        if ( engineBoostMapping != oldValue )
            eventsManager.onEngineBoostChanged( oldValue, engineBoostMapping, tempBoostFlag, tempBoostFlag );
    }
    
    void decEngineBoostMapping( Engine engine )
    {
        int oldValue = this.engineBoostMapping;
        this.engineBoostMapping = Math.max( (int)engine.getBoostRange().getMinValue(), engineBoostMapping - 1 );
        
        if ( engineBoostMapping != oldValue )
            eventsManager.onEngineBoostChanged( oldValue, engineBoostMapping, tempBoostFlag, tempBoostFlag );
    }
    
    void setTempBoostFlag( boolean tempBoostFlag )
    {
        boolean oldValue = this.tempBoostFlag;
        this.tempBoostFlag = tempBoostFlag;
        
        if ( tempBoostFlag != oldValue )
            eventsManager.onEngineBoostChanged( engineBoostMapping, engineBoostMapping, oldValue, tempBoostFlag );
    }
    
    public final boolean getTemporaryBoostFlag()
    {
        return ( tempBoostFlag );
    }
    
    public final int getEngineBoostMapping()
    {
        return ( engineBoostMapping );
    }
    
    public final int getEffectiveEngineBoostMapping()
    {
        if ( tempBoostFlag )
            return ( (int)gameData.getPhysics().getEngine().getBoostRange().getMaxValue() );
        
        return ( engineBoostMapping );
    }
    
    public final float getEngineLifetime()
    {
        return ( engineLifetime );
    }
    
    public final float getBrakeDiscThickness( Wheel wheel )
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
     * Gets the fuel usage of the last (timed) lap.
     * 
     * @return the fuel usage of the last (timed) lap.
     */
    public final float getFuelUsageLastLap()
    {
        return ( fuelUsageLastLap );
    }
    
    /**
     * Gets the average fuel usage of all recorded (timed) laps.
     * 
     * @return the average fuel usage of all recorded (timed) laps.
     */
    public final float getFuelUsageAverage()
    {
        return ( fuelUsageAverage );
    }
    
    /**
     * time since last update (seconds)
     */
    public final float getDeltaTime()
    {
        return ( data.getDeltaTime() );
    }
    
    /**
     * current lap number
     */
    public final int getCurrentLapNumber()
    {
        return ( data.getCurrentLapNumber() );
    }
    
    /**
     * time this lap was started
     */
    public final float getLapStartET()
    {
        return ( data.getLapStartET() );
    }
    
    /**
     * current vehicle name
     */
    public final String getVehicleName()
    {
        return ( data.getVehicleName() );
    }
    
    /**
     * current track name
     */
    public final String getTrackName()
    {
        return ( data.getTrackName() );
    }
    
    /**
     * world position in meters
     * 
     * @param position
     */
    public final TelemVect3 getPosition( TelemVect3 position )
    {
        return ( data.getPosition( position ) );
    }
    
    /**
     * world position in meters
     */
    public final float getPositionX()
    {
        return ( data.getPositionX() );
    }
    
    /**
     * world position in meters
     */
    public final float getPositionY()
    {
        return ( data.getPositionY() );
    }
    
    /**
     * world position in meters
     */
    public final float getPositionZ()
    {
        return ( data.getPositionZ() );
    }
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
     * 
     * @param localVel
     */
    public final TelemVect3 getLocalVelocity( TelemVect3 localVel )
    {
        return ( data.getLocalVelocity( localVel ) );
    }
    
    /**
     * velocity (meters/sec)
     */
    public final float getScalarVelocityMPS()
    {
        return ( data.getScalarVelocity() );
    }
    
    /**
     * velocity (mph)
     */
    public final float getScalarVelocityMPH()
    {
        float mps = getScalarVelocityMPS();
        
        return ( mps * MPS_TO_MPH );
    }
    
    /**
     * velocity (km/h)
     */
    public final float getScalarVelocityKPH()
    {
        float mps = getScalarVelocityMPS();
        
        return ( mps * MPS_TO_KPH );
    }
    
    /**
     * velocity in the units selected in the PLR.
     */
    public final float getScalarVelocity()
    {
        if ( gameData.getProfileInfo().getSpeedUnits() == SpeedUnits.MPH )
            return ( getScalarVelocityMPH() );
        
        return ( getScalarVelocityKPH() );
    }
    
    /**
     * acceleration (meters/sec^2) in local vehicle coordinates
     * 
     * @param localAccel
     */
    public final TelemVect3 getLocalAcceleration( TelemVect3 localAccel )
    {
        return ( data.getLocalAcceleration( localAccel ) );
    }
    
    /**
     * longitudinal acceleration (meters/sec^2)
     */
    public final float getLongitudinalAcceleration()
    {
        return ( data.getLongitudinalAcceleration() );
    }
    
    /**
     * lateral acceleration (meters/sec^2)
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
     * @param oriX
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
     * @param oriY
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
     * @param oriZ
     */
    public final TelemVect3 getOrientationZ( TelemVect3 oriZ )
    {
        return ( data.getOrientationZ( oriZ ) );
    }
    
    /**
     * rotation (radians/sec) in local vehicle coordinates
     * 
     * @param localRot
     */
    public final TelemVect3 getLocalRotation( TelemVect3 localRot )
    {
        return ( data.getLocalRotation( localRot ) );
    }
    
    /**
     * rotational acceleration (radians/sec^2) in local vehicle coordinates
     * 
     * @param localRotAccel
     */
    public final TelemVect3 getLocalRotationalAcceleration( TelemVect3 localRotAccel )
    {
        return ( data.getLocalRotationalAcceleration( localRotAccel ) );
    }
    
    /**
     * -1=reverse, 0=neutral, 1+=forward gears
     */
    public final short getCurrentGear()
    {
        return ( data.getCurrentGear() );
    }
    
    /**
     * engine RPM
     */
    public final float getEngineRPM()
    {
        return ( data.getEngineRPM() );
    }
    
    /**
     * Celsius
     */
    public final float getEngineWaterTemperatureC()
    {
        return ( data.getEngineWaterTemperature() );
    }
    
    /**
     * Fahrenheit
     */
    public final float getEngineWaterTemperatureF()
    {
        return ( FAHRENHEIT_OFFSET + getEngineWaterTemperatureC() * FAHRENHEIT_FACTOR );
    }
    
    /**
     * Selected units
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
     * Celsius
     */
    public final float getEngineOilTemperatureC()
    {
        return ( data.getEngineOilTemperature() );
    }
    
    /**
     * Fahrenheit
     */
    public final float getEngineOilTemperatureF()
    {
        return ( FAHRENHEIT_OFFSET + getEngineOilTemperatureC() * FAHRENHEIT_FACTOR );
    }
    
    /**
     * Selected units
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
     * clutch RPM
     */
    public final float getClutchRPM()
    {
        return ( data.getClutchRPM() );
    }
    
    /**
     * ranges  0.0-1.0
     */
    public final float getUnfilteredThrottle()
    {
        return ( data.getUnfilteredThrottle() );
    }
    
    /**
     * ranges  0.0-1.0
     */
    public final float getUnfilteredBrake()
    {
        return ( data.getUnfilteredBrake() );
    }
    
    /**
     * ranges -1.0-1.0 (left to right)
     */
    public final float getUnfilteredSteering()
    {
        return ( data.getUnfilteredSteering() );
    }
    
    /**
     * ranges  0.0-1.0
     */
    public final float getUnfilteredClutch()
    {
        return ( data.getUnfilteredClutch() );
    }
    
    /**
     * force on steering arms
     */
    public final float getSteeringArmForce()
    {
        return ( data.getSteeringArmForce() );
    }
    
    /**
     * amount of fuel (liters)
     */
    public final float getFuelL()
    {
        return ( data.getFuel() );
    }
    
    /**
     * amount of fuel (galons)
     */
    public final float getFuelGal()
    {
        return ( getFuelL() * LITERS_TO_GALONS );
    }
    
    /**
     * amount of fuel in the units selected in the PLR.
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
     * rev limit (base as reported by plugin interface)
     */
    public final float getEngineBaseMaxRPM()
    {
        return ( engineBaseMaxRPM );
    }
    
    /**
     * rev limit with max boost
     */
    public final float getEngineMaxRPM()
    {
        return ( engineMaxRPM );
    }
    
    /**
     * number of scheduled pitstops
     */
    public short getNumberOfScheduledPitstops()
    {
        return ( data.getNumberOfScheduledPitstops() );
    }
    
    /**
     * whether overheating icon is shown
     */
    public final boolean isOverheating()
    {
        return ( data.isOverheating() );
    }
    
    /**
     * whether any parts (besides wheels) have been detached
     */
    public final boolean isAnythingDetached()
    {
        return ( data.isAnythingDetached() );
    }
    
    /**
     * dent severity at 8 locations around the car (0=none, 1=some, 2=more)
     */
    public final short[] getDentSevirity()
    {
        return ( data.getDentSevirity() );
    }
    
    /**
     * time of last impact
     */
    public final float getLastImpactTime()
    {
        return ( data.getLastImpactTime() );
    }
    
    /**
     * magnitude of last impact
     */
    public final float getLastImpactMagnitude()
    {
        return ( data.getLastImpactMagnitude() );
    }
    
    /**
     * location of last impact
     * 
     * @param lastImpactPos
     */
    public final TelemVect3 getLastImpactPosition( TelemVect3 lastImpactPos )
    {
        return ( data.getLastImpactPosition( lastImpactPos ) );
    }
    
    /**
     * radians/sec
     */
    public final float getWheelRotation( Wheel wheel )
    {
        return ( data.getWheelRotation( wheel ) );
    }
    
    /**
     * meters
     */
    public final float getWheelSuspensionDeflection( Wheel wheel )
    {
        return ( data.getWheelSuspensionDeflection( wheel ) );
    }
    
    /**
     * meters
     */
    public final float getRideHeight( Wheel wheel )
    {
        return ( data.getRideHeight( wheel ) );
    }
    
    /**
     * Newtons
     */
    public final float getTireLoad( Wheel wheel )
    {
        return ( data.getTireLoad( wheel ) );
    }
    
    /**
     * Newtons
     */
    public final float getLateralForce( Wheel wheel )
    {
        return ( data.getLateralForce( wheel ) );
    }
    
    /**
     * an approximation of what fraction of the contact patch is sliding
     */
    public final float getGripFraction( Wheel wheel )
    {
        return ( data.getGripFraction( wheel ) );
    }
    
    /**
     * Kelvin
     */
    public final float getBrakeTemperatureK( Wheel wheel )
    {
        return ( data.getBrakeTemperature( wheel ) );
    }
    
    /**
     * Celsius
     */
    public final float getBrakeTemperatureC( Wheel wheel )
    {
        // float mBrakeTemp
        
        return ( getBrakeTemperatureK( wheel ) + ZERO_KELVIN );
    }
    
    /**
     * Fahrenheit
     */
    public final float getBrakeTemperatureF( Wheel wheel )
    {
        // float mBrakeTemp
        
        return ( FAHRENHEIT_OFFSET + getBrakeTemperatureC( wheel ) * FAHRENHEIT_FACTOR );
    }
    
    /**
     * Selected units
     */
    public final float getBrakeTemperature( Wheel wheel )
    {
        switch ( gameData.getProfileInfo().getMeasurementUnits() )
        {
            case IMPERIAL:
                return ( getBrakeTemperatureF( wheel ) );
            case METRIC:
            default:
                return ( getBrakeTemperatureC( wheel ) );
        }
    }
    
    /**
     * kPa
     */
    public final float getTirePressure( Wheel wheel )
    {
        return ( data.getTirePressure( wheel ) );
    }
    
    /**
     * Celsius
     */
    public final float getTireTemperatureC( Wheel wheel, WheelPart part )
    {
        return ( data.getTireTemperature( wheel, part ) + ZERO_KELVIN );
    }
    
    /**
     * Fahrenheit
     */
    public final float getTireTemperatureF( Wheel wheel, WheelPart part )
    {
        return ( FAHRENHEIT_OFFSET + getTireTemperatureC( wheel, part ) * FAHRENHEIT_FACTOR );
    }
    
    /**
     * Selected units (Celsius or Fahrenheit)
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
     * Celsius : ( (INSIDE + CENTER + OUTSIDE) / 3 )
     */
    public final float getTireTemperatureC( Wheel wheel )
    {
        float inside = getTireTemperatureC( wheel, WheelPart.INSIDE );
        float center = getTireTemperatureC( wheel, WheelPart.CENTER );
        float outside = getTireTemperatureC( wheel, WheelPart.OUTSIDE );
        
        return ( ( inside + center + outside ) / 3f );
    }
    
    /**
     * Fahrenheit : ( (INSIDE + CENTER + OUTSIDE) / 3 )
     */
    public final float getTireTemperatureF( Wheel wheel )
    {
        float inside = getTireTemperatureF( wheel, WheelPart.INSIDE );
        float center = getTireTemperatureF( wheel, WheelPart.CENTER );
        float outside = getTireTemperatureF( wheel, WheelPart.OUTSIDE );
        
        return ( ( inside + center + outside ) / 3f );
    }
    
    /**
     * Selected units (Celsius or Fahrenheit) : ( (INSIDE + CENTER + OUTSIDE) / 3 )
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
     * wear (0.0-1.0, fraction of maximum) ... this is not necessarily proportional with grip loss
     */
    public final float getTireWear( Wheel wheel )
    {
        return ( data.getTireWear( wheel ) );
    }
    
    /**
     * the material prefixes from the TDF file
     */
    public final String getTerrainName( Wheel wheel )
    {
        return ( data.getTerrainName( wheel ) );
    }
    
    /**
     * surface under the wheel
     */
    public final SurfaceType getSurfaceType( Wheel wheel )
    {
        return ( data.getSurfaceType( wheel ) );
    }
    
    /**
     * whether tire is flat
     */
    public final boolean isWheelFlat( Wheel wheel )
    {
        return ( data.isWheelFlat( wheel ) );
    }
    
    /**
     * whether wheel is detached
     */
    public final boolean isWheelDetached( Wheel wheel )
    {
        return ( data.isWheelDetached( wheel ) );
    }
    
    // future use
    
    // unsigned char mExpansion[32];
    
    TelemetryData( LiveGameData gameData, GameEventsManager eventsManager )
    {
        this.gameData = gameData;
        this.eventsManager = eventsManager;
        
        registerListener( TopspeedRecorder.MASTER_TOPSPEED_RECORDER );
        registerListener( LifetimeManager.INSTANCE );
    }
}
