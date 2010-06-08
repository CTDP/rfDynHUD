package net.ctdp.rfdynhud.gamedata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.SpeedUnits;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;

/**
 * 
 * @author Marvin Froehlich
 */
public class VehicleScoringInfo
{
    private final ScoringInfo scoringInfo;
    private final ProfileInfo profileInfo;
    
    VehicleScoringInfoCapsule data = null;
    
    private String name = null;
    private String nameShort = null;
    private String nameTLC = null;
    private int nameId = 0;
    private Integer nameID = null;
    
    private int lastTLCMrgUpdateId = -1;
    
    private String vehClass = null;
    private static int nextClassId = 1;
    private int classId = 0;
    private Integer classID = null;
    
    short placeByClass = -1;
    int numVehiclesInClass = -1;
    float timeBehindNextByClass = 0f;
    int lapsBehindNextByClass = -1;
    float timeBehindLeaderByClass = 0f;
    int lapsBehindLeaderByClass = -1;
    VehicleScoringInfo classLeaderVSI = null;
    VehicleScoringInfo classNextInFrontVSI = null;
    VehicleScoringInfo classNextBehindVSI = null;
    
    private int oldLap = -1;
    private int lap = -1;
    private int stintStartLap = -1;
    private float stintLength = 0f;
    private int pitState = -1;
    
    final ArrayList<Laptime> laptimes = new ArrayList<Laptime>();
    Laptime fastestLaptime = null;
    float avgLaptime = -1f;
    float avgSector1 = -1f;
    float avgSector2 = -1f;
    float avgSector3 = -1f;
    
    float topspeed = 0f;
    
    private static final HashMap<String, Integer> classToIDMap = new HashMap<String, Integer>();
    
    private void updateClassID()
    {
        String vehClass = getVehicleClass();
        
        Integer id = classToIDMap.get( vehClass );
        if ( id == null )
        {
            id = nextClassId++;
            classToIDMap.put( vehClass, id );
        }
        
        this.classId = id.intValue();
        this.classID = id;
    }
    
    void applyEditorPresets( EditorPresets editorPresets )
    {
        if ( editorPresets == null )
            return;
        
        if ( isPlayer() )
        {
            name = editorPresets.getDriverName();
            data.setDriverName( name );
            nameShort = null;
            nameTLC = null;
            nameID = data.refreshID();
            nameId = nameID.intValue();
        }
        
        topspeed = editorPresets.getTopSpeed( getPlace( false ) - 1 );
    }
    
    void onDataUpdated()
    {
        vehClass = null;
        classId = 0;
        classID = null;
        
        oldLap = lap;
        lap = getLapsCompleted() + 1;
    }
    
    void setDriverName( String name, Integer id )
    {
        this.name = name;
        this.nameShort = null;
        this.nameTLC = null;
        this.nameID = id;
        this.nameId = id.intValue();
    }
    
    /**
     * Gets the full name of the driver driving this vehicle.
     * 
     * @return the full name of the driver driving this vehicle.
     */
    public final String getDriverName()
    {
        /*
        if ( name == null )
        {
            name = data.getDriverName();
        }
        */
        
        return ( name );
    }
    
    /**
     * driver name (short form)
     */
    public final String getDriverNameShort()
    {
        if ( ( nameShort == null ) || ( lastTLCMrgUpdateId < ThreeLetterCodeManager.getUpdateId() ) )
        {
            nameShort = ThreeLetterCodeManager.getShortForm( getDriverName(), getDriverID() );
            lastTLCMrgUpdateId = ThreeLetterCodeManager.getUpdateId();
        }
        
        return ( nameShort );
    }
    
    /**
     * driver name (three letter code)
     */
    public final String getDriverNameTLC()
    {
        if ( ( nameTLC == null ) || ( lastTLCMrgUpdateId < ThreeLetterCodeManager.getUpdateId() ) )
        {
            nameTLC = ThreeLetterCodeManager.getThreeLetterCode( getDriverName(), getDriverID() );
            lastTLCMrgUpdateId = ThreeLetterCodeManager.getUpdateId();
        }
        
        return ( nameTLC );
    }
    
