package net.ctdp.rfdynhud.widgets.timecomp;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link TimeCompareWidget} displays lap- and sector times of the last few laps to compare them.
 * 
 * @author Marvin Froehlich
 */
public class TimeCompareWidget extends Widget
{
    private final BooleanProperty abbreviate = new BooleanProperty( this, "abbreviate", false );
    private final BooleanProperty displaySectors = new BooleanProperty( this, "displaySectors", true );
    
    private DrawnString headerString = null;
    private DrawnString[] timeStrings = null;
    
    private final IntValue lap = new IntValue();
    
    private int numDisplayedLaps = 0;
    
    private static final Alignment[] colAligns = { Alignment.RIGHT, Alignment.RIGHT, Alignment.RIGHT, Alignment.RIGHT, Alignment.RIGHT };
    private static final int colPadding = 10;
    private int[] colWidths = null;
    
    @Override
    public String getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Object createLocalStore()
    {
        return ( new LocalStore() );
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
    
    private void updateLaps( VehicleScoringInfo vsi )
    {
        LocalStore store = (LocalStore)getLocalStore();
        
        int lap = vsi.getCurrentLap();
        
        int n = 0;
        float sumS1 = 0f;
        float sumS2 = 0f;
        float sumS3 = 0f;
        float sumL = 0f;
        for ( int i = lap - 1; i >= 1 && n < numDisplayedLaps; i-- )
        {
            Laptime lt = vsi.getLaptime( i );
            if ( ( lt != null ) && ( lt.isInlap() != Boolean.TRUE ) && ( lt.isOutlap() != Boolean.TRUE ) && ( lt.getLapTime() > 0f ) )
            {
                store.displayedLaps[numDisplayedLaps - n - 1] = lt;
                sumS1 += lt.getSector1();
                sumS2 += lt.getSector2();
                sumS3 += lt.getSector3();
                sumL += lt.getLapTime();
                
                n++;
            }
        }
        
        store.avgS1 = sumS1 / n;
        store.avgS2 = sumS2 / n;
        store.avgS3 = sumS3 / n;
        store.avgL = sumL / n;
        
        if ( n < numDisplayedLaps )
        {
            System.arraycopy( store.displayedLaps, numDisplayedLaps - n, store.displayedLaps, 0, n );
            
            for ( int i = n; i < numDisplayedLaps; i++ )
                store.displayedLaps[i] = null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onLapStarted( vsi, gameData, editorPresets );
        
        if ( vsi == gameData.getScoringInfo().getViewedVehicleScoringInfo() )
        {
            updateLaps( vsi );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, editorPresets );
        
        updateLaps( viewedVSI );
        forceCompleteRedraw();
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
        
        LocalStore store = (LocalStore)getLocalStore();
        
        headerString = dsf.newDrawnString( "headerString", 0, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        
        int h = height + getBorder().getInnerBottomHeight() - getBorder().getOpaqueBottomHeight();
        int rowHeight = headerString.getMaxHeight( texture, false );
        numDisplayedLaps = Math.max( 1, ( h - rowHeight - rowHeight - 5 ) / rowHeight );
        
        store.displayedLaps = new Laptime[ numDisplayedLaps ];
        int numStrings = displaySectors.getBooleanValue() ? 5 : 2;
        this.colWidths = new int[ numStrings ];
        
        this.timeStrings = new DrawnString[ numDisplayedLaps + 1 ];
        
        DrawnString relY = headerString;
        for ( int j = 0; j < numDisplayedLaps; j++ )
        {
            timeStrings[j] = dsf.newDrawnString( "timeStrings" + j, null, relY, 0, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            relY = timeStrings[j];
        }
        
        timeStrings[numDisplayedLaps] = dsf.newDrawnString( "timeStrings" + numDisplayedLaps, null, relY, 0, 5, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        
        if ( displaySectors.getBooleanValue() )
        {
            if ( abbreviate.getBooleanValue() )
                headerString.getMaxColWidths( new String[] { Loc.header_lap_number, Loc.header_sector1_short, Loc.header_sector2_short, Loc.header_sector3_short, Loc.header_lap_short }, colAligns, colPadding, texture, colWidths );
            else
                headerString.getMaxColWidths( new String[] { Loc.header_lap_number, Loc.header_sector1, Loc.header_sector2, Loc.header_sector3, Loc.header_lap }, colAligns, colPadding, texture, colWidths );
            timeStrings[0].getMaxColWidths( new String[] { "00", "-00.000", "-00.000", "-00.000", "-0:00.000" }, colAligns, colPadding, texture, colWidths );
        }
        else
        {
            if ( abbreviate.getBooleanValue() )
                headerString.getMaxColWidths( new String[] { Loc.header_lap_number, Loc.header_lap_short }, colAligns, colPadding, texture, colWidths );
            else
                headerString.getMaxColWidths( new String[] { Loc.header_lap_number, Loc.header_lap }, colAligns, colPadding, texture, colWidths );
            timeStrings[0].getMaxColWidths( new String[] { "00", "-00.000" }, colAligns, colPadding, texture, colWidths );
        }
        
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        if ( vsi.getCurrentLap() > 0 )
            updateLaps( vsi );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final java.awt.Color backgroundColor = getBackgroundColor();
        
        LocalStore store = (LocalStore)getLocalStore();
        
        int padding = 10;
        
        if ( needsCompleteRedraw )
        {
            if ( displaySectors.getBooleanValue() )
            {
                if ( abbreviate.getBooleanValue() )
                    headerString.drawColumns( offsetX, offsetY, new String[] { Loc.header_lap_number, Loc.header_sector1_short, Loc.header_sector2_short, Loc.header_sector3_short, Loc.header_lap_short }, colAligns, padding, colWidths, backgroundColor, texture );
                else
                    headerString.drawColumns( offsetX, offsetY, new String[] { Loc.header_lap_number, Loc.header_sector1, Loc.header_sector2, Loc.header_sector3, Loc.header_lap }, colAligns, padding, colWidths, backgroundColor, texture );
            }
            else
            {
                if ( abbreviate.getBooleanValue() )
                    headerString.drawColumns( offsetX, offsetY, new String[] { Loc.header_lap_number, Loc.header_lap_short }, colAligns, padding, colWidths, backgroundColor, texture );
                else
                    headerString.drawColumns( offsetX, offsetY, new String[] { Loc.header_lap_number, Loc.header_lap }, colAligns, padding, colWidths, backgroundColor, texture );
            }
        }
        
        lap.update( gameData.getScoringInfo().getViewedVehicleScoringInfo().getCurrentLap() );
        
        if ( needsCompleteRedraw || lap.hasChanged() )
        {
            int last = -1;
            for ( int i = 0; i < numDisplayedLaps; i++ )
            {
                String[] s;
                if ( store.displayedLaps[i] == null )
                {
                    if ( displaySectors.getBooleanValue() )
                        s = new String[] { "--", "--.---", "--.---", "--.---", "-:--.---" };
                    else
                        s = new String[] { "--", "-:--.---" };
                }
                else
                {
                    if ( displaySectors.getBooleanValue() )
                        s = new String[] { String.valueOf( store.displayedLaps[i].getLap() ), TimingUtil.getTimeAsString( store.displayedLaps[i].getSector1(), true ), TimingUtil.getTimeAsString( store.displayedLaps[i].getSector2(), true ), TimingUtil.getTimeAsString( store.displayedLaps[i].getSector3(), true ), TimingUtil.getTimeAsString( store.displayedLaps[i].getLapTime(), true ) };
                    else
                        s = new String[] { String.valueOf( store.displayedLaps[i].getLap() ), TimingUtil.getTimeAsString( store.displayedLaps[i].getLapTime(), true ) };
                    
                    last = i;
                }
                
                timeStrings[i].drawColumns( offsetX, offsetY, s, colAligns, padding, colWidths, backgroundColor, texture );
            }
            
            String[] s;
            if ( last < 0 )
            {
                if ( displaySectors.getBooleanValue() )
                    s = new String[] { Loc.footer_gap, "--.---", "--.---", "--.---", "-:--.---" };
                else
                    s = new String[] { Loc.footer_gap, "-:--.---" };
            }
            else
            {
                Laptime lt = store.displayedLaps[last];
                
                if ( displaySectors.getBooleanValue() )
                    s = new String[] { Loc.footer_gap, TimingUtil.getTimeAsGapString( lt.getSector1() - store.avgS1 ), TimingUtil.getTimeAsGapString( lt.getSector2() - store.avgS2 ), TimingUtil.getTimeAsGapString( lt.getSector3() - store.avgS3 ), TimingUtil.getTimeAsGapString( lt.getLapTime() - store.avgL ) };
                else
                    s = new String[] { Loc.footer_gap, TimingUtil.getTimeAsGapString( lt.getLapTime() - store.avgL ) };
            }
            
            timeStrings[numDisplayedLaps].drawColumns( offsetX, offsetY, s, colAligns, padding, colWidths, backgroundColor, texture );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( abbreviate, "Whether to abbreviate \"Sector\" to \"Sec\", or not." );
        writer.writeProperty( displaySectors, "Display sector times?" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( abbreviate.loadProperty( key, value ) );
        else if ( displaySectors.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( abbreviate );
        propsCont.addProperty( displaySectors );
    }
    
    public TimeCompareWidget( String name )
    {
        super( name, 24.0625f, 13.916667f );
    }
}
