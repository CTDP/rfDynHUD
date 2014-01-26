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
package net.ctdp.rfdynhud.widgets.standard.fuelneedle;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.FactoredIntProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PosSizeProperty;
import net.ctdp.rfdynhud.properties.Position;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.Size;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.valuemanagers.IntervalManager;
import net.ctdp.rfdynhud.values.AbstractSize;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.widgets.base.needlemeter.NeedleMeterWidget;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link FuelNeedleWidget} displays the current fuel load with a needle.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class FuelNeedleWidget extends NeedleMeterWidget
{
    private final ImageProperty lowFuelWarningImageNameOff = new ImageProperty( "lowFuelWarningImageOff", "imageOff", "standard/shiftlight_off.png", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            lowFuelWarningImageOff = null;
        }
    };
    private TransformableTexture lowFuelWarningImageOff = null;
    
    private final ImageProperty lowFuelWarningImageNameOn = new ImageProperty( "lowFuelWarningImageOn", "imageOn", "standard/shiftlight_on_red.png", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            lowFuelWarningImageOn = null;
        }
    };
    private TransformableTexture lowFuelWarningImageOn = null;
    
    private final AbstractSize lowFuelWarnImgSize = new AbstractSize()
    {
        @Override
        public int getEffectiveWidth()
        {
            loadLowFuelWarningImages( null );
            
            if ( lowFuelWarningImageOn == null )
                return ( 0 );
            
            return ( lowFuelWarningImageOn.getWidth() );
        }
        
        @Override
        public int getEffectiveHeight()
        {
            loadLowFuelWarningImages( null );
            
            if ( lowFuelWarningImageOn == null )
                return ( 0 );
            
            return ( lowFuelWarningImageOn.getHeight() );
        }
    };
    
    private final Position lowFuelWarningImagePosition = Position.newLocalPosition( this, RelativePositioning.CENTER_CENTER, 29.0f, false, 4.0f, false, lowFuelWarnImgSize );
    private final Property lowFuelWarningImagePositionPositioningProperty = lowFuelWarningImagePosition.getPositioningProperty( "lowFuelWarningImagePositioning", "imagePositioning" );
    private final PosSizeProperty lowFuelWarningImagePositionXProperty = lowFuelWarningImagePosition.getXProperty( "lowFuelWarningImagePositionX", "imagePosX" );
    private final PosSizeProperty lowFuelWarningImagePositionYProperty = lowFuelWarningImagePosition.getYProperty( "lowFuelWarningImagePositionY", "imagePosY" );
    
    private final Size lowFuelWarningImageSize = Size.newLocalSize( this, 10.0f, true, 10.0f, true );
    //private final PosSizeProperty lowFuelWarningImageWidthProperty = lowFuelWarningImageSize.getWidthProperty( "lowFuelWarningImageWidth", "imageWidth" );
    private final PosSizeProperty lowFuelWarningImageHeightProperty = lowFuelWarningImageSize.getHeightProperty( "lowFuelWarningImageHeight", "imageHeight" );
    
    private final IntProperty lowFuelWarningLaps = new IntProperty( "lowFuelWarningLaps", "laps", 1, 1, 10, false );
    
    private final FactoredIntProperty lowFuelBlinkTime = new FactoredIntProperty( "lowFuelBlinkTime", "blinkTime", 1000000, 0, 500, 0, 5000 );
    private final IntervalManager lowFuelBlinkManager = new IntervalManager( lowFuelBlinkTime );
    
    public FuelNeedleWidget()
    {
        super( StandardWidgetSet.INSTANCE, StandardWidgetSet.WIDGET_PACKAGE, 16.3125f, 21.75f );
        
        getBorderProperty().setBorder( "" );
        lastMarkerBig.setBooleanValue( true );
        displayValue.setBooleanValue( false );
        displayMarkers.setBooleanValue( false );
        displayMarkerNumbers.setBooleanValue( false );
        needleMountY.setIntValue( 520 );
        needleRotationForMinValue.setFloatValue( -44 );
        needleRotationForMaxValue.setFloatValue( +42 );
    }
    
    @Override
    protected boolean hasText()
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
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( lowFuelWarningImageNameOff, "Image name for the off-state of the low fuel warning." );
        writer.writeProperty( lowFuelWarningImageNameOn, "Image name for the on-state of the low fuel warning." );
        writer.writeProperty( lowFuelWarningImagePositionPositioningProperty, "Positioning type for the low-fuel-warning image." );
        writer.writeProperty( lowFuelWarningImagePositionXProperty, "X-position for the low-fuel-warning image." );
        writer.writeProperty( lowFuelWarningImagePositionYProperty, "Y-position for the low-fuel-warning image." );
        //writer.writeProperty( lowFuelWarningImageWidthProperty, "Width for the low-fuel-warning image." );
        writer.writeProperty( lowFuelWarningImageHeightProperty, "Height for the low-fuel-warning image." );
        writer.writeProperty( lowFuelWarningLaps, "Number of laps to start warning before out of fuel." );
        writer.writeProperty( lowFuelBlinkTime, "Blink time in milli seconds for low fuel warning (0 to disable)." );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( lowFuelWarningImageNameOff ) );
        else if ( loader.loadProperty( lowFuelWarningImageNameOn ) );
        else if ( loader.loadProperty( lowFuelWarningImagePositionPositioningProperty ) );
        else if ( loader.loadProperty( lowFuelWarningImagePositionXProperty ) );
        else if ( loader.loadProperty( lowFuelWarningImagePositionYProperty ) );
        //else if ( loader.loadProperty( lowFuelWarningImageWidthProperty ) );
        else if ( loader.loadProperty( lowFuelWarningImageHeightProperty ) );
        else if ( loader.loadProperty( lowFuelWarningLaps ) );
        else if ( loader.loadProperty( lowFuelBlinkTime ) );
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
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Low Fuel Warning" );
        
        propsCont.addProperty( lowFuelWarningImageNameOff );
        propsCont.addProperty( lowFuelWarningImageNameOn );
        propsCont.addProperty( lowFuelWarningImagePositionPositioningProperty );
        propsCont.addProperty( lowFuelWarningImagePositionXProperty );
        propsCont.addProperty( lowFuelWarningImagePositionYProperty );
        //propsCont.addProperty( lowFuelWarningImageWidthProperty );
        propsCont.addProperty( lowFuelWarningImageHeightProperty );
        propsCont.addProperty( lowFuelWarningLaps );
        propsCont.addProperty( lowFuelBlinkTime );
    }
    
    @Override
    public void onPropertyChanged( Property property, Object oldValue, Object newValue )
    {
        super.onPropertyChanged( property, oldValue, newValue );
        
        if ( property == lowFuelWarningImagePositionPositioningProperty )
            forceReinitialization();
        else if ( property == lowFuelWarningImagePositionXProperty )
            forceReinitialization();
        else if ( property == lowFuelWarningImagePositionYProperty )
            forceReinitialization();
        //else if ( property == lowFuelWarningImageWidthProperty )
        //    forceReinitialization();
        else if ( property == lowFuelWarningImageHeightProperty )
            forceReinitialization();
    }
    
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.IMAGE_INDICATOR + "standard/fuel_needle_bg.png" );
    }
    
    @Override
    protected String getInitialNeedleImage()
    {
        return ( "standard/fuel_needle.png" );
    }
    
    private final boolean isLowFuelWaningUsed()
    {
        return ( lowFuelBlinkManager.isUsed() && !lowFuelWarningImageNameOn.isNoImage() );
    }
    
    private void resetBlink( boolean isEditorMode )
    {
        lowFuelBlinkManager.reset();
        
        if ( lowFuelWarningImageOff != null )
        {
            lowFuelWarningImageOff.setTranslation( lowFuelWarningImagePosition.getEffectiveX(), lowFuelWarningImagePosition.getEffectiveY() );
            lowFuelWarningImageOff.setVisible( false );
        }
        
        if ( lowFuelWarningImageOn != null )
        {
            lowFuelWarningImageOn.setTranslation( lowFuelWarningImagePosition.getEffectiveX(), lowFuelWarningImagePosition.getEffectiveY() );
            lowFuelWarningImageOn.setVisible( isEditorMode );
        }
    }
    
    private void loadLowFuelWarningImages( Boolean isEditorMode )
    {
        if ( !isLowFuelWaningUsed() )
        {
            lowFuelWarningImageOff = null;
            lowFuelWarningImageOn = null;
            
            return;
        }
        
        boolean offVisible = ( lowFuelWarningImageOff == null ) ? false : lowFuelWarningImageOff.isVisible();
        boolean onVisible = ( lowFuelWarningImageOn == null ) ? false : lowFuelWarningImageOn.isVisible();
        
        boolean offReloaded = false;
        
        if ( !lowFuelWarningImageNameOff.isNoImage() )
        {
            ImageTemplate it = lowFuelWarningImageNameOff.getImage();
            
            int h = lowFuelWarningImageSize.getEffectiveHeight();
            int w = Math.round( h * it.getBaseAspect() );
            
            boolean isEditorMode2;
            if ( isEditorMode == null )
            {
                if ( lowFuelWarningImageOff == null )
                    isEditorMode2 = false;
                else
                    isEditorMode2 = ( lowFuelWarningImageOff.getTexture().getWidth() != lowFuelWarningImageOff.getTexture().getMaxWidth() ) || ( lowFuelWarningImageOff.getTexture().getHeight() != lowFuelWarningImageOff.getTexture().getMaxHeight() );
            }
            else
            {
                isEditorMode2 = isEditorMode.booleanValue();
            }
            
            if ( ( lowFuelWarningImageOff == null ) || ( lowFuelWarningImageOff.getWidth() != w ) || ( lowFuelWarningImageOff.getHeight() != h ) )
            {
                lowFuelWarningImageOff = it.getScaledTransformableTexture( w, h, lowFuelWarningImageOff, isEditorMode2 );
                
                offReloaded = true;
            }
        }
        
        boolean onReloaded = false;
        
        if ( !lowFuelWarningImageNameOn.isNoImage() )
        {
            ImageTemplate it = lowFuelWarningImageNameOn.getImage();
            
            int h = lowFuelWarningImageSize.getEffectiveHeight();
            int w = Math.round( h * it.getBaseAspect() );
            
            boolean isEditorMode2;
            if ( isEditorMode == null )
            {
                if ( lowFuelWarningImageOn == null )
                    isEditorMode2 = false;
                else
                    isEditorMode2 = ( lowFuelWarningImageOn.getTexture().getWidth() != lowFuelWarningImageOn.getTexture().getMaxWidth() ) || ( lowFuelWarningImageOn.getTexture().getHeight() != lowFuelWarningImageOn.getTexture().getMaxHeight() );
            }
            else
            {
                isEditorMode2 = isEditorMode.booleanValue();
            }
            
            if ( ( lowFuelWarningImageOn == null ) || ( lowFuelWarningImageOn.getWidth() != w ) || ( lowFuelWarningImageOn.getHeight() != h ) )
            {
                lowFuelWarningImageOn = it.getScaledTransformableTexture( w, h, lowFuelWarningImageOn, isEditorMode2 );
                
                onReloaded = true;
            }
        }
        
        if ( offReloaded && ( lowFuelWarningImageOff != null ) )
        {
            lowFuelWarningImageOff.setTranslation( lowFuelWarningImagePosition.getEffectiveX(), lowFuelWarningImagePosition.getEffectiveY() );
            lowFuelWarningImageOff.setVisible( offVisible );
        }
        
        if ( onReloaded && ( lowFuelWarningImageOn != null ) )
        {
            lowFuelWarningImageOn.setTranslation( lowFuelWarningImagePosition.getEffectiveX(), lowFuelWarningImagePosition.getEffectiveY() );
            lowFuelWarningImageOn.setVisible( onVisible );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
        
        loadLowFuelWarningImages( isEditorMode );
        resetBlink( isEditorMode );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        return ( viewedVSI.isPlayer() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        super.initSubTextures( gameData, isEditorMode, widgetInnerWidth, widgetInnerHeight, collector );
        
        if ( !isLowFuelWaningUsed() )
            return;
        
        loadLowFuelWarningImages( isEditorMode );
        
        if ( lowFuelWarningImageOff != null )
            collector.add( lowFuelWarningImageOff );
        
        if ( lowFuelWarningImageOn != null )
            collector.add( lowFuelWarningImageOn );
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
        return ( gameData.getPhysics().getFuelRange().getMaxValue() );
    }
    
    @Override
    protected float getValue( LiveGameData gameData, boolean isEditorMode )
    {
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
        if ( vsi.isPlayer() )
            return ( gameData.getTelemetryData().getFuel() );
        
        return ( 0 );
    }
    
    @Override
    protected String getMarkerLabelForValue( LiveGameData gameData, boolean isEditorMode, float value )
    {
        return ( String.valueOf( (int)value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        super.initialize( gameData, isEditorMode, dsf, texture, width, height );
        
        resetBlink( isEditorMode );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        super.drawWidget( clock, needsCompleteRedraw, gameData, isEditorMode, texture, offsetX, offsetY, width, height );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        TelemetryData telemData = gameData.getTelemetryData();
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        final float fuel = telemData.getFuel();
        final float avgFuelUsage = telemData.getFuelUsageAverageL();
        final float stintLength = !isEditorMode ? vsi.getStintLength() : 5.2f;
        
        if ( isEditorMode )
        {
            if ( lowFuelWarningImageOn != null )
            {
                lowFuelWarningImageOn.setVisible( true );
                
                if ( lowFuelWarningImageOff != null )
                {
                    lowFuelWarningImageOff.setVisible( false );
                }
            }
            else if ( lowFuelWarningImageOff != null )
            {
                lowFuelWarningImageOff.setVisible( false );
            }
        }
        else if ( lowFuelBlinkManager.isUsed() && ( lowFuelWarningImageOn != null ) )
        {
            boolean warn = false;
            
            if ( avgFuelUsage > 0f )
            {
                float halfLiter = gameData.getProfileInfo().getMeasurementUnits().getFuelAmountFromLiters( 0.5f );
                float lapsForFuel = ( ( fuel - halfLiter ) / avgFuelUsage ) + ( stintLength - (int)stintLength );
                int maxLaps = scoringInfo.getEstimatedMaxLaps( vsi );
                if ( maxLaps < 0 )
                    maxLaps = 999999;
                int lapsRemaining = maxLaps - vsi.getLapsCompleted();
                
                warn = ( lapsForFuel < 1.05f + lowFuelWarningLaps.getIntValue() ) && ( lapsForFuel < lapsRemaining );
            }
            
            if ( warn )
            {
                lowFuelBlinkManager.update( scoringInfo.getSessionNanos() );
            }
            else
            {
                lowFuelBlinkManager.reset();
            }
            
            if ( lowFuelWarningImageOff != null )
                lowFuelWarningImageOff.setVisible( !lowFuelBlinkManager.getState() );
            
            lowFuelWarningImageOn.setVisible( lowFuelBlinkManager.getState() );
        }
    }
}
