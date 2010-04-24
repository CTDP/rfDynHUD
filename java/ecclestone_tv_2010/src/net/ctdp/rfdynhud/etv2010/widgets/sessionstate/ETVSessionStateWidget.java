package net.ctdp.rfdynhud.etv2010.widgets.sessionstate;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.YellowFlagState;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link ETVSessionStateWidget} displays the current lap.
 * 
 * @author Marvin Froehlich
 */
public class ETVSessionStateWidget extends Widget
{
    private final ColorProperty captionBackgroundColor = new ColorProperty( this, "captionBgColor", ETVUtils.ETV_STYLE_CAPTION_BACKGROUND_COLOR );
    private final ColorProperty captionColor = new ColorProperty( this, "captionColor", ETVUtils.ETV_STYLE_CAPTION_FONT_COLOR );
    
    private static enum LapDisplayType
    {
        CURRENT_LAP,
        LAPS_DONE,
        ;
    }
    
    private final EnumProperty<LapDisplayType> lapDisplayType = new EnumProperty<LapDisplayType>( this, "lapDisplayType", LapDisplayType.CURRENT_LAP );
    
    private static enum SessionLimit
    {
        LAPS,
        TIME,
        ;
    }
    
    private final EnumProperty<SessionLimit> sessionLimitPreference = new EnumProperty<SessionLimit>( this, "sessionLimitPreference", SessionLimit.LAPS );
    
    private SessionLimit sessionLimit = SessionLimit.LAPS;
    
    private DrawnString captionString = null;
    private DrawnString stateString = null;
    
    private final StringProperty testDayCaption = new StringProperty( this, "testDayCaption", "Lap" );
    private final StringProperty practice1Caption = new StringProperty( this, "practice1Caption", "" );
    private final StringProperty practice2Caption = new StringProperty( this, "practice2Caption", "Q1" );
    private final StringProperty practice3Caption = new StringProperty( this, "practice3Caption", "Q2" );
    private final StringProperty practice4Caption = new StringProperty( this, "practice4Caption", "Practice4" );
    private final StringProperty qualifyingCaption = new StringProperty( this, "qualifyingCaption", "Q3" );
    
    private static final String LAPS_CAPTION = "Lap";
    private static final String TIME_CAPTION = "Time";
    
    private String caption = LAPS_CAPTION;
    
    private final EnumValue<GamePhase> gamePhase = new EnumValue<GamePhase>();
    private final EnumValue<YellowFlagState> yellowFlagState = new EnumValue<YellowFlagState>( YellowFlagState.NONE );
    private final BoolValue sectorYellowFlag = new BoolValue();
    
    private final IntValue lap = new IntValue();
    private final FloatValue sessionTime = new FloatValue( -1f, 0.1f );
    
    private Color dataBgColor = Color.MAGENTA;
    private Color dataFontColor = Color.GREEN;
    
    private static final Alignment[] colAligns = new Alignment[] { Alignment.RIGHT, Alignment.CENTER, Alignment.RIGHT };
    private final int[] colWidths = new int[ 3 ];
    private static final int colPadding = 10;
    
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
    
    private static final SessionLimit getSessionLimit( ScoringInfo scoringInfo, SessionLimit preference )
    {
        final int maxLaps = scoringInfo.getMaxLaps();
        final float endTime = scoringInfo.getEndTime();
        
        if ( maxLaps < 10000 )
        {
            if ( ( endTime > 0f ) && ( endTime < 999999f ) )
            {
                int timeLaps = (int)( endTime / 95.0f ) * 2;
                Laptime fastestLap = scoringInfo.getPlayersVehicleScoringInfo().getFastestLaptime();
                if ( fastestLap != null )
                    timeLaps = (int)( endTime / ( fastestLap.getLapTime() * 1.033f ) );
                
                if ( timeLaps < maxLaps )
                    return ( SessionLimit.TIME );
                
                return ( preference );
            }
            
            return ( SessionLimit.LAPS );
        }
        
        if ( ( endTime > 0f ) && ( endTime < 999999f ) )
            return ( SessionLimit.TIME );
        
        
        return ( SessionLimit.LAPS );
    }
    
    private final String getCaption_( ScoringInfo scoringInfo )
    {
        switch ( scoringInfo.getSessionType() )
        {
            case TEST_DAY:
                return ( testDayCaption.getStringValue() );
            case PRACTICE1:
                return ( practice1Caption.getStringValue() );
            case PRACTICE2:
                return ( practice2Caption.getStringValue() );
            case PRACTICE3:
                return ( practice3Caption.getStringValue() );
            case PRACTICE4:
                return ( practice4Caption.getStringValue() );
            case QUALIFYING:
                return ( qualifyingCaption.getStringValue() );
            case WARMUP:
            case RACE:
                return ( "" );
        }
        
        return ( "N/A" );
    }
    
