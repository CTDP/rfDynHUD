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
package net.ctdp.rfdynhud.widgets._base.needlemeter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FactoredFloatProperty;
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
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link NeedleMeterWidget} is an abstract {@link Widget} implementation
 * for meter widgets with a needle on an analogue scale.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class NeedleMeterWidget extends Widget
{
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.IMAGE_INDICATOR + "default_rev_meter_bg.png" );
    }
    
    @Override
    protected void onBackgroundChanged( float deltaScaleX, float deltaScaleY )
    {
        super.onBackgroundChanged( deltaScaleX, deltaScaleY );
        
        if ( deltaScaleX > 0f )
        {
            markersInnerRadius.setIntValue( Math.round( markersInnerRadius.getIntValue() * deltaScaleX ) );
            markersLength.setIntValue( Math.round( markersLength.getIntValue() * ( deltaScaleX + deltaScaleY ) / 2 ) );
            valuePosX.setIntValue( Math.round( valuePosX.getIntValue() * deltaScaleX ) );
            valuePosY.setIntValue( Math.round( valuePosY.getIntValue() * deltaScaleY ) );
        }
    }
    
    protected void onNeedleImageNameChanged() {}
    
    private TransformableTexture needleTexture = null;
    
    private final ImageProperty needleImageName = new ImageProperty( this, "needleImageName", "imageName", "default_rev_meter_needle.png", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            needleTexture = null;
            onNeedleImageNameChanged();
        }
    };
    
    private final BooleanProperty displayValue = new BooleanProperty( this, "displayValue", true );
    
    private final ImageProperty valueBackgroundImageName = new ImageProperty( this, "valueBackgroundImageName", "backgroundImage", "cyan_circle.png", false, true );
    private TransformableTexture valueBackgroundTexture = null;
    private TextureImage2D valueBackgroundTexture_bak = null;
    
    private final IntProperty valuePosX = new IntProperty( this, "valuePosX", "posX", 100 );
    private final IntProperty valuePosY = new IntProperty( this, "valuePosY", "posY", 100 );
    private int valueBackgroundTexPosX, valueBackgroundTexPosY;
    
    private final FontProperty valueFont = new FontProperty( this, "valueFont", "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty valueFontColor = new ColorProperty( this, "valueFontColor", "fontColor", "#1A261C" );
    
    protected final IntProperty needlePivotBottomOffset = new IntProperty( this, "needlePivotBottomOffset", "pivotBottomOffset", 60 );
    
    protected final FactoredFloatProperty needleRotationForMinValue = new FactoredFloatProperty( this, "needleRotationForMinValue", "rotForMin", (float)Math.PI / 180f, -122.4f, -360.0f, +360.0f );
    protected final FactoredFloatProperty needleRotationForMaxValue = new FactoredFloatProperty( this, "needleRotationForMaxValue", "rotForMax", (float)Math.PI / 180f, +118.8f, -360.0f, +360.0f );
    
    protected final BooleanProperty displayMarkers = new BooleanProperty( this, "displayMarkers", true );
    protected final BooleanProperty displayMarkerNumbers = new BooleanProperty( this, "displayMarkerNumbers", "displayNumbers", true );
    protected final IntProperty markersInnerRadius = new IntProperty( this, "markersInnerRadius", "innerRadius", 224, 1, Integer.MAX_VALUE, false );
    protected final IntProperty markersLength = new IntProperty( this, "markersLength", "length", 50, 4, Integer.MAX_VALUE, false );
    
    protected int getMarkersBigStepLowerLimit()
    {
        return ( 300 );
    }
    
    protected final IntProperty markersBigStep = new IntProperty( this, "markersBigStep", "bigStep", 1000, getMarkersBigStepLowerLimit(), Integer.MAX_VALUE, false )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            fixSmallStep();
        }
    };
    
    protected int getMarkersSmallStepLowerLimit()
    {
        return ( 20 );
    }
    
    protected final IntProperty markersSmallStep = new IntProperty( this, "markersSmallStep", "smallStep", 200, getMarkersSmallStepLowerLimit(), Integer.MAX_VALUE, false )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            fixSmallStep();
        }
    };
    protected final ColorProperty markersColor = new ColorProperty( this, "markersColor", "color", "#FFFFFF" );
    protected final FontProperty markersFont = new FontProperty( this, "markersFont", "font", "Monospaced-BOLD-9va" );
    protected final ColorProperty markersFontColor = new ColorProperty( this, "markersFontColor", "fontColor", "#FFFFFF" );
    
    private DrawnString valueString = null;
    
    private final IntValue valueValue = new IntValue();
    
    private void fixSmallStep()
    {
        this.markersSmallStep.setIntValue( markersBigStep.getIntValue() / Math.round( (float)markersBigStep.getIntValue() / (float)markersSmallStep.getIntValue() ) );
    }
    
    protected boolean getDisplayValue()
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
    
    protected int getMarkersInnerRadius()
    {
        return ( markersInnerRadius.getIntValue() );
    }
    
    protected int getMarkersLength()
    {
        return ( markersLength.getIntValue() );
    }
    
    protected ImageTemplate getNeedleImage()
    {
        return ( needleImageName.getImage() );
    }
    
    protected final TransformableTexture getNeedleTexture()
    {
        return ( needleTexture );
    }
    
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
     * Gets the {@link FontColorProperty} for the value.
     * 
     * @return the {@link FontColorProperty} for the value.
     */
    protected ColorProperty getValueFontColor()
    {
        return ( valueFontColor );
    }
    
    protected int getNeedlePivotBottomOffset()
    {
        return ( needlePivotBottomOffset.getIntValue() );
    }
    
    protected float getNeedleRotationForMinValue()
    {
        return ( needleRotationForMinValue.getFactoredValue() );
    }
    
    protected float getNeedleRotationForMaxValue()
    {
        return ( needleRotationForMaxValue.getFactoredValue() );
    }
    
    private int loadValueBackgroundTexture( boolean isEditorMode )
    {
        if ( !displayValue.getBooleanValue() )
        {
            valueBackgroundTexture = null;
            valueBackgroundTexture_bak = null;
            return ( 0 );
        }
        
        try
        {
            ImageTemplate it = valueBackgroundImageName.getImage();
            
            if ( it == null )
            {
                valueBackgroundTexture = null;
                valueBackgroundTexture_bak = null;
                return ( 0 );
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
            Logger.log( t );
            
            return ( 0 );
        }
        
        return ( 1 );
    }
    
    private int loadNeedleTexture( boolean isEditorMode )
    {
        if ( needleImageName.isNoImage() )
        {
            needleTexture = null;
            return ( 0 );
        }
        
        try
        {
            ImageTemplate it = needleImageName.getImage();
            
            if ( it == null )
            {
                needleTexture = null;
                return ( 0 );
            }
            
            float scale = getBackground().getScaleX();
            int w = Math.round( it.getBaseWidth() * scale );
            int h = Math.round( it.getBaseHeight() * scale );
            needleTexture = it.getScaledTransformableTexture( w, h, needleTexture, isEditorMode );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( 0 );
        }
        
        return ( 1 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        int n = 0;
        
        n += loadValueBackgroundTexture( isEditorMode );
        n += loadNeedleTexture( isEditorMode );
        
        TransformableTexture[] result = new TransformableTexture[ n ];
        
        int i = 0;
        if ( valueBackgroundTexture != null )
            result[i++] = valueBackgroundTexture;
        if ( needleTexture != null )
            result[i++] = needleTexture;
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        valueValue.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleSetupUpdated( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onVehicleSetupUpdated( gameData, editorPresets );
        
        forceCompleteRedraw( true );
        forceReinitialization();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onNeededDataComplete( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onNeededDataComplete( gameData, editorPresets );
        
        valueValue.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        final float backgroundScaleX = getBackground().getScaleX();
        final float backgroundScaleY = getBackground().getScaleY();
        
        loadNeedleTexture( isEditorMode );
        
        if ( needleTexture != null )
        {
            needleTexture.setTranslation( (int)( ( width - needleTexture.getWidth() ) / 2 ), (int)( height / 2 - needleTexture.getHeight() + needlePivotBottomOffset.getIntValue() * backgroundScaleX ) );
            needleTexture.setRotationCenter( (int)( needleTexture.getWidth() / 2 ), (int)( needleTexture.getHeight() - needlePivotBottomOffset.getIntValue() * backgroundScaleX ) );
            //needleTexture.setRotation( 0f );
            //needleTexture.setScale( 1f, 1f );
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
            
            loadValueBackgroundTexture( isEditorMode );
            
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
     * Override {@link #getValueForValueDisplay(LiveGameData, EditorPresets)} to use a different value
     * for the digital value.
     * 
     * @param gameData
     * @param editorPresets
     * 
     * @return the value for the needle and the digital value display.
     */
    protected abstract float getValue( LiveGameData gameData, EditorPresets editorPresets );
    
    /**
     * Gets the value for the digital value display.
     * The default implementation simply gets the result of {@link #getValue(LiveGameData, EditorPresets)} and converts it to an int.
     * 
     * @param gameData
     * @param editorPresets
     * 
     * @return the value for the digital value display.
     */
    protected int getValueForValueDisplay( LiveGameData gameData, EditorPresets editorPresets )
    {
        return ( Math.round( getValue( gameData, editorPresets ) ) );
    }
    
    /**
     * Gets the minimum value for the needle and gauge.
     * 
     * @param gameData
     * @param editorPresets
     * 
     * @return the minimum value for the needle and gauge.
     */
    protected abstract float getMinValue( LiveGameData gameData, EditorPresets editorPresets );
    
    /**
     * Gets the maximum value for the needle and gauge.
     * 
     * @param gameData
     * @param editorPresets
     * 
     * @return the maximum value for the needle and gauge.
     */
    protected abstract float getMaxValue( LiveGameData gameData, EditorPresets editorPresets );
    
    /**
     * Gets the text label for the big markers at the given value.
     * 
     * @param gameData
     * @param editorPresets
     * @param value
     * 
     * @return the text label for the big markers at the given value.
     */
    protected abstract String getMarkerLabelForValue( LiveGameData gameData, EditorPresets editorPresets, float value );
    
    /**
     * 
     * @param gameData
     * @param editorPresets
     * @param texCanvas
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     * @param innerRadius
     * @param bigOuterRadius
     * @param smallOuterRadius
     */
    protected void prepareMarkersBackground( LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, float innerRadius, float bigOuterRadius, float smallOuterRadius )
    {
    }
    
    /**
     * Gets a certain marker's color at the given value.
     * 
     * @param gameData
     * @param editorPresets
     * @param value
     * @param minValue
     * @param maxValue
     * 
     * @return a certain marker's color at the given value.
     */
    protected Color getMarkerColorForValue( LiveGameData gameData, EditorPresets editorPresets, int value, int minValue, int maxValue )
    {
        return ( markersColor.getColor() );
    }
    
    /**
     * Draws the markers.
     * 
     * @param gameData
     * @param editorPresets
     * @param texCanvas
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     */
    protected void drawMarkers( LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        final float backgroundScaleX = getBackground().getScaleX();
        //final float backgroundScaleY = getBackground().getBackgroundScaleY();
        
        int minValue = (int)getMinValue( gameData, editorPresets );
        int maxValue = (int)getMaxValue( gameData, editorPresets );
        
        float centerX = offsetX + width / 2;
        float centerY = offsetY + height / 2;
        
        texCanvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        
        float innerRadius = markersInnerRadius.getIntValue() * backgroundScaleX;
        float bigOuterRadius = ( markersInnerRadius.getIntValue() + markersLength.getIntValue() - 1 ) * backgroundScaleX;
        float smallOuterRadius = innerRadius + ( bigOuterRadius - innerRadius ) * 0.75f;
        
        prepareMarkersBackground( gameData, editorPresets, texCanvas, offsetX, offsetY, width, height, innerRadius, bigOuterRadius, smallOuterRadius );
        
        if ( displayMarkers.getBooleanValue() )
        {
            Stroke oldStroke = texCanvas.getStroke();
            
            Stroke bigStroke = new BasicStroke( 2 );
            Stroke smallStroke = new BasicStroke( 1 );
            
            FontProperty numberFont = markersFont;
            texCanvas.setFont( numberFont.getFont() );
            FontMetrics metrics = numberFont.getMetrics();
            
            AffineTransform at0 = new AffineTransform( texCanvas.getTransform() );
            AffineTransform at1 = new AffineTransform( at0 );
            AffineTransform at2 = new AffineTransform();
            
            final int smallStep = markersSmallStep.getIntValue();
            for ( int value = minValue; value <= maxValue; value += smallStep )
            {
                float angle = +( needleRotationForMinValue.getFactoredValue() + ( needleRotationForMaxValue.getFactoredValue() - needleRotationForMinValue.getFactoredValue() ) * ( value / (float)maxValue ) );
                
                at2.setToRotation( angle, centerX, centerY );
                texCanvas.setTransform( at2 );
                
                texCanvas.setColor( getMarkerColorForValue( gameData, editorPresets, value, minValue, maxValue ) );
                
                if ( ( value % markersBigStep.getIntValue() ) == 0 )
                {
                    texCanvas.setStroke( bigStroke );
                    texCanvas.drawLine( Math.round( centerX ), Math.round( centerY - innerRadius ), Math.round( centerX ), Math.round( centerY - bigOuterRadius ) );
                    //texCanvas.drawLine( Math.round( centerX ), Math.round( ( centerY - innerRadius ) * backgroundScaleY / backgroundScaleX ), Math.round( centerX ), Math.round( ( centerY - outerRadius ) * backgroundScaleY / backgroundScaleX ) );
                    
                    if ( displayMarkerNumbers.getBooleanValue() )
                    {
                        String s = getMarkerLabelForValue( gameData, editorPresets, value );
                        
                        if ( s != null )
                        {
                            Rectangle2D bounds = metrics.getStringBounds( s, texCanvas );
                            float fw = (float)bounds.getWidth();
                            float fh = (float)( metrics.getAscent() - metrics.getDescent() );
                            float off = (float)Math.sqrt( fw * fw + fh * fh ) / 2f;
                            
                            at1.setToTranslation( 0f, -off );
                            at2.concatenate( at1 );
                            at1.setToRotation( -angle, Math.round( centerX ), Math.round( centerY - bigOuterRadius ) - fh / 2f );
                            at2.concatenate( at1 );
                            texCanvas.setTransform( at2 );
                            
                            texCanvas.drawString( s, Math.round( centerX ) - fw / 2f, Math.round( centerY - bigOuterRadius ) );
                        }
                    }
                }
                else
                {
                    texCanvas.setStroke( smallStroke );
                    texCanvas.drawLine( Math.round( centerX ), Math.round( centerY - innerRadius ), Math.round( centerX ), Math.round( centerY - smallOuterRadius ) );
                }
            }
            
            texCanvas.setTransform( at0 );
            texCanvas.setStroke( oldStroke );
        }
        
        texCanvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT );
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, editorPresets, texture, offsetX, offsetY, width, height, isRoot );
        
        if ( getDisplayMarkers() )
        {
            drawMarkers( gameData, editorPresets, texture.getTextureCanvas(), offsetX, offsetY, width, height );
        }
    }
    
    /**
     * Live-checks, whether the needle is to be rendered or not.
     * 
     * @param gameData
     * @param editorPresets
     * 
     * @return whether to render the needle or not.
     */
    protected boolean doRenderNeedle( LiveGameData gameData, EditorPresets editorPresets )
    {
        return ( true );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needleTexture != null )
        {
            if ( doRenderNeedle( gameData, editorPresets ) )
            {
                float value = getValue( gameData, editorPresets );
                float maxValue = getMaxValue( gameData, editorPresets );
                
                float rot0 = needleRotationForMinValue.getFactoredValue();
                float rot = -( value / maxValue ) * ( needleRotationForMinValue.getFactoredValue() - needleRotationForMaxValue.getFactoredValue() );
                
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
            valueValue.update( getValueForValueDisplay( gameData, editorPresets ) );
            if ( needsCompleteRedraw || ( clock1 && valueValue.hasChanged() ) )
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
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( needleImageName, "The name of the needle image." );
        writer.writeProperty( needlePivotBottomOffset, "The offset in (unscaled) pixels from the bottom of the image, where the center of the needle's axis is." );
        writer.writeProperty( needleRotationForMinValue, "The rotation for the needle image, that it has for min value (in degrees)." );
        writer.writeProperty( needleRotationForMaxValue, "The rotation for the needle image, that it has for max value (in degrees)." );
        writer.writeProperty( displayMarkers, "Display markers?" );
        writer.writeProperty( displayMarkerNumbers, "Display marker numbers?" );
        writer.writeProperty( markersInnerRadius, "The inner radius of the markers (in background image space)" );
        writer.writeProperty( markersLength, "The length of the markers (in background image space)" );
        writer.writeProperty( markersBigStep, "Step size of bigger rev markers" );
        writer.writeProperty( markersSmallStep, "Step size of smaller rev markers" );
        writer.writeProperty( markersColor, "The color used to draw the markers." );
        writer.writeProperty( markersFont, "The font used to draw the marker numbers." );
        writer.writeProperty( markersFontColor, "The font color used to draw the marker numbers." );
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
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( needleImageName ) );
        else if ( loader.loadProperty( needlePivotBottomOffset ) );
        else if ( loader.loadProperty( needleRotationForMinValue ) );
        else if ( loader.loadProperty( needleRotationForMaxValue ) );
        else if ( loader.loadProperty( displayMarkers ) );
        else if ( loader.loadProperty( displayMarkerNumbers ) );
        else if ( loader.loadProperty( markersInnerRadius ) );
        else if ( loader.loadProperty( markersLength ) );
        else if ( loader.loadProperty( markersBigStep ) );
        else if ( loader.loadProperty( markersSmallStep ) );
        else if ( loader.loadProperty( markersColor ) );
        else if ( loader.loadProperty( markersFont ) );
        else if ( loader.loadProperty( markersFontColor ) );
        else if ( loader.loadProperty( valueBackgroundImageName ) );
        else if ( loader.loadProperty( valuePosX ) );
        else if ( loader.loadProperty( valuePosY ) );
        else if ( loader.loadProperty( valueFont ) );
        else if ( loader.loadProperty( valueFontColor ) );
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
    protected boolean getSpecificPropertiesFirst( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        return ( false );
    }
    
    /**
     * Collects the properties for the needle.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void getNeedleProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( needleImageName );
        propsCont.addProperty( needlePivotBottomOffset );
        propsCont.addProperty( needleRotationForMinValue );
        propsCont.addProperty( needleRotationForMaxValue );
    }
    
    /**
     * Collects the properties for the markers.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void getMarkersProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( displayMarkers );
        propsCont.addProperty( displayMarkerNumbers );
        propsCont.addProperty( markersInnerRadius );
        propsCont.addProperty( markersLength );
        propsCont.addProperty( markersBigStep );
        propsCont.addProperty( markersSmallStep );
        propsCont.addProperty( markersColor );
        propsCont.addProperty( markersFont );
        propsCont.addProperty( markersFontColor );
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
    protected void getDigiValueProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
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
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        getSpecificPropertiesFirst( propsCont, forceAll );
        
        propsCont.addGroup( "Needle" );
        
        getNeedleProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Markers" );
        
        getMarkersProperties( propsCont, forceAll );
        
        propsCont.addGroup( getDigiValuePropertiesGroupName() );
        
        getDigiValueProperties( propsCont, forceAll );
    }
    
    /**
     * This method is called as the last item in the constructor.
     */
    protected void initParentProperties()
    {
    }
    
    /**
     * Creates a new {@link NeedleMeterWidget}.
     * 
     * @param name
     * @param width negative numbers for (screen_width - width)
     * @param widthPercent width parameter treated as percents
     * @param height negative numbers for (screen_height - height)
     * @param heightPercent height parameter treated as percents
     */
    public NeedleMeterWidget( String name, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        super( name, width, widthPercent, height, heightPercent );
        
        initParentProperties();
    }
    
    /**
     * Creates a new {@link NeedleMeterWidget}.
     * 
     * @param name
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     */
    public NeedleMeterWidget( String name, float width, float height )
    {
        this( name, width, true, height, true );
    }
}
