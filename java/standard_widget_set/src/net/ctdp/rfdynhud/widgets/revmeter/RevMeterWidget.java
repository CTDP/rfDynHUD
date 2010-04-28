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
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link RevMeterWidget} displays rev/RPM information.
 * 
 * @author Marvin Froehlich
 */
public class RevMeterWidget extends Widget
{
    public static final String DEFAULT_GEAR_FONT_NAME = "GearFont";
    
    private final ImageProperty backgroundImageName = new ImageProperty( this, "backgroundImageName", "default_rev_meter_bg.png" )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
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
            
            forceAndSetDirty();
        }
    };
    private final ImageProperty needleImageName = new ImageProperty( this, "needleImageName", "imageName", "default_rev_meter_needle.png" )
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
    
    private float backgroundScaleX, backgroundScaleY;
    
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
    
    private ShiftLight[] shiftLights = { new ShiftLight( this, 1 ) };
    
    private void initShiftLights( int oldNumber, int newNumber )
    {
        if ( newNumber > oldNumber )
        {
            ShiftLight[] newArray = new ShiftLight[ newNumber ];
            
            System.arraycopy( shiftLights, 0, newArray, 0, oldNumber );
            
            for ( int i = oldNumber; i < newNumber; i++ )
                newArray[i] = new ShiftLight( RevMeterWidget.this, i + 1 );
            
            shiftLights = newArray;
        }
        
        if ( ( oldNumber < 1 ) && ( newNumber == 1 ) )
            shiftLights[0].activationRPM.setValue( -500 );
    }
    
    private final IntProperty numShiftLights = new IntProperty( this, "numShiftLights", 1, 0, 5 )
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
    private final IntProperty rpmPosX1 = new IntProperty( this, "rpmPosX1", "rpmPosX", 170 );
    private final IntProperty rpmPosY1 = new IntProperty( this, "rpmPosY1", "rpmPosY", 603 );
    private final FontProperty rpmFont1 = new FontProperty( this, "rpmFont1", "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty rpmFontColor1 = new ColorProperty( this, "rpmFontColor1", "fontColor", "#C0BC3D" );
    private final StringProperty rpmJoinString1 = new StringProperty( this, "rpmJoinString1", "rpmJoinString", " / " );
    
    private final BooleanProperty displayRPMString2 = new BooleanProperty( this, "displayRPMString2", "displayRPMString", false );
    private final BooleanProperty displayCurrRPM2 = new BooleanProperty( this, "displayCurrRPM2", "displayCurrRPM", true );
    private final BooleanProperty displayMaxRPM2 = new BooleanProperty( this, "displayMaxRPM2", "displayMaxRPM", true );
    private final IntProperty rpmPosX2 = new IntProperty( this, "rpmPosX2", "rpmPosX", 170 );
    private final IntProperty rpmPosY2 = new IntProperty( this, "rpmPosY2", "rpmPosY", 603 );
    private final FontProperty rpmFont2 = new FontProperty( this, "rpmFont2", "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty rpmFontColor2 = new ColorProperty( this, "rpmFontColor2", "fontColor", "#C0BC3D" );
    private final StringProperty rpmJoinString2 = new StringProperty( this, "rpmJoinString2", "rpmJoinString", " / " );
    
    private DrawnString rpmString1 = null;
    private DrawnString rpmString2 = null;
    private DrawnString gearString = null;
    private DrawnString boostString = null;
    private DrawnString velocityString = null;
    
    private final IntValue gear = new IntValue();
    private final IntValue boost = new IntValue();
    private final IntValue velocity = new IntValue();
    
    @Override
    public String getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        String result = super.getDefaultNamedColorValue( name );
        
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
    public Object createLocalStore()
    {
        return ( new LocalStore() );
    }
    
    private void fixSmallStep()
    {
        this.revMarkersSmallStep.setIntValue( revMarkersBigStep.getIntValue() / Math.round( (float)revMarkersBigStep.getIntValue() / (float)revMarkersSmallStep.getIntValue() ) );
    }
    
    private int loadNeedleTexture( boolean isEditorMode )
    {
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
        
        if ( !gearBackgroundImageName.getStringValue().equals( "" ) )
            n += loadGearBackgroundTexture( isEditorMode );
        else
            gearBackgroundTexture = null;
        
        if ( !boostNumberBackgroundImageName.getStringValue().equals( "" ) )
            n += loadBoostNumberBackgroundTexture( isEditorMode );
        else
            boostNumberBackgroundTexture = null;
        
        if ( !velocityBackgroundImageName.getStringValue().equals( "" ) )
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
    public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        gear.reset();
        velocity.reset();
        
        LocalStore store = (LocalStore)getLocalStore();
        store.storedBaseMaxRPM = 1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        final Texture2DCanvas texCanvas = texture.getTextureCanvas();
        boolean reloadBackground = ( backgroundTexture == null );
        
        if ( isEditorMode && ( backgroundTexture != null ) && ( ( backgroundTexture.getWidth() != width ) || ( backgroundTexture.getHeight() != height ) ) )
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
        }
        
        loadNeedleTexture( isEditorMode );
        
        ImageTemplate it = backgroundImageName.getImage();
        backgroundScaleX = (float)width / (float)it.getBaseWidth();
        backgroundScaleY = (float)height / (float)it.getBaseHeight();
        
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            shiftLights[s].loadTextures( isEditorMode, backgroundImageName );
        
        needleTexture.setTranslation( (int)( ( width - needleTexture.getWidth() ) / 2 ), (int)( height / 2 - needleTexture.getHeight() + needleAxisBottomOffset.getIntValue() * backgroundScaleX ) );
        needleTexture.setRotationCenter( (int)( needleTexture.getWidth() / 2 ), (int)( needleTexture.getHeight() - needleAxisBottomOffset.getIntValue() * backgroundScaleX ) );
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
        
        gearString = new DrawnString( fx - (int)( fw / 2.0 ), fy - (int)( metrics.getDescent() + fh / 2.0 ), Alignment.LEFT, false, gearFont.getFont(), gearFont.isAntiAliased(), gearFontColor.getColor() );
        
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
        
        boostString = new DrawnString( fx - (int)( fw / 2.0 ), fy - (int)( metrics.getDescent() + fh / 2.0 ), Alignment.LEFT, false, boostNumberFont.getFont(), boostNumberFont.isAntiAliased(), boostNumberFontColor.getColor() );
        
        metrics = velocityFont.getMetrics();
        bounds = metrics.getStringBounds( "000", texCanvas );
        fw = bounds.getWidth();
        fh = metrics.getAscent() - metrics.getDescent();
        
        if ( !velocityBackgroundImageName.getStringValue().equals( "" ) )
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
        
        velocityString = new DrawnString( fx/* - (int)( fw / 2.0 )*/, fy - (int)( metrics.getDescent() + fh / 2.0 ), Alignment.LEFT, false, velocityFont.getFont(), velocityFont.isAntiAliased(), velocityFontColor.getColor() );
        
        if ( displayRPMString1.getBooleanValue() )
            rpmString1 = new DrawnString( width - Math.round( rpmPosX1.getIntValue() * backgroundScaleX ), Math.round( rpmPosY1.getIntValue() * backgroundScaleY ), Alignment.RIGHT, false, rpmFont1.getFont(), rpmFont1.isAntiAliased(), rpmFontColor1.getColor() );
        else
            rpmString1 = null;
        
        if ( displayRPMString2.getBooleanValue() )
            rpmString2 = new DrawnString( width - Math.round( rpmPosX2.getIntValue() * backgroundScaleX ), Math.round( rpmPosY2.getIntValue() * backgroundScaleY ), Alignment.RIGHT, false, rpmFont2.getFont(), rpmFont2.isAntiAliased(), rpmFontColor2.getColor() );
        else
            rpmString2 = null;
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
        texture.clear( backgroundTexture, offsetX, offsetY, width, height, true, null );
        
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            shiftLights[s].loadTextures( editorPresets != null, backgroundImageName );
        
        drawMarks( gameData, texture.getTextureCanvas(), offsetX, offsetY, width, height );
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
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        TelemetryData telemData = gameData.getTelemetryData();
        
        gear.update( telemData.getCurrentGear() );
        if ( needsCompleteRedraw || gear.hasChanged() )
        {
            String string = gear.getValue() == -1 ? "R" : gear.getValue() == 0 ? "N" : String.valueOf( gear );
            
            if ( gearBackgroundTexture == null )
                gearString.draw( offsetX, offsetY, string, backgroundTexture, offsetX, offsetY, texture );
            else
                gearString.draw( 0, 0, string, gearBackgroundTexture_bak, 0, 0, gearBackgroundTexture.getTexture() );
        }
        
        boost.update( telemData.getEffectiveEngineBoostMapping() );
        if ( needsCompleteRedraw || boost.hasChanged() )
        {
            if ( displayBoostNumber.getBooleanValue() )
            {
                if ( boostNumberBackgroundTexture == null )
                    boostString.draw( offsetX, offsetY, boost.getValueAsString(), backgroundTexture, offsetX, offsetY, texture );
                else
                    boostString.draw( 0, 0, boost.getValueAsString(), boostNumberBackgroundTexture_bak, 0, 0, boostNumberBackgroundTexture.getTexture() );
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
            velocity.update( Math.round( telemData.getScalarVelocityKPH() ) );
            if ( needsCompleteRedraw || ( clock1 && velocity.hasChanged( false ) ) )
            {
                velocity.setUnchanged();
                
                String string = velocity.getValueAsString();
                
                FontMetrics metrics = velocityFont.getMetrics();
                Rectangle2D bounds = metrics.getStringBounds( string, texture.getTextureCanvas() );
                double fw = bounds.getWidth();
                
                if ( velocityBackgroundTexture == null )
                    velocityString.draw( offsetX - (int)( fw / 2.0 ), offsetY, string, backgroundTexture, offsetX, offsetY, texture );
                else
                    velocityString.draw( (int)( -fw / 2.0 ), 0, string, velocityBackgroundTexture_bak, 0, 0, velocityBackgroundTexture.getTexture() );
            }
        }
        
        LocalStore store = (LocalStore)getLocalStore();
        
        float rpm = isEditorMode ? editorPresets.getEngineRPM() : telemData.getEngineRPM();
        //if ( gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleControl() == VehicleControl.LOCAL_PLAYER )
        float maxRPM = telemData.getEngineMaxRPM();
        if ( maxRPM > 100f )
            store.storedBaseMaxRPM = Math.max( maxRPM, store.storedBaseMaxRPM );
        maxRPM = gameData.getPhysics().getEngine().getMaxRPM( store.storedBaseMaxRPM );
        
        if ( displayRPMString1.getBooleanValue() && ( needsCompleteRedraw || clock1 ) )
        {
            String string = "";
            if ( displayCurrRPM1.getBooleanValue() )
                string = NumberUtil.formatFloat( rpm, 0, false );
            if ( displayCurrRPM1.getBooleanValue() && displayMaxRPM1.getBooleanValue() )
                string += rpmJoinString1.getStringValue();
            if ( displayMaxRPM1.getBooleanValue() )
                string += NumberUtil.formatFloat( maxRPM, 0, false );
            rpmString1.draw( offsetX, offsetY, string, backgroundTexture, offsetX, offsetY, texture );
        }
        
        if ( displayRPMString2.getBooleanValue() && ( needsCompleteRedraw || clock1 ) )
        {
            String string = "";
            if ( displayCurrRPM2.getBooleanValue() )
                string = NumberUtil.formatFloat( rpm, 0, false );
            if ( displayCurrRPM2.getBooleanValue() && displayMaxRPM2.getBooleanValue() )
                string += rpmJoinString2.getStringValue();
            if ( displayMaxRPM2.getBooleanValue() )
                string += NumberUtil.formatFloat( maxRPM, 0, false );
            rpmString2.draw( offsetX, offsetY, string, backgroundTexture, offsetX, offsetY, texture );
        }
        
        for ( int s = 0; s < numShiftLights.getIntValue(); s++ )
            shiftLights[s].updateTextures( gameData, store.storedBaseMaxRPM, rpm, boost.getValue(), backgroundScaleX, backgroundScaleY );
        
        if ( needleTexture != null )
        {
            float rot0 = needleRotationForZeroRPM.getFloatValue();
            float rot = -( rpm / maxRPM ) * ( needleRotationForZeroRPM.getFloatValue() - needleRotationForMaxRPM.getFloatValue() );
            
            needleTexture.setRotation( -rot0 - rot );
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
        
        writer.writeProperty( backgroundImageName, "The name of the background image." );
        writer.writeProperty( needleImageName, "The name of the needle image." );
        writer.writeProperty( needleAxisBottomOffset, "The offset in (unscaled) pixels from the bottom of the image, where the center of the needle's axis is." );
        writer.writeProperty( needleRotationForZeroRPM, "The rotation for the needle image, that is has for zero RPM (in degrees)." );
        writer.writeProperty( needleRotationForMaxRPM, "The rotation for the needle image, that is has for maximum RPM (in degrees)." );
        writer.writeProperty( displayRevMarkers, "Display rev markers?" );
        writer.writeProperty( displayRevMarkerNumbers, "Display rev marker numbers?" );
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
        writer.writeProperty( rpmPosX1, "The offset in (background image space) pixels from the right of the Widget, where the text is to be placed." );
        writer.writeProperty( rpmPosY1, "The offset in (background image space) pixels from the top of the Widget, where the text is to be placed." );
        writer.writeProperty( rpmFont1, "The font used to draw the RPM." );
        writer.writeProperty( rpmFontColor1, "The font color used to draw the RPM." );
        writer.writeProperty( rpmJoinString1, "The String to use to join the current and max RPM." );
        writer.writeProperty( displayRPMString2, "whether to display the digital RPM/Revs string or not" );
        writer.writeProperty( displayCurrRPM2, "whether to display the current revs or to hide them" );
        writer.writeProperty( displayMaxRPM2, "whether to display the maximum revs or to hide them" );
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
        
        if ( backgroundImageName.loadProperty( key, value ) );
        else if ( needleImageName.loadProperty( key, value ) );
        else if ( needleAxisBottomOffset.loadProperty( key, value ) );
        else if ( needleRotationForZeroRPM.loadProperty( key, value ) );
        else if ( needleRotationForMaxRPM.loadProperty( key, value ) );
        else if ( numShiftLights.loadProperty( key, value ) )
        {
            this.shiftLights = new ShiftLight[ numShiftLights.getIntValue() ];
            for ( int i = 0; i < numShiftLights.getIntValue(); i++ )
                shiftLights[i] = new ShiftLight( this, i + 1 );
        }
        else if ( loadShiftLightProperty( key, value ) );
        else if ( displayRevMarkers.loadProperty( key, value ) );
        else if ( displayRevMarkerNumbers.loadProperty( key, value ) );
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
        else if ( rpmPosX1.loadProperty( key, value ) );
        else if ( rpmPosY1.loadProperty( key, value ) );
        else if ( rpmFont1.loadProperty( key, value ) );
        else if ( rpmFontColor1.loadProperty( key, value ) );
        else if ( rpmJoinString1.loadProperty( key, value ) );
        else if ( displayRPMString2.loadProperty( key, value ) );
        else if ( displayCurrRPM2.loadProperty( key, value ) );
        else if ( displayMaxRPM2.loadProperty( key, value ) );
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
        
        propsCont.addProperty( backgroundImageName );
        
        propsCont.addGroup( "Needle" );
        
        propsCont.addProperty( needleImageName );
        propsCont.addProperty( needleAxisBottomOffset );
        propsCont.addProperty( needleRotationForZeroRPM );
        propsCont.addProperty( needleRotationForMaxRPM );
        
        propsCont.addGroup( "Rev Markers" );
        
        propsCont.addProperty( displayRevMarkers );
        propsCont.addProperty( displayRevMarkerNumbers );
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
        propsCont.addProperty( rpmPosX1 );
        propsCont.addProperty( rpmPosY1 );
        propsCont.addProperty( rpmFont1 );
        propsCont.addProperty( rpmFontColor1 );
        propsCont.addProperty( rpmJoinString1 );
        
        propsCont.addGroup( "DigitalRevs2" );
        
        propsCont.addProperty( displayRPMString2 );
        propsCont.addProperty( displayCurrRPM2 );
        propsCont.addProperty( displayMaxRPM2 );
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
        super( name, Size.PERCENT_OFFSET + 0.163125f, Size.PERCENT_OFFSET + 0.2175f );
        
        getBackgroundColorProperty().setColor( (String)null );
        
        initShiftLights( 0, 1 );
    }
}
