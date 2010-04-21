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
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.IntegerProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.Size;
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
    private final ColorProperty captionColor = new ColorProperty( this, "captionColor", ETVUtils.ETV_STYLE_CAPTION_FONT_COLOR );
    
    private final IntegerProperty positionFontSize = new IntegerProperty( this, "positionFontSize", 200 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            positionFont = null;
        }
    };
    
    private final BooleanProperty alwaysVisible = new BooleanProperty( this, "alwaysVisible", false );
    
    private final IntegerProperty visibleTimeBeforeSector = new IntegerProperty( this, "visibleTimeBeforeSector", 10 );
    private final IntegerProperty visibleTimeAfterSector = new IntegerProperty( this, "visibleTimeAfterSector", 10 );
    
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
    private final FloatValue relLaptime = new FloatValue();
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFontChanged( FontProperty property, String oldValue, String newValue )
    {
        super.onFontChanged( property, oldValue, newValue );
        
        if ( property == getFontProperty() )
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
    public void onSessionStarted( boolean isEditorMode, SessionType sessionType, LiveGameData gameData )
    {
        super.onSessionStarted( isEditorMode, sessionType, gameData );
        
        ownPlace.reset();
        ownLaptime.reset();
        relPlace.reset();
        relLaptime.reset();
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
    public void updateVisibility( EditorPresets editorPresets, LiveGameData gameData )
    {
        if ( alwaysVisible.getBooleanValue() )
        {
            setVisible( true );
            return;
        }
        
        super.updateVisibility( editorPresets, gameData );
        
        if ( editorPresets != null )
        {
            setVisible( true );
            return;
        }
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( scoringInfo.getSessionType().isRace() )
        {
            setVisible( false );
            return;
        }
        
        Laptime relTime = scoringInfo.getFastestLaptime();
        
        if ( relTime == null )
        {
            setVisible( true );
            return;
        }
        
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        short sector = vsi.getSector();
        
        if ( sector == 1 )
        {
            Laptime laptime = vsi.getLaptime( vsi.getCurrentLap() - 1 );
            
            if ( ( laptime == null ) || laptime.isInlap() )
            {
                setVisible( false );
            }
            else
            {
                if ( scoringInfo.getSessionTime() - vsi.getLapStartTime() < visibleTimeBeforeSector.getIntegerValue() )
                {
                    setVisible( true );
                }
                else
                {
                    if ( vsi.getPlace() == 1 )
                    {
                        relTime = scoringInfo.getSecondFastestLapVSI().getFastestLaptime();
                        
                        if ( relTime == null )
                        {
                            setVisible( true );
                            return;
                        }
                    }
                    
                    setVisible( relTime.getSector1() - laptime.getSector1() < visibleTimeAfterSector.getIntegerValue() );
                }
            }
        }
        else
        {
            Laptime laptime = vsi.getLaptime( vsi.getCurrentLap() );
            
            if ( laptime == null )
            {
                setVisible( false );
            }
            else
            {
                if ( sector == 2 )
                    setVisible( relTime.getSector2( true ) - laptime.getSector2( true ) < 10.0f );
                else if ( sector == 3 )
                    setVisible( relTime.getLapTime() - laptime.getLapTime() < 10.0f );
                else
                    setVisible( false );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
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
    protected void clearBackground( boolean isEditorMode, LiveGameData gameData, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        VehicleScoringInfo relVSI = scoringInfo.getVehicleScoringInfo( 0 );
        
        cacheTexture.clear( true, null );
        
        int dataWidth2 = ETVUtils.TRIANGLE_WIDTH + dataWidth + ETVUtils.TRIANGLE_WIDTH;
        
        Color capBgColor = captionBackgroundColor.getColor();
        if ( relVSI.getPlace() == 1 )
            capBgColor = captionBackgroundColor1st.getColor();
        
        ETVUtils.drawDataBackground( 2 * ETVUtils.TRIANGLE_WIDTH, 0 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidth2, rowHeight, getBackgroundColor(), cacheTexture, false );
        ETVUtils.drawDataBackground( 1 * ETVUtils.TRIANGLE_WIDTH, 1 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidth2, rowHeight, getBackgroundColor(), cacheTexture, false );
        ETVUtils.drawLabeledDataBackground( 0 * ETVUtils.TRIANGLE_WIDTH, 2 * ( rowHeight + ETVUtils.ITEM_GAP ), dataWidth2, rowHeight, "00", getFont(), capBgColor, getBackgroundColor(), cacheTexture, false );
        
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
        VehicleScoringInfo relVSI = scoringInfo.getVehicleScoringInfo( 0 );
        
        ownPlace.update( vsi.getPlace() );
        
        if ( needsCompleteRedraw || ( clock1 && ownPlace.hasChanged() ) )
        {
            bigPositionString.draw( offsetX, offsetY, ownPlace.getValueAsString(), cacheTexture, offsetX, offsetY, captionColor.getColor(), texture );
        }
        
        ownLaptime.update( scoringInfo.getSessionTime() - vsi.getLapStartTime() );
        
        if ( needsCompleteRedraw || ( clock1 && ownLaptime.hasChanged() ) )
        {
            if ( ownLaptime.isValid() )
                laptimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( ownLaptime.getValue(), false, true, true ), cacheTexture, offsetX, offsetY, texture );
            else
                laptimeString.draw( offsetX, offsetY, "", cacheTexture, offsetX, offsetY, texture );
        }
        
        relPlace.update( relVSI.getPlace() );
        
        if ( needsCompleteRedraw || ( clock1 && relPlace.hasChanged() ) )
        {
            relPositionString.draw( offsetX, offsetY, relPlace.getValueAsString(), cacheTexture, offsetX, offsetY, texture );
        }
        
        Laptime laptime = relVSI.getFastestLaptime();
        if ( laptime == null )
            relLaptime.update( -1f );
        else
            relLaptime.update( laptime.getLapTime() );
        
        if ( editorPresets != null )
            relLaptime.update( 90.0f );
        
        if ( needsCompleteRedraw || ( clock1 && relLaptime.hasChanged() ) )
        {
            if ( relLaptime.isValid() )
                relTimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( relLaptime.getValue(), false, false, true ), cacheTexture, offsetX, offsetY, texture );
            else
                relTimeString.draw( offsetX, offsetY, "", cacheTexture, offsetX, offsetY, texture );
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
