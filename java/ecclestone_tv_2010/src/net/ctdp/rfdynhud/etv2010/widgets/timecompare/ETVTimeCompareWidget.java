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
package net.ctdp.rfdynhud.etv2010.widgets.timecompare;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.etv2010.widgets._base.ETVTimingWidgetBase;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVImages;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVImages.BGType;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link ETVTimeCompareWidget} displays lap time gaps in race sessions.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ETVTimeCompareWidget extends ETVTimingWidgetBase
{
    private static final int NUM_DISPLAYED_LAPS = 3;
    
    private final IntProperty displayEveryXLaps = new IntProperty( this, "displayEveryXLaps", 3, 1, 20 );
    private final FloatProperty visibleTime = new FloatProperty( this, "visibleTime", 8.0f, 1.0f, 60.0f );
    
    private final BooleanProperty preferNextInFront = new BooleanProperty( this, "preferNextInFront", false );
    
    private DrawnString positionString1 = null;
    private DrawnString positionString2 = null;
    private DrawnString drivernameString1 = null;
    private DrawnString drivernameString2 = null;
    private DrawnString lapCaptionString1 = null;
    private DrawnString lapCaptionString2 = null;
    private DrawnString lapCaptionString3 = null;
    private DrawnString laptimeString1 = null;
    private DrawnString laptimeString2 = null;
    private DrawnString laptimeString3 = null;
    private DrawnString gapString1 = null;
    private DrawnString gapString2 = null;
    private DrawnString gapString3 = null;
    
    private final IntValue laps = new IntValue();
    private VehicleScoringInfo relVSI = null;
    
    private boolean waitingForNextBehind = false;
    private short decisionPlace = 0;
    private float hideTime = -1f;
    
    private final boolean getUseClassScoring()
    {
        return ( getConfiguration().getUseClassScoring() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPropertyChanged( Property property, Object oldValue, Object newValue )
    {
        super.onPropertyChanged( property, oldValue, newValue );
        
        if ( property == getFontProperty() )
        {
            forceReinitialization();
            forceCompleteRedraw( false );
        }
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
        
        laps.reset( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        waitingForNextBehind = false;
        hideTime = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.updateVisibility( gameData, isEditorMode );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        
        if ( isEditorMode )
        {
            relVSI = vsi.getNextInFront( getUseClassScoring() );
            waitingForNextBehind = false;
            return ( result );
        }
        
        relVSI = null;
        
        if ( !scoringInfo.getSessionType().isRace() || !vsi.getFinishStatus().isNone() )
        {
            return ( false );
        }
        
        laps.update( vsi.getLapsCompleted() );
        
        /*
        if ( laps.getValue() >= NUM_DISPLAYED_LAPS )
        {
            Logger.log( laps.hasChanged( false ) );
        }
        */
        
        if ( laps.hasChanged() )
        {
            if ( laps.getValue() < NUM_DISPLAYED_LAPS )
            {
                result = false;
            }
            else if ( ( laps.getValue() % displayEveryXLaps.getIntValue() ) == 0 )
            {
                if ( waitingForNextBehind && ( decisionPlace != vsi.getPlace( getUseClassScoring() ) ) )
                {
                    waitingForNextBehind = false;
                }
                
                if ( waitingForNextBehind )
                {
                    VehicleScoringInfo vsi_nb = vsi.getNextBehind( getUseClassScoring() );
                    if ( !vsi_nb.getFinishStatus().isNone() )
                    {
                        waitingForNextBehind = false;
                        result = false;
                    }
                    else if ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront( getUseClassScoring() ) < laps.getValue() )
                    {
                        laps.reset( true );
                        result = false;
                    }
                    else
                    {
                        waitingForNextBehind = false;
                        relVSI = vsi_nb;
                        result = true;
                        hideTime = scoringInfo.getSessionTime() + visibleTime.getFloatValue();
                        forceCompleteRedraw( false );
                    }
                }
                else
                {
                    boolean b = false;
                    
                    if ( scoringInfo.getNumVehicles() == 1 )
                    {
                        b = false;
                    }
                    else if ( vsi.getPlace( getUseClassScoring() ) == 1 )
                    {
                        VehicleScoringInfo vsi_nb = vsi.getNextBehind( getUseClassScoring() );
                        if ( vsi_nb.getFinishStatus().isNone()  )
                        {
                            if ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront( getUseClassScoring() ) < laps.getValue() )
                            {
                                waitingForNextBehind = true;
                                decisionPlace = vsi.getPlace( getUseClassScoring() );
                                laps.reset( true );
                                b = false;
                            }
                            else
                            {
                                waitingForNextBehind = false;
                                relVSI = vsi_nb;
                                b = true;
                            }
                        }
                        else
                        {
                            waitingForNextBehind = false;
                            b = false;
                        }
                    }
                    else if ( vsi.getNextBehind( getUseClassScoring() ) == null )
                    {
                        VehicleScoringInfo vsi_nif = vsi.getNextInFront( getUseClassScoring() );
                        b = vsi_nif.getFinishStatus().isNone();
                        
                        waitingForNextBehind = false;
                        
                        if ( b )
                        {
                            relVSI = vsi_nif;
                        }
                    }
                    else
                    {
                        // There are at least 3 vehicles in the race.
                        
                        VehicleScoringInfo vsi_nif = vsi.getNextInFront( getUseClassScoring() );
                        if ( vsi_nif.getFinishStatus().isNone() && ( vsi_nif.getLapsCompleted() + vsi.getLapsBehindNextInFront( getUseClassScoring() ) >= NUM_DISPLAYED_LAPS ) )
                        {
                            VehicleScoringInfo vsi_nb = vsi.getNextBehind( getUseClassScoring() );
                            
                            if ( preferNextInFront.getBooleanValue() || !vsi_nb.getFinishStatus().isNone() || ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront( getUseClassScoring() ) < NUM_DISPLAYED_LAPS - 1 ) )
                            {
                                waitingForNextBehind = false;
                                relVSI = vsi_nif;
                                b = true;
                            }
                            else
                            {
                                float gapToNextInFront = Math.abs( vsi.getTimeBehindNextInFront( getUseClassScoring() ) );
                                float gapToNextBehind = Math.abs( vsi_nb.getTimeBehindNextInFront( getUseClassScoring() ) );
                                
                                if ( gapToNextInFront < gapToNextBehind )
                                {
                                    waitingForNextBehind = false;
                                    relVSI = vsi_nif;
                                    b = true;
                                }
                                else if ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront( getUseClassScoring() ) < laps.getValue() )
                                {
                                    waitingForNextBehind = true;
                                    decisionPlace = vsi.getPlace( getUseClassScoring() );
                                    laps.reset( true );
                                    b = false;
                                }
                                else
                                {
                                    waitingForNextBehind = false;
                                    relVSI = vsi_nb;
                                    b = true;
                                }
                            }
                        }
                        else
                        {
                            VehicleScoringInfo vsi_nb = vsi.getNextBehind( getUseClassScoring() );
                            if ( vsi_nb.getFinishStatus().isNone() && ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront( getUseClassScoring() ) >= NUM_DISPLAYED_LAPS - 1 ) )
                            {
                                if ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront( getUseClassScoring() ) < laps.getValue() )
                                {
                                    waitingForNextBehind = false;
                                    decisionPlace = vsi.getPlace( getUseClassScoring() );
                                    laps.reset( true );
                                    b = false;
                                }
                                else
                                {
                                    waitingForNextBehind = false;
                                    relVSI = vsi_nb;
                                    b = true;
                                }
                            }
                            else
                            {
                                waitingForNextBehind = false;
                                b = false;
                            }
                        }
                    }
                    
                    result = b;
                    
                    if ( b )
                    {
                        hideTime = scoringInfo.getSessionTime() + visibleTime.getFloatValue();
                        forceCompleteRedraw( false );
                    }
                }
            }
            else
            {
                result = false;
                hideTime = -1f;
            }
        }
        else if ( scoringInfo.getSessionTime() < hideTime )
        {
            result = true;
        }
        else
        {
            result = false;
            hideTime = -1f;
        }
        
        return ( result );
    }
    
    private final Coords coords = new Coords();
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        Rectangle2D posBounds = TextureImage2D.getStringBounds( "00", getFontProperty() );
        
        final int gap = itemGap.getIntValue();
        coords.update( getImages(), width, height, gap, posBounds );
        
        int vMiddle = ETVUtils.getLabeledDataVMiddle( coords.rowHeight, posBounds );
        
        positionString1 = dsf.newDrawnString( "positionString1", coords.rowOffset1 + coords.posCenterA, 1 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        positionString2 = dsf.newDrawnString( "positionString2", coords.posCenterA, 2 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        drivernameString1 = dsf.newDrawnString( "drivernameString1", coords.rowOffset1 + coords.dataLeftA, 1 * ( coords.rowHeight + gap ) + vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        drivernameString2 = dsf.newDrawnString( "drivernameString2", coords.dataLeftA, 2 * ( coords.rowHeight + gap ) + vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        lapCaptionString1 = dsf.newDrawnString( "lapCaptionString1", coords.rowOffset2 + coords.offsetB + coords.dataCenterBC, 0 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        lapCaptionString2 = dsf.newDrawnString( "lapCaptionString2", coords.rowOffset2 + coords.offsetC + coords.dataCenterBC, 0 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        lapCaptionString3 = dsf.newDrawnString( "lapCaptionString3", coords.rowOffset2 + coords.offsetD + coords.dataCenterD, 0 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        laptimeString1 = dsf.newDrawnString( "laptimeString1", coords.rowOffset1 + coords.offsetB + coords.dataCenterBC, 1 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        laptimeString2 = dsf.newDrawnString( "laptimeString2", coords.rowOffset1 + coords.offsetC + coords.dataCenterBC, 1 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        laptimeString3 = dsf.newDrawnString( "laptimeString3", coords.rowOffset1 + coords.offsetD + coords.dataCenterD, 1 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        gapString1 = dsf.newDrawnString( "gapString1", coords.offsetB + coords.dataCenterBC, 2 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        gapString2 = dsf.newDrawnString( "gapString2", coords.offsetC + coords.dataCenterBC, 2 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        gapString3 = dsf.newDrawnString( "gapString3", coords.offsetD + coords.dataCenterD, 2 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        forceCompleteRedraw( true );
    }
    
    private static final float getLaptime( VehicleScoringInfo vsi, int lap )
    {
        Laptime lt = vsi.getLaptime( lap );
        
        if ( lt == null )
            return ( -1f );
        
        return ( lt.getLapTime() );        
    }
    
    private void drawStructure( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        
        final boolean useImages = this.useImages.getBooleanValue();
        final ETVImages images = getImages();
        final int gap = itemGap.getIntValue();
        
        Color captionBgColor = captionBackgroundColor.getColor();
        Color dataBgColor = dataBackgroundColor.getColor();
        if ( vsi.getPlace( getUseClassScoring() ) == 1 )
            captionBgColor = captionBackgroundColor1st.getColor();
        
        if ( useImages )
            ETVUtils.drawLabeledDataBackgroundI( offsetX + coords.rowOffset1, offsetY + 1 * ( coords.rowHeight + gap ), coords.widthA, coords.rowHeight, "00", getFontProperty(), images, BGType.NEUTRAL, texture, false );
        else
            ETVUtils.drawLabeledDataBackground( offsetX + coords.rowOffset1, offsetY + 1 * ( coords.rowHeight + gap ), coords.widthA, coords.rowHeight, "00", getFontProperty(), captionBgColor, dataBgColor, texture, false );
        positionString1.draw( offsetX, offsetY, String.valueOf( vsi.getPlace( getUseClassScoring() ) ), texture, false );
        drivernameString1.draw( offsetX, offsetY, vsi.getDriverNameShort(), texture, false );
        
        if ( useImages )
            ETVUtils.drawLabeledDataBackgroundI( offsetX, offsetY + 2 * ( coords.rowHeight + gap ), coords.widthA, coords.rowHeight, "00", getFontProperty(), images, BGType.NEUTRAL, texture, false );
        else
            ETVUtils.drawLabeledDataBackground( offsetX, offsetY + 2 * ( coords.rowHeight + gap ), coords.widthA, coords.rowHeight, "00", getFontProperty(), captionBgColor, dataBgColor, texture, false );
        positionString2.draw( offsetX, offsetY, String.valueOf( relVSI.getPlace( getUseClassScoring() ) ), texture, false );
        drivernameString2.draw( offsetX, offsetY, relVSI.getDriverNameShort(), texture, false );
        
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.rowOffset2 + coords.offsetB, offsetY, coords.widthBC, coords.rowHeight, images, BGType.CAPTION, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX +  coords.rowOffset2 + coords.offsetB, offsetY, coords.widthBC, coords.rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString1.draw( offsetX, offsetY, Loc.caption_lap + " " + ( vsi.getLapsCompleted() - 2 ), texture, false );
        
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.rowOffset2 + coords.offsetC, offsetY, coords.widthBC, coords.rowHeight, images, BGType.CAPTION, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.rowOffset2 + coords.offsetC, offsetY, coords.widthBC, coords.rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString2.draw( offsetX, offsetY, Loc.caption_lap + " " + ( vsi.getLapsCompleted() - 1 ), texture, false );
        
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.rowOffset2 + coords.offsetD, offsetY, coords.widthD, coords.rowHeight, images, BGType.CAPTION, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.rowOffset2 + coords.offsetD, offsetY, coords.widthD, coords.rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString3.draw( offsetX, offsetY, Loc.caption_lap + " " + ( vsi.getLapsCompleted() - 0 ), texture, false );
        
        float laptime1 = getLaptime( vsi, vsi.getLapsCompleted() - 2 );
        
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.rowOffset1 + coords.offsetB, offsetY + coords.rowHeight + gap, coords.widthBC, coords.rowHeight, images, BGType.NEUTRAL, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.rowOffset1 + coords.offsetB, offsetY + coords.rowHeight + gap, coords.widthBC, coords.rowHeight, dataBgColor, texture, false );
        if ( laptime1 > 0f )
        {
            laptimeString1.draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString( laptime1 ), texture, false );
        }
        
        float laptime2 = getLaptime( vsi, vsi.getLapsCompleted() - 1 );
        
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.rowOffset1 + coords.offsetC, offsetY + coords.rowHeight + gap, coords.widthBC, coords.rowHeight, images, BGType.NEUTRAL, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.rowOffset1 + coords.offsetC, offsetY + coords.rowHeight + gap, coords.widthBC, coords.rowHeight, dataBgColor, texture, false );
        if ( laptime2 > 0f )
        {
            laptimeString2.draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString( laptime2 ), texture, false );
        }
        
        float laptime3 = getLaptime( vsi, vsi.getLapsCompleted() - 0 );
        
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.rowOffset1 + coords.offsetD, offsetY + coords.rowHeight + gap, coords.widthD, coords.rowHeight, images, BGType.NEUTRAL, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.rowOffset1 + coords.offsetD, offsetY + coords.rowHeight + gap, coords.widthD, coords.rowHeight, dataBgColor, texture, false );
        if ( laptime3 > 0f )
        {
            laptimeString3.draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString( laptime3 ), texture, false );
        }
        
        float gap1, gap2, gap3;
        String gapStr1, gapStr2, gapStr3;
        if ( isEditorMode )
        {
            gap1 = -1.234f;
            gap2 = +0.123f;
            gap3 = -2.345f;
            
            gapStr1 = TimingUtil.getTimeAsGapString( gap1 );
            gapStr2 = TimingUtil.getTimeAsGapString( gap2 );
            gapStr3 = TimingUtil.getTimeAsGapString( gap3 );
        }
        else
        {
            int relLapsOffset = ( relVSI.getPlace( getUseClassScoring() ) < vsi.getPlace( getUseClassScoring() ) ) ? vsi.getLapsBehindNextInFront( getUseClassScoring() ) : relVSI.getLapsBehindNextInFront( getUseClassScoring() );
            
            float rlt1 = getLaptime( relVSI, relVSI.getLapsCompleted() - 2 + relLapsOffset );
            float rlt2 = getLaptime( relVSI, relVSI.getLapsCompleted() - 1 + relLapsOffset );
            float rlt3 = getLaptime( relVSI, relVSI.getLapsCompleted() - 0 + relLapsOffset );
            
            if ( rlt1 > 0f )
            {
                gap1 = rlt1 - vsi.getLaptime( vsi.getLapsCompleted() - 2 ).getLapTime();
                gapStr1 = TimingUtil.getTimeAsGapString( gap1 );
            }
            else
            {
                gap1 = 0f;
                gapStr1 = null;
            }
            
            if ( rlt2 > 0f )
            {
                gap2 = rlt2 - vsi.getLaptime( vsi.getLapsCompleted() - 1 ).getLapTime();
                gapStr2 = TimingUtil.getTimeAsGapString( gap2 );
            }
            else
            {
                gap2 = 0f;
                gapStr2 = null;
            }
            
            if ( rlt3 > 0f )
            {
                gap3 = rlt3 - vsi.getLaptime( vsi.getLapsCompleted() - 0 ).getLapTime();
                gapStr3 = TimingUtil.getTimeAsGapString( gap3 );
            }
            else
            {
                gap3 = 0f;
                gapStr3 = null;
            }
        }
        
        BGType bgType = BGType.FASTER;
        dataBgColor = dataBackgroundColorFaster.getColor();
        Color dataColor = dataColorFaster.getColor();
        if ( gapStr1 == null )
        {
            bgType = BGType.NEUTRAL;
            dataBgColor = dataBackgroundColor.getColor();
            dataColor = getFontColor();
        }
        else if ( gap1 < 0f )
        {
            bgType = BGType.SLOWER;
            dataBgColor = dataBackgroundColorSlower.getColor();
            dataColor = dataColorSlower.getColor();
        }
        
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.offsetB, offsetY + 2 * ( coords.rowHeight + gap ), coords.widthBC, coords.rowHeight, images, bgType, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.offsetB, offsetY + 2 * ( coords.rowHeight + gap ), coords.widthBC, coords.rowHeight, dataBgColor, texture, false );
        if ( gapStr1 != null )
        {
            gapString1.resetClearRect();
            gapString1.draw( offsetX, offsetY, gapStr1, dataColor, texture, false );
        }
        
        bgType = BGType.FASTER;
        dataBgColor = dataBackgroundColorFaster.getColor();
        dataColor = dataColorFaster.getColor();
        if ( gapStr2 == null )
        {
            bgType = BGType.NEUTRAL;
            dataBgColor = dataBackgroundColor.getColor();
            dataColor = getFontColor();
        }
        else if ( gap2 < 0f )
        {
            bgType = BGType.SLOWER;
            dataBgColor = dataBackgroundColorSlower.getColor();
            dataColor = dataColorSlower.getColor();
        }
        
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.offsetC, offsetY + 2 * ( coords.rowHeight + gap ), coords.widthBC, coords.rowHeight, images, bgType, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.offsetC, offsetY + 2 * ( coords.rowHeight + gap ), coords.widthBC, coords.rowHeight, dataBgColor, texture, false );
        if ( gapStr2 != null )
        {
            gapString2.resetClearRect();
            gapString2.draw( offsetX, offsetY, gapStr2, dataColor, texture, false );
        }
        
        bgType = BGType.FASTER;
        dataBgColor = dataBackgroundColorFaster.getColor();
        dataColor = dataColorFaster.getColor();
        if ( gapStr3 == null )
        {
            bgType = BGType.NEUTRAL;
            dataBgColor = dataBackgroundColor.getColor();
            dataColor = getFontColor();
        }
        else if ( gap3 < 0f )
        {
            bgType = BGType.SLOWER;
            dataBgColor = dataBackgroundColorSlower.getColor();
            dataColor = dataColorSlower.getColor();
        }
        
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.offsetD, offsetY + 2 * ( coords.rowHeight + gap ), coords.widthD, coords.rowHeight, images, bgType, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.offsetD, offsetY + 2 * ( coords.rowHeight + gap ), coords.widthD, coords.rowHeight, dataBgColor, texture, false );
        if ( gapStr3 != null )
        {
            gapString3.resetClearRect();
            gapString3.draw( offsetX, offsetY, gapStr3, dataColor, texture, false );
        }
    }
    
    @Override
    public void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw )
        {
            drawStructure( gameData, isEditorMode, texture, offsetX, offsetY );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( displayEveryXLaps, "Show the Widget every x laps." );
        writer.writeProperty( visibleTime, "Time in seconds to keep the Widget visible." );
        writer.writeProperty( preferNextInFront, "Whether to prefer next in front, even if next behind is closer." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( displayEveryXLaps ) );
        else if ( loader.loadProperty( visibleTime ) );
        else if ( loader.loadProperty( preferNextInFront ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( displayEveryXLaps );
        propsCont.addProperty( visibleTime );
        propsCont.addProperty( preferNextInFront );
    }
    
    public ETVTimeCompareWidget( String name )
    {
        super( name, 40.7f, 8.496094f );
    }
}
