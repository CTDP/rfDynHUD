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
package net.ctdp.rfdynhud.widgets.speedo;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.SpeedUnits;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._base.needlemeter.NeedleMeterWidget;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link SpeedoWidget} renders the current velocity as needle on a scale.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SpeedoWidget extends NeedleMeterWidget
{
    private final IntProperty maxVelocity = new IntProperty( this, "maxVelocity", 380, 0, 1000 );
    
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.IMAGE_INDICATOR + "default_rev_meter_bg.png" );
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
    public int getVersion()
    {
        return ( composeVersion( 1, 0, 0 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
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
    
    @Override
    protected float getMinValue( LiveGameData gameData, EditorPresets editorPresets )
    {
        return ( 0 );
    }
    
    @Override
    protected float getMaxValue( LiveGameData gameData, EditorPresets editorPresets )
    {
        if ( gameData.getProfileInfo().getSpeedUnits() == SpeedUnits.MPH )
            return ( maxVelocity.getFloatValue() * TelemetryData.KPH_TO_MPH );
        
        return ( maxVelocity.getFloatValue() );
    }
    
    @Override
    protected float getValue( LiveGameData gameData, EditorPresets editorPresets )
    {
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
        if ( vsi.isPlayer() )
            return ( gameData.getTelemetryData().getScalarVelocityKPH() );
        
        return ( vsi.getScalarVelocityKPH() );
    }
    
    @Override
    protected String getTextForValue( float value )
    {
        return ( String.valueOf( (int)value ) );
    }
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    @Override
    protected void initParentProperties()
    {
        super.initParentProperties();
        
        markersBigStep.setIntValue( 100 );
        markersSmallStep.setIntValue( 10 );
    }
    
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
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
    
    @Override
    protected boolean getSpecificPropertiesFirst( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        if ( !super.getSpecificPropertiesFirst( propsCont, forceAll ) )
            propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( maxVelocity );
        
        return ( true );
    }
    
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
    }
    
    public SpeedoWidget( String name )
    {
        super( name, 16.3125f, 21.75f );
    }
}
