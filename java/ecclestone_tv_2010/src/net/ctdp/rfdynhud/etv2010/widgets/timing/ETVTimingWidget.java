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
package net.ctdp.rfdynhud.etv2010.widgets.timing;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.etv2010.widgets._base.ETVTimingWidgetBase;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.EnumProperty;
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
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.LapState;
import net.ctdp.rfdynhud.values.ValidityTest;

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
    
    private final IntProperty positionFontSize = new IntProperty( this, "positionFontSize", 200 );
    
    private final EnumProperty<DisplayType> displayType = new EnumProperty<DisplayType>( this, "displayType", DisplayType.AT_SECTORS );
    
    private final IntProperty visibleTimeBeforeSector = new IntProperty( this, "visibleTimeBeforeSector", 7 );
    private final IntProperty visibleTimeAfterSector = new IntProperty( this, "visibleTimeAfterSector", 7 );
    
    private Font positionFont = null;
    
    private int rowHeight = 0;
    
    private int dataWidth = 0;
    private int bigPositionWidth = 0;
    private int smallPositionWidth = 0;
    
    private DrawnString drivernameString = null;
    private DrawnString laptimeString = null;
    private DrawnString bigPositionString = null;
    private DrawnString relPositionString = null;
    private DrawnString relTimeString = null;
    
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
    public int getVersion()
    {
        return ( composeVersion( 1, 0, 0 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPropertyChanged( Property property, Object oldValue, Object newValue )
    {
        super.onPropertyChanged( property, oldValue, newValue );
        
        if ( property == positionFontSize )
        {
            positionFont = null;
        }
        else if ( property == getFontProperty() )
        {
            positionFont = null;
            
            forceReinitialization();
            forceCompleteRedraw( false );
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
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
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
    public void updateVisibility( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.updateVisibility( clock1, clock2, gameData, editorPresets );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        VehicleScoringInfo refVSI = scoringInfo.getFastestLapVSI();
        Laptime relTime = refVSI.getFastestLaptime();
        
        LapState ls = LapState.getLapState( vsi, relTime, visibleTimeBeforeSector.getIntValue(), visibleTimeAfterSector.getIntValue(), true );
        
        lapState.update( ls );
        
        if ( editorPresets != null )
        {
            referenceTimeAbs = new Laptime( 2, 28.733f, 29.649f, 26.36f, false, false, true );
            referenceTimePers = editorPresets.getLastLaptime();
            referencePlace = 1;
            
            setUserVisible2( true );
            return;
        }
        
        if ( ( ls != LapState.AFTER_SECTOR1_START ) && ( ( vsi.getStintLength() % 1.0f ) > 0.1f ) )
        {
            referenceTimeAbs = relTime;
            referenceTimePers = vsi.getFastestLaptime();
            referencePlace = refVSI.getPlace( getConfiguration().getUseClassScoring() );
        }
        
        if ( scoringInfo.getSessionType().isRace() )
        {
            setUserVisible2( false );
            return;
        }
        
        if ( displayType.getEnumValue() == DisplayType.ALWAYS )
        {
            setUserVisible2( true );
            return;
        }
        
        if ( displayType.getEnumValue() == DisplayType.IF_LAP_VALID )
        {
            setUserVisible2( !vsi.getLaptime( vsi.getCurrentLap() ).isOutlap() );
            return;
        }
        
        if ( relTime == null )
        {
            setUserVisible2( ls == LapState.BEFORE_SECTOR3_END );
            return;
        }
        
        if ( ( ls == LapState.SOMEWHERE ) || ( ls == LapState.OUTLAP ) )
        {
            setUserVisible2( false );
            return;
        }
        
        setUserVisible2( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( lapState.hasChanged() )
            return ( true );
        
        if ( editorPresets != null )
        {
            hasRefTime.update( referenceTimeAbs != null );
            
            if ( hasRefTime.hasChanged() )
                return ( true );
        }
        
        return ( false );
    }
    
    private void updateRowHeight( int height )
    {
        rowHeight = ( height - 2 * ETVUtils.ITEM_GAP ) / 3;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        updateRowHeight( height );
        
        Rectangle2D posBounds = texture.getStringBounds( "00", getPositionFont(), isFontAntiAliased() );
        
        bigPositionWidth = (int)Math.round( posBounds.getWidth() );
        dataWidth = width - 6 * ETVUtils.TRIANGLE_WIDTH - bigPositionWidth - ETVUtils.TRIANGLE_WIDTH / 2;
        
        int vMiddle = ETVUtils.getLabeledDataVMiddle( rowHeight + ETVUtils.ITEM_GAP + rowHeight, posBounds );
        
        bigPositionString = dsf.newDrawnString( "bigPositionString", width - 2 * ETVUtils.TRIANGLE_WIDTH - bigPositionWidth / 2, 0 + vMiddle, Alignment.CENTER, false, getPositionFont(), isFontAntiAliased(), captionColor.getColor() );
        
        Rectangle2D capBounds = texture.getStringBounds( "00", getFontProperty() );
        
        smallPositionWidth = (int)Math.round( capBounds.getWidth() );
        
        vMiddle = ETVUtils.getLabeledDataVMiddle( rowHeight, capBounds );
        
        drivernameString = dsf.newDrawnString( "drivernameString", 3 * ETVUtils.TRIANGLE_WIDTH + dataWidth, 0 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
        laptimeString = dsf.newDrawnString( "laptimeString", 2 * ETVUtils.TRIANGLE_WIDTH + dataWidth, 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
        relPositionString = dsf.newDrawnString( "relPositionString", 1 * ETVUtils.TRIANGLE_WIDTH + smallPositionWidth / 2, 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        relTimeString = dsf.newDrawnString( "relTimeString", 1 * ETVUtils.TRIANGLE_WIDTH + dataWidth, 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        forceCompleteRedraw( true );
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, editorPresets, texture, offsetX, offsetY, width, height, isRoot );
        
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        
        int dataWidth2 = ETVUtils.TRIANGLE_WIDTH + dataWidth + ETVUtils.TRIANGLE_WIDTH;
        
        ETVUtils.drawDataBackground( offsetX + 2 * ETVUtils.TRIANGLE_WIDTH, offsetY + 0 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidth2, rowHeight, dataBackgroundColor.getColor(), texture, false );
        ETVUtils.drawDataBackground( offsetX + 1 * ETVUtils.TRIANGLE_WIDTH, offsetY + 1 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidth2, rowHeight, dataBackgroundColor.getColor(), texture, false );
        
        if ( ( editorPresets != null ) || ( referenceTimeAbs != null ) )
        {
            Color capBgColor = captionBackgroundColor.getColor();
            if ( referencePlace == 1 )
                capBgColor = captionBackgroundColor1st.getColor();
            
            Color dataBgColor = dataBackgroundColor.getColor();
            if ( editorPresets != null )
            {
                switch ( vsi.getSector() )
                {
                    case 1:
                        if ( editorPresets.getLastLapTime() < referenceTimeAbs.getLapTime() )
                            dataBgColor = dataBackgroundColorFastest.getColor();
                        else if ( editorPresets.getLastLapTime() < referenceTimePers.getLapTime() )
                            dataBgColor = dataBackgroundColorFaster.getColor();
                        else
                            dataBgColor = dataBackgroundColorSlower.getColor();
                        break;
                    case 2:
                        if ( editorPresets.getCurrentSector1Time() < referenceTimeAbs.getSector1() )
                            dataBgColor = dataBackgroundColorFastest.getColor();
                        else if ( editorPresets.getCurrentSector1Time() < referenceTimePers.getSector1() )
                            dataBgColor = dataBackgroundColorFaster.getColor();
                        else
                            dataBgColor = dataBackgroundColorSlower.getColor();
                        break;
                    case 3:
                        if ( editorPresets.getCurrentSector2Time( true ) < referenceTimeAbs.getSector2( true ) )
                            dataBgColor = dataBackgroundColorFastest.getColor();
                        else if ( editorPresets.getCurrentSector2Time( true ) < referenceTimePers.getSector2( true ) )
                            dataBgColor = dataBackgroundColorFaster.getColor();
                        else
                            dataBgColor = dataBackgroundColorSlower.getColor();
                        break;
                }
            }
            else if ( referenceTimeAbs != null )
            {
                LapState ls = lapState.getValue();
                
                if ( !ls.isAfterSectorStart() )
                {
                    dataBgColor = dataBackgroundColor1st.getColor();
                }
                else
                {
                    switch ( vsi.getSector() )
                    {
                        case 1:
                            if ( vsi.getLastLapTime() < referenceTimeAbs.getLapTime() )
                                dataBgColor = dataBackgroundColorFastest.getColor();
                            else if ( ( referenceTimePers != null ) && ( vsi.getLastLapTime() < referenceTimePers.getLapTime() ) )
                                dataBgColor = dataBackgroundColorFaster.getColor();
                            else
                                dataBgColor = dataBackgroundColorSlower.getColor();
                            break;
                        case 2:
                            if ( vsi.getCurrentSector1() < referenceTimeAbs.getSector1() )
                                dataBgColor = dataBackgroundColorFastest.getColor();
                            else if ( ( referenceTimePers != null ) && ( vsi.getCurrentSector1() < referenceTimePers.getSector1() ) )
                                dataBgColor = dataBackgroundColorFaster.getColor();
                            else
                                dataBgColor = dataBackgroundColorSlower.getColor();
                            break;
                        case 3:
                            if ( vsi.getCurrentSector2( true ) < referenceTimeAbs.getSector2( true ) )
                                dataBgColor = dataBackgroundColorFastest.getColor();
                            else if ( ( referenceTimePers != null ) && ( vsi.getCurrentSector2( true ) < referenceTimePers.getSector2( true ) ) )
                                dataBgColor = dataBackgroundColorFaster.getColor();
                            else
                                dataBgColor = dataBackgroundColorSlower.getColor();
                            break;
                    }
                }
            }
            
            ETVUtils.drawLabeledDataBackground( offsetX + 0 * ETVUtils.TRIANGLE_WIDTH, offsetY + 2 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidth2, rowHeight, "00", getFont(), capBgColor, dataBgColor, texture, false );
        }
        
        Color capBgColor = captionBackgroundColor.getColor();
        if ( vsi.getPlace( getConfiguration().getUseClassScoring() ) == 1 )
            capBgColor = captionBackgroundColor1st.getColor();
        
        ETVUtils.drawDataBackground( offsetX + width - 4 * ETVUtils.TRIANGLE_WIDTH - bigPositionWidth, offsetY, 4 * ETVUtils.TRIANGLE_WIDTH + bigPositionWidth, 2 * rowHeight + ETVUtils.ITEM_GAP, 2, capBgColor, texture, false );
        
        drivernameString.resetClearRect();
        drivernameString.draw( offsetX, offsetY, vsi.getDriverNameShort(), texture, false );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        
        ownPlace.update( vsi.getPlace( getConfiguration().getUseClassScoring() ) );
        
        if ( needsCompleteRedraw || ( clock1 && ownPlace.hasChanged() ) )
        {
            bigPositionString.draw( offsetX, offsetY, ownPlace.getValueAsString(), captionColor.getColor(), texture );
        }
        
        LapState ls = lapState.getValue();
        if ( editorPresets != null )
        {
            switch ( vsi.getSector() )
            {
                case 1:
                    ownLaptime.update( editorPresets.getLastLapTime() );
                    break;
                case 2:
                    ownLaptime.update( editorPresets.getCurrentSector1Time() );
                    break;
                case 3:
                    ownLaptime.update( editorPresets.getCurrentSector2Time( true ) );
                    break;
            }
        }
        else if ( ( ls == LapState.SOMEWHERE ) || ls.isBeforeSectorEnd() )
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
            ownLaptime.update( -1f );
        }
        
        if ( needsCompleteRedraw || ( clock1 && ownLaptime.hasChanged() ) )
        {
            if ( ownLaptime.isValid() )
                laptimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString( ownLaptime.getValue() ), texture );
            else
                laptimeString.draw( offsetX, offsetY, "", texture );
        }
        
        if ( ( editorPresets != null ) || ( referenceTimeAbs != null ) )
        {
            relPlace.update( referencePlace );
            
            if ( needsCompleteRedraw || ( clock1 && relPlace.hasChanged() ) )
            {
                relPositionString.draw( offsetX, offsetY, relPlace.getValueAsString(), texture );
            }
            
            Color dataColor = getFontColor();
            
            if ( editorPresets != null )
            {
                switch ( vsi.getSector() )
                {
                    case 1:
                        relLaptime.update( editorPresets.getLastLapTime() - referenceTimeAbs.getLapTime() );
                        
                        if ( vsi.getLastLapTime() < referenceTimeAbs.getLapTime() )
                            dataColor = dataColorFastest.getColor();
                        else if ( ( referenceTimePers != null ) && ( vsi.getLastLapTime() < referenceTimePers.getLapTime() ) )
                            dataColor = dataColorFaster.getColor();
                        else
                            dataColor = dataColorSlower.getColor();
                        break;
                    case 2:
                        relLaptime.update( editorPresets.getCurrentSector1Time() - referenceTimeAbs.getSector1() );
                        
                        if ( editorPresets.getCurrentSector1Time() < referenceTimeAbs.getSector1() )
                            dataColor = dataColorFastest.getColor();
                        else if ( editorPresets.getCurrentSector1Time() < referenceTimePers.getSector1() )
                            dataColor = dataColorFaster.getColor();
                        else
                            dataColor = dataColorSlower.getColor();
                        break;
                    case 3:
                        relLaptime.update( editorPresets.getCurrentSector2Time( true ) - referenceTimeAbs.getSector2( true ) );
                        
                        if ( editorPresets.getCurrentSector2Time( true ) < referenceTimeAbs.getSector2( true ) )
                            dataColor = dataColorFastest.getColor();
                        else if ( editorPresets.getCurrentSector2Time( true ) < referenceTimePers.getSector2( true ) )
                            dataColor = dataColorFaster.getColor();
                        else
                            dataColor = dataColorSlower.getColor();
                        break;
                }
            }
            else if ( referenceTimeAbs == null )
            {
                relLaptime.update( relLaptime.getResetValue() );
            }
            else if ( ( ls == LapState.SOMEWHERE ) || ls.isBeforeSectorEnd() )
            {
                switch ( vsi.getSector() )
                {
                    case 1:
                        relLaptime.update( referenceTimeAbs.getSector1() );
                        break;
                    case 2:
                        relLaptime.update( referenceTimeAbs.getSector2( true ) );
                        break;
                    case 3:
                        relLaptime.update( referenceTimeAbs.getLapTime() );
                        break;
                }
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
            
            if ( needsCompleteRedraw || ( clock1 && relLaptime.hasChanged() ) )
            {
                if ( relLaptime.isValid() )
                {
                    if ( ls.isAfterSectorStart() || ( editorPresets != null ) )
                        relTimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsGapString( relLaptime.getValue() ), dataColor, texture );
                    else if ( ( ls == LapState.SOMEWHERE ) || ls.isBeforeSectorEnd() )
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
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( positionFontSize, "Font size for the position in percent relative to the normal font size." );
        
        writer.writeProperty( displayType, "Always display or just at sector boundaries or always if valid time was made?" );
        writer.writeProperty( visibleTimeBeforeSector, "The Widget is visible for the given amount of seconds before the relative sector time is reached." );
        writer.writeProperty( visibleTimeAfterSector, "The Widget is visible for the given amount after a sector was finished." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( positionFontSize ) );
        else if ( loader.loadProperty( displayType ) );
        else if ( loader.loadProperty( visibleTimeBeforeSector ) );
        else if ( loader.loadProperty( visibleTimeAfterSector ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addProperty( positionFontSize );
        
        propsCont.addGroup( "Visiblity" );
        
        propsCont.addProperty( displayType );
        propsCont.addProperty( visibleTimeBeforeSector );
        propsCont.addProperty( visibleTimeAfterSector );
    }
    
    public ETVTimingWidget( String name )
    {
        super( name, 20.0f, 8.496094f );
    }
}
