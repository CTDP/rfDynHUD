package net.ctdp.rfdynhud.etv2010.widgets.timing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.IntegerProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.LapState;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.values.ValidityTest;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link ETVTimingWidget} displays the current lap time.
 * 
 * @author Marvin Froehlich
 */
public class ETVTimingWidget extends Widget
{
    private final ColorProperty captionBackgroundColor = new ColorProperty( this, "captionBgColor", ETVUtils.ETV_STYLE_CAPTION_BACKGROUND_COLOR );
    private final ColorProperty captionBackgroundColor1st = new ColorProperty( this, "captionBgColor1st", ETVUtils.ETV_STYLE_CAPTION_BACKGROUND_COLOR_1ST );
    private final ColorProperty captionColor = new ColorProperty( this, "captionColor", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR_1ST );
    private final ColorProperty dataBackgroundColor1st = new ColorProperty( this, "dataBgColor1st", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR_1ST );
    private final ColorProperty dataBackgroundColorFaster = new ColorProperty( this, "dataBgColorFaster", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR_FASTER );
    private final ColorProperty dataBackgroundColorSlower = new ColorProperty( this, "dataBgColorSlower", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR_SLOWER );
    private final ColorProperty dataColorFaster = new ColorProperty( this, "dataColorFaster", ETVUtils.ETV_STYLE_DATA_FONT_COLOR_FASTER );
    private final ColorProperty dataColorSlower = new ColorProperty( this, "dataColorSlower", ETVUtils.ETV_STYLE_DATA_FONT_COLOR_SLOWER );
    
    private final IntegerProperty positionFontSize = new IntegerProperty( this, "positionFontSize", 200 );
    
    private final BooleanProperty alwaysVisible = new BooleanProperty( this, "alwaysVisible", false );
    
    private final IntegerProperty visibleTimeBeforeSector = new IntegerProperty( this, "visibleTimeBeforeSector", 7 );
    private final IntegerProperty visibleTimeAfterSector = new IntegerProperty( this, "visibleTimeAfterSector", 7 );
    
    private Font positionFont = null;
    
    private int rowHeight = 0;
    
    private int dataWidth = 0;
    private int bigPositionWidth = 0;
    private int smallPositionWidth = 0;
    
    private TextureImage2D cacheTexture = null;
    
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
    
