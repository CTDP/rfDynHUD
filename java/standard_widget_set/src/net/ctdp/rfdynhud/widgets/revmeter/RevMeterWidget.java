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
package net.ctdp.rfdynhud.widgets.revmeter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.PhysicsSetting;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

import org.openmali.vecmath2.util.ColorUtils;

/**
 * The {@link RevMeterWidget} displays rev/RPM information.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class RevMeterWidget extends Widget
{
    public static final String DEFAULT_GEAR_FONT_NAME = "GearFont";
    
    private final BooleanProperty hideWhenViewingOtherCar = new BooleanProperty( this, "hideWhenViewingOtherCar", false );
    
    private final ImageProperty backgroundImageName = new ImageProperty( this, "backgroundImageName", "backgroundImageName", "default_rev_meter_bg.png", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            boolean fixPositions = ( backgroundTexture != null );
            
            backgroundTexture = null;
            needleTexture = null;
            for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
                shiftLights[i].resetTextures();
            gearBackgroundTexture = null;
            gearBackgroundTexture_bak = null;
            boostNumberBackgroundTexture = null;
            boostNumberBackgroundTexture_bak = null;
            velocityBackgroundTexture = null;
            velocityBackgroundTexture_bak = null;
            
            if ( fixPositions )
            {
                float oldBgScaleX = backgroundScaleX;
                float oldBgScaleY = backgroundScaleY;
                
                loadBackgroundImage( true, getInnerSize().getEffectiveWidth(), getInnerSize().getEffectiveHeight() );
                
                float corrX = oldBgScaleX / backgroundScaleX;
                float corrY = oldBgScaleY / backgroundScaleY;
                
                revMarkersInnerRadius.setIntValue( Math.round( revMarkersInnerRadius.getIntValue() * corrX ) );
                revMarkersLength.setIntValue( Math.round( revMarkersLength.getIntValue() * ( corrX + corrY ) / 2 ) );
                gearPosX.setIntValue( Math.round( gearPosX.getIntValue() * corrX ) );
                gearPosY.setIntValue( Math.round( gearPosY.getIntValue() * corrY ) );
                boostBarPosX.setIntValue( Math.round( boostBarPosX.getIntValue() * corrX ) );
                boostBarPosY.setIntValue( Math.round( boostBarPosY.getIntValue() * corrY ) );
                boostBarWidth.setIntValue( Math.round( boostBarWidth.getIntValue() * corrX ) );
                boostBarHeight.setIntValue( Math.round( boostBarHeight.getIntValue() * corrY ) );
                boostNumberPosX.setIntValue( Math.round( boostNumberPosX.getIntValue() * corrX ) );
                boostNumberPosY.setIntValue( Math.round( boostNumberPosY.getIntValue() * corrY ) );
                velocityPosX.setIntValue( Math.round( velocityPosX.getIntValue() * corrX ) );
                velocityPosY.setIntValue( Math.round( velocityPosY.getIntValue() * corrY ) );
                rpmPosX1.setIntValue( Math.round( rpmPosX1.getIntValue() * corrX ) );
                rpmPosY1.setIntValue( Math.round( rpmPosY1.getIntValue() * corrY ) );
                rpmPosX2.setIntValue( Math.round( rpmPosX2.getIntValue() * corrX ) );
                rpmPosY2.setIntValue( Math.round( rpmPosY2.getIntValue() * corrY ) );
            }
            
            forceAndSetDirty();
        }
    };
    private final ImageProperty needleImageName = new ImageProperty( this, "needleImageName", "imageName", "default_rev_meter_needle.png", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            needleTexture = null;
        }
    };
    private final ImageProperty gearBackgroundImageName = new ImageProperty( this, "gearBackgroundImageName", "backgroundImageName", "", false, true );
    private final ImageProperty boostNumberBackgroundImageName = new ImageProperty( this, "boostNumberBackgroundImageName", "numberBGImageName", "", false, true );
    private final ImageProperty velocityBackgroundImageName = new ImageProperty( this, "velocityBackgroundImageName", "velocityBGImageName", "cyan_circle.png", false, true );
    
    private TextureImage2D backgroundTexture = null;
    private TransformableTexture needleTexture = null;
    private TransformableTexture gearBackgroundTexture = null;
    private TextureImage2D gearBackgroundTexture_bak = null;
    private TransformableTexture boostNumberBackgroundTexture = null;
    private TextureImage2D boostNumberBackgroundTexture_bak = null;
    private TransformableTexture velocityBackgroundTexture = null;
    private TextureImage2D velocityBackgroundTexture_bak = null;
    
    private int gearBackgroundTexPosX, gearBackgroundTexPosY;
    private int boostNumberBackgroundTexPosX, boostNumberBackgroundTexPosY;
    private int velocityBackgroundTexPosX, velocityBackgroundTexPosY;
    
    private float backgroundScaleX = 1.0f;
    private float backgroundScaleY = 1.0f;
    
    private final IntProperty needleAxisBottomOffset = new IntProperty( this, "needleAxisBottomOffset", "axisBottomOffset", 60 );
    
    private final FloatProperty needleRotationForZeroRPM = new FloatProperty( this, "rotationForZeroRPM", (float)Math.PI * 0.68f )
    {
        @Override
        public void setValue( Object value )
        {
            super.setValue( ( (Number)value ).floatValue() * (float)Math.PI / 180f );
        }
        
        @Override
        public Object getValue()
        {
            return ( super.getFloatValue() * 180f / (float)Math.PI );
        }
    };
    private final FloatProperty needleRotationForMaxRPM = new FloatProperty( this, "rotationForMaxRPM", -(float)Math.PI * 0.66f )
    {
        @Override
        public void setValue( Object value )
        {
            super.setValue( ( (Number)value ).floatValue() * (float)Math.PI / 180f );
        }
        
        @Override
        public Object getValue()
        {
            return ( super.getFloatValue() * 180f / (float)Math.PI );
        }
    };
    
    private final BooleanProperty displayRevMarkers = new BooleanProperty( this, "displayRevMarkers", true );
    private final BooleanProperty displayRevMarkerNumbers = new BooleanProperty( this, "displayRevMarkerNumbers", true );
    private final BooleanProperty useMaxRevLimit = new BooleanProperty( this, "useMaxRevLimit", true );
    private final IntProperty revMarkersInnerRadius = new IntProperty( this, "revMarkersInnerRadius", "innerRadius", 224 );
    private final IntProperty revMarkersLength = new IntProperty( this, "revMarkersLength", "length", 50, 4, Integer.MAX_VALUE, false );
    private final IntProperty revMarkersBigStep = new IntProperty( this, "revMarkersBigStep", "bigStep", 1000, 300, Integer.MAX_VALUE, false )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            fixSmallStep();
        }
    };
    private final IntProperty revMarkersSmallStep = new IntProperty( this, "revMarkersSmallStep", "smallStep", 200, 20, Integer.MAX_VALUE, false )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            fixSmallStep();
        }
    };
    private final ColorProperty revMarkersColor = new ColorProperty( this, "revMarkersColor", "color", "#FFFFFF" );
    private final ColorProperty revMarkersMediumColor = new ColorProperty( this, "revMarkersMediumColor", "mediumColor", "#FFFF00" );
    private final ColorProperty revMarkersHighColor = new ColorProperty( this, "revMarkersHighColor", "highColor", "#FF0000" );
    private final FontProperty revMarkersFont = new FontProperty( this, "revMarkersFont", "font", "Monospaced-BOLD-9va" );
    private final ColorProperty revMarkersFontColor = new ColorProperty( this, "revMarkersFontColor", "fontColor", "#FFFFFF" );
    private final BooleanProperty fillHighBackground = new BooleanProperty( this, "fillHighBackground", false );
    private final BooleanProperty interpolateMarkerColors = new BooleanProperty( this, "interpolateMarkerColors", "interpolateColors", false );
    
    private final ShiftLight[] shiftLights = { null, null, null, null, null };
    
    private void initShiftLights( int oldNumber, int newNumber )
    {
        for ( int i = oldNumber; i < newNumber; i++ )
        {
            if ( shiftLights[i] == null )
            {
                shiftLights[i] = new ShiftLight( this, i + 1 );
                
                if ( ( i == 0 ) && ( oldNumber == 0 ) && ( newNumber == 1 ) )
                {
                    shiftLights[0].activationRPM.setValue( -500 );
                }
                else if ( ( i == 0 ) && ( oldNumber == 0 ) && ( newNumber == 2 ) )
                {
                    shiftLights[0].activationRPM.setValue( -200 );
                }
                else if ( ( i == 1 ) && ( oldNumber < 2 ) && ( newNumber == 2 ) )
                {
                    shiftLights[1].activationRPM.setValue( -600 );
                }
            }
        }
    }
    
    private final IntProperty numShiftLights = new IntProperty( this, "numShiftLights", 0, 0, 5 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            initShiftLights( oldValue, newValue );
        }
    };
    
    private final IntProperty gearPosX = new IntProperty( this, "gearPosX", "posX", 354 );
    private final IntProperty gearPosY = new IntProperty( this, "gearPosY", "posY", 512 );
    
    private final FontProperty gearFont = new FontProperty( this, "gearFont", "font", DEFAULT_GEAR_FONT_NAME );
    private final ColorProperty gearFontColor = new ColorProperty( this, "gearFontColor", "fontColor", "#1A261C" );
    
    private final BooleanProperty displayBoostBar = new BooleanProperty( this, "displayBoostBar", "displayBar", true );
    private final IntProperty boostBarPosX = new IntProperty( this, "boostBarPosX", "barPosX", 135 );
    private final IntProperty boostBarPosY = new IntProperty( this, "boostBarPosY", "barPosY", 671 );
    private final IntProperty boostBarWidth = new IntProperty( this, "boostBarWidth", "barWidth", 438 );
    private final IntProperty boostBarHeight = new IntProperty( this, "boostBarHeight", "barHeight", 27 );
    private final BooleanProperty displayBoostNumber = new BooleanProperty( this, "displayBoostNumber", "displayNumber", true );
    private final IntProperty boostNumberPosX = new IntProperty( this, "boostNumberPosX", "numberPosX", 392 );
    private final IntProperty boostNumberPosY = new IntProperty( this, "boostNumberPosY", "numberPosY", 544 );
    private final FontProperty boostNumberFont = new FontProperty( this, "boostNumberFont", "numberFont", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty boostNumberFontColor = new ColorProperty( this, "boostNumberFontColor", "numberFontColor", "#FF0000" );
    
    private final BooleanProperty displayVelocity = new BooleanProperty( this, "displayVelocity", true );
    
    private final IntProperty velocityPosX = new IntProperty( this, "velocityPosX", "posX", 100 );
    private final IntProperty velocityPosY = new IntProperty( this, "velocityPosY", "posY", 100 );
    
    private final FontProperty velocityFont = new FontProperty( this, "velocityFont", "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty velocityFontColor = new ColorProperty( this, "velocityFontColor", "fontColor", "#1A261C" );
    
    private final BooleanProperty displayRPMString1 = new BooleanProperty( this, "displayRPMString1", "displayRPMString", true );
    private final BooleanProperty displayCurrRPM1 = new BooleanProperty( this, "displayCurrRPM1", "displayCurrRPM", true );
    private final BooleanProperty displayMaxRPM1 = new BooleanProperty( this, "displayMaxRPM1", "displayMaxRPM", true );
    private final BooleanProperty useBoostRevLimit1 = new BooleanProperty( this, "useBoostRevLimit1", false );
    private final IntProperty rpmPosX1 = new IntProperty( this, "rpmPosX1", "rpmPosX", 170 );
    private final IntProperty rpmPosY1 = new IntProperty( this, "rpmPosY1", "rpmPosY", 603 );
    private final FontProperty rpmFont1 = new FontProperty( this, "rpmFont1", "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty rpmFontColor1 = new ColorProperty( this, "rpmFontColor1", "fontColor", ColorProperty.STANDARD_FONT_COLOR_NAME );
    private final StringProperty rpmJoinString1 = new StringProperty( this, "rpmJoinString1", "rpmJoinString", " / " );
    
    private final BooleanProperty displayRPMString2 = new BooleanProperty( this, "displayRPMString2", "displayRPMString", false );
    private final BooleanProperty displayCurrRPM2 = new BooleanProperty( this, "displayCurrRPM2", "displayCurrRPM", true );
    private final BooleanProperty displayMaxRPM2 = new BooleanProperty( this, "displayMaxRPM2", "displayMaxRPM", true );
    private final BooleanProperty useBoostRevLimit2 = new BooleanProperty( this, "useBoostRevLimit2", false );
    private final IntProperty rpmPosX2 = new IntProperty( this, "rpmPosX2", "rpmPosX", 170 );
    private final IntProperty rpmPosY2 = new IntProperty( this, "rpmPosY2", "rpmPosY", 603 );
    private final FontProperty rpmFont2 = new FontProperty( this, "rpmFont2", "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty rpmFontColor2 = new ColorProperty( this, "rpmFontColor2", "fontColor", ColorProperty.STANDARD_FONT_COLOR_NAME );
    private final StringProperty rpmJoinString2 = new StringProperty( this, "rpmJoinString2", "rpmJoinString", " / " );
    
    private DrawnString rpmString1 = null;
    private DrawnString rpmString2 = null;
    private DrawnString gearString = null;
    private DrawnString boostString = null;
    private DrawnString velocityString = null;
    
    private final FloatValue maxRPMCheck = new FloatValue();
    private final IntValue gear = new IntValue();
    private final IntValue boost = new IntValue();
    private final IntValue velocity = new IntValue();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersion()
    {
        return ( composeVersion( 1, 1, 0 ) );
    }
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
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
    
    private void setControlVisibility( VehicleScoringInfo viewedVSI )
    {
        setUserVisible1( viewedVSI.isPlayer() || !hideWhenViewingOtherCar.getBooleanValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, editorPresets );
        
        setControlVisibility( gameData.getScoringInfo().getViewedVehicleScoringInfo() );
    }
    
    private void fixSmallStep()
    {
        this.revMarkersSmallStep.setIntValue( revMarkersBigStep.getIntValue() / Math.round( (float)revMarkersBigStep.getIntValue() / (float)revMarkersSmallStep.getIntValue() ) );
    }
    
    private int loadNeedleTexture( boolean isEditorMode )
    {
        if ( needleImageName.isNoImage() )
        {
            needleTexture = null;
            return ( 0 );
        }
        
        if ( ( needleTexture == null ) || isEditorMode )
        {
            try
            {
                ImageTemplate it = backgroundImageName.getImage();
                float scale = ( it == null ) ? 1.0f : getSize().getEffectiveWidth() / (float)it.getBaseWidth();
                it = needleImageName.getImage();
                
                if ( it == null )
                {
                    needleTexture = null;
                    return ( 0 );
                }
                
                int w = Math.round( it.getBaseWidth() * scale );
                int h = Math.round( it.getBaseHeight() * scale );
                if ( ( needleTexture == null ) || ( needleTexture.getWidth() != w ) || ( needleTexture.getHeight() != h ) )
                {
                    needleTexture = it.getScaledTransformableTexture( w, h );
                }
            }
            catch ( Throwable t )
            {
                Logger.log( t );
                
                return ( 0 );
            }
        }
        
        return ( 1 );
    }
    
    private int loadGearBackgroundTexture( boolean isEditorMode )
    {
        if ( ( gearBackgroundTexture == null ) || isEditorMode )
        {
            try
            {
                ImageTemplate it0 = backgroundImageName.getImage();
                float scale = ( it0 == null ) ? 1.0f : getSize().getEffectiveWidth() / (float)it0.getBaseWidth();
                ImageTemplate it = gearBackgroundImageName.getImage();
                
                if ( it == null )
                {
                    gearBackgroundTexture = null;
                    gearBackgroundTexture_bak = null;
                    return ( 0 );
                }
                
                int w = Math.round( it.getBaseWidth() * scale );
                int h = Math.round( it.getBaseHeight() * scale );
                if ( ( gearBackgroundTexture == null ) || ( gearBackgroundTexture.getWidth() != w ) || ( gearBackgroundTexture.getHeight() != h ) )
                {
                    gearBackgroundTexture = it.getScaledTransformableTexture( w, h );
                    gearBackgroundTexture.setDynamic( true );
                    
                    gearBackgroundTexture_bak = TextureImage2D.createOfflineTexture( gearBackgroundTexture.getWidth(), gearBackgroundTexture.getHeight(), gearBackgroundTexture.getTexture().hasAlphaChannel() );
                    gearBackgroundTexture_bak.clear( gearBackgroundTexture.getTexture(), true, null );
                }
            }
            catch ( Throwable t )
            {
                Logger.log( t );
                
                return ( 0 );
            }
        }
        
        return ( 1 );
    }
    
    private int loadBoostNumberBackgroundTexture( boolean isEditorMode )
    {
        if ( !displayBoostNumber.getBooleanValue() )
        {
            boostNumberBackgroundTexture = null;
            boostNumberBackgroundTexture_bak = null;
            return ( 0 );
        }
        
        if ( ( boostNumberBackgroundTexture == null ) || isEditorMode )
        {
            try
            {
                ImageTemplate it0 = backgroundImageName.getImage();
                float scale = ( it0 == null ) ? 1.0f : getSize().getEffectiveWidth() / (float)it0.getBaseWidth();
                ImageTemplate it = boostNumberBackgroundImageName.getImage();
                
                if ( it == null )
                {
                    boostNumberBackgroundTexture = null;
                    boostNumberBackgroundTexture_bak = null;
                    return ( 0 );
                }
                
                int w = Math.round( it.getBaseWidth() * scale );
                int h = Math.round( it.getBaseHeight() * scale );
                if ( ( boostNumberBackgroundTexture == null ) || ( boostNumberBackgroundTexture.getWidth() != w ) || ( boostNumberBackgroundTexture.getHeight() != h ) )
                {
                    boostNumberBackgroundTexture = it.getScaledTransformableTexture( w, h );
                    boostNumberBackgroundTexture.setDynamic( true );
                    
                    boostNumberBackgroundTexture_bak = TextureImage2D.createOfflineTexture( boostNumberBackgroundTexture.getWidth(), boostNumberBackgroundTexture.getHeight(), boostNumberBackgroundTexture.getTexture().hasAlphaChannel() );
                    boostNumberBackgroundTexture_bak.clear( boostNumberBackgroundTexture.getTexture(), true, null );
                }
            }
            catch ( Throwable t )
            {
                Logger.log( t );
                
                return ( 0 );
            }
        }
        
        return ( 1 );
    }
    
    private int loadVelocityBackgroundTexture( boolean isEditorMode )
    {
        if ( !displayVelocity.getBooleanValue() )
        {
            velocityBackgroundTexture = null;
            velocityBackgroundTexture_bak = null;
            return ( 0 );
        }
        
        if ( ( velocityBackgroundTexture == null ) || isEditorMode )
        {
            try
            {
                ImageTemplate it0 = backgroundImageName.getImage();
                float scale = ( it0 == null ) ? 1.0f : getSize().getEffectiveWidth() / (float)it0.getBaseWidth();
                ImageTemplate it = velocityBackgroundImageName.getImage();
                
                if ( it == null )
                {
                    velocityBackgroundTexture = null;
                    velocityBackgroundTexture_bak = null;
                    return ( 0 );
                }
                
                int w = Math.round( it.getBaseWidth() * scale );
                int h = Math.round( it.getBaseHeight() * scale );
                if ( ( velocityBackgroundTexture == null ) || ( velocityBackgroundTexture.getWidth() != w ) || ( velocityBackgroundTexture.getHeight() != h ) )
                {
                    velocityBackgroundTexture = it.getScaledTransformableTexture( w, h );
                    velocityBackgroundTexture.setDynamic( true );
                    
                    velocityBackgroundTexture_bak = TextureImage2D.createOfflineTexture( velocityBackgroundTexture.getWidth(), velocityBackgroundTexture.getHeight(), velocityBackgroundTexture.getTexture().hasAlphaChannel() );
                    velocityBackgroundTexture_bak.clear( velocityBackgroundTexture.getTexture(), true, null );
                }
            }
            catch ( Throwable t )
            {
                Logger.log( t );
                
                return ( 0 );
            }
        }
        
        return ( 1 );
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetWidth, int widgetHeight )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        int n = 0;
        
        n += loadNeedleTexture( isEditorMode );
        
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            n += shiftLights[s].loadTextures( isEditorMode, backgroundImageName );
        
        if ( !gearBackgroundImageName.isNoImage() )
            n += loadGearBackgroundTexture( isEditorMode );
        else
            gearBackgroundTexture = null;
        
        if ( !boostNumberBackgroundImageName.isNoImage() )
            n += loadBoostNumberBackgroundTexture( isEditorMode );
        else
            boostNumberBackgroundTexture = null;
        
        if ( !velocityBackgroundImageName.isNoImage() )
            n += loadVelocityBackgroundTexture( isEditorMode );
        else
            velocityBackgroundTexture = null;
        
        TransformableTexture[] result = new TransformableTexture[ n ];
        
        int i = 0;
        if ( needleTexture != null )
            result[i++] = needleTexture;
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            i = shiftLights[s].writeTexturesToArray( result, i );
        if ( gearBackgroundTexture != null )
            result[i++] = gearBackgroundTexture;
        if ( boostNumberBackgroundTexture != null )
            result[i++] = boostNumberBackgroundTexture;
        if ( velocityBackgroundTexture != null )
            result[i++] = velocityBackgroundTexture;
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        maxRPMCheck.reset();
        gear.reset();
        velocity.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleSetupUpdated( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onVehicleSetupUpdated( gameData, editorPresets );
        
        forceCompleteRedraw();
        forceReinitialization();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onNeededDataComplete( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onNeededDataComplete( gameData, editorPresets );
        
        maxRPMCheck.reset();
        gear.reset();
        velocity.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, editorPresets );
        
        setControlVisibility( viewedVSI );
    }
    
    private void loadBackgroundImage( boolean isEditorMode, int width, int height )
    {
        if ( backgroundImageName.isNoImage() )
        {
            backgroundTexture = null;
            backgroundScaleX = 1.0f;
            backgroundScaleY = 1.0f;
        }
        else
        {
            boolean reloadBackground = ( backgroundTexture == null );
            
            if ( !reloadBackground && isEditorMode && ( ( backgroundTexture.getWidth() != width ) || ( backgroundTexture.getHeight() != height ) ) )
                reloadBackground = true;
            
            if ( reloadBackground )
            {
                try
                {
                    backgroundTexture = backgroundImageName.getImage().getScaledTextureImage( width, height );
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
                
                ImageTemplate it = backgroundImageName.getImage();
                backgroundScaleX = (float)width / (float)it.getBaseWidth();
                backgroundScaleY = (float)height / (float)it.getBaseHeight();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        final Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        loadBackgroundImage( isEditorMode, width, height );
        loadNeedleTexture( isEditorMode );
        
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            shiftLights[s].loadTextures( isEditorMode, backgroundImageName );
        
        if ( needleTexture != null )
        {
            needleTexture.setTranslation( (int)( ( width - needleTexture.getWidth() ) / 2 ), (int)( height / 2 - needleTexture.getHeight() + needleAxisBottomOffset.getIntValue() * backgroundScaleX ) );
            needleTexture.setRotationCenter( (int)( needleTexture.getWidth() / 2 ), (int)( needleTexture.getHeight() - needleAxisBottomOffset.getIntValue() * backgroundScaleX ) );
            //needleTexture.setRotation( 0f );
            //needleTexture.setScale( 1f, 1f );
        }
        
        FontMetrics metrics = texCanvas.getFontMetrics( gearFont.getFont() );
        Rectangle2D bounds = metrics.getStringBounds( "X", texCanvas );
        double fw = bounds.getWidth();
        double fh = metrics.getAscent() - metrics.getDescent();
        int fx, fy;
        
        if ( !gearBackgroundImageName.isNoImage() )
            loadGearBackgroundTexture( isEditorMode );
        else
            gearBackgroundTexture = null;
        
        if ( gearBackgroundTexture == null )
        {
            fx = Math.round( gearPosX.getIntValue() * backgroundScaleX );
            fy = Math.round( gearPosY.getIntValue() * backgroundScaleY );
        }
        else
        {
            gearBackgroundTexPosX = Math.round( gearPosX.getIntValue() * backgroundScaleX - gearBackgroundTexture.getWidth() / 2.0f );
            gearBackgroundTexPosY = Math.round( gearPosY.getIntValue() * backgroundScaleY - gearBackgroundTexture.getHeight() / 2.0f );
            
            fx = gearBackgroundTexture.getWidth() / 2;
            fy = gearBackgroundTexture.getHeight() / 2;
        }
        
        gearString = dsf.newDrawnString( "gearString", fx - (int)( fw / 2.0 ), fy - (int)( metrics.getDescent() + fh / 2.0 ), Alignment.LEFT, false, gearFont.getFont(), gearFont.isAntiAliased(), gearFontColor.getColor() );
        
        metrics = texCanvas.getFontMetrics( boostNumberFont.getFont() );
        bounds = metrics.getStringBounds( "0", texCanvas );
        fw = bounds.getWidth();
        fh = metrics.getAscent() - metrics.getDescent();
        
        if ( !boostNumberBackgroundImageName.isNoImage() )
            loadBoostNumberBackgroundTexture( isEditorMode );
        else
            boostNumberBackgroundTexture = null;
        
        if ( boostNumberBackgroundTexture == null )
        {
            fx = Math.round( boostNumberPosX.getIntValue() * backgroundScaleX );
            fy = Math.round( boostNumberPosY.getIntValue() * backgroundScaleY );
        }
        else
        {
            boostNumberBackgroundTexPosX = Math.round( boostNumberPosX.getIntValue() * backgroundScaleX - boostNumberBackgroundTexture.getWidth() / 2.0f );
            boostNumberBackgroundTexPosY = Math.round( boostNumberPosY.getIntValue() * backgroundScaleY - boostNumberBackgroundTexture.getHeight() / 2.0f );
            
            fx = boostNumberBackgroundTexture.getWidth() / 2;
            fy = boostNumberBackgroundTexture.getHeight() / 2;
        }
        
        boostString = dsf.newDrawnString( "boostString", fx - (int)( fw / 2.0 ), fy - (int)( metrics.getDescent() + fh / 2.0 ), Alignment.LEFT, false, boostNumberFont.getFont(), boostNumberFont.isAntiAliased(), boostNumberFontColor.getColor() );
        
        metrics = velocityFont.getMetrics();
        bounds = metrics.getStringBounds( "000", texCanvas );
        fw = bounds.getWidth();
        fh = metrics.getAscent() - metrics.getDescent();
        
        if ( !velocityBackgroundImageName.isNoImage() )
            loadVelocityBackgroundTexture( isEditorMode );
        else
            velocityBackgroundTexture = null;
        
        if ( velocityBackgroundTexture == null )
        {
            fx = Math.round( velocityPosX.getIntValue() * backgroundScaleX );
            fy = Math.round( velocityPosY.getIntValue() * backgroundScaleY );
        }
        else
        {
            velocityBackgroundTexPosX = Math.round( velocityPosX.getIntValue() * backgroundScaleX - velocityBackgroundTexture.getWidth() / 2.0f );
            velocityBackgroundTexPosY = Math.round( velocityPosY.getIntValue() * backgroundScaleY - velocityBackgroundTexture.getHeight() / 2.0f );
            
            fx = velocityBackgroundTexture.getWidth() / 2;
            fy = velocityBackgroundTexture.getHeight() / 2;
        }
        
        velocityString = dsf.newDrawnString( "velocityString", fx/* - (int)( fw / 2.0 )*/, fy - (int)( metrics.getDescent() + fh / 2.0 ), Alignment.LEFT, false, velocityFont.getFont(), velocityFont.isAntiAliased(), velocityFontColor.getColor() );
        
        rpmString1 = dsf.newDrawnStringIf( displayRPMString1.getBooleanValue(), "rpmString1", width - Math.round( rpmPosX1.getIntValue() * backgroundScaleX ), Math.round( rpmPosY1.getIntValue() * backgroundScaleY ), Alignment.RIGHT, false, rpmFont1.getFont(), rpmFont1.isAntiAliased(), rpmFontColor1.getColor() );
        rpmString2 = dsf.newDrawnStringIf( displayRPMString2.getBooleanValue(), "rpmString2", width - Math.round( rpmPosX2.getIntValue() * backgroundScaleX ), Math.round( rpmPosY2.getIntValue() * backgroundScaleY ), Alignment.RIGHT, false, rpmFont2.getFont(), rpmFont2.isAntiAliased(), rpmFontColor2.getColor() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        maxRPMCheck.update( gameData.getTelemetryData().getEngineMaxRPM() );
        if ( maxRPMCheck.hasChanged() )
            return ( true );
        
        return ( false );
    }
    
    private Color interpolateColor( Color c0, Color c1, float alpha )
    {
        return ( new Color( Math.max( 0, Math.min( Math.round( c0.getRed() + ( c1.getRed() - c0.getRed() ) * alpha ), 255 ) ),
                            Math.max( 0, Math.min( Math.round( c0.getGreen() + ( c1.getGreen() - c0.getGreen() ) * alpha ), 255 ) ),
                            Math.max( 0, Math.min( Math.round( c0.getBlue() + ( c1.getBlue() - c0.getBlue() ) * alpha ), 255 ) )
                          ) );
    }
    
    private void drawMarks( LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        if ( !displayRevMarkers.getBooleanValue() && !fillHighBackground.getBooleanValue() )
            return;
        
        float baseMaxRPM = gameData.getTelemetryData().getEngineBaseMaxRPM();
        float maxRPM = gameData.getTelemetryData().getEngineMaxRPM();
        
        if ( useMaxRevLimit.getBooleanValue() )
        {
            maxRPM = gameData.getPhysics().getEngine().getRevLimitRange().getMaxValue();
        }
        
        VehiclePhysics.Engine engine = gameData.getPhysics().getEngine();
        PhysicsSetting boostRange = engine.getBoostRange();
        
        int minBoost = ( engine.getRPMIncreasePerBoostLevel() > 0f ) ? (int)boostRange.getMinValue() : (int)boostRange.getMaxValue();
        //int maxBoost = ( engine.getRPMIncreasePerBoostLevel() > 0f ) ? (int)boostRange.getMaxValue() : (int)boostRange.getMinValue();
        int mediumBoost = Math.round( boostRange.getMinValue() + ( boostRange.getMaxValue() - boostRange.getMinValue() ) / 2f );
        
        float lowRPM = gameData.getPhysics().getEngine().getMaxRPM( baseMaxRPM, minBoost );
        float mediumRPM = gameData.getPhysics().getEngine().getMaxRPM( baseMaxRPM, mediumBoost );
        
        float centerX = offsetX + width / 2;
        float centerY = offsetY + height / 2;
        
        texCanvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        
        Stroke thousand = null;
        Stroke twoHundred = null;
        
        AffineTransform at0 = new AffineTransform( texCanvas.getTransform() );
        AffineTransform at1 = null;
        AffineTransform at2 = null;
        
        Stroke oldStroke = texCanvas.getStroke();
        
        if ( displayRevMarkers.getBooleanValue() )
        {
            thousand = new BasicStroke( 2 );
            twoHundred = new BasicStroke( 1 );
            
            at1 = new AffineTransform( at0 );
            at2 = new AffineTransform();
        }
        
        float innerRadius = revMarkersInnerRadius.getIntValue() * backgroundScaleX;
        float outerRadius = ( revMarkersInnerRadius.getIntValue() + revMarkersLength.getIntValue() - 1 ) * backgroundScaleX;
        float outerRadius2 = innerRadius + ( outerRadius - innerRadius ) * 0.75f;
        
        if ( fillHighBackground.getBooleanValue() )
        {
            Shape oldClip = texCanvas.getClip();
            
            int nPoints = 360;
            int[] xPoints = new int[ nPoints ];
            int[] yPoints = new int[ nPoints ];
            
            final float TWO_PI = (float)( Math.PI * 2f );
            
            for ( int i = 0; i < nPoints; i++ )
            {
                float angle = i * ( TWO_PI / ( nPoints + 1 ) );
                xPoints[i] = Math.round( centerX + (float)Math.cos( angle ) * innerRadius );
                yPoints[i] = Math.round( centerY + -(float)Math.sin( angle ) * innerRadius );
            }
            
            Polygon p = new Polygon( xPoints, yPoints, nPoints );
            Area area = new Area( oldClip );
            area.subtract( new Area( p ) );
            
            texCanvas.setClip( area );
            
            float lowAngle = -( needleRotationForZeroRPM.getFloatValue() + ( needleRotationForMaxRPM.getFloatValue() - needleRotationForZeroRPM.getFloatValue() ) * ( lowRPM / maxRPM ) );
            //float mediumAngle = -( needleRotationForZeroRPM.getFloatValue() + ( needleRotationForMaxRPM.getFloatValue() - needleRotationForZeroRPM.getFloatValue() ) * ( mediumRPM / maxRPM ) );
            float maxAngle = -needleRotationForMaxRPM.getFloatValue();
            float oneDegree = (float)( Math.PI / 180.0 );
            
            for ( float angle = lowAngle; angle < maxAngle - oneDegree; angle += oneDegree )
            {
                int rpm = Math.round( ( -angle - needleRotationForZeroRPM.getFloatValue() ) * maxRPM / ( needleRotationForMaxRPM.getFloatValue() - needleRotationForZeroRPM.getFloatValue() ) );
                
                if ( rpm <= mediumRPM )
                    texCanvas.setColor( interpolateMarkerColors.getBooleanValue() ? interpolateColor( revMarkersColor.getColor(), revMarkersMediumColor.getColor(), ( rpm - lowRPM ) / ( mediumRPM - lowRPM ) ) : revMarkersMediumColor.getColor() );
                else
                    texCanvas.setColor( interpolateMarkerColors.getBooleanValue() ? interpolateColor( revMarkersMediumColor.getColor(), revMarkersHighColor.getColor(), ( rpm - mediumRPM ) / ( maxRPM - mediumRPM ) ) : revMarkersHighColor.getColor() );
                
                texCanvas.fillArc( Math.round( centerX - outerRadius2 ), Math.round( centerY - outerRadius2 ), Math.round( outerRadius2 + outerRadius2 ), Math.round( outerRadius2 + outerRadius2 ), Math.round( 90f - angle * 180f / (float)Math.PI ), ( angle < maxAngle - oneDegree - oneDegree ) ? -2 : -1 );
            }
            
            texCanvas.setClip( oldClip );
        }
        
        if ( displayRevMarkers.getBooleanValue() )
        {
            Font numberFont = revMarkersFont.getFont();
            texCanvas.setFont( numberFont );
            FontMetrics metrics = texCanvas.getFontMetrics( numberFont );
            
            final int smallStep = revMarkersSmallStep.getIntValue();
            for ( int rpm = 0; rpm <= maxRPM; rpm += smallStep )
            {
                float angle = -( needleRotationForZeroRPM.getFloatValue() + ( needleRotationForMaxRPM.getFloatValue() - needleRotationForZeroRPM.getFloatValue() ) * ( rpm / maxRPM ) );
                
                at2.setToRotation( angle, centerX, centerY );
                texCanvas.setTransform( at2 );
                
                if ( fillHighBackground.getBooleanValue() || ( rpm <= lowRPM ) )
                    texCanvas.setColor( revMarkersColor.getColor() );
                else if ( rpm <= mediumRPM )
                    texCanvas.setColor( interpolateMarkerColors.getBooleanValue() ? interpolateColor( revMarkersColor.getColor(), revMarkersMediumColor.getColor(), ( rpm - lowRPM ) / ( mediumRPM - lowRPM ) ) : revMarkersMediumColor.getColor() );
                else
                    texCanvas.setColor( interpolateMarkerColors.getBooleanValue() ? interpolateColor( revMarkersMediumColor.getColor(), revMarkersHighColor.getColor(), ( rpm - mediumRPM ) / ( maxRPM - mediumRPM ) ) : revMarkersHighColor.getColor() );
                
                if ( ( rpm % revMarkersBigStep.getIntValue() ) == 0 )
                {
                    texCanvas.setStroke( thousand );
                    texCanvas.drawLine( Math.round( centerX ), Math.round( centerY - innerRadius ), Math.round( centerX ), Math.round( centerY - outerRadius ) );
                    //texCanvas.drawLine( Math.round( centerX ), Math.round( ( centerY - innerRadius ) * backgroundScaleY / backgroundScaleX ), Math.round( centerX ), Math.round( ( centerY - outerRadius ) * backgroundScaleY / backgroundScaleX ) );
                    
                    if ( displayRevMarkerNumbers.getBooleanValue() )
                    {
                        String s = String.valueOf( rpm / 1000 );
                        Rectangle2D bounds = metrics.getStringBounds( s, texCanvas );
                        float fw = (float)bounds.getWidth();
                        float fh = (float)( metrics.getAscent() - metrics.getDescent() );
                        float off = (float)Math.sqrt( fw * fw + fh * fh ) / 2f;
                        
                        at1.setToTranslation( 0f, -off );
                        at2.concatenate( at1 );
                        at1.setToRotation( -angle, Math.round( centerX ), Math.round( centerY - outerRadius ) - fh / 2f );
                        at2.concatenate( at1 );
                        texCanvas.setTransform( at2 );
                        
                        texCanvas.drawString( s, Math.round( centerX ) - fw / 2f, Math.round( centerY - outerRadius ) );
                    }
                }
                else
                {
                    texCanvas.setStroke( twoHundred );
                    texCanvas.drawLine( Math.round( centerX ), Math.round( centerY - innerRadius ), Math.round( centerX ), Math.round( centerY - outerRadius2 ) );
                }
            }
        }
        
        texCanvas.setTransform( at0 );
        texCanvas.setStroke( oldStroke );
        texCanvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT );
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( backgroundTexture == null )
            texture.clear( offsetX, offsetY, width, height, true, null );
        else
            texture.clear( backgroundTexture, offsetX, offsetY, width, height, true, null );
        
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            shiftLights[s].loadTextures( editorPresets != null, backgroundImageName );
        
        drawMarks( gameData, texture.getTextureCanvas(), offsetX, offsetY, width, height );
    }
    
    /**
     * 
     * @param boost
     * @param maxBoost
     * @param inverted
     * @param tempBoost
     * @param texCanvas
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     */
    private void drawBoostBar( int boost, int maxBoost, boolean inverted, boolean tempBoost, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        if ( inverted )
            boost = maxBoost - boost + 1;
        
        TextureImage2D image = texCanvas.getImage();
        
        image.clear( Color.BLACK, offsetX, offsetY, width, height, true, null );
        
        texCanvas.setColor( Color.WHITE );
        texCanvas.drawRect( offsetX, offsetY, width - 1, height - 1 );
        
        int x0 = 0;
        for ( int i = 1; i <= maxBoost; i++ )
        {
            int right = Math.min( width * i / maxBoost, width - 1 );
            
            if ( i <= boost )
            {
                Color color = new Color( Math.min( Math.round( ( 255f / maxBoost ) + i * 255f / ( maxBoost + 1 ) ), 255 ), 0, 0 );
                image.clear( color, offsetX + x0 + 1, offsetY + 1, right - x0 - 1, height - 2, true, null );
            }
            
            if ( i < maxBoost )
                texCanvas.drawLine( offsetX + right, offsetY + 1, offsetX + right, offsetY + height - 2 );
            
            x0 = right;
        }
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        TelemetryData telemData = gameData.getTelemetryData();
        
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
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
                if ( backgroundTexture == null )
                    gearString.draw( offsetX, offsetY, string, ColorUtils.BLACK_TRANSPARENT, texture );
                else
                    gearString.draw( offsetX, offsetY, string, backgroundTexture, texture );
            }
            else
            {
                if ( needsCompleteRedraw )
                    gearBackgroundTexture.getTexture().clear( gearBackgroundTexture_bak, true, null );
                
                gearString.draw( 0, 0, string, gearBackgroundTexture_bak, 0, 0, gearBackgroundTexture.getTexture() );
            }
        }
        
        boost.update( vsi.isPlayer() ? telemData.getEffectiveEngineBoostMapping() : gameData.getPhysics().getEngine().getLowestBoostLevel() );
        if ( needsCompleteRedraw || boost.hasChanged() )
        {
            if ( displayBoostNumber.getBooleanValue() )
            {
                if ( boostNumberBackgroundTexture == null )
                {
                    if ( backgroundTexture == null )
                        boostString.draw( offsetX, offsetY, boost.getValueAsString(), ColorUtils.BLACK_TRANSPARENT, texture );
                    else
                        boostString.draw( offsetX, offsetY, boost.getValueAsString(), backgroundTexture, texture );
                }
                else
                {
                    if ( needsCompleteRedraw )
                        boostNumberBackgroundTexture.getTexture().clear( boostNumberBackgroundTexture_bak, true, null );
                    
                    boostString.draw( 0, 0, boost.getValueAsString(), boostNumberBackgroundTexture_bak, 0, 0, boostNumberBackgroundTexture.getTexture() );
                }
            }
            
            if ( displayBoostBar.getBooleanValue() )
            {
                int maxBoost = (int)gameData.getPhysics().getEngine().getBoostRange().getMaxValue();
                boolean inverted = ( gameData.getPhysics().getEngine().getRPMIncreasePerBoostLevel() < 0f );
                boolean tempBoost = false;
                drawBoostBar( boost.getValue(), maxBoost, inverted, tempBoost, texture.getTextureCanvas(), offsetX + Math.round( boostBarPosX.getIntValue() * backgroundScaleX ), offsetY + Math.round( boostBarPosY.getIntValue() * backgroundScaleY ), Math.round( boostBarWidth.getIntValue() * backgroundScaleX ), Math.round( boostBarHeight.getIntValue() * backgroundScaleY ) );
            }
        }
        
        if ( displayVelocity.getBooleanValue() )
        {
            velocity.update( Math.round( vsi.isPlayer() ? telemData.getScalarVelocity() : vsi.getScalarVelocity() ) );
            if ( needsCompleteRedraw || ( clock1 && velocity.hasChanged( false ) ) )
            {
                velocity.setUnchanged();
                
                String string = velocity.getValueAsString();
                
                FontMetrics metrics = velocityFont.getMetrics();
                Rectangle2D bounds = metrics.getStringBounds( string, texture.getTextureCanvas() );
                double fw = bounds.getWidth();
                
                if ( velocityBackgroundTexture == null )
                {
                    if ( backgroundTexture == null )
                        velocityString.draw( offsetX - (int)( fw / 2.0 ), offsetY, string, ColorUtils.BLACK_TRANSPARENT, texture );
                    else
                        velocityString.draw( offsetX - (int)( fw / 2.0 ), offsetY, string, backgroundTexture, offsetX, offsetY, texture );
                }
                else
                {
                    if ( needsCompleteRedraw )
                        velocityBackgroundTexture.getTexture().clear( velocityBackgroundTexture_bak, true, null );
                    
                    velocityString.draw( (int)( -fw / 2.0 ), 0, string, velocityBackgroundTexture_bak, 0, 0, velocityBackgroundTexture.getTexture() );
                }
            }
        }
        
        float rpm = isEditorMode ? editorPresets.getEngineRPM() : telemData.getEngineRPM();
        float maxRPM = telemData.getEngineMaxRPM();
        
        if ( useMaxRevLimit.getBooleanValue() )
        {
            maxRPM = gameData.getPhysics().getEngine().getRevLimitRange().getMaxValue();
        }
        
        if ( displayRPMString1.getBooleanValue() && ( needsCompleteRedraw || clock1 ) )
        {
            String string = "";
            if ( vsi.isPlayer() )
            {
                if ( displayCurrRPM1.getBooleanValue() )
                    string = NumberUtil.formatFloat( rpm, 0, false );
                if ( displayCurrRPM1.getBooleanValue() && displayMaxRPM1.getBooleanValue() )
                    string += rpmJoinString1.getStringValue();
                if ( displayMaxRPM1.getBooleanValue() )
                {
                    float maxRPM2 = maxRPM;
                    if ( useBoostRevLimit1.getBooleanValue() )
                        maxRPM2 = gameData.getPhysics().getEngine().getMaxRPM( maxRPM, boost.getValue() );
                    
                    string += NumberUtil.formatFloat( maxRPM2, 0, false );
                }
            }
            
            if ( backgroundTexture == null )
                rpmString1.draw( offsetX, offsetY, string, ColorUtils.BLACK_TRANSPARENT, texture );
            else
                rpmString1.draw( offsetX, offsetY, string, backgroundTexture, texture );
        }
        
        if ( displayRPMString2.getBooleanValue() && ( needsCompleteRedraw || clock1 ) )
        {
            String string = "";
            if ( vsi.isPlayer() )
            {
                if ( displayCurrRPM2.getBooleanValue() )
                    string = NumberUtil.formatFloat( rpm, 0, false );
                if ( displayCurrRPM2.getBooleanValue() && displayMaxRPM2.getBooleanValue() )
                    string += rpmJoinString2.getStringValue();
                if ( displayMaxRPM2.getBooleanValue() )
                {
                    float maxRPM2 = maxRPM;
                    if ( useBoostRevLimit2.getBooleanValue() )
                        maxRPM2 = gameData.getPhysics().getEngine().getMaxRPM( maxRPM, boost.getValue() );
                    
                    string += NumberUtil.formatFloat( maxRPM2, 0, false );
                }
            }
            
            if ( backgroundTexture == null )
                rpmString2.draw( offsetX, offsetY, string, ColorUtils.BLACK_TRANSPARENT, texture );
            else
                rpmString2.draw( offsetX, offsetY, string, backgroundTexture, texture );
        }
        
        {
            float rpm2 = vsi.isPlayer() ? rpm : 0f;
            float baseMaxRPM = vsi.isPlayer() ? telemData.getEngineBaseMaxRPM() : 100000f;
            for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
                shiftLights[s].updateTextures( gameData, rpm2, baseMaxRPM, boost.getValue(), backgroundScaleX, backgroundScaleY );
        }
        
        if ( needleTexture != null )
        {
            if ( vsi.isPlayer() )
            {
                float rot0 = needleRotationForZeroRPM.getFloatValue();
                float rot = -( rpm / maxRPM ) * ( needleRotationForZeroRPM.getFloatValue() - needleRotationForMaxRPM.getFloatValue() );
                
                needleTexture.setRotation( -rot0 - rot );
                needleTexture.setVisible( true );
            }
            else
            {
                needleTexture.setVisible( false );
            }
        }
        
        if ( gearBackgroundTexture != null )
            gearBackgroundTexture.setTranslation( gearBackgroundTexPosX, gearBackgroundTexPosY );
        
        if ( boostNumberBackgroundTexture != null )
            boostNumberBackgroundTexture.setTranslation( boostNumberBackgroundTexPosX, boostNumberBackgroundTexPosY );
        
        if ( velocityBackgroundTexture != null )
            velocityBackgroundTexture.setTranslation( velocityBackgroundTexPosX, velocityBackgroundTexPosY );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( hideWhenViewingOtherCar, "Hide the Widget when another car is being observed?" );
        writer.writeProperty( backgroundImageName, "The name of the background image." );
        writer.writeProperty( needleImageName, "The name of the needle image." );
        writer.writeProperty( needleAxisBottomOffset, "The offset in (unscaled) pixels from the bottom of the image, where the center of the needle's axis is." );
        writer.writeProperty( needleRotationForZeroRPM, "The rotation for the needle image, that is has for zero RPM (in degrees)." );
        writer.writeProperty( needleRotationForMaxRPM, "The rotation for the needle image, that is has for maximum RPM (in degrees)." );
        writer.writeProperty( displayRevMarkers, "Display rev markers?" );
        writer.writeProperty( displayRevMarkerNumbers, "Display rev marker numbers?" );
        writer.writeProperty( useMaxRevLimit, "Whether to use maximum possible (by setup) rev limit" );
        writer.writeProperty( revMarkersInnerRadius, "The inner radius of the rev markers (in background image space)" );
        writer.writeProperty( revMarkersLength, "The length of the rev markers (in background image space)" );
        writer.writeProperty( revMarkersBigStep, "Step size of bigger rev markers" );
        writer.writeProperty( revMarkersSmallStep, "Step size of smaller rev markers" );
        writer.writeProperty( revMarkersColor, "The color used to draw the rev markers." );
        writer.writeProperty( revMarkersMediumColor, "The color used to draw the rev markers for medium boost." );
        writer.writeProperty( revMarkersHighColor, "The color used to draw the rev markers for high revs." );
        writer.writeProperty( fillHighBackground, "Fill the rev markers' background with medium and high color instead of coloring the markers." );
        writer.writeProperty( interpolateMarkerColors, "Interpolate medium and high colors." );
        writer.writeProperty( revMarkersFont, "The font used to draw the rev marker numbers." );
        writer.writeProperty( revMarkersFontColor, "The font color used to draw the rev marker numbers." );
        writer.writeProperty( numShiftLights, "The number of shift lights to render." );
        for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
            shiftLights[i].saveProperties( writer );
        writer.writeProperty( gearBackgroundImageName, "The name of the image to render behind the gear number." );
        writer.writeProperty( gearPosX, "The x-offset in pixels to the gear label." );
        writer.writeProperty( gearPosY, "The y-offset in pixels to the gear label." );
        writer.writeProperty( gearFont, "The font used to draw the gear." );
        writer.writeProperty( gearFontColor, "The font color used to draw the gear." );
        writer.writeProperty( displayBoostBar, "Display a graphical bar for engine boost mapping?" );
        writer.writeProperty( boostBarPosX, "The x-position of the boost bar." );
        writer.writeProperty( boostBarPosY, "The y-position of the boost bar." );
        writer.writeProperty( boostBarWidth, "The width of the boost bar." );
        writer.writeProperty( boostBarHeight, "The height of the boost bar." );
        writer.writeProperty( displayBoostNumber, "Display a number for engine boost mapping?" );
        writer.writeProperty( boostNumberBackgroundImageName, "The name of the image to render behind the boost number." );
        writer.writeProperty( boostNumberPosX, "The x-position of the boost number." );
        writer.writeProperty( boostNumberPosY, "The y-position of the boost number." );
        writer.writeProperty( boostNumberFont, "The font used to draw the boost number." );
        writer.writeProperty( boostNumberFontColor, "The font color used to draw the boost bar." );
        writer.writeProperty( displayVelocity, "Display velocity on this Widget?" );
        writer.writeProperty( velocityBackgroundImageName, "The name of the image to render behind the velocity number." );
        writer.writeProperty( velocityPosX, "The x-offset in pixels to the velocity label." );
        writer.writeProperty( velocityPosY, "The y-offset in pixels to the velocity label." );
        writer.writeProperty( velocityFont, "The font used to draw the velocity." );
        writer.writeProperty( velocityFontColor, "The font color used to draw the velocity." );
        writer.writeProperty( displayRPMString1, "whether to display the digital RPM/Revs string or not" );
        writer.writeProperty( displayCurrRPM1, "whether to display the current revs or to hide them" );
        writer.writeProperty( displayMaxRPM1, "whether to display the maximum revs or to hide them" );
        writer.writeProperty( useBoostRevLimit1, "whether to use boost level to display max RPM" );
        writer.writeProperty( rpmPosX1, "The offset in (background image space) pixels from the right of the Widget, where the text is to be placed." );
        writer.writeProperty( rpmPosY1, "The offset in (background image space) pixels from the top of the Widget, where the text is to be placed." );
        writer.writeProperty( rpmFont1, "The font used to draw the RPM." );
        writer.writeProperty( rpmFontColor1, "The font color used to draw the RPM." );
        writer.writeProperty( rpmJoinString1, "The String to use to join the current and max RPM." );
        writer.writeProperty( displayRPMString2, "whether to display the digital RPM/Revs string or not" );
        writer.writeProperty( displayCurrRPM2, "whether to display the current revs or to hide them" );
        writer.writeProperty( displayMaxRPM2, "whether to display the maximum revs or to hide them" );
        writer.writeProperty( useBoostRevLimit2, "whether to use boost level to display max RPM" );
        writer.writeProperty( rpmPosX2, "The offset in (background image space) pixels from the right of the Widget, where the text is to be placed." );
        writer.writeProperty( rpmPosY2, "The offset in (background image space) pixels from the top of the Widget, where the text is to be placed." );
        writer.writeProperty( rpmFont2, "The font used to draw the RPM." );
        writer.writeProperty( rpmFontColor2, "The font color used to draw the RPM." );
        writer.writeProperty( rpmJoinString2, "The String to use to join the current and max RPM." );
    }
    
    private boolean loadShiftLightProperty( String key, String value )
    {
        for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
            if ( shiftLights[i].loadProperty( key, value ) )
                return ( true );
        
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( hideWhenViewingOtherCar.loadProperty( key, value ) );
        else if ( backgroundImageName.loadProperty( key, value ) );
        else if ( needleImageName.loadProperty( key, value ) );
        else if ( needleAxisBottomOffset.loadProperty( key, value ) );
        else if ( needleRotationForZeroRPM.loadProperty( key, value ) );
        else if ( needleRotationForMaxRPM.loadProperty( key, value ) );
        else if ( numShiftLights.loadProperty( key, value ) )
        {
            for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
                shiftLights[i] = new ShiftLight( this, i + 1 );
        }
        else if ( loadShiftLightProperty( key, value ) );
        else if ( displayRevMarkers.loadProperty( key, value ) );
        else if ( displayRevMarkerNumbers.loadProperty( key, value ) );
        else if ( useMaxRevLimit.loadProperty( key, value ) );
        else if ( revMarkersInnerRadius.loadProperty( key, value ) );
        else if ( revMarkersLength.loadProperty( key, value ) );
        else if ( revMarkersBigStep.loadProperty( key, value ) );
        else if ( revMarkersSmallStep.loadProperty( key, value ) );
        else if ( revMarkersColor.loadProperty( key, value ) );
        else if ( revMarkersMediumColor.loadProperty( key, value ) );
        else if ( revMarkersHighColor.loadProperty( key, value ) );
        else if ( fillHighBackground.loadProperty( key, value ) );
        else if ( interpolateMarkerColors.loadProperty( key, value ) );
        else if ( revMarkersFont.loadProperty( key, value ) );
        else if ( revMarkersFontColor.loadProperty( key, value ) );
        else if ( gearBackgroundImageName.loadProperty( key, value ) );
        else if ( gearPosX.loadProperty( key, value ) );
        else if ( gearPosY.loadProperty( key, value ) );
        else if ( gearFont.loadProperty( key, value ) );
        else if ( gearFontColor.loadProperty( key, value ) );
        else if ( displayBoostBar.loadProperty( key, value ) );
        else if ( boostBarPosX.loadProperty( key, value ) );
        else if ( boostBarPosY.loadProperty( key, value ) );
        else if ( boostBarWidth.loadProperty( key, value ) );
        else if ( boostBarHeight.loadProperty( key, value ) );
        else if ( displayBoostNumber.loadProperty( key, value ) );
        else if ( boostNumberBackgroundImageName.loadProperty( key, value ) );
        else if ( boostNumberPosX.loadProperty( key, value ) );
        else if ( boostNumberPosY.loadProperty( key, value ) );
        else if ( boostNumberFont.loadProperty( key, value ) );
        else if ( boostNumberFontColor.loadProperty( key, value ) );
        else if ( displayVelocity.loadProperty( key, value ) );
        else if ( velocityBackgroundImageName.loadProperty( key, value ) );
        else if ( velocityPosX.loadProperty( key, value ) );
        else if ( velocityPosY.loadProperty( key, value ) );
        else if ( velocityFont.loadProperty( key, value ) );
        else if ( velocityFontColor.loadProperty( key, value ) );
        else if ( displayRPMString1.loadProperty( key, value ) );
        else if ( displayCurrRPM1.loadProperty( key, value ) );
        else if ( displayMaxRPM1.loadProperty( key, value ) );
        else if ( useBoostRevLimit1.loadProperty( key, value ) );
        else if ( rpmPosX1.loadProperty( key, value ) );
        else if ( rpmPosY1.loadProperty( key, value ) );
        else if ( rpmFont1.loadProperty( key, value ) );
        else if ( rpmFontColor1.loadProperty( key, value ) );
        else if ( rpmJoinString1.loadProperty( key, value ) );
        else if ( displayRPMString2.loadProperty( key, value ) );
        else if ( displayCurrRPM2.loadProperty( key, value ) );
        else if ( displayMaxRPM2.loadProperty( key, value ) );
        else if ( useBoostRevLimit2.loadProperty( key, value ) );
        else if ( rpmPosX2.loadProperty( key, value ) );
        else if ( rpmPosY2.loadProperty( key, value ) );
        else if ( rpmFont2.loadProperty( key, value ) );
        else if ( rpmFontColor2.loadProperty( key, value ) );
        else if ( rpmJoinString2.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( hideWhenViewingOtherCar );
        propsCont.addProperty( backgroundImageName );
        
        propsCont.addGroup( "Needle" );
        
        propsCont.addProperty( needleImageName );
        propsCont.addProperty( needleAxisBottomOffset );
        propsCont.addProperty( needleRotationForZeroRPM );
        propsCont.addProperty( needleRotationForMaxRPM );
        
        propsCont.addGroup( "Rev Markers" );
        
        propsCont.addProperty( displayRevMarkers );
        propsCont.addProperty( displayRevMarkerNumbers );
        propsCont.addProperty( useMaxRevLimit );
        propsCont.addProperty( revMarkersInnerRadius );
        propsCont.addProperty( revMarkersLength );
        propsCont.addProperty( revMarkersBigStep );
        propsCont.addProperty( revMarkersSmallStep );
        propsCont.addProperty( revMarkersColor );
        propsCont.addProperty( revMarkersMediumColor );
        propsCont.addProperty( revMarkersHighColor );
        propsCont.addProperty( fillHighBackground );
        propsCont.addProperty( interpolateMarkerColors );
        propsCont.addProperty( revMarkersFont );
        propsCont.addProperty( revMarkersFontColor );
        
        propsCont.addGroup( "Shift Lights" );
        
        propsCont.addProperty( numShiftLights );
        
        for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
            shiftLights[i].getProperties( propsCont, forceAll );
        
        if ( forceAll )
        {
            if ( numShiftLights.getIntValue() < 1 )
                ShiftLight.DEFAULT_SHIFT_LIGHT1.getProperties( propsCont, forceAll );
            if ( numShiftLights.getIntValue() < 2 )
                ShiftLight.DEFAULT_SHIFT_LIGHT2.getProperties( propsCont, forceAll );
            if ( numShiftLights.getIntValue() < 3 )
                ShiftLight.DEFAULT_SHIFT_LIGHT3.getProperties( propsCont, forceAll );
            if ( numShiftLights.getIntValue() < 4 )
                ShiftLight.DEFAULT_SHIFT_LIGHT4.getProperties( propsCont, forceAll );
            if ( numShiftLights.getIntValue() < 5 )
                ShiftLight.DEFAULT_SHIFT_LIGHT5.getProperties( propsCont, forceAll );
        }
        
        propsCont.addGroup( "Gear" );
        
        propsCont.addProperty( gearBackgroundImageName );
        propsCont.addProperty( gearPosX );
        propsCont.addProperty( gearPosY );
        propsCont.addProperty( gearFont );
        propsCont.addProperty( gearFontColor );
        
        propsCont.addGroup( "Engine Boost" );
        
        propsCont.addProperty( displayBoostBar );
        propsCont.addProperty( boostBarPosX );
        
        propsCont.addProperty( boostBarPosY );
        propsCont.addProperty( boostBarWidth );
        propsCont.addProperty( boostBarHeight );
        propsCont.addProperty( displayBoostNumber );
        propsCont.addProperty( boostNumberBackgroundImageName );
        propsCont.addProperty( boostNumberPosX );
        propsCont.addProperty( boostNumberPosY );
        propsCont.addProperty( boostNumberFont );
        propsCont.addProperty( boostNumberFontColor );
        
        propsCont.addGroup( "Velocity" );
        
        propsCont.addProperty( displayVelocity );
        propsCont.addProperty( velocityBackgroundImageName );
        propsCont.addProperty( velocityPosX );
        propsCont.addProperty( velocityPosY );
        propsCont.addProperty( velocityFont );
        propsCont.addProperty( velocityFontColor );
        
        propsCont.addGroup( "DigitalRevs1" );
        
        propsCont.addProperty( displayRPMString1 );
        propsCont.addProperty( displayCurrRPM1 );
        propsCont.addProperty( displayMaxRPM1 );
        propsCont.addProperty( useBoostRevLimit1 );
        propsCont.addProperty( rpmPosX1 );
        propsCont.addProperty( rpmPosY1 );
        propsCont.addProperty( rpmFont1 );
        propsCont.addProperty( rpmFontColor1 );
        propsCont.addProperty( rpmJoinString1 );
        
        propsCont.addGroup( "DigitalRevs2" );
        
        propsCont.addProperty( displayRPMString2 );
        propsCont.addProperty( displayCurrRPM2 );
        propsCont.addProperty( displayMaxRPM2 );
        propsCont.addProperty( useBoostRevLimit2 );
        propsCont.addProperty( rpmPosX2 );
        propsCont.addProperty( rpmPosY2 );
        propsCont.addProperty( rpmFont2 );
        propsCont.addProperty( rpmFontColor2 );
        propsCont.addProperty( rpmJoinString2 );
    }
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    public RevMeterWidget( String name )
    {
        super( name, 16.3125f, 21.75f );
        
        getBackgroundColorProperty().setColor( (String)null );
        
        numShiftLights.setIntValue( 2 );
    }
}
