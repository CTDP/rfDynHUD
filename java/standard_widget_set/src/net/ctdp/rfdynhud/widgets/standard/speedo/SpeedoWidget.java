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
package net.ctdp.rfdynhud.widgets.standard.speedo;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.SpeedUnits;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.widgets.base.needlemeter.NeedleMeterWidget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link SpeedoWidget} renders the current velocity as needle on a scale.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SpeedoWidget extends NeedleMeterWidget
{
    private final IntProperty maxVelocity = new IntProperty( "maxVelocity", 340, 1, 1000 );
    
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.IMAGE_INDICATOR + "standard/rev_meter_bg.png" );
    }
    
    @Override
    protected int getMarkersBigStepLowerLimit()
    {
        return ( 20 );
    }
    
    @Override
    protected int getMarkersSmallStepLowerLimit()
    {
        return ( 5 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /*
    @Override
    protected FontProperty getValueFont()
    {
        return ( getFontProperty() );
    }
    
    @Override
    protected ColorProperty getValueFontColor()
    {
        return ( getFontColorProperty() );
    }
    */
    
    @Override
    protected float getMinDataValue( LiveGameData gameData, boolean isEditorMode )
    {
        return ( 0 );
    }
    
    @Override
    protected float getMaxDataValue( LiveGameData gameData, boolean isEditorMode )
    {
        if ( gameData.getProfileInfo().getSpeedUnits() == SpeedUnits.MPH )
            return ( maxVelocity.getFloatValue() * SpeedUnits.Convert.KPH_TO_MPH );
        
        return ( maxVelocity.getFloatValue() );
    }
    
    @Override
    protected float getValue( LiveGameData gameData, boolean isEditorMode )
    {
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
        if ( vsi.isPlayer() )
            return ( gameData.getTelemetryData().getScalarVelocity() );
        
        return ( vsi.getScalarVelocity() );
    }
    
    @Override
    protected String getMarkerLabelForValue( LiveGameData gameData, boolean isEditorMode, float value )
    {
        return ( String.valueOf( (int)value ) );
    }
    
    @Override
    protected void initParentProperties()
    {
        super.initParentProperties();
        
        markersBigStep.setIntValue( 100 );
        markersSmallStep.setIntValue( 10 );
    }
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( maxVelocity, "The maximum velocity in km/h." );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( maxVelocity ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void addMaxValuePropertyToContainer( PropertiesContainer propsCont, boolean forceAll )
    {
        // We don't need this here!
    }
    
    @Override
    protected boolean getSpecificPropertiesFirst( PropertiesContainer propsCont, boolean forceAll )
    {
        if ( !super.getSpecificPropertiesFirst( propsCont, forceAll ) )
            propsCont.addGroup( "Misc" );
        
        propsCont.addProperty( maxVelocity );
        
        return ( true );
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
    }
    
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    public SpeedoWidget()
    {
        super( 16.3125f, 21.75f );
        
        getBorderProperty().setBorder( "" );
        lastMarkerBig.setBooleanValue( true );
    }
}
