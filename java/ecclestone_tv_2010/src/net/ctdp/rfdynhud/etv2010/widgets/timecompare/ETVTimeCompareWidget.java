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
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link ETVTimeCompareWidget} displays lap time gaps in race sessions.
 * 
 * @author Marvin Froehlich
 */
public class ETVTimeCompareWidget extends ETVTimingWidgetBase
{
    private static final int NUM_DISPLAYED_LAPS = 3;
    
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
            forceCompleteRedraw();
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
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onSessionStarted( sessionType, gameData, editorPresets );
        
        laps.reset( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        waitingForNextBehind = false;
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
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        
        if ( editorPresets != null )
        {
            relVSI = vsi.getNextInFront( getUseClassScoring() );
            waitingForNextBehind = false;
            return;
        }
        
        relVSI = null;
        
        if ( !scoringInfo.getSessionType().isRace() || !vsi.getFinishStatus().isNone() )
        {
            setUserVisible2( false );
            return;
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
                setUserVisible2( false );
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
                        setUserVisible2( false );
                    }
                    else if ( vsi_nb.getLapsCompleted() + vsi_nb.getLapsBehindNextInFront( getUseClassScoring() ) < laps.getValue() )
                    {
                        laps.reset( true );
                        setUserVisible2( false );
                    }
                    else
                    {
                        waitingForNextBehind = false;
                        relVSI = vsi_nb;
                        setUserVisible2( true );
                        hideTime = scoringInfo.getSessionTime() + visibleTime.getFloatValue();
                        forceCompleteRedraw();
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
                    
                    setUserVisible2( b );
                    
                    if ( b )
                    {
                        hideTime = scoringInfo.getSessionTime() + visibleTime.getFloatValue();
                        forceCompleteRedraw();
                    }
                }
            }
            else
            {
                setUserVisible2( false );
                hideTime = -1f;
            }
        }
        else if ( scoringInfo.getSessionTime() < hideTime )
        {
            setUserVisible2( true );
        }
        else
        {
            setUserVisible2( false );
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
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
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
        
        positionString1 = dsf.newDrawnString( "positionString1", 2 * ETVUtils.TRIANGLE_WIDTH + positionWidth / 2, 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        positionString2 = dsf.newDrawnString( "positionString2", 1 * ETVUtils.TRIANGLE_WIDTH + positionWidth / 2, 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        drivernameString1 = dsf.newDrawnString( "drivernameString1", 3 * ETVUtils.TRIANGLE_WIDTH + positionWidth, 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        drivernameString2 = dsf.newDrawnString( "drivernameString2", 2 * ETVUtils.TRIANGLE_WIDTH + positionWidth, 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        lapCaptionString1 = dsf.newDrawnString( "lapCaptionString1", namesWidth + 1 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH * 3 / 2 + dataWidthTimes / 2 + 0 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 0 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        lapCaptionString2 = dsf.newDrawnString( "lapCaptionString2", namesWidth + 2 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH * 3 / 2 + dataWidthTimes / 2 + 1 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 0 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        lapCaptionString3 = dsf.newDrawnString( "lapCaptionString3", namesWidth + 3 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH * 3 / 2 + dataWidthTimes / 2 + 2 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 0 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        laptimeString1 = dsf.newDrawnString( "laptimeString1", namesWidth + 1 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 0 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        laptimeString2 = dsf.newDrawnString( "laptimeString2", namesWidth + 2 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 1 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        laptimeString3 = dsf.newDrawnString( "laptimeString3", namesWidth + 3 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 2 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        gapString1 = dsf.newDrawnString( "gapString1", namesWidth + 1 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 0 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        gapString2 = dsf.newDrawnString( "gapString2", namesWidth + 2 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 1 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        gapString3 = dsf.newDrawnString( "gapString3", namesWidth + 3 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 + dataWidthTimes / 2 + 2 * ( ETVUtils.TRIANGLE_WIDTH + dataWidthTimes ), 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
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
        
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        
        texture.clear( offsetX, offsetY, width, height, true, null );
        
        //int positionWidth2 = ETVUtils.TRIANGLE_WIDTH + positionWidth + ETVUtils.TRIANGLE_WIDTH;
        //int dataWidthNames2 = ETVUtils.TRIANGLE_WIDTH + dataWidthNames + ETVUtils.TRIANGLE_WIDTH;
        int dataWidthTimes2 = ETVUtils.TRIANGLE_WIDTH + dataWidthTimes + ETVUtils.TRIANGLE_WIDTH;
        
        Color captionBgColor = captionBackgroundColor.getColor();
        Color dataBgColor = getBackgroundColor();
        if ( vsi.getPlace( getUseClassScoring() ) == 1 )
            captionBgColor = captionBackgroundColor1st.getColor();
        
        ETVUtils.drawLabeledDataBackground( offsetX + ETVUtils.TRIANGLE_WIDTH, offsetY + 1 * ( rowHeight + ETVUtils.ITEM_GAP ), namesWidth - ETVUtils.TRIANGLE_WIDTH / 2, rowHeight, "00", getFont(), captionBgColor, dataBgColor, texture, false );
        positionString1.draw( offsetX, offsetY, String.valueOf( vsi.getPlace( getUseClassScoring() ) ), texture );
        drivernameString1.draw( offsetX, offsetY, vsi.getDriverNameShort(), texture );
        
        captionBgColor = captionBackgroundColor.getColor();
        dataBgColor = getBackgroundColor();
        if ( vsi.getPlace( getUseClassScoring() ) == 1 )
            captionBgColor = captionBackgroundColor1st.getColor();
        
        ETVUtils.drawLabeledDataBackground( offsetX, offsetY + 2 * ( rowHeight + ETVUtils.ITEM_GAP ), namesWidth - ETVUtils.TRIANGLE_WIDTH / 2, rowHeight, "00", getFont(), captionBgColor, dataBgColor, texture, false );
        positionString2.draw( offsetX, offsetY, String.valueOf( relVSI.getPlace( getUseClassScoring() ) ), texture );
        drivernameString2.draw( offsetX, offsetY, relVSI.getDriverNameShort(), texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 + 0 * dataWidthTimes2, offsetY, dataWidthTimes2, rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString1.draw( offsetX, offsetY, Loc.caption_lap + " " + ( vsi.getLapsCompleted() - 2 ), texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 2 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 - ETVUtils.TRIANGLE_WIDTH + 1 * dataWidthTimes2, offsetY, dataWidthTimes2, rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString2.draw( offsetX, offsetY, Loc.caption_lap + " " + ( vsi.getLapsCompleted() - 1 ), texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 3 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 - 2 * ETVUtils.TRIANGLE_WIDTH + 2 * dataWidthTimes2, offsetY, dataWidthTimes2, rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString3.draw( offsetX, offsetY, Loc.caption_lap + " " + ( vsi.getLapsCompleted() - 0 ), texture );
        
        float laptime1 = isEditorMode ? 84.567f : getLaptime( vsi, vsi.getLapsCompleted() - 2 );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 + 0 * dataWidthTimes2, offsetY + rowHeight + ETVUtils.ITEM_GAP, dataWidthTimes2, rowHeight, getBackgroundColor(), texture, false );
        if ( laptime1 > 0f )
        {
            laptimeString1.draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString( laptime1 ), texture );
        }
        
        float laptime2 = isEditorMode ? editorPresets.getLastLapTime() : getLaptime( vsi, vsi.getLapsCompleted() - 1 );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 2 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 - ETVUtils.TRIANGLE_WIDTH + 1 * dataWidthTimes2, offsetY + rowHeight + ETVUtils.ITEM_GAP, dataWidthTimes2, rowHeight, getBackgroundColor(), texture, false );
        if ( laptime2 > 0f )
        {
            laptimeString2.draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString( laptime2 ), texture );
        }
        
        float laptime3 = isEditorMode ? editorPresets.getCurrentLapTime() : getLaptime( vsi, vsi.getLapsCompleted() - 0 );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 3 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 - 2 * ETVUtils.TRIANGLE_WIDTH + 2 * dataWidthTimes2, offsetY + rowHeight + ETVUtils.ITEM_GAP, dataWidthTimes2, rowHeight, getBackgroundColor(), texture, false );
        if ( laptime3 > 0f )
        {
            laptimeString3.draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString( laptime3 ), texture );
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
        super( name, 40.7f, 8.496094f );
    }
}