    /**
     * Uniquely identifes this vehicle's driver. It returns the same value as {@link #getDriverID()}, but as a primitive int.
     * 
     * @return the driver's id.
     */
    public final int getDriverId()
    {
        return ( nameId );
    }
    
    /**
     * Uniquely identifes this vehicle's driver. It returns the same value as {@link #getDriverId()}, but as an {@link Integer} instance.
     * 
     * @return the driver's id.
     */
    public final Integer getDriverID()
    {
        return ( nameID );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ( getDriverId() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( !( o instanceof VehicleScoringInfo ) )
            return ( false );
        
        return ( this.getDriverId() == ( (VehicleScoringInfo)o ).getDriverId() );
    }
    
    /**
     * vehicle name
     */
    public final String getVehicleName()
    {
        return ( data.getVehicleName() );
    }
    
    /**
     * laps completed
     */
    public final short getLapsCompleted()
    {
        return ( data.getLapsCompleted() );
    }
    
    public final short getCurrentLap()
    {
        if ( isInPits() && ( getStintLength() < 0.5f ) )
            return ( getLapsCompleted() );
        
        return ( (short)( getLapsCompleted() + 1 ) );
    }
    
    public final boolean isLapJustStarted()
    {
        return ( lap != oldLap );
    }
    
    /**
     * Gets the {@link SessionLimit} of the current session. If the session limit is defined to be LAPS,
     * LAPS is returned. If it is defined to be timed, TIME is returned. Otherwise the method
     * tries to guess the limit based on the average laptime.
     * 
     * @param preference if both TIME and LAPS are possible, preference is returned.
     * 
     * @return the {@link SessionLimit}.
     */
    public final SessionLimit getSessionLimit( SessionLimit preference )
    {
        int maxLaps = scoringInfo.getMaxLaps();
        if ( maxLaps > Integer.MAX_VALUE / 2 )
            maxLaps = 0;
        final float endTime = scoringInfo.getEndTime();
        
        if ( ( maxLaps > 0 ) && ( maxLaps < 10000 ) )
        {
            if ( ( endTime > 0f ) && ( endTime < 999999f ) )
            {
                float avgLaptime = getAverageLaptime();
                
                if ( avgLaptime < 0f )
                {
                    if ( preference == null )
                        return ( SessionLimit.LAPS );
                    
                    return ( preference );
                }
                
                int timeLaps = (int)( endTime / avgLaptime );
                
                if ( timeLaps < maxLaps )
                    return ( SessionLimit.TIME );
            }
            
            return ( SessionLimit.LAPS );
        }
        
        if ( ( endTime > 0f ) && ( endTime < 999999f ) )
            return ( SessionLimit.TIME );
        
        return ( null );
    }
    
    /**
     * Gets the {@link SessionLimit} of the current session. If the session limit is defined to be LAPS,
     * LAPS is returned. If it is defined to be timed, TIME is returned. Otherwise the method
     * tries to guess the limit based on the average laptime.
     * 
     * @return the {@link SessionLimit}.
     */
    public final SessionLimit getSessionLimit()
    {
        return ( getSessionLimit( null ) );
    }
    
    /**
     * Gets the estimated max laps based on the session end time and average lap time.
     * If the {@link SessionLimit} is defined to be LAPS, then max laps is known and returned.
     * 
     * @return the estimated max laps.
     */
    public final int getEstimatedMaxLaps()
    {
        if ( scoringInfo.getSessionType().isRace() && scoringInfo.getLeadersVehicleScoringInfo().getFinishStatus().isFinished() )
        {
            return ( scoringInfo.getLeadersVehicleScoringInfo().getLapsCompleted() );
        }
        
        short lapsCompleted = getLapsCompleted();
        int maxLaps = scoringInfo.getMaxLaps();
        if ( maxLaps > Integer.MAX_VALUE / 2 )
            maxLaps = 0;
        float endTime = scoringInfo.getEndTime();
        if ( ( lapsCompleted == 0 ) || ( endTime < 0f ) || ( endTime > 999999f ) )
        {
            if ( maxLaps > 0 )
                return ( maxLaps );
            
            return ( -1 );
        }
        
        float avgLaptime = getAverageLaptime();
        if ( avgLaptime < 0f )
        {
            if ( maxLaps > 0 )
                return ( maxLaps );
            
            return ( -1 );
        }
        
        float restTime = endTime - getLapStartTime();
        int timeLaps = lapsCompleted + (int)( restTime / avgLaptime ) + 1;
        
        if ( ( maxLaps <= 0 ) || ( timeLaps < maxLaps ) )
            return ( timeLaps );
        
        return ( maxLaps );
    }
    
    /**
     * Gets the number of remaining laps (with fractions).
     * 
     * @param maxLaps
     * 
     * @return the number of remaining laps.
     */
    public final float getLapsRemaining( int maxLaps )
    {
        if ( maxLaps < 0 )
            return ( -1f );
        
        int lr = maxLaps - getLapsCompleted();
        
        if ( getFinishStatus().isFinished() )
            return ( lr );
        
        return ( lr - getNormalizedLapDistance() );
    }
    
    /**
     * sector
     */
    public final byte getSector()
    {
        return ( data.getSector() );
    }
    
    /**
     * finish status
     */
    public final FinishStatus getFinishStatus()
    {
        return ( data.getFinishStatus() );
    }
    
    /**
     * current distance around track
     */
    public final float getLapDistance()
    {
        return ( data.getLapDistance() );
    }
    
    /**
     * current distance around track in range 0..1
     */
    public final float getNormalizedLapDistance()
    {
        return ( getLapDistance() / scoringInfo.getTrackLength() );
    }
    
    void updateStintLength()
    {
        int currentLap = getLapsCompleted() + 1; // Don't use getCurrentLap(), since it depends on stint length!
        boolean isInPits = isInPits();
        boolean isStanding = ( Math.abs( getScalarVelocityMPS() ) < 0.1f );
        float trackPos = getNormalizedLapDistance();
        
        if ( ( stintStartLap < 0 ) || ( isInPits && ( stintStartLap != currentLap ) && isStanding ) || ( stintStartLap > currentLap ) )
        {
            stintStartLap = currentLap;
        }
        
        int oldPitState = pitState;
        if ( oldPitState == -1 )
        {
            if ( isInPits && isStanding )
                pitState = 2;
            else if ( isInPits )
                pitState = 1;
            else
                pitState = 0;
        }
        else
        {
            if ( ( oldPitState == 2 ) && !isInPits )
            {
                stintStartLap = currentLap;
            }
            
            if ( isInPits )
            {
                if ( isStanding && ( oldPitState != 2 ) )
                    pitState = 2;
                else if ( oldPitState == 0 )
                    pitState = 1;
            }
            else if ( oldPitState != 0 )
            {
                pitState = 0;
            }
        }
        
        stintLength = currentLap - stintStartLap + trackPos;
    }
    
    void resetDerivateData()
    {
        stintStartLap = -1;
        oldLap = -1;
        laptimes.clear();
        fastestLaptime = null;
        avgLaptime = -1f;
        avgSector1 = -1f;
        avgSector2 = -1f;
        avgSector3 = -1f;
    }
    
    void onSessionStarted()
    {
        resetDerivateData();
    }
    
    void onSessionEnded()
    {
        resetDerivateData();
    }
    
    public final int getStintStartLap()
    {
        return ( stintStartLap );
    }
    
    public final float getStintLength()
    {
        return ( stintLength );
    }
    
    /**
     * Gets the Laptime object for the given lap.
     * 
     * @param lap
     * 
     * @return the Laptime object for the given lap.
     */
    public final Laptime getLaptime( int lap )
    {
        if ( ( lap < 1 ) || ( laptimes == null ) || ( lap > laptimes.size() ) )
            return ( null );
        
        return ( laptimes.get( lap - 1 ) );
    }
    
    /**
     * Gets this driver's fastest {@link Laptime}.
     * 
     * @return this driver's fastest {@link Laptime}.
     */
    public final Laptime getFastestLaptime()
    {
        return ( fastestLaptime );
    }
    
    /**
     * Gets the average laptime of the current session. Inlaps, outlaps and laps slower than (1.06 * fastest) are ignored.
     * 
     * @return the average laptime or -1.
     */
    public final float getAverageLaptime()
    {
        return ( avgLaptime );
    }
    
    /**
     * Gets the average sector 1 time of the current session. Inlaps, outlaps and laps slower than (1.06 * fastest) are ignored.
     * 
     * @return the average sector 1 time or -1.
     */
    public final float getAverageSector1Time()
    {
        return ( avgSector1 );
    }
    
    /**
     * Gets the average sector 2 time of the current session. Inlaps, outlaps and laps slower than (1.06 * fastest) are ignored.
     * 
     * @return the average sector 2 time or -1.
     */
    public final float getAverageSector2Time()
    {
        return ( avgSector2 );
    }
    
    /**
     * Gets the average sector 3 time of the current session. Inlaps, outlaps and laps slower than (1.06 * fastest) are ignored.
     * 
     * @return the average sector 3 time or -1.
     */
    public final float getAverageSector3Time()
    {
        return ( avgSector3 );
    }
    
    /**
     * lateral position with respect to *very approximate* "center" path
     */
    public final float getPathLateral()
    {
        return ( data.getPathLateral() );
    }
    
    /**
     * track edge (w.r.t. "center" path) on same side of track as vehicle
     */
    public final float getTrackEdge()
    {
        return ( data.getTrackEdge() );
    }
    
    /**
     * best sector 1
     */
    public final float getBestSector1()
    {
        return ( data.getBestSector1() );
    }
    
    /**
     * best sector 2
     * 
     * @param includingSector1
     */
    public final float getBestSector2( boolean includingSector1 )
    {
        float sec2 = data.getBestSector2();
        
        if ( !includingSector1 )
            sec2 -= getBestSector1();
        
        return ( sec2 );
    }
    
    /**
     * best lap time
     */
    public final float getBestLapTime()
    {
        return ( data.getBestLapTime() );
    }
    
    /**
     * best sector 3
     */
    public final float getBestSector3()
    {
        return ( getBestLapTime() - getBestSector2( true ) );
    }
    
    /**
     * last sector 1
     */
    public final float getLastSector1()
    {
        // float mLastSector1
        
        return ( data.getLastSector1() );
    }
    
    /**
     * last sector 2
     * 
     * @param includingSector1
     */
    public final float getLastSector2( boolean includingSector1 )
    {
        // float mLastSector2
        
        float sec2 = data.getLastSector2();
        
        if ( !includingSector1 )
            sec2 -= getLastSector1();
        
        return ( sec2 );
    }
    
    /**
     * last lap time
     */
    public final float getLastLapTime()
    {
        return ( data.getLastLapTime() );
    }
    
    /**
     * last sector 3
     */
    public final float getLastSector3()
    {
        return ( getLastLapTime() - getLastSector2( true ) );
    }
    
    /**
     * current sector 1 (if valid)
     */
    public final float getCurrentSector1()
    {
        return ( data.getCurrentSector1() );
    }
    
    /**
     * current sector 2
     * 
     * @param includingSector1 only affects result if sector1 is valid
     */
    public final float getCurrentSector2( boolean includingSector1 )
    {
        float sec2 = data.getCurrentSector2();
        
        if ( !includingSector1 && ( sec2 > 0f ) )
            sec2 -= getCurrentSector1();
        
        return ( sec2 );
    }
    
    /**
     * The current laptime (may be incomplete).
     * 
     * @return current laptime.
     */
    public final float getCurrentLaptime()
    {
        if ( getStintLength() < 1.0f )
            return ( -1f );
        
        if ( !scoringInfo.getSessionType().isRace() && ( getStintLength() < 1.0f ) )
            return ( -1f );
        
        return ( scoringInfo.getSessionTime() - getLapStartTime() );
    }
    
    /**
     * number of pitstops made
     */
    public final short getNumPitstopsMade()
    {
        return ( data.getNumPitstopsMade() );
    }
    
    /**
     * number of outstanding penalties
     */
    public final short getNumOutstandingPenalties()
    {
        return ( data.getNumOutstandingPenalties() );
    }
    
    /**
     * is this the player's vehicle?
     */
    public final boolean isPlayer()
    {
        return ( data.isPlayer() );
    }
    
    /**
     * who's in control?
     */
    public final VehicleControl getVehicleControl()
    {
        return ( data.getVehicleControl() );
    }
    
    /**
     * between pit entrance and pit exit (not always accurate for remote vehicles)
     */
    public final boolean isInPits()
    {
        return ( data.isInPits() );
    }
    
    /**
     * Gets the number of vehicles in the same vehicle class.
     * 
     * @return the number of vehicles in the same vehicle class.
     */
    public final int getNumVehiclesInSameClass()
    {
        scoringInfo.updateClassScoring();
        
        return ( numVehiclesInClass );
    }
    
    /**
     * 1-based position
     * 
     * @param byClass only consider vehicles in the same class
     */
    public final short getPlace( boolean byClass )
    {
        if ( byClass )
        {
            scoringInfo.updateClassScoring();
            
            return ( placeByClass );
        }
        
        return ( data.getPlace() );
    }
    
    /**
     * Gets the {@link VehicleScoringInfo}, that leads the same class.
     * 
     * @return the {@link VehicleScoringInfo}, that leads the same class.
     */
    public final VehicleScoringInfo getLeaderByClass()
    {
        scoringInfo.updateClassScoring();
        
        return ( classLeaderVSI );
    }
    
    /**
     * Gets the {@link VehicleScoringInfo}, that is next in front.
     * 
     * @param byClass only consider vehicles in the same class
     * 
     * @return the {@link VehicleScoringInfo}, that is next in front.
     */
    public final VehicleScoringInfo getNextInFront( boolean byClass )
    {
        if ( byClass )
        {
            scoringInfo.updateClassScoring();
            
            return ( classNextInFrontVSI );
        }
        
        short place = getPlace( false );
        
        if ( place == 1 )
            return ( null );
        
        return ( scoringInfo.getVehicleScoringInfo( place - 2 ) );
    }
    
    /**
     * Gets the {@link VehicleScoringInfo}, that is next behind.
     * 
     * @param byClass only consider vehicles in the same class
     * 
     * @return the {@link VehicleScoringInfo}, that is next behind.
     */
    public final VehicleScoringInfo getNextBehind( boolean byClass )
    {
        if ( byClass )
        {
            scoringInfo.updateClassScoring();
            
            return ( classNextBehindVSI );
        }
        
        short place = getPlace( false );
        
        if ( place == scoringInfo.getNumVehicles() )
            return ( null );
        
        return ( scoringInfo.getVehicleScoringInfo( place + 0 ) );
    }
    
    /**
     * vehicle class
     */
    public final String getVehicleClass()
    {
        if ( vehClass == null )
        {
            vehClass = data.getVehicleClass();
        }
        
        return ( vehClass );
    }
    
    void setVehClass( String vehClass )
    {
        //getVehicleClass();
        this.vehClass = vehClass;
    }
    
    public final int getVehicleClassId()
    {
        if ( classId <= 0 )
        {
            updateClassID();
        }
        
        return ( classId );
    }
    
    public final Integer getVehicleClassID()
    {
        if ( classID == null )
        {
            updateClassID();
        }
        
        return ( classID );
    }
    
    /**
     * time behind vehicle in next higher place
     * 
     * @param byClass only consider vehicles in the same class
     */
    public final float getTimeBehindNextInFront( boolean byClass )
    {
        if ( byClass )
        {
            scoringInfo.updateClassScoring();
            
            return ( timeBehindNextByClass );
        }
        
        return ( data.getTimeBehindNextInFront() );
    }
    
    /**
     * laps behind vehicle in next higher place
     * 
     * @param byClass only consider vehicles in the same class
     */
    public final int getLapsBehindNextInFront( boolean byClass )
    {
        if ( byClass )
        {
            scoringInfo.updateClassScoring();
            
            return ( lapsBehindNextByClass );
        }
        
        return ( data.getLapsBehindNextInFront() );
    }
    
    /**
     * time behind leader
     * 
     * @param byClass only consider vehicles in the same class
     */
    public final float getTimeBehindLeader( boolean byClass )
    {
        if ( byClass )
        {
            scoringInfo.updateClassScoring();
            
            return ( timeBehindLeaderByClass );
        }
        
        return ( data.getTimeBehindLeader() );
    }
    
    /**
     * laps behind leader
     * 
     * @param byClass only consider vehicles in the same class
     */
    public final int getLapsBehindLeader( boolean byClass )
    {
        if ( byClass )
        {
            scoringInfo.updateClassScoring();
            
            return ( lapsBehindLeaderByClass );
        }
        
        return ( data.getLapsBehindNextInFront() );
    }
    
    /**
     * time this lap was started
     */
    public final float getLapStartTime()
    {
        return ( data.getLapStartTime() );
    }
    
    /**
     * world position in meters
     * 
     * @param position
     */
    public final void getWorldPosition( TelemVect3 position )
    {
        data.getWorldPosition( position );
    }
    
    /**
     * world position in meters
     */
    public final float getWorldPositionX()
    {
        return ( data.getWorldPositionX() );
    }
    
    /**
     * world position in meters
     */
    public final float getWorldPositionY()
    {
        return ( data.getWorldPositionY() );
    }
    
    /**
     * world position in meters
     */
    public final float getWorldPositionZ()
    {
        return ( data.getWorldPositionZ() );
    }
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
     * 
     * @param localVel
     */
    public final void getLocalVelocity( TelemVect3 localVel )
    {
        data.getLocalVelocity( localVel );
    }
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
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
        
        return ( mps * TelemetryData.MPS_TO_MPH );
    }
    
