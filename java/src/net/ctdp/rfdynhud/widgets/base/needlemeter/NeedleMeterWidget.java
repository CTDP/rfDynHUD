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
package net.ctdp.rfdynhud.widgets.base.needlemeter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FactoredFloatProperty;
import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetSet;

/**
 * The {@link NeedleMeterWidget} is an abstract {@link Widget} implementation
 * for meter widgets with a needle on an analogue scale.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class NeedleMeterWidget extends Widget
{
    public static final int NEEDLE_LOCAL_Z_INDEX = 1000;
    
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.IMAGE_INDICATOR + "standard/rev_meter_bg.png" );
    }
    
    @Override
    protected void onBackgroundChanged( boolean imageChanged, float deltaScaleX, float deltaScaleY )
    {
        super.onBackgroundChanged( imageChanged, deltaScaleX, deltaScaleY );
        
        if ( deltaScaleX > 0f )
        {
            markersInnerRadius.setIntValue( Math.round( markersInnerRadius.getIntValue() * deltaScaleX ) );
            markersLength.setIntValue( Math.round( markersLength.getIntValue() * ( deltaScaleX + deltaScaleY ) / 2 ) );
            valuePosX.setIntValue( Math.round( valuePosX.getIntValue() * deltaScaleX ) );
            valuePosY.setIntValue( Math.round( valuePosY.getIntValue() * deltaScaleY ) );
        }
    }
    
    protected static final float MIN_MAX_VALUE_NONE = 1000000000f;
    
    protected final FloatProperty minValue = new FloatProperty( "minValue", -MIN_MAX_VALUE_NONE );
    protected final FloatProperty maxValue = new FloatProperty( "maxValue", +MIN_MAX_VALUE_NONE );
    
    
    protected final BooleanProperty displayMarkers = new BooleanProperty( "displayMarkers", true );
    protected final BooleanProperty displayMarkerNumbers = new BooleanProperty( "displayMarkerNumbers", "displayNumbers", true );
    protected final BooleanProperty markerNumbersInside = new BooleanProperty( "markerNumbersInside", "numbersInside", false );
    protected final IntProperty markersInnerRadius = new IntProperty( "markersInnerRadius", "innerRadius", 224, 1, Integer.MAX_VALUE, false );
    protected final IntProperty markersLength = new IntProperty( "markersLength", "length", 50, 4, Integer.MAX_VALUE, false );
    protected final BooleanProperty markersOnCircle = new BooleanProperty( "markersOnCircle", true );
    protected final FactoredFloatProperty firstMarkerNumberOffset = new FactoredFloatProperty( "firstMarkerNumberOffset", "firstNumberOffset", FactoredFloatProperty.FACTOR_DEGREES_TO_RADIANS, 0f, -360.0f, +360.0f );
    protected final FactoredFloatProperty lastMarkerNumberOffset = new FactoredFloatProperty( "lastMarkerNumberOffset", "lastNumberOffset", FactoredFloatProperty.FACTOR_DEGREES_TO_RADIANS, 0f, -360.0f, +360.0f );
    
    protected int getMarkersBigStepLowerLimit()
    {
        return ( 300 );
    }
    
    protected final IntProperty markersBigStep = new IntProperty( "markersBigStep", "bigStep", 1000, getMarkersBigStepLowerLimit(), Integer.MAX_VALUE, false )
    {
        @Override
        protected void onValueChanged( Integer oldValue, int newValue )
        {
            fixSmallStep();
        }
    };
    
    protected int getMarkersSmallStepLowerLimit()
    {
        return ( 20 );
    }
    
    protected final IntProperty markersSmallStep = new IntProperty( "markersSmallStep", "smallStep", 200, getMarkersSmallStepLowerLimit(), Integer.MAX_VALUE, false )
    {
        @Override
        protected void onValueChanged( Integer oldValue, int newValue )
        {
            fixSmallStep();
        }
    };
    
    protected final BooleanProperty lastMarkerBig = new BooleanProperty( "lastMarkerBig", false );
    
    protected final ColorProperty markersColor = new ColorProperty( "markersColor", "color", "#FFFFFF" );
    protected final FontProperty markersFont = new FontProperty( "markersFont", "font", "Monospaced" + FontUtils.SEPARATOR + "BOLD" + FontUtils.SEPARATOR + "9va" );
    protected final ColorProperty markersFontColor = new ColorProperty( "markersFontColor", "fontColor", "#FFFFFF" );
    protected final ColorProperty markersFontDropShadowColor = new ColorProperty( "markersFontDropShadowColor", "fontDropShadowColor", "#00000000" );
    protected final BooleanProperty markerNumbersCentered = new BooleanProperty( "markerNumbersCentered", "numbersCentered", false );
    
    
    protected void onNeedleImageNameChanged() {}
    
    private TransformableTexture needleTexture = null;
    
    protected String getInitialNeedleImage()
    {
        return ( "standard/rev_meter_needle.png" );
    }
    
    private final ImageProperty needleImageName = new ImageProperty( "needleImageName", "imageName", getInitialNeedleImage(), false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            onNeedleImageNameChanged();
        }
    };
    
    protected final IntProperty needleMountX = new IntProperty( "needleMountX", -1, -1, 5000 );
    protected final IntProperty needleMountY = new IntProperty( "needleMountY", -1, -1, 5000 );
    
    protected final IntProperty needlePivotBottomOffset = new IntProperty( "needlePivotBottomOffset", "pivotBottomOffset", 60 );
    
    protected final FactoredFloatProperty needleRotationForMinValue = new FactoredFloatProperty( "needleRotationForMinValue", "rotForMin", FactoredFloatProperty.FACTOR_DEGREES_TO_RADIANS, -122.4f, -360.0f, +360.0f );
    protected final FactoredFloatProperty needleRotationForMaxValue = new FactoredFloatProperty( "needleRotationForMaxValue", "rotForMax", FactoredFloatProperty.FACTOR_DEGREES_TO_RADIANS, +118.8f, -360.0f, +360.0f );
    
    private Boolean drawNeeldeMount = null;
    
    protected final BooleanProperty displayValue = new BooleanProperty( "displayValue", true );
    
    protected final ImageProperty valueBackgroundImageName = new ImageProperty( "valueBackgroundImageName", "backgroundImage", "standard/cyan_circle.png", false, true );
    private TransformableTexture valueBackgroundTexture = null;
    private TextureImage2D valueBackgroundTexture_bak = null;
    
    protected final IntProperty valuePosX = new IntProperty( "valuePosX", "posX", 100 );
    protected final IntProperty valuePosY = new IntProperty( "valuePosY", "posY", 100 );
    private int valueBackgroundTexPosX, valueBackgroundTexPosY;
    
    protected final FontProperty valueFont = new FontProperty( "valueFont", "font", FontProperty.STANDARD_FONT.getKey() );
    protected final ColorProperty valueFontColor = new ColorProperty( "valueFontColor", "fontColor", "#1A261C" );
    
    private DrawnString valueString = null;
    
    private final IntValue valueValue = new IntValue();
    
    @Override
    public void onPropertyChanged( Property property, Object oldValue, Object newValue )
    {
        super.onPropertyChanged( property, oldValue, newValue );
        
        if ( ( oldValue != null ) && ( ( property == needleMountX ) || ( property == needleMountY ) ) )
        {
            drawNeeldeMount = true;
        }
    }
    
    private void fixSmallStep()
    {
        this.markersSmallStep.setIntValue( markersBigStep.getIntValue() / Math.round( (float)markersBigStep.getIntValue() / (float)markersSmallStep.getIntValue() ) );
    }
    
    public void setDisplayValue( boolean display )
    {
        this.displayValue.setBooleanValue( display );
    }
    
    public boolean getDisplayValue()
    {
        return ( displayValue.getBooleanValue() );
    }
    
    protected boolean getDisplayMarkers()
    {
        return ( displayMarkers.getBooleanValue() );
    }
    
    protected boolean getDisplayMarkerNumbers()
    {
        return ( displayMarkerNumbers.getBooleanValue() );
    }
    
    protected boolean getMarkerNumbersInside()
    {
        return ( markerNumbersInside.getBooleanValue() );
    }
    
    protected int getMarkersInnerRadius()
    {
        return ( markersInnerRadius.getIntValue() );
    }
    
    protected int getMarkersLength()
    {
        return ( markersLength.getIntValue() );
    }
    
    protected boolean getMarkersOnCircle()
    {
        return ( markersOnCircle.getBooleanValue() );
    }
    
    protected ImageTemplate getNeedleImage()
    {
        return ( needleImageName.getImage() );
    }
    
    protected final TransformableTexture getNeedleTexture()
    {
        return ( needleTexture );
    }
    
    protected int getNeedlePivotBottomOffset()
    {
        return ( needlePivotBottomOffset.getIntValue() );
    }
    
    protected int getNeedleMountX( int widgetWidth )
    {
        if ( needleMountX.getIntValue() < 0 )
            return ( widgetWidth / 2 );
        
        return ( Math.round( needleMountX.getIntValue() * getBackground().getScaleX() ) );
    }
    
    protected int getNeedleMountY( int widgetHeight )
    {
        if ( needleMountY.getIntValue() < 0 )
            return ( widgetHeight / 2 );
        
        return ( Math.round( needleMountY.getIntValue() * getBackground().getScaleY() ) );
    }
    
    protected float getNeedleRotationForMinValue()
    {
        return ( needleRotationForMinValue.getFactoredValue() );
    }
    
    protected float getNeedleRotationForMaxValue()
    {
        return ( needleRotationForMaxValue.getFactoredValue() );
    }
    
    //protected 
    
    protected ImageTemplate getValueBackgroundImage()
    {
        return ( valueBackgroundImageName.getImage() );
    }
    
    /*
    protected TextureImage2D getValueBackgroundTexture()
    {
        return ( valueBackgroundTexture_bak );
    }
    */
    
    protected int getValuePosX()
    {
        return ( valuePosX.getIntValue() );
    }
    
    protected int getValuePosY()
    {
        return ( valuePosY.getIntValue() );
    }
    
    /**
     * Gets the {@link FontProperty} for the value.
     * 
     * @return the {@link FontProperty} for the value.
     */
    protected FontProperty getValueFont()
    {
        return ( valueFont );
    }
    
    /**
     * Gets the {@link ColorProperty} for the value.
     * 
     * @return the {@link ColorProperty} for the value.
     */
    protected ColorProperty getValueFontColor()
    {
        return ( valueFontColor );
    }
    
    private boolean loadValueBackgroundTexture( boolean isEditorMode )
    {
        if ( !displayValue.getBooleanValue() )
        {
            valueBackgroundTexture = null;
            valueBackgroundTexture_bak = null;
            return ( false );
        }
        
        try
        {
            ImageTemplate it = valueBackgroundImageName.getImage();
            
            if ( it == null )
            {
                valueBackgroundTexture = null;
                valueBackgroundTexture_bak = null;
                return ( false );
            }
            
            float scale = getBackground().getScaleX();
            int w = Math.round( it.getBaseWidth() * scale );
            int h = Math.round( it.getBaseHeight() * scale );
            if ( ( valueBackgroundTexture == null ) || ( valueBackgroundTexture.getWidth() != w ) || ( valueBackgroundTexture.getHeight() != h ) )
            {
                valueBackgroundTexture_bak = it.getScaledTextureImage( w, h, valueBackgroundTexture_bak, isEditorMode );
                
                valueBackgroundTexture = TransformableTexture.getOrCreate( w, h, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, valueBackgroundTexture, isEditorMode );
                valueBackgroundTexture.setDynamic( true );
                valueBackgroundTexture.getTexture().clear( valueBackgroundTexture_bak, true, null );
                
                forceAndSetDirty( false );
            }
        }
        catch ( Throwable t )
        {
            log( t );
            
            return ( false );
        }
        
        return ( true );
    }
    
    private boolean loadNeedleTexture( boolean isEditorMode )
    {
        if ( needleImageName.isNoImage() )
        {
            needleTexture = null;
            return ( false );
        }
        
        try
        {
            ImageTemplate it = needleImageName.getImage();
            
            if ( it == null )
            {
                needleTexture = null;
                return ( false );
            }
            
            float scale = getBackground().getScaleX();
            int w = Math.round( it.getBaseWidth() * scale );
            int h = Math.round( it.getBaseHeight() * scale );
            needleTexture = it.getScaledTransformableTexture( w, h, needleTexture, isEditorMode );
            
            needleTexture.setLocalZIndex( NEEDLE_LOCAL_Z_INDEX );
        }
        catch ( Throwable t )
        {
            log( t );
            
            return ( false );
        }
        
        return ( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        if ( loadValueBackgroundTexture( isEditorMode ) )
            collector.add( valueBackgroundTexture );
        if ( loadNeedleTexture( isEditorMode ) )
            collector.add( needleTexture );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        valueValue.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleSetupUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        super.onVehicleSetupUpdated( gameData, isEditorMode );
        
        forceCompleteRedraw( true );
        forceReinitialization();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onNeededDataComplete( LiveGameData gameData, boolean isEditorMode )
    {
        super.onNeededDataComplete( gameData, isEditorMode );
        
        valueValue.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        final float backgroundScaleX = getBackground().getScaleX();
        final float backgroundScaleY = getBackground().getScaleY();
        
        if ( needleTexture != null )
        {
            int mountX = getNeedleMountX( width );
            int mountY = getNeedleMountY( height );
            
            needleTexture.setTranslation( mountX - needleTexture.getWidth() / 2, mountY - needleTexture.getHeight() + needlePivotBottomOffset.getIntValue() * backgroundScaleX );
            needleTexture.setRotationCenter( needleTexture.getWidth() / 2, (int)( needleTexture.getHeight() - needlePivotBottomOffset.getIntValue() * backgroundScaleX ) );
        }
        
        if ( displayValue.getBooleanValue() )
        {
            FontProperty valueFont = getValueFont();
            ColorProperty valueFontColor = getValueFontColor();
            
            FontMetrics metrics = valueFont.getMetrics();
            //Rectangle2D bounds = metrics.getStringBounds( "000", texture.getTextureCanvas() );
            //double fw = bounds.getWidth();
            double fh = metrics.getAscent() - metrics.getDescent();
            int fx, fy;
            
            if ( valueBackgroundTexture == null )
            {
                fx = Math.round( valuePosX.getIntValue() * backgroundScaleX );
                fy = Math.round( valuePosY.getIntValue() * backgroundScaleY );
            }
            else
            {
                valueBackgroundTexPosX = Math.round( valuePosX.getIntValue() * backgroundScaleX - valueBackgroundTexture.getWidth() / 2.0f );
                valueBackgroundTexPosY = Math.round( valuePosY.getIntValue() * backgroundScaleY - valueBackgroundTexture.getHeight() / 2.0f );
                
                fx = valueBackgroundTexture.getWidth() / 2;
                fy = valueBackgroundTexture.getHeight() / 2;
            }
            
            valueString = dsf.newDrawnString( "valueString", fx/* - (int)( fw / 2.0 )*/, fy - (int)( metrics.getDescent() + fh / 2.0 ), Alignment.LEFT, false, valueFont.getFont(), valueFont.isAntiAliased(), valueFontColor.getColor() );
        }
        else
        {
            valueBackgroundTexture = null;
            valueBackgroundTexture_bak = null;
        }
    }
    
    /**
     * Gets the value for the needle and the digital value display.
     * Override {@link #getValueForValueDisplay(LiveGameData, boolean)} to use a different value
     * for the digital value.
     * 
     * @param gameData
     * @param isEditorMode
     * 
     * @return the value for the needle and the digital value display.
     */
    protected abstract float getValue( LiveGameData gameData, boolean isEditorMode );
    
    /**
     * Gets the value for the digital value display.
     * The default implementation simply gets the result of {@link #getValue(LiveGameData, boolean)} and converts it to an int.
     * 
     * @param gameData
     * @param isEditorMode
     * 
     * @return the value for the digital value display.
     */
    protected int getValueForValueDisplay( LiveGameData gameData, boolean isEditorMode)
    {
        return ( Math.round( getValue( gameData, isEditorMode ) ) );
    }
    
    /**
     * Gets the minimum value for the markers and needle coming from game data or known limits.
     * 
     * @param gameData
     * @param isEditorMode
     * 
     * @return the minimum value for the markers and needle.
     */
    protected abstract float getMinDataValue( LiveGameData gameData, boolean isEditorMode );
    
    /**
     * Gets the maximum value for the markers and needle coming from game data or known limits.
     * 
     * @param gameData
     * @param isEditorMode
     * 
     * @return the maximum value for the markers and needle.
     */
    protected abstract float getMaxDataValue( LiveGameData gameData, boolean isEditorMode );
    
    /**
     * Gets the minimum value for the markers and needle.
     * If the minValue property is set to a valid value, the value is returned, otherwise the result of {@link #getMinDataValue(LiveGameData, boolean)} is returned.
     * 
     * @param gameData
     * @param isEditorMode
     * 
     * @return the minimum value for the markers and needle.
     */
    protected final float getMinValue( LiveGameData gameData, boolean isEditorMode )
    {
        float minDataValue = getMinDataValue( gameData, isEditorMode );
        
        if ( minValue.getFloatValue() > minDataValue )
            return ( minValue.getFloatValue() );
        
        return ( minDataValue );
    }
    
    /**
     * Gets the maximum value for the markers and needle.
     * If the maxValue property is set to a valid value, the value is returned, otherwise the result of {@link #getMaxDataValue(LiveGameData, boolean)} is returned.
     * 
     * @param gameData
     * @param isEditorMode
     * 
     * @return the maximum value for the markers and needle.
     */
    protected final float getMaxValue( LiveGameData gameData, boolean isEditorMode )
    {
        float maxDataValue = getMaxDataValue( gameData, isEditorMode );
        
        if ( maxValue.getFloatValue() < maxDataValue )
            return ( maxValue.getFloatValue() );
        
        return ( maxDataValue );
    }
    
    /**
     * Gets, whether the needle may go below the {@link #getMinValue(LiveGameData, boolean)} result.
     * The default implementation returns <code>false</code>.
     * 
     * @return whether the needle may go below the {@link #getMinValue(LiveGameData, boolean)} result.
     */
    protected boolean getNeedleMayExceedMinimum()
    {
        return ( false );
    }
    
    /**
     * Gets, whether the needle may go beyond the {@link #getMaxValue(LiveGameData, boolean)} result.
     * The default implementation returns <code>true</code>.
     * 
     * @return whether the needle may go beyond the {@link #getMaxValue(LiveGameData, boolean)} result.
     */
    protected boolean getNeedleMayExceedMaximum()
    {
        return ( true );
    }
    
    /**
     * Gets the text label for the big markers at the given value.
     * 
     * @param gameData
     * @param isEditorMode
     * @param value
     * 
     * @return the text label for the big markers at the given value.
     */
    protected abstract String getMarkerLabelForValue( LiveGameData gameData, boolean isEditorMode, float value );
    
    /**
     * 
     * @param gameData
     * @param isEditorMode
     * @param texCanvas
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     * @param innerRadius
     * @param bigOuterRadius
     * @param smallOuterRadius
     */
    protected void prepareMarkersBackground( LiveGameData gameData, boolean isEditorMode, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, float innerRadius, float bigOuterRadius, float smallOuterRadius )
    {
    }
    
    /**
     * Gets a certain marker's color at the given value.
     * 
     * @param gameData
     * @param isEditorMode
     * @param value
     * @param minValue
     * @param maxValue
     * 
     * @return a certain marker's color at the given value.
     */
    protected Color getMarkerColorForValue( LiveGameData gameData, boolean isEditorMode, int value, int minValue, int maxValue )
    {
        return ( markersColor.getColor() );
    }
    
    /**
     * Gets a certain marker number's color at the given value.
     * 
     * @param gameData
     * @param isEditorMode
     * @param value
     * @param minValue
     * @param maxValue
     * 
     * @return a certain marker's color at the given value.
     */
    protected Color getMarkerNumberColorForValue( LiveGameData gameData, boolean isEditorMode, int value, int minValue, int maxValue )
    {
        return ( markersFontColor.getColor() );
    }
    
    /**
     * Draws the markers.
     * 
     * @param gameData
     * @param isEditorMode
     * @param texCanvas
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     */
    protected void drawMarkers( LiveGameData gameData, boolean isEditorMode, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        final boolean dm = getDisplayMarkers();
        final boolean dmn = getDisplayMarkerNumbers();
        final boolean mni = getMarkerNumbersInside();
        
        final float backgroundScaleX = getBackground().getScaleX();
        //final float backgroundScaleY = getBackground().getBackgroundScaleY();
        
        int minValue = (int)getMinValue( gameData, isEditorMode );
        int maxValue = (int)getMaxValue( gameData, isEditorMode );
        float range = ( maxValue - minValue );
        
        final float centerX = offsetX + getNeedleMountX( width );
        final float centerY = offsetY + getNeedleMountY( height );
        final float innerAspect = markersOnCircle.getBooleanValue() ? 1.0f : getInnerSize().getAspect();
        
        texCanvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        
        float innerRadius = markersInnerRadius.getIntValue() * backgroundScaleX;
        //float markersLength2 = markersLength.getIntValue() * backgroundScaleX;
        float bigOuterRadius0 = ( markersInnerRadius.getIntValue() + markersLength.getIntValue() - 1 ) * backgroundScaleX;
        float bigOuterRadius = ( markersInnerRadius.getIntValue() + ( dm ? markersLength.getIntValue() - 1 : 0 ) ) * backgroundScaleX;
        float smallOuterRadius0 = innerRadius + ( bigOuterRadius0 - innerRadius ) * 0.75f;
        float smallOuterRadius = innerRadius + ( bigOuterRadius - innerRadius ) * 0.75f;
        
        prepareMarkersBackground( gameData, isEditorMode, texCanvas, offsetX, offsetY, width, height, innerRadius, bigOuterRadius, smallOuterRadius0 );
        
        Stroke oldStroke = texCanvas.getStroke();
        
        Stroke bigStroke = new BasicStroke( 2 );
        Stroke smallStroke = new BasicStroke( 1 );
        
        FontProperty numberFont = markersFont;
        texCanvas.setFont( numberFont.getFont() );
        FontMetrics metrics = numberFont.getMetrics();
        
        AffineTransform at0 = new AffineTransform( texCanvas.getTransform() );
        AffineTransform at1 = new AffineTransform();
        AffineTransform at2 = new AffineTransform();
        AffineTransform atCenterTranslate = AffineTransform.getTranslateInstance( centerX, centerY );
        AffineTransform atEllipticScale = AffineTransform.getScaleInstance( 1.0, 1.0 / innerAspect );
        Point2D.Double p0 = new Point2D.Double();
        
        /*
        String biggestString = String.valueOf( getMarkerLabelForValue( gameData, isEditorMode, Math.max( minValue, maxValue ) ) );
        Rectangle2D biggestBounds = metrics.getStringBounds( biggestString, texCanvas );
        double maxFW = biggestBounds.getWidth();
        double maxFH = biggestBounds.getHeight();
        //double maxFH = metrics.getAscent() - metrics.getDescent();
        double maxOff = Math.sqrt( maxFW * maxFW + maxFH * maxFH ) / 2.0;
        */
        
        Color dropShadowColor = markersFontDropShadowColor.getColor();
        float dropShadowOffset = 2.2f; //numberFont.getFont().getSize() * 0.2f;
        boolean drawDropShadow = ( dropShadowColor.getAlpha() > 0 );
        
        double equalOff = 0.0;
        if ( markerNumbersCentered.getBooleanValue() )
        {
            String biggestString = String.valueOf( getMarkerLabelForValue( gameData, isEditorMode, Math.max( minValue, maxValue ) ) );
            Rectangle2D bounds = metrics.getStringBounds( biggestString, texCanvas );
            double fw = bounds.getWidth();
            //double fh = metrics.getAscent() - metrics.getDescent();
            double fh = bounds.getHeight();
            equalOff = Math.sqrt( fw * fw + fh * fh ) / 2.0;
        }
        
        final int smallStep = markersSmallStep.getIntValue();
        for ( int value = minValue; value <= maxValue; value += smallStep )
        {
            float angle = +( needleRotationForMinValue.getFactoredValue() + ( needleRotationForMaxValue.getFactoredValue() - needleRotationForMinValue.getFactoredValue() ) * ( ( value - minValue ) / range ) );
            
            if ( value == minValue )
                angle += firstMarkerNumberOffset.getFactoredValue();
            else if ( value + markersBigStep.getIntValue() > maxValue )
                angle += lastMarkerNumberOffset.getFactoredValue();
            
            at1.setTransform( atCenterTranslate );
            at2.setTransform( atEllipticScale );
            at1.concatenate( at2 );
            at2.setToRotation( angle );
            at1.concatenate( at2 );
            at2.setToTranslation( 0, -innerRadius );
            at1.concatenate( at2 );
            
            p0.setLocation( 0, 0 );
            at1.transform( p0, p0 );
            
            double vecX = p0.x - centerX;
            double vecY = p0.y - centerY;
            double len = Math.sqrt( vecX * vecX + vecY * vecY );
            vecX /= len;
            vecY /= len;
            
            texCanvas.setColor( getMarkerColorForValue( gameData, isEditorMode, value, minValue, maxValue ) );
            
            if ( ( ( value % markersBigStep.getIntValue() ) == 0 ) || ( lastMarkerBig.getBooleanValue() && ( value + smallStep > maxValue ) ) )
            {
                if ( dm )
                {
                    texCanvas.setStroke( bigStroke );
                    float l = bigOuterRadius - innerRadius;
                    texCanvas.drawLine( (int)Math.round( p0.x ), (int)Math.round( p0.y ), (int)Math.round( p0.x + vecX * l ), (int)Math.round( p0.y + vecY * l ) );
                }
                
                if ( dmn )
                {
                    String s = getMarkerLabelForValue( gameData, isEditorMode, value );
                    
                    if ( s != null )
                    {
                        Rectangle2D bounds = metrics.getStringBounds( s, texCanvas );
                        double fw = bounds.getWidth();
                        //double fh = metrics.getAscent() - metrics.getDescent();
                        double fh = bounds.getHeight();
                        double off = markerNumbersCentered.getBooleanValue() ? equalOff : Math.sqrt( fw * fw + fh * fh ) / 2.0;
                        
                        int x, y;
                        if ( mni )
                        {
                            x = (int)Math.round( p0.x + vecX * ( -off - 1 ) );
                            y = (int)Math.round( p0.y + vecY * ( -off - 1 ) );
                        }
                        else
                        {
                            x = (int)Math.round( p0.x + vecX * ( bigOuterRadius0 - innerRadius + off + 1 ) );
                            y = (int)Math.round( p0.y + vecY * ( bigOuterRadius0 - innerRadius + off + 1 ) );
                        }
                        
                        if ( drawDropShadow )
                        {
                            texCanvas.setColor( dropShadowColor );
                            texCanvas.drawString( s, x - (int)( fw / 2 ) + dropShadowOffset, y + (int)( -fh / 2 - bounds.getY() ) + dropShadowOffset );
                        }
                        
                        texCanvas.setColor( getMarkerNumberColorForValue( gameData, isEditorMode, value, minValue, maxValue ) );
                        texCanvas.drawString( s, x - (int)( fw / 2 ), y + (int)( -fh / 2 - bounds.getY() ) );
                        /*
                        texCanvas.setColor( Color.GREEN );
                        texCanvas.setStroke( smallStroke );
                        texCanvas.drawLine( x - 2, y - 2, x + 2, y + 2 );
                        texCanvas.drawLine( x - 2, y + 2, x + 2, y - 2 );
                        */
                    }
                }
            }
            else if ( dm )
            {
                texCanvas.setStroke( smallStroke );
                float l = smallOuterRadius - innerRadius;
                texCanvas.drawLine( (int)Math.round( p0.x ), (int)Math.round( p0.y ), (int)Math.round( p0.x + vecX * l ), (int)Math.round( p0.y + vecY * l ) );
            }
        }
        
        texCanvas.setTransform( at0 );
        texCanvas.setStroke( oldStroke );
        
        texCanvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT );
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        if ( getDisplayMarkers() || getDisplayMarkerNumbers() )
        {
            drawMarkers( gameData, isEditorMode, texture.getTextureCanvas(), offsetX, offsetY, width, height );
        }
    }
    
    /**
     * Live-checks, whether the needle is to be rendered or not.
     * 
     * @param gameData
     * @param isEditorMode
     * 
     * @return whether to render the needle or not.
     */
    protected boolean doRenderNeedle( LiveGameData gameData, boolean isEditorMode )
    {
        return ( true );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needleTexture != null )
        {
            if ( doRenderNeedle( gameData, isEditorMode ) )
            {
                float value = getValue( gameData, isEditorMode );
                float minValue = getMinValue( gameData, isEditorMode );
                float maxValue = getMaxValue( gameData, isEditorMode );
                if ( !getNeedleMayExceedMinimum() )
                    value = Math.max( minValue, value );
                if ( !getNeedleMayExceedMaximum() )
                    value = Math.min( value, maxValue );
                
                float rot0 = needleRotationForMinValue.getFactoredValue();
                float rot = -( ( value - minValue ) / ( maxValue - minValue ) ) * ( needleRotationForMinValue.getFactoredValue() - needleRotationForMaxValue.getFactoredValue() );
                
                needleTexture.setRotation( rot0 + rot );
                needleTexture.setVisible( true );
            }
            else
            {
                needleTexture.setVisible( false );
            }
        }
        
        if ( displayValue.getBooleanValue() )
        {
            valueValue.update( getValueForValueDisplay( gameData, isEditorMode ) );
            if ( needsCompleteRedraw || ( clock.c() && valueValue.hasChanged() ) )
            {
                String string = valueValue.getValueAsString();
                
                FontMetrics metrics = getValueFont().getMetrics();
                Rectangle2D bounds = metrics.getStringBounds( string, texture.getTextureCanvas() );
                double fw = bounds.getWidth();
                
                if ( valueBackgroundTexture == null )
                {
                    valueString.draw( offsetX - (int)( fw / 2.0 ), offsetY, string, texture );
                }
                else
                {
                    if ( needsCompleteRedraw )
                        valueBackgroundTexture.getTexture().clear( valueBackgroundTexture_bak, true, null );
                    
                    valueString.draw( (int)( -fw / 2.0 ), 0, string, valueBackgroundTexture.getTexture(), valueBackgroundTexture_bak, 0, 0 );
                }
            }
            
            if ( valueBackgroundTexture != null )
                valueBackgroundTexture.setTranslation( valueBackgroundTexPosX, valueBackgroundTexPosY );
        }
        
        if ( isEditorMode && ( drawNeeldeMount != null ) )
        {
            final int centerX = offsetX + getNeedleMountX( width );
            final int centerY = offsetY + getNeedleMountY( height );
            
            Texture2DCanvas texCanvas = texture.getTextureCanvas();
            
            Color oldColor = texCanvas.getColor();
            texCanvas.setColor( Color.MAGENTA );
            Stroke oldStroke = texCanvas.getStroke();
            texCanvas.setStroke( new BasicStroke( 3 ) );
            texCanvas.drawLine( centerX - 8, centerY - 8, centerX + 8, centerY + 8 );
            texCanvas.drawLine( centerX - 8, centerY + 8, centerX + 8, centerY - 8 );
            texCanvas.setStroke( oldStroke );
            texCanvas.setColor( oldColor );
            
            drawNeeldeMount = null;
        }
    }
    
    protected void saveMarkersProperties( PropertyWriter writer ) throws IOException
    {
        writer.writeProperty( displayMarkers, "Display markers?" );
        writer.writeProperty( displayMarkerNumbers, "Display marker numbers?" );
        writer.writeProperty( markerNumbersInside, "Render marker numbers inside of the markers?" );
        writer.writeProperty( markersInnerRadius, "The inner radius of the markers (in background image space)" );
        writer.writeProperty( markersLength, "The length of the markers (in background image space)" );
        writer.writeProperty( markersOnCircle, "Draw markers on circle, even if the Widget has an aspect ratio unequal to 1.0" );
        writer.writeProperty( firstMarkerNumberOffset, "The rotational offset in clockwise degrees for the first marker number." );
        writer.writeProperty( lastMarkerNumberOffset, "The rotational offset in clockwise degrees for the last marker number." );
        writer.writeProperty( markersBigStep, "Step size of bigger rev markers" );
        writer.writeProperty( markersSmallStep, "Step size of smaller rev markers" );
        writer.writeProperty( lastMarkerBig, "Whether to force the last marker to be treated as a big one." );
        writer.writeProperty( markersColor, "The color used to draw the markers." );
        writer.writeProperty( markersFont, "The font used to draw the marker numbers." );
        writer.writeProperty( markersFontColor, "The font color used to draw the marker numbers." );
        writer.writeProperty( markersFontDropShadowColor, "The font color for the marker numbers drop shadow." );
        writer.writeProperty( markerNumbersCentered, "Draw marker numbers at their centers at an equal distance around needle mount?" );
    }
    
    protected void saveNeedleProperties( PropertyWriter writer ) throws IOException
    {
        writer.writeProperty( needleImageName, "The name of the needle image." );
        writer.writeProperty( needleMountX, "The x-offset in background image pixels to the needle mount (-1 for center)." );
        writer.writeProperty( needleMountY, "The y-offset in background image pixels to the needle mount (-1 for center)." );
        writer.writeProperty( needlePivotBottomOffset, "The offset in (unscaled) pixels from the bottom of the image, where the center of the needle's axis is." );
        writer.writeProperty( needleRotationForMinValue, "The rotation for the needle image, that it has for min value (in degrees)." );
        writer.writeProperty( needleRotationForMaxValue, "The rotation for the needle image, that it has for max value (in degrees)." );
    }
    
    protected void saveDigiValueProperties( PropertyWriter writer ) throws IOException
    {
        writer.writeProperty( displayValue, "Display the digital value?" );
        writer.writeProperty( valueBackgroundImageName, "The name of the image to render behind the value number." );
        writer.writeProperty( valuePosX, "The x-offset in pixels to the value label." );
        writer.writeProperty( valuePosY, "The y-offset in pixels to the value label." );
        writer.writeProperty( valueFont, "The font used to draw the value." );
        writer.writeProperty( valueFontColor, "The font color used to draw the value." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( minValue, "The minimum value accepted for the markers and needle" );
        writer.writeProperty( maxValue, "The maximum value accepted for the markers and needle" );
        
        saveMarkersProperties( writer );
        
        saveNeedleProperties( writer );
        
        saveDigiValueProperties( writer );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( minValue ) );
        else if ( loader.loadProperty( maxValue ) );
        
        else if ( loader.loadProperty( displayMarkers ) );
        else if ( loader.loadProperty( displayMarkerNumbers ) );
        else if ( loader.loadProperty( markerNumbersInside ) );
        else if ( loader.loadProperty( markersInnerRadius ) );
        else if ( loader.loadProperty( markersLength ) );
        else if ( loader.loadProperty( markersOnCircle ) );
        else if ( loader.loadProperty( firstMarkerNumberOffset ) );
        else if ( loader.loadProperty( lastMarkerNumberOffset ) );
        else if ( loader.loadProperty( markersBigStep ) );
        else if ( loader.loadProperty( markersSmallStep ) );
        else if ( loader.loadProperty( lastMarkerBig ) );
        else if ( loader.loadProperty( markersColor ) );
        else if ( loader.loadProperty( markersFont ) );
        else if ( loader.loadProperty( markersFontColor ) );
        else if ( loader.loadProperty( markersFontDropShadowColor ) );
        else if ( loader.loadProperty( markerNumbersCentered ) );
        
        else if ( loader.loadProperty( needleImageName ) );
        else if ( loader.loadProperty( needleMountX ) );
        else if ( loader.loadProperty( needleMountY ) );
        else if ( loader.loadProperty( needlePivotBottomOffset ) );
        else if ( loader.loadProperty( needleRotationForMinValue ) );
        else if ( loader.loadProperty( needleRotationForMaxValue ) );
        
        else if ( loader.loadProperty( displayValue ) );
        else if ( loader.loadProperty( valueBackgroundImageName ) );
        else if ( loader.loadProperty( valuePosX ) );
        else if ( loader.loadProperty( valuePosY ) );
        else if ( loader.loadProperty( valueFont ) );
        else if ( loader.loadProperty( valueFontColor ) );
    }
    
    /**
     * Adds the minValue property to the container.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void addMinValuePropertyToContainer( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( minValue );
    }
    
    /**
     * Adds the maxValue property to the container.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void addMaxValuePropertyToContainer( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( maxValue );
    }
    
    /**
     * Collects the widget type specific properties before needle, markers and digi value.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     * 
     * @return <code>true</code>, if the implementation has added a group, <code>false</code> otherwise.
     */
    protected boolean getSpecificPropertiesFirst( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( "Misc" );
        
        addMinValuePropertyToContainer( propsCont, forceAll );
        addMaxValuePropertyToContainer( propsCont, forceAll );
        
        return ( true );
    }
    
    /**
     * Collects the properties for the markers.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void getMarkersProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( "Markers" );
        
        propsCont.addProperty( displayMarkers );
        propsCont.addProperty( displayMarkerNumbers );
        propsCont.addProperty( markerNumbersInside );
        propsCont.addProperty( markersInnerRadius );
        propsCont.addProperty( markersLength );
        propsCont.addProperty( markersOnCircle );
        propsCont.addProperty( firstMarkerNumberOffset );
        propsCont.addProperty( lastMarkerNumberOffset );
        propsCont.addProperty( markersBigStep );
        propsCont.addProperty( markersSmallStep );
        propsCont.addProperty( lastMarkerBig );
        propsCont.addProperty( markersColor );
        propsCont.addProperty( markersFont );
        propsCont.addProperty( markersFontColor );
        propsCont.addProperty( markersFontDropShadowColor );
        propsCont.addProperty( markerNumbersCentered );
    }
    
    /**
     * Collects the properties for the needle.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void getNeedleProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( "Needle" );
        
        propsCont.addProperty( needleImageName );
        propsCont.addProperty( needleMountX );
        propsCont.addProperty( needleMountY );
        propsCont.addProperty( needlePivotBottomOffset );
        propsCont.addProperty( needleRotationForMinValue );
        propsCont.addProperty( needleRotationForMaxValue );
    }
    
    /**
     * Gets the display name of the properties group for the digital value in the editor.
     * 
     * @return the display name of the properties group for the digital value in the editor.
     */
    protected String getDigiValuePropertiesGroupName()
    {
        return ( "Digital Value" );
    }
    
    /**
     * Collects the properties for the digital value.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void getDigiValueProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( getDigiValuePropertiesGroupName() );
        
        propsCont.addProperty( displayValue );
        propsCont.addProperty( valueBackgroundImageName );
        propsCont.addProperty( valuePosX );
        propsCont.addProperty( valuePosY );
        propsCont.addProperty( valueFont );
        propsCont.addProperty( valueFontColor );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        getSpecificPropertiesFirst( propsCont, forceAll );
        
        getMarkersProperties( propsCont, forceAll );
        
        getNeedleProperties( propsCont, forceAll );
        
        getDigiValueProperties( propsCont, forceAll );
    }
    
    /**
     * This method is called as the last item in the constructor.
     */
    protected void initParentProperties()
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        markersFont.setFont( "Dialog", Font.PLAIN, 5, false, true );
        valueFont.setFont( "Dialog", Font.PLAIN, 5, false, true );
    }
    
    /**
     * Creates a new {@link NeedleMeterWidget}.
     * 
     * @param widgetSet the {@link WidgetSet} this {@link Widget} belongs to
     * @param widgetPackage the package in the editor
     * @param width negative numbers for (screen_width - width)
     * @param widthPercent width parameter treated as percents
     * @param height negative numbers for (screen_height - height)
     * @param heightPercent height parameter treated as percents
     */
    public NeedleMeterWidget( WidgetSet widgetSet, WidgetPackage widgetPackage, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        super( widgetSet, widgetPackage, width, widthPercent, height, heightPercent );
        
        initParentProperties();
    }
    
    /**
     * Creates a new {@link NeedleMeterWidget}.
     * 
     * @param widgetSet the {@link WidgetSet} this {@link Widget} belongs to
     * @param widgetPackage the package in the editor
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     */
    public NeedleMeterWidget( WidgetSet widgetSet, WidgetPackage widgetPackage, float width, float height )
    {
        this( widgetSet, widgetPackage, width, true, height, true );
    }
}
