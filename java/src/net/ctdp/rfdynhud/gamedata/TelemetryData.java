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
 * @author Marvin Froehlich
 */
public class TelemetryData
{
    static final float ZERO_KELVIN = -273.15f;
    static final float FAHRENHEIT_OFFSET = 32.0f;
    static final float FAHRENHEIT_FACTOR = 1.8f;
    static final float MPS_TO_MPH = 2.237f;
    static final float MPS_TO_KPH = 3.6f; // 3600f / 1000f
    static final float LITERS_TO_GALONS = 0.26417287f;
    
    private static final int OFFSET_DELTA_TIME = 0;
    private static final int OFFSET_LAP_NUMBER = OFFSET_DELTA_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAP_START_TIME = OFFSET_LAP_NUMBER + ByteUtil.SIZE_LONG;
    private static final int OFFSET_VEHICLE_NAME = OFFSET_LAP_START_TIME + ByteUtil.SIZE_FLOAT;
    private static final int MAX_VEHICLE_NAME_LENGTH = 64;
    private static final int OFFSET_TRACK_NAME = OFFSET_VEHICLE_NAME + MAX_VEHICLE_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int MAX_TRACK_NAME_LENGTH = 64;
    
    private static final int OFFSET_POSITION = OFFSET_TRACK_NAME + MAX_TRACK_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LOCAL_VELOCITY = OFFSET_POSITION + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_LOCAL_ACCELERATION = OFFSET_LOCAL_VELOCITY + ByteUtil.SIZE_VECTOR3;
    
    private static final int OFFSET_ORIENTATION_X = OFFSET_LOCAL_ACCELERATION + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_ORIENTATION_Y = OFFSET_ORIENTATION_X + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_ORIENTATION_Z = OFFSET_ORIENTATION_Y + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_LOCAL_ROTATION = OFFSET_ORIENTATION_Z + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_LOCAL_ROTATION_ACCELERATION = OFFSET_LOCAL_ROTATION + ByteUtil.SIZE_VECTOR3;
    
    private static final int OFFSET_GEAR = OFFSET_LOCAL_ROTATION_ACCELERATION + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_ENGINE_RPM = OFFSET_GEAR + ByteUtil.SIZE_LONG;
    private static final int OFFSET_ENGINE_WATER_TEMP = OFFSET_ENGINE_RPM + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_ENGINE_OIL_TEMP = OFFSET_ENGINE_WATER_TEMP + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_CLUTCH_RPM = OFFSET_ENGINE_OIL_TEMP + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_UNFILTERED_THROTTLE = OFFSET_CLUTCH_RPM + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_UNFILTERED_BRAKE = OFFSET_UNFILTERED_THROTTLE + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_UNFILTERED_STEERING = OFFSET_UNFILTERED_BRAKE + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_UNFILTERED_CLUTCH = OFFSET_UNFILTERED_STEERING + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_STEERING_ARM_FORCE = OFFSET_UNFILTERED_CLUTCH + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_FUEL = OFFSET_STEERING_ARM_FORCE + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_ENGINE_MAX_RPM = OFFSET_FUEL + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_SCHEDULED_STOPS = OFFSET_ENGINE_MAX_RPM + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_OVERHEATING = OFFSET_SCHEDULED_STOPS + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_DETACHED = OFFSET_OVERHEATING + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_DENT_SEVERITY = OFFSET_DETACHED + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_LAST_IMPACT_TIME = OFFSET_DENT_SEVERITY + 8 * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LAST_IMPACT_MAGNITUDE = OFFSET_LAST_IMPACT_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAST_IMPACT_POSITION = OFFSET_LAST_IMPACT_MAGNITUDE + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_EXPANSION = OFFSET_LAST_IMPACT_POSITION + ByteUtil.SIZE_VECTOR3;
    
