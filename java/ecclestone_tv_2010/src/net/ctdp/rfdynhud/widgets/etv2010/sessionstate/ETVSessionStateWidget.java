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
package net.ctdp.rfdynhud.widgets.etv2010.sessionstate;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionLimit;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.YellowFlagState;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.etv2010._base.ETVWidgetBase;
import net.ctdp.rfdynhud.widgets.etv2010._util.ETVUtils;
import net.ctdp.rfdynhud.widgets.etv2010._util.ETVImages.BGType;

/**
 * The {@link ETVSessionStateWidget} displays the current lap.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ETVSessionStateWidget extends ETVWidgetBase
{
    private final EnumProperty<SessionLimit> sessionLimitPreference = new EnumProperty<SessionLimit>( "sessionLimitPreference", "sessionLimitPref", SessionLimit.LAPS );
    
    private SessionLimit sessionLimit = SessionLimit.LAPS;
    
    private DrawnString captionString = null;
    private DrawnString stateString = null;
    
    private String caption = getCaption( SessionType.RACE, SessionLimit.LAPS );
    
    private final EnumValue<GamePhase> gamePhase = new EnumValue<GamePhase>();
    private final EnumValue<YellowFlagState> yellowFlagState = new EnumValue<YellowFlagState>( YellowFlagState.NONE );
    private final BoolValue sectorYellowFlag = new BoolValue();
    
    private final IntValue lap = new IntValue();
    private final FloatValue sessionTime = new FloatValue( -1f, 0.1f );
    
    private BGType bgType = BGType.NEUTRAL;
    private Color dataBgColor = Color.MAGENTA;
    private Color dataFontColor = Color.GREEN;
    
    private static final Alignment[] colAligns = new Alignment[] { Alignment.RIGHT, Alignment.CENTER, Alignment.RIGHT };
    private final int[] colWidths = new int[ 3 ];
    private static final int colPadding = 10;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNeededData()
    {
        return ( Widget.NEEDED_DATA_SCORING );
    }
    
    private static final String getCaption( SessionType sessionType, SessionLimit sessionLimit )
    {
        switch ( sessionType )
        {
            case TEST_DAY:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_TEST_DAY_time );
                
                return ( Loc.caption_TEST_DAY_laps );
            case PRACTICE1:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_PRACTICE1_time );
                
                return ( Loc.caption_PRACTICE1_laps );
            case PRACTICE2:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_PRACTICE2_time );
                
                return ( Loc.caption_PRACTICE2_laps );
            case PRACTICE3:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_PRACTICE3_time );
                
                return ( Loc.caption_PRACTICE3_laps );
            case PRACTICE4:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_PRACTICE4_time );
                
                return ( Loc.caption_PRACTICE4_laps );
            case QUALIFYING:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_QUALIFYING_time );
                
                return ( Loc.caption_QUALIFYING_laps );
            case WARMUP:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_WARMUP_time );
                
                return ( Loc.caption_WARMUP_laps );
            case RACE:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_RACE_time );
                
                return ( Loc.caption_RACE_laps );
        }
        
        // Unreachable code!
        return ( "N/A" );
    }
    
    private boolean updateSessionLimit( LiveGameData gameData )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        SessionLimit oldSessionLimit = sessionLimit;
        String oldCaption = caption;
        
        sessionLimit = scoringInfo.getViewedVehicleScoringInfo().getSessionLimit( sessionLimitPreference.getEnumValue() );
        caption = getCaption( scoringInfo.getSessionType(), sessionLimit );
        
        if ( ( sessionLimit != oldSessionLimit ) || !caption.equals( oldCaption ) )
        {
            forceReinitialization();
            forceCompleteRedraw( false );
            
            return ( true );
        }
        
        return ( false );
    }
    
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, boolean isEditorMode )
    {
        super.onSessionStarted( sessionType, gameData, isEditorMode );
        
        yellowFlagState.reset();
        sectorYellowFlag.reset();
        lap.reset();
        sessionTime.reset();
        gamePhase.reset();
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
    protected boolean checkForChanges( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        gamePhase.update( scoringInfo.getGamePhase() );
        yellowFlagState.update( scoringInfo.getYellowFlagState() );
        sectorYellowFlag.update( scoringInfo.getSectorYellowFlag( scoringInfo.getViewedVehicleScoringInfo().getSector() ) );
        
        boolean changed = false;
        if ( gamePhase.hasChanged() )
            changed = true;
        if ( yellowFlagState.hasChanged() )
            changed = true;
        if ( sectorYellowFlag.hasChanged() )
            changed = true;
        
        bgType = BGType.NEUTRAL;
        dataBgColor = dataBackgroundColor.getColor();
        dataFontColor = getFontColor();
        if ( ( gamePhase.getValue() == GamePhase.FORMATION_LAP ) || ( gamePhase.getValue() == GamePhase.FULL_COURSE_YELLOW ) || sectorYellowFlag.getValue() )
        {
            bgType = BGType.LABEL_YELLOW;
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
            bgType = BGType.LABEL_RED;
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
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        texCanvas.setFont( getFont() );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D capBounds = metrics.getStringBounds( caption, texCanvas );
        
        boolean useImages = this.useImages.getBooleanValue();
        
        int dataAreaCenter = useImages ? getImages().getLabeledDataDataCenter( width, height, capBounds ) : ETVUtils.getLabeledDataDataCenter( width, height, capBounds );
        int vMiddle = ETVUtils.getLabeledDataVMiddle( height, capBounds );
        
        int captionLeft = useImages ? getImages().getLabeledDataCaptionLeft( height ) : ETVUtils.getTriangleWidth( height );
        
        captionString = dsf.newDrawnString( "captionString", captionLeft, vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        stateString = dsf.newDrawnString( "stateString", dataAreaCenter, vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        if ( sessionLimit == SessionLimit.LAPS )
            stateString.getMinColWidths( new String[] { "00", "/", "00" }, colAligns, colPadding, colWidths );
        
        forceCompleteRedraw( false );
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        if ( useImages.getBooleanValue() )
            ETVUtils.drawLabeledDataBackgroundI( offsetX, offsetY, width, height, caption, getFontProperty(), getImages(), bgType, texture, false );
        else
            ETVUtils.drawLabeledDataBackground( offsetX, offsetY, width, height, caption, getFontProperty(), captionBackgroundColor.getColor(), dataBgColor, texture, false );
    }
    
    @Override
    public void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getSessionType().isRace() ? scoringInfo.getLeadersVehicleScoringInfo() : scoringInfo.getViewedVehicleScoringInfo();
        
        if ( needsCompleteRedraw )
        {
            captionString.draw( offsetX, offsetY, caption, texture );
        }
        
        if ( sessionLimit == SessionLimit.TIME )
        {
            sessionTime.update( gameData.getScoringInfo().getSessionTime() );
            float endTime = gameData.getScoringInfo().getEndTime();
            if ( needsCompleteRedraw || ( clock.c() && ( sessionTime.hasChanged( false ) || gamePhase.hasChanged( false ) ) ) )
            {
                sessionTime.setUnchanged();
                gamePhase.setUnchanged();
                
                if ( gamePhase.getValue() == GamePhase.SESSION_OVER )
                    stateString.draw( offsetX, offsetY, "00:00:00", dataFontColor, texture );
                else if ( scoringInfo.getSessionType().isRace() && ( ( gamePhase.getValue() == GamePhase.FORMATION_LAP ) || ( endTime < 0f ) || ( endTime > 3000000f ) ) )
                    stateString.draw( offsetX, offsetY, "--:--:--", dataFontColor, texture );
                else if ( scoringInfo.getSessionType().isTestDay() || ( endTime < 0f ) || ( endTime > 3000000f ) )
                    stateString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( sessionTime.getValue(), true, false ), dataFontColor, texture );
                else
                    stateString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( endTime - sessionTime.getValue(), true, false ), dataFontColor, texture );
            }
        }
        else
        {
            if ( scoringInfo.getSessionType().isRace() && ( gamePhase.getValue() == GamePhase.FORMATION_LAP ) )
                lap.update( 0 );
            else if ( gameData.getProfileInfo().getShowCurrentLap() )
                lap.update( vsi.getCurrentLap() );
            else
                lap.update( vsi.getLapsCompleted() );
            
            if ( needsCompleteRedraw || ( clock.c() && lap.hasChanged() ) )
            {
                int maxLaps = scoringInfo.getMaxLaps();
                String maxLapsStr = ( maxLaps < 10000 ) ? String.valueOf( maxLaps ) : "--";
                
                stateString.drawColumns( offsetX, offsetY, new String[] { lap.getValueAsString(), "/", maxLapsStr }, colAligns, colPadding, colWidths, dataFontColor, texture );
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
        
        writer.writeProperty( sessionLimitPreference, "If a session is limited by both laps and time, this limit will be displayed." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( sessionLimitPreference ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void getItemGapProperty( PropertiesContainer propsCont, boolean forceAll )
    {
        // No super call. We don't need the item gap here!
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Misc" );
        
        propsCont.addProperty( sessionLimitPreference );
    }
    
    public ETVSessionStateWidget()
    {
        super( 12.0f, 2.54f );
    }
}