    private Laptime referenceTime = null;
    private int referencePlace = 0;
    
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
            forceCompleteRedraw();
        }
    }
    
    private final Font getPositionFont()
    {
        if ( positionFont == null )
        {
            Font base = getFont();
            
            positionFont = base.deriveFont( base.getSize() * ( positionFontSize.getIntegerValue() / 100f ) );
        }
        
        return ( positionFont );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedColorValue( String name )
    {
        String result = super.getDefaultNamedColorValue( name );
        
        if ( result != null )
            return ( result );
        
        return ( ETVUtils.getDefaultNamedColorValue( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        String result = super.getDefaultNamedFontValue( name );
        
        if ( result != null )
            return ( result );
        
        return ( ETVUtils.getDefaultNamedFontValue( name ) );
    }
    
    @Override
    public String getWidgetPackage()
    {
        return ( ETVUtils.WIDGET_PACKAGE );
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
        
        referenceTime = null;
        referencePlace = 0;
        
        forceCompleteRedraw();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBoundInputStateChanged( boolean isEditorMode, InputAction action, boolean state, int modifierMask )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVisibility( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.updateVisibility( gameData, editorPresets );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        VehicleScoringInfo refVSI = scoringInfo.getFastestLapVSI();
        Laptime relTime = refVSI.getFastestLaptime();
        
        LapState ls = LapState.getLapState( scoringInfo, vsi, relTime, visibleTimeBeforeSector.getIntegerValue(), visibleTimeAfterSector.getIntegerValue(), true );
        
        lapState.update( ls );
        
        if ( ( ls != LapState.AFTER_SECTOR1_START ) && ( ( vsi.getStintLength() % 1.0f ) > 0.1f ) )
        {
            referenceTime = relTime;
            referencePlace = refVSI.getPlace();
        }
        
        if ( editorPresets != null )
        {
            referenceTime = new Laptime( 2, 28.733f, 29.649f, 26.36f, false, false, true );
            referencePlace = 1;
            
            setVisible( true );
            return;
        }
        
        if ( scoringInfo.getSessionType().isRace() )
        {
            setVisible( false );
            return;
        }
        
        if ( alwaysVisible.getBooleanValue() )
        {
            setVisible( true );
            return;
        }
        
        if ( relTime == null )
        {
            setVisible( ls == LapState.BEFORE_SECTOR3_END );
            return;
        }
        
        if ( ( ls == LapState.SOMEWHERE ) || ( ls == LapState.OUTLAP ) )
        {
            setVisible( false );
            return;
        }
        
        setVisible( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        return ( lapState.hasChanged() );
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
        cacheTexture = TextureImage2D.createOfflineTexture( width, height, true );
        
        updateRowHeight( height );
        
        Texture2DCanvas texCanvas = cacheTexture.getTextureCanvas();
        
        texCanvas.setFont( getPositionFont() );
        FontMetrics positionMetrics = texCanvas.getFontMetrics();
        
        Rectangle2D posBounds = positionMetrics.getStringBounds( "00", texCanvas );
        
        bigPositionWidth = (int)Math.round( posBounds.getWidth() );
        dataWidth = width - 6 * ETVUtils.TRIANGLE_WIDTH - bigPositionWidth - ETVUtils.TRIANGLE_WIDTH / 2;
        
        int vMiddle = ETVUtils.getLabeledDataVMiddle( rowHeight + ETVUtils.ITEM_GAP + rowHeight, posBounds );
        
        bigPositionString = new DrawnString( width - 2 * ETVUtils.TRIANGLE_WIDTH - bigPositionWidth / 2, 0 + vMiddle, Alignment.CENTER, false, getPositionFont(), isFontAntiAliased(), captionColor.getColor() );;
        
        texCanvas.setFont( getFont() );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D capBounds = metrics.getStringBounds( "00", texCanvas );
        
        smallPositionWidth = (int)Math.round( capBounds.getWidth() );
        
        vMiddle = ETVUtils.getLabeledDataVMiddle( rowHeight, capBounds );
        
        drivernameString = new DrawnString( 3 * ETVUtils.TRIANGLE_WIDTH + dataWidth, 0 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
        laptimeString = new DrawnString( 2 * ETVUtils.TRIANGLE_WIDTH + dataWidth, 1 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
        relPositionString = new DrawnString( 1 * ETVUtils.TRIANGLE_WIDTH + smallPositionWidth / 2, 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        relTimeString = new DrawnString( 1 * ETVUtils.TRIANGLE_WIDTH + dataWidth, 2 * ( rowHeight + ETVUtils.ITEM_GAP ) + vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        forceCompleteRedraw();
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        cacheTexture.clear( true, null );
        
        int dataWidth2 = ETVUtils.TRIANGLE_WIDTH + dataWidth + ETVUtils.TRIANGLE_WIDTH;
        
        Color capBgColor = captionBackgroundColor.getColor();
        if ( referencePlace == 1 )
            capBgColor = captionBackgroundColor1st.getColor();
        
        Color dataBgColor = getBackgroundColor();
        LapState ls = lapState.getValue();
        if ( editorPresets != null )
        {
            switch ( vsi.getSector() )
            {
                case 1:
                    if ( editorPresets.getLastLaptime() < referenceTime.getLapTime() )
                        dataBgColor = dataBackgroundColorFaster.getColor();
                    else
                        dataBgColor = dataBackgroundColorSlower.getColor();
                    break;
                case 2:
                    if ( editorPresets.getCurrentSector1Time() < referenceTime.getSector1() )
                        dataBgColor = dataBackgroundColorFaster.getColor();
                    else
                        dataBgColor = dataBackgroundColorSlower.getColor();
                    break;
                case 3:
                    if ( editorPresets.getCurrentSector2Time( true ) < referenceTime.getSector2( true ) )
                        dataBgColor = dataBackgroundColorFaster.getColor();
                    else
                        dataBgColor = dataBackgroundColorSlower.getColor();
                    break;
            }
        }
        else if ( referenceTime != null )
        {
            if ( ls.isBeforeSectorEnd() )
            {
                dataBgColor = dataBackgroundColor1st.getColor();
            }
            else if ( ls.isAfterSectorStart() )
            {
                switch ( vsi.getSector() )
                {
                    case 1:
                        if ( vsi.getLastLapTime() < referenceTime.getLapTime() )
                            dataBgColor = dataBackgroundColorFaster.getColor();
                        else
                            dataBgColor = dataBackgroundColorSlower.getColor();
                        break;
                    case 2:
                        if ( vsi.getCurrentSector1() < referenceTime.getSector1() )
                            dataBgColor = dataBackgroundColorFaster.getColor();
                        else
                            dataBgColor = dataBackgroundColorSlower.getColor();
                        break;
                    case 3:
                        if ( vsi.getCurrentSector2( true ) < referenceTime.getSector2( true ) )
                            dataBgColor = dataBackgroundColorFaster.getColor();
                        else
                            dataBgColor = dataBackgroundColorSlower.getColor();
                        break;
                }
            }
        }
        
        ETVUtils.drawDataBackground( 2 * ETVUtils.TRIANGLE_WIDTH, 0 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidth2, rowHeight, getBackgroundColor(), cacheTexture, false );
        ETVUtils.drawDataBackground( 1 * ETVUtils.TRIANGLE_WIDTH, 1 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidth2, rowHeight, getBackgroundColor(), cacheTexture, false );
        ETVUtils.drawLabeledDataBackground( 0 * ETVUtils.TRIANGLE_WIDTH, 2 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidth2, rowHeight, "00", getFont(), capBgColor, dataBgColor, cacheTexture, false );
        
        capBgColor = captionBackgroundColor.getColor();
        if ( vsi.getPlace() == 1 )
            capBgColor = captionBackgroundColor1st.getColor();
        
        ETVUtils.drawDataBackground( width - 4 * ETVUtils.TRIANGLE_WIDTH - bigPositionWidth, 0, 4 * ETVUtils.TRIANGLE_WIDTH + bigPositionWidth, 2 * rowHeight + ETVUtils.ITEM_GAP, 2, capBgColor, cacheTexture, false );
        
        drivernameString.resetClearRect();
        drivernameString.draw( 0, 0, vsi.getDriverNameShort(), Color.RED, cacheTexture );
        
        texture.clear( cacheTexture, offsetX, offsetY, true, null );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        ownPlace.update( vsi.getPlace() );
        
        if ( needsCompleteRedraw || ( clock1 && ownPlace.hasChanged() ) )
        {
            bigPositionString.draw( offsetX, offsetY, ownPlace.getValueAsString(), cacheTexture, offsetX, offsetY, captionColor.getColor(), texture );
        }
        
        LapState ls = lapState.getValue();
        if ( editorPresets != null )
        {
            switch ( vsi.getSector() )
            {
                case 1:
                    ownLaptime.update( editorPresets.getLastLaptime() );
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
                laptimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( ownLaptime.getValue(), false, false, true ), cacheTexture, offsetX, offsetY, texture );
            else
                laptimeString.draw( offsetX, offsetY, "", cacheTexture, offsetX, offsetY, texture );
        }
        
        relPlace.update( referencePlace );
        
        if ( needsCompleteRedraw || ( clock1 && relPlace.hasChanged() ) )
        {
            relPositionString.draw( offsetX, offsetY, relPlace.getValueAsString(), cacheTexture, offsetX, offsetY, texture );
        }
        
        if ( editorPresets != null )
        {
            switch ( vsi.getSector() )
            {
                case 1:
                    relLaptime.update( editorPresets.getLastLaptime() - referenceTime.getLapTime() );
                    break;
                case 2:
                    relLaptime.update( editorPresets.getCurrentSector1Time() - referenceTime.getSector1() );
                    break;
                case 3:
                    relLaptime.update( editorPresets.getCurrentSector2Time( true ) - referenceTime.getSector2( true ) );
                    break;
            }
        }
        else if ( referenceTime == null )
        {
            relLaptime.update( relLaptime.getResetValue() );
        }
        else if ( ( ls == LapState.SOMEWHERE ) || ls.isBeforeSectorEnd() )
        {
            switch ( vsi.getSector() )
            {
                case 1:
                    relLaptime.update( referenceTime.getSector1() );
                    break;
                case 2:
                    relLaptime.update( referenceTime.getSector2( true ) );
                    break;
                case 3:
                    relLaptime.update( referenceTime.getLapTime() );
                    break;
            }
        }
        else if ( ls.isAfterSectorStart() )
        {
            switch ( vsi.getSector() )
            {
                case 1:
                    relLaptime.update( vsi.getLastLapTime() - referenceTime.getLapTime() );
                    break;
                case 2:
                    relLaptime.update( vsi.getCurrentSector1() - referenceTime.getSector1() );
                    break;
                case 3:
                    relLaptime.update( vsi.getCurrentSector2( true ) - referenceTime.getSector2( true ) );
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
                {
                    if ( relLaptime.getValue() < 0f )
                        relTimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsGapString( relLaptime.getValue() ), cacheTexture, offsetX, offsetY, dataColorFaster.getColor(), texture );
                    else
                        relTimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsGapString( relLaptime.getValue() ), cacheTexture, offsetX, offsetY, dataColorSlower.getColor(), texture );
                }
                else if ( ( ls == LapState.SOMEWHERE ) || ls.isBeforeSectorEnd() )
                {
                    relTimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( relLaptime.getValue(), false, false, true ), cacheTexture, offsetX, offsetY, texture );
                }
            }
            else
            {
                relTimeString.draw( offsetX, offsetY, "", cacheTexture, offsetX, offsetY, texture );
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
        
        writer.writeProperty( captionBackgroundColor, "The background color for the \"Position\" caption." );
        writer.writeProperty( captionBackgroundColor1st, "The background color for the \"Position\" caption for first place." );
        writer.writeProperty( captionColor, "The font color for the \"Position\" caption." );
        writer.writeProperty( dataBackgroundColor1st, "The background color for the data area, for first place." );
        writer.writeProperty( dataBackgroundColorFaster, "The background color for the data area, if a negative gap is displayed." );
        writer.writeProperty( dataBackgroundColorSlower, "The background color for the data area, if a positive gap is displayed." );
        writer.writeProperty( dataColorFaster, "The font color for the data area, if a negative gap is displayed." );
        writer.writeProperty( dataColorSlower, "The font color for the data area, if a positive gap is displayed." );
        
        writer.writeProperty( positionFontSize, "Font size for the position in percent relative to the normal font size." );
        
        writer.writeProperty( alwaysVisible, "Always visible? If true, visibleTimeBeforeSector and visibleTimeAfterSector are ignored." );
        writer.writeProperty( visibleTimeBeforeSector, "The Widget is visible for the given amount of seconds before the relative sector time is reached." );
        writer.writeProperty( visibleTimeAfterSector, "The Widget is visible for the given amount after a sector was finished." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( captionBackgroundColor.loadProperty( key, value ) );
        else if ( captionBackgroundColor1st.loadProperty( key, value ) );
        else if ( captionColor.loadProperty( key, value ) );
        else if ( dataBackgroundColor1st.loadProperty( key, value ) );
        else if ( dataBackgroundColorFaster.loadProperty( key, value ) );
        else if ( dataBackgroundColorSlower.loadProperty( key, value ) );
        else if ( dataColorFaster.loadProperty( key, value ) );
        else if ( dataColorSlower.loadProperty( key, value ) );
        else if ( positionFontSize.loadProperty( key, value ) );
        else if ( alwaysVisible.loadProperty( key, value ) );
        else if ( visibleTimeBeforeSector.loadProperty( key, value ) );
        else if ( visibleTimeAfterSector.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont )
    {
        super.getProperties( propsCont );
        
        propsCont.addGroup( "Colors and Fonts" );
        
        propsCont.addProperty( captionBackgroundColor );
        propsCont.addProperty( captionBackgroundColor1st );
        propsCont.addProperty( captionColor );
        propsCont.addProperty( dataBackgroundColor1st );
        propsCont.addProperty( dataBackgroundColorFaster );
        propsCont.addProperty( dataBackgroundColorSlower );
        propsCont.addProperty( dataColorFaster );
        propsCont.addProperty( dataColorSlower );
        propsCont.addProperty( positionFontSize );
        
        propsCont.addGroup( "Visiblity" );
        
        propsCont.addProperty( alwaysVisible );
        propsCont.addProperty( visibleTimeBeforeSector );
        propsCont.addProperty( visibleTimeAfterSector );
    }
    
    /*
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    */
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    public ETVTimingWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.2f, Size.PERCENT_OFFSET + 0.08496094f );
        
        getBackgroundColorProperty().setValue( ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR );
        getFontColorProperty().setValue( ETVUtils.ETV_STYLE_DATA_FONT_COLOR );
        getFontProperty().setValue( ETVUtils.ETV_STYLE_FONT );
    }
}