    private static final int OFFSET_WHEEL_DATA = OFFSET_EXPANSION + 64 * ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_WHEEL_ROTATION = 1;
    private static final int OFFSET_WHEEL_SUSPENSION_DEFLECTION = OFFSET_WHEEL_ROTATION + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_RIDE_HEIGHT = OFFSET_WHEEL_SUSPENSION_DEFLECTION + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_TIRE_LOAD = OFFSET_RIDE_HEIGHT + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LATERAL_FORCE = OFFSET_TIRE_LOAD + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_GRIP_FRACTION = OFFSET_LATERAL_FORCE + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_BRAKE_TEMP = OFFSET_GRIP_FRACTION + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_TIRE_PRESSURE = OFFSET_BRAKE_TEMP + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_TIRE_TEMPERATURES = OFFSET_TIRE_PRESSURE + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_TIRE_WEAR = OFFSET_TIRE_TEMPERATURES + 3 * ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_TERRAIN_NAME = OFFSET_TIRE_WEAR + ByteUtil.SIZE_FLOAT;
    private static final int MAX_TERRAIN_NAME_LENGTH = 16;
    private static final int OFFSET_SURFACE_TYPE = OFFSET_TERRAIN_NAME + MAX_TERRAIN_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_IS_WHEEL_FLAT = OFFSET_SURFACE_TYPE + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_IS_WHEEL_DETACHED = OFFSET_IS_WHEEL_FLAT + ByteUtil.SIZE_BOOL;
    
    private static final int OFFSET_WHEEL_DATA_EXPENSION = OFFSET_IS_WHEEL_DETACHED + ByteUtil.SIZE_BOOL;
    
    private static final int WHEEL_DATA_SIZE = OFFSET_WHEEL_DATA_EXPENSION + 32 * ByteUtil.SIZE_CHAR;
    
    private static final int BUFFER_SIZE = OFFSET_WHEEL_DATA + ( 4 * WHEEL_DATA_SIZE );
    
    final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    private boolean updatedInTimeScope = false;
    private long updateId = 0L;
    private boolean sessionJustStarted = false;
    
    private final LiveGameData gameData;
    
    private final RFactorEventsManager eventsManager;
    
    private float engineBaseMaxRPM = 1000.12345f;
    private float engineMaxRPM = 1000.12345f;
    
    private int engineBoostMapping = 5;
    private boolean tempBoostFlag = false;
    
    float engineLifetime = 0.0f;
    float brakeDiscThicknessFL = 0.0f;
    float brakeDiscThicknessFR = 0.0f;
    float brakeDiscThicknessRL = 0.0f;
    float brakeDiscThicknessRR = 0.0f;
    
