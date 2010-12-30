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
package net.ctdp.rfdynhud.widgets.etv2010.timing;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.LapState;
import net.ctdp.rfdynhud.values.ValidityTest;
import net.ctdp.rfdynhud.widgets.etv2010._base.ETVTimingWidgetBase;
import net.ctdp.rfdynhud.widgets.etv2010._util.ETVImages.BGType;
import net.ctdp.rfdynhud.widgets.etv2010._util.ETVUtils;

/**
 * The {@link ETVTimingWidget} displays the current lap time.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ETVTimingWidget extends ETVTimingWidgetBase
{
    private static enum DisplayType
    {
        ALWAYS,
        IF_LAP_VALID,
        AT_SECTORS,
        ;
    }
    
    private final IntProperty positionFontSize = new IntProperty( "positionFontSize", 200 );
    
    private final BooleanProperty alwaysShowFull1000 = new BooleanProperty( "alwaysShowFull1000", "showFull1000", false );
    
    protected final IntProperty positionItemGap = new IntProperty( "positionItemGap", 5, 0, 100 );
    
    private final EnumProperty<DisplayType> displayType = new EnumProperty<DisplayType>( "displayType", DisplayType.AT_SECTORS );
    
    private Font positionFont = null;
    
    private DrawnString drivernameString = null;
    private DrawnString laptimeString = null;
    private DrawnString bigPositionString = null;
    private DrawnString relPositionString = null;
    private DrawnString relTimeString = null;
    private int laptimeOffsetX = 0;
    
    private final IntValue vsiID = new IntValue();
    private final IntValue ownPlace = new IntValue();
    private final FloatValue ownLaptime = new FloatValue();
    private final IntValue relPlace = new IntValue();
    private final FloatValue relLaptime = new FloatValue( -100000f, FloatValue.DEFAULT_COMPARE_PRECISION, ValidityTest.GREATER_THAN, -99999f );
    private final EnumValue<LapState> lapState = new EnumValue<LapState>();
    
    private Laptime referenceTimeAbs = null;
    private Laptime referenceTimePers = null;
    private int referencePlace = 0;
    
    private final BoolValue hasRefTime = new BoolValue();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPropertyChanged( Property property, Object oldValue, Object newValue )
    {
        super.onPropertyChanged( property, oldValue, newValue );
        
        if ( property == positionFontSize )
        {
            positionFont = null;
        }
        else if ( property == getFontProperty() )
        {
            positionFont = null;
        }
    }
    
    private final Font getPositionFont()
    {
        if ( positionFont == null )
        {
            Font base = getFont();
            
            positionFont = base.deriveFont( base.getSize() * ( positionFontSize.getIntValue() / 100f ) );
        }
        
        return ( positionFont );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        vsiID.reset();
        ownPlace.reset();
        ownLaptime.reset();
        relPlace.reset();
        relLaptime.reset();
        lapState.reset();
        
        referenceTimeAbs = null;
        referenceTimePers = null;
        referencePlace = 0;
        
        hasRefTime.reset();
        
        forceCompleteRedraw( true );
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
    protected Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
    {
        /*Boolean result = */super.updateVisibility( gameData, isEditorMode );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        VehicleScoringInfo refVSI = ( scoringInfo.getCompareVSI() == null ) ? scoringInfo.getFastestLapVSI() : scoringInfo.getCompareVSI();
        Laptime refTime = refVSI.getFastestLaptime();
        if ( ( refTime != null ) && ( refTime.getLapTime() < 0f ) )
            refTime = null;
        
        LapState ls = isEditorMode ? LapState.AFTER_SECTOR1_START : LapState.getLapState( gameData.getTrackInfo().getTrack(), vsi );
        if ( !isEditorMode && ( ls == LapState.AFTER_SECTOR1_START ) && ( vsi.getStintLength() < 2.0f ) )
            ls = LapState.SOMEWHERE;
        
        if ( ( ( ls == LapState.BEFORE_SECTOR1_END ) || ( ls == LapState.AFTER_SECTOR2_START ) ) && ( ( refTime == null ) || ( refTime.getSector1() <= 0f ) ) )
            ls = LapState.SOMEWHERE;
        
        if ( ( ( ls == LapState.BEFORE_SECTOR2_END ) || ( ls == LapState.AFTER_SECTOR3_START ) ) && ( ( refTime == null ) || ( refTime.getSector2( true ) <= 0f ) ) )
            ls = LapState.SOMEWHERE;
        
        lapState.update( ls );
        
        if ( isEditorMode || ( ls != LapState.AFTER_SECTOR1_START ) )
        {
            referenceTimeAbs = refTime;
            referenceTimePers = vsi.getFastestLaptime();
            if ( ( referenceTimePers != null ) && ( referenceTimePers.getLapTime() < 0f ) )
                referenceTimePers = null;
            referencePlace = isEditorMode ? 1 : refVSI.getPlace( getConfiguration().getUseClassScoring() );
        }
        
        if ( isEditorMode )
        {
            return ( true );
        }
        
        if ( scoringInfo.getSessionType().isRace() )
        {
            return ( false );
        }
        
        if ( displayType.getEnumValue() == DisplayType.ALWAYS )
        {
            return ( true );
        }
        
        if ( displayType.getEnumValue() == DisplayType.IF_LAP_VALID )
        {
            if ( vsi.isInPits() )
                return ( false );
            
            if ( ls == LapState.OUTLAP )
                return ( false );
            
            if ( ( vsi.getLaptime( vsi.getCurrentLap() ) == null ) || ( vsi.getLaptime( vsi.getCurrentLap() ).isInlap() == Boolean.TRUE ) )
                return ( false );
            
            return ( true );
        }
        
        if ( refTime == null )
        {
            return ( ls == LapState.BEFORE_SECTOR3_END );
        }
        
        if ( ( ls == LapState.SOMEWHERE ) || ( ls == LapState.OUTLAP ) || vsi.isInPits() || ( vsi.getLaptime( vsi.getCurrentLap() ) == null ) || ( vsi.getLaptime( vsi.getCurrentLap() ).isInlap() == Boolean.TRUE ) )
        {
            return ( false );
        }
        
        return ( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int width, int height )
    {
        if ( lapState.hasChanged() )
            return ( true );
        
        //if ( isEditorMode )
        {
            hasRefTime.update( referenceTimeAbs != null );
            
            if ( hasRefTime.hasChanged() )
                return ( true );
        }
        
        return ( false );
    }
    
    private final Coords coords = new Coords();
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        Rectangle2D bigPosBounds = TextureImage2D.getStringBounds( "00", getPositionFont(), isFontAntiAliased() );
        Rectangle2D posBounds = TextureImage2D.getStringBounds( "00", getFontProperty() );
        int gap = itemGap.getIntValue();
        coords.update( getImages(), width, height, gap, positionItemGap.getIntValue(), bigPosBounds, posBounds );
        
        int vMiddle = ETVUtils.getLabeledDataVMiddle( coords.rowHeight + gap + coords.rowHeight, bigPosBounds );
        
        bigPositionString = dsf.newDrawnString( "bigPositionString", width - coords.bigPosWidth + coords.bigPosCenter, 0 + vMiddle, Alignment.CENTER, false, getPositionFont(), isFontAntiAliased(), captionColor.getColor() );
        
        vMiddle = ETVUtils.getLabeledDataVMiddle( coords.rowHeight, posBounds );
        
        drivernameString = dsf.newDrawnString( "drivernameString", coords.rowOffset2 + coords.dataRightB, 0 * ( coords.rowHeight + gap ) + vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
        laptimeString = dsf.newDrawnString( "laptimeString", coords.rowOffset1 + coords.dataRightB, 1 * ( coords.rowHeight + gap ) + vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
        relPositionString = dsf.newDrawnString( "relPositionString", coords.posCenterA, 2 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        relTimeString = dsf.newDrawnString( "relTimeString", coords.dataRightA, 2 * ( coords.rowHeight + gap ) + vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        laptimeOffsetX = -TextureImage2D.getStringWidth( "00", getFontProperty() );
        
        forceCompleteRedraw( true );
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        
        final boolean useImages = this.useImages.getBooleanValue();
        final int gap = itemGap.getIntValue();
        
        // driver name field
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.rowOffset2, offsetY + 0 * ( coords.rowHeight + gap ), coords.mainFieldWidthB, coords.rowHeight, getImages(), BGType.NEUTRAL, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.rowOffset2, offsetY + 0 * ( coords.rowHeight + gap ), coords.mainFieldWidthB, coords.rowHeight, dataBackgroundColor.getColor(), texture, false );
        
        // lap time field
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.rowOffset1, offsetY + 1 * ( coords.rowHeight + gap ), coords.mainFieldWidthB, coords.rowHeight, getImages(), BGType.NEUTRAL, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.rowOffset1, offsetY + 1 * ( coords.rowHeight + gap ), coords.mainFieldWidthB, coords.rowHeight, dataBackgroundColor.getColor(), texture, false );
        
        // reference time field
        if ( isEditorMode || ( referenceTimeAbs != null ) )
        {
            BGType bgType = BGType.NEUTRAL;
            Color capBgColor = captionBackgroundColor.getColor();
            Color dataBgColor = dataBackgroundColor.getColor();
            if ( referencePlace == 1 )
            {
                bgType = BGType.POSITION_FIRST;
                capBgColor = captionBackgroundColor1st.getColor();
                dataBgColor = dataBackgroundColor1st.getColor();
            }
            
            if ( ( referenceTimeAbs != null ) && ( isEditorMode || ( lapState.getValue().isAfterSectorStart() && !vsi.isInPits() ) ) )
            {
                switch ( vsi.getSector() )
                {
                    case 1:
                        if ( ( vsi.getLaptime( vsi.getLapsCompleted() ) == null ) || ( vsi.getLaptime( vsi.getLapsCompleted() ).isOutlap() == Boolean.TRUE ) )
                        {
                        }
                        else if ( vsi.getLastLapTime() < referenceTimeAbs.getLapTime() )
                        {
                            bgType = BGType.FASTEST;
                            dataBgColor = dataBackgroundColorFastest.getColor();
                        }
                        else if ( ( referenceTimePers != null ) && ( vsi.getLastLapTime() < referenceTimePers.getLapTime() ) )
                        {
                            bgType = BGType.FASTER;
                            dataBgColor = dataBackgroundColorFaster.getColor();
                        }
                        else
                        {
                            bgType = BGType.SLOWER;
                            dataBgColor = dataBackgroundColorSlower.getColor();
                        }
                        break;
                    case 2:
                        if ( vsi.getCurrentSector1() < referenceTimeAbs.getSector1() )
                        {
                            bgType = BGType.FASTEST;
                            dataBgColor = dataBackgroundColorFastest.getColor();
                        }
                        else if ( ( referenceTimePers != null ) && ( vsi.getCurrentSector1() < referenceTimePers.getSector1() ) )
                        {
                            bgType = BGType.FASTER;
                            dataBgColor = dataBackgroundColorFaster.getColor();
                        }
                        else
                        {
                            bgType = BGType.SLOWER;
                            dataBgColor = dataBackgroundColorSlower.getColor();
                        }
                        break;
                    case 3:
                        if ( vsi.getCurrentSector2( true ) < referenceTimeAbs.getSector2( true ) )
                        {
                            bgType = BGType.FASTEST;
                            dataBgColor = dataBackgroundColorFastest.getColor();
                        }
                        else if ( ( referenceTimePers != null ) && ( vsi.getCurrentSector2( true ) < referenceTimePers.getSector2( true ) ) )
                        {
                            bgType = BGType.FASTER;
                            dataBgColor = dataBackgroundColorFaster.getColor();
                        }
                        else
                        {
                            bgType = BGType.SLOWER;
                            dataBgColor = dataBackgroundColorSlower.getColor();
                        }
                        break;
                }
            }
            
            if ( bgType != null )
            {
                if ( useImages )
                    ETVUtils.drawLabeledDataBackgroundI( offsetX, offsetY + 2 * ( coords.rowHeight + gap ), coords.mainFieldWidthA, coords.rowHeight, "00", getFontProperty(), getImages(), bgType, texture, false );
                else
                    ETVUtils.drawLabeledDataBackground( offsetX, offsetY + 2 * ( coords.rowHeight + gap ), coords.mainFieldWidthA, coords.rowHeight, "00", getFontProperty(), capBgColor, dataBgColor, texture, false );
            }
        }
        
        Color capBgColor = captionBackgroundColor.getColor();
        boolean first = ( vsi.getPlace( getConfiguration().getUseClassScoring() ) == 1 );
        if ( first )
            capBgColor = captionBackgroundColor1st.getColor();
        
        if ( useImages )
            ETVUtils.drawBigPositionBackgroundI( offsetX + width - coords.bigPosWidth, offsetY, coords.bigPosWidth, coords.rowHeight + gap + coords.rowHeight, getImages(), first, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + width - coords.bigPosWidth, offsetY, coords.bigPosWidth, coords.rowHeight + gap + coords.rowHeight, capBgColor, texture, false );
    }
    
    @Override
    public void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        
        vsiID.update( vsi.getDriverId() );
        if ( needsCompleteRedraw || ( clock.c() && vsiID.hasChanged() ) )
        {
            drivernameString.draw( offsetX, offsetY, vsi.getDriverNameShort( getShowNamesInAllUppercase() ), texture );
        }
        //drivernameString.draw( offsetX, offsetY, lapState.getValue().SHORT, texture );
        
        ownPlace.update( vsi.getPlace( getConfiguration().getUseClassScoring() ) );
        if ( needsCompleteRedraw || ( clock.c() && ownPlace.hasChanged() ) )
        {
            bigPositionString.draw( offsetX, offsetY, ownPlace.getValueAsString(), captionColor.getColor(), texture );
        }
        
        LapState ls = lapState.getValue();
        if ( isEditorMode && ( ( ls == LapState.SOMEWHERE ) || ls.isBeforeSectorEnd() ) )
        {
            ownLaptime.update( vsi.getCurrentLaptime() );
        }
        else if ( ls.isAfterSectorStart() )
        {
            switch ( vsi.getSector() )
            {
                case 1:
                    ownLaptime.update( vsi.getLastLapTime() );
                    break;
                case 2:
                    ownLaptime.update( vsi.getCurrentSector1() );
                    break;
                case 3:
                    ownLaptime.update( vsi.getCurrentSector2( true ) );
                    break;
            }
        }
        else
        {
            ownLaptime.update( vsi.getCurrentLaptime() );
        }
        
        if ( needsCompleteRedraw || ( clock.c() && ownLaptime.hasChanged() ) )
        {
            if ( ownLaptime.isValid() )
            {
                if ( alwaysShowFull1000.getBooleanValue() || ls.isAfterSectorStart() )
                    laptimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString( ownLaptime.getValue() ), texture );
                else
                    laptimeString.draw( offsetX + laptimeOffsetX, offsetY, TimingUtil.getTimeAsString( ownLaptime.getValue(), false, false, true, false ), texture );
            }
            else
            {
                laptimeString.draw( offsetX, offsetY, "", texture );
            }
        }
        
        //debug( "abs: ", referenceTimeAbs );
        //debug( "pers: ", referenceTimePers );
        
        if ( isEditorMode || ( referenceTimeAbs != null ) )
        {
            relPlace.update( referencePlace );
            
            if ( needsCompleteRedraw || ( clock.c() && relPlace.hasChanged() ) )
            {
                relPositionString.draw( offsetX, offsetY, relPlace.getValueAsString(), texture );
            }
            
            Color dataColor = getFontColor();
            
            if ( referenceTimeAbs == null )
            {
                relLaptime.update( relLaptime.getResetValue() );
            }
            else if ( ( ls == LapState.OUTLAP ) || ( ls == LapState.SOMEWHERE ) || ls.isBeforeSectorEnd() )
            {
                float lt = -1f;
                switch ( vsi.getSector() )
                {
                    case 1:
                        lt = referenceTimeAbs.getSector1();
                        break;
                    case 2:
                        lt = referenceTimeAbs.getSector2( true );
                        break;
                    case 3:
                        lt = referenceTimeAbs.getLapTime();
                        break;
                }
                
                if ( lt <= 0f )
                    lt = relLaptime.getResetValue();
                
                relLaptime.update( lt );
            }
            else if ( ls.isAfterSectorStart() )
            {
                switch ( vsi.getSector() )
                {
                    case 1:
                        relLaptime.update( vsi.getLastLapTime() - referenceTimeAbs.getLapTime() );
                        
                        if ( vsi.getLastLapTime() < referenceTimeAbs.getLapTime() )
                            dataColor = dataColorFastest.getColor();
                        else if ( ( referenceTimePers != null ) && ( vsi.getLastLapTime() < referenceTimePers.getLapTime() ) )
                            dataColor = dataColorFaster.getColor();
                        else
                            dataColor = dataColorSlower.getColor();
                        break;
                    case 2:
                        relLaptime.update( vsi.getCurrentSector1() - referenceTimeAbs.getSector1() );
                        
                        if ( vsi.getCurrentSector1() < referenceTimeAbs.getSector1() )
                            dataColor = dataColorFastest.getColor();
                        else if ( ( referenceTimePers != null ) && ( vsi.getCurrentSector1() < referenceTimePers.getSector1() ) )
                            dataColor = dataColorFaster.getColor();
                        else
                            dataColor = dataColorSlower.getColor();
                        break;
                    case 3:
                        relLaptime.update( vsi.getCurrentSector2( true ) - referenceTimeAbs.getSector2( true ) );
                        
                        if ( vsi.getCurrentSector2( true ) < referenceTimeAbs.getSector2( true ) )
                            dataColor = dataColorFastest.getColor();
                        else if ( ( referenceTimePers != null ) && ( vsi.getCurrentSector2( true ) < referenceTimePers.getSector2( true ) ) )
                            dataColor = dataColorFaster.getColor();
                        else
                            dataColor = dataColorSlower.getColor();
                        break;
                }
            }
            else
            {
                relLaptime.update( relLaptime.getResetValue() );
            }
            
            if ( needsCompleteRedraw || ( clock.c() && relLaptime.hasChanged() ) )
            {
                if ( relLaptime.isValid() && ownLaptime.isValid() )
                {
                    if ( ls.isAfterSectorStart() )
                        relTimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsGapString( relLaptime.getValue() ), dataColor, texture );
                    else if ( ( ls == LapState.OUTLAP ) || ( ls == LapState.SOMEWHERE ) || ls.isBeforeSectorEnd() )
                        relTimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString( relLaptime.getValue() ), texture );
                }
                else
                {
                    relTimeString.draw( offsetX, offsetY, "", texture );
                }
            }
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( positionFontSize, "Font size for the position in percent relative to the normal font size." );
        writer.writeProperty( alwaysShowFull1000, "Show full thousands in compare time, even if sector not completed?" );
        writer.writeProperty( positionItemGap, "The gap between the main elements and the position element in pixels." );
        
        writer.writeProperty( displayType, "Always display or just at sector boundaries or always if valid time was made?" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( positionFontSize ) );
        else if ( loader.loadProperty( alwaysShowFull1000 ) );
        else if ( loader.loadProperty( positionItemGap ) );
        else if ( loader.loadProperty( displayType ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void addVisibilityPropertiesToContainer( PropertiesContainer propsCont, boolean forceAll )
    {
        super.addVisibilityPropertiesToContainer( propsCont, forceAll );
        
        propsCont.addProperty( displayType );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void getPropertiesForParentGroup( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getPropertiesForParentGroup( propsCont, forceAll );
        
        propsCont.addProperty( positionFontSize );
        propsCont.addProperty( alwaysShowFull1000 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void getItemGapProperty( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getItemGapProperty( propsCont, forceAll );
        
        propsCont.addProperty( positionItemGap );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        positionItemGap.setIntValue( 0 );
    }
    
    public ETVTimingWidget()
    {
        super( 20.0f, 8.496094f );
    }
}
