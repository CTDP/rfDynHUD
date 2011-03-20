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
package net.ctdp.rfdynhud.widgets.standard.timecomp;

import java.awt.Font;
import java.io.IOException;
import java.util.Arrays;

import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.StatefulWidget;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link TimeCompareWidget} displays lap- and sector times of the last few laps to compare them.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TimeCompareWidget extends StatefulWidget<Object, LocalStore>
{
    private final BooleanProperty abbreviate = new BooleanProperty( "abbreviate", false );
    private final BooleanProperty displaySectors = new BooleanProperty( "displaySectors", true );
    
    private DrawnString headerString = null;
    private DrawnString[] timeStrings = null;
    
    private final IntValue lap = new IntValue();
    
    private int numDisplayedLaps = 0;
    
    private static final Alignment[] colAligns = { Alignment.RIGHT, Alignment.RIGHT, Alignment.RIGHT, Alignment.RIGHT, Alignment.RIGHT };
    private int colPadding = 10;
    private int[] colWidths = null;
    
    public TimeCompareWidget()
    {
        super( StandardWidgetSet.INSTANCE, StandardWidgetSet.WIDGET_PACKAGE_TIMING, 24.0625f, 13.916667f );
    }
    
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        colPadding = 4;
        getFontProperty().setFont( "Dialog", Font.PLAIN, 3, false, true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( abbreviate, "Whether to abbreviate \"Sector\" to \"Sec\", or not." );
        writer.writeProperty( displaySectors, "Display sector times?" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( abbreviate ) );
        else if ( loader.loadProperty( displaySectors ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Misc" );
        
        propsCont.addProperty( abbreviate );
        propsCont.addProperty( displaySectors );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object createGeneralStore()
    {
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected LocalStore createLocalStore()
    {
        return ( new LocalStore() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, boolean isEditorMode )
    {
        super.onSessionStarted( sessionType, gameData, isEditorMode );
        
        lap.reset();
    }
    
    private void updateLaps( VehicleScoringInfo vsi )
    {
        LocalStore store = getLocalStore();
        
        int lap = vsi.getCurrentLap();
        
        int n = 0;
        for ( int i = lap - 1; i >= 1 && n < numDisplayedLaps; i-- )
        {
            Laptime lt = vsi.getLaptime( i );
            if ( ( lt != null ) && ( lt.isInlap() != Boolean.TRUE ) && ( lt.isOutlap() != Boolean.TRUE ) && ( lt.getLapTime() > 0f ) )
            {
                store.displayedLaps[numDisplayedLaps - n - 1] = lt;
                n++;
            }
        }
        
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
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, boolean isEditorMode )
    {
        super.onLapStarted( vsi, gameData, isEditorMode );
        
        if ( vsi == gameData.getScoringInfo().getViewedVehicleScoringInfo() )
        {
            updateLaps( vsi );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        updateLaps( viewedVSI );
        forceCompleteRedraw( false );
        
        return ( result );
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
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        final java.awt.Font font = getFont();
        final boolean fontAntiAliased = isFontAntiAliased();
        final java.awt.Color fontColor = getFontColor();
        
        LocalStore store = getLocalStore();
        
        headerString = dsf.newDrawnString( "headerString", 0, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        
        int h = height + getBorder().getInnerBottomHeight() - getBorder().getOpaqueBottomHeight();
        int rowHeight = headerString.calcMaxHeight( false );
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
        
        Arrays.fill( colWidths, 0 );
        int totalWidth;
        
        if ( displaySectors.getBooleanValue() )
        {
            if ( abbreviate.getBooleanValue() )
                headerString.getMaxColWidths( new String[] { Loc.header_lap_number, Loc.header_sector1_short, Loc.header_sector2_short, Loc.header_sector3_short, Loc.header_lap_short }, colAligns, colPadding, colWidths );
            else
                headerString.getMaxColWidths( new String[] { Loc.header_lap_number, Loc.header_sector1, Loc.header_sector2, Loc.header_sector3, Loc.header_lap }, colAligns, colPadding, colWidths );
            totalWidth = timeStrings[0].getMaxColWidths( new String[] { "00", "-00.000", "-00.000", "-00.000", "-0:00.000" }, colAligns, colPadding, colWidths );
        }
        else
        {
            if ( abbreviate.getBooleanValue() )
                headerString.getMaxColWidths( new String[] { Loc.header_lap_number, Loc.header_lap_short }, colAligns, colPadding, colWidths );
            else
                headerString.getMaxColWidths( new String[] { Loc.header_lap_number, Loc.header_lap }, colAligns, colPadding, colWidths );
            totalWidth = timeStrings[0].getMaxColWidths( new String[] { "00", "-0:00.000" }, colAligns, colPadding, colWidths );
        }
        
        if ( totalWidth < width )
        {
            int rest = width - totalWidth;
            
            if ( displaySectors.getBooleanValue() )
            {
                colWidths[2] += rest / 3;
                rest -= rest / 3;
                colWidths[3] += rest / 2;
                rest -= rest / 2;
                colWidths[4] += rest;
            }
        }
        
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        if ( vsi.getCurrentLap() > 0 )
            updateLaps( vsi );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        LocalStore store = getLocalStore();
        
        if ( needsCompleteRedraw )
        {
            if ( displaySectors.getBooleanValue() )
            {
                if ( abbreviate.getBooleanValue() )
                    headerString.drawColumns( offsetX, offsetY, new String[] { Loc.header_lap_number, Loc.header_sector1_short, Loc.header_sector2_short, Loc.header_sector3_short, Loc.header_lap_short }, colAligns, colPadding, colWidths, texture );
                else
                    headerString.drawColumns( offsetX, offsetY, new String[] { Loc.header_lap_number, Loc.header_sector1, Loc.header_sector2, Loc.header_sector3, Loc.header_lap }, colAligns, colPadding, colWidths, texture );
            }
            else
            {
                if ( abbreviate.getBooleanValue() )
                    headerString.drawColumns( offsetX, offsetY, new String[] { Loc.header_lap_number, Loc.header_lap_short }, colAligns, colPadding, colWidths, texture );
                else
                    headerString.drawColumns( offsetX, offsetY, new String[] { Loc.header_lap_number, Loc.header_lap }, colAligns, colPadding, colWidths, texture );
            }
        }
        
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
        lap.update( vsi.getCurrentLap() );
        
        if ( needsCompleteRedraw || lap.hasChanged() )
        {
            int lastDspIdx = -1;
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
                        s = new String[] { String.valueOf( store.displayedLaps[i].getLap() ), TimingUtil.getTimeAsString( store.displayedLaps[i].getSector1(), false, false, true ), TimingUtil.getTimeAsString( store.displayedLaps[i].getSector2(), false, false, true ), TimingUtil.getTimeAsString( store.displayedLaps[i].getSector3(), false, false, true ), TimingUtil.getTimeAsString( store.displayedLaps[i].getLapTime(), false, false, true ) };
                    else
                        s = new String[] { String.valueOf( store.displayedLaps[i].getLap() ), TimingUtil.getTimeAsString( store.displayedLaps[i].getLapTime(), false, false, true ) };
                    
                    lastDspIdx = i;
                }
                
                timeStrings[i].drawColumns( offsetX, offsetY, s, colAligns, colPadding, colWidths, texture );
            }
            
            Laptime lt = ( lastDspIdx >= 0 ) ? store.displayedLaps[lastDspIdx] : null;
            Laptime lastDriven = vsi.getLaptime( vsi.getLapsCompleted() );
            Laptime avgLaptime = vsi.getOldAverageLaptime();
            
            String[] s;
            if ( ( lt != null ) && ( lastDriven != null ) && ( lastDriven.isOutlap() == Boolean.FALSE ) && ( avgLaptime != null ) )
            {
                if ( displaySectors.getBooleanValue() )
                    s = new String[] { Loc.footer_gap, TimingUtil.getTimeAsGapString( lt.getSector1() - avgLaptime.getSector1() ), TimingUtil.getTimeAsGapString( lt.getSector2() - avgLaptime.getSector2() ), TimingUtil.getTimeAsGapString( lt.getSector3() - avgLaptime.getSector3() ), TimingUtil.getTimeAsGapString( lt.getLapTime() - avgLaptime.getLapTime() ) };
                else
                    s = new String[] { Loc.footer_gap, TimingUtil.getTimeAsGapString( lt.getLapTime() - avgLaptime.getLapTime() ) };
            }
            else
            {
                if ( displaySectors.getBooleanValue() )
                    s = new String[] { Loc.footer_gap, "--.---", "--.---", "--.---", "-:--.---" };
                else
                    s = new String[] { Loc.footer_gap, "-:--.---" };
            }
            
            timeStrings[numDisplayedLaps].drawColumns( offsetX, offsetY, s, colAligns, colPadding, colWidths, texture );
        }
    }
}
