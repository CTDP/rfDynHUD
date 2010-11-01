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
package net.ctdp.rfdynhud.widgets.base.revneedlemeter;

import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.DelayProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.needlemeter.NeedleMeterWidget;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * The {@link AbstractRevNeedleMeterWidget} displays rev/RPM information.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractRevNeedleMeterWidget extends NeedleMeterWidget
{
    public static final int PEAK_NEEDLE_LOCAL_Z_INDEX = NEEDLE_LOCAL_Z_INDEX - 1;
    public static final String DEFAULT_GEAR_FONT_NAME = "GearFont";
    
    private final BooleanProperty hideWhenViewingOtherCar = new BooleanProperty( this, "hideWhenViewingOtherCar", "hideWhenOtherCar", false );
    
    
    @Override
    protected int getMarkersBigStepLowerLimit()
    {
        return ( 300 );
    }
    
    @Override
    protected int getMarkersSmallStepLowerLimit()
    {
        return ( 20 );
    }
    
    protected final BooleanProperty useMaxRevLimit = new BooleanProperty( this, "useMaxRevLimit", true );
    
    
    protected final BooleanProperty displayGear = new BooleanProperty( this, "displayGear", "displayGear", true );
    protected final ImageProperty gearBackgroundImageName = new ImageProperty( this, "gearBackgroundImageName", "bgImageName", "", false, true );
    private TransformableTexture gearBackgroundTexture = null;
    private TextureImage2D gearBackgroundTexture_bak = null;
    protected final IntProperty gearPosX = new IntProperty( this, "gearPosX", "posX", 354 );
    protected final IntProperty gearPosY = new IntProperty( this, "gearPosY", "posY", 512 );
    private int gearBackgroundTexPosX, gearBackgroundTexPosY;
    protected final FontProperty gearFont = new FontProperty( this, "gearFont", "font", DEFAULT_GEAR_FONT_NAME );
    protected final ColorProperty gearFontColor = new ColorProperty( this, "gearFontColor", "fontColor", "#1A261C" );
    
    
    protected String getInitialPeakNeedleImage()
    {
        return ( "default_rev_meter_needle.png" );
    }
    
    protected final ImageProperty peakNeedleImageName = new ImageProperty( this, "peakNeedleImageName", "imageName", getInitialPeakNeedleImage(), false, true );
    protected final IntProperty peakNeedlePivotBottomOffset = new IntProperty( this, "peakNeedlePivotBottomOffset", "pivotBottomOffset", 60 );
    private TransformableTexture peakNeedleTexture = null;
    
    protected final DelayProperty peakNeedleCooldown = new DelayProperty( this, "peakNeedleCooldown", "cooldown", DelayProperty.DisplayUnits.MILLISECONDS, 1000, 0, 5000, false );
    protected final DelayProperty peakNeedleWaitTime = new DelayProperty( this, "peakNeedleWaitTime", "wait", DelayProperty.DisplayUnits.MILLISECONDS, 1000, 0, 5000, false );
    protected final DelayProperty peakNeedleDownshiftIgnoreTime = new DelayProperty( this, "peakNeedleDownshiftIgnoreTime", "downshiftIgnoreTime", DelayProperty.DisplayUnits.MILLISECONDS, 1500, 0, 5000, false );
    private long nextPeakRecordTime = -1L;
    private long lastPeakRecordTime = -1L;
    
    private DrawnString gearString = null;
    
    private final FloatValue maxRPMCheck = new FloatValue();
    private final IntValue gear = new IntValue();
    
    private float peakRPM = 0f;
    private short lastGear = 0;
    
    @Override
    protected void onBackgroundChanged( boolean imageChanged, float deltaScaleX, float deltaScaleY )
    {
        super.onBackgroundChanged( imageChanged, deltaScaleX, deltaScaleY );
        
        // TODO: Don't set to null!
        gearBackgroundTexture = null;
        gearBackgroundTexture_bak = null;
        
        if ( deltaScaleX > 0f )
        {
            gearPosX.setIntValue( Math.round( gearPosX.getIntValue() * deltaScaleX ) );
            gearPosY.setIntValue( Math.round( gearPosY.getIntValue() * deltaScaleY ) );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        String result = super.getDefaultNamedFontValue( name );
        
        if ( result != null )
            return ( result );
        
        if ( name.equals( DEFAULT_GEAR_FONT_NAME ) )
            return ( "Monospaced-BOLD-26va" );
        
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNeededData()
    {
        return ( Widget.NEEDED_DATA_TELEMETRY/* | Widget.NEEDED_DATA_SETUP*/ );
    }
    
    @Override
    protected float getMinDataValue( LiveGameData gameData, boolean isEditorModes )
    {
        return ( 0 );
    }
    
    @Override
    protected float getMaxDataValue( LiveGameData gameData, boolean isEditorMode )
    {
        /*
        if ( useMaxRevLimit.getBooleanValue() )
            return ( gameData.getPhysics().getEngine().getRevLimitRange().getMaxValue() );
        
        return ( gameData.getTelemetryData().getEngineMaxRPM() );
        */
        
        if ( useMaxRevLimit.getBooleanValue() )
            return ( gameData.getPhysics().getEngine().getMaxRPM( gameData.getPhysics().getEngine().getRevLimitRange().getMaxValue() ) );
        
        return ( gameData.getPhysics().getEngine().getMaxRPM( gameData.getSetup().getEngine().getRevLimit() ) );
    }
    
    @Override
    protected float getValue( LiveGameData gameData, boolean isEditorMode )
    {
        return ( gameData.getTelemetryData().getEngineRPM() );
    }
    
    @Override
    protected int getValueForValueDisplay( LiveGameData gameData, boolean isEditorMode )
    {
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
        if ( vsi.isPlayer() )
            return ( Math.round( gameData.getTelemetryData().getScalarVelocity() ) );
        
        return ( Math.round( vsi.getScalarVelocity() ) );
    }
    
    @Override
    protected String getMarkerLabelForValue( LiveGameData gameData, boolean isEditorMode, float value )
    {
        return ( String.valueOf( Math.round( value / 1000 ) ) );
    }
    
    public void setPeakNeedleImageName( String image )
    {
        this.peakNeedleImageName.setImageName( image );
    }
    
    public final String getPeakNeedleImageName()
    {
        return ( peakNeedleImageName.getImageName() );
    }
    
    private boolean loadPeakNeedleTexture( boolean isEditorMode )
    {
        if ( peakNeedleImageName.isNoImage() )
        {
            peakNeedleTexture = null;
            return ( false );
        }
        
        try
        {
            ImageTemplate it = peakNeedleImageName.getImage();
            
            if ( it == null )
            {
                peakNeedleTexture = null;
                return ( false );
            }
            
            float scale = getBackground().getScaleX();
            int w = Math.round( it.getBaseWidth() * scale );
            int h = Math.round( it.getBaseHeight() * scale );
            peakNeedleTexture = it.getScaledTransformableTexture( w, h, peakNeedleTexture, isEditorMode );
            
            peakNeedleTexture.setLocalZIndex( PEAK_NEEDLE_LOCAL_Z_INDEX );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( false );
        }
        
        return ( true );
    }
    
    private boolean loadGearBackgroundTexture( boolean isEditorMode )
    {
        if ( !displayGear.getBooleanValue() )
        {
            gearBackgroundTexture = null;
            gearBackgroundTexture_bak = null;
            return ( false );
        }
        
        try
        {
            ImageTemplate it = gearBackgroundImageName.getImage();
            
            if ( it == null )
            {
                gearBackgroundTexture = null;
                gearBackgroundTexture_bak = null;
                return ( false );
            }
            
            float scale = getBackground().getScaleX();
            int w = Math.round( it.getBaseWidth() * scale );
            int h = Math.round( it.getBaseHeight() * scale );
            boolean[] changeInfo = new boolean[ 2 ];
            gearBackgroundTexture = it.getScaledTransformableTexture( w, h, gearBackgroundTexture, isEditorMode, changeInfo );
            gearBackgroundTexture.setDynamic( true );
            
            if ( changeInfo[1] ) // redrawn
            {
                gearBackgroundTexture_bak = TextureImage2D.getOrCreateDrawTexture( w, h, it.hasAlpha(), gearBackgroundTexture_bak, isEditorMode );
                gearBackgroundTexture_bak.clear( gearBackgroundTexture.getTexture(), true, null );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( false );
        }
        
        return ( true );
    }
    
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        super.initSubTextures( gameData, isEditorMode, widgetInnerWidth, widgetInnerHeight, collector );
        
        if ( loadPeakNeedleTexture( isEditorMode ) )
            collector.add( peakNeedleTexture );
        if ( loadGearBackgroundTexture( isEditorMode ) )
            collector.add( gearBackgroundTexture );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        lastGear = 0;
        peakRPM = 0f;
        maxRPMCheck.reset();
        gear.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onNeededDataComplete( LiveGameData gameData, boolean isEditorMode )
    {
        super.onNeededDataComplete( gameData, isEditorMode );
        
        lastGear = 0;
        peakRPM = 0f;
        maxRPMCheck.reset();
        gear.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        return ( viewedVSI.isPlayer() || !hideWhenViewingOtherCar.getBooleanValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        super.initialize( gameData, isEditorMode, dsf, texture, width, height );
        
        final Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        FontMetrics metrics = null;
        Rectangle2D bounds = null;
        double fw = 0, fh = 0;
        double fd = 0;
        int fx = 0, fy = 0;
        
        if ( displayGear.getBooleanValue() )
        {
            metrics = gearFont.getMetrics();
            bounds = metrics.getStringBounds( "X", texCanvas );
            fw = bounds.getWidth();
            fd = metrics.getDescent();
            fh = metrics.getAscent() - fd;
            
            if ( gearBackgroundTexture == null )
            {
                fx = Math.round( gearPosX.getIntValue() * getBackground().getScaleX() );
                fy = Math.round( gearPosY.getIntValue() * getBackground().getScaleY() );
            }
            else
            {
                gearBackgroundTexPosX = Math.round( gearPosX.getIntValue() * getBackground().getScaleX() - gearBackgroundTexture.getWidth() / 2.0f );
                gearBackgroundTexPosY = Math.round( gearPosY.getIntValue() * getBackground().getScaleY() - gearBackgroundTexture.getHeight() / 2.0f );
                
                fx = gearBackgroundTexture.getWidth() / 2;
                fy = gearBackgroundTexture.getHeight() / 2;
            }
        }
        
        gearString = dsf.newDrawnStringIf( displayGear.getBooleanValue(), "gearString", fx - (int)( fw / 2.0 ), fy - (int)( fd + fh / 2.0 ), Alignment.LEFT, false, gearFont.getFont(), gearFont.isAntiAliased(), gearFontColor.getColor() );
        
        int mountX = getNeedleMountX( width );
        int mountY = getNeedleMountY( height );
        
        if ( peakNeedleTexture != null )
        {
            peakNeedleTexture.setTranslation( mountX - peakNeedleTexture.getWidth() / 2, mountY - peakNeedleTexture.getHeight() + peakNeedlePivotBottomOffset.getIntValue() * getBackground().getScaleX() );
            peakNeedleTexture.setRotationCenter( peakNeedleTexture.getWidth() / 2, (int)( peakNeedleTexture.getHeight() - peakNeedlePivotBottomOffset.getIntValue() * getBackground().getScaleY() ) );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int width, int height )
    {
        maxRPMCheck.update( gameData.getTelemetryData().getEngineMaxRPM() );
        if ( maxRPMCheck.hasChanged() )
            return ( true );
        
        return ( false );
    }
    
    @Override
    protected boolean doRenderNeedle( LiveGameData gameData, boolean isEditorMode )
    {
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
        return ( vsi.isPlayer() );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        super.drawWidget( clock, needsCompleteRedraw, gameData, isEditorMode, texture, offsetX, offsetY, width, height );
        
        TelemetryData telemData = gameData.getTelemetryData();
        
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
        if ( displayGear.getBooleanValue() )
        {
            gear.update( vsi.isPlayer() ? telemData.getCurrentGear() : -1000 );
            if ( needsCompleteRedraw || gear.hasChanged() )
            {
                String string;
                if ( vsi.isPlayer() )
                    string = gear.getValue() == -1 ? "R" : gear.getValue() == 0 ? "N" : String.valueOf( gear );
                else
                    string = "";
                
                if ( gearBackgroundTexture == null )
                {
                    gearString.draw( offsetX, offsetY, string, texture );
                }
                else
                {
                    if ( needsCompleteRedraw )
                        gearBackgroundTexture.getTexture().clear( gearBackgroundTexture_bak, true, null );
                    
                    gearString.draw( 0, 0, string, gearBackgroundTexture.getTexture(), gearBackgroundTexture_bak, 0, 0 );
                }
            }
        }
        
        if ( gearBackgroundTexture != null )
            gearBackgroundTexture.setTranslation( gearBackgroundTexPosX, gearBackgroundTexPosY );
        
        if ( peakNeedleTexture != null )
        {
            if ( doRenderNeedle( gameData, isEditorMode ) )
            {
                long sessionNanos = gameData.getScoringInfo().getSessionNanos();
                float rpmRange = ( getMaxValue( gameData, isEditorMode ) - getMinValue( gameData, isEditorMode ) );
                
                float value;
                
                if ( isEditorMode )
                {
                    value = getMaxValue( gameData, isEditorMode );
                }
                else
                {
                    value = peakRPM;
                    
                    float rpm2 = vsi.isPlayer() ? telemData.getEngineRPM() : 0f;
                    
                    if ( rpm2 < peakRPM - 5f )
                    {
                        if ( sessionNanos > lastPeakRecordTime + peakNeedleWaitTime.getDelayNanos() )
                        {
                            float cooldown = (float)( ( sessionNanos - lastPeakRecordTime - peakNeedleWaitTime.getDelayNanos() ) / 1000000000.0 ) / ( peakNeedleCooldown.getDelaySeconds() * ( peakRPM / rpmRange ) );
                            if ( cooldown > 1.0f )
                                cooldown = 1.0f;
                            
                            value = Math.max( rpm2, peakRPM - ( peakRPM * cooldown ) );
                        }
                    }
                    
                    if ( gear.getValue() < lastGear )
                    {
                        nextPeakRecordTime = sessionNanos + peakNeedleDownshiftIgnoreTime.getDelayNanos();
                    }
                    
                    lastGear = (short)gear.getValue();
                    
                    if ( sessionNanos > nextPeakRecordTime )
                    {
                        if ( ( rpm2 >= peakRPM ) || ( rpm2 >= value ) )
                        {
                            peakRPM = rpm2;
                        }
                        
                        if ( Math.abs( rpm2 - peakRPM ) < 5f )
                        {
                            lastPeakRecordTime = sessionNanos;
                        }
                    }
                }
                
                float minValue = getMinValue( gameData, isEditorMode );
                float maxValue = getMaxValue( gameData, isEditorMode );
                if ( !getNeedleMayExceedMinimum() )
                    value = Math.max( minValue, value );
                if ( !getNeedleMayExceedMaximum() )
                    value = Math.min( value, maxValue );
                
                
                float rot0 = needleRotationForMinValue.getFactoredValue();
                float rot = -( ( value - minValue ) / rpmRange ) * ( needleRotationForMinValue.getFactoredValue() - needleRotationForMaxValue.getFactoredValue() );
                
                peakNeedleTexture.setRotation( rot0 + rot );
                peakNeedleTexture.setVisible( true );
            }
            else
            {
                peakNeedleTexture.setVisible( false );
            }
        }
    }
    
    @Override
    protected void initParentProperties()
    {
        super.initParentProperties();
        
        markersBigStep.setIntValue( 1000 );
        markersSmallStep.setIntValue( 200 );
    }
    
    @Override
    protected void saveMarkersProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveMarkersProperties( writer );
        
        writer.writeProperty( useMaxRevLimit, "Whether to use maximum possible (by setup) rev limit" );
    }
    
    @Override
    protected void saveNeedleProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveNeedleProperties( writer );
        
        writer.writeProperty( peakNeedleImageName, "The name of the peak needle image." );
        writer.writeProperty( peakNeedlePivotBottomOffset, "The offset in (unscaled) pixels from the bottom of the image, where the center of the peak needle's axis is." );
        writer.writeProperty( peakNeedleWaitTime, "The time in milliseconds to let the peak needle stay at the peak value." );
        writer.writeProperty( peakNeedleCooldown, "The time in milliseconds, that the peak needle takes to go down from max RPM to zero." );
        writer.writeProperty( peakNeedleDownshiftIgnoreTime, "The time in milliseconds to ignore current revs after a downshift." );
    }
    
    protected void saveGearProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( displayGear, "Display the gear?" );
        writer.writeProperty( gearBackgroundImageName, "The name of the image to render behind the gear number." );
        writer.writeProperty( gearPosX, "The x-offset in pixels to the gear label." );
        writer.writeProperty( gearPosY, "The y-offset in pixels to the gear label." );
        writer.writeProperty( gearFont, "The font used to draw the gear." );
        writer.writeProperty( gearFontColor, "The font color used to draw the gear." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( hideWhenViewingOtherCar, "Hide the Widget when another car is being observed?" );
        
        saveGearProperties( writer );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( hideWhenViewingOtherCar ) );
        
        else if ( loader.loadProperty( useMaxRevLimit ) );
        
        else if ( loader.loadProperty( peakNeedleImageName ) );
        else if ( loader.loadProperty( peakNeedlePivotBottomOffset ) );
        else if ( loader.loadProperty( peakNeedleWaitTime ) );
        else if ( loader.loadProperty( peakNeedleCooldown ) );
        else if ( loader.loadProperty( peakNeedleDownshiftIgnoreTime ) );
        
        else if ( loader.loadProperty( displayGear ) );
        else if ( loader.loadProperty( gearBackgroundImageName ) );
        else if ( loader.loadProperty( gearPosX ) );
        else if ( loader.loadProperty( gearPosY ) );
        else if ( loader.loadProperty( gearFont ) );
        else if ( loader.loadProperty( gearFontColor ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void addVisibilityPropertiesToContainer( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.addVisibilityPropertiesToContainer( propsCont, forceAll );
        
        if ( getMasterWidget() == null )
        {
            propsCont.addProperty( hideWhenViewingOtherCar );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void addMaxValuePropertyToContainer( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        // We don't need this here!
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void getMarkersProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getMarkersProperties( propsCont, forceAll );
        
        propsCont.addProperty( useMaxRevLimit );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void getNeedleProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getNeedleProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Peak Needle" );
        
        propsCont.addProperty( peakNeedleImageName );
        propsCont.addProperty( peakNeedlePivotBottomOffset );
        propsCont.addProperty( peakNeedleWaitTime );
        propsCont.addProperty( peakNeedleCooldown );
        propsCont.addProperty( peakNeedleDownshiftIgnoreTime );
    }
    
    /**
     * Collects the properties for the gear.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void getGearProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( "Gear" );
        
        propsCont.addProperty( displayGear );
        propsCont.addProperty( gearBackgroundImageName );
        propsCont.addProperty( gearPosX );
        propsCont.addProperty( gearPosY );
        propsCont.addProperty( gearFont );
        propsCont.addProperty( gearFontColor );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDigiValuePropertiesGroupName()
    {
        return ( "Velocity" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        getGearProperties( propsCont, forceAll );
    }
    
    public AbstractRevNeedleMeterWidget( float width, float height )
    {
        super( width, height );
        
        getBorderProperty().setBorder( "" );
    }
}
