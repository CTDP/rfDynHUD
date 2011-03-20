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
package net.ctdp.rfdynhud.widgets.standard.misc;

import java.awt.Font;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.SpeedUnits;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.DelayProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.ValidityTest;
import net.ctdp.rfdynhud.widgets.base.widget.StatefulWidget;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link MiscWidget} displays miscellaneous information like fastest lap, session time, top speed, etc..
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class MiscWidget extends StatefulWidget<Object, LocalStore>
{
    private final BooleanProperty displayScoring = new BooleanProperty( "displayScoring", true );
    private final BooleanProperty displayTiming = new BooleanProperty( "displayTiming", true );
    private final BooleanProperty displayVelocity = new BooleanProperty( "displayVelocity", true );
    
    private final DelayProperty relTopspeedResetDelay = new DelayProperty( "relTopspeedResetDelay", DelayProperty.DisplayUnits.MILLISECONDS, 10000, 0, 60000 ); // ten seconds
    
    private DrawnString scoringString1 = null;
    private DrawnString scoringString2 = null;
    private DrawnString scoringString3 = null;
    
    private DrawnString lapString = null;
    private DrawnString stintString = null;
    private DrawnString sessionTimeString = null;
    
    private DrawnString absTopspeedString = null;
    private DrawnString relTopspeedString = null;
    private DrawnString velocityString = null;
    
    private int oldStintLength = -1;
    private final IntValue leaderID = new IntValue();
    private boolean leaderValid = false;
    private final IntValue place = new IntValue( ValidityTest.GREATER_THAN, 0 );
    private final FloatValue fastestLap = new FloatValue( ValidityTest.GREATER_THAN, 0f );
    private final FloatValue sessionTime = new FloatValue( -1f, 0.1f );
    private final EnumValue<GamePhase> gamePhase1 = new EnumValue<GamePhase>();
    private final EnumValue<GamePhase> gamePhase2 = new EnumValue<GamePhase>();
    private final IntValue lapsCompleted = new IntValue();
    private float oldLapsRemaining = -1f;
    private float oldAbsTopspeed = -1f;
    private float oldRelTopspeed = -1f;
    private float relTopspeed = -1f;
    private int oldVelocity = -1;
    private boolean updateAbs = false;
    
    private int[] scoringColWidths = new int[ 2 ];
    private int[] timingColWidths = new int[ 2 ];
    private int[] velocityColWidths = new int[ 3 ];
    private final Alignment[] scoringAlignment = new Alignment[] { Alignment.RIGHT, Alignment.LEFT };
    private final Alignment[] timingAlignment = new Alignment[] { Alignment.RIGHT, Alignment.LEFT };
    private final Alignment[] velocityAlignment = new Alignment[] { Alignment.RIGHT, Alignment.LEFT, Alignment.LEFT };
    private static final int padding = 4;
    
    public MiscWidget()
    {
        super( StandardWidgetSet.INSTANCE, StandardWidgetSet.WIDGET_PACKAGE, 66.25f, 5.83f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        getFontProperty().setFont( "Dialog", Font.PLAIN, 5, false, true );
        displayTiming.setBooleanValue( false );
        displayVelocity.setBooleanValue( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( displayScoring, "Display the scoring part of the Widget?" );
        writer.writeProperty( displayTiming, "Display the timing part of the Widget?" );
        writer.writeProperty( displayVelocity, "Display the velocity and top speed part of the Widget?" );
        writer.writeProperty( relTopspeedResetDelay, "The delay after which the relative topspeed is resetted (in milliseconds)." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( displayScoring ) );
        else if ( loader.loadProperty( displayTiming ) );
        else if ( loader.loadProperty( displayVelocity ) );
        else if ( loader.loadProperty( relTopspeedResetDelay ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Misc" );
        
        propsCont.addProperty( displayScoring );
        propsCont.addProperty( displayTiming );
        propsCont.addProperty( displayVelocity );
        
        propsCont.addProperty( relTopspeedResetDelay );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object createGeneralStore()
    {
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public LocalStore createLocalStore()
    {
        return ( new LocalStore() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNeededData()
    {
        return ( Widget.NEEDED_DATA_SCORING );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, boolean isEditorMode )
    {
        super.onSessionStarted( sessionType, gameData, isEditorMode );
        
        oldAbsTopspeed = -1f;
        
        getLocalStore().lastDisplayedAbsTopspeed = 0f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        oldStintLength = -1;
        
        leaderID.reset();
        leaderValid = false;
        
        place.reset();
        fastestLap.reset();
        
        sessionTime.reset();
        gamePhase1.reset();
        gamePhase2.reset();
        lapsCompleted.reset();
        oldLapsRemaining = -1f;
        
        oldRelTopspeed = -1f;
        relTopspeed = -1f;
        getLocalStore().lastRelTopspeedTime = -1L;
        oldVelocity = -1;
        
        updateAbs = false;
        
        //forceReinitialization();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        oldRelTopspeed = -1f;
        relTopspeed = -1f;
        getLocalStore().lastRelTopspeedTime = -1L;
        getLocalStore().lastDisplayedAbsTopspeed = viewedVSI.getTopspeed();
        oldAbsTopspeed = -1f;
        oldVelocity = -1;
        
        updateAbs = true;
        
        return ( result );
    }
    
    private static final String getSpeedUnits( SpeedUnits speedUnits )
    {
        if ( speedUnits == SpeedUnits.MPH )
            return ( Loc.velocity_units_IMPERIAL );
        
        return ( Loc.velocity_units_METRIC );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        final java.awt.Font font = getFont();
        final boolean fontAntiAliased = isFontAntiAliased();
        final java.awt.Color fontColor = getFontColor();
        
        final int left = 2;
        final int center = width / 2;
        final int right = width - 2;
        final int top = -2;
        
        final String speedUnits = getSpeedUnits( gameData.getProfileInfo().getSpeedUnits() );
        
        {
            boolean b = displayScoring.getBooleanValue();
            
            scoringString1 = dsf.newDrawnStringIf( b, "scoringString1", left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            scoringString2 = dsf.newDrawnStringIf( b, "scoringString2", null, scoringString1, left, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            scoringString3 = dsf.newDrawnStringIf( b, "scoringString3", null, scoringString2, left, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        }
        
        {
            boolean b = displayTiming.getBooleanValue();
            if ( displayScoring.getBooleanValue() && displayVelocity.getBooleanValue() )
            {
                lapString = dsf.newDrawnStringIf( b, "lapString", center, top, Alignment.CENTER, false, font, fontAntiAliased, fontColor );
                stintString = dsf.newDrawnStringIf( b, "stintString", null, lapString, center, 0, Alignment.CENTER, false, font, fontAntiAliased, fontColor );
                sessionTimeString = dsf.newDrawnStringIf( b, "sessionTimeString", null, stintString, center, 0, Alignment.CENTER, false, font, fontAntiAliased, fontColor );
            }
            else if ( !displayScoring.getBooleanValue() )
            {
                lapString = dsf.newDrawnStringIf( b, "lapString", left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
                stintString = dsf.newDrawnStringIf( b, "stintString", null, lapString, left, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
                sessionTimeString = dsf.newDrawnStringIf( b, "sessionTimeString", null, stintString, left, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            }
            else if ( !displayVelocity.getBooleanValue() )
            {
                lapString = dsf.newDrawnStringIf( b, "lapString", right, top, Alignment.RIGHT, false, font, fontAntiAliased, fontColor );
                stintString = dsf.newDrawnStringIf( b, "stintString", null, lapString, right, 0, Alignment.RIGHT, false, font, fontAntiAliased, fontColor );
                sessionTimeString = dsf.newDrawnStringIf( b, "sessionTimeString", null, stintString, right, 0, Alignment.RIGHT, false, font, fontAntiAliased, fontColor );
            }
        }
        
        if ( displayVelocity.getBooleanValue() )
        {
            if ( displayScoring.getBooleanValue() || displayTiming.getBooleanValue() )
            {
                absTopspeedString = dsf.newDrawnString( "absTopspeedString", right, top, Alignment.RIGHT, false, font, fontAntiAliased, fontColor );
                relTopspeedString = dsf.newDrawnString( "relTopspeedString", null, absTopspeedString, right, 0, Alignment.RIGHT, false, font, fontAntiAliased, fontColor );
                velocityString = dsf.newDrawnString( "velocityString", null, relTopspeedString, right, 0, Alignment.RIGHT, false, font, fontAntiAliased, fontColor );
            }
            else
            {
                absTopspeedString = dsf.newDrawnString( "absTopspeedString", left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
                relTopspeedString = dsf.newDrawnString( "relTopspeedString", null, absTopspeedString, left, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
                velocityString = dsf.newDrawnString( "velocityString", null, relTopspeedString, left, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            }
            
            velocityColWidths[0] = 0;
            velocityColWidths[1] = 0;
            velocityColWidths[2] = 0;
            
            absTopspeedString.getMaxColWidths( new String[] { Loc.velocity_topspeed1_prefix + ":", "000.0", speedUnits }, velocityAlignment, padding, velocityColWidths );
            relTopspeedString.getMaxColWidths( new String[] { Loc.velocity_topspeed2_prefix + ":", "000.0", speedUnits }, velocityAlignment, padding, velocityColWidths );
            velocityString.getMaxColWidths( new String[] { Loc.velocity_velocity_prefix + ":", "000", speedUnits }, velocityAlignment, padding, velocityColWidths );
        }
        else
        {
            absTopspeedString = null;
            relTopspeedString = null;
            velocityString = null;
        }
    }
    
    private void updateScoringColWidths( ScoringInfo scoringInfo, String leaderName )
    {
        scoringColWidths[0] = 0;
        scoringColWidths[1] = 0;
        
        scoringString1.getMaxColWidths( new String[] { Loc.scoring_leader_prefix + ":", leaderName }, scoringAlignment, padding, scoringColWidths );
        if ( place.isValid() )
            scoringString2.getMaxColWidths( new String[] { Loc.scoring_place_prefix + ":", place.getValueAsString() + "/" + scoringInfo.getNumVehicles() }, scoringAlignment, padding, scoringColWidths );
        else
            scoringString2.getMaxColWidths( new String[] { Loc.scoring_place_prefix + ":", Loc.scoring_place_na }, scoringAlignment, padding, scoringColWidths );
        
        if ( fastestLap.isValid() )
            scoringString3.getMaxColWidths( new String[] { Loc.scoring_fastest_lap_prefix + ":", TimingUtil.getTimeAsString( fastestLap.getValue(), true ) + " (" + scoringInfo.getFastestLapVSI().getDriverNameShort() + ")" }, scoringAlignment, padding, scoringColWidths );
        else
            scoringString3.getMaxColWidths( new String[] { Loc.scoring_fastest_lap_prefix + ":", Loc.scoring_fastest_lap_na }, scoringAlignment, padding, scoringColWidths );
    }
    
    private void updateTimingColWidths( String[] lapStringValue, String[] stintStringValue, String[] sessionTimeStringValue )
    {
        timingColWidths[0] = 0;
        timingColWidths[1] = 0;
        
        lapString.getMaxColWidths( lapStringValue, timingAlignment, padding, timingColWidths );
        stintString.getMaxColWidths( stintStringValue, timingAlignment, padding, timingColWidths );
        sessionTimeString.getMaxColWidths( sessionTimeStringValue, timingAlignment, padding, timingColWidths );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        
        if ( displayScoring.getBooleanValue() )
        {
            VehicleScoringInfo leaderVSI = scoringInfo.getLeadersVehicleScoringInfo();
            leaderID.update( leaderVSI.getDriverId() );
            String leaderName = leaderVSI.getDriverNameShort();
            place.update( vsi.getPlace( getConfiguration().getUseClassScoring() ) );
            VehicleScoringInfo fastestLapVSI = scoringInfo.getFastestLapVSI();
            String fastestLapper = fastestLapVSI.getDriverNameShort( false );
            Laptime fl = fastestLapVSI.getFastestLaptime();
            fastestLap.update( ( fl == null ) ? -1f : fl.getLapTime() );
            
            boolean colWidthsUpdated = false;
            
            boolean lv = scoringInfo.getSessionType().isRace() || ( leaderVSI.getFastestLaptime() != null );
            if ( needsCompleteRedraw || leaderID.hasChanged() || ( lv != leaderValid ) )
            {
                leaderValid = lv;
                
                if ( !colWidthsUpdated )
                {
                    updateScoringColWidths( scoringInfo, leaderName );
                    colWidthsUpdated = true;
                }
                
                if ( scoringInfo.getSessionType().isRace() )
                {
                    if ( leaderValid )
                        scoringString1.drawColumns( offsetX, offsetY, new String[] { Loc.scoring_leader_prefix + ":", leaderName }, scoringAlignment, padding, scoringColWidths, texture );
                    else
                        scoringString1.drawColumns( offsetX, offsetY, new String[] { Loc.scoring_leader_prefix + ":", Loc.scoring_leader_na }, scoringAlignment, padding, scoringColWidths, texture );
                }
                else
                {
                    scoringString1.draw( offsetX, offsetY, "", texture );
                }
            }
            
            if ( needsCompleteRedraw || place.hasValidityChanged() || place.hasChanged() )
            {
                if ( !colWidthsUpdated )
                {
                    updateScoringColWidths( scoringInfo, leaderName );
                    colWidthsUpdated = true;
                }
                
                if ( place.isValid() )
                    scoringString2.drawColumns( offsetX, offsetY, new String[] { Loc.scoring_place_prefix + ":", place.getValueAsString() + "/" + ( getConfiguration().getUseClassScoring() ? vsi.getNumVehiclesInSameClass() : scoringInfo.getNumVehicles() ) }, scoringAlignment, padding, scoringColWidths, texture );
                else
                    scoringString2.drawColumns( offsetX, offsetY, new String[] { Loc.scoring_place_prefix + ":", Loc.scoring_place_na }, scoringAlignment, padding, scoringColWidths, texture );
            }
            
            if ( needsCompleteRedraw || fastestLap.hasValidityChanged() || fastestLap.hasChanged() )
            {
                if ( !colWidthsUpdated )
                {
                    updateScoringColWidths( scoringInfo, leaderName );
                    colWidthsUpdated = true;
                }
                
                if ( fastestLap.isValid() )
                    scoringString3.drawColumns( offsetX, offsetY, new String[] { Loc.scoring_fastest_lap_prefix + ":", TimingUtil.getTimeAsLaptimeString( fastestLap.getValue() ) + " (" + fastestLapper + ")" }, scoringAlignment, padding, scoringColWidths, texture );
                else
                    scoringString3.drawColumns( offsetX, offsetY, new String[] { Loc.scoring_fastest_lap_prefix + ":", Loc.scoring_fastest_lap_na }, scoringAlignment, padding, scoringColWidths, texture );
            }
        }
        
        if ( displayTiming.getBooleanValue() )
        {
            String[] lapStringValue = new String[ 2 ];
            String[] stintStringValue = new String[ 2 ];
            String[] sessionTimeStringValue = new String[ 2 ];
            boolean needsLapRedraw = false;
            boolean needsStintRedraw = false;
            boolean needsTimeRedraw = false;
            
            if ( gameData.getProfileInfo().getShowCurrentLap() )
                lapStringValue[0] = Loc.timing_current_lap_prefix + ":";
            else
                lapStringValue[0] = Loc.timing_laps_done_prefix + ":";
            
            lapsCompleted.update( vsi.getLapsCompleted() );
            final int maxLaps = scoringInfo.getEstimatedMaxLaps( vsi );
            if ( maxLaps > 0 )
            {
                float lapsRemaining = vsi.getLapsRemaining( maxLaps );
                float roundedLapsRemaining;
                if ( vsi.getNormalizedLapDistance() >= 0.9f )
                    roundedLapsRemaining = (float)Math.ceil( lapsRemaining * 10f ) / 10f;
                else
                    roundedLapsRemaining = Math.round( lapsRemaining * 10f ) / 10f;
                
                if ( scoringInfo.getSessionType().isRace() && ( ( scoringInfo.getGamePhase() == GamePhase.BEFORE_SESSION_HAS_BEGUN ) || ( scoringInfo.getGamePhase() == GamePhase.FORMATION_LAP ) ) )
                    lapStringValue[1] = "0 / " + maxLaps + " / " + maxLaps;
                else if ( gameData.getProfileInfo().getShowCurrentLap() )
                    lapStringValue[1] = ( lapsCompleted.getValue() + 1 ) + " / " + maxLaps + " / " + NumberUtil.formatFloat( roundedLapsRemaining, 1, true );
                else
                    lapStringValue[1] = lapsCompleted + " / " + maxLaps + " / " + NumberUtil.formatFloat( roundedLapsRemaining, 1, true );
                
                gamePhase1.update( scoringInfo.getGamePhase() );
                
                if ( needsCompleteRedraw || ( roundedLapsRemaining != oldLapsRemaining ) || gamePhase1.hasChanged() )
                {
                    oldLapsRemaining = roundedLapsRemaining;
                    
                    needsLapRedraw = true;
                }
            }
            else
            {
                if ( gameData.getProfileInfo().getShowCurrentLap() )
                    lapStringValue[1] = String.valueOf( lapsCompleted.getValue() + 1 );
                else
                    lapStringValue[1] = String.valueOf( lapsCompleted );
                
                if ( needsCompleteRedraw || lapsCompleted.hasChanged() )
                {
                    needsLapRedraw = true;
                }
            }
            
            {
                stintStringValue[0] = Loc.timing_stintlength_prefix + ":";
                
                int stintLength = (int)( ( !isEditorMode ? vsi.getStintLength() : 5.2f ) * 10f );
                boolean changed = ( stintLength != oldStintLength );
                if ( vsi.isInPits() )
                {
                    if ( oldStintLength < 0 )
                        stintStringValue[1] = Loc.timing_stintlength_na;
                    else
                        stintStringValue[1] = String.valueOf( Math.round( oldStintLength / 10f ) );
                }
                else
                {
                    oldStintLength = stintLength;
                    
                    stintStringValue[1] = String.valueOf( oldStintLength / 10f );
                }
                
                if ( needsCompleteRedraw || changed )
                {
                    needsStintRedraw = true;
                }
            }
            
            sessionTimeStringValue[0] = Loc.timing_sessiontime_prefix + ":";
            
            sessionTime.update( gameData.getScoringInfo().getSessionTime() );
            gamePhase2.update( scoringInfo.getGamePhase() );
            float endTime = gameData.getScoringInfo().getEndTime();
            
            if ( scoringInfo.getGamePhase() == GamePhase.SESSION_OVER )
                sessionTimeStringValue[1] = "00:00:00";
            else if ( scoringInfo.getSessionType().isRace() && ( ( scoringInfo.getGamePhase() == GamePhase.FORMATION_LAP ) || ( endTime < 0f ) || ( endTime > 3000000f ) ) )
                sessionTimeStringValue[1] = "--:--:--";
            else if ( scoringInfo.getSessionType().isTestDay() || ( endTime < 0f ) || ( endTime > 3000000f ) )
                sessionTimeStringValue[1] = TimingUtil.getTimeAsString( sessionTime.getValue(), true, false );
            else
                sessionTimeStringValue[1] = TimingUtil.getTimeAsString( Math.min( sessionTime.getValue() - endTime, 0f ), true, false );
            
            if ( needsCompleteRedraw || ( clock.c() && ( sessionTime.hasChanged( false ) || gamePhase2.hasChanged( false ) ) ) )
            {
                sessionTime.setUnchanged();
                gamePhase2.setUnchanged();
                
                needsTimeRedraw = true;
            }
            
            boolean colWidthsUpdated = false;
            
            if ( needsLapRedraw )
            {
                if ( !colWidthsUpdated )
                {
                    updateTimingColWidths( lapStringValue, stintStringValue, sessionTimeStringValue );
                    
                    colWidthsUpdated = true;
                }
                
                lapString.drawColumns( offsetX, offsetY, lapStringValue, timingAlignment, padding, timingColWidths, texture );
            }
            
            if ( needsStintRedraw )
            {
                if ( !colWidthsUpdated )
                {
                    updateTimingColWidths( lapStringValue, stintStringValue, sessionTimeStringValue );
                    
                    colWidthsUpdated = true;
                }
                
                stintString.drawColumns( offsetX, offsetY, stintStringValue, timingAlignment, padding, timingColWidths, texture );
            }
            
            if ( needsTimeRedraw )
            {
                if ( !colWidthsUpdated )
                {
                    updateTimingColWidths( lapStringValue, stintStringValue, sessionTimeStringValue );
                    
                    colWidthsUpdated = true;
                }
                
                sessionTimeString.drawColumns( offsetX, offsetY, sessionTimeStringValue, timingAlignment, padding, timingColWidths, texture );
            }
        }
        
        if ( displayVelocity.getBooleanValue() )
        {
            float floatVelocity = vsi.isPlayer() ? gameData.getTelemetryData().getScalarVelocity() : vsi.getScalarVelocity();
            int velocity = Math.round( floatVelocity );
            
            if ( floatVelocity >= relTopspeed )
            {
                relTopspeed = floatVelocity;
                getLocalStore().lastRelTopspeedTime = scoringInfo.getSessionNanos();
            }
            else if ( ( getLocalStore().lastRelTopspeedTime + relTopspeedResetDelay.getDelayNanos() < scoringInfo.getSessionNanos() ) && ( floatVelocity < relTopspeed - 50f ) )
            {
                relTopspeed = floatVelocity;
                oldRelTopspeed = -1f;
                //lastRelTopspeedTime = scoringInfo.getSessionNanos();
                
                updateAbs = true;
            }
            
            boolean forceUpdateAbs = false;
            float topspeed = vsi.getTopspeed();
            if ( topspeed < getLocalStore().lastDisplayedAbsTopspeed )
            {
                getLocalStore().lastDisplayedAbsTopspeed = 0f;
                oldAbsTopspeed = -1f;
                topspeed = 0f;
                updateAbs = true;
                forceUpdateAbs = true;
            }
            
            if ( isEditorMode )
            {
                topspeed = 301.7f;
                getLocalStore().lastDisplayedAbsTopspeed = topspeed;
                relTopspeed = 274.3f;
            }
            
            final String speedUnits = getSpeedUnits( gameData.getProfileInfo().getSpeedUnits() );
            
            if ( needsCompleteRedraw || ( ( clock.c() || forceUpdateAbs ) && updateAbs && ( topspeed > oldAbsTopspeed ) ) )
            {
                if ( !needsCompleteRedraw )
                    getLocalStore().lastDisplayedAbsTopspeed = topspeed;
                
                updateAbs = false;
                oldAbsTopspeed = topspeed;
                absTopspeedString.drawColumns( offsetX, offsetY, new String[] { Loc.velocity_topspeed1_prefix + ":", NumberUtil.formatFloat( getLocalStore().lastDisplayedAbsTopspeed, 1, true ), speedUnits }, velocityAlignment, padding, velocityColWidths, texture );
            }
            
            if ( needsCompleteRedraw || ( clock.c() && ( relTopspeed > oldRelTopspeed ) ) )
            {
                oldRelTopspeed = relTopspeed;
                relTopspeedString.drawColumns( offsetX, offsetY, new String[] { Loc.velocity_topspeed2_prefix + ":", NumberUtil.formatFloat( oldRelTopspeed, 1, true ), speedUnits }, velocityAlignment, padding, velocityColWidths, texture );
            }
            
            if ( needsCompleteRedraw || ( clock.c() && ( velocity != oldVelocity ) ) )
            {
                oldVelocity = velocity;
                velocityString.drawColumns( offsetX, offsetY, new String[] { Loc.velocity_velocity_prefix + ":", String.valueOf( velocity ), speedUnits }, velocityAlignment, padding, velocityColWidths, texture );
            }
        }
    }
}
