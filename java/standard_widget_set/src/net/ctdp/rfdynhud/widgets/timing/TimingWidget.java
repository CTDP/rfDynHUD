package net.ctdp.rfdynhud.widgets.timing;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link TimingWidget} displays lap- and sector times and gaps.
 * 
 * @author Marvin Froehlich
 */
public class TimingWidget extends Widget
{
    private final BooleanProperty displayAbsFastest = new BooleanProperty( this, "displayAbsFastest", true );
    private final BooleanProperty cumulativeSectors = new BooleanProperty( this, "cumulativeSectors", false );
    private final BooleanProperty forceCurrentCumulSectors = new BooleanProperty( this, "forceCurrentCumulSectors", true );
    private final IntProperty lastLapDisplayDelay = new IntProperty( this, "lastLapDisplayDelay", 10000, -100, Integer.MAX_VALUE ); // ten seconds
    
    private final ColorProperty slowerColor = new ColorProperty( this, "slowerColor", "#FF7248" );
    private final ColorProperty fasterColor = new ColorProperty( this, "fasterColor", "#6AFF3D" );
    
    private DrawnString absFastestLapHeaderString = null;
    private DrawnString absFastestLapDriverString = null;
    private DrawnString absSector1String = null;
    private DrawnString absSector2String = null;
    private DrawnString absSector3String = null;
    private DrawnString absFastestLapString = null;
    
    private DrawnString ownFastestLapHeaderString = null;
    private DrawnString ownSector1String = null;
    private DrawnString ownSector2String = null;
    private DrawnString ownSector3String = null;
    private DrawnString ownFastestLapString = null;
    
    private DrawnString currLapHeaderString = null;
    private DrawnString currSector1String = null;
    private DrawnString currSector2String = null;
    private DrawnString currSector3String = null;
    private DrawnString currLapString = null;
    
    private String oldLeader = null;
    private int oldAbsFastestLap = -1;
    private boolean absFLValid = false;
    private int oldOwnFastestLap = -1;
    private boolean ownFLValid = false;
    private boolean currLapValid = false;
    
    private float lastLapDisplayTime = -1f;
    private float gapOFSec1 = 0f;
    private boolean gapOFSec1Valid = false;
    private float gapOFSec2 = 0f;
    private boolean gapOFSec2Valid = false;
    private float gapOFSec3 = 0f;
    private boolean gapOFSec3Valid = false;
    private float gapOFLap = 0f;
    private float gapAFSec1 = 0f;
    private boolean gapAFSec1Valid = false;
    private float gapAFSec2 = 0f;
    private boolean gapAFSec2Valid = false;
    private float gapAFSec3 = 0f;
    private boolean gapAFSec3Valid = false;
    private float gapAFLap = 0f;
    
    private final java.awt.Color[] fontColors = new java.awt.Color[ 5 ];
    
    private Laptime delayedAbsFastestLap = null;
    private Laptime delayedOwnFastestLap = null;
    
    private boolean delayedAbsFastestIsOwn = false;
    
    private static final int padding = 10;
    
    @Override
    public String getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onSessionStarted( sessionType, gameData, editorPresets );
        
        oldLeader = null;
        
        oldAbsFastestLap = -1;
        oldOwnFastestLap = -1;
        
        absFLValid = false;
        ownFLValid = false;
        currLapValid = false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        currLapValid = false;
        
        lastLapDisplayTime = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPitsExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onPitsExited( gameData, editorPresets );
        
