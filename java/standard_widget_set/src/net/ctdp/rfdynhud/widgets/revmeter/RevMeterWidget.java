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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.PhysicsSetting;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._base.needlemeter.NeedleMeterWidget;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link RevMeterWidget} displays rev/RPM information.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class RevMeterWidget extends NeedleMeterWidget
{
    public static final String DEFAULT_GEAR_FONT_NAME = "GearFont";
    
    private final BooleanProperty hideWhenViewingOtherCar = new BooleanProperty( this, "hideWhenViewingOtherCar", false );
    
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.IMAGE_INDICATOR + "default_rev_meter_bg.png" );
    }
    
    @Override
    protected void onBackgroundChanged( float deltaScaleX, float deltaScaleY )
    {
        super.onBackgroundChanged( deltaScaleX, deltaScaleY );
        
        // TODO: Don't set to null!
        for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
            shiftLights[i].resetTextures();
        gearBackgroundTexture = null;
        gearBackgroundTexture_bak = null;
        boostNumberBackgroundTexture = null;
        boostNumberBackgroundTexture_bak = null;
        
        if ( deltaScaleX > 0f )
        {
            gearPosX.setIntValue( Math.round( gearPosX.getIntValue() * deltaScaleX ) );
            gearPosY.setIntValue( Math.round( gearPosY.getIntValue() * deltaScaleY ) );
            boostBarPosX.setIntValue( Math.round( boostBarPosX.getIntValue() * deltaScaleX ) );
            boostBarPosY.setIntValue( Math.round( boostBarPosY.getIntValue() * deltaScaleY ) );
            boostBarWidth.setIntValue( Math.round( boostBarWidth.getIntValue() * deltaScaleX ) );
            boostBarHeight.setIntValue( Math.round( boostBarHeight.getIntValue() * deltaScaleY ) );
            boostNumberPosX.setIntValue( Math.round( boostNumberPosX.getIntValue() * deltaScaleX ) );
            boostNumberPosY.setIntValue( Math.round( boostNumberPosY.getIntValue() * deltaScaleY ) );
            rpmPosX1.setIntValue( Math.round( rpmPosX1.getIntValue() * deltaScaleX ) );
            rpmPosY1.setIntValue( Math.round( rpmPosY1.getIntValue() * deltaScaleY ) );
            rpmPosX2.setIntValue( Math.round( rpmPosX2.getIntValue() * deltaScaleX ) );
            rpmPosY2.setIntValue( Math.round( rpmPosY2.getIntValue() * deltaScaleY ) );
        }
    }
    
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
    
    private final ImageProperty gearBackgroundImageName = new ImageProperty( this, "gearBackgroundImageName", "backgroundImageName", "", false, true );
    private final ImageProperty boostNumberBackgroundImageName = new ImageProperty( this, "boostNumberBackgroundImageName", "numberBGImageName", "", false, true );
    
    private TransformableTexture gearBackgroundTexture = null;
    private TextureImage2D gearBackgroundTexture_bak = null;
    private TransformableTexture boostNumberBackgroundTexture = null;
    private TextureImage2D boostNumberBackgroundTexture_bak = null;
    
    private int gearBackgroundTexPosX, gearBackgroundTexPosY;
    private int boostNumberBackgroundTexPosX, boostNumberBackgroundTexPosY;
    
    private final BooleanProperty useMaxRevLimit = new BooleanProperty( this, "useMaxRevLimit", true );
    private final ColorProperty revMarkersMediumColor = new ColorProperty( this, "revMarkersMediumColor", "mediumColor", "#FFFF00" );
    private final ColorProperty revMarkersHighColor = new ColorProperty( this, "revMarkersHighColor", "highColor", "#FF0000" );
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
    
    private final BooleanProperty displayGear = new BooleanProperty( this, "displayGear", "displayGear", true );
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
    
    private final BooleanProperty displayRPMString1 = new BooleanProperty( this, "displayRPMString1", "displayRPMString", true );
    private final BooleanProperty displayCurrRPM1 = new BooleanProperty( this, "displayCurrRPM1", "displayCurrRPM", true );
    private final BooleanProperty displayMaxRPM1 = new BooleanProperty( this, "displayMaxRPM1", "displayMaxRPM", true );
    private final BooleanProperty useBoostRevLimit1 = new BooleanProperty( this, "useBoostRevLimit1", "useBoostRevLimit", false );
    private final IntProperty rpmPosX1 = new IntProperty( this, "rpmPosX1", "rpmPosX", 170 );
    private final IntProperty rpmPosY1 = new IntProperty( this, "rpmPosY1", "rpmPosY", 603 );
    private final FontProperty rpmFont1 = new FontProperty( this, "rpmFont1", "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty rpmFontColor1 = new ColorProperty( this, "rpmFontColor1", "fontColor", ColorProperty.STANDARD_FONT_COLOR_NAME );
    private final StringProperty rpmJoinString1 = new StringProperty( this, "rpmJoinString1", "rpmJoinString", " / " );
    
    private final BooleanProperty displayRPMString2 = new BooleanProperty( this, "displayRPMString2", "displayRPMString", false );
    private final BooleanProperty displayCurrRPM2 = new BooleanProperty( this, "displayCurrRPM2", "displayCurrRPM", true );
    private final BooleanProperty displayMaxRPM2 = new BooleanProperty( this, "displayMaxRPM2", "displayMaxRPM", true );
    private final BooleanProperty useBoostRevLimit2 = new BooleanProperty( this, "useBoostRevLimit2", "useBoostRevLimit", false );
    private final IntProperty rpmPosX2 = new IntProperty( this, "rpmPosX2", "rpmPosX", 170 );
    private final IntProperty rpmPosY2 = new IntProperty( this, "rpmPosY2", "rpmPosY", 603 );
    private final FontProperty rpmFont2 = new FontProperty( this, "rpmFont2", "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty rpmFontColor2 = new ColorProperty( this, "rpmFontColor2", "fontColor", ColorProperty.STANDARD_FONT_COLOR_NAME );
    private final StringProperty rpmJoinString2 = new StringProperty( this, "rpmJoinString2", "rpmJoinString", " / " );
    
    private DrawnString rpmString1 = null;
    private DrawnString rpmString2 = null;
    private DrawnString gearString = null;
    private DrawnString boostString = null;
    
    private final FloatValue maxRPMCheck = new FloatValue();
    private final IntValue gear = new IntValue();
    private final IntValue boost = new IntValue();
    
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
    
    @Override
    protected float getMinValue( LiveGameData gameData, EditorPresets editorPresets )
    {
        return ( 0 );
    }
    
    @Override
    protected float getMaxValue( LiveGameData gameData, EditorPresets editorPresets )
    {
        if ( useMaxRevLimit.getBooleanValue() )
            return ( gameData.getPhysics().getEngine().getRevLimitRange().getMaxValue() );
        
        return ( gameData.getTelemetryData().getEngineMaxRPM() );
    }
    
    @Override
    protected float getValue( LiveGameData gameData, EditorPresets editorPresets )
    {
        return ( ( editorPresets != null ) ? editorPresets.getEngineRPM() : gameData.getTelemetryData().getEngineRPM() );
    }
    
    @Override
    protected int getValueForValueDisplay( LiveGameData gameData, EditorPresets editorPresets )
    {
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
        if ( vsi.isPlayer() )
            return ( Math.round( gameData.getTelemetryData().getScalarVelocity() ) );
        
        return ( Math.round( vsi.getScalarVelocity() ) );
    }
    
    @Override
    protected String getMarkerLabelForValue( LiveGameData gameData, EditorPresets editorPresets, float value )
    {
        return ( String.valueOf( Math.round( value / 1000 ) ) );
    }
    
    private int loadGearBackgroundTexture( boolean isEditorMode )
    {
        if ( !displayGear.getBooleanValue() )
        {
            gearBackgroundTexture = null;
            gearBackgroundTexture_bak = null;
            return ( 0 );
        }
        
        try
        {
            ImageTemplate it = gearBackgroundImageName.getImage();
            
            if ( it == null )
            {
                gearBackgroundTexture = null;
                gearBackgroundTexture_bak = null;
                return ( 0 );
            }
            
            float scale = getBackground().getScaleX();
            int w = Math.round( it.getBaseWidth() * scale );
            int h = Math.round( it.getBaseHeight() * scale );
            if ( ( gearBackgroundTexture == null ) || ( gearBackgroundTexture.getWidth() != w ) || ( gearBackgroundTexture.getHeight() != h ) )
            {
                gearBackgroundTexture = it.getScaledTransformableTexture( w, h, gearBackgroundTexture, isEditorMode );
                gearBackgroundTexture.setDynamic( true );
                
                gearBackgroundTexture_bak = TextureImage2D.getOrCreateDrawTexture( gearBackgroundTexture.getWidth(), gearBackgroundTexture.getHeight(), gearBackgroundTexture.getTexture().hasAlphaChannel(), gearBackgroundTexture_bak, isEditorMode );
                gearBackgroundTexture_bak.clear( gearBackgroundTexture.getTexture(), true, null );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( 0 );
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
        
        try
        {
            ImageTemplate it = boostNumberBackgroundImageName.getImage();
            
            if ( it == null )
            {
                boostNumberBackgroundTexture = null;
                boostNumberBackgroundTexture_bak = null;
                return ( 0 );
            }
            
            float scale = getBackground().getScaleX();
            int w = Math.round( it.getBaseWidth() * scale );
            int h = Math.round( it.getBaseHeight() * scale );
            if ( ( boostNumberBackgroundTexture == null ) || ( boostNumberBackgroundTexture.getWidth() != w ) || ( boostNumberBackgroundTexture.getHeight() != h ) )
            {
                boostNumberBackgroundTexture = it.getScaledTransformableTexture( w, h, boostNumberBackgroundTexture, isEditorMode );
                boostNumberBackgroundTexture.setDynamic( true );
                
                boostNumberBackgroundTexture_bak = TextureImage2D.getOrCreateDrawTexture( boostNumberBackgroundTexture.getWidth(), boostNumberBackgroundTexture.getHeight(), boostNumberBackgroundTexture.getTexture().hasAlphaChannel(), boostNumberBackgroundTexture_bak, isEditorMode );
                boostNumberBackgroundTexture_bak.clear( boostNumberBackgroundTexture.getTexture(), true, null );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( 0 );
        }
        
        return ( 1 );
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        TransformableTexture[] superResult = super.getSubTexturesImpl( gameData, editorPresets, widgetInnerWidth, widgetInnerHeight );
        
        final boolean isEditorMode = ( editorPresets != null );
        
        int n = 0;
        
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            n += shiftLights[s].loadTextures( isEditorMode );
        
        n += loadGearBackgroundTexture( isEditorMode );
        n += loadBoostNumberBackgroundTexture( isEditorMode );
        
        TransformableTexture[] result = new TransformableTexture[ superResult.length + n ];
        System.arraycopy( superResult, 0, result, 0, superResult.length );
        
        int i = superResult.length;
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            i = shiftLights[s].writeTexturesToArray( result, i );
        if ( gearBackgroundTexture != null )
            result[i++] = gearBackgroundTexture;
        if ( boostNumberBackgroundTexture != null )
            result[i++] = boostNumberBackgroundTexture;
        
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        super.initialize( clock1, clock2, gameData, editorPresets, dsf, texture, offsetX, offsetY, width, height );
        
        final boolean isEditorMode = ( editorPresets != null );
        final Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            shiftLights[s].loadTextures( isEditorMode );
        
        FontMetrics metrics = null;
        Rectangle2D bounds = null;
        double fw = 0, fh = 0;
        double fd = 0;
        int fx = 0, fy = 0;
        
        loadGearBackgroundTexture( isEditorMode );
        
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
        
        loadBoostNumberBackgroundTexture( isEditorMode );
        
        if ( displayBoostNumber.getBooleanValue() )
        {
            metrics = boostNumberFont.getMetrics();
            bounds = metrics.getStringBounds( "0", texCanvas );
            fw = bounds.getWidth();
            fd = metrics.getDescent();
            fh = metrics.getAscent() - fd;
            
            if ( boostNumberBackgroundTexture == null )
            {
                fx = Math.round( boostNumberPosX.getIntValue() * getBackground().getScaleX() );
                fy = Math.round( boostNumberPosY.getIntValue() * getBackground().getScaleY() );
            }
            else
            {
                boostNumberBackgroundTexPosX = Math.round( boostNumberPosX.getIntValue() * getBackground().getScaleX() - boostNumberBackgroundTexture.getWidth() / 2.0f );
                boostNumberBackgroundTexPosY = Math.round( boostNumberPosY.getIntValue() * getBackground().getScaleY() - boostNumberBackgroundTexture.getHeight() / 2.0f );
                
                fx = boostNumberBackgroundTexture.getWidth() / 2;
                fy = boostNumberBackgroundTexture.getHeight() / 2;
            }
        }
        
        boostString = dsf.newDrawnStringIf( displayBoostNumber.getBooleanValue(), "boostString", fx - (int)( fw / 2.0 ), fy - (int)( fd + fh / 2.0 ), Alignment.LEFT, false, boostNumberFont.getFont(), boostNumberFont.isAntiAliased(), boostNumberFontColor.getColor() );
        
        rpmString1 = dsf.newDrawnStringIf( displayRPMString1.getBooleanValue(), "rpmString1", width - Math.round( rpmPosX1.getIntValue() * getBackground().getScaleX() ), Math.round( rpmPosY1.getIntValue() * getBackground().getScaleY() ), Alignment.RIGHT, false, rpmFont1.getFont(), rpmFont1.isAntiAliased(), rpmFontColor1.getColor() );
        rpmString2 = dsf.newDrawnStringIf( displayRPMString2.getBooleanValue(), "rpmString2", width - Math.round( rpmPosX2.getIntValue() * getBackground().getScaleX() ), Math.round( rpmPosY2.getIntValue() * getBackground().getScaleY() ), Alignment.RIGHT, false, rpmFont2.getFont(), rpmFont2.isAntiAliased(), rpmFontColor2.getColor() );
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
    
    private float lowRPM = -1f;
    private float mediumRPM = -1f;
    
    @Override
    protected void prepareMarkersBackground( LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, float innerRadius, float bigOuterRadius, float smallOuterRadius )
    {
        super.prepareMarkersBackground( gameData, editorPresets, texCanvas, offsetX, offsetY, width, height, innerRadius, bigOuterRadius, smallOuterRadius );
        
        if ( !fillHighBackground.getBooleanValue() )
            return;
        
        float centerX = offsetX + width / 2;
        float centerY = offsetY + height / 2;
        
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
        
        Shape oldClip = texCanvas.getClip();
        
        Polygon p = new Polygon( xPoints, yPoints, nPoints );
        Area area = new Area( oldClip );
        area.subtract( new Area( p ) );
        
        texCanvas.setClip( area );
        
        int maxValue = (int)getMaxValue( gameData, editorPresets );
        
        float lowAngle = ( needleRotationForMinValue.getFloatValue() + ( needleRotationForMaxValue.getFloatValue() - needleRotationForMinValue.getFloatValue() ) * ( lowRPM / maxValue ) );
        //float mediumAngle = ( needleRotationForMinValue.getFloatValue() + ( needleRotationForMaxValue.getFloatValue() - needleRotationForMinValue.getFloatValue() ) * ( mediumRPM / maxRPM ) );
        float maxAngle = needleRotationForMaxValue.getFloatValue();
        float oneDegree = 1; //(float)( Math.PI / 180.0 );
        
        for ( float angle = lowAngle; angle < maxAngle - oneDegree; angle += oneDegree )
        {
            int rpm = Math.round( ( angle - needleRotationForMinValue.getFloatValue() ) * maxValue / ( needleRotationForMaxValue.getFloatValue() - needleRotationForMinValue.getFloatValue() ) );
            
            if ( rpm <= mediumRPM )
                texCanvas.setColor( interpolateMarkerColors.getBooleanValue() ? interpolateColor( markersColor.getColor(), revMarkersMediumColor.getColor(), ( rpm - lowRPM ) / ( mediumRPM - lowRPM ) ) : revMarkersMediumColor.getColor() );
            else
                texCanvas.setColor( interpolateMarkerColors.getBooleanValue() ? interpolateColor( revMarkersMediumColor.getColor(), revMarkersHighColor.getColor(), ( rpm - mediumRPM ) / ( maxValue - mediumRPM ) ) : revMarkersHighColor.getColor() );
            
            texCanvas.fillArc( Math.round( centerX - smallOuterRadius ), Math.round( centerY - smallOuterRadius ), Math.round( smallOuterRadius + smallOuterRadius ), Math.round( smallOuterRadius + smallOuterRadius ), Math.round( 90f - angle ), ( angle < maxAngle - oneDegree - oneDegree ) ? -2 : -1 );
        }
        
        //texCanvas.setClip( oldClip );
    }
    
    /*
    @Override
    protected boolean getDisplayMarkers()
    {
        return ( super.getDisplayMarkers() || fillHighBackground.getBooleanValue() );
    }
    */
    
    @Override
    protected Color getMarkerColorForValue( LiveGameData gameData, EditorPresets editorPresets, int value, int minValue, int maxValue )
    {
        if ( fillHighBackground.getBooleanValue() || ( value <= lowRPM ) )
            return ( markersColor.getColor() );
        
        if ( value <= mediumRPM )
            return ( interpolateMarkerColors.getBooleanValue() ? interpolateColor( markersColor.getColor(), revMarkersMediumColor.getColor(), ( value - lowRPM ) / ( mediumRPM - lowRPM ) ) : revMarkersMediumColor.getColor() );
        
        return ( interpolateMarkerColors.getBooleanValue() ? interpolateColor( revMarkersMediumColor.getColor(), revMarkersHighColor.getColor(), ( value - mediumRPM ) / ( maxValue - mediumRPM ) ) : revMarkersHighColor.getColor() );
    }
    
    @Override
    protected void drawMarkers( LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        float baseMaxRPM = gameData.getTelemetryData().getEngineBaseMaxRPM();
        
        VehiclePhysics.Engine engine = gameData.getPhysics().getEngine();
        PhysicsSetting boostRange = engine.getBoostRange();
        
        int minBoost = ( engine.getRPMIncreasePerBoostLevel() > 0f ) ? (int)boostRange.getMinValue() : (int)boostRange.getMaxValue();
        //int maxBoost = ( engine.getRPMIncreasePerBoostLevel() > 0f ) ? (int)boostRange.getMaxValue() : (int)boostRange.getMinValue();
        int mediumBoost = Math.round( boostRange.getMinValue() + ( boostRange.getMaxValue() - boostRange.getMinValue() ) / 2f );
        
        lowRPM = gameData.getPhysics().getEngine().getMaxRPM( baseMaxRPM, minBoost );
        mediumRPM = gameData.getPhysics().getEngine().getMaxRPM( baseMaxRPM, mediumBoost );
        
        super.drawMarkers( gameData, editorPresets, texCanvas, offsetX, offsetY, width, height );
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
    protected boolean doRenderNeedle( LiveGameData gameData, EditorPresets editorPresets )
    {
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
        return ( vsi.isPlayer() );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        super.drawWidget( clock1, clock2, needsCompleteRedraw, gameData, editorPresets, texture, offsetX, offsetY, width, height );
        
        final boolean isEditorMode = ( editorPresets != null );
        
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
        
        boost.update( vsi.isPlayer() ? telemData.getEffectiveEngineBoostMapping() : gameData.getPhysics().getEngine().getLowestBoostLevel() );
        
        if ( needsCompleteRedraw || boost.hasChanged() )
        {
            if ( displayBoostNumber.getBooleanValue() )
            {
                if ( boostNumberBackgroundTexture == null )
                {
                    boostString.draw( offsetX, offsetY, boost.getValueAsString(), texture );
                }
                else
                {
                    if ( needsCompleteRedraw )
                        boostNumberBackgroundTexture.getTexture().clear( boostNumberBackgroundTexture_bak, true, null );
                    
                    boostString.draw( 0, 0, boost.getValueAsString(), boostNumberBackgroundTexture.getTexture(), boostNumberBackgroundTexture_bak, 0, 0 );
                }
            }
            
            if ( displayBoostBar.getBooleanValue() )
            {
                int maxBoost = (int)gameData.getPhysics().getEngine().getBoostRange().getMaxValue();
                boolean inverted = ( gameData.getPhysics().getEngine().getRPMIncreasePerBoostLevel() < 0f );
                boolean tempBoost = false;
                drawBoostBar( boost.getValue(), maxBoost, inverted, tempBoost, texture.getTextureCanvas(), offsetX + Math.round( boostBarPosX.getIntValue() * getBackground().getScaleY() ), offsetY + Math.round( boostBarPosY.getIntValue() * getBackground().getScaleY() ), Math.round( boostBarWidth.getIntValue() * getBackground().getScaleX() ), Math.round( boostBarHeight.getIntValue() * getBackground().getScaleY() ) );
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
            
            rpmString1.draw( offsetX, offsetY, string, texture );
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
            
            rpmString2.draw( offsetX, offsetY, string, texture );
        }
        
        if ( numShiftLights.getIntValue() > 0 )
        {
            float rpm2 = vsi.isPlayer() ? rpm : 0f;
            float baseMaxRPM = vsi.isPlayer() ? telemData.getEngineBaseMaxRPM() : 100000f;
            for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
                shiftLights[s].updateTextures( gameData, rpm2, baseMaxRPM, boost.getValue(), getBackground().getScaleX(), getBackground().getScaleY() );
        }
        
        if ( gearBackgroundTexture != null )
            gearBackgroundTexture.setTranslation( gearBackgroundTexPosX, gearBackgroundTexPosY );
        
        if ( boostNumberBackgroundTexture != null )
            boostNumberBackgroundTexture.setTranslation( boostNumberBackgroundTexPosX, boostNumberBackgroundTexPosY );
    }
    
    @Override
    protected void initParentProperties()
    {
        super.initParentProperties();
        
        markersBigStep.setIntValue( 1000 );
        markersSmallStep.setIntValue( 200 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( hideWhenViewingOtherCar, "Hide the Widget when another car is being observed?" );
        writer.writeProperty( useMaxRevLimit, "Whether to use maximum possible (by setup) rev limit" );
        writer.writeProperty( revMarkersMediumColor, "The color used to draw the rev markers for medium boost." );
        writer.writeProperty( revMarkersHighColor, "The color used to draw the rev markers for high revs." );
        writer.writeProperty( fillHighBackground, "Fill the rev markers' background with medium and high color instead of coloring the markers." );
        writer.writeProperty( interpolateMarkerColors, "Interpolate medium and high colors." );
        writer.writeProperty( numShiftLights, "The number of shift lights to render." );
        for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
            shiftLights[i].saveProperties( writer );
        writer.writeProperty( displayGear, "Display the gear?" );
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
    
    private boolean loadShiftLightProperty( PropertyLoader loader )
    {
        for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
            if ( shiftLights[i].loadProperty( loader ) )
                return ( true );
        
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( hideWhenViewingOtherCar ) );
        else if ( loader.loadProperty( numShiftLights ) )
        {
            for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
                shiftLights[i] = new ShiftLight( this, i + 1 );
        }
        else if ( loadShiftLightProperty( loader ) );
        else if ( loader.loadProperty( useMaxRevLimit ) );
        else if ( loader.loadProperty( revMarkersMediumColor ) );
        else if ( loader.loadProperty( revMarkersHighColor ) );
        else if ( loader.loadProperty( fillHighBackground ) );
        else if ( loader.loadProperty( interpolateMarkerColors ) );
        else if ( loader.loadProperty( displayGear ) );
        else if ( loader.loadProperty( gearBackgroundImageName ) );
        else if ( loader.loadProperty( gearPosX ) );
        else if ( loader.loadProperty( gearPosY ) );
        else if ( loader.loadProperty( gearFont ) );
        else if ( loader.loadProperty( gearFontColor ) );
        else if ( loader.loadProperty( displayBoostBar ) );
        else if ( loader.loadProperty( boostBarPosX ) );
        else if ( loader.loadProperty( boostBarPosY ) );
        else if ( loader.loadProperty( boostBarWidth ) );
        else if ( loader.loadProperty( boostBarHeight ) );
        else if ( loader.loadProperty( displayBoostNumber ) );
        else if ( loader.loadProperty( boostNumberBackgroundImageName ) );
        else if ( loader.loadProperty( boostNumberPosX ) );
        else if ( loader.loadProperty( boostNumberPosY ) );
        else if ( loader.loadProperty( boostNumberFont ) );
        else if ( loader.loadProperty( boostNumberFontColor ) );
        else if ( loader.loadProperty( displayRPMString1 ) );
        else if ( loader.loadProperty( displayCurrRPM1 ) );
        else if ( loader.loadProperty( displayMaxRPM1 ) );
        else if ( loader.loadProperty( useBoostRevLimit1 ) );
        else if ( loader.loadProperty( rpmPosX1 ) );
        else if ( loader.loadProperty( rpmPosY1 ) );
        else if ( loader.loadProperty( rpmFont1 ) );
        else if ( loader.loadProperty( rpmFontColor1 ) );
        else if ( loader.loadProperty( rpmJoinString1 ) );
        else if ( loader.loadProperty( displayRPMString2 ) );
        else if ( loader.loadProperty( displayCurrRPM2 ) );
        else if ( loader.loadProperty( displayMaxRPM2 ) );
        else if ( loader.loadProperty( useBoostRevLimit2 ) );
        else if ( loader.loadProperty( rpmPosX2 ) );
        else if ( loader.loadProperty( rpmPosY2 ) );
        else if ( loader.loadProperty( rpmFont2 ) );
        else if ( loader.loadProperty( rpmFontColor2 ) );
        else if ( loader.loadProperty( rpmJoinString2 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean getSpecificPropertiesFirst( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        if ( super.getSpecificPropertiesFirst( propsCont, forceAll ) )
            propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( hideWhenViewingOtherCar );
        
        return ( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void getMarkersProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getMarkersProperties( propsCont, forceAll );
        
        propsCont.addProperty( useMaxRevLimit );
        propsCont.addProperty( revMarkersMediumColor );
        propsCont.addProperty( revMarkersHighColor );
        propsCont.addProperty( fillHighBackground );
        propsCont.addProperty( interpolateMarkerColors );
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
        
        propsCont.addProperty( displayGear );
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
        
        numShiftLights.setIntValue( 2 );
        initShiftLights( 0, 2 );
    }
}