    /**
     * velocity (km/h)
     */
    public final float getScalarVelocityKPH()
    {
        float mps = getScalarVelocityMPS();
        
        return ( mps * TelemetryData.MPS_TO_KPH );
    }
    
    /**
     * velocity in the units selected in the PLR.
     */
    public final float getScalarVelocity()
    {
        if ( profileInfo.getSpeedUnits() == SpeedUnits.MPH )
            return ( getScalarVelocityMPH() );
        
        return ( getScalarVelocityKPH() );
    }
    
    /**
     * topspeed in km/h
     */
    public final float getTopspeed()
    {
        return ( topspeed );
    }
    
    /**
     * acceleration (meters/sec^2) in local vehicle coordinates
     * 
     * @param localAccel
     */
    public final void getLocalAcceleration( TelemVect3 localAccel )
    {
        data.getLocalAcceleration( localAccel );
    }
    
    /**
     * top row of orientation matrix (also converts local vehicle vectors into world X using dot product)
     * 
     * @param oriX
     */
    public final void getOrientationX( TelemVect3 oriX )
    {
        data.getOrientationX( oriX );
    }
    
    /**
     * mid row of orientation matrix (also converts local vehicle vectors into world Y using dot product)
     * 
     * @param oriY
     */
    public final void getOrientationY( TelemVect3 oriY )
    {
        data.getOrientationY( oriY );
    }
    