        currLapValid = false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayerLapStarted( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onPlayerLapStarted( gameData, editorPresets );
        
        VehicleScoringInfo vsi = gameData.getScoringInfo().getPlayersVehicleScoringInfo();
        
        if ( vsi.getStintLength() < 1.9f )
            lastLapDisplayTime = -1f;
        else if ( lastLapDisplayDelay.getIntValue() < 0 )
            lastLapDisplayTime = vsi.getLapStartTime() + ( vsi.getLaptime( vsi.getLapsCompleted() ).getSector1() * -lastLapDisplayDelay.getIntValue() / 100f );
        else
            lastLapDisplayTime = vsi.getLapStartTime() + ( lastLapDisplayDelay.getIntValue() / 1000f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final java.awt.Font font = getFont();
        final boolean fontAntiAliased = isFontAntiAliased();
        final java.awt.Color fontColor = getFontColor();
        
        final int left1 = 2;
        final int left2 = left1 + 10;
        int top = -2;
        final int sectionGap = 7;
        
        DrawnString yRel = null;
        
        if ( displayAbsFastest.getBooleanValue() )
        {
            absFastestLapHeaderString = dsf.newDrawnString( "absFastestLapHeaderString", left1, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            absFastestLapDriverString = dsf.newDrawnString( "absFastestLapDriverString", null, absFastestLapHeaderString, left2, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Driver: ", null );
            absSector1String = dsf.newDrawnString( "absSector1String", null, absFastestLapDriverString, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            absSector2String = dsf.newDrawnString( "absSector2String", null, absSector1String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            if ( !cumulativeSectors.getBooleanValue() )
            {
                absSector3String = dsf.newDrawnString( "absSector3String", null, absSector2String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
                yRel = absSector3String;
            }
            else
            {
                absSector3String = null;
                yRel = absSector2String;
            }
            absFastestLapString = dsf.newDrawnString( "absFastestLapString", null, yRel, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            yRel = absFastestLapString;
            top = sectionGap;
        }
        else
        {
            absFastestLapHeaderString = null;
            absFastestLapDriverString = null;
            absSector1String = null;
            absSector2String = null;
            absSector3String = null;
            absFastestLapString = null;
            yRel = null;
        }
        
        ownFastestLapHeaderString = dsf.newDrawnString( "ownFastestLapHeaderString", null, yRel, left1, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        ownSector1String = dsf.newDrawnString( "ownSector1String", null, ownFastestLapHeaderString, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        ownSector2String = dsf.newDrawnString( "ownSector2String", null, ownSector1String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        if ( !cumulativeSectors.getBooleanValue() )
        {
            ownSector3String = dsf.newDrawnString( "ownSector3String", null, ownSector2String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            yRel = ownSector3String;
        }
        else
        {
            ownSector3String = null;
            yRel = ownSector2String;
        }
        ownFastestLapString = dsf.newDrawnString( "ownFastestLapString", null, yRel, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        
        currLapHeaderString = dsf.newDrawnString( "currLapHeaderString", null, ownFastestLapString, left1, sectionGap, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        currSector1String = dsf.newDrawnString( "currSector1String", null, currLapHeaderString, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        currSector2String = dsf.newDrawnString( "currSector2String", null, currSector1String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        if ( !cumulativeSectors.getBooleanValue() && !forceCurrentCumulSectors.getBooleanValue() )
        {
            currSector3String = dsf.newDrawnString( "currSector3String", null, currSector2String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            yRel = currSector3String;
        }
        else
        {
            currSector3String = null;
            yRel = currSector2String;
        }
        currLapString = dsf.newDrawnString( "currLapString", null, yRel, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        
        fontColors[0] = getFontColor();
        fontColors[1] = getFontColor();
        fontColors[2] = getFontColor();
        fontColors[3] = getFontColor();
        fontColors[4] = getFontColor();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        final java.awt.Color backgroundColor = getBackgroundColor();
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo afVSI = scoringInfo.getFastestLapVSI();
        boolean absFastestIsSecond = false;
        if ( afVSI == scoringInfo.getPlayersVehicleScoringInfo() )
        {
            if ( scoringInfo.getSecondFastestLapVSI() != null )
            {
                afVSI = scoringInfo.getSecondFastestLapVSI();
                absFastestIsSecond = true;
            }
        }
        Laptime afLaptime = afVSI.getFastestLaptime();
        if ( isEditorMode && ( afVSI == scoringInfo.getPlayersVehicleScoringInfo() ) )
        {
            // Just to get differences...
            
            if ( afVSI == scoringInfo.getVehicleScoringInfo( 0 ) )
                afVSI = scoringInfo.getVehicleScoringInfo( 1 );
            else
                afVSI = scoringInfo.getVehicleScoringInfo( 0 );
        }
        
        if ( displayAbsFastest.getBooleanValue() )
        {
            // absolute fastest lap
            
            float lap = afVSI.getBestLapTime();
            boolean lv = ( lap > 0f );
            String leader = lv ? afVSI.getDriverName() : "";
            String testLeader = lv ? scoringInfo.getFastestLapVSI().getDriverName() : "";
            
            if ( needsCompleteRedraw || !testLeader.equals( oldLeader ) )
            {
                oldLeader = testLeader;
                
                if ( absFastestIsSecond )
                    absFastestLapHeaderString.draw( offsetX, offsetY, "Abs. second fastest Lap:", backgroundColor, texture );
                else
                    absFastestLapHeaderString.draw( offsetX, offsetY, "Abs. fastest Lap:", backgroundColor, texture );
                absFastestLapDriverString.draw( offsetX, offsetY, leader, backgroundColor, texture );
            }
            
            int lap_ = Math.round( lap * 10000f );
            
            if ( needsCompleteRedraw || ( lap_ != oldAbsFastestLap ) || ( lv != absFLValid ) )
            {
                oldAbsFastestLap = lap_;
                absFLValid = lv;
                
                if ( absFLValid )
                {
                    float sec1 = ( afLaptime != null ) ? afLaptime.getSector1() : -1f;
                    float sec2 = ( afLaptime != null ) ? afLaptime.getSector2( cumulativeSectors.getBooleanValue() ) : -1f;
                    float sec3 = ( afLaptime != null ) ? ( cumulativeSectors.getBooleanValue() ? sec2 + afLaptime.getSector3() : afLaptime.getSector3() ) : -1f;
                    
                    if ( isEditorMode )
                    {
                        sec1 = 12.345f;
                        sec2 = 35.226f;
                        sec3 = 34.567f;
                    }
                    
                    int cols = 3;
                    String[][] s = new String[4][cols];
                    int[] colWidths = new int[cols];
                    final Alignment[] aligns = { Alignment.RIGHT, Alignment.LEFT, Alignment.RIGHT };
                    
                    s[0][0] = "Sec1:";
                    s[0][1] = null;
                    if ( sec1 > 0f )
                        s[0][2] = TimingUtil.getTimeAsLaptimeString( sec1 );
                    else
                        s[0][3] = "-:--.---";
                    
                    s[1][0] = "Sec2:";
                    s[1][1] = null;
                    if ( sec2 > 0f )
                        s[1][2] = TimingUtil.getTimeAsLaptimeString( sec2 );
                    else
                        s[1][2] = "-:--.---";
                    
                    if ( !cumulativeSectors.getBooleanValue() )
                    {
                        s[2][0] = "Sec3:";
                        s[2][1] = null;
                        if ( sec3 > 0f )
                            s[2][2] = TimingUtil.getTimeAsLaptimeString( sec3 );
                        else
                            s[2][2] = "-:--.---";
                    }
                    else
                    {
                        s[2][0] = null;
                        s[2][1] = null;
                        s[2][2] = "";
                    }
                    
                    s[3][0] = "Lap:";
                    s[3][1] = null;
                    s[3][2] = TimingUtil.getTimeAsLaptimeString( lap );
                    
                    absSector1String.getMaxColWidths( s[0], aligns, padding, texture, colWidths );
                    absSector2String.getMaxColWidths( s[1], aligns, padding, texture, colWidths );
                    if ( !cumulativeSectors.getBooleanValue() )
                        absSector3String.getMaxColWidths( s[2], aligns, padding, texture, colWidths );
                    absFastestLapString.getMaxColWidths( s[3], aligns, padding, texture, colWidths );
                    
                    absSector1String.drawColumns( offsetX, offsetY, s[0], aligns, padding, colWidths, backgroundColor, texture );
                    absSector2String.drawColumns( offsetX, offsetY, s[1], aligns, padding, colWidths, backgroundColor, texture );
                    if ( !cumulativeSectors.getBooleanValue() )
                        absSector3String.drawColumns( offsetX, offsetY, s[2], aligns, padding, colWidths, backgroundColor, texture );
                    absFastestLapString.drawColumns( offsetX, offsetY, s[3], aligns, padding, colWidths, backgroundColor, texture );
                }
                else
                {
                    absSector1String.draw( offsetX, offsetY, "-:--.---", backgroundColor, texture );
                    absSector2String.draw( offsetX, offsetY, "-:--.---", backgroundColor, texture );
                    if ( !cumulativeSectors.getBooleanValue() )
                        absSector3String.draw( offsetX, offsetY, "-:--.---", backgroundColor, texture );
                    absFastestLapString.draw( offsetX, offsetY, "-:--.---", backgroundColor, texture );
                }
            }
        }
        
        {
            // own fastest lap
            
            final VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
            Laptime laptime = vsi.getFastestLaptime();
            float lap = vsi.getBestLapTime();
            
            if ( needsCompleteRedraw )
            {
                ownFastestLapHeaderString.draw( offsetX, offsetY, "Own fastest Lap:", backgroundColor, texture );
            }
            
            int lap_ = Math.round( lap * 10000f );
            
            boolean lv = ( lap > 0f );
            if ( needsCompleteRedraw || ( lap_ != oldOwnFastestLap ) || ( lv != ownFLValid ) )
            {
                oldOwnFastestLap = lap_;
                ownFLValid = lv;
                
                if ( ownFLValid )
                {
                    boolean displayCumul = cumulativeSectors.getBooleanValue();
                    
                    float afSec1 = ( afLaptime != null ) ? afLaptime.getSector1() : -1f; //afVSI.getBestSector1();
                    float afSec2 = ( afLaptime != null ) ? afLaptime.getSector2( displayCumul ) : -1f; //afVSI.getBestSector2( displayCumul );
                    float afSec3 = ( afLaptime != null ) ? ( displayCumul ? afSec2 + afLaptime.getSector3() : afLaptime.getSector3() ) : -1f; //displayCumul ? afSec2 + afVSI.getBestSector3() : afVSI.getBestSector3();
                    float afLap = ( afLaptime != null ) ? afLaptime.getLapTime() : -1f;// afVSI.getBestLapTime();
                    
                    float sec1 = ( laptime != null ) ? laptime.getSector1() : -1f; //vsi.getBestSector1();
                    float sec2 = ( laptime != null ) ? laptime.getSector2( displayCumul ) : -1f; //vsi.getBestSector2( displayCumul );
                    float sec3 = ( laptime != null ) ? ( displayCumul ? sec2 + laptime.getSector3() : laptime.getSector3() ) : -1f; //displayCumul ? sec2 + vsi.getBestSector3() : vsi.getBestSector3();
                    
                    if ( isEditorMode )
                    {
                        afSec1 = 12.345f;
                        afSec2 = 35.226f;
                        afSec3 = 34.567f;
                        afLap = 82.138f;
                        
                        sec1 = 34.567f;
                        sec2 = 32.750f;
                        sec3 = 12.345f;
                    }
                    
                    final boolean dispGapToAbs = ( displayAbsFastest.getBooleanValue() && vsi != afVSI );
                    int cols = dispGapToAbs ? 4 : 3;
                    String[][] s = new String[4][cols];
                    int[] colWidths = new int[cols];
                    final Alignment[] aligns = ( vsi != afVSI ) ? new Alignment[] { Alignment.RIGHT, Alignment.LEFT, Alignment.RIGHT, Alignment.RIGHT } : new Alignment[] { Alignment.RIGHT, Alignment.LEFT, Alignment.RIGHT };
                    
                    s[0][0] = "Sec1:";
                    s[0][1] = null;
                    if ( sec1 > 0f )
                        s[0][2] = TimingUtil.getTimeAsLaptimeString( sec1 );
                    else
                        s[0][2] = "-:--.---";
                    if ( dispGapToAbs )
                    {
                        if ( afSec1 < 0f )
                            s[0][3] = null;
                        else if ( sec1 > 0f )
                            s[0][3] = "(" + ( sec1 - afSec1 >= 0f ? "+" : "" ) + TimingUtil.getTimeAsLaptimeString( sec1 - afSec1 ) + ")";
                        else
                            s[0][32] = null;
                    }
                    
                    s[1][0] = "Sec2:";
                    s[1][1] = null;
                    if ( sec2 > 0f )
                        s[1][2] = TimingUtil.getTimeAsLaptimeString( sec2 );
                    else
                        s[1][2] = "-:--.---";
                    if ( dispGapToAbs )
                    {
                        if ( afSec2 < 0f )
                            s[1][3] = null;
                        else if ( sec2 > 0f )
                            s[1][3] = "(" + ( sec2 - afSec2 >= 0f ? "+" : "" ) + TimingUtil.getTimeAsLaptimeString( sec2 - afSec2 ) + ")";
                        else
                            s[1][3] = null;
                    }
                    
                    if ( !displayCumul )
                    {
                        s[2][0] = "Sec3:";
                        s[2][1] = null;
                        if ( sec3 > 0f )
                            s[2][2] = TimingUtil.getTimeAsLaptimeString( sec3 );
                        else
                            s[2][2] = "-:--.---";
                        if ( dispGapToAbs )
                        {
                            if ( afSec3 < 0f )
                                s[2][3] = null;
                            else if ( sec3 > 0f )
                                s[2][3] = "(" + ( sec3 - afSec3 >= 0f ? "+" : "" ) + TimingUtil.getTimeAsLaptimeString( sec3 - afSec3 ) + ")";
                            else
                                s[2][3] = null;
                        }
                    }
                    else
                    {
                        s[2][0] = null;
                        s[2][1] = null;
                        s[2][2] = "";
                        if ( vsi != afVSI )
                            s[2][3] = "";
                    }
                    
                    s[3][0] = "Lap:";
                    s[3][1] = null;
                    s[3][2] = TimingUtil.getTimeAsLaptimeString( lap );
                    if ( dispGapToAbs )
                        s[3][3] = "(" + ( lap - afLap >= 0f ? "+" : "" ) + TimingUtil.getTimeAsLaptimeString( lap - afLap ) + ")";
                    
                    ownSector1String.getMaxColWidths( s[0], aligns, padding, texture, colWidths );
                    ownSector2String.getMaxColWidths( s[1], aligns, padding, texture, colWidths );
                    if ( !displayCumul )
                        ownSector3String.getMaxColWidths( s[2], aligns, padding, texture, colWidths );
                    ownFastestLapString.getMaxColWidths( s[3], aligns, padding, texture, colWidths );
                    
                    ownSector1String.drawColumns( offsetX, offsetY, s[0], aligns, padding, colWidths, backgroundColor, texture );
                    ownSector2String.drawColumns( offsetX, offsetY, s[1], aligns, padding, colWidths, backgroundColor, texture );
                    if ( !displayCumul )
                        ownSector3String.drawColumns( offsetX, offsetY, s[2], aligns, padding, colWidths, backgroundColor, texture );
                    ownFastestLapString.drawColumns( offsetX, offsetY, s[3], aligns, padding, colWidths, backgroundColor, texture );
                }
                else
                {
                    ownSector1String.draw( offsetX, offsetY, "-:--.---", backgroundColor, texture );
                    ownSector2String.draw( offsetX, offsetY, "-:--.---", backgroundColor, texture );
                    if ( !cumulativeSectors.getBooleanValue() )
                        ownSector3String.draw( offsetX, offsetY, "-:--.---", backgroundColor, texture );
                    ownFastestLapString.draw( offsetX, offsetY, "-:--.---", backgroundColor, texture );
                }
            }
        }
        
        {
            // current lap
            
            if ( needsCompleteRedraw )
            {
                currLapHeaderString.draw( offsetX, offsetY, "Current Lap:", backgroundColor, texture );
            }
            
            final VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
            Laptime ownFastestLaptime = vsi.getFastestLaptime();
            //float lap = ( vsi.getStintLength() > 1f ) ? scoringInfo.getCurrentTime() - vsi.getLapStartTime() : -1f;
            float lap = scoringInfo.getSessionTime() - vsi.getLapStartTime();
            
            boolean lv = ( lap > 0f );
            if ( needsCompleteRedraw || clock1 || ( lv != currLapValid ) )
            //if ( needsCompleteRedraw || ( clock1 && ( lv != currLapValid ) ) )
            {
                currLapValid = lv;
                
                final boolean isDelaying = isEditorMode || ( scoringInfo.getSessionTime() <= lastLapDisplayTime );
                
                final boolean absFastestIsOwn = isDelaying ? delayedAbsFastestIsOwn : ( vsi == afVSI );
                final short sector = vsi.getSector();
                final boolean displayCumul = cumulativeSectors.getBooleanValue() || forceCurrentCumulSectors.getBooleanValue();
                
                float afSec1 = isDelaying ? ( delayedAbsFastestLap != null ? delayedAbsFastestLap.getSector1() : -1f ) : ( afLaptime != null ) ? afLaptime.getSector1(): -1f; //afVSI.getBestSector1();
                float afSec2 = isDelaying ? ( delayedAbsFastestLap != null ? delayedAbsFastestLap.getSector2( displayCumul ) : -1f ) : ( afLaptime != null ) ? afLaptime.getSector2( displayCumul ): -1f; //afVSI.getBestSector2( displayCumul );
                float afSec3 = isDelaying ? ( delayedAbsFastestLap != null ? delayedAbsFastestLap.getSector3() : -1f ) : ( displayCumul ? ( delayedAbsFastestLap != null ? afSec2 + delayedAbsFastestLap.getSector3() : -1f ) : ( afLaptime != null ? afLaptime.getSector3() : -1f ) );
                float afLap = isDelaying ? ( delayedAbsFastestLap != null ? delayedAbsFastestLap.getLapTime() : -1f ) : ( afLaptime != null ? afLaptime.getLapTime() : -1f );
                
                Laptime ofLaptime = vsi.getFastestLaptime();
                float ofSec1 = isDelaying ? ( delayedOwnFastestLap != null ? delayedOwnFastestLap.getSector1() : -1f ) : ( ownFastestLaptime != null ? ownFastestLaptime.getSector1() : -1f );
                float ofSec2 = isDelaying ? ( delayedOwnFastestLap != null ? delayedOwnFastestLap.getSector2( displayCumul ) : -1f ) : ( ownFastestLaptime != null ? ownFastestLaptime.getSector2( displayCumul ) : -1f );
                float ofSec3 = isDelaying ? ( delayedOwnFastestLap != null ? delayedOwnFastestLap.getSector3() : -1f ) : ( displayCumul ? ( delayedOwnFastestLap != null ? ofSec2 + delayedOwnFastestLap.getSector3() : -1f ) : ( ofLaptime != null ? ofLaptime.getSector3() : -1f ) );
                float ofLap = isDelaying ? ( delayedOwnFastestLap != null ? delayedOwnFastestLap.getLapTime() : -1f ) : ( ofLaptime != null ? ofLaptime.getLapTime() : -1f );
                
                float sec1 = isDelaying ? vsi.getLastSector1() : vsi.getCurrentSector1();
                float sec2 = isDelaying ? vsi.getLastSector2( displayCumul ) : vsi.getCurrentSector2( displayCumul );
                float sec3 = isDelaying ? vsi.getLastSector3() : ( ( sec2 > 0f ) ? ( displayCumul ? lap : lap - sec2 ) : -1f );
                
                if ( isEditorMode )
                {
                    afSec1 = 12.345f;
                    afSec2 = 35.226f;
                    afSec3 = 34.567f;
                    afLap = 82.138f;
                    
                    ofSec1 = 34.567f;
                    ofSec2 = 32.750f;
                    ofSec3 = 12.345f;
                    ofLap = 79.662f;
                    
                    //sec1 = 34.561f;
                    //sec2 = 32.552f;
                    //sec3 = 12.432f;
                    sec1 = editorPresets.getCurrentSector1Time();
                    sec2 = editorPresets.getCurrentSector2Time( false );
                    sec3 = editorPresets.getCurrentSector3Time();
                }
                
                if ( isDelaying )
                {
                    lap = vsi.getLastLapTime();
                }
                
                boolean afValid = afLap > 0f;
                boolean ofValid = ofLap > 0f;
                
                final boolean dispAbsFastest = displayAbsFastest.getBooleanValue();
                int cols = dispAbsFastest ? 5 : 4;
                String[][] s = new String[4][cols];
                int[] colWidths = new int[cols];
                final Alignment[] aligns = new Alignment[ cols ];
                aligns[0] = Alignment.RIGHT;
                aligns[1] = Alignment.LEFT;
                for ( int i = 2; i < aligns.length; i++ )
                    aligns[i] = Alignment.RIGHT;
                
                if ( !isDelaying || isEditorMode )
                {
                    gapOFSec1 = sec1 - ofSec1;
                    gapOFSec1Valid = ( ofSec1 > 0f );
                    gapOFSec2 = sec2 - ofSec2;
                    gapOFSec2Valid = ( ofSec2 > 0f );
                    gapOFSec3 = sec3 - ofSec3;
                    gapOFSec3Valid = ( ofSec3 > 0f );
                    gapOFLap = lap - ofLap;
                    gapAFSec1 = sec1 - afSec1;
                    gapAFSec1Valid = ( afSec1 > 0f );
                    gapAFSec2 = sec2 - afSec2;
                    gapAFSec2Valid = ( afSec2 > 0f );
                    gapAFSec3 = sec3 - afSec3;
                    gapAFSec3Valid = ( afSec3 > 0f );
                    gapAFLap = lap - afLap;
                    
                    delayedAbsFastestLap = afLaptime;
                    delayedOwnFastestLap = ofLaptime;
                    delayedAbsFastestIsOwn = ( afVSI == vsi );
                }
                else
                {
                    gapOFLap = lap - ofLap;
                    gapAFLap = lap - afLap;
                }
                
                java.awt.Color sfColor1a = getFontColor();
                java.awt.Color sfColor1b = getFontColor();
                s[0][0] = "Sec1:";
                s[0][1] = null;
                if ( sec1 > 0f )
                {
                    s[0][2] = TimingUtil.getTimeAsLaptimeString( sec1 );
                    if ( !isEditorMode && ( sector == 1 ) && !isDelaying )
                    {
                        if ( cols >= 4 )
                            s[0][3] = null;
                        if ( cols >= 5 )
                            s[0][4] = null;
                    }
                    else
                    {
                        if ( ofValid && gapOFSec1Valid )
                        {
                            sfColor1a = ( gapOFSec1 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                            s[0][3] = "(" + TimingUtil.getTimeAsGapString( gapOFSec1 ) + ")";
                        }
                        else
                            s[0][3] = "--.---";
                        
                        if ( dispAbsFastest )
                        {
                            if ( afValid && !absFastestIsOwn )
                                if ( gapAFSec1Valid )
                                {
                                    sfColor1b = ( gapAFSec1 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                    s[0][4] = "(" + TimingUtil.getTimeAsGapString( gapAFSec1 ) + ")";
                                }
                                else
                                    s[0][4] = "--.---";
                            else
                                s[0][4] = null;
                        }
                    }
                }
                else
                {
                    s[0][2] = "--.---";
                    if ( cols >= 4 )
                        s[0][3] = null;
                    if ( cols >= 5 )
                        s[0][4] = null;
                }
                
                java.awt.Color sfColor2a = getFontColor();
                java.awt.Color sfColor2b = getFontColor();
                s[1][0] = "Sec2:";
                s[1][1] = null;
                if ( sec2 > 0f )
                {
                    s[1][2] = TimingUtil.getTimeAsLaptimeString( sec2 );
                    if ( !isEditorMode && ( sector == 2 ) && !isDelaying )
                    {
                        if ( cols >= 4 )
                            s[1][3] = null;
                        if ( cols >= 5 )
                            s[1][4] = null;
                    }
                    else
                    {
                        if ( ofValid && gapOFSec2Valid )
                        {
                            sfColor2a = ( gapOFSec2 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                            s[1][3] = "(" + TimingUtil.getTimeAsGapString( gapOFSec2 ) + ")";
                        }
                        else
                            s[1][3] = "--.---";
                        
                        if ( dispAbsFastest )
                        {
                            if ( afValid && !absFastestIsOwn )
                                if ( gapAFSec2Valid )
                                {
                                    sfColor2b = ( gapAFSec2 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                    s[1][4] = "(" + TimingUtil.getTimeAsGapString( gapAFSec2 ) + ")";
                                }
                                else
                                    s[1][4] = "--.---";
                            else
                                s[1][4] = null;
                        }
                    }
                }
                else
                {
                    s[1][2] = "--.---";
                    if ( cols >= 4 )
                        s[1][3] = null;
                    if ( cols >= 5 )
                        s[1][4] = null;
                }
                
                java.awt.Color sfColor3a = getFontColor();
                java.awt.Color sfColor3b = getFontColor();
                s[2][0] = "Sec3:";
                s[2][1] = null;
                if ( !displayCumul )
                {
                    if ( sec3 > 0f )
                    {
                        s[2][2] = TimingUtil.getTimeAsLaptimeString( sec3 );
                        if ( !isEditorMode && ( sector == 3 ) && !isDelaying )
                        {
                            if ( cols >= 4 )
                                s[2][3] = null;
                            if ( cols >= 5 )
                                s[2][4] = null;
                        }
                        else
                        {
                            if ( ofValid && gapOFSec3Valid )
                            {
                                sfColor3a = ( gapOFSec3 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                s[2][3] = "(" + TimingUtil.getTimeAsGapString( gapOFSec3 ) + ")";
                            }
                            else
                                s[2][3] = "--.---";
                            
                            if ( dispAbsFastest )
                            {
                                if ( afValid && !absFastestIsOwn )
                                    if ( gapAFSec3Valid )
                                    {
                                        sfColor3b = ( gapAFSec3 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                        s[2][4] = "(" + TimingUtil.getTimeAsGapString( gapAFSec3 ) + ")";
                                    }
                                    else
                                        s[2][4] = "--.---";
                                else
                                    s[2][4] = null;
                            }
                        }
                    }
                    else
                    {
                        s[2][2] = "--.---";
                        if ( cols >= 4 )
                            s[2][3] = null;
                        if ( cols >= 5 )
                            s[2][4] = null;
                    }
                }
                else
                {
                    s[2][2] = null;
                    if ( cols >= 4 )
                        s[2][3] = null;
                    if ( cols >= 5 )
                        s[2][4] = null;
                }
                
                java.awt.Color sfColorLa = getFontColor();
                java.awt.Color sfColorLb = getFontColor();
                s[3][0] = "Lap:";
                s[3][1] = null;
                if ( isEditorMode || ( ( lap > 0f ) && ( vsi.getLapsCompleted() >= vsi.getStintStartLap() ) ) )
                {
                    s[3][2] = TimingUtil.getTimeAsLaptimeString( lap );
                    if ( isDelaying )
                    {
                        if ( ofValid )
                        {
                            sfColorLa = ( gapOFLap < 0f )? fasterColor.getColor() : slowerColor.getColor();
                            s[3][3] = "(" + TimingUtil.getTimeAsGapString( gapOFLap ) + ")";
                        }
                        else
                            s[3][3] = "--.---";
                        
                        if ( dispAbsFastest )
                        {
                            if ( afValid && !absFastestIsOwn )
                            {
                                sfColorLb = ( gapAFLap < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                s[3][4] = "(" + TimingUtil.getTimeAsGapString( gapAFLap ) + ")";
                            }
                            else
                                s[3][4] = null;
                        }
                    }
                    else
                    {
                        if ( cols >= 4 )
                            s[3][3] = null;
                        if ( cols >= 5 )
                            s[3][4] = null;
                    }
                }
                else
                {
                    s[3][2] = "-:--.---";
                    if ( cols >= 4 )
                        s[3][3] = null;
                    if ( cols >= 5 )
                        s[3][4] = null;
                }
                
                currSector1String.getMaxColWidths( s[0], aligns, padding, texture, colWidths );
                currSector2String.getMaxColWidths( s[1], aligns, padding, texture, colWidths );
                if ( !displayCumul )
                    currSector3String.getMaxColWidths( s[2], aligns, padding, texture, colWidths );
                
                String s31 = s[3][2];
                s[3][2] = TimingUtil.getTimeAsLaptimeString( 90.0f );
                currLapString.getMaxColWidths( s[3], aligns, padding, texture, colWidths );
                s[3][2] = s31;
                
                fontColors[3] = sfColor1a;
                fontColors[4] = sfColor1b;
                currSector1String.drawColumns( offsetX, offsetY, s[0], aligns, padding, colWidths, backgroundColor, fontColors, texture );
                
                fontColors[3] = sfColor2a;
                fontColors[4] = sfColor2b;
                currSector2String.drawColumns( offsetX, offsetY, s[1], aligns, padding, colWidths, backgroundColor, fontColors, texture );
                
                if ( !displayCumul )
                {
                    fontColors[3] = sfColor3a;
                    fontColors[4] = sfColor3b;
                    currSector3String.drawColumns( offsetX, offsetY, s[2], aligns, padding, colWidths, backgroundColor, fontColors, texture );
                }
                
                
                fontColors[3] = sfColorLa;
                fontColors[4] = sfColorLb;
                currLapString.drawColumns( offsetX, offsetY, s[3], aligns, padding, colWidths, backgroundColor, fontColors, texture );
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
        
        writer.writeProperty( displayAbsFastest, "Display the absolute fastest lap part of the Widget?" );
        writer.writeProperty( cumulativeSectors, "Display the second sector as a sum?" );
        writer.writeProperty( forceCurrentCumulSectors, "Display the second sector as a sum even if the others not?" );
        writer.writeProperty( lastLapDisplayDelay, "The time for which the last driven lap will keepbeing displayed (in milliseconds)." );
        writer.writeProperty( slowerColor, "The font color to use for positive gaps." );
        writer.writeProperty( fasterColor, "The font color to use for negative gaps." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( displayAbsFastest.loadProperty( key, value ) );
        else if ( cumulativeSectors.loadProperty( key, value ) );
        else if ( forceCurrentCumulSectors.loadProperty( key, value ) );
        else if ( lastLapDisplayDelay.loadProperty( key, value ) );
        else if ( slowerColor.loadProperty( key, value ) );
        else if ( fasterColor.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( displayAbsFastest );
        propsCont.addProperty( cumulativeSectors );
        propsCont.addProperty( forceCurrentCumulSectors );
        propsCont.addProperty( lastLapDisplayDelay );
        propsCont.addProperty( slowerColor );
        propsCont.addProperty( fasterColor );
    }
    
    public TimingWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.240625f, Size.PERCENT_OFFSET + 0.30083334f );
    }
}
