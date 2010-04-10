package net.ctdp.rfdynhud.widgets.timing;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.editor.properties.BooleanProperty;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.IntegerProperty;
import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.TimingUtil;
import net.ctdp.rfdynhud.widgets._util.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
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
    private final IntegerProperty lastLapDisplayDelay = new IntegerProperty( this, "lastLapDisplayDelay", "lastLapDisplayDelay", 10000, -100, Integer.MAX_VALUE, false ); // ten seconds
    
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
    
    private final java.awt.Color[] fontColors = new java.awt.Color[ 4 ];
    
    private Laptime delayedAbsFastestLap = null;
    private Laptime delayedOwnFastestLap = null;
    
    private boolean delayedAbsFastestIsOwn = false;
    
    @Override
    public String getWidgetPackage()
    {
        return ( "" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( boolean isEditorMode, SessionType sessionType, LiveGameData gameData )
    {
        super.onSessionStarted( isEditorMode, sessionType, gameData );
        
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
    public void onRealtimeEntered( boolean isEditorMode, LiveGameData gameData )
    {
        super.onRealtimeEntered( isEditorMode, gameData );
        
        currLapValid = false;
        
        lastLapDisplayTime = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPitsExited( LiveGameData gameData )
    {
        super.onPitsExited( gameData );
        
        currLapValid = false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayerLapStarted( boolean isEditorMode, LiveGameData gameData )
    {
        super.onPlayerLapStarted( isEditorMode, gameData );
        
        VehicleScoringInfo vsi = gameData.getScoringInfo().getPlayersVehicleScoringInfo();
        
        if ( vsi.getStintLength() < 1.9f )
            lastLapDisplayTime = -1f;
        else if ( lastLapDisplayDelay.getIntegerValue() < 0 )
            lastLapDisplayTime = vsi.getLapStartTime() + ( vsi.getLaptime( vsi.getLapsCompleted() ).getSector1() * -lastLapDisplayDelay.getIntegerValue() / 100f );
        else
            lastLapDisplayTime = vsi.getLapStartTime() + ( lastLapDisplayDelay.getIntegerValue() / 1000f );
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
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
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
            absFastestLapHeaderString = new DrawnString( left1, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            absFastestLapDriverString = new DrawnString( null, absFastestLapHeaderString, left2, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Driver: ", null );
            absSector1String = new DrawnString( null, absFastestLapDriverString, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Sec1: ", null );
            absSector2String = new DrawnString( null, absSector1String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Sec2: ", null );
            if ( !cumulativeSectors.getBooleanValue() )
            {
                absSector3String = new DrawnString( null, absSector2String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Sec3: ", null );
                yRel = absSector3String;
            }
            else
            {
                absSector3String = null;
                yRel = absSector2String;
            }
            absFastestLapString = new DrawnString( null, yRel, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, " Lap: ", null );
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
        
        ownFastestLapHeaderString = new DrawnString( null, yRel, left1, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        ownSector1String = new DrawnString( null, ownFastestLapHeaderString, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Sec1: ", null );
        ownSector2String = new DrawnString( null, ownSector1String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Sec2: ", null );
        if ( !cumulativeSectors.getBooleanValue() )
        {
            ownSector3String = new DrawnString( null, ownSector2String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Sec3: ", null );
            yRel = ownSector3String;
        }
        else
        {
            ownSector3String = null;
            yRel = ownSector2String;
        }
        ownFastestLapString = new DrawnString( null, yRel, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, " Lap: ", null );
        
        currLapHeaderString = new DrawnString( null, ownFastestLapString, left1, sectionGap, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        currSector1String = new DrawnString( null, currLapHeaderString, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Sec1: ", null );
        currSector2String = new DrawnString( null, currSector1String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Sec2: ", null );
        if ( !cumulativeSectors.getBooleanValue() && !forceCurrentCumulSectors.getBooleanValue() )
        {
            currSector3String = new DrawnString( null, currSector2String, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Sec3: ", null );
            yRel = currSector3String;
        }
        else
        {
            currSector3String = null;
            yRel = currSector2String;
        }
        currLapString = new DrawnString( null, yRel, left2, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, " Lap: ", null );
        
        fontColors[0] = getFontColor();
        fontColors[1] = getFontColor();
        fontColors[2] = getFontColor();
        fontColors[3] = getFontColor();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        final TextureImage2D image = texCanvas.getImage();
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
                    absFastestLapHeaderString.draw( offsetX, offsetY, "Abs. second fastest Lap:", backgroundColor, image );
                else
                    absFastestLapHeaderString.draw( offsetX, offsetY, "Abs. fastest Lap:", backgroundColor, image );
                absFastestLapDriverString.draw( offsetX, offsetY, leader, backgroundColor, image );
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
                    
                    int cols = 2;
                    String[][] s = new String[4][cols];
                    int[] colWidths = new int[cols];
                    Alignment[] aligns = { Alignment.LEFT, Alignment.RIGHT };
                    int padding = 10;
                    
                    s[0][0] = null;
                    if ( sec1 > 0f )
                        s[0][1] = TimingUtil.getTimeAsString( sec1, false, false, true );
                    else
                        s[0][1] = "-:--.---";
                    
                    s[1][0] = null;
                    if ( sec2 > 0f )
                        s[1][1] = TimingUtil.getTimeAsString( sec2, false, false, true );
                    else
                        s[1][1] = "-:--.---";
                    
                    if ( !cumulativeSectors.getBooleanValue() )
                    {
                        s[2][0] = null;
                        if ( sec3 > 0f )
                            s[2][1] = TimingUtil.getTimeAsString( sec3, false, false, true );
                        else
                            s[2][1] = "-:--.---";
                    }
                    else
                    {
                        s[2][0] = null;
                        s[2][1] = "";
                    }
                    
                    s[3][0] = null;
                    s[3][1] = TimingUtil.getTimeAsString( lap, false, false, true );
                    
                    absSector1String.getMaxColWidths( s[0], padding, image, colWidths );
                    absSector2String.getMaxColWidths( s[1], padding, image, colWidths );
                    if ( !cumulativeSectors.getBooleanValue() )
                        absSector3String.getMaxColWidths( s[2], padding, image, colWidths );
                    absFastestLapString.getMaxColWidths( s[3], padding, image, colWidths );
                    colWidths[0] -= padding;
                    
                    absSector1String.drawColumns( offsetX, offsetY, s[0], aligns, padding, colWidths, backgroundColor, image );
                    absSector2String.drawColumns( offsetX, offsetY, s[1], aligns, padding, colWidths, backgroundColor, image );
                    if ( !cumulativeSectors.getBooleanValue() )
                        absSector3String.drawColumns( offsetX, offsetY, s[2], aligns, padding, colWidths, backgroundColor, image );
                    absFastestLapString.drawColumns( offsetX, offsetY, s[3], aligns, padding, colWidths, backgroundColor, image );
                }
                else
                {
                    absSector1String.draw( offsetX, offsetY, "-:--.---", backgroundColor, image );
                    absSector2String.draw( offsetX, offsetY, "-:--.---", backgroundColor, image );
                    if ( !cumulativeSectors.getBooleanValue() )
                        absSector3String.draw( offsetX, offsetY, "-:--.---", backgroundColor, image );
                    absFastestLapString.draw( offsetX, offsetY, "-:--.---", backgroundColor, image );
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
                ownFastestLapHeaderString.draw( offsetX, offsetY, "Own fastest Lap:", backgroundColor, image );
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
                    int cols = dispGapToAbs ? 3 : 2;
                    String[][] s = new String[4][cols];
                    int[] colWidths = new int[cols];
                    Alignment[] aligns = ( vsi != afVSI ) ? new Alignment[] { Alignment.LEFT, Alignment.RIGHT, Alignment.RIGHT } : new Alignment[] { Alignment.LEFT, Alignment.RIGHT };
                    int padding = 10;
                    
                    s[0][0] = null;
                    if ( sec1 > 0f )
                        s[0][1] = TimingUtil.getTimeAsString( sec1, false, false, true );
                    else
                        s[0][1] = "-:--.---";
                    if ( dispGapToAbs )
                    {
                        if ( afSec1 < 0f )
                            s[0][2] = null;
                        else if ( sec1 > 0f )
                            s[0][2] = "(" + ( sec1 - afSec1 >= 0f ? "+" : "" ) + TimingUtil.getTimeAsString( sec1 - afSec1, false, false, true ) + ")";
                        else
                            s[0][2] = null;
                    }
                    
                    s[1][0] = null;
                    if ( sec2 > 0f )
                        s[1][1] = TimingUtil.getTimeAsString( sec2, false, false, true );
                    else
                        s[1][1] = "-:--.---";
                    if ( dispGapToAbs )
                    {
                        if ( afSec2 < 0f )
                            s[1][2] = null;
                        else if ( sec2 > 0f )
                            s[1][2] = "(" + ( sec2 - afSec2 >= 0f ? "+" : "" ) + TimingUtil.getTimeAsString( sec2 - afSec2, false, false, true ) + ")";
                        else
                            s[1][2] = null;
                    }
                    
                    if ( !displayCumul )
                    {
                        s[2][0] = null;
                        if ( sec3 > 0f )
                            s[2][1] = TimingUtil.getTimeAsString( sec3, false, false, true );
                        else
                            s[2][1] = "-:--.---";
                        if ( dispGapToAbs )
                        {
                            if ( afSec3 < 0f )
                                s[2][2] = null;
                            else if ( sec3 > 0f )
                                s[2][2] = "(" + ( sec3 - afSec3 >= 0f ? "+" : "" ) + TimingUtil.getTimeAsString( sec3 - afSec3, false, false, true ) + ")";
                            else
                                s[2][2] = null;
                        }
                    }
                    else
                    {
                        s[2][0] = null;
                        s[2][1] = "";
                        if ( vsi != afVSI )
                            s[2][2] = "";
                    }
                    
                    s[3][0] = null;
                    s[3][1] = TimingUtil.getTimeAsString( lap, false, false, true );
                    if ( dispGapToAbs )
                        s[3][2] = "(" + ( lap - afLap >= 0f ? "+" : "" ) + TimingUtil.getTimeAsString( lap - afLap, false, false, true ) + ")";
                    
                    ownSector1String.getMaxColWidths( s[0], padding, image, colWidths );
                    ownSector2String.getMaxColWidths( s[1], padding, image, colWidths );
                    if ( !displayCumul )
                        ownSector3String.getMaxColWidths( s[2], padding, image, colWidths );
                    ownFastestLapString.getMaxColWidths( s[3], padding, image, colWidths );
                    colWidths[0] -= padding;
                    
                    ownSector1String.drawColumns( offsetX, offsetY, s[0], aligns, padding, colWidths, backgroundColor, image );
                    ownSector2String.drawColumns( offsetX, offsetY, s[1], aligns, padding, colWidths, backgroundColor, image );
                    if ( !displayCumul )
                        ownSector3String.drawColumns( offsetX, offsetY, s[2], aligns, padding, colWidths, backgroundColor, image );
                    ownFastestLapString.drawColumns( offsetX, offsetY, s[3], aligns, padding, colWidths, backgroundColor, image );
                }
                else
                {
                    ownSector1String.draw( offsetX, offsetY, "-:--.---", backgroundColor, image );
                    ownSector2String.draw( offsetX, offsetY, "-:--.---", backgroundColor, image );
                    if ( !cumulativeSectors.getBooleanValue() )
                        ownSector3String.draw( offsetX, offsetY, "-:--.---", backgroundColor, image );
                    ownFastestLapString.draw( offsetX, offsetY, "-:--.---", backgroundColor, image );
                }
            }
        }
        
        {
            // current lap
            
            if ( needsCompleteRedraw )
            {
                currLapHeaderString.draw( offsetX, offsetY, "Current Lap:", backgroundColor, image );
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
                    
                    sec1 = 34.561f;
                    sec2 = 32.552f;
                    sec3 = 12.432f;
                }
                
                if ( isDelaying )
                {
                    lap = vsi.getLastLapTime();
                }
                
                boolean afValid = afLap > 0f;
                boolean ofValid = ofLap > 0f;
                
                final boolean dispAbsFastest = displayAbsFastest.getBooleanValue();
                int cols = dispAbsFastest ? 4 : 3;
                String[][] s = new String[4][cols];
                int[] colWidths = new int[cols];
                Alignment[] aligns = new Alignment[ cols ];
                aligns[0] = Alignment.LEFT;
                for ( int i = 1; i < aligns.length; i++ )
                    aligns[i] = Alignment.RIGHT;
                int padding = 10;
                
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
                s[0][0] = null;
                if ( sec1 > 0f )
                {
                    s[0][1] = TimingUtil.getTimeAsString( sec1, false, false, true );
                    if ( !isEditorMode && ( sector == 1 ) && !isDelaying )
                    {
                        if ( cols >= 3 )
                            s[0][2] = null;
                        if ( cols >= 4 )
                            s[0][3] = null;
                    }
                    else
                    {
                        if ( ofValid && gapOFSec1Valid )
                        {
                            sfColor1a = ( gapOFSec1 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                            s[0][2] = "(" + TimingUtil.getTimeAsGapString( gapOFSec1 ) + ")";
                        }
                        else
                            s[0][2] = "--.---";
                        
                        if ( dispAbsFastest )
                        {
                            if ( afValid && !absFastestIsOwn )
                                if ( gapAFSec1Valid )
                                {
                                    sfColor1b = ( gapAFSec1 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                    s[0][3] = "(" + TimingUtil.getTimeAsGapString( gapAFSec1 ) + ")";
                                }
                                else
                                    s[0][3] = "--.---";
                            else
                                s[0][3] = null;
                        }
                    }
                }
                else
                {
                    s[0][1] = "--.---";
                    if ( cols >= 3 )
                        s[0][2] = null;
                    if ( cols >= 4 )
                        s[0][3] = null;
                }
                
                java.awt.Color sfColor2a = getFontColor();
                java.awt.Color sfColor2b = getFontColor();
                s[1][0] = null;
                if ( sec2 > 0f )
                {
                    s[1][1] = TimingUtil.getTimeAsString( sec2, false, false, true );
                    if ( !isEditorMode && ( sector == 2 ) && !isDelaying )
                    {
                        if ( cols >= 3 )
                            s[1][2] = null;
                        if ( cols >= 4 )
                            s[1][3] = null;
                    }
                    else
                    {
                        if ( ofValid && gapOFSec2Valid )
                        {
                            sfColor2a = ( gapOFSec2 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                            s[1][2] = "(" + TimingUtil.getTimeAsGapString( gapOFSec2 ) + ")";
                        }
                        else
                            s[1][2] = "--.---";
                        
                        if ( dispAbsFastest )
                        {
                            if ( afValid && !absFastestIsOwn )
                                if ( gapAFSec2Valid )
                                {
                                    sfColor2b = ( gapAFSec2 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                    s[1][3] = "(" + TimingUtil.getTimeAsGapString( gapAFSec2 ) + ")";
                                }
                                else
                                    s[1][3] = "--.---";
                            else
                                s[1][3] = null;
                        }
                    }
                }
                else
                {
                    s[1][1] = "--.---";
                    if ( cols >= 3 )
                        s[1][2] = null;
                    if ( cols >= 4 )
                        s[1][3] = null;
                }
                
                java.awt.Color sfColor3a = getFontColor();
                java.awt.Color sfColor3b = getFontColor();
                s[2][0] = null;
                if ( !displayCumul )
                {
                    s[2][0] = null;
                    if ( sec3 > 0f )
                    {
                        s[2][1] = TimingUtil.getTimeAsString( sec3, false, false, true );
                        if ( !isEditorMode && ( sector == 3 ) && !isDelaying )
                        {
                            if ( cols >= 3 )
                                s[2][2] = null;
                            if ( cols >= 4 )
                                s[2][3] = null;
                        }
                        else
                        {
                            if ( ofValid && gapOFSec3Valid )
                            {
                                sfColor3a = ( gapOFSec3 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                s[2][2] = "(" + TimingUtil.getTimeAsGapString( gapOFSec3 ) + ")";
                            }
                            else
                                s[2][2] = "--.---";
                            
                            if ( dispAbsFastest )
                            {
                                if ( afValid && !absFastestIsOwn )
                                    if ( gapAFSec3Valid )
                                    {
                                        sfColor3b = ( gapAFSec3 < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                        s[2][3] = "(" + TimingUtil.getTimeAsGapString( gapAFSec3 ) + ")";
                                    }
                                    else
                                        s[2][3] = "--.---";
                                else
                                    s[2][3] = null;
                            }
                        }
                    }
                    else
                    {
                        s[2][1] = "--.---";
                        if ( cols >= 3 )
                            s[2][2] = null;
                        if ( cols >= 4 )
                            s[2][3] = null;
                    }
                }
                else
                {
                    s[2][1] = null;
                    if ( cols >= 3 )
                        s[2][2] = null;
                    if ( cols >= 4 )
                        s[2][3] = null;
                }
                
                java.awt.Color sfColorLa = getFontColor();
                java.awt.Color sfColorLb = getFontColor();
                s[3][0] = null;
                if ( isEditorMode || ( ( lap > 0f ) && ( vsi.getLapsCompleted() >= vsi.getStintStartLap() ) ) )
                {
                    s[3][1] = TimingUtil.getTimeAsString( lap, false, false, true );
                    if ( isDelaying )
                    {
                        if ( ofValid )
                        {
                            sfColorLa = ( gapOFLap < 0f )? fasterColor.getColor() : slowerColor.getColor();
                            s[3][2] = "(" + TimingUtil.getTimeAsGapString( gapOFLap ) + ")";
                        }
                        else
                            s[3][2] = "--.---";
                        
                        if ( dispAbsFastest )
                        {
                            if ( afValid && !absFastestIsOwn )
                            {
                                sfColorLb = ( gapAFLap < 0f )? fasterColor.getColor() : slowerColor.getColor();
                                s[3][3] = "(" + TimingUtil.getTimeAsGapString( gapAFLap ) + ")";
                            }
                            else
                                s[3][3] = null;
                        }
                    }
                    else
                    {
                        if ( cols >= 3 )
                            s[3][2] = null;
                        if ( cols >= 4 )
                            s[3][3] = null;
                    }
                }
                else
                {
                    s[3][1] = "-:--.---";
                    if ( cols >= 3 )
                        s[3][2] = null;
                    if ( cols >= 4 )
                        s[3][3] = null;
                }
                
                currSector1String.getMaxColWidths( s[0], padding, image, colWidths );
                currSector2String.getMaxColWidths( s[1], padding, image, colWidths );
                if ( !displayCumul )
                    currSector3String.getMaxColWidths( s[2], padding, image, colWidths );
                currLapString.getMaxColWidths( s[3], padding, image, colWidths );
                colWidths[0] -= padding;
                
                fontColors[2] = sfColor1a;
                fontColors[3] = sfColor1b;
                currSector1String.drawColumns( offsetX, offsetY, s[0], aligns, padding, colWidths, backgroundColor, fontColors, image );
                
                fontColors[2] = sfColor2a;
                fontColors[3] = sfColor2b;
                currSector2String.drawColumns( offsetX, offsetY, s[1], aligns, padding, colWidths, backgroundColor, fontColors, image );
                
                if ( !displayCumul )
                {
                    fontColors[2] = sfColor3a;
                    fontColors[3] = sfColor3b;
                    currSector3String.drawColumns( offsetX, offsetY, s[2], aligns, padding, colWidths, backgroundColor, fontColors, image );
                }
                
                
                fontColors[2] = sfColorLa;
                fontColors[3] = sfColorLb;
                currLapString.drawColumns( offsetX, offsetY, s[3], aligns, padding, colWidths, backgroundColor, fontColors, image );
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
    public void getProperties( WidgetPropertiesContainer propsCont )
    {
        super.getProperties( propsCont );
        
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
