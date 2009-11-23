package net.ctdp.rfdynhud.widgets.revmeter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.BooleanProperty;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.FloatProperty;
import net.ctdp.rfdynhud.editor.properties.FontProperty;
import net.ctdp.rfdynhud.editor.properties.ImageProperty;
import net.ctdp.rfdynhud.editor.properties.IntegerProperty;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.PhysicsSetting;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.IntValue;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link RevMeterWidget} displays rev/RPM information.
 * 
 * @author Marvin Froehlich
 */
public class RevMeterWidget extends Widget
{
    private final ImageProperty backgroundImageName = new ImageProperty( this, "backgroundImageName", "revmeter.png" );
    private final ImageProperty needleImageName = new ImageProperty( this, "needleImageName", "imageName", "needle.png" );
    private final ImageProperty shiftLightImageName = new ImageProperty( this, "shiftLightImageName", "imageName", "shiftlight_on.png" );
    private final ImageProperty gearBackgroundImageName = new ImageProperty( this, "gearBackgroundImageName", "backgroundImageName", "", false, true );
    private final ImageProperty boostNumberBackgroundImageName = new ImageProperty( this, "boostNumberBackgroundImageName", "numberBGImageName", "", false, true );
    
    private TextureImage2D backgroundTexture = null;
    private TransformableTexture needleTexture = null;
    private TransformableTexture shiftLightTexture = null;
    private TransformableTexture gearBackgroundTexture = null;
    private TextureImage2D gearBackgroundTexture_bak = null;
    private TransformableTexture boostNumberBackgroundTexture = null;
    private TextureImage2D boostNumberBackgroundTexture_bak = null;
    
    private int gearBackgroundTexPosX, gearBackgroundTexPosY;
    private int boostNumberBackgroundTexPosX, boostNumberBackgroundTexPosY;
    
    private float backgroundScaleX, backgroundScaleY;
    
    private final IntegerProperty needleAxisBottomOffset = new IntegerProperty( this, "needleAxisBottomOffset", "axisBottomOffset", 12 );
    
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
    
    private final IntegerProperty editorRPM = new IntegerProperty( this, "editorRPM", 3750 );
    