    public static interface TelemetryDataUpdateListener
    {
        public void onSessionStarted( LiveGameData gameData, EditorPresets editorPresets );
        public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets );
        public void onTelemetryDataUpdated( LiveGameData gameData, EditorPresets editorPresets );
        public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets );
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
            TelemetryDataUpdateListener[] tmp = new TelemetryDataUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
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
            
            float bmr = ByteUtil.readFloat( buffer, OFFSET_ENGINE_MAX_RPM );
            bmr = gameData.getPhysics().getEngine().getRevLimitRange().limitValue( bmr );
            
            if ( bmr != engineBaseMaxRPM )
            {
                // the car is controlled by the player but not the AI
                this.engineBaseMaxRPM = bmr;
            }
            
            if ( sessionJustStarted )
            {
                if ( updateListeners != null )
                {
                    for ( int i = 0; i < updateListeners.length; i++ )
                        updateListeners[i].onSessionStarted( gameData, editorPresets );
                }
                
                sessionJustStarted = false;
            }
            
            if ( updateListeners != null )
            {
                for ( int i = 0; i < updateListeners.length; i++ )
                    updateListeners[i].onTelemetryDataUpdated( gameData, editorPresets );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
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
    
    /**
     * 
     * @param editorPresets
     */
    void onSessionStarted( EditorPresets editorPresets )
    {
        this.sessionJustStarted = true;
        this.updatedInTimeScope = false;
    }
    
    void onSessionEnded()
    {
        this.updatedInTimeScope = false;
    }
    
    void onRealtimeEntered( EditorPresets editorPresets )
    {
        this.updatedInTimeScope = true;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
                updateListeners[i].onRealtimeEntered( gameData, editorPresets );
        }
    }
    
    void onRealtimeExited( EditorPresets editorPresets )
    {
        this.updatedInTimeScope = false;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
                updateListeners[i].onRealtimeExited( gameData, editorPresets );
        }
    }
    
    void loadFromStream( InputStream in, EditorPresets editorPresets ) throws IOException
    {
        prepareDataUpdate();
        
        int offset = 0;
        int bytesToRead = BUFFER_SIZE;
        
        while ( bytesToRead > 0 )
        {
            int n = in.read( buffer, offset, bytesToRead );
            
            if ( n < 0 )
                throw new IOException();
            
            offset += n;
            bytesToRead -= n;
        }
        
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
            eventsManager.onEngineBoostMappingChanged( oldValue, engineBoostMapping );
    }
    
    void decEngineBoostMapping( Engine engine )
    {
        int oldValue = this.engineBoostMapping;
        this.engineBoostMapping = Math.max( (int)engine.getBoostRange().getMinValue(), engineBoostMapping - 1 );
        
        if ( engineBoostMapping != oldValue )
            eventsManager.onEngineBoostMappingChanged( oldValue, engineBoostMapping );
    }
    
    void setTempBoostFlag( boolean tempBoostFlag )
    {
        boolean oldValue = this.tempBoostFlag;
        this.tempBoostFlag = tempBoostFlag;
        
        if ( tempBoostFlag != oldValue )
            eventsManager.onTemporaryEngineBoostStateChanged( tempBoostFlag );
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
    
    /*
     * ################################
     * TelemInfoBase
     * ################################
     */
    
    // Time
    
    /**
     * time since last update (seconds)
     */
    public final float getDeltaTime()
    {
        // float mDeltaTime
        
        return ( ByteUtil.readFloat( buffer, OFFSET_DELTA_TIME ) );
    }
    
    /**
     * current lap number
     */
    public final int getCurrentLapNumber()
    {
        // long mLapNumber
        
        return ( (int)ByteUtil.readLong( buffer, OFFSET_LAP_NUMBER ) );
    }
    
    /**
     * time this lap was started
     */
    public final float getLapStartET()
    {
        // float mLapStartET
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAP_START_TIME ) );
    }
    
    /**
     * current vehicle name
     */
    public final String getVehicleName()
    {
        // char mVehicleName[64]
        
        return ( ByteUtil.readString( buffer, OFFSET_VEHICLE_NAME, MAX_VEHICLE_NAME_LENGTH ) );
    }
    
    /**
     * current track name
     */
    public final String getTrackName()
    {
        // char mTrackName[64]
        
        return ( ByteUtil.readString( buffer, OFFSET_TRACK_NAME, MAX_TRACK_NAME_LENGTH ) );
    }
    
    // Position and derivatives
    
    /**
     * world position in meters
     * 
     * @param position
     */
    public final TelemVect3 getPosition( TelemVect3 position )
    {
        // TelemVect3 mPos
        
        ByteUtil.readVector( buffer, OFFSET_POSITION, position );
        
        return ( position );
    }
    
    /**
     * world position in meters
     */
    public final float getPositionX()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_POSITION + 0 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * world position in meters
     */
    public final float getPositionY()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_POSITION + 1 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * world position in meters
     */
    public final float getPositionZ()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_POSITION + 2 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
     * 
     * @param localVel
     */
    public final TelemVect3 getLocalVelocity( TelemVect3 localVel )
    {
        // TelemVect3 mLocalVel
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_VELOCITY, localVel );
        
        return ( localVel );
    }
    
    /**
     * velocity (meters/sec)
     */
    public final float getScalarVelocityMPS()
    {
        float vecX = ByteUtil.readFloat( buffer, OFFSET_LOCAL_VELOCITY + 0 * ByteUtil.SIZE_FLOAT );
        float vecY = ByteUtil.readFloat( buffer, OFFSET_LOCAL_VELOCITY + 1 * ByteUtil.SIZE_FLOAT );
        float vecZ = ByteUtil.readFloat( buffer, OFFSET_LOCAL_VELOCITY + 2 * ByteUtil.SIZE_FLOAT );
        
        return ( (float)Math.sqrt( vecX * vecX + vecY * vecY + vecZ * vecZ ) );
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
        // TelemVect3 mLocalAccel
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_ACCELERATION, localAccel );
        
        return ( localAccel );
    }
    
    /**
     * longitudinal acceleration (meters/sec^2)
     */
    public final float getLongitudinalAcceleration()
    {
        // TelemVect3 mLocalAccel
        
        return ( -ByteUtil.readFloat( buffer, OFFSET_LOCAL_ACCELERATION + 2 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * lateral acceleration (meters/sec^2)
     */
    public final float getLateralAcceleration()
    {
        // TelemVect3 mLocalAccel
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LOCAL_ACCELERATION + 0 * ByteUtil.SIZE_FLOAT ) );
    }
    
    // Orientation and derivatives
    
    /**
     * top row of orientation matrix
     * 
     * (also converts local vehicle vectors into world X using dot product)
     * 
     * @param oriX
     */
    public final TelemVect3 getOrientationX( TelemVect3 oriX )
    {
        // TelemVect3 mOriX
        
        ByteUtil.readVector( buffer, OFFSET_ORIENTATION_X, oriX );
        
        return ( oriX );
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
        // TelemVect3 mOriY
        
        ByteUtil.readVector( buffer, OFFSET_ORIENTATION_Y, oriY );
        
        return ( oriY );
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
        // TelemVect3 mOriZ
        
        ByteUtil.readVector( buffer, OFFSET_ORIENTATION_Z, oriZ );
        
        return ( oriZ );
    }
    
    /**
     * rotation (radians/sec) in local vehicle coordinates
     * 
     * @param localRot
     */
    public final TelemVect3 getLocalRotation( TelemVect3 localRot )
    {
        // TelemVect3 mLocalRot
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_ROTATION, localRot );
        
        return ( localRot );
    }
    
    /**
     * rotational acceleration (radians/sec^2) in local vehicle coordinates
     * 
     * @param localRotAccel
     */
    public final TelemVect3 getLocalRotationalAcceleration( TelemVect3 localRotAccel )
    {
        // TelemVect3 mLocalRotAccel
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_ROTATION_ACCELERATION, localRotAccel );
        
        return ( localRotAccel );
    }
    
    // Vehicle status
    
    /**
     * -1=reverse, 0=neutral, 1+=forward gears
     */
    public final short getCurrentGear()
    {
        // long mGear
        
        return ( (short)ByteUtil.readLong( buffer, OFFSET_GEAR ) );
    }
    
    /**
     * engine RPM
     */
    public final float getEngineRPM()
    {
        // float mEngineRPM
        
        return ( ByteUtil.readFloat( buffer, OFFSET_ENGINE_RPM ) );
    }
    
    /**
     * Celsius
     */
    public final float getEngineWaterTemperatureC()
    {
        // float mEngineWaterTemp
        
        return ( ByteUtil.readFloat( buffer, OFFSET_ENGINE_WATER_TEMP ) );
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
        // float mEngineOilTemp
        
        return ( ByteUtil.readFloat( buffer, OFFSET_ENGINE_OIL_TEMP ) );
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
        // float mClutchRPM
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CLUTCH_RPM ) );
    }
    
    // Driver input
    
    /**
     * ranges  0.0-1.0
     */
    public final float getUnfilteredThrottle()
    {
        // float mUnfilteredThrottle
        
        return ( ByteUtil.readFloat( buffer, OFFSET_UNFILTERED_THROTTLE ) );
    }
    
    /**
     * ranges  0.0-1.0
     */
    public final float getUnfilteredBrake()
    {
        // float mUnfilteredBrake
        
        return ( ByteUtil.readFloat( buffer, OFFSET_UNFILTERED_BRAKE ) );
    }
    
    /**
     * ranges -1.0-1.0 (left to right)
     */
    public final float getUnfilteredSteering()
    {
        // float mUnfilteredSteering
        
        return ( ByteUtil.readFloat( buffer, OFFSET_UNFILTERED_STEERING ) );
    }
    
    /**
     * ranges  0.0-1.0
     */
    public final float getUnfilteredClutch()
    {
        // float mUnfilteredClutch
        
        return ( ByteUtil.readFloat( buffer, OFFSET_UNFILTERED_CLUTCH ) );
    }
    
    // Misc
    
    /**
     * force on steering arms
     */
    public final float getSteeringArmForce()
    {
        // float mSteeringArmForce
        
        return ( ByteUtil.readFloat( buffer, OFFSET_STEERING_ARM_FORCE ) );
    }
    
    /*
     * ################################
     * TelemInfoV2
     * ################################
     */
    
    // state/damage info
    
    /**
     * amount of fuel (liters)
     */
    public final float getFuelL()
    {
        // float mFuel
        
        return ( ByteUtil.readFloat( buffer, OFFSET_FUEL ) );
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
        // float mEngineMaxRPM
        
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
        // unsigned char mScheduledStops
        
        return ( ByteUtil.readUnsignedByte( buffer, OFFSET_SCHEDULED_STOPS ) );
    }
    
    /**
     * whether overheating icon is shown
     */
    public final boolean isOverheating()
    {
        // bool mOverheating
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_OVERHEATING ) );
    }
    
    /**
     * whether any parts (besides wheels) have been detached
     */
    public final boolean isAnythingDetached()
    {
        // bool mDetached
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_DETACHED ) );
    }
    
    /**
     * dent severity at 8 locations around the car (0=none, 1=some, 2=more)
     */
    public final short[] getDentSevirity()
    {
        // unsigned char mDentSeverity[8]
        
        short[] result = new short[ 8 ];
        
        for ( int i = 0; i < result.length; i++ )
        {
            result[i] = ByteUtil.readUnsignedByte( buffer, OFFSET_DENT_SEVERITY + i * ByteUtil.SIZE_CHAR );
        }
        
        return ( result );
    }
    
    /**
     * time of last impact
     */
    public final float getLastImpactTime()
    {
        // float mLastImpactET
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAST_IMPACT_TIME ) );
    }
    
    /**
     * magnitude of last impact
     */
    public final float getLastImpactMagnitude()
    {
        // float mLastImpactMagnitude
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAST_IMPACT_MAGNITUDE ) );
    }
    
    /**
     * location of last impact
     * 
     * @param lastImpactPos
     */
    public final TelemVect3 getLastImpactPosition( TelemVect3 lastImpactPos )
    {
        // TelemVect3 mLastImpactPos
        
        ByteUtil.readVector( buffer, OFFSET_LAST_IMPACT_POSITION, lastImpactPos );
        
        return ( lastImpactPos );
    }
    
    // Future use
    
    //unsigned char mExpansion[64];
    
    /*
     * ################################
     * TelemWheelV2
     * ################################
     */
    
    /**
     * radians/sec
     */
    public final float getWheelRotation( Wheel wheel )
    {
        // float mRotation
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_WHEEL_ROTATION ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_WHEEL_ROTATION ) );
            case REAR_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_WHEEL_ROTATION ) );
            case REAR_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_WHEEL_ROTATION ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * meters
     */
    public final float getWheelSuspensionDeflection( Wheel wheel )
    {
        // float mSuspensionDeflection
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_DEFLECTION ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_DEFLECTION ) );
            case REAR_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_DEFLECTION ) );
            case REAR_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_DEFLECTION ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * meters
     */
    public final float getRideHeight( Wheel wheel )
    {
        // float mRideHeight
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_RIDE_HEIGHT ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_RIDE_HEIGHT ) );
            case REAR_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_RIDE_HEIGHT ) );
            case REAR_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_RIDE_HEIGHT ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * Newtons
     */
    public final float getTireLoad( Wheel wheel )
    {
        // float mTireLoad
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_TIRE_LOAD ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_TIRE_LOAD ) );
            case REAR_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_TIRE_LOAD ) );
            case REAR_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_TIRE_LOAD ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * Newtons
     */
    public final float getLateralForce( Wheel wheel )
    {
        // float mLateralForce
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_LATERAL_FORCE ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_LATERAL_FORCE ) );
            case REAR_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_LATERAL_FORCE ) );
            case REAR_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_LATERAL_FORCE ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * an approximation of what fraction of the contact patch is sliding
     */
    public final float getGripFraction( Wheel wheel )
    {
        // float mGripFract
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_GRIP_FRACTION ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_GRIP_FRACTION ) );
            case REAR_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_GRIP_FRACTION ) );
            case REAR_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_GRIP_FRACTION ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * Kelvin
     */
    public final float getBrakeTemperatureK( Wheel wheel )
    {
        // float mBrakeTemp
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_BRAKE_TEMP ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_BRAKE_TEMP ) );
            case REAR_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_BRAKE_TEMP ) );
            case REAR_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_BRAKE_TEMP ) );
        }
        
        // Unreachable code!
        return ( 0f );
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
        // float mPressure
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_TIRE_PRESSURE ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_TIRE_PRESSURE ) );
            case REAR_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_TIRE_PRESSURE ) );
            case REAR_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_TIRE_PRESSURE ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * Celsius
     */
    public final float getTireTemperatureC( Wheel wheel, WheelPart part )
    {
        // float mTemperature[3], left/center/right (not to be confused with inside/center/outside!)
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_TIRE_TEMPERATURES + part.getArrayIndexFL() * ByteUtil.SIZE_FLOAT ) + ZERO_KELVIN );
            case FRONT_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_TIRE_TEMPERATURES + part.getArrayIndexFR() * ByteUtil.SIZE_FLOAT ) + ZERO_KELVIN );
            case REAR_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_TIRE_TEMPERATURES + part.getArrayIndexRL() * ByteUtil.SIZE_FLOAT ) + ZERO_KELVIN );
            case REAR_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_TIRE_TEMPERATURES + part.getArrayIndexRR() * ByteUtil.SIZE_FLOAT ) + ZERO_KELVIN );
        }
        
        // Unreachable code!
        return ( 0f );
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
        // float mWear
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_TIRE_WEAR ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_TIRE_WEAR ) );
            case REAR_LEFT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_TIRE_WEAR ) );
            case REAR_RIGHT:
                return ( ByteUtil.readFloat( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_TIRE_WEAR ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * the material prefixes from the TDF file
     */
    public final String getTerrainName( Wheel wheel )
    {
        // char mTerrainName[16]
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readString( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_TERRAIN_NAME, MAX_TERRAIN_NAME_LENGTH ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readString( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_TERRAIN_NAME, MAX_TERRAIN_NAME_LENGTH ) );
            case REAR_LEFT:
                return ( ByteUtil.readString( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_TERRAIN_NAME, MAX_TERRAIN_NAME_LENGTH ) );
            case REAR_RIGHT:
                return ( ByteUtil.readString( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_TERRAIN_NAME, MAX_TERRAIN_NAME_LENGTH ) );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    /**
     * surface under the wheel
     */
    public final SurfaceType getSurfaceType( Wheel wheel )
    {
        // unsigned char mSurfaceType
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( SurfaceType.getFromIndex( ByteUtil.readUnsignedByte( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_SURFACE_TYPE ) ) );
            case FRONT_RIGHT:
                return ( SurfaceType.getFromIndex( ByteUtil.readUnsignedByte( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_SURFACE_TYPE ) ) );
            case REAR_LEFT:
                return ( SurfaceType.getFromIndex( ByteUtil.readUnsignedByte( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_SURFACE_TYPE ) ) );
            case REAR_RIGHT:
                return ( SurfaceType.getFromIndex( ByteUtil.readUnsignedByte( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_SURFACE_TYPE ) ) );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    /**
     * whether tire is flat
     */
    public final boolean isWheelFlat( Wheel wheel )
    {
        // bool mFlat
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_FLAT ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_FLAT ) );
            case REAR_LEFT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_FLAT ) );
            case REAR_RIGHT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_FLAT ) );
        }
        
        // Unreachable code!
        return ( false );
    }
    
    /**
     * whether wheel is detached
     */
    public final boolean isWheelDetached( Wheel wheel )
    {
        // bool mDetached
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_DETACHED ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_DETACHED ) );
            case REAR_LEFT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_DETACHED ) );
            case REAR_RIGHT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_DETACHED ) );
        }
        
        // Unreachable code!
        return ( false );
    }
    
    // future use
    
    // unsigned char mExpansion[32];
    
    TelemetryData( LiveGameData gameData, RFactorEventsManager eventsManager )
    {
        this.gameData = gameData;
        this.eventsManager = eventsManager;
        
        registerListener( TopspeedRecorder.MASTER_TOPSPEED_RECORDER );
        registerListener( LifetimeManager.INSTANCE );
    }
}
