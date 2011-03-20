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
package net.ctdp.rfdynhud.widgets.standard.revmeter;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.AbstractPropertiesKeeper;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.GenericPropertiesIterator;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.revneedlemeter.AbstractRevNeedleMeterWidget;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetSet;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link RevMeterWidget} displays rev/RPM information.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class RevMeterWidget extends AbstractRevNeedleMeterWidget
{
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
    
    protected final ColorProperty revMarkersMediumColor = new ColorProperty( "revMarkersMediumColor", "mediumColor", "#FFFF00" );
    protected final ColorProperty revMarkersHighColor = new ColorProperty( "revMarkersHighColor", "highColor", "#FF0000" );
    protected final BooleanProperty fillHighBackground = new BooleanProperty( "fillHighBackground", false );
    protected final BooleanProperty interpolateMarkerColors = new BooleanProperty( "interpolateMarkerColors", "interpolateColors", false );
    
    
    private void initShiftLights( int oldNumber, int newNumber )
    {
        for ( int i = oldNumber; i < newNumber; i++ )
        {
            if ( shiftLights[i] == null )
            {
                shiftLights[i] = new ShiftLight( this, i + 1 );
                
                GenericPropertiesIterator it = new GenericPropertiesIterator( shiftLights[i] );
                while ( it.hasNext() )
                    AbstractPropertiesKeeper.setKeeper( it.next(), this );
                
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
    
    private final IntProperty numShiftLights = new IntProperty( "numShiftLights", 0, 0, 20 )
    {
        @Override
        protected void onValueChanged( Integer oldValue, int newValue )
        {
            if ( oldValue == null )
                oldValue = 0;
            //if ( oldValue != null )
                initShiftLights( oldValue, newValue );
        }
    };
    
    private final ShiftLight[] shiftLights = { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null };
    
    protected final BooleanProperty displayBoostBar = new BooleanProperty( "displayBoostBar", "displayBar", true );
    protected final IntProperty boostBarPosX = new IntProperty( "boostBarPosX", "barPosX", 135 );
    protected final IntProperty boostBarPosY = new IntProperty( "boostBarPosY", "barPosY", 671 );
    protected final IntProperty boostBarWidth = new IntProperty( "boostBarWidth", "barWidth", 438 );
    protected final IntProperty boostBarHeight = new IntProperty( "boostBarHeight", "barHeight", 27 );
    protected final BooleanProperty displayBoostNumber = new BooleanProperty( "displayBoostNumber", "displayNumber", true );
    protected final ImageProperty boostNumberBackgroundImageName = new ImageProperty( "boostNumberBackgroundImageName", "numBGImageName", "", false, true );
    private TransformableTexture boostNumberBackgroundTexture = null;
    private TextureImage2D boostNumberBackgroundTexture_bak = null;
    private int boostNumberBackgroundTexPosX, boostNumberBackgroundTexPosY;
    protected final IntProperty boostNumberPosX = new IntProperty( "boostNumberPosX", "numberPosX", 392 );
    protected final IntProperty boostNumberPosY = new IntProperty( "boostNumberPosY", "numberPosY", 544 );
    protected final FontProperty boostNumberFont = new FontProperty( "boostNumberFont", "numberFont", FontProperty.STANDARD_FONT.getKey() );
    protected final ColorProperty boostNumberFontColor = new ColorProperty( "boostNumberFontColor", "numberFontColor", "#FF0000" );
    
    protected final BooleanProperty displayRPMString1 = new BooleanProperty( "displayRPMString1", "displayRPMString", true );
    protected final BooleanProperty displayCurrRPM1 = new BooleanProperty( "displayCurrRPM1", "displayCurrRPM", true );
    protected final BooleanProperty displayMaxRPM1 = new BooleanProperty( "displayMaxRPM1", "displayMaxRPM", true );
    protected final BooleanProperty useBoostRevLimit1 = new BooleanProperty( "useBoostRevLimit1", "useBoostRevLimit", false );
    protected final IntProperty rpmPosX1 = new IntProperty( "rpmPosX1", "rpmPosX", 170 );
    protected final IntProperty rpmPosY1 = new IntProperty( "rpmPosY1", "rpmPosY", 603 );
    protected final FontProperty rpmFont1 = new FontProperty( "rpmFont1", "font", FontProperty.STANDARD_FONT.getKey() );
    protected final ColorProperty rpmFontColor1 = new ColorProperty( "rpmFontColor1", "fontColor", ColorProperty.STANDARD_FONT_COLOR.getKey() );
    protected final StringProperty rpmJoinString1 = new StringProperty( "rpmJoinString1", "rpmJoinString", " / " );
    
    protected final BooleanProperty displayRPMString2 = new BooleanProperty( "displayRPMString2", "displayRPMString", false );
    protected final BooleanProperty displayCurrRPM2 = new BooleanProperty( "displayCurrRPM2", "displayCurrRPM", true );
    protected final BooleanProperty displayMaxRPM2 = new BooleanProperty( "displayMaxRPM2", "displayMaxRPM", true );
    protected final BooleanProperty useBoostRevLimit2 = new BooleanProperty( "useBoostRevLimit2", "useBoostRevLimit", false );
    protected final IntProperty rpmPosX2 = new IntProperty( "rpmPosX2", "rpmPosX", 170 );
    protected final IntProperty rpmPosY2 = new IntProperty( "rpmPosY2", "rpmPosY", 603 );
    protected final FontProperty rpmFont2 = new FontProperty( "rpmFont2", "font", FontProperty.STANDARD_FONT.getKey() );
    protected final ColorProperty rpmFontColor2 = new ColorProperty( "rpmFontColor2", "fontColor", ColorProperty.STANDARD_FONT_COLOR.getKey() );
    protected final StringProperty rpmJoinString2 = new StringProperty( "rpmJoinString2", "rpmJoinString", " / " );
    
    private DrawnString rpmString1 = null;
    private DrawnString rpmString2 = null;
    private DrawnString boostString = null;
    
    private final IntValue boost = new IntValue();
    
    public RevMeterWidget( WidgetSet widgetSet, WidgetPackage widgetPackage, float width, float height )
    {
        super( widgetSet, widgetPackage, width, height );
        
        int initialShiftLights = getInitialNumberOfShiftLights();
        if ( initialShiftLights != 0 )
        {
            numShiftLights.setIntValue( initialShiftLights );
            initShiftLights( 0, numShiftLights.getIntValue() );
        }
    }
    
    public RevMeterWidget()
    {
        this( StandardWidgetSet.INSTANCE, StandardWidgetSet.WIDGET_PACKAGE, 16.3125f, 21.75f );
    }
    
    protected int getInitialNumberOfShiftLights()
    {
        return ( 2 );
    }
    
    @Override
    protected void saveMarkersProperties( PropertyWriter writer ) throws IOException
    {
        super.saveMarkersProperties( writer );
        
        writer.writeProperty( revMarkersMediumColor, "The color used to draw the rev markers for medium boost." );
        writer.writeProperty( revMarkersHighColor, "The color used to draw the rev markers for high revs." );
        writer.writeProperty( fillHighBackground, "Fill the rev markers' background with medium and high color instead of coloring the markers." );
        writer.writeProperty( interpolateMarkerColors, "Interpolate medium and high colors." );
    }
    
    protected void saveShiftLightsProperties( PropertyWriter writer ) throws IOException
    {
        writer.writeProperty( numShiftLights, "The number of shift lights to render." );
        for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
            shiftLights[i].saveProperties( writer );
    }
    
    protected void saveBoostProperties( PropertyWriter writer ) throws IOException
    {
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
    }
    
    protected void saveDigiRevsProperties( PropertyWriter writer ) throws IOException
    {
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        saveShiftLightsProperties( writer );
        
        saveBoostProperties( writer );
        
        saveDigiRevsProperties( writer );
    }
    
    private boolean loadShiftLightProperty( PropertyLoader loader )
    {
        for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
            if ( shiftLights[i].loadProperty( loader ) )
                return ( true );
        
        return ( false );
    }
    
    private void handleBackwardsCompatiblity( PropertyLoader loader )
    {
        if ( loader.getCurrentKey().equals( "displayRevMarkers" ) )
            displayMarkers.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "displayRevMarkerNumbers" ) )
            displayMarkerNumbers.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "revMarkersInnerRadius" ) )
            markersInnerRadius.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "revMarkersLength" ) )
            markersLength.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "revMarkersBigStep" ) )
            markersBigStep.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "revMarkersSmallStep" ) )
            markersSmallStep.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "revMarkersColor" ) )
            markersColor.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "revMarkersFont" ) )
            markersFont.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "revMarkersFontColor" ) )
            markersFontColor.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "displayVelocity" ) )
            displayValue.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "velocityBackgroundImageName" ) )
            valueBackgroundImageName.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "velocityPosX" ) )
            valuePosX.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "velocityPosY" ) )
            valuePosY.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "velocityFont" ) )
            valueFont.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "velocityFontColor" ) )
            valueFontColor.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "needleAxisBottomOffset" ) )
            needlePivotBottomOffset.loadValue( loader, loader.getCurrentValue() );
        else if ( loader.getCurrentKey().equals( "rotationForZeroRPM" ) )
            { needleRotationForMinValue.loadValue( loader, loader.getCurrentValue() ); needleRotationForMinValue.setFloatValue( -needleRotationForMinValue.getFloatValue() ); }
        else if ( loader.getCurrentKey().equals( "rotationForMaxRPM" ) )
            { needleRotationForMaxValue.loadValue( loader, loader.getCurrentValue() ); needleRotationForMaxValue.setFloatValue( -needleRotationForMaxValue.getFloatValue() ); }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.getSourceVersion().getBuild() < 70 )
        {
            handleBackwardsCompatiblity( loader );
        }
        
        if ( loader.loadProperty( numShiftLights ) )
        {
            for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
                shiftLights[i] = new ShiftLight( this, i + 1 );
        }
        else if ( loadShiftLightProperty( loader ) );
        
        else if ( loader.loadProperty( revMarkersMediumColor ) );
        else if ( loader.loadProperty( revMarkersHighColor ) );
        else if ( loader.loadProperty( fillHighBackground ) );
        else if ( loader.loadProperty( interpolateMarkerColors ) );
        
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
        
        if ( ( loader.getSourceVersion().getBuild() < 70 ) && loader.getCurrentKey().equals( "/" ) )
        {
            // Properties loading has finished for this Widget and the sourceversion is outdated.
            if ( this.getNeedleImage() == null )
                peakNeedleImageName.setImageName( "" );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void getMarkersProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getMarkersProperties( propsCont, forceAll );
        
        propsCont.addProperty( revMarkersMediumColor );
        propsCont.addProperty( revMarkersHighColor );
        propsCont.addProperty( fillHighBackground );
        propsCont.addProperty( interpolateMarkerColors );
    }
    
    /**
     * Collects the properties for the shift lights.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void getShiftLightsProperties( PropertiesContainer propsCont, boolean forceAll )
    {
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
    }
    
    /**
     * Collects the properties for the engine boost.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void getBoostProperties( PropertiesContainer propsCont, boolean forceAll )
    {
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
    }
    
    /**
     * Collects the properties for the digital revs.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void getDigiRevsProperties( PropertiesContainer propsCont, boolean forceAll )
    {
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
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        getShiftLightsProperties( propsCont, forceAll );
        
        getBoostProperties( propsCont, forceAll );
        
        getDigiRevsProperties( propsCont, forceAll );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean cloneProperty( Property src, Property trg )
    {
        boolean result = super.cloneProperty( src, trg );
        
        if ( trg == numShiftLights )
            return ( true );
        
        return ( result );
    }
    
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.IMAGE_INDICATOR + "standard/rev_meter_bg.png" );
    }
    
    @Override
    protected void onBackgroundChanged( boolean imageChanged, float deltaScaleX, float deltaScaleY )
    {
        super.onBackgroundChanged( imageChanged, deltaScaleX, deltaScaleY );
        
        for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
            shiftLights[i].onBackgroundChanged( imageChanged, deltaScaleX, deltaScaleY );
        
        // TODO: Don't set to null!
        boostNumberBackgroundTexture = null;
        boostNumberBackgroundTexture_bak = null;
        
        if ( deltaScaleX > 0f )
        {
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
    protected String getMarkerLabelForValue( LiveGameData gameData, boolean isEditorMode, float value )
    {
        return ( String.valueOf( Math.round( value / 1000 ) ) );
    }
    
    private boolean loadBoostNumberBackgroundTexture( boolean isEditorMode )
    {
        if ( !displayBoostNumber.getBooleanValue() )
        {
            boostNumberBackgroundTexture = null;
            boostNumberBackgroundTexture_bak = null;
            return ( false );
        }
        
        try
        {
            ImageTemplate it = boostNumberBackgroundImageName.getImage();
            
            if ( it == null )
            {
                boostNumberBackgroundTexture = null;
                boostNumberBackgroundTexture_bak = null;
                return ( false );
            }
            
            float scale = getBackground().getScaleX();
            int w = Math.round( it.getBaseWidth() * scale );
            int h = Math.round( it.getBaseHeight() * scale );
            boolean[] changeInfo = new boolean[ 2 ];
            boostNumberBackgroundTexture = it.getScaledTransformableTexture( w, h, boostNumberBackgroundTexture, isEditorMode, changeInfo );
            boostNumberBackgroundTexture.setDynamic( true );
            
            if ( changeInfo[1] ) // redrawn
            {
                boostNumberBackgroundTexture_bak = TextureImage2D.getOrCreateDrawTexture( w, h, it.hasAlpha(), boostNumberBackgroundTexture_bak, isEditorMode );
                boostNumberBackgroundTexture_bak.clear( boostNumberBackgroundTexture.getTexture(), true, null );
            }
        }
        catch ( Throwable t )
        {
            log( t );
            
            return ( false );
        }
        
        return ( true );
    }
    
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        super.initSubTextures( gameData, isEditorMode, widgetInnerWidth, widgetInnerHeight, collector );
        
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            shiftLights[s].loadTextures( isEditorMode, collector );
        
        if ( loadBoostNumberBackgroundTexture( isEditorMode ) )
            collector.add( boostNumberBackgroundTexture );
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
    protected void prepareMarkersBackground( LiveGameData gameData, boolean isEditorMode, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, float innerRadius, float bigOuterRadius, float smallOuterRadius )
    {
        super.prepareMarkersBackground( gameData, isEditorMode, texCanvas, offsetX, offsetY, width, height, innerRadius, bigOuterRadius, smallOuterRadius );
        
        if ( !fillHighBackground.getBooleanValue() )
            return;
        
        final float centerX = offsetX + width / 2;
        final float centerY = offsetY + height / 2;
        final float innerAspect = getMarkersOnCircle() ? 1.0f : getInnerSize().getAspect();
        
        int nPoints = 360;
        int[] xPoints = new int[ nPoints ];
        int[] yPoints = new int[ nPoints ];
        
        final float TWO_PI = (float)( Math.PI * 2f );
        
        for ( int i = 0; i < nPoints; i++ )
        {
            float angle = i * ( TWO_PI / ( nPoints + 1 ) );
            xPoints[i] = Math.round( centerX + (float)Math.cos( angle ) * innerRadius );
            yPoints[i] = Math.round( centerY + -(float)Math.sin( angle ) * innerRadius / innerAspect );
        }
        
        Shape oldClip = texCanvas.getClip();
        
        Polygon p = new Polygon( xPoints, yPoints, nPoints );
        Area area = new Area( oldClip );
        area.subtract( new Area( p ) );
        
        texCanvas.setClip( area );
        
        int maxValue = (int)getMaxValue( gameData, isEditorMode );
        
        float lowAngle = ( needleRotationForMinValue.getFloatValue() + ( needleRotationForMaxValue.getFloatValue() - needleRotationForMinValue.getFloatValue() ) * ( lowRPM / maxValue ) );
        //float mediumAngle = ( needleRotationForMinValue.getFloatValue() + ( needleRotationForMaxValue.getFloatValue() - needleRotationForMinValue.getFloatValue() ) * ( mediumRPM / maxRPM ) );
        float maxAngle = needleRotationForMaxValue.getFloatValue();
        float oneDegree = 1; //(float)( Math.PI / 180.0 );
        
        for ( float angle = lowAngle; angle < maxAngle - oneDegree; angle += oneDegree )
        {
            angle = (float)Math.floor( angle );
            int rpm = Math.round( ( angle - needleRotationForMinValue.getFloatValue() ) * maxValue / ( needleRotationForMaxValue.getFloatValue() - needleRotationForMinValue.getFloatValue() ) );
            
            if ( rpm <= mediumRPM )
                texCanvas.setColor( interpolateMarkerColors.getBooleanValue() ? interpolateColor( markersColor.getColor(), revMarkersMediumColor.getColor(), ( rpm - lowRPM ) / ( mediumRPM - lowRPM ) ) : revMarkersMediumColor.getColor() );
            else
                texCanvas.setColor( interpolateMarkerColors.getBooleanValue() ? interpolateColor( revMarkersMediumColor.getColor(), revMarkersHighColor.getColor(), ( rpm - mediumRPM ) / ( maxValue - mediumRPM ) ) : revMarkersHighColor.getColor() );
            
            texCanvas.fillArc( Math.round( centerX - smallOuterRadius ), Math.round( centerY - smallOuterRadius / innerAspect ), Math.round( smallOuterRadius + smallOuterRadius ), Math.round( ( smallOuterRadius + smallOuterRadius ) / innerAspect ), Math.round( 90f - angle ), ( angle < maxAngle - oneDegree - oneDegree ) ? -2 : -1 );
        }
        
        texCanvas.setClip( oldClip );
    }
    
    @Override
    protected Color getMarkerColorForValue( LiveGameData gameData, boolean isEditorMode, int value, int minValue, int maxValue )
    {
        if ( fillHighBackground.getBooleanValue() || ( value <= lowRPM ) )
            return ( super.getMarkerColorForValue( gameData, isEditorMode, value, minValue, maxValue ) );
        
        if ( value <= mediumRPM )
            return ( interpolateMarkerColors.getBooleanValue() ? interpolateColor( markersColor.getColor(), revMarkersMediumColor.getColor(), ( value - lowRPM ) / ( mediumRPM - lowRPM ) ) : revMarkersMediumColor.getColor() );
        
        return ( interpolateMarkerColors.getBooleanValue() ? interpolateColor( revMarkersMediumColor.getColor(), revMarkersHighColor.getColor(), ( value - mediumRPM ) / ( maxValue - mediumRPM ) ) : revMarkersHighColor.getColor() );
    }
    
    @Override
    protected void drawMarkers( LiveGameData gameData, boolean isEditorMode, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        VehiclePhysics.Engine engine = gameData.getPhysics().getEngine();
        /*
        PhysicsSetting boostRange = engine.getBoostRange();
        
        int minBoost = ( engine.getRPMIncreasePerBoostLevel() > 0f ) ? (int)boostRange.getMinValue() : (int)boostRange.getMaxValue();
        //int maxBoost = ( engine.getRPMIncreasePerBoostLevel() > 0f ) ? (int)boostRange.getMaxValue() : (int)boostRange.getMinValue();
        int mediumBoost = Math.round( boostRange.getMinValue() + ( boostRange.getMaxValue() - boostRange.getMinValue() ) / 2f );
        
        float baseMaxRPM = gameData.getTelemetryData().getEngineBaseMaxRPM();
        lowRPM = engine.getMaxRPM( baseMaxRPM, minBoost );
        mediumRPM = engine.getMaxRPM( baseMaxRPM, mediumBoost );
        */
        lowRPM = engine.getBaseLifetimeRPM();
        mediumRPM = engine.getBaseLifetimeRPM() + engine.getHalfLifetimeRPMOffset();
        
        super.drawMarkers( gameData, isEditorMode, texCanvas, offsetX, offsetY, width, height );
    }
    
    /**
     * Draws the boost bar.
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
    protected void drawBoostBar( int boost, int maxBoost, boolean inverted, boolean tempBoost, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        if ( inverted )
            boost = maxBoost - boost + 1;
        
        TextureImage2D image = texCanvas.getImage();
        
        image.clear( Color.BLACK, offsetX, offsetY, width, height, true, null );
        
        texCanvas.setColor( Color.WHITE );
        texCanvas.drawRect( offsetX, offsetY, width - 1, height - 1 );
        
        int midBoost = ( ( maxBoost % 2 ) == 0 ) ? -maxBoost / 2 : maxBoost / 2 + 1;
        
        int x0 = 0;
        for ( int b = 1; b <= maxBoost; b++ )
        {
            int right = Math.min( width * b / maxBoost, width - 1 );
            
            if ( b <= boost )
            {
                Color color;
                
                if ( maxBoost == 1 )
                {
                    color = Color.RED;
                }
                else if ( maxBoost == 2 )
                {
                    if ( b == 1 )
                        color = Color.GREEN;
                    else
                        color = Color.RED;
                }
                else
                {
                    boolean isMidBoost = ( midBoost < 0 ) ? ( b == -midBoost || b == -midBoost + 1 ) : ( b == midBoost );
                    
                    if ( isMidBoost )
                    {
                        color = Color.YELLOW;
                    }
                    else if ( b < Math.abs( midBoost ) )
                    {
                        int r = Math.round( ( b - 1 ) * 255f / ( Math.abs( midBoost ) - 1 ) );
                        
                        color = new Color( r, 255, 0 );
                    }
                    else if ( midBoost < 0 )
                    {
                        int g = Math.round( ( -midBoost + 1 - b - midBoost - 1 ) * 255f / ( maxBoost + midBoost - 1 ) );
                        
                        color = new Color( 255, g, 0 );
                    }
                    else
                    {
                        int g = Math.round( ( midBoost - b + midBoost - 1 ) * 255f / ( maxBoost - midBoost ) );
                        
                        color = new Color( 255, g, 0 );
                    }
                }
                
                image.clear( color, offsetX + x0 + 1, offsetY + 1, right - x0 - 1, height - 2, true, null );
            }
            
            if ( b < maxBoost )
                texCanvas.drawLine( offsetX + right, offsetY + 1, offsetX + right, offsetY + height - 2 );
            
            x0 = right;
        }
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
        
        float rpm = getValue( gameData, isEditorMode );
        //float maxRPM = telemData.getEngineMaxRPM();
        float baseMaxRPM = gameData.getSetup().getEngine().getRevLimit();
        float maxRPM = gameData.getPhysics().getEngine().getMaxRPM( baseMaxRPM );
        float boostMaxRPM = gameData.getPhysics().getEngine().getMaxRPM( baseMaxRPM, boost.getValue() );
        
        if ( displayRPMString1.getBooleanValue() && ( needsCompleteRedraw || clock.c() ) )
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
                    float maxRPM2 = useBoostRevLimit1.getBooleanValue() ? boostMaxRPM : maxRPM;
                    
                    string += NumberUtil.formatFloat( maxRPM2, 0, false );
                }
            }
            
            rpmString1.draw( offsetX, offsetY, string, texture );
        }
        
        if ( displayRPMString2.getBooleanValue() && ( needsCompleteRedraw || clock.c() ) )
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
                    float maxRPM2 = useBoostRevLimit2.getBooleanValue() ? boostMaxRPM : maxRPM;
                    
                    string += NumberUtil.formatFloat( maxRPM2, 0, false );
                }
            }
            
            rpmString2.draw( offsetX, offsetY, string, texture );
        }
        
        if ( numShiftLights.getIntValue() > 0 )
        {
            float rpm2 = vsi.isPlayer() ? rpm : 0f;
            for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
                shiftLights[s].updateTextures( rpm2, boostMaxRPM, getBackground().getScaleX(), getBackground().getScaleY() );
        }
        
        if ( boostNumberBackgroundTexture != null )
            boostNumberBackgroundTexture.setTranslation( boostNumberBackgroundTexPosX, boostNumberBackgroundTexPosY );
    }
}