    /**
     * bot row of orientation matrix (also converts local vehicle vectors into world Z using dot product)
     * 
     * @param oriZ
     */
    public final void getOrientationZ( TelemVect3 oriZ )
    {
        data.getOrientationZ( oriZ );
    }
    
    /**
     * rotation (radians/sec) in local vehicle coordinates
     * 
     * @param localRot
     */
    public final void getLocalRotation( TelemVect3 localRot )
    {
        data.getLocalRotation( localRot );
    }
    
    /**
     * rotational acceleration (radians/sec^2) in local vehicle coordinates
     * 
     * @param localRotAccel
     */
    public final void getLocalRotationalAcceleration( TelemVect3 localRotAccel )
    {
        data.getLocalRotationalAcceleration( localRotAccel );
    }
    
    // Future use
    //unsigned char mExpansion[128];
    
    VehicleScoringInfo( ScoringInfo scoringInfo, ProfileInfo profileInfo )
    {
        this.scoringInfo = scoringInfo;
        this.profileInfo = profileInfo;
    }
    
    public static final class VSIPlaceComparator implements Comparator<VehicleScoringInfo>
    {
        public static final VSIPlaceComparator INSTANCE = new VSIPlaceComparator();
        
        public int compare( VehicleScoringInfo vsi1, VehicleScoringInfo vsi2 )
        {
            if ( vsi1.getPlace( false ) < vsi2.getPlace( false ) )
                return ( -1 );
            
            if ( vsi1.getPlace( false ) > vsi2.getPlace( false ) )
                return ( +1 );
            
            return ( 0 );
        }
        
        private VSIPlaceComparator()
        {
        }
    }
}
