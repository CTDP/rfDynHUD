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
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.FontProperty;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
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
import net.ctdp.rfdynhud.util.TextureLoader;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.IntValue;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.openmali.vecmath2.util.ColorUtils;

/**
 * The {@link RevMeterWidget} displays rev/RPM information.
 * 
 * @author Marvin Froehlich
 */
public class RevMeterWidget extends Widget
{
    private String backgroundImageName = "revmeter.png";
    private String needleImageName = "needle.png";
    private String shiftLightImageName = "shiftlight_on.png";
    
    private TextureImage2D backgroundTexture = null;
    private TransformableTexture needleTexture = null;
    private TransformableTexture shiftLightTexture = null;
    
    private float backgroundScaleX, backgroundScaleY;
    
    private int needleAxisBottomOffset = 12;
    
    private float needleRotationForZeroRPM = (float)Math.PI * 0.68f;
    private float needleRotationForMaxRPM = -(float)Math.PI * 0.66f;
    
    private int editorRPM = 3750;
    
    private boolean displayRevMarkers = true;
    private boolean displayRevMarkerNumbers = true;
    private int revMarkersInnerRadius = 224;
    private int revMarkersLength = 50;
    private int revMarkersBigStep = 1000;
    private int revMarkersSmallStep = 200;
    private String revMarkersColorKey = "#FFFFFF";
    private Color revMarkersColor = null;
    private String revMarkersMediumColorKey = "#FFFF00";
    private Color revMarkersMediumColor = null;
    private String revMarkersHighColorKey = "#FF0000";
    private Color revMarkersHighColor = null;
    private final FontProperty revMarkersFont = new FontProperty( this, "revMarkersFont", "rmFont", "Monospaced-BOLD-9va" );
    private String revMarkersFontColorKey = "#FFFFFF";
    private Color revMarkersFontColor = null;
    private boolean interpolateMarkerColors = false;
    
    private boolean displayShiftLight = true;
    private int shiftLightPosX = 625;
    private int shiftLightPosY = 42;
    private int shiftLightRPM = -500;
    
    private int gearPosX = 325;
    private int gearPosY = 450;
    
    private final FontProperty gearFont = new FontProperty( this, "gearFont", "GearFont" );
    private String gearFontColorKey = "#C0BC3D";
    private Color gearFontColor = null;
    
    private int rpmPosY = 603;
    
    private DrawnString rpmString = null;
    private DrawnString gearString = null;
    private DrawnString boostString = null;
    
    private final IntValue gear = new IntValue();
    private final IntValue boost = new IntValue();
    
    private boolean displayBoostBar = true;
    
    private boolean displayBoostNumber = true;
    
    private int boostBarPosX = 135;
    private int boostBarPosY = 560;
    private int boostBarWidth = 438;
    private int boostBarHeight = 35;
    private int boostNumberPosX = 377;
    private int boostNumberPosY = 510;
    
    private final FontProperty boostNumberFont = new FontProperty( this, "boostNumberFont", "StandardFont" );
    
    private String boostNumberFontColorKey = "#FF0000";
    private Color boostNumberFontColor = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object createLocalStore()
    {
        return ( new LocalStore() );
    }
    
    public void setBackgroundImageName( String imageName )
    {
        this.backgroundImageName = imageName;
        
        this.backgroundTexture = null;
        
        forceAndSetDirty();
    }
    
    public final String getBackgroundImageName()
    {
        return ( backgroundImageName );
    }
    
    public void setNeedleImageName( String imageName )
    {
        this.needleImageName = imageName;
        
        this.needleTexture = null;
        
        forceAndSetDirty();
    }
    
    public final String getNeedleImageName()
    {
        return ( needleImageName );
    }
    
    public void setNeedleAxisBottomOffset( int offset )
    {
        this.needleAxisBottomOffset = offset;
        
        forceAndSetDirty();
    }
    
    public final int getNeedleAxisBottomOffset()
    {
        return ( needleAxisBottomOffset );
    }
    
    public void setNeedleRotationForZeroRPM( float radians )
    {
        this.needleRotationForZeroRPM = radians;
        
        forceAndSetDirty();
    }
    
    public final void setNeedleRotationForZeroRPMasDegrees( float degrees )
    {
        setNeedleRotationForZeroRPM( degrees * (float)Math.PI / 180f );
    }
    
    public final float getNeedleRotationForZeroRPM()
    {
        return ( needleRotationForZeroRPM );
    }
    
    public final float getNeedleRotationForZeroRPMasDegrees()
    {
        return( needleRotationForZeroRPM * 180f / (float)Math.PI );
    }
    
    public void setNeedleRotationForMaxRPM( float radians )
    {
        this.needleRotationForMaxRPM = radians;
        
        forceAndSetDirty();
    }
    
    public final void setNeedleRotationForMaxRPMasDegrees( float degrees )
    {
        setNeedleRotationForMaxRPM( degrees * (float)Math.PI / 180f );
    }
    
    public final float getNeedleRotationForMaxRPM()
    {
        return ( needleRotationForMaxRPM );
    }
    
