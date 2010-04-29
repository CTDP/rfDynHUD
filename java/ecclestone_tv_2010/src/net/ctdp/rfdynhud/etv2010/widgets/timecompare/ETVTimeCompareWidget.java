package net.ctdp.rfdynhud.etv2010.widgets.timecompare;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.etv2010.widgets._base.ETVTimingWidgetBase;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
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
    
    private final IntValue lap = new IntValue();
    
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
        
        lap.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        hideTime = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVisibility( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.updateVisibility( gameData, editorPresets );
        
        if ( editorPresets != null )
            return;
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        if ( !scoringInfo.getSessionType().isRace() || !vsi.getFinishStatus().isNone() )
        {
            setVisible( false );
            return;
        }
        
        lap.update( vsi.getCurrentLap() );
        
        if ( lap.hasChanged() )
        {
            if ( lap.getValue() < 4 )
            {
                setVisible( false );
            }
            else if ( ( ( lap.getValue() - 1 ) % displayEveryXLaps.getIntValue() ) == 0 )
            {
                boolean b = true;
                
                if ( scoringInfo.getNumVehicles() == 1 )
                {
                    b = false;
                }
                else if ( vsi.getPlace() == 1 )
                {
                    VehicleScoringInfo vsi2 = scoringInfo.getVehicleScoringInfo( 1 ); // 2nd
                    b = vsi2.getFinishStatus().isNone() && ( vsi2.getLapsCompleted() >= 3 );
                }
                else if ( vsi.getPlace() == scoringInfo.getNumVehicles() )
                {
                    VehicleScoringInfo vsi2 = scoringInfo.getVehicleScoringInfo( scoringInfo.getNumVehicles() - 2 ); // next in front
                    b = vsi2.getFinishStatus().isNone();
                }
                else
                {
                    // There are at least 3 vehicles in the race.
                    
                    VehicleScoringInfo vsi2 = scoringInfo.getVehicleScoringInfo( vsi.getPlace() - 2 ); // next in front
                    VehicleScoringInfo vsi3 = scoringInfo.getVehicleScoringInfo( vsi.getPlace() ); // next behind
                    b = ( ( vsi2.getFinishStatus().isNone() && ( vsi2.getLapsCompleted() >= 3 ) ) || ( vsi3.getFinishStatus().isNone() && ( vsi3.getLapsCompleted() >= 3 ) ) );
                }
                
                setVisible( b );
                
                if ( b )
                {
                    hideTime = scoringInfo.getSessionTime() + visibleTime.getFloatValue();
                    forceCompleteRedraw();
                }
            }
            else
            {
                setVisible( false );
                hideTime = -1f;
            }
        }
        else if ( scoringInfo.getSessionTime() < hideTime )
        {
            setVisible( true );
            forceCompleteRedraw();
        }
        else
        {
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
        
        namesWidth = Math.round( width * 2f / 5f );
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
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        VehicleScoringInfo relVSI;
        //if ( scoringInfo.getNumVehicles() == 1 ) // Because of updateVisibility() this is impossible here.
        if ( vsi.getPlace() == 1 )
        {
            // Because of updateVisibility() next behind must be valid.
            relVSI = scoringInfo.getVehicleScoringInfo( 1 );
        }
        else if ( vsi.getPlace() == scoringInfo.getNumVehicles() )
        {
            // Because of updateVisibility() next in front must be valid.
            relVSI = scoringInfo.getVehicleScoringInfo( scoringInfo.getNumVehicles() - 2 );
        }
        else
        {
            // There are at least 3 vehicles in the race.
            
            VehicleScoringInfo vsi2 = scoringInfo.getVehicleScoringInfo( vsi.getPlace() - 2 ); // next in front
            if ( vsi2.getFinishStatus().isNone() && ( vsi2.getLapsCompleted() >= 3 ) )
            {
                VehicleScoringInfo vsi3 = scoringInfo.getVehicleScoringInfo( vsi.getPlace() ); // next behind
                if ( vsi2.getFinishStatus().isNone() && ( vsi2.getLapsCompleted() >= 3 ) )
                {
                    if ( preferNextInFront.getBooleanValue() )
                    {
                        relVSI = vsi2;
                    }
                    else
                    {
                        float gapToNextInFront = Math.abs( vsi.getTimeBehindNextInFront() );
                        float gapToNextBehind = Math.abs( vsi3.getTimeBehindNextInFront() );
                        
                        if ( gapToNextInFront < gapToNextBehind )
                            relVSI = vsi2;
                        else
                            relVSI = vsi3;
                    }
                }
                else
                {
                    relVSI = vsi2;
                }
            }
            else
            {
                // Because of updateVisibility() next behind must be valid.
                
                relVSI = scoringInfo.getVehicleScoringInfo( vsi.getPlace() ); // next behind
            }
        }
        
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
        lapCaptionString1.draw( offsetX, offsetY, "Lap " + ( vsi.getCurrentLap() - 3 ), null, texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 2 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 - ETVUtils.TRIANGLE_WIDTH + 1 * dataWidthTimes2, offsetY, dataWidthTimes2, rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString2.resetClearRect();
        lapCaptionString2.draw( offsetX, offsetY, "Lap " + ( vsi.getCurrentLap() - 2 ), null, texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 3 * ETVUtils.ITEM_GAP + ETVUtils.TRIANGLE_WIDTH / 2 - 2 * ETVUtils.TRIANGLE_WIDTH + 2 * dataWidthTimes2, offsetY, dataWidthTimes2, rowHeight, captionBackgroundColor.getColor(), texture, false );
        lapCaptionString3.resetClearRect();
        lapCaptionString3.draw( offsetX, offsetY, "Lap " + ( vsi.getCurrentLap() - 1 ), null, texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 + 0 * dataWidthTimes2, offsetY + rowHeight + ETVUtils.ITEM_GAP, dataWidthTimes2, rowHeight, getBackgroundColor(), texture, false );
        laptimeString1.resetClearRect();
        laptimeString1.draw( offsetX, offsetY, "1:24.567", null, texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 2 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 - ETVUtils.TRIANGLE_WIDTH + 1 * dataWidthTimes2, offsetY + rowHeight + ETVUtils.ITEM_GAP, dataWidthTimes2, rowHeight, getBackgroundColor(), texture, false );
        laptimeString2.resetClearRect();
        laptimeString2.draw( offsetX, offsetY, "1:25.678", null, texture );
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 3 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH / 2 - 2 * ETVUtils.TRIANGLE_WIDTH + 2 * dataWidthTimes2, offsetY + rowHeight + ETVUtils.ITEM_GAP, dataWidthTimes2, rowHeight, getBackgroundColor(), texture, false );
        laptimeString3.resetClearRect();
        laptimeString3.draw( offsetX, offsetY, "1:26.789", null, texture );
        
        float gap1, gap2, gap3;
        if ( editorPresets != null )
        {
            gap1 = -1.234f;
            gap2 = +0.123f;
            gap3 = -2.345f;
        }
        else
        {
            gap1 = relVSI.getLaptime( relVSI.getCurrentLap() - 3 ).getLapTime() - vsi.getLaptime( vsi.getCurrentLap() - 3 ).getLapTime();
            gap2 = relVSI.getLaptime( relVSI.getCurrentLap() - 2 ).getLapTime() - vsi.getLaptime( vsi.getCurrentLap() - 2 ).getLapTime();
            gap3 = relVSI.getLaptime( relVSI.getCurrentLap() - 1 ).getLapTime() - vsi.getLaptime( vsi.getCurrentLap() - 1 ).getLapTime();
        }
        
        dataBgColor = dataBackgroundColorFaster.getColor();
        Color dataColor = dataColorFaster.getColor();
        if ( gap1 < 0f )
        {
            dataBgColor = dataBackgroundColorSlower.getColor();
            dataColor = dataColorSlower.getColor();
        }
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH * 3 / 2 + 0 * dataWidthTimes2, offsetY + 2 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidthTimes2, rowHeight, dataBgColor, texture, false );
        gapString1.resetClearRect();
        gapString1.draw( offsetX, offsetY, TimingUtil.getTimeAsGapString( gap1 ), null, dataColor, texture );
        
        dataBgColor = dataBackgroundColorFaster.getColor();
        dataColor = dataColorFaster.getColor();
        if ( gap2 < 0f )
        {
            dataBgColor = dataBackgroundColorSlower.getColor();
            dataColor = dataColorSlower.getColor();
        }
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 2 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH * 3 / 2 - ETVUtils.TRIANGLE_WIDTH + 1 * dataWidthTimes2, offsetY + 2 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidthTimes2, rowHeight, dataBgColor, texture, false );
        gapString2.resetClearRect();
        gapString2.draw( offsetX, offsetY, TimingUtil.getTimeAsGapString( gap2 ), null, dataColor, texture );
        
        dataBgColor = dataBackgroundColorFaster.getColor();
        dataColor = dataColorFaster.getColor();
        if ( gap2 < 0f )
        {
            dataBgColor = dataBackgroundColorSlower.getColor();
            dataColor = dataColorSlower.getColor();
        }
        
        ETVUtils.drawDataBackground( offsetX + namesWidth + 3 * ETVUtils.ITEM_GAP - ETVUtils.TRIANGLE_WIDTH * 3 / 2 - 2 * ETVUtils.TRIANGLE_WIDTH + 2 * dataWidthTimes2, offsetY + 2 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidthTimes2, rowHeight, dataBgColor, texture, false );
        gapString3.resetClearRect();
        gapString3.draw( offsetX, offsetY, TimingUtil.getTimeAsGapString( gap3 ), null, dataColor, texture );
        
        
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
        
        writer.writeProperty( preferNextInFront, "Whether to prefer next in front, even if next behind is closer." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( preferNextInFront.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( preferNextInFront );
    }
    
    public ETVTimeCompareWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.50f, Size.PERCENT_OFFSET + 0.08496094f );
    }
}
