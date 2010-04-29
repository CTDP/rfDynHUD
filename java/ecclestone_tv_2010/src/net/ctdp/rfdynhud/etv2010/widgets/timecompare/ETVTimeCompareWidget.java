package net.ctdp.rfdynhud.etv2010.widgets.timecompare;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.etv2010.widgets._base.ETVTimingWidgetBase;
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
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.Size;

/**
 * The {@link ETVTimeCompareWidget} displays lap time gaps in race sessions.
 * 
 * @author Marvin Froehlich
 */
public class ETVTimeCompareWidget extends ETVTimingWidgetBase
{
    private final IntProperty displayEveryXLaps = new IntProperty( this, "displayEveryXLaps", 3, 1, 20 );
    private final FloatProperty visibleTime = new FloatProperty( this, "visibleTime", 8.0f, 1.0f, 60.0f );
    
    private final BooleanProperty preferNextInFront = new BooleanProperty( this, "preferNextInFront", false );
    
    private int rowHeight = 0;
    
    private int namesWidth = 0;
    private int timesWidth = 0;
    
    private int positionWidth = 0;
    //private int dataWidthNames = 0;
    private int dataWidthTimes = 0;
    
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
    private int relLapsOffset = 0;
    
    private int decision = 0;
    private short decisionPlace = 0;
    private float hideTime = -1f;
    
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
            forceCompleteRedraw();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onSessionStarted( sessionType, gameData, editorPresets );
        
