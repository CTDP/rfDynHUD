package net.ctdp.rfdynhud.tvstyle2010.widgets.lap;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.openmali.types.twodee.Rect2i;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.EnumProperty;
import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.YellowFlagState;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.EnumValue;
import net.ctdp.rfdynhud.widgets._util.IntValue;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link LapWidget} displays the current lap.
 * 
 * @author Marvin Froehlich
 */
public class LapWidget extends Widget
{
    private final ColorProperty captionBackgroundColor = new ColorProperty( this, "captionBackgroundColor", "#787878" );
    private final ColorProperty captionColor = new ColorProperty( this, "captionColor", "#FFFFFF" );
    
    private static enum LapDisplayType
    {
        CURRENT_LAP,
        LAPS_DONE,
        ;
    }
    
    private final EnumProperty<LapDisplayType> lapDisplayType = new EnumProperty<LapDisplayType>( this, "lapDisplayType", LapDisplayType.CURRENT_LAP );
    
    private static enum LapDriver
    {
        LEADER,
        ME,
        ;
    }
    
    private final EnumProperty<LapDriver> lapDriver = new EnumProperty<LapDriver>( this, "lapDriver", LapDriver.LEADER );
    
    private DrawnString captionString = null;
    private DrawnString lapString = null;
    
    private static final String CAPTION = "Lap";
    
    private final IntValue lap = new IntValue();
    private final EnumValue<YellowFlagState> yellowFlagState = new EnumValue<YellowFlagState>( YellowFlagState.NONE );
    
    private static final Alignment[] colAligns = new Alignment[] { Alignment.RIGHT, Alignment.CENTER, Alignment.RIGHT };
    private final int[] colWidths = new int[ 3 ];
    private static final int colPadding = 10;
    
    private static final int TRIANGLE_WIDTH = 15;
    
    @Override
    public String getWidgetPackage()
    {
        return ( "CTDP/TV-Style 2010" );
    }
    
    @Override
    public void onSessionStarted( boolean isEditorMode, SessionType sessionType, LiveGameData gameData )
    {
        super.onSessionStarted( isEditorMode, sessionType, gameData );
        
        lap.reset();
        yellowFlagState.reset();
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
        if ( yellowFlagState.hasChanged() )
        {
            return ( true );
        }
        
        return ( false );
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
        
        int capWidth = (int)Math.ceil( capBounds.getWidth() );
        int capHeight = (int)Math.ceil( capBounds.getHeight() );
        int vMiddle = ( height - capHeight ) / 2;
        int lapAreaWidth = width - 3 * TRIANGLE_WIDTH - capWidth;
        int dataAreaCenter = width - TRIANGLE_WIDTH - lapAreaWidth / 2;
        
        captionString = new DrawnString( TRIANGLE_WIDTH, vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        lapString = new DrawnString( dataAreaCenter, vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        lapString.getMinColWidths( new String[] { "00", "/", "00" }, colAligns, colPadding, texture, colWidths );
    }
    
    @Override
    protected void clearBackground( boolean isEditorMode, LiveGameData gameData, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        //super.clearBackground( isEditorMode, gameData, texture, offsetX, offsetY, width, height );
        texture.clear( offsetX, offsetY, width, height, true, null );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        texCanvas.setFont( getFont() );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D capBounds = metrics.getStringBounds( CAPTION, texCanvas );
        
        texCanvas.setColor( captionBackgroundColor.getColor() );
        
        final boolean aaTrian = true;
        
        texCanvas.setAntialiazingEnabled( aaTrian );
        
        int[] xPoints = new int[] { offsetX + 0, offsetX + TRIANGLE_WIDTH, offsetX + TRIANGLE_WIDTH, offsetX + 0 };
        int[] yPoints = new int[] { offsetY + height, offsetY + height, offsetY + 0, offsetY + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
        
        texCanvas.setAntialiazingEnabled( false );
        
        int capWidth = (int)Math.ceil( capBounds.getWidth() );
        Rect2i rect = new Rect2i( offsetX + TRIANGLE_WIDTH, offsetY + 0, capWidth, offsetY + height );
        
        texCanvas.fillRect( rect );
        //texCanvas.drawRect( rect );
        
        texCanvas.setAntialiazingEnabled( aaTrian );
        
        xPoints = new int[] { offsetX + TRIANGLE_WIDTH + capWidth, offsetX + TRIANGLE_WIDTH + capWidth + TRIANGLE_WIDTH, offsetX + TRIANGLE_WIDTH + capWidth, offsetX + TRIANGLE_WIDTH + capWidth };
        yPoints = new int[] { offsetY + height, offsetY + 0, offsetY + 0, offsetY + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
        
        
        if ( yellowFlagState.getValue() == YellowFlagState.NONE )
            texCanvas.setColor( getBackgroundColor() );
        else
            texCanvas.setColor( Color.YELLOW );
        
        texCanvas.setAntialiazingEnabled( aaTrian );
        
        xPoints = new int[] { offsetX + TRIANGLE_WIDTH + capWidth, offsetX + TRIANGLE_WIDTH + TRIANGLE_WIDTH + capWidth, offsetX + TRIANGLE_WIDTH + TRIANGLE_WIDTH + capWidth, offsetX + TRIANGLE_WIDTH + capWidth };
        yPoints = new int[] { offsetY + height, offsetY + height, offsetY + 0, offsetY + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
        
        texCanvas.setAntialiazingEnabled( false );
        
        //int lapAreaWidth = width - 4 * TRIANGLE_WIDTH - capWidth;
        rect = new Rect2i( offsetX + 2 * TRIANGLE_WIDTH + capWidth, offsetY + 0, width - 3 * TRIANGLE_WIDTH - capWidth, offsetY + height );
        
        texCanvas.fillRect( rect );
        //texCanvas.drawRect( rect );
        
        texCanvas.setAntialiazingEnabled( aaTrian );
        
        xPoints = new int[] { offsetX + width - TRIANGLE_WIDTH, offsetX + width, offsetX + width - TRIANGLE_WIDTH, offsetX + width - TRIANGLE_WIDTH };
        yPoints = new int[] { offsetY + height, offsetY + 0, offsetY + 0, offsetY + height };
        
        texCanvas.fillPolygon( xPoints, yPoints, xPoints.length );
        //texCanvas.drawPolygon( xPoints, yPoints, xPoints.length );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = ( lapDriver.getEnumValue() == LapDriver.ME ) ? scoringInfo.getPlayersVehicleScoringInfo() : scoringInfo.getVehicleScoringInfo( 0 );
        
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
        
        if ( needsCompleteRedraw || lap.hasChanged() )
        {
            Color bgColor = getBackgroundColor();
            Color color = getFontColor();
            if ( yellowFlagState.getValue() != YellowFlagState.NONE )
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
        writer.writeProperty( lapDriver, "The driver, who's laps are used for the lap display. Valid values: LEADER, ME." );
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
        else if ( lapDriver.loadProperty( key, value ) );
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
        propsCont.addProperty( lapDriver );
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
    
    public LapWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.12f, Size.PERCENT_OFFSET + 0.0254f );
        
        getBackgroundColorProperty().setValue( "#000000" );
        getFontColorProperty().setValue( "#FFFFFF" );
        getFontProperty().setValue( "TVStyleFont" );
    }
}
