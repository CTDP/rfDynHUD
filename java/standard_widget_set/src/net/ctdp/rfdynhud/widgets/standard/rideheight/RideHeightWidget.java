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
package net.ctdp.rfdynhud.widgets.standard.rideheight;

import java.awt.Font;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link RideHeightWidget} indicates, when your car hits the road.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class RideHeightWidget extends Widget
{
    private final BooleanProperty displayHeader = new BooleanProperty( this, "displayHeader", true );
    
    private final FontProperty headerFont = new FontProperty( this, "headerFont", "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty headerFontColor = new ColorProperty( this, "headerFontColor", "fontColor", ColorProperty.STANDARD_FONT_COLOR_NAME );
    
    private float minFL = Float.MAX_VALUE;
    private float minFR = Float.MAX_VALUE;
    private float minRL = Float.MAX_VALUE;
    private float minRR = Float.MAX_VALUE;
    
    private long t0 = -1;
    
    private DrawnString headerString = null;
    private DrawnString flString = null;
    private DrawnString frString = null;
    private DrawnString rlString = null;
    private DrawnString rrString = null;
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE_TELEMETRY );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        t0 = gameData.getScoringInfo().getSessionNanos();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        return ( viewedVSI.getVehicleControl().isLocalPlayer() );
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
        headerString = dsf.newDrawnStringIf( displayHeader.getBooleanValue(), "headerString", 0, 0, Alignment.LEFT, false, headerFont.getFont(), headerFont.isAntiAliased(), headerFontColor.getColor() );
        
        int y0 = displayHeader.getBooleanValue() ? headerString.calcMaxHeight( true ) : 0;
        int h = (int)TextureImage2D.getStringBounds( "0.0", getFontProperty() ).getHeight();
        
        String units = Loc.units_METRIC;
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            units = Loc.units_IMPERIAL;
        
        flString = dsf.newDrawnString( "flString", 0, y0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), null, units );
        frString = dsf.newDrawnString( "frString", width, y0, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor(), null, units );
        rlString = dsf.newDrawnString( "rlString", 0, height - h, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), null, units );
        rrString = dsf.newDrawnString( "rrString", width, height - h, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor(), null, units );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw && displayHeader.getBooleanValue() )
        {
            headerString.draw( offsetX, offsetY, Loc.header, texture );
        }
        
        final TelemetryData telemData = gameData.getTelemetryData();
        
        minFL = Math.min( minFL, telemData.getRideHeight( Wheel.FRONT_LEFT ) );
        minFR = Math.min( minFR, telemData.getRideHeight( Wheel.FRONT_RIGHT ) );
        minRL = Math.min( minRL, telemData.getRideHeight( Wheel.REAR_LEFT ) );
        minRR = Math.min( minRR, telemData.getRideHeight( Wheel.REAR_RIGHT ) );
        
        boolean c2 = gameData.getScoringInfo().getSessionNanos() - t0 > 1000000000L;
        
        if ( needsCompleteRedraw || c2 )
        {
            minFL = Math.max( 0f, minFL * 100f );
            minFR = Math.max( 0f, minFR * 100f );
            minRL = Math.max( 0f, minRL * 100f );
            minRR = Math.max( 0f, minRR * 100f );
            
            if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            {
                minFL /= 2.54f;
                minFR /= 2.54f;
                minRL /= 2.54f;
                minRR /= 2.54f;
            }
            
            flString.draw( offsetX, offsetY, NumberUtil.formatFloat( minFL, 1, true ), ( minFL <= 0f ? java.awt.Color.RED : null ), texture );
            frString.draw( offsetX, offsetY, NumberUtil.formatFloat( minFR, 1, true ), ( minFR <= 0f ? java.awt.Color.RED : null ), texture );
            rlString.draw( offsetX, offsetY, NumberUtil.formatFloat( minRL, 1, true ), ( minRL <= 0f ? java.awt.Color.RED : null ), texture );
            rrString.draw( offsetX, offsetY, NumberUtil.formatFloat( minRR, 1, true ), ( minRR <= 0f ? java.awt.Color.RED : null ), texture );
            
            minFL = Float.MAX_VALUE;
            minFR = Float.MAX_VALUE;
            minRL = Float.MAX_VALUE;
            minRR = Float.MAX_VALUE;
            
            t0 = gameData.getScoringInfo().getSessionNanos();
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( displayHeader, "Whether to display the header or not." );
        writer.writeProperty( headerFont, "Font for the header." );
        writer.writeProperty( headerFontColor, "Font color for the header" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( displayHeader ) );
        else if ( loader.loadProperty( headerFont ) );
        else if ( loader.loadProperty( headerFontColor ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Header" );
        
        propsCont.addProperty( displayHeader );
        propsCont.addProperty( headerFont );
        propsCont.addProperty( headerFontColor );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        getFontProperty().setFont( "Dialog", Font.PLAIN, 9, false, true );
    }
    
    public RideHeightWidget()
    {
        super( 9.3f, 7.25f );
    }
}