    public final float getNeedleRotationForMaxRPMasDegrees()
    {
        return( needleRotationForMaxRPM * 180f / (float)Math.PI );
    }
    
    public void setEditorRPM( int rpm )
    {
        this.editorRPM = rpm;
        
        forceAndSetDirty();
    }
    
    public final int getEditorRPM()
    {
        return ( editorRPM );
    }
    
    public void setDisplayRevMarkers( boolean display )
    {
        this.displayRevMarkers = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayRevMarkers()
    {
        return ( displayRevMarkers );
    }
    
    public void setDisplayRevMarkerNumbers( boolean display )
    {
        this.displayRevMarkerNumbers = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayRevMarkerNumbers()
    {
        return ( displayRevMarkerNumbers );
    }
    
    public void setRevMarkersInnerRadius( int radius )
    {
        this.revMarkersInnerRadius = Math.max( 10, radius );
        
        forceAndSetDirty();
    }
    
    public final int getRevMarkersInnerRadius()
    {
        return ( revMarkersInnerRadius );
    }
    
    public void setRevMarkersLength( int length )
    {
        this.revMarkersLength = Math.max( 4, length );
        
        forceAndSetDirty();
    }
    
    public final int getRevMarkersLength()
    {
        return ( revMarkersLength );
    }
    
    private void fixSmallStep()
    {
        this.revMarkersSmallStep = revMarkersBigStep / Math.round( (float)revMarkersBigStep / (float)revMarkersSmallStep );
    }
    
    public void setRevMarkersBigStep( int step )
    {
        this.revMarkersBigStep = Math.max( 300, step );
        
        fixSmallStep();
        
        forceAndSetDirty();
    }
    
    public final int getRevMarkersBigStep()
    {
        return ( revMarkersBigStep );
    }
    
    public void setRevMarkersSmallStep( int step )
    {
        this.revMarkersSmallStep = Math.max( 20, step );
        
        fixSmallStep();
        
        forceAndSetDirty();
    }
    
    public final int getRevMarkersSmallStep()
    {
        return ( revMarkersSmallStep );
    }
    
    public void setRevMarkersColor( String color )
    {
        this.revMarkersColorKey = color;
        this.revMarkersColor = null;
        
        forceAndSetDirty();
    }
    
    public final void setRevMarkersColor( Color color )
    {
        setRevMarkersColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setRevMarkersColor( int red, int green, int blue )
    {
        setRevMarkersColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getRevMarkersColor()
    {
        revMarkersColor = ColorProperty.getColorFromColorKey( revMarkersColorKey, revMarkersColor, getConfiguration() );
        
        return ( revMarkersColor );
    }
    
    public void setRevMarkersMediumColor( String color )
    {
        this.revMarkersMediumColorKey = color;
        this.revMarkersMediumColor = null;
        
        forceAndSetDirty();
    }
    
    public final void setRevMarkersMediumColor( Color color )
    {
        setRevMarkersMediumColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setRevMarkersMediumColor( int red, int green, int blue )
    {
        setRevMarkersMediumColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getRevMarkersMediumColor()
    {
        revMarkersMediumColor = ColorProperty.getColorFromColorKey( revMarkersMediumColorKey, revMarkersMediumColor, getConfiguration() );
        
        return ( revMarkersMediumColor );
    }
    
    public void setRevMarkersHighColor( String color )
    {
        this.revMarkersHighColorKey = color;
        this.revMarkersHighColor = null;
        
        forceAndSetDirty();
    }
    
    public final void setRevMarkersHighColor( Color color )
    {
        setRevMarkersHighColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setRevMarkersHighColor( int red, int green, int blue )
    {
        setRevMarkersHighColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getRevMarkersHighColor()
    {
        revMarkersHighColor = ColorProperty.getColorFromColorKey( revMarkersHighColorKey, revMarkersHighColor, getConfiguration() );
        
        return ( revMarkersHighColor );
    }
    
    public final java.awt.Font getRevMarkersFont()
    {
        return ( revMarkersFont.getFont() );
    }
    
    public final boolean isRevMarkersFontAntialiased()
    {
        return ( revMarkersFont.isAntiAliased() );
    }
    
    public void setRevMarkersFontColor( String color )
    {
        this.revMarkersFontColorKey = color;
        this.revMarkersFontColor = null;
        
        forceAndSetDirty();
    }
    
    public final void setRevMarkersFontColor( Color color )
    {
        setRevMarkersFontColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setRevMarkersFontColor( int red, int green, int blue )
    {
        setBoostNumberFontColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getRevMarkersFontColor()
    {
        revMarkersFontColor = ColorProperty.getColorFromColorKey( revMarkersFontColorKey, revMarkersFontColor, getConfiguration() );
        
        return ( revMarkersFontColor );
    }
    
    public void setDisplayShiftLight( boolean display )
    {
        this.displayShiftLight = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayShiftLight()
    {
        return ( displayShiftLight );
    }
    
    public void setShiftLightImageName( String imageName )
    {
        this.shiftLightImageName = imageName;
        
        this.shiftLightTexture = null;
        
        forceAndSetDirty();
    }
    
    public final String getShiftLightImageName()
    {
        return ( shiftLightImageName );
    }
    
    public void setShiftLightPosX( int x )
    {
        this.shiftLightPosX = x;
        
        forceAndSetDirty();
    }
    
    public final int getShiftLightPosX()
    {
        return ( shiftLightPosX );
    }
    
    public void setShiftLightPosY( int y )
    {
        this.shiftLightPosY = y;
        
        forceAndSetDirty();
    }
    
    public final int getShiftLightPosY()
    {
        return ( shiftLightPosY );
    }
    
    public void setShiftLightRPM( int rpm )
    {
        this.shiftLightRPM = rpm;
        
        forceAndSetDirty();
    }
    
    public final int getShiftLightRPM()
    {
        return ( shiftLightRPM );
    }
    
    public void setGearPosX( int x )
    {
        this.gearPosX = x;
        
        forceAndSetDirty();
    }
    
    public final int getGearPosX()
    {
        return ( gearPosX );
    }
    
    public void setGearPosY( int y )
    {
        this.gearPosY = y;
        
        forceAndSetDirty();
    }
    
    public final int getGearPosY()
    {
        return ( gearPosY );
    }
    
    public final java.awt.Font getGearFont()
    {
        return ( gearFont.getFont() );
    }
    
    public final boolean isGearFontAntialiased()
    {
        return ( gearFont.isAntiAliased() );
    }
    
    public void setGearFontColor( String color )
    {
        this.gearFontColorKey = color;
        this.gearFontColor = null;
        
        forceAndSetDirty();
    }
    
    public final void setGearFontColor( Color color )
    {
        setGearFontColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setGearFontColor( int red, int green, int blue )
    {
        setGearFontColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getGearFontColor()
    {
        gearFontColor = ColorProperty.getColorFromColorKey( gearFontColorKey, gearFontColor, getConfiguration() );
        
        return ( gearFontColor );
    }
    
    public void setRPMPosY( int y )
    {
        this.rpmPosY = y;
        
        forceAndSetDirty();
    }
    
    public final int getRPMPosY()
    {
        return ( rpmPosY );
    }
    
    public void setDisplayBoostBar( boolean display )
    {
        this.displayBoostBar = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayBoostBar()
    {
        return ( displayBoostBar );
    }
    
    public void setBoostBarPosX( int x )
    {
        this.boostBarPosX = x;
        
        forceAndSetDirty();
    }
    
    public final int getBoostBarPosX()
    {
        return ( boostBarPosX );
    }
    
    public void setBoostBarPosY( int y )
    {
        this.boostBarPosY = y;
        
        forceAndSetDirty();
    }
    
    public final int getBoostBarPosY()
    {
        return ( boostBarPosY );
    }
    
    public void setBoostBarWidth( int width )
    {
        this.boostBarWidth = width;
        
        forceAndSetDirty();
    }
    
    public final int getBoostBarWidth()
    {
        return ( boostBarWidth );
    }
    
    public void setBoostBarHeight( int height )
    {
        this.boostBarHeight = height;
        
        forceAndSetDirty();
    }
    
    public final int getBoostBarHeight()
    {
        return ( boostBarHeight );
    }
    
    public void setDisplayBoostNumber( boolean display )
    {
        this.displayBoostNumber = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayBoostNumber()
    {
        return ( displayBoostNumber );
    }
    
    public void setBoostNumberPosX( int x )
    {
        this.boostNumberPosX = x;
        
        forceAndSetDirty();
    }
    
    public final int getBoostNumberPosX()
    {
        return ( boostNumberPosX );
    }
    
    public void setBoostNumberPosY( int y )
    {
        this.boostNumberPosY = y;
        
        forceAndSetDirty();
    }
    
    public final int getBoostNumberPosY()
    {
        return ( boostNumberPosY );
    }
    
    public final java.awt.Font getBoostNumberFont()
    {
        return ( boostNumberFont.getFont() );
    }
    
    public final boolean isBoostNumberFontAntialiased()
    {
        return ( boostNumberFont.isAntiAliased() );
    }
    
    public void setBoostNumberFontColor( String color )
    {
        this.boostNumberFontColorKey = color;
        this.boostNumberFontColor = null;
        
        forceAndSetDirty();
    }
    
    public final void setBoostNumberFontColor( Color color )
    {
        setBoostNumberFontColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setBoostNumberFontColor( int red, int green, int blue )
    {
        setBoostNumberFontColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getBoostNumberFontColor()
    {
        boostNumberFontColor = ColorProperty.getColorFromColorKey( boostNumberFontColorKey, boostNumberFontColor, getConfiguration() );
        
        return ( boostNumberFontColor );
    }
    
    private void loadNeedleTexture( boolean isEditorMode )
    {
        if ( ( needleTexture == null ) || isEditorMode )
        {
            try
            {
                BufferedImage bi = TextureLoader.getImage( backgroundImageName );
                float scale = getSize().getEffectiveWidth() / (float)bi.getWidth();
                bi = TextureLoader.getImage( needleImageName );
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
            }
        }
    }
    
    private void loadShiftLightTexture( boolean isEditorMode )
    {
        if ( ( shiftLightTexture == null ) || isEditorMode )
        {
            try
            {
                BufferedImage bi0 = TextureLoader.getImage( backgroundImageName );
                float scale = getSize().getEffectiveWidth() / (float)bi0.getWidth();
                BufferedImage bi = TextureLoader.getImage( shiftLightImageName );
                int slWidth = (int)( bi.getWidth() * scale );
                int slHeight = (int)( bi.getHeight() * scale );
                if ( ( shiftLightTexture == null ) || ( shiftLightTexture.getWidth() != slWidth ) || ( shiftLightTexture.getHeight() != slHeight * 2 ) )
                {
                    shiftLightTexture = new TransformableTexture( slWidth, slHeight * 2, 0, 0, 0, 0, 0f, 1f, 1f );
                    shiftLightTexture.getTexture().clear( false, null );
                    shiftLightTexture.getTexture().getTextureCanvas().setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
                    shiftLightTexture.getTexture().getTextureCanvas().drawImage( bi, 0, 0, slWidth, slHeight, 0, 0, bi.getWidth(), bi.getHeight() );
                    shiftLightTexture.getTexture().getTextureCanvas().drawImage( bi0, 0, slHeight, slWidth, shiftLightTexture.getHeight(), getShiftLightPosX(), getShiftLightPosY(), getShiftLightPosX() + bi.getWidth(), getShiftLightPosY() + bi.getHeight() );
                }
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    @Override
    public TransformableTexture[] getSubTextures( boolean isEditorMode, int widgetWidth, int widgetHeight )
    {
        loadNeedleTexture( isEditorMode );
        
        if ( getDisplayShiftLight() )
        {
            loadShiftLightTexture( isEditorMode );
            
            return ( new TransformableTexture[] { needleTexture, shiftLightTexture } );
        }
        
        return ( new TransformableTexture[] { needleTexture } );
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
                BufferedImage bi = TextureLoader.getImage( backgroundImageName );
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
        
        BufferedImage bi = TextureLoader.getImage( backgroundImageName );
        backgroundScaleX = (float)width / (float)bi.getWidth();
        backgroundScaleY = (float)height / (float)bi.getHeight();
        
        if ( getDisplayShiftLight() )
        {
            loadShiftLightTexture( isEditorMode );
        }
        
        needleTexture.setTranslation( (int)( ( width - needleTexture.getWidth() ) / 2 ), (int)( height / 2 - needleTexture.getHeight() + needleAxisBottomOffset * backgroundScaleX ) );
        needleTexture.setRotationCenter( (int)( needleTexture.getWidth() / 2 ), (int)( needleTexture.getHeight() - needleAxisBottomOffset * backgroundScaleX ) );
        //needleTexture.setRotation( 0f );
        //needleTexture.setScale( 1f, 1f );
        
        gearString = new DrawnString( Math.round( getGearPosX() * backgroundScaleX ), Math.round( getGearPosY() * backgroundScaleY ), Alignment.LEFT, false, getGearFont(), isGearFontAntialiased(), getGearFontColor(), null );
        
        boostString = new DrawnString( Math.round( getBoostNumberPosX() * backgroundScaleX ), Math.round( getBoostNumberPosY() * backgroundScaleY ), Alignment.LEFT, false, getBoostNumberFont(), isBoostNumberFontAntialiased(), getBoostNumberFontColor(), null );
        
        rpmString = new DrawnString( width / 2, Math.round( getRPMPosY() * backgroundScaleY ), Alignment.CENTER, false, getFont(), isFontAntialiased(), getFontColor(), null );
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
        if ( !getDisplayRevMarkers() )
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
        
        float innerRadius = getRevMarkersInnerRadius() * backgroundScaleX;
        float outerRadius = ( getRevMarkersInnerRadius() + getRevMarkersLength() - 1 ) * backgroundScaleX;
        float outerRadius2 = innerRadius + ( outerRadius - innerRadius ) * 0.75f;
        
        Font numberFont = getRevMarkersFont();
        texCanvas.setFont( numberFont );
        FontMetrics metrics = texCanvas.getFontMetrics( numberFont );
        
        for ( int rpm = 0; rpm <= maxRPM; rpm += getRevMarkersSmallStep() )
        {
            float angle = -( needleRotationForZeroRPM + ( needleRotationForMaxRPM - needleRotationForZeroRPM ) * ( rpm / maxRPM ) );
            
            at2.setToRotation( angle, centerX, centerY );
            texCanvas.setTransform( at2 );
            
            if ( rpm <= lowRPM )
                texCanvas.setColor( getRevMarkersColor() );
            else if ( rpm <= mediumRPM )
                texCanvas.setColor( interpolateMarkerColors ? interpolateColor( getRevMarkersColor(), getRevMarkersMediumColor(), ( rpm - lowRPM ) / ( mediumRPM - lowRPM ) ) : getRevMarkersMediumColor() );
            else
                texCanvas.setColor( interpolateMarkerColors ? interpolateColor( getRevMarkersMediumColor(), getRevMarkersHighColor(), ( rpm - mediumRPM ) / ( maxRPM - mediumRPM ) ) : getRevMarkersHighColor() );
            
            if ( ( rpm % getRevMarkersBigStep() ) == 0 )
            {
                texCanvas.setStroke( thousand );
                texCanvas.drawLine( Math.round( centerX ), Math.round( centerY - innerRadius ), Math.round( centerX ), Math.round( centerY - outerRadius ) );
                //texCanvas.drawLine( Math.round( centerX ), Math.round( ( centerY - innerRadius ) * backgroundScaleY / backgroundScaleX ), Math.round( centerX ), Math.round( ( centerY - outerRadius ) * backgroundScaleY / backgroundScaleX ) );
                
                if ( getDisplayRevMarkerNumbers() )
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
        
        if ( getDisplayShiftLight() )
        {
            loadShiftLightTexture( isEditorMode );
            
            texCanvas.getImage().clear( offsetX + Math.round( getShiftLightPosX() * backgroundScaleX ), offsetY + Math.round( getShiftLightPosY() * backgroundScaleY ), shiftLightTexture.getWidth(), shiftLightTexture.getHeight(), true, null );
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
    protected void drawWidget( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, boolean needsCompleteRedraw )
    {
        TextureImage2D image = texCanvas.getImage();
        TelemetryData telemData = gameData.getTelemetryData();
        
        gear.update( telemData.getCurrentGear() );
        if ( needsCompleteRedraw || gear.hasChanged() )
        {
            String string = gear.getValue() == -1 ? "R" : gear.getValue() == 0 ? "N" : String.valueOf( gear );
            gearString.draw( offsetX, offsetY, string, backgroundTexture, image );
        }
        
        boost.update( telemData.getEngineBoostMapping() );
        if ( needsCompleteRedraw || boost.hasChanged() )
        {
            if ( getDisplayBoostNumber() )
            {
                boostString.draw( offsetX, offsetY, boost.getValueAsString(), backgroundTexture, image );
            }
            
            if ( getDisplayBoostBar() )
            {
                int maxBoost = (int)gameData.getPhysics().getEngine().getBoostRange().getMaxValue();
                boolean inverted = ( gameData.getPhysics().getEngine().getRPMIncreasePerBoostLevel() < 0f );
                boolean tempBoost = false;
                drawBoostBar( boost.getValue(), maxBoost, inverted, tempBoost, texCanvas, offsetX + Math.round( getBoostBarPosX() * backgroundScaleX ), offsetY + Math.round( getBoostBarPosY() * backgroundScaleY ), Math.round( getBoostBarWidth() * backgroundScaleX ), Math.round( getBoostBarHeight() * backgroundScaleY ) );
            }
        }
        
        LocalStore store = (LocalStore)getLocalStore();
        
        float rpm = isEditorMode ? editorRPM : telemData.getEngineRPM();
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
        
        if ( getDisplayShiftLight() )
        {
            float maxRPM_boost = gameData.getPhysics().getEngine().getMaxRPM( store.storedBaseMaxRPM, boost.getValue() );
            if ( rpm >= maxRPM_boost + getShiftLightRPM() )
            {
                shiftLightTexture.setClipRect( 0, 0, shiftLightTexture.getWidth(), shiftLightTexture.getHeight() / 2, true );
                shiftLightTexture.setTranslation( Math.round( getShiftLightPosX() * backgroundScaleX ), Math.round( getShiftLightPosY() * backgroundScaleY ) );
            }
            else
            {
                shiftLightTexture.setClipRect( 0, shiftLightTexture.getHeight() / 2, shiftLightTexture.getWidth(), shiftLightTexture.getHeight() / 2, true );
                shiftLightTexture.setTranslation( Math.round( getShiftLightPosX() * backgroundScaleX ), Math.round( getShiftLightPosY() * backgroundScaleY ) - shiftLightTexture.getHeight() / 2 );
            }
        }
        
        float rot0 = needleRotationForZeroRPM;
        float rot = -( rpm / maxRPM ) * ( needleRotationForZeroRPM - needleRotationForMaxRPM );
        
        needleTexture.setRotation( -rot0 - rot );
        
        if ( isEditorMode )
        {
            needleTexture.drawInEditor( texCanvas, offsetX, offsetY );
            
            if ( getDisplayShiftLight() )
            {
                texCanvas.getImage().clear( offsetX + Math.round( getShiftLightPosX() * backgroundScaleX ), offsetX + Math.round( getShiftLightPosY() * backgroundScaleY ), shiftLightTexture.getWidth(), shiftLightTexture.getHeight(), true, null );
                shiftLightTexture.drawInEditor( texCanvas, offsetX, offsetY );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( "backgroundImageName", getBackgroundImageName(), "The name of the background image." );
        writer.writeProperty( "needleImageName", getNeedleImageName(), "The name of the needle image." );
        writer.writeProperty( "needleAxisBottomOffset", getNeedleAxisBottomOffset(), "The offset in (unscaled) pixels from the bottom of the image, where the center of the needle's axis is." );
        writer.writeProperty( "needleRotationForZeroRPM", getNeedleRotationForZeroRPMasDegrees(), "The rotation for the needle image, that is has for zero RPM (in degrees)." );
        writer.writeProperty( "needleRotationForMaxRPM", getNeedleRotationForMaxRPMasDegrees(), "The rotation for the needle image, that is has for maximum RPM (in degrees)." );
        writer.writeProperty( "editorRPM", getEditorRPM(), "The RPM (rounds per minute) displayed in the editor (not in rFactor)" );
        writer.writeProperty( "displayRevMarkers", getDisplayRevMarkers(), "Display rev markers?" );
        writer.writeProperty( "displayRevMarkerNumbers", getDisplayRevMarkerNumbers(), "Display rev marker numbers?" );
        writer.writeProperty( "revMarkersInnerRadius", getRevMarkersInnerRadius(), "The inner radius of the rev markers (in background image space)" );
        writer.writeProperty( "revMarkersLength", getRevMarkersLength(), "The length of the rev markers (in background image space)" );
        writer.writeProperty( "revMarkersBigStep", getRevMarkersBigStep(), "Step size of bigger rev markers" );
        writer.writeProperty( "revMarkersSmallStep", getRevMarkersSmallStep(), "Step size of smaller rev markers" );
        writer.writeProperty( "revMarkersColor", revMarkersColorKey, "The color used to draw the rev markers." );
        writer.writeProperty( "revMarkersMediumColor", revMarkersMediumColorKey, "The color used to draw the rev markers for medium boost." );
        writer.writeProperty( "revMarkersHighColor", revMarkersHighColorKey, "The color used to draw the rev markers for high revs." );
        writer.writeProperty( revMarkersFont.getPropertyName(), revMarkersFont.getFontKey(), "The font used to draw the rev marker numbers." );
        writer.writeProperty( "revMarkersFontColor", revMarkersFontColorKey, "The font color used to draw the rev marker numbers." );
        writer.writeProperty( "displayShiftLight", getDisplayShiftLight(), "Display a shift light?" );
        writer.writeProperty( "shiftLightImageName", getShiftLightImageName(), "The name of the shift light image." );
        writer.writeProperty( "shiftLightPosX", getShiftLightPosX(), "The x-offset in pixels to the gear label." );
        writer.writeProperty( "shiftLightPosY", getShiftLightPosY(), "The y-offset in pixels to the gear label." );
        writer.writeProperty( "shiftLightRPM", getShiftLightRPM(), "The RPM (rounds per minute) to subtract from the maximum for the level to display shoft light on" );
        writer.writeProperty( "gearPosX", getGearPosX(), "The x-offset in pixels to the gear label." );
        writer.writeProperty( "gearPosY", getGearPosY(), "The y-offset in pixels to the gear label." );
        writer.writeProperty( gearFont.getPropertyName(), gearFont.getFontKey(), "The font used to draw the gear." );
        writer.writeProperty( "gearFontColor", gearFontColorKey, "The font color used to draw the gear." );
        writer.writeProperty( "displayBoostBar", getDisplayBoostBar(), "Display a graphical bar for engine boost mapping?" );
        writer.writeProperty( "boostBarPosX", getBoostBarPosX(), "The x-position of the boost bar." );
        writer.writeProperty( "boostBarPosY", getBoostBarPosY(), "The y-position of the boost bar." );
        writer.writeProperty( "boostBarWidth", getBoostBarWidth(), "The width of the boost bar." );
        writer.writeProperty( "boostBarHeight", getBoostBarHeight(), "The height of the boost bar." );
        writer.writeProperty( "displayBoostNumber", getDisplayBoostNumber(), "Display a number for engine boost mapping?" );
        writer.writeProperty( "boostNumberPosX", getBoostNumberPosX(), "The x-position of the boost number." );
        writer.writeProperty( "boostNumberPosY", getBoostNumberPosY(), "The y-position of the boost number." );
        writer.writeProperty( boostNumberFont.getPropertyName(), boostNumberFont.getFontKey(), "The font used to draw the boost number." );
        writer.writeProperty( "boostNumberFontColor", boostNumberFontColorKey, "The font color used to draw the boost bar." );
        writer.writeProperty( "rpmPosY", getRPMPosY(), "The offset in (background image space) pixels from the top of the Widget, where the text is to be placed." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( key.equals( "backgroundImageName" ) )
            this.backgroundImageName = value;
        
        else if ( key.equals( "needleImageName" ) )
            this.needleImageName = value;
        
        else if ( key.equals( "needleAxisBottomOffset" ) )
            this.needleAxisBottomOffset = Integer.parseInt( value );
        
        else if ( key.equals( "needleRotationForZeroRPM" ) )
            this.setNeedleRotationForZeroRPMasDegrees( Float.parseFloat( value ) );
        
        else if ( key.equals( "needleRotationForMaxRPM" ) )
            this.setNeedleRotationForMaxRPMasDegrees( Float.parseFloat( value ) );
        
        else if ( key.equals( "editorRPM" ) )
            this.editorRPM = Integer.parseInt( value );
        
        else if ( key.equals( "displayShiftLight" ) )
            this.displayShiftLight = Boolean.parseBoolean( value );
        
        else if ( key.equals( "shiftLightImageName" ) )
            this.shiftLightImageName = value;
        
        else if ( key.equals( "shiftLightPosX" ) )
            this.gearPosX = Integer.parseInt( value );
        
        else if ( key.equals( "shiftLightPosY" ) )
            this.gearPosY = Integer.parseInt( value );
        
        else if ( key.equals( "shiftLightRPM" ) )
            this.shiftLightRPM = Integer.parseInt( value );
        
        else if ( key.equals( "displayRevMarkers" ) )
            this.displayRevMarkers = Boolean.parseBoolean( value );
        
        else if ( key.equals( "displayRevMarkerNumbers" ) )
            this.displayRevMarkerNumbers = Boolean.parseBoolean( value );
        
        else if ( key.equals( "revMarkersInnerRadius" ) )
            this.revMarkersInnerRadius = Integer.parseInt( value );
        
        else if ( key.equals( "revMarkersLength" ) )
            this.revMarkersLength = Integer.parseInt( value );
        
        else if ( key.equals( "revMarkersBigStep" ) )
            this.revMarkersBigStep = Integer.parseInt( value );
        
        else if ( key.equals( "revMarkersSmallStep" ) )
            this.revMarkersSmallStep = Integer.parseInt( value );
        
        else if ( key.equals( "revMarkersColor" ) )
            this.revMarkersColorKey = value;
        
        else if ( key.equals( "revMarkersMediumColor" ) )
            this.revMarkersMediumColorKey = value;
        
        else if ( key.equals( "revMarkersHighColor" ) )
            this.revMarkersHighColorKey = value;
        
        else if ( revMarkersFont.loadProperty( key, value ) )
            ;
        
        else if ( key.equals( "revMarkersFontColor" ) )
            this.revMarkersFontColorKey = value;
        
        else if ( key.equals( "gearPosX" ) )
            this.gearPosX = Integer.parseInt( value );
        
        else if ( key.equals( "gearPosY" ) )
            this.gearPosY = Integer.parseInt( value );
        
        else if ( gearFont.loadProperty( key, value ) )
            ;
        
        else if ( key.equals( "gearFontColor" ) )
            this.gearFontColorKey = value;
        
        else if ( key.equals( "rpmPosY" ) )
            this.rpmPosY = Integer.parseInt( value );
        
        else if ( key.equals( "displayBoostBar" ) )
            this.displayBoostBar = Boolean.parseBoolean( value );
        
        else if ( key.equals( "boostBarPosX" ) )
            this.boostBarPosX = Integer.parseInt( value );
        
        else if ( key.equals( "boostBarPosY" ) )
            this.boostBarPosY = Integer.parseInt( value );
        
        else if ( key.equals( "boostBarWidth" ) )
            this.boostBarWidth = Integer.parseInt( value );
        
        else if ( key.equals( "boostBarHeight" ) )
            this.boostBarHeight = Integer.parseInt( value );
        
        else if ( key.equals( "displayBoostNumber" ) )
            this.displayBoostNumber = Boolean.parseBoolean( value );
        
        else if ( key.equals( "boostNumberPosX" ) )
            this.boostNumberPosX = Integer.parseInt( value );
        
        else if ( key.equals( "boostNumberPosY" ) )
            this.boostNumberPosY = Integer.parseInt( value );
        
        else if ( boostNumberFont.loadProperty( key, value ) )
            ;
        
        else if ( key.equals( "boostNumberFontColor" ) )
            this.boostNumberFontColorKey = value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( FlaggedList propsList )
    {
        super.getProperties( propsList );
        
        FlaggedList props = new FlaggedList( "Specific", true );
        
        props.add( new Property( "backgroundImageName", PropertyEditorType.IMAGE )
        {
            @Override
            public void setValue( Object value )
            {
                setBackgroundImageName( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( getBackgroundImageName() );
            }
        } );
        
        FlaggedList needleProps = new FlaggedList( "Needle", true );
        
        needleProps.add( new Property( "imageName", PropertyEditorType.IMAGE )
        {
            @Override
            public void setValue( Object value )
            {
                setNeedleImageName( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( getNeedleImageName() );
            }
        } );
        
        needleProps.add( new Property( "axisBottomOffset", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setNeedleAxisBottomOffset( (Integer)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getNeedleAxisBottomOffset() );
            }
        } );
        
        needleProps.add( new Property( "rotationForZeroRPM", PropertyEditorType.FLOAT )
        {
            @Override
            public void setValue( Object value )
            {
                setNeedleRotationForZeroRPMasDegrees( (Float)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getNeedleRotationForZeroRPMasDegrees() );
            }
        } );
        
        needleProps.add( new Property( "rotationForMaxRPM", PropertyEditorType.FLOAT )
        {
            @Override
            public void setValue( Object value )
            {
                setNeedleRotationForMaxRPMasDegrees( (Float)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getNeedleRotationForMaxRPMasDegrees() );
            }
        } );
        
        props.add( needleProps );
        
        props.add( new Property( "editorRPM", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setEditorRPM( (Integer)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getEditorRPM() );
            }
        } );
        
        
        
        FlaggedList revMarkersProps = new FlaggedList( "Rev Markers", true );
        
        revMarkersProps.add( new Property( "displayRevMarkers", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayRevMarkers( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayRevMarkers() );
            }
        } );
        
        revMarkersProps.add( new Property( "displayRevMarkerNumbers", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayRevMarkerNumbers( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayRevMarkerNumbers() );
            }
        } );
        
        revMarkersProps.add( new Property( "rmInnerRadius", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setRevMarkersInnerRadius( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getRevMarkersInnerRadius() );
            }
        } );
        
        revMarkersProps.add( new Property( "rmLength", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setRevMarkersLength( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getRevMarkersLength() );
            }
        } );
        
        revMarkersProps.add( new Property( "rmBigStep", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setRevMarkersBigStep( (Integer)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getRevMarkersBigStep() );
            }
        } );
        
        revMarkersProps.add( new Property( "rmSmallStep", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setRevMarkersSmallStep( (Integer)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getRevMarkersSmallStep() );
            }
        } );
        
        revMarkersProps.add( new ColorProperty( "rmColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setRevMarkersColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( revMarkersColorKey );
            }
        } );
        
        revMarkersProps.add( new ColorProperty( "rmMediumColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setRevMarkersMediumColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( revMarkersMediumColorKey );
            }
        } );
        
        revMarkersProps.add( new ColorProperty( "rmHighColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setRevMarkersHighColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( revMarkersHighColorKey );
            }
        } );
        
        revMarkersProps.add( revMarkersFont );
        
        revMarkersProps.add( new ColorProperty( "rmFontColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setRevMarkersFontColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( revMarkersFontColorKey );
            }
        } );
        
        props.add( revMarkersProps );
        
        FlaggedList shiftLightProps = new FlaggedList( "Shift Light", true );
        
        shiftLightProps.add( new Property( "displayShiftLight", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayShiftLight( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayShiftLight() );
            }
        } );
        
        shiftLightProps.add( new Property( "imageName", PropertyEditorType.IMAGE )
        {
            @Override
            public void setValue( Object value )
            {
                setShiftLightImageName( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( getShiftLightImageName() );
            }
        } );
        
        shiftLightProps.add( new Property( "shiftLightPosX", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setShiftLightPosX( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getShiftLightPosX() );
            }
        } );
        
        shiftLightProps.add( new Property( "shiftLightPosY", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setShiftLightPosY( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getShiftLightPosY() );
            }
        } );
        
        shiftLightProps.add( new Property( "shiftLightRPM", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setShiftLightRPM( (Integer)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getShiftLightRPM() );
            }
        } );
        
        props.add( shiftLightProps );
        
        FlaggedList gearProps = new FlaggedList( "Gear", true );
        
        gearProps.add( new Property( "gearPosX", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setGearPosX( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getGearPosX() );
            }
        } );
        
        gearProps.add( new Property( "gearPosY", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setGearPosY( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getGearPosY() );
            }
        } );
        
        gearProps.add( gearFont );
        
        gearProps.add( new ColorProperty( "gearFontColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setGearFontColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( gearFontColorKey );
            }
        } );
        
        props.add( gearProps );
        
        FlaggedList boostProps = new FlaggedList( "Engine Boost", true );
        
        boostProps.add( new Property( "displayBoostBar", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayBoostBar( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayBoostBar() );
            }
        } );
        
        boostProps.add( new Property( "boostBarPosX", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setBoostBarPosX( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getBoostBarPosX() );
            }
        } );
        
        boostProps.add( new Property( "boostBarPosY", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setBoostBarPosY( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getBoostBarPosY() );
            }
        } );
        
        boostProps.add( new Property( "boostBarWidth", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setBoostBarWidth( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getBoostBarWidth() );
            }
        } );
        
        boostProps.add( new Property( "boostBarHeight", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setBoostBarHeight( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getBoostBarHeight() );
            }
        } );
        
        boostProps.add( new Property( "displayBoostNumber", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayBoostNumber( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayBoostNumber() );
            }
        } );
        
        boostProps.add( new Property( "boostNumberPosX", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setBoostNumberPosX( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getBoostNumberPosX() );
            }
        } );
        
        boostProps.add( new Property( "boostNumberPosY", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setBoostNumberPosY( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getBoostNumberPosY() );
            }
        } );
        
        boostProps.add( boostNumberFont );
        
        boostProps.add( new ColorProperty( "boostNumberFontColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setBoostNumberFontColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( boostNumberFontColorKey );
            }
        } );
        
        props.add( boostProps );
        
        props.add( new Property( "rpmPosY", PropertyEditorType.INTEGER )
        {
            public void setValue( Object value )
            {
                setRPMPosY( ( (Number)value ).intValue() );
            }
            
            public Object getValue()
            {
                return ( getRPMPosY() );
            }
        } );
        
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
        
        setBackgroundColor( (String)null );
    }
}
