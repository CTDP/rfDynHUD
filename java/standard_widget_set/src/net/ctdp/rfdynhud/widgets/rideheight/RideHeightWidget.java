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
package net.ctdp.rfdynhud.widgets.rideheight;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link RideHeightWidget} indicates, when your car hits the road.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class RideHeightWidget extends Widget
{
    private float minFL = Float.MAX_VALUE;
    private float minFR = Float.MAX_VALUE;
    private float minRL = Float.MAX_VALUE;
    private float minRR = Float.MAX_VALUE;
    
    private long t0 = -1;
    
    private DrawnString flString = null;
    private DrawnString frString = null;
    private DrawnString rlString = null;
    private DrawnString rrString = null;
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
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
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        flString = dsf.newDrawnString( "flString", 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        frString = dsf.newDrawnString( "frString", 100, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        rlString = dsf.newDrawnString( "rlString", 0, 25, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        rrString = dsf.newDrawnString( "rrString", 100, 25, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final TelemetryData telemData = gameData.getTelemetryData();
        
        minFL = Math.min( minFL, telemData.getRideHeight( Wheel.FRONT_LEFT ) );
        minFR = Math.min( minFR, telemData.getRideHeight( Wheel.FRONT_RIGHT ) );
        minRL = Math.min( minRL, telemData.getRideHeight( Wheel.REAR_LEFT ) );
        minRR = Math.min( minRR, telemData.getRideHeight( Wheel.REAR_RIGHT ) );
        
        boolean c2 = gameData.getScoringInfo().getSessionNanos() - t0 > 1000000000L;
        
        if ( needsCompleteRedraw || c2 )
        {
            minFL = Math.max( 0f, minFL );
            minFR = Math.max( 0f, minFR );
            minRL = Math.max( 0f, minRL );
            minRR = Math.max( 0f, minRR );
            
            flString.draw( offsetX, offsetY, NumberUtil.formatFloat( minFL * 100f, 1, true ), ( minFL <= 0f ? java.awt.Color.RED : null ), texture );
            frString.draw( offsetX, offsetY, NumberUtil.formatFloat( minFR * 100f, 1, true ), ( minFR <= 0f ? java.awt.Color.RED : null ), texture );
            rlString.draw( offsetX, offsetY, NumberUtil.formatFloat( minRL * 100f, 1, true ), ( minRL <= 0f ? java.awt.Color.RED : null ), texture );
            rrString.draw( offsetX, offsetY, NumberUtil.formatFloat( minRR * 100f, 1, true ), ( minRR <= 0f ? java.awt.Color.RED : null ), texture );
            
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
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
    }
    
    public RideHeightWidget( String name )
    {
        super( name, 12.5f, 5.5f );
    }
}
