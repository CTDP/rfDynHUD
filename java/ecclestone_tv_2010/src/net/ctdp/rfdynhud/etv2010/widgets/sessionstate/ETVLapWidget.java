package net.ctdp.rfdynhud.etv2010.widgets.sessionstate;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.EnumProperty;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.YellowFlagState;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.widgets._util.BoolValue;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.EnumValue;
import net.ctdp.rfdynhud.widgets._util.IntValue;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link ETVLapWidget} displays the current lap.
 * 
 * @author Marvin Froehlich
 */
public class ETVLapWidget extends Widget
{
    private final ColorProperty captionBackgroundColor = new ColorProperty( this, "captionBackgroundColor", ETVUtils.TV_STYLE_CAPTION_BACKGROUND_COLOR );
    private final ColorProperty captionColor = new ColorProperty( this, "captionColor", ETVUtils.TV_STYLE_CAPTION_FONT_COLOR );
    
    private static enum LapDisplayType
    {
        CURRENT_LAP,
        LAPS_DONE,
        ;
    }
    
    private final EnumProperty<LapDisplayType> lapDisplayType = new EnumProperty<LapDisplayType>( this, "lapDisplayType", LapDisplayType.CURRENT_LAP );
    
    private DrawnString captionString = null;
    private DrawnString lapString = null;
    
    private static final String CAPTION = "Lap";
    
    private final IntValue lap = new IntValue();
    private final EnumValue<YellowFlagState> yellowFlagState = new EnumValue<YellowFlagState>( YellowFlagState.NONE );
    private final BoolValue sectorYellowFlag = new BoolValue();
    
    private static final Alignment[] colAligns = new Alignment[] { Alignment.RIGHT, Alignment.CENTER, Alignment.RIGHT };
    private final int[] colWidths = new int[ 3 ];
    private static final int colPadding = 10;
    
    @Override
    public String getWidgetPackage()
    {
        return ( ETVUtils.WIDGET_PACKAGE );
    }
    
    @Override
    public void onSessionStarted( boolean isEditorMode, SessionType sessionType, LiveGameData gameData )
    {
        super.onSessionStarted( isEditorMode, sessionType, gameData );
        
        lap.reset();
        yellowFlagState.reset();
        sectorYellowFlag.reset();
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
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        yellowFlagState.update( scoringInfo.getYellowFlagState() );
        //sectorYellowFlag.update( scoringInfo.getSectorYellowFlag( scoringInfo.getPlayersVehicleScoringInfo().getSector() ) );
        sectorYellowFlag.update( false );
        
        boolean changed = false;
        if ( yellowFlagState.hasChanged() )
            changed = true;
        if ( sectorYellowFlag.hasChanged() )
            changed = true;
        
        return ( changed );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        texCanvas.setFont( getFont() );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D capBounds = metrics.getStringBounds( CAPTION, texCanvas );
        
        int dataAreaCenter = ETVUtils.getLabeledDataDataCenter( width, capBounds );
        int vMiddle = ETVUtils.getLabeledDataVMiddle( height, capBounds );
        
        captionString = new DrawnString( ETVUtils.TRIANGLE_WIDTH, vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        lapString = new DrawnString( dataAreaCenter, vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        lapString.getMinColWidths( new String[] { "00", "/", "00" }, colAligns, colPadding, texture, colWidths );
    }
    
    @Override
    protected void clearBackground( boolean isEditorMode, LiveGameData gameData, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        Color dataBgColor;
        if ( ( yellowFlagState.getValue() == YellowFlagState.NONE ) && !sectorYellowFlag.getValue() )
            dataBgColor = getBackgroundColor();
        else
            dataBgColor = Color.YELLOW;
        
        ETVUtils.drawLabeledDataBackground( offsetX, offsetY, width, height, CAPTION, getFont(), captionBackgroundColor.getColor(), dataBgColor, texture, true );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getSessionType().isRace() ? scoringInfo.getVehicleScoringInfo( 0 ) : scoringInfo.getPlayersVehicleScoringInfo();
        
        if ( needsCompleteRedraw )
        {
            captionString.draw( offsetX, offsetY, CAPTION, captionBackgroundColor.getColor(), texture );
        }
        
        if ( ( scoringInfo.getSessionType() == SessionType.RACE ) && ( scoringInfo.getGamePhase() == GamePhase.FORMATION_LAP ) )
            lap.update( 0 );
        else if ( lapDisplayType.getValue() == LapDisplayType.CURRENT_LAP )
            lap.update( vsi.getCurrentLap() );
        else if ( lapDisplayType.getValue() == LapDisplayType.LAPS_DONE )
            lap.update( vsi.getLapsCompleted() );
        
        if ( needsCompleteRedraw || ( clock1 && lap.hasChanged() ) )
        {
            Color bgColor = getBackgroundColor();
            Color color = getFontColor();
            if ( ( yellowFlagState.getValue() != YellowFlagState.NONE ) || sectorYellowFlag.getValue() )
            {
                bgColor = Color.YELLOW;
                color = Color.BLACK;
            }
            
            int maxLaps = scoringInfo.getMaxLaps();
            String maxLapsStr = ( maxLaps < 10000 ) ? String.valueOf( maxLaps ) : "--";
            
            lapString.drawColumns( offsetX, offsetY, new String[] { lap.getValueAsString(), "/", maxLapsStr }, colAligns, colPadding, colWidths, bgColor, color, texture );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( captionBackgroundColor, "The background color for the \"Lap\" caption." );
        writer.writeProperty( captionColor, "The font color for the \"Lap\" caption." );
        writer.writeProperty( lapDisplayType, "The way the laps are displayed. Valid values: CURRENT_LAP, LAPS_DONE." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( captionBackgroundColor.loadProperty( key, value ) );
        else if ( captionColor.loadProperty( key, value ) );
        else if ( lapDisplayType.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont )
    {
        super.getProperties( propsCont );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( captionBackgroundColor );
        propsCont.addProperty( captionColor );
        propsCont.addProperty( lapDisplayType );
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
    
    public ETVLapWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.12f, Size.PERCENT_OFFSET + 0.0254f );
        
        getBackgroundColorProperty().setValue( ETVUtils.TV_STYLE_DATA_BACKGROUND_COLOR );
        getFontColorProperty().setValue( ETVUtils.TV_STYLE_DATA_FONT_COLOR );
        getFontProperty().setValue( ETVUtils.TV_STYLE_FONT );
    }
}