    private final BooleanProperty displayRevMarkers = new BooleanProperty( this, "displayRevMarkers", true );
    private final BooleanProperty displayRevMarkerNumbers = new BooleanProperty( this, "displayRevMarkerNumbers", true );
    private final IntegerProperty revMarkersInnerRadius = new IntegerProperty( this, "revMarkersInnerRadius", "innerRadius", 224 );
    private final IntegerProperty revMarkersLength = new IntegerProperty( this, "revMarkersLength", "length", 50, 4, Integer.MAX_VALUE, false );
    private final IntegerProperty revMarkersBigStep = new IntegerProperty( this, "revMarkersBigStep", "bigStep", 1000, 300, Integer.MAX_VALUE, false )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            fixSmallStep();
        }
    };
    private final IntegerProperty revMarkersSmallStep = new IntegerProperty( this, "revMarkersSmallStep", "smallStep", 200, 20, Integer.MAX_VALUE, false )
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
    private final BooleanProperty interpolateMarkerColors = new BooleanProperty( this, "interpolateMarkerColors", "interpolateColors", false );
    
    private final BooleanProperty displayShiftLight = new BooleanProperty( this, "displayShiftLight", true );
    private final IntegerProperty shiftLightPosX = new IntegerProperty( this, "shiftLightPosX", "posX", 625 );
    private final IntegerProperty shiftLightPosY = new IntegerProperty( this, "shiftLightPosY", "posY", 42 );
    private final IntegerProperty shiftLightRPM = new IntegerProperty( this, "shiftLightRPM", "activationRPM", -500 );
    
    private final IntegerProperty gearPosX = new IntegerProperty( this, "gearPosX", "posX", 354 );
    private final IntegerProperty gearPosY = new IntegerProperty( this, "gearPosY", "posY", 512 );
    
    private final FontProperty gearFont = new FontProperty( this, "gearFont", "font", "GearFont" );
    private final ColorProperty gearFontColor = new ColorProperty( this, "gearFontColor", "fontColor", "#C0BC3D" );
    
    private final IntegerProperty rpmPosY = new IntegerProperty( this, "rpmPosY", 603 );
    
    private DrawnString rpmString = null;
    private DrawnString gearString = null;
    private DrawnString boostString = null;
    
    private final IntValue gear = new IntValue();
    private final IntValue boost = new IntValue();
    
    private final BooleanProperty displayBoostBar = new BooleanProperty( this, "displayBoostBar", "displayBar", true );
    private final IntegerProperty boostBarPosX = new IntegerProperty( this, "boostBarPosX", "barPosX", 135 );
    private final IntegerProperty boostBarPosY = new IntegerProperty( this, "boostBarPosY", "barPosY", 560 );
    private final IntegerProperty boostBarWidth = new IntegerProperty( this, "boostBarWidth", "barWidth", 438 );
    private final IntegerProperty boostBarHeight = new IntegerProperty( this, "boostBarHeight", "barHeight", 35 );
    private final BooleanProperty displayBoostNumber = new BooleanProperty( this, "displayBoostNumber", "displayNumber", true );
    private final IntegerProperty boostNumberPosX = new IntegerProperty( this, "boostNumberPosX", "numberPosX", 392 );
    private final IntegerProperty boostNumberPosY = new IntegerProperty( this, "boostNumberPosY", "numberPosY", 544 );
    private final FontProperty boostNumberFont = new FontProperty( this, "boostNumberFont", "numberFont", "StandardFont" );
    private final ColorProperty boostNumberFontColor = new ColorProperty( this, "boostNumberFontColor", "numberFontColor", "#FF0000" );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object createLocalStore()
    {
        return ( new LocalStore() );
    }
    
    private void fixSmallStep()
    {
        this.revMarkersSmallStep.setIntegerValue( revMarkersBigStep.getIntegerValue() / Math.round( (float)revMarkersBigStep.getIntegerValue() / (float)revMarkersSmallStep.getIntegerValue() ) );
    }
    
    private int loadNeedleTexture( boolean isEditorMode )
    {
        if ( ( needleTexture == null ) || isEditorMode )
        {
            try
            {
                BufferedImage bi = backgroundImageName.getBufferedImage();
                float scale = ( bi == null ) ? 1.0f : getSize().getEffectiveWidth() / (float)bi.getWidth();
                bi = needleImageName.getBufferedImage();
                
                if ( bi == null )
                {
                    needleTexture = null;
                    return ( 0 );
                }
                
                int needleWidth = (int)( bi.getWidth() * scale );
                int needleHeight = (int)( bi.getHeight() * scale );
                if ( ( needleTexture == null ) || ( needleTexture.getWidth() != needleWidth ) || ( needleTexture.getHeight() != needleHeight ) )
                {
                    needleTexture = new TransformableTexture( needleWidth, needleHeight, 0, 0, 0, 0, 0f, 1f, 1f );
                    needleTexture.getTexture().clear( false, null );
                    needleTexture.getTexture().getTextureCanvas().setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
                    needleTexture.getTexture().getTextureCanvas().drawImage( bi, 0, 0, needleTexture.getWidth(), needleTexture.getHeight(), 0, 0, bi.getWidth(), bi.getHeight() );
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
    
    private int loadShiftLightTexture( boolean isEditorMode )
    {
        if ( ( shiftLightTexture == null ) || isEditorMode )
        {
            try
            {
                BufferedImage bi0 = backgroundImageName.getBufferedImage();
                float scale = ( bi0 == null ) ? 1.0f : getSize().getEffectiveWidth() / (float)bi0.getWidth();
                BufferedImage bi = shiftLightImageName.getBufferedImage();
                
                if ( bi == null )
                {
                    shiftLightTexture = null;
                    return ( 0 );
                }
                
                int slWidth = (int)( bi.getWidth() * scale );
                int slHeight = (int)( bi.getHeight() * scale );
                if ( ( shiftLightTexture == null ) || ( shiftLightTexture.getWidth() != slWidth ) || ( shiftLightTexture.getHeight() != slHeight * 2 ) )
                {
                    shiftLightTexture = new TransformableTexture( slWidth, slHeight * 2, 0, 0, 0, 0, 0f, 1f, 1f );
                    shiftLightTexture.getTexture().clear( false, null );
                    shiftLightTexture.getTexture().getTextureCanvas().setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
                    shiftLightTexture.getTexture().getTextureCanvas().drawImage( bi, 0, 0, slWidth, slHeight, 0, 0, bi.getWidth(), bi.getHeight() );
                    shiftLightTexture.getTexture().getTextureCanvas().drawImage( bi0, 0, slHeight, slWidth, shiftLightTexture.getHeight(), shiftLightPosX.getIntegerValue(), shiftLightPosY.getIntegerValue(), shiftLightPosX.getIntegerValue() + bi.getWidth(), shiftLightPosY.getIntegerValue() + bi.getHeight() );
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
                BufferedImage bi0 = backgroundImageName.getBufferedImage();
                float scale = ( bi0 == null ) ? 1.0f : getSize().getEffectiveWidth() / (float)bi0.getWidth();
                BufferedImage bi = gearBackgroundImageName.getBufferedImage();
                
                if ( bi == null )
                {
                    gearBackgroundTexture = null;
                    gearBackgroundTexture_bak = null;
                    return ( 0 );
                }
                
                int gbWidth = (int)( bi.getWidth() * scale );
                int gbHeight = (int)( bi.getHeight() * scale );
                if ( ( gearBackgroundTexture == null ) || ( gearBackgroundTexture.getWidth() != gbWidth ) || ( gearBackgroundTexture.getHeight() != gbHeight ) )
                {
                    gearBackgroundTexture = new TransformableTexture( gbWidth, gbHeight, 0, 0, 0, 0, 0f, 1f, 1f );
                    gearBackgroundTexture.getTexture().clear( false, null );
                    gearBackgroundTexture.getTexture().getTextureCanvas().setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
                    gearBackgroundTexture.getTexture().getTextureCanvas().drawImage( bi, 0, 0, gearBackgroundTexture.getWidth(), gearBackgroundTexture.getHeight(), 0, 0, bi.getWidth(), bi.getHeight() );
                    
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
                BufferedImage bi0 = backgroundImageName.getBufferedImage();
                float scale = ( bi0 == null ) ? 1.0f : getSize().getEffectiveWidth() / (float)bi0.getWidth();
                BufferedImage bi = boostNumberBackgroundImageName.getBufferedImage();
                
                if ( bi == null )
                {
                    boostNumberBackgroundTexture = null;
                    boostNumberBackgroundTexture_bak = null;
                    return ( 0 );
                }
                
                int bnbWidth = (int)( bi.getWidth() * scale );
                int bnbHeight = (int)( bi.getHeight() * scale );
                if ( ( boostNumberBackgroundTexture == null ) || ( boostNumberBackgroundTexture.getWidth() != bnbWidth ) || ( boostNumberBackgroundTexture.getHeight() != bnbHeight ) )
                {
                    boostNumberBackgroundTexture = new TransformableTexture( bnbWidth, bnbHeight, 0, 0, 0, 0, 0f, 1f, 1f );
                    boostNumberBackgroundTexture.getTexture().clear( false, null );
                    boostNumberBackgroundTexture.getTexture().getTextureCanvas().setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
                    boostNumberBackgroundTexture.getTexture().getTextureCanvas().drawImage( bi, 0, 0, boostNumberBackgroundTexture.getWidth(), boostNumberBackgroundTexture.getHeight(), 0, 0, bi.getWidth(), bi.getHeight() );
                    
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
    
    @Override
    public TransformableTexture[] getSubTextures( boolean isEditorMode, int widgetWidth, int widgetHeight )
    {
        int n = 0;
        
        n += loadNeedleTexture( isEditorMode );
        
        if ( displayShiftLight.getBooleanValue() )
            n += loadShiftLightTexture( isEditorMode );
        else
            shiftLightTexture = null;
        
        if ( !gearBackgroundImageName.getStringValue().equals( "" ) )
            n += loadGearBackgroundTexture( isEditorMode );
        else
            gearBackgroundTexture = null;
        
        if ( !boostNumberBackgroundImageName.getStringValue().equals( "" ) )
            n += loadBoostNumberBackgroundTexture( isEditorMode );
        else
            boostNumberBackgroundTexture = null;
        
        TransformableTexture[] result = new TransformableTexture[ n ];
        
        int i = 0;
        if ( needleTexture != null )
            result[i++] = needleTexture;
        if ( shiftLightTexture != null )
            result[i++] = shiftLightTexture;
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
    public void onRealtimeExited( boolean isEditorMode, LiveGameData gameData )
    {
        super.onRealtimeEntered( isEditorMode, gameData );
        
        gear.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBoundInputStateChanged( boolean isEditorMode, InputAction action, boolean state, int modifierMask )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        boolean reloadBackground = ( backgroundTexture == null );
        
        if ( isEditorMode && ( backgroundTexture != null ) && ( ( backgroundTexture.getWidth() != width ) || ( backgroundTexture.getHeight() != height ) ) )
            reloadBackground = true;
        
        if ( reloadBackground )
        {
            try
            {
                BufferedImage bi = backgroundImageName.getBufferedImage();
                backgroundTexture = TextureImage2D.createOfflineTexture( width, height, true );
                backgroundTexture.clear( false, null );
                backgroundTexture.getTextureCanvas().setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
                backgroundTexture.getTextureCanvas().drawImage( bi, 0, 0, width, height, 0, 0, bi.getWidth(), bi.getHeight() );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
        
        loadNeedleTexture( isEditorMode );
        
        BufferedImage bi = backgroundImageName.getBufferedImage();
        backgroundScaleX = (float)width / (float)bi.getWidth();
        backgroundScaleY = (float)height / (float)bi.getHeight();
        
        if ( displayShiftLight.getBooleanValue() )
        {
            loadShiftLightTexture( isEditorMode );
        }
        
        needleTexture.setTranslation( (int)( ( width - needleTexture.getWidth() ) / 2 ), (int)( height / 2 - needleTexture.getHeight() + needleAxisBottomOffset.getIntegerValue() * backgroundScaleX ) );
        needleTexture.setRotationCenter( (int)( needleTexture.getWidth() / 2 ), (int)( needleTexture.getHeight() - needleAxisBottomOffset.getIntegerValue() * backgroundScaleX ) );
        //needleTexture.setRotation( 0f );
        //needleTexture.setScale( 1f, 1f );
        
        FontMetrics metrics = texCanvas.getFontMetrics( gearFont.getFont() );
        Rectangle2D bounds = metrics.getStringBounds( "X", texCanvas );
        double fw = bounds.getWidth();
        double fh = metrics.getAscent() - metrics.getDescent();
        int fx, fy;
        
        if ( !gearBackgroundImageName.getStringValue().equals( "" ) )
            loadGearBackgroundTexture( isEditorMode );
        else
            gearBackgroundTexture = null;
        
        if ( gearBackgroundTexture == null )
        {
            fx = Math.round( gearPosX.getIntegerValue() * backgroundScaleX );
            fy = Math.round( gearPosY.getIntegerValue() * backgroundScaleY );
        }
        else
        {
            gearBackgroundTexPosX = Math.round( gearPosX.getIntegerValue() * backgroundScaleX - gearBackgroundTexture.getWidth() / 2.0f );
            gearBackgroundTexPosY = Math.round( gearPosY.getIntegerValue() * backgroundScaleY - gearBackgroundTexture.getHeight() / 2.0f );
            
            fx = gearBackgroundTexture.getWidth() / 2;
            fy = gearBackgroundTexture.getHeight() / 2;
        }
        
        gearString = new DrawnString( fx - (int)( fw / 2.0 ), fy - (int)( metrics.getDescent() + fh / 2.0 ), Alignment.LEFT, false, gearFont.getFont(), gearFont.isAntiAliased(), gearFontColor.getColor(), null );
        
        metrics = texCanvas.getFontMetrics( boostNumberFont.getFont() );
        bounds = metrics.getStringBounds( "0", texCanvas );
        fw = bounds.getWidth();
        fh = metrics.getAscent() - metrics.getDescent();
        
        if ( !boostNumberBackgroundImageName.getStringValue().equals( "" ) )
            loadBoostNumberBackgroundTexture( isEditorMode );
        else
            boostNumberBackgroundTexture = null;
        
        if ( boostNumberBackgroundTexture == null )
        {
            fx = Math.round( boostNumberPosX.getIntegerValue() * backgroundScaleX );
            fy = Math.round( boostNumberPosY.getIntegerValue() * backgroundScaleY );
        }
        else
        {
            boostNumberBackgroundTexPosX = Math.round( boostNumberPosX.getIntegerValue() * backgroundScaleX - boostNumberBackgroundTexture.getWidth() / 2.0f );
            boostNumberBackgroundTexPosY = Math.round( boostNumberPosY.getIntegerValue() * backgroundScaleY - boostNumberBackgroundTexture.getHeight() / 2.0f );
            
            fx = boostNumberBackgroundTexture.getWidth() / 2;
            fy = boostNumberBackgroundTexture.getHeight() / 2;
        }
        
        boostString = new DrawnString( fx - (int)( fw / 2.0 ), fy - (int)( metrics.getDescent() + fh / 2.0 ), Alignment.LEFT, false, boostNumberFont.getFont(), boostNumberFont.isAntiAliased(), boostNumberFontColor.getColor(), null );
        
        rpmString = new DrawnString( width / 2, Math.round( rpmPosY.getIntegerValue() * backgroundScaleY ), Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor(), null );
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
        if ( !displayRevMarkers.getBooleanValue() )
            return;
        
        LocalStore store = (LocalStore)getLocalStore();
        
        float maxRPM = gameData.getTelemetryData().getEngineMaxRPM();
        if ( maxRPM > 100f )
            store.storedBaseMaxRPM = maxRPM;
        maxRPM = gameData.getPhysics().getEngine().getMaxRPM( store.storedBaseMaxRPM );
        
        VehiclePhysics.Engine engine = gameData.getPhysics().getEngine();
        PhysicsSetting boostRange = engine.getBoostRange();
        
        int minBoost = ( engine.getRPMIncreasePerBoostLevel() > 0f ) ? (int)boostRange.getMinValue() : (int)boostRange.getMaxValue();
        //int maxBoost = ( engine.getRPMIncreasePerBoostLevel() > 0f ) ? (int)boostRange.getMaxValue() : (int)boostRange.getMinValue();
        int mediumBoost = Math.round( boostRange.getMinValue() + ( boostRange.getMaxValue() - boostRange.getMinValue() ) / 2f );
        
        float lowRPM = gameData.getPhysics().getEngine().getMaxRPM( store.storedBaseMaxRPM, minBoost );
        float mediumRPM = gameData.getPhysics().getEngine().getMaxRPM( store.storedBaseMaxRPM, mediumBoost );
        
        float centerX = offsetX + width / 2;
        float centerY = offsetY + height / 2;
        
        texCanvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        
        Stroke oldStroke = texCanvas.getStroke();
        
        Stroke thousand = new BasicStroke( 2 );
        Stroke twoHundred = new BasicStroke( 1 );
        
        AffineTransform at0 = new AffineTransform( texCanvas.getTransform() );
        AffineTransform at1 = new AffineTransform( at0 );
        AffineTransform at2 = new AffineTransform();
        
        float innerRadius = revMarkersInnerRadius.getIntegerValue() * backgroundScaleX;
        float outerRadius = ( revMarkersInnerRadius.getIntegerValue() + revMarkersLength.getIntegerValue() - 1 ) * backgroundScaleX;
        float outerRadius2 = innerRadius + ( outerRadius - innerRadius ) * 0.75f;
        
        Font numberFont = revMarkersFont.getFont();
        texCanvas.setFont( numberFont );
        FontMetrics metrics = texCanvas.getFontMetrics( numberFont );
        
        final int smallStep = revMarkersSmallStep.getIntegerValue();
        for ( int rpm = 0; rpm <= maxRPM; rpm += smallStep )
        {
            float angle = -( needleRotationForZeroRPM.getFloatValue() + ( needleRotationForMaxRPM.getFloatValue() - needleRotationForZeroRPM.getFloatValue() ) * ( rpm / maxRPM ) );
            
            at2.setToRotation( angle, centerX, centerY );
            texCanvas.setTransform( at2 );
            
            if ( rpm <= lowRPM )
                texCanvas.setColor( revMarkersColor.getColor() );
            else if ( rpm <= mediumRPM )
                texCanvas.setColor( interpolateMarkerColors.getBooleanValue() ? interpolateColor( revMarkersColor.getColor(), revMarkersMediumColor.getColor(), ( rpm - lowRPM ) / ( mediumRPM - lowRPM ) ) : revMarkersMediumColor.getColor() );
            else
                texCanvas.setColor( interpolateMarkerColors.getBooleanValue() ? interpolateColor( revMarkersMediumColor.getColor(), revMarkersHighColor.getColor(), ( rpm - mediumRPM ) / ( maxRPM - mediumRPM ) ) : revMarkersHighColor.getColor() );
            
            if ( ( rpm % revMarkersBigStep.getIntegerValue() ) == 0 )
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
        
        texCanvas.setTransform( at0 );
        texCanvas.setStroke( oldStroke );
        texCanvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT );
    }
    
    @Override
    protected void clearBackground( boolean isEditorMode, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        texCanvas.getImage().clear( backgroundTexture, offsetX, offsetY, width, height, true, null );
        
        if ( displayShiftLight.getBooleanValue() )
        {
            loadShiftLightTexture( isEditorMode );
            
            texCanvas.getImage().clear( offsetX + Math.round( shiftLightPosX.getIntegerValue() * backgroundScaleX ), offsetY + Math.round( shiftLightPosY.getIntegerValue() * backgroundScaleY ), shiftLightTexture.getWidth(), shiftLightTexture.getHeight(), true, null );
        }
        
        drawMarks( gameData, texCanvas, offsetX, offsetY, width, height );
    }
    
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
    protected void drawWidget( boolean isEditorMode, boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        TextureImage2D image = texCanvas.getImage();
        TelemetryData telemData = gameData.getTelemetryData();
        
        gear.update( telemData.getCurrentGear() );
        if ( needsCompleteRedraw || gear.hasChanged() )
        {
            String string = gear.getValue() == -1 ? "R" : gear.getValue() == 0 ? "N" : String.valueOf( gear );
            
            if ( gearBackgroundTexture == null )
                gearString.draw( offsetX, offsetY, string, backgroundTexture, image );
            else
                gearString.draw( 0, 0, string, gearBackgroundTexture_bak, gearBackgroundTexture.getTexture() );
        }
        
        boost.update( telemData.getEngineBoostMapping() );
        if ( needsCompleteRedraw || boost.hasChanged() )
        {
            if ( displayBoostNumber.getBooleanValue() )
            {
                if ( boostNumberBackgroundTexture == null )
                    boostString.draw( offsetX, offsetY, boost.getValueAsString(), backgroundTexture, image );
                else
                    boostString.draw( 0, 0, boost.getValueAsString(), boostNumberBackgroundTexture_bak, boostNumberBackgroundTexture.getTexture() );
            }
            
            if ( displayBoostBar.getBooleanValue() )
            {
                int maxBoost = (int)gameData.getPhysics().getEngine().getBoostRange().getMaxValue();
                boolean inverted = ( gameData.getPhysics().getEngine().getRPMIncreasePerBoostLevel() < 0f );
                boolean tempBoost = false;
                drawBoostBar( boost.getValue(), maxBoost, inverted, tempBoost, texCanvas, offsetX + Math.round( boostBarPosX.getIntegerValue() * backgroundScaleX ), offsetY + Math.round( boostBarPosY.getIntegerValue() * backgroundScaleY ), Math.round( boostBarWidth.getIntegerValue() * backgroundScaleX ), Math.round( boostBarHeight.getIntegerValue() * backgroundScaleY ) );
            }
        }
        
        LocalStore store = (LocalStore)getLocalStore();
        
        float rpm = isEditorMode ? editorRPM.getIntegerValue() : telemData.getEngineRPM();
        //if ( gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleControl() == VehicleControl.LOCAL_PLAYER )
        float maxRPM = telemData.getEngineMaxRPM();
        if ( maxRPM > 100f )
            store.storedBaseMaxRPM = maxRPM;
        maxRPM = gameData.getPhysics().getEngine().getMaxRPM( store.storedBaseMaxRPM );
        
        if ( needsCompleteRedraw || clock1 )
        {
            String string = NumberUtil.formatFloat( rpm, 0, false ) + " / " + NumberUtil.formatFloat( maxRPM, 0, false );
            rpmString.draw( offsetX, offsetY, string, backgroundTexture, image );
        }
        
        if ( shiftLightTexture != null )
        {
            float maxRPM_boost = gameData.getPhysics().getEngine().getMaxRPM( store.storedBaseMaxRPM, boost.getValue() );
            if ( rpm >= maxRPM_boost + shiftLightRPM.getIntegerValue() )
            {
                shiftLightTexture.setClipRect( 0, 0, shiftLightTexture.getWidth(), shiftLightTexture.getHeight() / 2, true );
                shiftLightTexture.setTranslation( Math.round( shiftLightPosX.getIntegerValue() * backgroundScaleX ), Math.round( shiftLightPosY.getIntegerValue() * backgroundScaleY ) );
            }
            else
            {
                shiftLightTexture.setClipRect( 0, shiftLightTexture.getHeight() / 2, shiftLightTexture.getWidth(), shiftLightTexture.getHeight() / 2, true );
                shiftLightTexture.setTranslation( Math.round( shiftLightPosX.getIntegerValue() * backgroundScaleX ), Math.round( shiftLightPosY.getIntegerValue() * backgroundScaleY ) - shiftLightTexture.getHeight() / 2 );
            }
        }
        
        if ( needleTexture != null )
        {
            float rot0 = needleRotationForZeroRPM.getFloatValue();
            float rot = -( rpm / maxRPM ) * ( needleRotationForZeroRPM.getFloatValue() - needleRotationForMaxRPM.getFloatValue() );
            
            needleTexture.setRotation( -rot0 - rot );
        }
        
        if ( isEditorMode )
        {
            if ( needleTexture != null )
            {
                needleTexture.drawInEditor( texCanvas, offsetX, offsetY );
            }
            
            if ( shiftLightTexture != null )
            {
                texCanvas.getImage().clear( offsetX + Math.round( shiftLightPosX.getIntegerValue() * backgroundScaleX ), offsetX + Math.round( shiftLightPosY.getIntegerValue() * backgroundScaleY ), shiftLightTexture.getWidth(), shiftLightTexture.getHeight(), true, null );
                shiftLightTexture.drawInEditor( texCanvas, offsetX, offsetY );
            }
        }
        
        if ( gearBackgroundTexture != null )
        {
            if ( isEditorMode )
                gearBackgroundTexture.drawInEditor( texCanvas, offsetX + gearBackgroundTexPosX, offsetY + gearBackgroundTexPosY );
            else
                gearBackgroundTexture.setTranslation( gearBackgroundTexPosX, gearBackgroundTexPosY );
        }
        
        if ( boostNumberBackgroundTexture != null )
        {
            if ( isEditorMode )
                boostNumberBackgroundTexture.drawInEditor( texCanvas, offsetX + boostNumberBackgroundTexPosX, offsetY + boostNumberBackgroundTexPosY );
            else
                boostNumberBackgroundTexture.setTranslation( boostNumberBackgroundTexPosX, boostNumberBackgroundTexPosY );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( backgroundImageName, "The name of the background image." );
        writer.writeProperty( needleImageName, "The name of the needle image." );
        writer.writeProperty( needleAxisBottomOffset, "The offset in (unscaled) pixels from the bottom of the image, where the center of the needle's axis is." );
        writer.writeProperty( needleRotationForZeroRPM, "The rotation for the needle image, that is has for zero RPM (in degrees)." );
        writer.writeProperty( needleRotationForMaxRPM, "The rotation for the needle image, that is has for maximum RPM (in degrees)." );
        writer.writeProperty( editorRPM, "The RPM (rounds per minute) displayed in the editor (not in rFactor)" );
        writer.writeProperty( displayRevMarkers, "Display rev markers?" );
        writer.writeProperty( displayRevMarkerNumbers, "Display rev marker numbers?" );
        writer.writeProperty( revMarkersInnerRadius, "The inner radius of the rev markers (in background image space)" );
        writer.writeProperty( revMarkersLength, "The length of the rev markers (in background image space)" );
        writer.writeProperty( revMarkersBigStep, "Step size of bigger rev markers" );
        writer.writeProperty( revMarkersSmallStep, "Step size of smaller rev markers" );
        writer.writeProperty( revMarkersColor, "The color used to draw the rev markers." );
        writer.writeProperty( revMarkersMediumColor, "The color used to draw the rev markers for medium boost." );
        writer.writeProperty( revMarkersHighColor, "The color used to draw the rev markers for high revs." );
        writer.writeProperty( revMarkersFont, "The font used to draw the rev marker numbers." );
        writer.writeProperty( revMarkersFontColor, "The font color used to draw the rev marker numbers." );
        writer.writeProperty( displayShiftLight, "Display a shift light?" );
        writer.writeProperty( shiftLightImageName, "The name of the shift light image." );
        writer.writeProperty( shiftLightPosX, "The x-offset in pixels to the gear label." );
        writer.writeProperty( shiftLightPosY, "The y-offset in pixels to the gear label." );
        writer.writeProperty( shiftLightRPM, "The RPM (rounds per minute) to subtract from the maximum for the level to display shoft light on" );
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
        writer.writeProperty( rpmPosY, "The offset in (background image space) pixels from the top of the Widget, where the text is to be placed." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( backgroundImageName.loadProperty( key, value ) );
        else if ( needleImageName.loadProperty( key, value ) );
        else if ( needleAxisBottomOffset.loadProperty( key, value ) );
        else if ( needleRotationForZeroRPM.loadProperty( key, value ) );
        else if ( needleRotationForMaxRPM.loadProperty( key, value ) );
        else if ( editorRPM.loadProperty( key, value ) );
        else if ( displayShiftLight.loadProperty( key, value ) );
        else if ( shiftLightImageName.loadProperty( key, value ) );
        else if ( shiftLightPosX.loadProperty( key, value ) );
        else if ( shiftLightPosY.loadProperty( key, value ) );
        else if ( shiftLightRPM.loadProperty( key, value ) );
        else if ( displayRevMarkers.loadProperty( key, value ) );
        else if ( displayRevMarkerNumbers.loadProperty( key, value ) );
        else if ( revMarkersInnerRadius.loadProperty( key, value ) );
        else if ( revMarkersLength.loadProperty( key, value ) );
        else if ( revMarkersBigStep.loadProperty( key, value ) );
        else if ( revMarkersSmallStep.loadProperty( key, value ) );
        else if ( revMarkersColor.loadProperty( key, value ) );
        else if ( revMarkersMediumColor.loadProperty( key, value ) );
        else if ( revMarkersHighColor.loadProperty( key, value ) );
        else if ( revMarkersFont.loadProperty( key, value ) );
        else if ( revMarkersFontColor.loadProperty( key, value ) );
        else if ( gearBackgroundImageName.loadProperty( key, value ) );
        else if ( gearPosX.loadProperty( key, value ) );
        else if ( gearPosY.loadProperty( key, value ) );
        else if ( gearFont.loadProperty( key, value ) );
        else if ( gearFontColor.loadProperty( key, value ) );
        else if ( rpmPosY.loadProperty( key, value ) );
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
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( FlaggedList propsList )
    {
        super.getProperties( propsList );
        
        FlaggedList props = new FlaggedList( "Specific", true );
        
        props.add( backgroundImageName );
        
        FlaggedList needleProps = new FlaggedList( "Needle", true );
        
        needleProps.add( needleImageName );
        needleProps.add( needleAxisBottomOffset );
        needleProps.add( needleRotationForZeroRPM );
        needleProps.add( needleRotationForMaxRPM );
        
        props.add( needleProps );
        
        props.add( editorRPM );
        
        FlaggedList revMarkersProps = new FlaggedList( "Rev Markers", true );
        
        revMarkersProps.add( displayRevMarkers );
        revMarkersProps.add( displayRevMarkerNumbers );
        revMarkersProps.add( revMarkersInnerRadius );
        revMarkersProps.add( revMarkersLength );
        revMarkersProps.add( revMarkersBigStep );
        revMarkersProps.add( revMarkersSmallStep );
        revMarkersProps.add( revMarkersColor );
        revMarkersProps.add( revMarkersMediumColor );
        revMarkersProps.add( revMarkersHighColor );
        revMarkersProps.add( revMarkersFont );
        revMarkersProps.add( revMarkersFontColor );
        
        props.add( revMarkersProps );
        
        FlaggedList shiftLightProps = new FlaggedList( "Shift Light", true );
        
        shiftLightProps.add( displayShiftLight );
        shiftLightProps.add( shiftLightImageName );
        shiftLightProps.add( shiftLightPosX );
        shiftLightProps.add( shiftLightPosY );
        shiftLightProps.add( shiftLightRPM );
        
        props.add( shiftLightProps );
        
        FlaggedList gearProps = new FlaggedList( "Gear", true );
        
        gearProps.add( gearBackgroundImageName );
        gearProps.add( gearPosX );
        gearProps.add( gearPosY );
        gearProps.add( gearFont );
        gearProps.add( gearFontColor );
        
        props.add( gearProps );
        
        FlaggedList boostProps = new FlaggedList( "Engine Boost", true );
        
        boostProps.add( displayBoostBar );
        boostProps.add( boostBarPosX );
        
        boostProps.add( boostBarPosY );
        boostProps.add( boostBarWidth );
        boostProps.add( boostBarHeight );
        boostProps.add( displayBoostNumber );
        boostProps.add( boostNumberBackgroundImageName );
        boostProps.add( boostNumberPosX );
        boostProps.add( boostNumberPosY );
        boostProps.add( boostNumberFont );
        boostProps.add( boostNumberFontColor );
        
        props.add( boostProps );
        
        props.add( rpmPosY );
        
        propsList.add( props );
    }
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    public RevMeterWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.163125f, Size.PERCENT_OFFSET + 0.2175f );
        
        getBackgroundColorProperty().setColor( (String)null );
    }
}
