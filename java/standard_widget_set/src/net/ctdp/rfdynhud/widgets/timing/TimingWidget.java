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
package net.ctdp.rfdynhud.widgets.timing;

import java.io.IOException;
import java.util.Arrays;

import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.LongValue;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link TimingWidget} displays lap- and sector times and gaps.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TimingWidget extends Widget
{
    private final BooleanProperty displayAbsFastest = new BooleanProperty( this, "displayAbsFastest", true );
    private final BooleanProperty cumulativeSectors = new BooleanProperty( this, "cumulativeSectors", false );
    private final BooleanProperty forceCurrentCumulSectors = new BooleanProperty( this, "forceCurrentCumulSectors", "forceCurrCumulSects", true );
    private final IntProperty lastLapDisplayDelay = new IntProperty( this, "lastLapDisplayDelay", 10000, -100, Integer.MAX_VALUE ); // ten seconds
    
    private final ColorProperty slowerColor = new ColorProperty( this, "slowerColor", "#FF7248" );
    private final ColorProperty fasterColor = new ColorProperty( this, "fasterColor", "#6AFF3D" );
    
    private DrawnString absFastestLapHeaderString = null;
    private DrawnString absFastestLapDriverString = null;
    private DrawnString absSector1String = null;
    private DrawnString absSector2String = null;
    private DrawnString absSector3String = null;
    private DrawnString absFastestLapString = null;
    
    private DrawnString ownFastestLapHeaderString = null;
    private DrawnString ownSector1String = null;
    private DrawnString ownSector2String = null;
    private DrawnString ownSector3String = null;
    private DrawnString ownFastestLapString = null;
    
    private DrawnString currLapHeaderString = null;
    private DrawnString currSector1String = null;
    private DrawnString currSector2String = null;
    private DrawnString currSector3String = null;
    private DrawnString currLapString = null;
    
    private final IntValue leaderID = new IntValue();
    private int oldAbsFastestLap = -1;
    private boolean absFLValid = false;
    private int oldOwnFastestLap = -1;
    private boolean ownFLValid = false;
    private final BoolValue currLapValid = new BoolValue();
    
    private float lastLapDisplayTime = -1f;
    private float gapOFSec1 = 0f;
    private boolean gapOFSec1Valid = false;
    private float gapOFSec2 = 0f;
    private boolean gapOFSec2Valid = false;
    private float gapOFSec3 = 0f;
    private boolean gapOFSec3Valid = false;
    private float gapOFLap = 0f;
    private float gapAFSec1 = 0f;
    private boolean gapAFSec1Valid = false;
    private float gapAFSec2 = 0f;
    private boolean gapAFSec2Valid = false;
    private float gapAFSec3 = 0f;
    private boolean gapAFSec3Valid = false;
    private float gapAFLap = 0f;
    
    private final java.awt.Color[] fontColors = new java.awt.Color[ 5 ];
    
    private Laptime delayedAbsFastestLap = null;
    private Laptime delayedOwnFastestLap = null;
    
    private boolean delayedAbsFastestIsOwn = false;
    
    private int colPadding = 10;
    
    private final LongValue scoringInfoUpdateID = new LongValue();
    private final String[][] oldClStrings = new String[ 4 ][ 0 ];
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE_TIMING );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, boolean isEditorMode )
    {
        super.onSessionStarted( sessionType, gameData, isEditorMode );
        
        leaderID.reset();
        
        oldAbsFastestLap = -1;
        oldOwnFastestLap = -1;
        
        absFLValid = false;
        ownFLValid = false;
        currLapValid.reset( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        currLapValid.reset( true );
        
        lastLapDisplayTime = -1f;
        
        scoringInfoUpdateID.reset( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPitsExited( LiveGameData gameData, boolean isEditorMode )
    {
        super.onPitsExited( gameData, isEditorMode );
        
        currLapValid.reset( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, boolean isEditorMode )
    {
        super.onLapStarted( vsi, gameData, isEditorMode );
        
        if ( vsi == gameData.getScoringInfo().getViewedVehicleScoringInfo() )
        {
            if ( vsi.getStintLength() < 1.9f )
                lastLapDisplayTime = -1f;
            else if ( lastLapDisplayDelay.getIntValue() < 0 )
                lastLapDisplayTime = vsi.getLapStartTime() + ( vsi.getLaptime( vsi.getLapsCompleted() ).getSector1() * -lastLapDisplayDelay.getIntValue() / 100f );
            else
                lastLapDisplayTime = vsi.getLapStartTime() + ( lastLapDisplayDelay.getIntValue() / 1000f );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        lastLapDisplayTime = -1f;
        scoringInfoUpdateID.reset( true );
        leaderID.reset();
        
        return ( result );
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
        
        final int left1 = 2;
        final int left2 = left1 + 10;
        int top = -2;
        final int sectionGap = 7;
        
        DrawnString yRel = null;
        
        if ( displayAbsFastest.getBooleanValue() )
        {
            absFastestLapHeaderString = dsf.newDrawnString( "absFastestLapHeaderString", left1, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            absFastestLapDriverString = dsf.newDrawnString( "absFastestLapDriverString", null, absFastestLapHeaderString, left2, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, Loc.abs_fastest_header_prefix + ": ", null );
            absSector1String = dsf.newDrawnString( "absSector1String", null, absFastestLapDriverString, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            absSector2String = dsf.newDrawnString( "absSector2String", null, absSector1String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            if ( !cumulativeSectors.getBooleanValue() )
            {
                absSector3String = dsf.newDrawnString( "absSector3String", null, absSector2String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
                yRel = absSector3String;
            }
            else
            {
                absSector3String = null;
                yRel = absSector2String;
            }
            absFastestLapString = dsf.newDrawnString( "absFastestLapString", null, yRel, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            yRel = absFastestLapString;
            top = sectionGap;
        }
        else
        {
            absFastestLapHeaderString = null;
            absFastestLapDriverString = null;
            absSector1String = null;
            absSector2String = null;
            absSector3String = null;
            absFastestLapString = null;
            //yRel = null;
        }
        
        ownFastestLapHeaderString = dsf.newDrawnString( "ownFastestLapHeaderString", null, yRel, left1, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        ownSector1String = dsf.newDrawnString( "ownSector1String", null, ownFastestLapHeaderString, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        ownSector2String = dsf.newDrawnString( "ownSector2String", null, ownSector1String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        if ( !cumulativeSectors.getBooleanValue() )
        {
            ownSector3String = dsf.newDrawnString( "ownSector3String", null, ownSector2String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            yRel = ownSector3String;
        }
        else
        {
            ownSector3String = null;
            yRel = ownSector2String;
        }
        ownFastestLapString = dsf.newDrawnString( "ownFastestLapString", null, yRel, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        
        currLapHeaderString = dsf.newDrawnString( "currLapHeaderString", null, ownFastestLapString, left1, sectionGap, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        currSector1String = dsf.newDrawnString( "currSector1String", null, currLapHeaderString, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        currSector2String = dsf.newDrawnString( "currSector2String", null, currSector1String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        if ( !cumulativeSectors.getBooleanValue() && !forceCurrentCumulSectors.getBooleanValue() )
        {
            currSector3String = dsf.newDrawnString( "currSector3String", null, currSector2String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            yRel = currSector3String;
        }
        else
        {
            currSector3String = null;
            yRel = currSector2String;
        }
        currLapString = dsf.newDrawnString( "currLapString", null, yRel, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        
        fontColors[0] = getFontColor();
        fontColors[1] = getFontColor();
        fontColors[2] = getFontColor();
        fontColors[3] = getFontColor();
        fontColors[4] = getFontColor();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        scoringInfoUpdateID.update( scoringInfo.getUpdateId() );
        
        VehicleScoringInfo myVSI = scoringInfo.getViewedVehicleScoringInfo();
        
        VehicleScoringInfo afVSI = scoringInfo.getFastestLapVSI();
        boolean absFastestIsSecond = false;
        if ( afVSI == myVSI )
        {
            if ( scoringInfo.getSecondFastestLapVSI() != null )
            {
                afVSI = scoringInfo.getSecondFastestLapVSI();
                absFastestIsSecond = true;
            }
        }
        
        if ( isEditorMode && ( afVSI == myVSI ) )
        {
            // Just to get differences...
            
            if ( afVSI == scoringInfo.getLeadersVehicleScoringInfo() )
                afVSI = scoringInfo.getVehicleScoringInfo( 1 );
            else
                afVSI = scoringInfo.getLeadersVehicleScoringInfo();
        }
        
        Laptime afLaptime = afVSI.getFastestLaptime();
        
        if ( displayAbsFastest.getBooleanValue() )
        {
            // absolute fastest lap
            
            Laptime lt = afVSI.getFastestLaptime();
            float lap = ( lt == null ) ? -1f : lt.getLapTime();
            boolean lv = ( lap > 0f );
            String leaderName = lv ? afVSI.getDriverName( false ) : "";
            leaderID.update( lv ? scoringInfo.getFastestLapVSI().getDriverId() : -1 );
            
            if ( needsCompleteRedraw || leaderID.hasChanged() )
            {
                if ( absFastestIsSecond )
                    absFastestLapHeaderString.draw( offsetX, offsetY, Loc.abs_second_fastest_prefix + ":", texture );
                else
                    absFastestLapHeaderString.draw( offsetX, offsetY, Loc.abs_fastest_prefix + ":", texture );
                absFastestLapDriverString.draw( offsetX, offsetY, leaderName, texture );
            }
            
            int lap_ = Math.round( lap * 10000f );
            
            if ( needsCompleteRedraw || ( lap_ != oldAbsFastestLap ) || ( lv != absFLValid ) )
            {
                oldAbsFastestLap = lap_;
                absFLValid = lv;
                
                if ( absFLValid )
                {
                    float sec1 = ( afLaptime != null ) ? afLaptime.getSector1() : -1f;
                    float sec2 = ( afLaptime != null ) ? afLaptime.getSector2( cumulativeSectors.getBooleanValue() ) : -1f;
                    float sec3 = ( afLaptime != null ) ? ( cumulativeSectors.getBooleanValue() ? sec2 + afLaptime.getSector3() : afLaptime.getSector3() ) : -1f;
                    
                    int cols = 3;
                    String[][] s = new String[4][cols];
                    int[] colWidths = new int[cols];
                    final Alignment[] aligns = { Alignment.RIGHT, Alignment.LEFT, Alignment.RIGHT };
                    
                    s[0][0] = Loc.timing_sector1_prefix + ":";
                    s[0][1] = null;
                    if ( sec1 > 0f )
                        s[0][2] = TimingUtil.getTimeAsLaptimeString( sec1 );
                    else
                        s[0][2] = "-:--.---";
                    
                    s[1][0] = Loc.timing_sector2_prefix + ":";
                    s[1][1] = null;
                    if ( sec2 > 0f )
                        s[1][2] = TimingUtil.getTimeAsLaptimeString( sec2 );
                    else
                        s[1][2] = "-:--.---";
                    
                    if ( !cumulativeSectors.getBooleanValue() )
                    {
                        s[2][0] = Loc.timing_sector3_prefix + ":";
                        s[2][1] = null;
                        if ( sec3 > 0f )
                            s[2][2] = TimingUtil.getTimeAsLaptimeString( sec3 );
                        else
                            s[2][2] = "-:--.---";
                    }
                    else
                    {
                        s[2][0] = null;
                        s[2][1] = null;
                        s[2][2] = "";
                    }
                    
                    s[3][0] = Loc.timing_lap_prefix + ":";
                    s[3][1] = null;
                    s[3][2] = TimingUtil.getTimeAsLaptimeString( lap );
                    
                    absSector1String.getMaxColWidths( s[0], aligns, colPadding, colWidths );
                    absSector2String.getMaxColWidths( s[1], aligns, colPadding, colWidths );
                    if ( !cumulativeSectors.getBooleanValue() )
                        absSector3String.getMaxColWidths( s[2], aligns, colPadding, colWidths );
                    absFastestLapString.getMaxColWidths( s[3], aligns, colPadding, colWidths );
                    
                    absSector1String.drawColumns( offsetX, offsetY, s[0], aligns, colPadding, colWidths, texture );
                    absSector2String.drawColumns( offsetX, offsetY, s[1], aligns, colPadding, colWidths, texture );
                    if ( !cumulativeSectors.getBooleanValue() )
                        absSector3String.drawColumns( offsetX, offsetY, s[2], aligns, colPadding, colWidths, texture );
                    absFastestLapString.drawColumns( offsetX, offsetY, s[3], aligns, colPadding, colWidths, texture );
                }
                else
                {
                    absSector1String.draw( offsetX, offsetY, "-:--.---", texture );
                    absSector2String.draw( offsetX, offsetY, "-:--.---", texture );
                    if ( !cumulativeSectors.getBooleanValue() )
                        absSector3String.draw( offsetX, offsetY, "-:--.---", texture );
                    absFastestLapString.draw( offsetX, offsetY, "-:--.---", texture );
                }
            }
        }
        
        {
            // own fastest lap
            
            Laptime laptime = myVSI.getFastestLaptime();
            float lap = myVSI.getBestLapTime();
            
            if ( needsCompleteRedraw )
            {
                ownFastestLapHeaderString.draw( offsetX, offsetY, "Own fastest Lap:", texture );
            }
            
            int lap_ = Math.round( lap * 10000f );
            
            boolean lv = ( lap > 0f );
            if ( needsCompleteRedraw || ( lap_ != oldOwnFastestLap ) || ( lv != ownFLValid ) )
            {
                oldOwnFastestLap = lap_;
                ownFLValid = lv;
                
                if ( ownFLValid )
                {
                    boolean displayCumul = cumulativeSectors.getBooleanValue();
                    
                    float afSec1 = ( afLaptime != null ) ? afLaptime.getSector1() : -1f; //afVSI.getBestSector1();
                    float afSec2 = ( afLaptime != null ) ? afLaptime.getSector2( displayCumul ) : -1f; //afVSI.getBestSector2( displayCumul );
                    float afSec3 = ( afLaptime != null ) ? ( displayCumul ? afSec2 + afLaptime.getSector3() : afLaptime.getSector3() ) : -1f; //displayCumul ? afSec2 + afVSI.getBestSector3() : afVSI.getBestSector3();
                    float afLap = ( afLaptime != null ) ? afLaptime.getLapTime() : -1f;// afVSI.getBestLapTime();
                    
                    float sec1 = ( laptime != null ) ? laptime.getSector1() : -1f; //vsi.getBestSector1();
                    float sec2 = ( laptime != null ) ? laptime.getSector2( displayCumul ) : -1f; //vsi.getBestSector2( displayCumul );
                    float sec3 = ( laptime != null ) ? ( displayCumul ? sec2 + laptime.getSector3() : laptime.getSector3() ) : -1f; //displayCumul ? sec2 + vsi.getBestSector3() : vsi.getBestSector3();
                    
                    final boolean dispGapToAbs = ( displayAbsFastest.getBooleanValue() && ( myVSI != afVSI ) );
                    int cols = dispGapToAbs ? 4 : 3;
                    String[][] s = new String[4][cols];
                    int[] colWidths = new int[cols];
                    final Alignment[] aligns = ( myVSI != afVSI ) ? new Alignment[] { Alignment.RIGHT, Alignment.LEFT, Alignment.RIGHT, Alignment.RIGHT } : new Alignment[] { Alignment.RIGHT, Alignment.LEFT, Alignment.RIGHT };
                    
                    java.awt.Color sfColor1 = getFontColor();
                    
                    s[0][0] = Loc.timing_sector1_prefix + ":";
                    s[0][1] = null;
                    if ( sec1 > 0f )
                        s[0][2] = TimingUtil.getTimeAsLaptimeString( sec1 );
                    else
                        s[0][2] = "-:--.---";
                    if ( dispGapToAbs )
                    {
                        if ( afSec1 < 0f )
                        {
                            s[0][3] = null;
                        }
                        else if ( sec1 > 0f )
                        {
                            s[0][3] = "(" + TimingUtil.getTimeAsGapString( sec1 - afSec1 ) + ")";
                            sfColor1 = ( sec1 - afSec1 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                        }
                        else
                        {
                            s[0][3] = null;
                        }
                    }
                    
                    java.awt.Color sfColor2 = getFontColor();
                    
                    s[1][0] = Loc.timing_sector2_prefix + ":";
                    s[1][1] = null;
                    if ( sec2 > 0f )
                        s[1][2] = TimingUtil.getTimeAsLaptimeString( sec2 );
                    else
                        s[1][2] = "-:--.---";
                    if ( dispGapToAbs )
                    {
                        if ( afSec2 < 0f )
                        {
                            s[1][3] = null;
                        }
                        else if ( sec2 > 0f )
                        {
                            s[1][3] = "(" + TimingUtil.getTimeAsGapString( sec2 - afSec2 ) + ")";
                            sfColor2 = ( sec2 - afSec2 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                        }
                        else
                        {
                            s[1][3] = null;
                        }
                    }
                    
                    java.awt.Color sfColor3 = getFontColor();
                    
                    if ( !displayCumul )
                    {
                        s[2][0] = Loc.timing_sector3_prefix + ":";
                        s[2][1] = null;
                        if ( sec3 > 0f )
                            s[2][2] = TimingUtil.getTimeAsLaptimeString( sec3 );
                        else
                            s[2][2] = "-:--.---";
                        if ( dispGapToAbs )
                        {
                            if ( afSec3 < 0f )
                            {
                                s[2][3] = null;
                            }
                            else if ( sec3 > 0f )
                            {
                                s[2][3] = "(" + TimingUtil.getTimeAsGapString( sec3 - afSec3 ) + ")";
                                sfColor3 = ( sec3 - afSec3 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                            }
                            else
                            {
                                s[2][3] = null;
                            }
                        }
                    }
                    else
                    {
                        s[2][0] = null;
                        s[2][1] = null;
                        s[2][2] = "";
                        if ( myVSI != afVSI )
                            s[2][3] = "";
                    }
                    
                    java.awt.Color sfColorL = getFontColor();
                    
                    s[3][0] = Loc.timing_lap_prefix + ":";
                    s[3][1] = null;
                    s[3][2] = TimingUtil.getTimeAsLaptimeString( lap );
                    if ( dispGapToAbs )
                    {
                        if ( afLap > 0f )
                        {
                            s[3][3] = "(" + TimingUtil.getTimeAsGapString( lap - afLap  ) + ")";
                            sfColorL = ( lap - afLap < 0f )? fasterColor.getColor() : slowerColor.getColor();
                        }
                        else
                        {
                            s[3][3] = null;
                        }
                    }
                    
                    ownSector1String.getMaxColWidths( s[0], aligns, colPadding, colWidths );
                    ownSector2String.getMaxColWidths( s[1], aligns, colPadding, colWidths );
                    if ( !displayCumul )
                        ownSector3String.getMaxColWidths( s[2], aligns, colPadding, colWidths );
                    ownFastestLapString.getMaxColWidths( s[3], aligns, colPadding, colWidths );
                    
                    fontColors[3] = sfColor1;
                    ownSector1String.drawColumns( offsetX, offsetY, s[0], aligns, colPadding, colWidths, fontColors, texture );
                    fontColors[3] = sfColor2;
                    ownSector2String.drawColumns( offsetX, offsetY, s[1], aligns, colPadding, colWidths, fontColors, texture );
                    fontColors[3] = sfColor3;
                    if ( !displayCumul )
                        ownSector3String.drawColumns( offsetX, offsetY, s[2], aligns, colPadding, colWidths, fontColors, texture );
                    fontColors[3] = sfColorL;
                    ownFastestLapString.drawColumns( offsetX, offsetY, s[3], aligns, colPadding, colWidths, fontColors, texture );
                }
                else
                {
                    ownSector1String.draw( offsetX, offsetY, "-:--.---", texture );
                    ownSector2String.draw( offsetX, offsetY, "-:--.---", texture );
                    if ( !cumulativeSectors.getBooleanValue() )
                        ownSector3String.draw( offsetX, offsetY, "-:--.---", texture );
                    ownFastestLapString.draw( offsetX, offsetY, "-:--.---", texture );
                }
            }
        }
        
        {
            // current lap
            
            if ( needsCompleteRedraw )
            {
                currLapHeaderString.draw( offsetX, offsetY, "Current Lap:", texture );
            }
            
            Laptime ownFastestLaptime = myVSI.getFastestLaptime();
            float lap = myVSI.getCurrentLaptime();
            
            currLapValid.update( lap > 0f );
            if ( needsCompleteRedraw || clock.c() || ( clock.c() && ( currLapValid.hasChanged( false ) ) ) )
            {
                currLapValid.setUnchanged();
                
                final boolean isDelaying = isEditorMode || ( scoringInfo.getSessionTime() <= lastLapDisplayTime );
                final boolean isDelaying2 = !isEditorMode && isDelaying;
                
                final boolean absFastestIsOwn = isDelaying ? delayedAbsFastestIsOwn : ( myVSI == afVSI );
                final short sector = myVSI.getSector();
                final boolean displayCumul = cumulativeSectors.getBooleanValue() || forceCurrentCumulSectors.getBooleanValue();
                
                float afSec1 = isDelaying2 ? ( delayedAbsFastestLap != null ? delayedAbsFastestLap.getSector1() : -1f ) : ( afLaptime != null ) ? afLaptime.getSector1(): -1f; //afVSI.getBestSector1();
                float afSec2 = isDelaying2 ? ( delayedAbsFastestLap != null ? delayedAbsFastestLap.getSector2( displayCumul ) : -1f ) : ( afLaptime != null ) ? afLaptime.getSector2( displayCumul ): -1f; //afVSI.getBestSector2( displayCumul );
                float afSec3 = isDelaying2 ? ( delayedAbsFastestLap != null ? delayedAbsFastestLap.getSector3() : -1f ) : ( displayCumul ? ( delayedAbsFastestLap != null ? afSec2 + delayedAbsFastestLap.getSector3() : -1f ) : ( afLaptime != null ? afLaptime.getSector3() : -1f ) );
                float afLap = isDelaying2 ? ( delayedAbsFastestLap != null ? delayedAbsFastestLap.getLapTime() : -1f ) : ( afLaptime != null ? afLaptime.getLapTime() : -1f );
                
                Laptime ofLaptime = myVSI.getFastestLaptime();
                float ofSec1 = isDelaying2 ? ( delayedOwnFastestLap != null ? delayedOwnFastestLap.getSector1() : -1f ) : ( ownFastestLaptime != null ? ownFastestLaptime.getSector1() : -1f );
                float ofSec2 = isDelaying2 ? ( delayedOwnFastestLap != null ? delayedOwnFastestLap.getSector2( displayCumul ) : -1f ) : ( ownFastestLaptime != null ? ownFastestLaptime.getSector2( displayCumul ) : -1f );
                float ofSec3 = isDelaying2 ? ( delayedOwnFastestLap != null ? delayedOwnFastestLap.getSector3() : -1f ) : ( displayCumul ? ( delayedOwnFastestLap != null ? ofSec2 + delayedOwnFastestLap.getSector3() : -1f ) : ( ofLaptime != null ? ofLaptime.getSector3() : -1f ) );
                float ofLap = isDelaying2 ? ( delayedOwnFastestLap != null ? delayedOwnFastestLap.getLapTime() : -1f ) : ( ofLaptime != null ? ofLaptime.getLapTime() : -1f );
                
                float sec1 = isDelaying ? myVSI.getLastSector1() : myVSI.getCurrentSector1();
                float sec2 = isDelaying ? myVSI.getLastSector2( displayCumul ) : myVSI.getCurrentSector2( displayCumul );
                float sec3 = isDelaying ? myVSI.getLastSector3() : ( ( sec2 > 0f ) ? ( displayCumul ? lap : lap - sec2 ) : -1f );
                
                if ( isDelaying )
                {
                    lap = myVSI.getLastLapTime();
                }
                
                boolean afValid = afLap > 0f;
                boolean ofValid = ofLap > 0f;
                
                final boolean dispAbsFastest = displayAbsFastest.getBooleanValue();
                int cols = dispAbsFastest ? 5 : 4;
                String[][] s = new String[4][cols];
                int[] colWidths = new int[cols];
                final Alignment[] aligns = new Alignment[ cols ];
                aligns[0] = Alignment.RIGHT;
                aligns[1] = Alignment.LEFT;
                for ( int i = 2; i < aligns.length; i++ )
                    aligns[i] = Alignment.RIGHT;
                
                if ( !isDelaying || isEditorMode )
                {
                    gapOFSec1 = sec1 - ofSec1;
                    gapOFSec1Valid = ( ofSec1 > 0f );
                    gapOFSec2 = sec2 - ofSec2;
                    gapOFSec2Valid = ( ofSec2 > 0f );
                    gapOFSec3 = sec3 - ofSec3;
                    gapOFSec3Valid = ( ofSec3 > 0f );
                    gapOFLap = lap - ofLap;
                    gapAFSec1 = sec1 - afSec1;
                    gapAFSec1Valid = ( afSec1 > 0f );
                    gapAFSec2 = sec2 - afSec2;
                    gapAFSec2Valid = ( afSec2 > 0f );
                    gapAFSec3 = sec3 - afSec3;
                    gapAFSec3Valid = ( afSec3 > 0f );
                    gapAFLap = lap - afLap;
                    
                    delayedAbsFastestLap = afLaptime;
                    delayedOwnFastestLap = ofLaptime;
                    delayedAbsFastestIsOwn = ( afVSI == myVSI );
                }
                else
                {
                    gapOFLap = lap - ofLap;
                    gapAFLap = lap - afLap;
                }
                
                java.awt.Color sfColor1a = getFontColor();
                java.awt.Color sfColor1b = getFontColor();
                s[0][0] = Loc.timing_sector1_prefix + ":";
                s[0][1] = null;
                if ( sec1 > 0f )
                {
                    s[0][2] = TimingUtil.getTimeAsLaptimeString( sec1 );
                    if ( !isEditorMode && ( sector == 1 ) && !isDelaying )
                    {
                        if ( cols >= 4 )
                            s[0][3] = null;
                        if ( cols >= 5 )
                            s[0][4] = null;
                    }
                    else
                    {
                        if ( ofValid && gapOFSec1Valid )
                        {
                            sfColor1a = ( gapOFSec1 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                            s[0][3] = "(" + TimingUtil.getTimeAsGapString( gapOFSec1 ) + ")";
                        }
                        else
                            s[0][3] = "--.---";
                        
                        if ( dispAbsFastest )
                        {
                            if ( afValid && !absFastestIsOwn )
                                if ( gapAFSec1Valid )
                                {
                                    sfColor1b = ( gapAFSec1 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                    s[0][4] = "(" + TimingUtil.getTimeAsGapString( gapAFSec1 ) + ")";
                                }
                                else
                                    s[0][4] = "--.---";
                            else
                                s[0][4] = null;
                        }
                    }
                }
                else
                {
                    s[0][2] = "--.---";
                    if ( cols >= 4 )
                        s[0][3] = null;
                    if ( cols >= 5 )
                        s[0][4] = null;
                }
                
                java.awt.Color sfColor2a = getFontColor();
                java.awt.Color sfColor2b = getFontColor();
                s[1][0] = Loc.timing_sector2_prefix + ":";
                s[1][1] = null;
                if ( sec2 > 0f )
                {
                    s[1][2] = TimingUtil.getTimeAsLaptimeString( sec2 );
                    if ( !isEditorMode && ( sector == 2 ) && !isDelaying )
                    {
                        if ( cols >= 4 )
                            s[1][3] = null;
                        if ( cols >= 5 )
                            s[1][4] = null;
                    }
                    else
                    {
                        if ( ofValid && gapOFSec2Valid )
                        {
                            sfColor2a = ( gapOFSec2 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                            s[1][3] = "(" + TimingUtil.getTimeAsGapString( gapOFSec2 ) + ")";
                        }
                        else
                            s[1][3] = "--.---";
                        
                        if ( dispAbsFastest )
                        {
                            if ( afValid && !absFastestIsOwn )
                                if ( gapAFSec2Valid )
                                {
                                    sfColor2b = ( gapAFSec2 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                    s[1][4] = "(" + TimingUtil.getTimeAsGapString( gapAFSec2 ) + ")";
                                }
                                else
                                    s[1][4] = "--.---";
                            else
                                s[1][4] = null;
                        }
                    }
                }
                else
                {
                    s[1][2] = "--.---";
                    if ( cols >= 4 )
                        s[1][3] = null;
                    if ( cols >= 5 )
                        s[1][4] = null;
                }
                
                java.awt.Color sfColor3a = getFontColor();
                java.awt.Color sfColor3b = getFontColor();
                s[2][0] = Loc.timing_sector3_prefix + ":";
                s[2][1] = null;
                if ( !displayCumul )
                {
                    if ( sec3 > 0f )
                    {
                        s[2][2] = TimingUtil.getTimeAsLaptimeString( sec3 );
                        if ( !isEditorMode && ( sector == 3 ) && !isDelaying )
                        {
                            if ( cols >= 4 )
                                s[2][3] = null;
                            if ( cols >= 5 )
                                s[2][4] = null;
                        }
                        else
                        {
                            if ( ofValid && gapOFSec3Valid )
                            {
                                sfColor3a = ( gapOFSec3 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                s[2][3] = "(" + TimingUtil.getTimeAsGapString( gapOFSec3 ) + ")";
                            }
                            else
                                s[2][3] = "--.---";
                            
                            if ( dispAbsFastest )
                            {
                                if ( afValid && !absFastestIsOwn )
                                    if ( gapAFSec3Valid )
                                    {
                                        sfColor3b = ( gapAFSec3 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                        s[2][4] = "(" + TimingUtil.getTimeAsGapString( gapAFSec3 ) + ")";
                                    }
                                    else
                                        s[2][4] = "--.---";
                                else
                                    s[2][4] = null;
                            }
                        }
                    }
                    else
                    {
                        s[2][2] = "--.---";
                        if ( cols >= 4 )
                            s[2][3] = null;
                        if ( cols >= 5 )
                            s[2][4] = null;
                    }
                }
                else
                {
                    s[2][2] = null;
                    if ( cols >= 4 )
                        s[2][3] = null;
                    if ( cols >= 5 )
                        s[2][4] = null;
                }
                
                java.awt.Color sfColorLa = getFontColor();
                java.awt.Color sfColorLb = getFontColor();
                s[3][0] = Loc.timing_lap_prefix + ":";
                s[3][1] = null;
                if ( isEditorMode || ( ( lap > 0f ) && ( myVSI.getLapsCompleted() >= myVSI.getStintStartLap() ) ) )
                {
                    s[3][2] = TimingUtil.getTimeAsLaptimeString( lap );
                    if ( isDelaying )
                    {
                        if ( ofValid )
                        {
                            sfColorLa = ( gapOFLap < 0f )? fasterColor.getColor() : slowerColor.getColor();
                            s[3][3] = "(" + TimingUtil.getTimeAsGapString( gapOFLap ) + ")";
                        }
                        else
                            s[3][3] = "--.---";
                        
                        if ( dispAbsFastest )
                        {
                            if ( afValid && !absFastestIsOwn )
                            {
                                sfColorLb = ( gapAFLap < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                s[3][4] = "(" + TimingUtil.getTimeAsGapString( gapAFLap ) + ")";
                            }
                            else
                                s[3][4] = null;
                        }
                    }
                    else
                    {
                        if ( cols >= 4 )
                            s[3][3] = null;
                        if ( cols >= 5 )
                            s[3][4] = null;
                    }
                }
                else
                {
                    s[3][2] = "-:--.---";
                    if ( cols >= 4 )
                        s[3][3] = null;
                    if ( cols >= 5 )
                        s[3][4] = null;
                }
                
                currSector1String.getMaxColWidths( s[0], aligns, colPadding, colWidths );
                currSector2String.getMaxColWidths( s[1], aligns, colPadding, colWidths );
                if ( !displayCumul )
                    currSector3String.getMaxColWidths( s[2], aligns, colPadding, colWidths );
                
                String s31 = s[3][2];
                s[3][2] = TimingUtil.getTimeAsLaptimeString( 90.0f );
                currLapString.getMaxColWidths( s[3], aligns, colPadding, colWidths );
                s[3][2] = s31;
                
                if ( oldClStrings[0].length != s[0].length )
                    oldClStrings[0] = new String[ s[0].length ];
                if ( needsCompleteRedraw || !Arrays.equals( s[0], oldClStrings[0] ) )
                {
                    System.arraycopy( s[0], 0, oldClStrings[0], 0, s[0].length );
                    fontColors[3] = sfColor1a;
                    fontColors[4] = sfColor1b;
                    currSector1String.drawColumns( offsetX, offsetY, s[0], aligns, colPadding, colWidths, fontColors, texture );
                }
                
                if ( oldClStrings[1].length != s[1].length )
                    oldClStrings[1] = new String[ s[1].length ];
                if ( needsCompleteRedraw || !Arrays.equals( s[1], oldClStrings[1] ) )
                {
                    System.arraycopy( s[1], 0, oldClStrings[1], 0, s[1].length );
                    fontColors[3] = sfColor2a;
                    fontColors[4] = sfColor2b;
                    currSector2String.drawColumns( offsetX, offsetY, s[1], aligns, colPadding, colWidths, fontColors, texture );
                }                
                if ( !displayCumul )
                {
                    if ( oldClStrings[2].length != s[2].length )
                        oldClStrings[2] = new String[ s[2].length ];
                    if ( needsCompleteRedraw || !Arrays.equals( s[2], oldClStrings[2] ) )
                    {
                        System.arraycopy( s[2], 0, oldClStrings[2], 0, s[2].length );
                        fontColors[3] = sfColor3a;
                        fontColors[4] = sfColor3b;
                        currSector3String.drawColumns( offsetX, offsetY, s[2], aligns, colPadding, colWidths, fontColors, texture );
                    }
                }
                
                
                if ( oldClStrings[3].length != s[3].length )
                    oldClStrings[3] = new String[ s[3].length ];
                if ( needsCompleteRedraw || !Arrays.equals( s[3], oldClStrings[3] ) )
                {
                    System.arraycopy( s[3], 0, oldClStrings[3], 0, s[3].length );
                    fontColors[3] = sfColorLa;
                    fontColors[4] = sfColorLb;
                    currLapString.drawColumns( offsetX, offsetY, s[3], aligns, colPadding, colWidths, fontColors, texture );
                }
            }
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( displayAbsFastest, "Display the absolute fastest lap part of the Widget?" );
        writer.writeProperty( cumulativeSectors, "Display the second sector as a sum?" );
        writer.writeProperty( forceCurrentCumulSectors, "Display the second sector as a sum even if the others not?" );
        writer.writeProperty( lastLapDisplayDelay, "The time for which the last driven lap will keepbeing displayed (in milliseconds)." );
        writer.writeProperty( slowerColor, "The font color to use for positive gaps." );
        writer.writeProperty( fasterColor, "The font color to use for negative gaps." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( displayAbsFastest ) );
        else if ( loader.loadProperty( cumulativeSectors ) );
        else if ( loader.loadProperty( forceCurrentCumulSectors ) );
        else if ( loader.loadProperty( lastLapDisplayDelay ) );
        else if ( loader.loadProperty( slowerColor ) );
        else if ( loader.loadProperty( fasterColor ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( displayAbsFastest );
        propsCont.addProperty( cumulativeSectors );
        propsCont.addProperty( forceCurrentCumulSectors );
        propsCont.addProperty( lastLapDisplayDelay );
        propsCont.addProperty( slowerColor );
        propsCont.addProperty( fasterColor );
    }
    
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        colPadding = 1;
    }
    
    public TimingWidget()
    {
        super( 24.0625f, 30.083334f );
    }
}
