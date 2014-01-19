/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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
package net.ctdp.rfdynhud.widgets.standard.dashboard;

import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.widgets.base.widget.AbstractAssembledWidget;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.standard.fuelneedle.FuelNeedleWidget;
import net.ctdp.rfdynhud.widgets.standard.revmeter.RevMeterWidget;
import net.ctdp.rfdynhud.widgets.standard.speedo.SpeedoWidget;

public class DashboardWidget extends AbstractAssembledWidget
{
    public DashboardWidget( boolean initParts )
    {
        super( StandardWidgetSet.INSTANCE, StandardWidgetSet.WIDGET_PACKAGE, 33.6f, true, 23.0f, true, initParts );
    }
    
    public DashboardWidget()
    {
        this( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Widget[] initParts( float width, boolean widthPercent, float height, boolean heightPercent )
    {
        SpeedoWidget speedo = new SpeedoWidget();
        speedo.setName( "Speedo" );
        FuelNeedleWidget fuel = new FuelNeedleWidget();
        fuel.setName( "Fuel" );
        //fuel.setDisplayValue( false );
        RevMeterWidget revs = new RevMeterWidget();
        revs.setName( "Revs" );
        revs.setDisplayValue( false );
        revs.setPeakNeedleImageName( "" );
        
        return ( new Widget[] { speedo, fuel, revs } );
    }
    
    @Override
    protected void arrangeParts( Widget[] parts )
    {
        int innerWidth = this.getInnerSize().getEffectiveWidth();
        int innerHeight = this.getInnerSize().getEffectiveHeight();
        
        int w1 = innerWidth * 45 / 100;
        int w2 = innerWidth * 25 / 100;
        
        parts[0].getSize().setEffectiveSize( w1, w1 );
        parts[1].getSize().setEffectiveSize( w2, w2 );
        parts[2].getSize().setEffectiveSize( w1, w1 );
        
        parts[1].getPosition().setEffectivePosition( RelativePositioning.BOTTOM_CENTER, ( innerWidth - w2 ) / 2, innerHeight - w2 );
        parts[2].getPosition().setEffectivePosition( RelativePositioning.TOP_RIGHT, innerWidth - w1, 0 );
    }
}