        laps.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        decision = 0;
        hideTime = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVisibility( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.updateVisibility( clock1, clock2, gameData, editorPresets );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        if ( editorPresets != null )
        {
            relVSI = scoringInfo.getVehicleScoringInfo( vsi.getPlace() - 2 ); // next in front
            relLapsOffset = vsi.getLapsBehindNextInFront();
            decision = 0;
            return;
        }
        
        relVSI = null;
        
        if ( !scoringInfo.getSessionType().isRace() || !vsi.getFinishStatus().isNone() )
        {
            setVisible( false );
            return;
        }
        
        laps.update( vsi.getLapsCompleted() );
        
        if ( laps.hasChanged() )
        {
            if ( laps.getValue() < 3 )
            {
                decision = 0;
                setVisible( false );
            }
            else if ( ( laps.getValue() % displayEveryXLaps.getIntValue() ) == 0 )
            {
                if ( ( decision != 0 ) && ( decisionPlace != vsi.getPlace() ) )
                {
                    //Logger.log( "x" );
                    decision = 0;
                }
                
                if ( decision == -1 )
                {
                }
                else if ( decision == +1 )
                {
                    VehicleScoringInfo vsi_nb = scoringInfo.getVehicleScoringInfo( vsi.getPlace() ); // next behind
                    if ( !vsi_nb.getFinishStatus().isNone() )
                    {
                        //Logger.log( "y" );
                        decision = 0;
                        laps.reset();
                        setVisible( false );
                    }
                    else if ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront() >= laps.getValue() )
                    {
                        //Logger.log( "z" );
                        decision = 0;
                        relVSI = vsi_nb;
                        relLapsOffset = vsi_nb.getLapsBehindNextInFront();
                        setVisible( true );
                    }
                }
                else
                {
                    boolean b = false;
                    
                    if ( scoringInfo.getNumVehicles() == 1 )
                    {
                        b = false;
                    }
                    else if ( vsi.getPlace() == 1 )
                    {
                        VehicleScoringInfo vsi_nb = scoringInfo.getVehicleScoringInfo( 1 ); // 2nd
                        if ( vsi_nb.getFinishStatus().isNone()  )
                        {
                            if ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront() < laps.getValue() )
                            {
                                //Logger.log( "a" );
                                decision = +1;
                                decisionPlace = vsi.getPlace();
                                b = false;
                                laps.reset();
                            }
                            else
                            {
                                decision = 0;
                                relVSI = vsi_nb;
                                relLapsOffset = vsi_nb.getLapsBehindNextInFront();
                                b = true;
                            }
                        }
                        else
                        {
                            decision = 0;
                            b = false;
                        }
                    }
                    else if ( vsi.getPlace() == scoringInfo.getNumVehicles() )
                    {
                        VehicleScoringInfo vsi_nif = scoringInfo.getVehicleScoringInfo( scoringInfo.getNumVehicles() - 2 ); // next in front
                        b = vsi_nif.getFinishStatus().isNone();
                        
                        decision = 0;
                        
                        if ( b )
                        {
                            relVSI = vsi_nif;
                            relLapsOffset = vsi.getLapsBehindNextInFront();
                        }
                    }
                    else
                    {
                        // There are at least 3 vehicles in the race.
                        
                        VehicleScoringInfo vsi_nif = scoringInfo.getVehicleScoringInfo( vsi.getPlace() - 2 ); // next in front
                        if ( vsi_nif.getFinishStatus().isNone() && ( vsi_nif.getLapsCompleted() + vsi.getLapsBehindNextInFront() >= 3 ) )
                        {
                            VehicleScoringInfo vsi_nb = scoringInfo.getVehicleScoringInfo( vsi.getPlace() ); // next behind
                            
                            if ( preferNextInFront.getBooleanValue() || !vsi_nb.getFinishStatus().isNone() || ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront() >= 3 ) )
                            {
                                decision = 0;
                                relVSI = vsi_nif;
                                relLapsOffset = vsi.getLapsBehindNextInFront();
                                b = true;
                            }
                            else
                            {
                                float gapToNextInFront = Math.abs( vsi.getTimeBehindNextInFront() );
                                float gapToNextBehind = Math.abs( vsi_nb.getTimeBehindNextInFront() );
                                
                                if ( gapToNextInFront < gapToNextBehind )
                                {
                                    decision = 0;
                                    relVSI = vsi_nif;
                                    relLapsOffset = vsi.getLapsBehindNextInFront();
                                    b = true;
                                }
                                else if ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront() < laps.getValue() )
                                {
                                    //Logger.log( "b" );
                                    decision = +1;
                                    decisionPlace = vsi.getPlace();
                                    b = false;
                                    laps.reset();
                                }
                                else
                                {
                                    decision = 0;
                                    relVSI = vsi_nb;
                                    relLapsOffset = vsi_nb.getLapsBehindNextInFront();
                                    b = true;
                                }
                            }
                        }
                        else
                        {
                            VehicleScoringInfo vsi_nb = scoringInfo.getVehicleScoringInfo( vsi.getPlace() ); // next behind
                            if ( vsi_nb.getFinishStatus().isNone() && ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront() >= 3 ) )
                            {
                                if ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront() < laps.getValue() )
                                {
                                    //Logger.log( "c" );
                                    decision = +1;
                                    decisionPlace = vsi.getPlace();
                                    b = false;
                                    laps.reset();
                                }
                                else
                                {
                                    decision = 0;
                                    relVSI = vsi_nb;
                                    relLapsOffset = vsi_nb.getLapsBehindNextInFront();
                                    b = true;
                                }
                            }
                            else
                            {
                                decision = 0;
                                b = false;
                            }
                        }
                    }
                    
                    setVisible( b );
                    
                    if ( b )
                    {
                        hideTime = scoringInfo.getSessionTime() + visibleTime.getFloatValue();
                        forceCompleteRedraw();
                    }
                }
            }
            else
            {
                decision = 0;
                setVisible( false );
                hideTime = -1f;
            }
        }
        else if ( scoringInfo.getSessionTime() < hideTime )
        {
            setVisible( true );
        }
        else
        {
            decision = 0;
            setVisible( false );
            hideTime = -1f;
        }
    }
    
    private void updateRowHeight( int height )
    {
        rowHeight = ( height - 2 * ETVUtils.ITEM_GAP ) / 3;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        updateRowHeight( height );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        texCanvas.setFont( getFont() );
        FontMetrics positionMetrics = texCanvas.getFontMetrics();
        
        Rectangle2D posBounds = positionMetrics.getStringBounds( "00", texCanvas );
        
        namesWidth = Math.round( width * 0.35f );
        timesWidth = width - namesWidth - ETVUtils.ITEM_GAP;
        
        positionWidth = (int)Math.round( posBounds.getWidth() );
        //dataWidthNames = namesWidth - positionWidth - 4 * ETVUtils.TRIANGLE_WIDTH;
        dataWidthTimes = ( timesWidth - 3 * ETVUtils.TRIANGLE_WIDTH * 3 / 2 - 2 * ETVUtils.ITEM_GAP ) / 3;
        
        int vMiddle = ETVUtils.getLabeledDataVMiddle( rowHeight, posBounds );
        
        positionString1 = new DrawnString( 2 * ETVUtils.TRIANGLE_WIDTH + positionWidth / 2, 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        positionString2 = new DrawnString( 1 * ETVUtils.TRIANGLE_WIDTH + positionWidth / 2, 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        drivernameString1 = new DrawnString( 3 * ETVUtils.TRIANGLE_WIDTH + positionWidth, 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        drivernameString2 = new DrawnString( 2 * ETVUtils.TRIANGLE_WIDTH + positionWidth, 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        lapCaptionString1 = new DrawnString( namesWidth + 1 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH * 3 / 2 + dataWidthTimes / 2 + 0 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 0 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        lapCaptionString2 = new DrawnString( namesWidth + 2 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH * 3 / 2 + dataWidthTimes / 2 + 1 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 0 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        lapCaptionString3 = new DrawnString( namesWidth + 3 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH * 3 / 2 + dataWidthTimes / 2 + 2 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 0 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        laptimeString1 = new DrawnString( namesWidth + 1 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 0 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        laptimeString2 = new DrawnString( namesWidth + 2 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 1 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        laptimeString3 = new DrawnString( namesWidth + 3 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 2 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        gapString1 = new DrawnString( namesWidth + 1 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 0 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        gapString2 = new DrawnString( namesWidth + 2 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 1 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        gapString3 = new DrawnString( namesWidth + 3 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 2 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        forceCompleteRedraw();
    }
    
    private static final float getLaptime( VehicleScoringInfo vsi, int lap )
    {
        Laptime lt = vsi.getLaptime( lap );
        
        if ( lt == null )
            return ( -1f );
        
        return ( lt.getLapTime() );        
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        texture.clear( offsetX, offsetY, width, height, true, null );
        
        //int positionWidth2 = ETVUtils.TRIANGLE_WIDTH + positionWidth + ETVUtils.TRIANGLE_WIDTH;
        //int dataWidthNames2 = ETVUtils.TRIANGLE_WIDTH + dataWidthNames + ETVUtils.TRIANGLE_WIDTH;
        int dataWidthTimes2 = ETVUtils.TRIANGLE_WIDTH + dataWidthTimes + ETVUtils.TRIANGLE_WIDTH;
        
        Color captionBgColor = captionBackgroundColor.getColor();
        Color dataBgColor = getBackgroundColor();
        if ( vsi.getPlace() == 1 )
            captionBgColor = captionBackgroundColor1st.getColor();
        
        ETVUtils.drawLabeledDataBackground( offsetX + ETVUtils.TRIANGLE_WIDTH, offsetY + 1 * ( rowHeight + ETVUtils.ITEM_GAP ), namesWidth - ETVUtils.TRIANGLE_WIDTH / 2, rowHeight, "00", getFont(), captionBgColor, dataBgColor, texture, false );
        positionString1.resetClearRect();
        positionString1.draw( offsetX, offsetY, String.valueOf( vsi.getPlace() ), null, texture );
        drivernameString1.resetClearRect();
        drivernameString1.draw( offsetX, offsetY, vsi.getDriverNameShort(), null, texture );
        
        captionBgColor = captionBackgroundColor.getColor();
        dataBgColor = getBackgroundColor();
        if ( vsi.getPlace() == 1 )
            captionBgColor = captionBackgroundColor1st.getColor();
        
        ETVUtils.drawLabeledDataBackground( offsetX, offsetY + 2 * ( rowHeight + ETVUtils.ITEM_GAP ), namesWidth - ETVUtils.TRIANGLE_WIDTH / 2, rowHeight, "00", getFont(), captionBgColor, dataBgColor, texture, false );
        positionString2.resetClearRect();
        positionString2.draw( offsetX, offsetY, String.valueOf( relVSI.getPlace() ), null, texture );
        drivernameString2.resetClearRect();
        drivernameString2.draw( offsetX, offsetY, relVSI.getDriverNameShort(), null, texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 + 0 * dataWidthTimes2, offsetY, dataWidthTimes2, rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString1.resetClearRect();
        lapCaptionString1.draw( offsetX, offsetY, "Lap " + ( vsi.getLapsCompleted() - 2 ), null, texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 2 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 - ETVUtils.TRIANGLE_WIDTH + 1 * dataWidthTimes2, offsetY, dataWidthTimes2, rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString2.resetClearRect();
        lapCaptionString2.draw( offsetX, offsetY, "Lap " + ( vsi.getLapsCompleted() - 1 ), null, texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 3 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 - 2 * ETVUtils.TRIANGLE_WIDTH + 2 * dataWidthTimes2, offsetY, dataWidthTimes2, rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString3.resetClearRect();
        lapCaptionString3.draw( offsetX, offsetY, "Lap " + ( vsi.getLapsCompleted() - 0 ), null, texture );
        
        float laptime1 = isEditorMode ? 84.567f : getLaptime( vsi, vsi.getLapsCompleted() - 2 );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 + 0 * dataWidthTimes2, offsetY + rowHeight + ETVUtils.ITEM_GAP, dataWidthTimes2, rowHeight, getBackgroundColor(), texture, false );
        if ( laptime1 > 0f )
        {
            laptimeString1.resetClearRect();
            laptimeString1.draw( offsetX, offsetY, TimingUtil.getTimeAsString( laptime1, false, false, true ), null, texture );
        }
        
        float laptime2 = isEditorMode ? editorPresets.getLastLaptime() : getLaptime( vsi, vsi.getLapsCompleted() - 1 );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 2 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 - ETVUtils.TRIANGLE_WIDTH + 1 * dataWidthTimes2, offsetY + rowHeight + ETVUtils.ITEM_GAP, dataWidthTimes2, rowHeight, getBackgroundColor(), texture, false );
        if ( laptime2 > 0f )
        {
            laptimeString2.resetClearRect();
            laptimeString2.draw( offsetX, offsetY, TimingUtil.getTimeAsString( laptime2, false, false, true ), null, texture );
        }
        
        float laptime3 = isEditorMode ? editorPresets.getCurrentLaptime() : getLaptime( vsi, vsi.getLapsCompleted() - 0 );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 3 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 - 2 * ETVUtils.TRIANGLE_WIDTH + 2 * dataWidthTimes2, offsetY + rowHeight + ETVUtils.ITEM_GAP, dataWidthTimes2, rowHeight, getBackgroundColor(), texture, false );
        if ( laptime3 > 0f )
        {
            laptimeString3.resetClearRect();
            laptimeString3.draw( offsetX, offsetY, TimingUtil.getTimeAsString( laptime3, false, false, true ), null, texture );
        }
        
        float gap1, gap2, gap3;
        String gapStr1, gapStr2, gapStr3;
        if ( editorPresets != null )
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
        
        dataBgColor = dataBackgroundColorFaster.getColor();
        Color dataColor = dataColorFaster.getColor();
        if ( gapStr1 == null )
        {
            dataBgColor = getBackgroundColor();
            dataColor = getFontColor();
        }
        else if ( gap1 < 0f )
        {
            dataBgColor = dataBackgroundColorSlower.getColor();
            dataColor = dataColorSlower.getColor();
        }
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH * 3 / 2 + 0 * dataWidthTimes2, offsetY + 2 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidthTimes2, rowHeight, dataBgColor, texture, false );
        if ( gapStr1 != null )
        {
            gapString1.resetClearRect();
            gapString1.draw( offsetX, offsetY, gapStr1, null, dataColor, texture );
        }
        
        dataBgColor = dataBackgroundColorFaster.getColor();
        dataColor = dataColorFaster.getColor();
        if ( gapStr2 == null )
        {
            dataBgColor = getBackgroundColor();
            dataColor = getFontColor();
        }
        else if ( gap2 < 0f )
        {
            dataBgColor = dataBackgroundColorSlower.getColor();
            dataColor = dataColorSlower.getColor();
        }
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 2 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH * 3 / 2 - ETVUtils.TRIANGLE_WIDTH + 1 * dataWidthTimes2, offsetY + 2 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidthTimes2, rowHeight, dataBgColor, texture, false );
        if ( gapStr2 != null )
        {
            gapString2.resetClearRect();
            gapString2.draw( offsetX, offsetY, gapStr2, null, dataColor, texture );
        }
        
        dataBgColor = dataBackgroundColorFaster.getColor();
        dataColor = dataColorFaster.getColor();
        if ( gapStr3 == null )
        {
            dataBgColor = getBackgroundColor();
            dataColor = getFontColor();
        }
        else if ( gap3 < 0f )
        {
            dataBgColor = dataBackgroundColorSlower.getColor();
            dataColor = dataColorSlower.getColor();
        }
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 3 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH * 3 / 2 - 2 * ETVUtils.TRIANGLE_WIDTH + 2 * dataWidthTimes2, offsetY + 2 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidthTimes2, rowHeight, dataBgColor, texture, false );
        if ( gapStr3 != null )
        {
            gapString3.resetClearRect();
            gapString3.draw( offsetX, offsetY, gapStr3, null, dataColor, texture );
        }
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
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
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( displayEveryXLaps.loadProperty( key, value ) );
        else if ( visibleTime.loadProperty( key, value ) );
        else if ( preferNextInFront.loadProperty( key, value ) );
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
        super( name, Size.PERCENT_OFFSET + 0.407f, Size.PERCENT_OFFSET + 0.08496094f );
    }
}