    private final String getCaption( ScoringInfo scoringInfo, SessionLimit sessionLimit )
    {
        String caption = getCaption_( scoringInfo );
        
        if ( caption.equals( "" ) )
        {
            if ( sessionLimit == SessionLimit.TIME )
                return ( TIME_CAPTION );
            
            return ( LAPS_CAPTION );
        }
        
        return ( caption );
    }
    
    private boolean updateSessionLimit( LiveGameData gameData )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        SessionLimit oldSessionLimit = sessionLimit;
        String oldCaption = caption;
        
        sessionLimit = getSessionLimit( scoringInfo, sessionLimitPreference.getEnumValue() );
        caption = getCaption( scoringInfo, sessionLimit );
        
        if ( ( sessionLimit != oldSessionLimit ) || !caption.equals( oldCaption ) )
        {
            //Logger.log( ">> sessionLimit changed: " + sessionLimit + ", " + caption );
            
            forceReinitialization();
            forceCompleteRedraw();
            
            return ( true );
        }
        
        return ( false );
    }
    
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, editorPresets );
        
        //Logger.log( "afterConfigurationLoaded(): " + gameData.getScoringInfo().getMaxLaps() + ", " + gameData.getScoringInfo().getEndTime() );
        //updateSessionLimit( gameData );
    }
    
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onSessionStarted( sessionType, gameData, editorPresets );
        
        //Logger.log( "onSessionStarted(): " + gameData.getScoringInfo().getSessionType() + ", " + gameData.getScoringInfo().getMaxLaps() + ", " + gameData.getScoringInfo().getEndTime() );
        
        yellowFlagState.reset();
        sectorYellowFlag.reset();
        lap.reset();
        sessionTime.reset();
        gamePhase.reset();
        
        //updateSessionLimit( gameData );
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
        
        gamePhase.update( scoringInfo.getGamePhase() );
        yellowFlagState.update( scoringInfo.getYellowFlagState() );
        sectorYellowFlag.update( scoringInfo.getSectorYellowFlag( scoringInfo.getPlayersVehicleScoringInfo().getSector() ) );
        
        boolean changed = false;
        if ( gamePhase.hasChanged() )
            changed = true;
        if ( yellowFlagState.hasChanged() )
            changed = true;
        if ( sectorYellowFlag.hasChanged() )
            changed = true;
        
        dataBgColor = getBackgroundColor();
        dataFontColor = getFontColor();
        if ( ( gamePhase.getValue() == GamePhase.FORMATION_LAP ) || ( gamePhase.getValue() == GamePhase.FULL_COURSE_YELLOW ) || sectorYellowFlag.getValue() )
        {
            dataBgColor = Color.YELLOW;
            dataFontColor = Color.BLACK;
        }
        /*
        else if ( gamePhase.getValue() == GamePhase.GREEN_FLAG )
        {
            dataBgColor = Color.GREEN;
            dataFontColor = Color.WHITE;
        }
        */
        else if ( gamePhase.getValue() == GamePhase.SESSION_STOPPED )
        {
            dataBgColor = Color.RED;
            dataFontColor = Color.WHITE;
        }
        
        if ( updateSessionLimit( gameData ) )
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
        
        Rectangle2D capBounds = metrics.getStringBounds( caption, texCanvas );
        
        int dataAreaCenter = ETVUtils.getLabeledDataDataCenter( width, capBounds );
        int vMiddle = ETVUtils.getLabeledDataVMiddle( height, capBounds );
        
        captionString = new DrawnString( ETVUtils.TRIANGLE_WIDTH, vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        stateString = new DrawnString( dataAreaCenter, vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        if ( sessionLimit == SessionLimit.LAPS )
            stateString.getMinColWidths( new String[] { "00", "/", "00" }, colAligns, colPadding, texture, colWidths );
        
        forceCompleteRedraw();
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ETVUtils.drawLabeledDataBackground( offsetX, offsetY, width, height, caption, getFont(), captionBackgroundColor.getColor(), dataBgColor, texture, true );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getSessionType().isRace() ? scoringInfo.getVehicleScoringInfo( 0 ) : scoringInfo.getPlayersVehicleScoringInfo();
        
        if ( needsCompleteRedraw )
        {
            captionString.draw( offsetX, offsetY, caption, captionBackgroundColor.getColor(), texture );
        }
        
        if ( sessionLimit == SessionLimit.TIME )
        {
            sessionTime.update( gameData.getScoringInfo().getSessionTime() );
            float totalTime = gameData.getScoringInfo().getEndTime();
            if ( needsCompleteRedraw || ( clock1 && ( sessionTime.hasChanged( false ) || gamePhase.hasChanged( false ) ) ) )
            {
                sessionTime.setUnchanged();
                gamePhase.setUnchanged();
                
                if ( scoringInfo.getSessionType().isRace() && ( ( gamePhase.getValue() == GamePhase.FORMATION_LAP ) || ( totalTime < 0f ) || ( totalTime > 3000000f ) ) )
                    stateString.draw( offsetX, offsetY, "--:--:--", dataBgColor, dataFontColor, texture );
                else if ( scoringInfo.getSessionType().isTestDay() || ( totalTime < 0f ) || ( totalTime > 3000000f ) )
                    stateString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( sessionTime.getValue(), true, false ), dataBgColor, dataFontColor, texture );
                else
                    stateString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( totalTime - sessionTime.getValue(), true, false ), dataBgColor, dataFontColor, texture );
            }
        }
        else
        {
            if ( scoringInfo.getSessionType().isRace() && ( gamePhase.getValue() == GamePhase.FORMATION_LAP ) )
                lap.update( 0 );
            else if ( lapDisplayType.getValue() == LapDisplayType.CURRENT_LAP )
                lap.update( vsi.getCurrentLap() );
            else if ( lapDisplayType.getValue() == LapDisplayType.LAPS_DONE )
                lap.update( vsi.getLapsCompleted() );
            
            if ( needsCompleteRedraw || ( clock1 && lap.hasChanged() ) )
            {
                int maxLaps = scoringInfo.getMaxLaps();
                String maxLapsStr = ( maxLaps < 10000 ) ? String.valueOf( maxLaps ) : "--";
                
                stateString.drawColumns( offsetX, offsetY, new String[] { lap.getValueAsString(), "/", maxLapsStr }, colAligns, colPadding, colWidths, dataBgColor, dataFontColor, texture );
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
        
        writer.writeProperty( captionBackgroundColor, "The background color for the \"Lap\" caption." );
        writer.writeProperty( captionColor, "The font color for the \"Lap\" caption." );
        writer.writeProperty( testDayCaption, "The caption String (on the left) for the TEST_DAY session." );
        writer.writeProperty( practice1Caption, "The caption String (on the left) for the PRACTICE1 session." );
        writer.writeProperty( practice2Caption, "The caption String (on the left) for the PRACTICE2 session." );
        writer.writeProperty( practice3Caption, "The caption String (on the left) for the PRACTICE3 session." );
        writer.writeProperty( practice4Caption, "The caption String (on the left) for the PRACTICE4 session." );
        writer.writeProperty( qualifyingCaption, "The caption String (on the left) for the QUALIFYING session." );
        writer.writeProperty( lapDisplayType, "The way the laps are displayed. Valid values: CURRENT_LAP, LAPS_DONE." );
        writer.writeProperty( sessionLimitPreference, "If a session is limited by both laps and time, this limit will be displayed." );
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
        else if ( testDayCaption.loadProperty( key, value ) );
        else if ( practice1Caption.loadProperty( key, value ) );
        else if ( practice2Caption.loadProperty( key, value ) );
        else if ( practice3Caption.loadProperty( key, value ) );
        else if ( practice4Caption.loadProperty( key, value ) );
        else if ( qualifyingCaption.loadProperty( key, value ) );
        else if ( lapDisplayType.loadProperty( key, value ) );
        else if ( sessionLimitPreference.loadProperty( key, value ) );
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
        propsCont.addProperty( testDayCaption );
        propsCont.addProperty( practice1Caption );
        propsCont.addProperty( practice2Caption );
        propsCont.addProperty( practice3Caption );
        propsCont.addProperty( practice4Caption );
        propsCont.addProperty( qualifyingCaption );
        propsCont.addProperty( lapDisplayType );
        propsCont.addProperty( sessionLimitPreference );
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
    
    public ETVSessionStateWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.12f, Size.PERCENT_OFFSET + 0.0254f );
        
        getBackgroundColorProperty().setValue( ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR );
        getFontColorProperty().setValue( ETVUtils.ETV_STYLE_DATA_FONT_COLOR );
        getFontProperty().setValue( ETVUtils.ETV_STYLE_FONT );
    }
}
