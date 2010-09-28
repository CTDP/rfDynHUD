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
package net.ctdp.rfdynhud.widgets.fuelneedle;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.FactoredIntProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.AbstractSize;
import net.ctdp.rfdynhud.values.Position;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._base.needlemeter.NeedleMeterWidget;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link FuelNeedleWidget} displays the current fuel load with a needle.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class FuelNeedleWidget extends NeedleMeterWidget
{
    private final ImageProperty lowFuelWarningImageNameOff = new ImageProperty( this, "lowFuelWarningImageOff", "imageOff", "shiftlight_off.png", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            lowFuelWarningImageOff = null;
        }
    };
    private TransformableTexture lowFuelWarningImageOff = null;
    
    private final ImageProperty lowFuelWarningImageNameOn = new ImageProperty( this, "lowFuelWarningImageOn", "imageOn", "shiftlight_on_red.png", false, true )
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
    
    private final Position lowFuelWarningImagePosition = new Position( this, false, RelativePositioning.TOP_RIGHT, 4.0f, false, 4.0f, false, lowFuelWarnImgSize )
    {
        @Override
        protected void onPositioningPropertySet( RelativePositioning positioning )
        {
            forceReinitialization();
        }
        
        @Override
        protected void onXPropertySet( float x )
        {
            forceReinitialization();
        }
        
        @Override
        protected void onYPropertySet( float y )
        {
            forceReinitialization();
        }
    };
    
    private final Size lowFuelWarningImageSize = new Size( this, false, 20.0f, true, 20.0f, true )
    {
        @Override
        protected void onWidthPropertySet( float width )
        {
            forceReinitialization();
        }
        
        @Override
        protected void onHeightPropertySet( float height )
        {
            forceReinitialization();
        }
    };
    
    private final FactoredIntProperty lowFuelBlinkTime = new FactoredIntProperty( this, "lowFuelBlinkTime", "blinkTime", 1000000, 0, 500, 0, 5000 );
    private long nextBlinkTime = -1L;
    private boolean blinkState = false;
    
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.IMAGE_INDICATOR + "smiths_chrono_fuel.png" );
    }
    
    @Override
    protected String getInitialNeedleImage()
    {
        return ( "chrono_needle.png" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void bake()
    {
        super.bake();
        
        lowFuelWarningImagePosition.bake();
    }
    
    @Override
    public void setAllPosAndSizeToPercents()
    {
        super.setAllPosAndSizeToPercents();
        
        lowFuelWarningImagePosition.setXToPercents();
        lowFuelWarningImagePosition.setYToPercents();
    }
    
    @Override
    public void setAllPosAndSizeToPixels()
    {
        super.setAllPosAndSizeToPixels();
        
        lowFuelWarningImagePosition.setXToPixels();
        lowFuelWarningImagePosition.setYToPixels();
    }
    
    private final boolean isLowFuelWaningUsed()
    {
        return ( ( lowFuelBlinkTime.getIntValue() > 0 ) && !lowFuelWarningImageNameOn.isNoImage() );
    }
    
    private void resetBlink( boolean isEditorMode )
    {
        this.nextBlinkTime = -1L;
        this.blinkState = false;
        
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
    
    private void setControlVisibility( VehicleScoringInfo viewedVSI )
    {
        setUserVisible1( viewedVSI.isPlayer() && viewedVSI.getVehicleControl().isLocalPlayer() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, isEditorMode );
        
        setControlVisibility( gameData.getScoringInfo().getViewedVehicleScoringInfo() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        loadLowFuelWarningImages( isEditorMode );
        resetBlink( isEditorMode );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        setControlVisibility( viewedVSI );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
    {
        TransformableTexture[] superResult = super.getSubTexturesImpl( gameData, isEditorMode, widgetInnerWidth, widgetInnerHeight );
        
        if ( !isLowFuelWaningUsed() )
            return ( superResult );
        
        loadLowFuelWarningImages( isEditorMode );
        
        int i = 0;
        if ( superResult != null )
            i = superResult.length;
        
        TransformableTexture[] tts;
        if ( lowFuelWarningImageNameOff.isNoImage() || lowFuelWarningImageNameOn.isNoImage() )
            tts = new TransformableTexture[ i + 1 ];
        else
            tts = new TransformableTexture[ i + 2 ];
        
        if ( i > 0 )
            System.arraycopy( superResult, 0, tts, 0, i );
        
        if ( lowFuelWarningImageOff != null )
        {
            tts[i++] = lowFuelWarningImageOff;
        }
        
        if ( lowFuelWarningImageOn != null )
        {
            tts[i++] = lowFuelWarningImageOn;
        }
        
        return ( tts );
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
        final float avgFuelUsage = telemData.getFuelUsageAverage();
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
        else if ( ( lowFuelBlinkTime.getFactoredValue() > 0L ) && ( lowFuelWarningImageOn != null ) )
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
                
                warn = ( lapsForFuel < 2.05f ) && ( lapsForFuel < lapsRemaining );
            }
            
            if ( warn )
            {
                if ( nextBlinkTime < 0L )
                {
                    nextBlinkTime = scoringInfo.getSessionNanos() + lowFuelBlinkTime.getFactoredValue();
                    blinkState = true;
                }
                else if ( scoringInfo.getSessionNanos() >= nextBlinkTime )
                {
                    nextBlinkTime = scoringInfo.getSessionNanos() + lowFuelBlinkTime.getFactoredValue();
                    blinkState = !blinkState;
                }
            }
            else
            {
                nextBlinkTime = -1L;
                blinkState = false;
            }
            
            if ( lowFuelWarningImageOff != null )
                lowFuelWarningImageOff.setVisible( !blinkState );
            
            lowFuelWarningImageOn.setVisible( blinkState );
        }
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
        
        writer.writeProperty( lowFuelWarningImageNameOff, "Image name for the off-state of the low fuel warning." );
        writer.writeProperty( lowFuelWarningImageNameOn, "Image name for the on-state of the low fuel warning." );
        writer.writeProperty( lowFuelWarningImagePosition.getPositioningProperty( "lowFuelWarningImagePositioning" ), "Positioning type for the low-fuel-warning image." );
        writer.writeProperty( lowFuelWarningImagePosition.getXProperty( "lowFuelWarningImagePositionX" ), "X-position for the low-fuel-warning image." );
        writer.writeProperty( lowFuelWarningImagePosition.getYProperty( "lowFuelWarningImagePositionY" ), "Y-position for the low-fuel-warning image." );
        //writer.writeProperty( lowFuelWarningImageSize.getWidthProperty( "lowFuelWarningImageWidth" ), "Width for the low-fuel-warning image." );
        writer.writeProperty( lowFuelWarningImageSize.getHeightProperty( "lowFuelWarningImageHeight" ), "Height for the low-fuel-warning image." );
        writer.writeProperty( lowFuelBlinkTime, "Blink time in milli seconds for low fuel warning (0 to disable)." );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( lowFuelWarningImageNameOff ) );
        else if ( loader.loadProperty( lowFuelWarningImageNameOn ) );
        else if ( loader.loadProperty( lowFuelWarningImagePosition.getPositioningProperty( "lowFuelWarningImagePositioning", "imagePositioning" ) ) );
        else if ( loader.loadProperty( lowFuelWarningImagePosition.getXProperty( "lowFuelWarningImagePositionX", "imagePosX" ) ) );
        else if ( loader.loadProperty( lowFuelWarningImagePosition.getYProperty( "lowFuelWarningImagePositionY", "imagePosY" ) ) );
        //else if ( loader.loadProperty( lowFuelWarningImageSize.getWidthProperty( "lowFuelWarningImageWidth", "imageWidth" ) ) );
        else if ( loader.loadProperty( lowFuelWarningImageSize.getHeightProperty( "lowFuelWarningImageHeight", "imageHeight" ) ) );
        else if ( loader.loadProperty( lowFuelBlinkTime ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void addMaxValuePropertyToContainer( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        // We don't need this here!
    }
    
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Low Fuel Warning" );
        
        propsCont.addProperty( lowFuelWarningImageNameOff );
        propsCont.addProperty( lowFuelWarningImageNameOn );
        propsCont.addProperty( lowFuelWarningImagePosition.getPositioningProperty( "lowFuelWarningImagePositioning", "imagePositioning" ) );
        propsCont.addProperty( lowFuelWarningImagePosition.getXProperty( "lowFuelWarningImagePositionX", "imagePosX" ) );
        propsCont.addProperty( lowFuelWarningImagePosition.getYProperty( "lowFuelWarningImagePositionY", "imagePosY" ) );
        //propsCont.addProperty( lowFuelWarningImageSize.getWidthProperty( "lowFuelWarningImageWidth", "imageWidth" ) );
        propsCont.addProperty( lowFuelWarningImageSize.getHeightProperty( "lowFuelWarningImageHeight", "imageHeight" ) );
        propsCont.addProperty( lowFuelBlinkTime );
    }
    
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    public FuelNeedleWidget( String name )
    {
        super( name, 16.3125f, 21.75f );
        
        getBorderProperty().setBorder( "" );
        lastMarkerBig.setBooleanValue( true );
        displayValue.setBooleanValue( false );
        displayMarkers.setBooleanValue( false );
        displayMarkerNumbers.setBooleanValue( false );
        needleMountY.setIntValue( 520 );
        needleRotationForMinValue.setFloatValue( -44 );
        needleRotationForMaxValue.setFloatValue( +42 );
    }
}
