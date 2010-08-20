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
package net.ctdp.rfdynhud.widgets.fuel;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionLimit;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.AbstractSize;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.LongValue;
import net.ctdp.rfdynhud.values.Position;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.values.ValidityTest;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link FuelWidget} displays fuel information like current fuel load, fuel usage per lap,
 * and computed fuel for scheduled pitstops.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class FuelWidget extends Widget
{
    private static final InputAction INPUT_ACTION_INC_PITSTOP = new InputAction( "IncPitstopAction" );
    private static final InputAction INPUT_ACTION_DEC_PITSTOP = new InputAction( "DecPitstopAction" );
    
    private final BooleanProperty displayFuelBar = new BooleanProperty( this, "displayFuelBar", true );
    private final BooleanProperty displayTankSize = new BooleanProperty( this, "displayTankSize", true );
    private final BooleanProperty displayFuelLoad = new BooleanProperty( this, "displayFuelLoad", true );
    private final BooleanProperty displayFuelWeight = new BooleanProperty( this, "displayFuelWeight", true );
    private final BooleanProperty displayFuelLaps = new BooleanProperty( this, "displayFuelLaps", true );
    private final BooleanProperty displayFuelUsage = new BooleanProperty( this, "displayFuelUsage", true );
    private final BooleanProperty displayPitstopInfo = new BooleanProperty( this, "displayPitstopInfo", true );
    
    private final FontProperty font2 = new FontProperty( this, "font2", FontProperty.STANDARD_FONT3_NAME );
    
    private final FontProperty tankSizeFont = new FontProperty( this, "tankSizeFont", "Monospaced-PLAIN-9v" );
    private final FontProperty fuelFont = new FontProperty( this, "fuelFont", FontProperty.STANDARD_FONT_NAME );
    
    private final ColorProperty fuelBarBackground = new ColorProperty( this, "fuelBarBackground", "#000000" );
    private final ColorProperty fuelBarColor = new ColorProperty( this, "fuelBarColor", "#54760B" );
    
    private final Size fuelBarWidth;
    
    private final ColorProperty fuelFontColor = new ColorProperty( this, "fuelFontColor", "#FFFFFFCD" );
    
    private final BooleanProperty roundUpRemainingLaps = new BooleanProperty( this, "roundUpRemainingLaps", false );
    
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
    
    private final IntProperty lowFuelBlinkTime = new IntProperty( this, "lowFuelBlinkTime", "blinkTime", 500, 0, 5000, false )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            lowFuelBlinkNanos = newValue * 1000000L;
        }
    };
    private long lowFuelBlinkNanos = lowFuelBlinkTime.getIntValue() * 1000000L;
    private long nextBlinkTime = -1L;
    private boolean blinkState = false;
    
    private DrawnString fuelLoadString0 = null;
    private DrawnString fuelLoadString1 = null;
    private DrawnString fuelLoadString2 = null;
    private DrawnString fuelLoadString3 = null;
    
    private DrawnString fuelUsageHeaderString = null;
    private DrawnString fuelUsageLastLapHeaderString = null;
    private DrawnString fuelUsageAvgHeaderString = null;
    
    private DrawnString fuelUsageLastLapString = null;
    private DrawnString fuelUsageAvgString = null;
    
    private DrawnString nextPitstopHeaderString = null;
    private DrawnString nextPitstopLapString = null;
    private DrawnString nextPitstopFuelString = null;
    
    /*
    private static final class StintFuel
    {
        public final float stintLength;
        public final float fuelConsumption;
        
        public StintFuel( float stintLength, float fuelConsumption )
        {
            this.stintLength = stintLength;
            this.fuelConsumption = fuelConsumption;
        }
    }
    */
    
    private int oldNextPitstopLapCorrection = -1;
    private int nextPitstopLapCorrection = 0;
    private int nextPitstopFuelLapsCorrection = 0;
    private final IntValue pitstopFuel = new IntValue( ValidityTest.GREATER_THAN, 0 );
    private final IntValue stintLengthV = new IntValue( ValidityTest.GREATER_THAN, 0 );
    private final LongValue fuelUsage = new LongValue();
    
    private int oldFuel = -1;
    private float oldAverage = -1f;
    
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
    public InputAction[] getInputActions()
    {
        return ( new InputAction[] { INPUT_ACTION_INC_PITSTOP, INPUT_ACTION_DEC_PITSTOP } );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void bake()
    {
        super.bake();
        
        lowFuelWarningImagePosition.bake();
        fuelBarWidth.bake();
    }
    
    @Override
    public void setAllPosAndSizeToPercents()
    {
        super.setAllPosAndSizeToPercents();
        
        lowFuelWarningImagePosition.setXToPercents();
        lowFuelWarningImagePosition.setYToPercents();
        
        fuelBarWidth.setWidthToPercents();
        fuelBarWidth.setHeightToPercents();
    }
    
    @Override
    public void setAllPosAndSizeToPixels()
    {
        super.setAllPosAndSizeToPixels();
        
        lowFuelWarningImagePosition.setXToPixels();
        lowFuelWarningImagePosition.setYToPixels();
        
        fuelBarWidth.setWidthToPixels();
        fuelBarWidth.setHeightToPixels();
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
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, editorPresets );
        
        setControlVisibility( gameData.getScoringInfo().getViewedVehicleScoringInfo() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        loadLowFuelWarningImages( editorPresets != null );
        resetBlink( editorPresets != null );
        
        this.nextPitstopLapCorrection = 0;
        this.nextPitstopFuelLapsCorrection = 0;
        this.oldNextPitstopLapCorrection = ( ( nextPitstopLapCorrection + Short.MAX_VALUE / 2 ) << 16 ) | ( nextPitstopFuelLapsCorrection + Short.MAX_VALUE / 2 );
        this.pitstopFuel.reset();
        
        this.stintLengthV.reset();
        this.fuelUsage.reset();
        this.oldFuel = -1;
        this.oldAverage = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPitsExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onPitsExited( gameData, editorPresets );
        
        if ( stintLengthV.getValue() < 1 )
        {
            this.nextPitstopLapCorrection = 0;
            this.nextPitstopFuelLapsCorrection = 0;
        }
        
        this.stintLengthV.reset();
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
    public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, EditorPresets editorPresets )
    {
        if ( action == INPUT_ACTION_INC_PITSTOP )
        {
            if ( ( modifierMask & InputAction.MODIFIER_MASK_SHIFT ) == 0 )
            {
                if ( nextPitstopLapCorrection < 0 )
                {
                    this.nextPitstopLapCorrection++;
                    this.nextPitstopFuelLapsCorrection--;
                }
            }
            else
            {
                this.nextPitstopFuelLapsCorrection++;
            }
        }
        else if ( action == INPUT_ACTION_DEC_PITSTOP )
        {
            if ( ( modifierMask & InputAction.MODIFIER_MASK_SHIFT ) == 0 )
            {
                this.nextPitstopLapCorrection--;
                this.nextPitstopFuelLapsCorrection++;
            }
            else
            {
                this.nextPitstopFuelLapsCorrection--;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        if ( !isLowFuelWaningUsed() )
            return ( null );
        
        loadLowFuelWarningImages( editorPresets != null );
        
        TransformableTexture[] tts;
        if ( lowFuelWarningImageNameOff.isNoImage() || lowFuelWarningImageNameOn.isNoImage() )
            tts = new TransformableTexture[ 1 ];
        else
            tts = new TransformableTexture[ 2 ];
        
        int i = 0;
        
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
    
    private static final String getFuelUnits( MeasurementUnits measurementUnits )
    {
        if ( measurementUnits == MeasurementUnits.IMPERIAL )
            return ( Loc.fuel_units_IMPERIAL );
        
        return ( Loc.fuel_units_METRIC );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final java.awt.Font font = getFont();
        final boolean fontAntiAliased = isFontAntiAliased();
        final java.awt.Font font2 = this.font2.getFont();
        final boolean font2AntiAliased = this.font2.isAntiAliased();
        final java.awt.Color fontColor = getFontColor();
        final java.awt.Font tankSizeFont = this.tankSizeFont.getFont();
        final boolean tankSizeFontAntiAliased = this.tankSizeFont.isAntiAliased();
        final java.awt.Font fuelFont = this.fuelFont.getFont();
        final boolean fuelFontAntiAliased = this.fuelFont.isAntiAliased();
        final java.awt.Color fuelFontColor = this.fuelFontColor.getColor();
        
        //fuelHeaderString = dsf.newDrawnString( "fuelHeaderString", left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, Loc.fuelHeader_prefix + ": (", ")" );
        
        boolean showAnyAdditionalText = displayFuelUsage.getBooleanValue() || displayPitstopInfo.getBooleanValue();
        int fuelBarWidth = showAnyAdditionalText ? this.fuelBarWidth.getEffectiveWidth() : width;
        int fuelBarCenter = showAnyAdditionalText ? ( fuelBarWidth / 2 ) : ( width / 2 );
        
        DrawnString relY = null;
        
        if ( displayTankSize.getBooleanValue() )
        {
            fuelLoadString0 = dsf.newDrawnString( "fuelLoadString0", null, relY, fuelBarCenter, 0, Alignment.CENTER, false, tankSizeFont, tankSizeFontAntiAliased, fuelFontColor, null, null );
            relY = fuelLoadString0;
        }
        else
        {
            fuelLoadString0 = null;
        }
        
        if ( displayFuelLoad.getBooleanValue() )
        {
            fuelLoadString1 = dsf.newDrawnString( "fuelLoadString1", null, relY, fuelBarCenter, 0, Alignment.CENTER, false, fuelFont, fuelFontAntiAliased, fuelFontColor, null, getFuelUnits( gameData.getProfileInfo().getMeasurementUnits() ) );
            relY = fuelLoadString1;
        }
        else
        {
            fuelLoadString1 = null;
        }
        
        if ( displayFuelWeight.getBooleanValue() )
        {
            fuelLoadString2 = dsf.newDrawnString( "fuelLoadString2", null, relY, fuelBarCenter, 0, Alignment.CENTER, false, fuelFont, fuelFontAntiAliased, fuelFontColor, null, Loc.fuelLoad2_postfix );
            relY = fuelLoadString2;
        }
        else
        {
            fuelLoadString2 = null;
        }
        
        if ( displayFuelLaps.getBooleanValue() )
        {
            fuelLoadString3 = dsf.newDrawnString( "fuelLoadString3", null, relY, fuelBarCenter, 0, Alignment.CENTER, false, font2, font2AntiAliased, fuelFontColor, null, null );
            relY = fuelLoadString3;
        }
        else
        {
            fuelLoadString3 = null;
        }
        
        int textTop = -2;
        int textLeft = fuelBarWidth + 4;
        
        if ( !displayFuelBar.getBooleanValue() && !displayTankSize.getBooleanValue() && !displayFuelLoad.getBooleanValue() && !displayFuelWeight.getBooleanValue() && !displayFuelLaps.getBooleanValue() )
            textLeft = 0;
        
        boolean b = displayFuelUsage.getBooleanValue();
        //if ( displayFuelUsage.getBooleanValue() )
        {
            int lastToAvgSpacing = 75; // 85
            
            fuelUsageHeaderString = dsf.newDrawnStringIf( b, "fuelUsageHeaderString", null, null, textLeft, textTop, Alignment.LEFT, false, font, fontAntiAliased, fontColor, Loc.fuelUsageHeader + ":", null );
            fuelUsageLastLapHeaderString = dsf.newDrawnStringIf( b, "fuelUsageLastLapHeaderString", null, fuelUsageHeaderString, textLeft + 50, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor, Loc.fuelUsageLastLapHeader, null );
            fuelUsageAvgHeaderString = dsf.newDrawnStringIf( b, "fuelUsageAvgHeaderString", null, fuelUsageHeaderString, textLeft + 50 + lastToAvgSpacing, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor, Loc.fuelUsageAvgHeader, null );
            
            fuelUsageLastLapString = dsf.newDrawnStringIf( b, "fuelUsageOneLapString", null, fuelUsageLastLapHeaderString, textLeft + 50, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor );
            fuelUsageAvgString = dsf.newDrawnStringIf( b, "fuelUsageAvgString", null, fuelUsageAvgHeaderString, textLeft + 50 + lastToAvgSpacing, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor );
        }
        
        b = displayPitstopInfo.getBooleanValue();
        //if ( displayPitstopInfo.getBooleanValue() )
        {
            int psOffsetY = displayFuelUsage.getBooleanValue() ? 7 : textTop;
            
            nextPitstopHeaderString = dsf.newDrawnStringIf( b, "nextPitstopHeaderString", null, fuelUsageLastLapString, textLeft, psOffsetY, Alignment.LEFT, false, font, fontAntiAliased, fontColor, Loc.nextPitstopHeader + ":", null );
            nextPitstopLapString = dsf.newDrawnStringIf( b, "nextPitstopLapString", null, nextPitstopHeaderString, textLeft + 10, 2, Alignment.LEFT, false, font2, font2AntiAliased, fontColor, Loc.nextPitstopLap_prefix + ": ", null );
            nextPitstopFuelString = dsf.newDrawnStringIf( b, "nextPitstopFuelString", null, nextPitstopLapString, textLeft + 10, 0, Alignment.LEFT, false, font2, font2AntiAliased, fontColor, Loc.nextPitstopFuel_prefix + ": ", null );
        }
        
        loadLowFuelWarningImages( editorPresets != null );
        resetBlink( editorPresets != null );
    }
    
    private void drawFuelBar( float fuel, int tankSize, TextureImage2D texture, int x, int y, int height )
    {
        final boolean displayFuelBar = this.displayFuelBar.getBooleanValue();
        
        boolean showAnyAdditionalText = displayFuelUsage.getBooleanValue() || displayPitstopInfo.getBooleanValue();
        int w = showAnyAdditionalText ? this.fuelBarWidth.getEffectiveWidth() : getInnerSize().getEffectiveWidth();
        int h = height;
        
        int barHeight = displayFuelBar ? Math.min( (int)( h * fuel / tankSize ), h ) : 0;
        
        if ( !displayFuelBar || ( barHeight < h ) )
        {
            if ( !getBackground().valueEquals( fuelBarBackground.getColor() ) && ( fuelBarBackground.getColor() != null ) && ( fuelBarBackground.getColor().getAlpha() > 0 ) )
                texture.clear( fuelBarBackground.getColor(), x, y, w, h - barHeight, false, null );
        }
        
        if ( displayFuelBar )
        {
            texture.clear( fuelBarColor.getColor(), x, y + h - barHeight, w, barHeight, false, null );
        }
        
        texture.markDirty( x, y, w, h );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        TelemetryData telemData = gameData.getTelemetryData();
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        MeasurementUnits measurementUnits = gameData.getProfileInfo().getMeasurementUnits();
        
        final int tankSize = (int)gameData.getPhysics().getFuelRange().getMaxValue();
        
        if ( needsCompleteRedraw )
        {
            //fuelHeaderString.draw( offsetX, offsetY, String.valueOf( tankSize ) + " " + ( measurementUnits == MeasurementUnits.IMPERIAL ? Loc.fuelHeader_postfix_IMPERIAL : Loc.fuelHeader_postfix_METRIC ), backgroundColor, texture );
            
            if ( displayFuelUsage.getBooleanValue() )
            {
                fuelUsageHeaderString.draw( offsetX, offsetY, "", texture );
                fuelUsageLastLapHeaderString.draw( offsetX, offsetY, "", texture );
                fuelUsageAvgHeaderString.draw( offsetX, offsetY, "", texture );
            }
            
            if ( displayPitstopInfo.getBooleanValue() )
            {
                //nextPitstopHeaderString.draw( offsetX, offsetY, "", backgroundColor, image );
            }
        }
        
        final float fuel = isEditorMode ? ( tankSize * 3f / 4f ) : telemData.getFuel();
        final float fuelL = isEditorMode ? ( tankSize * 3f / 4f ) : telemData.getFuelL();
        final float avgFuelUsage = telemData.getFuelUsageAverage();
        final float lastFuelUsage = telemData.getFuelUsageLastLap();
        final float stintLength = ( editorPresets == null ) ? vsi.getStintLength() : 5.2f;
        
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
        else if ( ( this.lowFuelBlinkNanos > 0L ) && ( lowFuelWarningImageOn != null ) )
        {
            float lapsForFuel = ( fuel - 0.5f ) / avgFuelUsage;
            float restLapLength = 1.0f - ( stintLength % 1f );
            
            int lapsRemaining = scoringInfo.getEstimatedMaxLaps( vsi ) - vsi.getLapsCompleted() - 1;
            
            if ( ( avgFuelUsage > 0f ) && ( lapsForFuel - restLapLength < 1.0f ) && ( lapsRemaining > 0 ) )
            {
                if ( nextBlinkTime < 0L )
                {
                    nextBlinkTime = scoringInfo.getSessionNanos() + lowFuelBlinkNanos;
                    blinkState = true;
                }
                else if ( scoringInfo.getSessionNanos() >= nextBlinkTime )
                {
                    nextBlinkTime = scoringInfo.getSessionNanos() + lowFuelBlinkNanos;
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
        
        int fuel_ = Math.round( fuel * 10f );
        if ( needsCompleteRedraw || ( clock1 && ( ( fuel_ != oldFuel ) || ( avgFuelUsage != oldAverage ) ) ) )
        {
            oldFuel = fuel_;
            oldAverage = avgFuelUsage;
            
            //int fuelY = fuelHeaderString.getAbsY() + fuelHeaderString.getMaxHeight( true ) + 0;
            int fuelY = 0;
            int fuelHeight = height;
            
            boolean fuelBarDrawn = ( displayFuelBar.getBooleanValue() || displayTankSize.getBooleanValue() || displayFuelLoad.getBooleanValue() || displayFuelWeight.getBooleanValue() || displayFuelLaps.getBooleanValue() );
            if ( fuelBarDrawn )
                drawFuelBar( fuel, tankSize, texture, offsetX , offsetY + fuelY, fuelHeight );
            
            if ( displayTankSize.getBooleanValue() )
                fuelLoadString0.draw( offsetX, offsetY + fuelY, "(" + String.valueOf( tankSize ) + getFuelUnits( gameData.getProfileInfo().getMeasurementUnits() ) + ")", texture, !fuelBarDrawn );
            if ( displayFuelLoad.getBooleanValue() )
                fuelLoadString1.draw( offsetX, offsetY + fuelY, NumberUtil.formatFloat( fuel, 1, true ), texture, !fuelBarDrawn );
            if ( displayFuelWeight.getBooleanValue() )
                fuelLoadString2.draw( offsetX, offsetY + fuelY, NumberUtil.formatFloat( fuelL * gameData.getPhysics().getWeightOfOneLiterOfFuel(), 1, true ), texture, !fuelBarDrawn );
            
            if ( displayFuelLaps.getBooleanValue() )
            {
                String string;
                if ( avgFuelUsage > 0f )
                {
                    if ( roundUpRemainingLaps.getBooleanValue() )
                        string = NumberUtil.formatFloat( ( fuel / avgFuelUsage ) + ( stintLength - (int)stintLength ), 1, true ) + Loc.fuelLoad3_postfix;
                    else
                        string = NumberUtil.formatFloat( fuel / avgFuelUsage, 1, true ) + Loc.fuelLoad3_postfix;
                }
                else
                {
                    string = Loc.fuelLoad3_na;
                }
                fuelLoadString3.draw( offsetX, offsetY + fuelY, string, texture, !fuelBarDrawn );
            }
        }
        
        stintLengthV.update( (int)stintLength );
        
        if ( displayFuelUsage.getBooleanValue() )
        {
            fuelUsage.update( ( (long)Float.floatToIntBits( lastFuelUsage ) << 32 ) | (long)Float.floatToIntBits( avgFuelUsage ) );
            
            if ( needsCompleteRedraw || ( clock1 && fuelUsage.hasChanged() ) )
            {
                if ( avgFuelUsage < 0f )
                {
                    String string;
                    if ( lastFuelUsage > 0f )
                        string = NumberUtil.formatFloat( lastFuelUsage, 2, true ) + getFuelUnits( measurementUnits );
                    else
                        string = Loc.fuelUsageLastLap_na;
                    fuelUsageLastLapString.draw( offsetX, offsetY, string, texture );
                    
                    fuelUsageAvgString.draw( offsetX, offsetY, Loc.fuelUsageAvg_na, texture );
                }
                else
                {
                    String string;
                    if ( lastFuelUsage > 0f )
                        string = NumberUtil.formatFloat( lastFuelUsage, 2, true ) + getFuelUnits( measurementUnits );
                    else
                        string = Loc.fuelUsageLastLap_na;
                    fuelUsageLastLapString.draw( offsetX, offsetY, string, texture );
                    
                    string = NumberUtil.formatFloat( avgFuelUsage, 2, true ) + getFuelUnits( measurementUnits );
                    fuelUsageAvgString.draw( offsetX, offsetY, string, texture );
                }
            }
        }
        
        if ( displayPitstopInfo.getBooleanValue() )
        {
            if ( needsCompleteRedraw )
            {
                nextPitstopHeaderString.draw( offsetX, offsetY, "", texture );
            }
            
            int nextPitstopLap = -1;
            int pitstopFuel_ = -1;
            int pitstopLaps = -1;
            int maxLaps = scoringInfo.getEstimatedMaxLaps( vsi );
            if ( maxLaps > 0 )
            {
                maxLaps -= vsi.getLapsBehindLeader( false );
                
                if ( vsi.getSessionLimit() == SessionLimit.TIME )
                    maxLaps++; // In a timed race we never know, if we might be fast enough to drive another lap.
            }
            if ( !isEditorMode && ( avgFuelUsage > 0f ) )
            {
                int currLap = vsi.getCurrentLap();
                
                int remainingFuelLaps = (int)Math.floor( ( fuel / avgFuelUsage ) + ( stintLength - (int)stintLength ) );
                nextPitstopLap = vsi.getLapsCompleted() + remainingFuelLaps + nextPitstopLapCorrection;
                
                if ( nextPitstopLap < currLap )
                {
                    int delta = currLap - nextPitstopLap;
                    
                    nextPitstopLapCorrection += delta;
                    nextPitstopFuelLapsCorrection -= delta;
                    nextPitstopLap = vsi.getLapsCompleted() + remainingFuelLaps + nextPitstopLapCorrection;
                }
                
                int nextPitstopIndex = Math.min( gameData.getScoringInfo().getPlayersVehicleScoringInfo().getNumPitstopsMade() + 1, gameData.getSetup().getGeneral().getNumPitstops() );
                float pitstopFuel0 = gameData.getSetup().getGeneral().getFuel( nextPitstopIndex );
                pitstopLaps = (int)Math.floor( pitstopFuel0 / avgFuelUsage );
                pitstopFuel_ = (int)Math.ceil( ( pitstopLaps + nextPitstopFuelLapsCorrection ) * avgFuelUsage );
                
                while ( pitstopFuel_ < avgFuelUsage )
                {
                    nextPitstopFuelLapsCorrection++;
                    pitstopFuel_ = (int)Math.ceil( ( pitstopLaps + nextPitstopFuelLapsCorrection ) * avgFuelUsage );
                }
                
                while ( ( pitstopFuel_ > tankSize ) || ( scoringInfo.getSessionType().isRace() && ( maxLaps > 0 ) && ( nextPitstopLap + pitstopLaps + nextPitstopFuelLapsCorrection > maxLaps ) ) )
                {
                    nextPitstopFuelLapsCorrection--;
                    pitstopFuel_ = (int)Math.ceil( ( pitstopLaps + nextPitstopFuelLapsCorrection ) * avgFuelUsage );
                }
            }
            else if ( isEditorMode )
            {
                nextPitstopLap = 31;
                pitstopLaps = (int)( 72 / avgFuelUsage );
            }
            
            if ( isEditorMode )
                pitstopFuel.update( 72 );
            else
                pitstopFuel.update( pitstopFuel_ );
            
            int tmp = ( ( nextPitstopLapCorrection + Short.MAX_VALUE / 2 ) << 16 ) | ( nextPitstopFuelLapsCorrection + Short.MAX_VALUE / 2 );
            
            if ( needsCompleteRedraw || ( tmp != oldNextPitstopLapCorrection ) || pitstopFuel.hasChanged() )
            {
                oldNextPitstopLapCorrection = tmp;
                
                if ( ( avgFuelUsage > 0f ) && ( maxLaps > 0 ) )
                {
                    if ( pitstopFuel.isValid() )
                    {
                        String string = String.valueOf( nextPitstopLap ) + " (" + NumberUtil.delta( nextPitstopLapCorrection ) + ")";
                        nextPitstopLapString.draw( offsetX, offsetY, string, texture );
                        
                        string = String.valueOf( pitstopFuel.getValue() + (int)Math.ceil( avgFuelUsage * 0.25f ) ) + getFuelUnits( measurementUnits ) + " (" + ( pitstopLaps + nextPitstopFuelLapsCorrection ) + Loc.nextPitstopFuel_laps + "," + NumberUtil.delta( nextPitstopFuelLapsCorrection ) + ")";
                        nextPitstopFuelString.draw( offsetX, offsetY, string, texture );
                    }
                    else
                    {
                        nextPitstopLapString.draw( offsetX, offsetY, Loc.nextPitstopLap_enough, texture );
                        nextPitstopFuelString.draw( offsetX, offsetY, Loc.nextPitstopFuel_enough, texture );
                    }
                }
                else
                {
                    nextPitstopLapString.draw( offsetX, offsetY, Loc.nextPitstopLap_na + " (" + NumberUtil.delta( nextPitstopLapCorrection ) + ")", texture );
                    nextPitstopFuelString.draw( offsetX, offsetY, Loc.nextPitstopFuel_na + " (" + NumberUtil.delta( nextPitstopFuelLapsCorrection ) + " " + Loc.nextPitstopFuel_laps + ")", texture );
                }
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
        
        writer.writeProperty( font2, "The used (smaller) font." );
        writer.writeProperty( fuelBarBackground, "The color used for the fuel bar's background." );
        writer.writeProperty( fuelBarColor, "The color used for the fuel bar." );
        writer.writeProperty( tankSizeFont, "The used font for max fuel load (tank size)." );
        writer.writeProperty( fuelFont, "The used font for fuel load." );
        writer.writeProperty( fuelFontColor, "The color to use for fuel load in the format #RRGGBB (hex)." );
        writer.writeProperty( roundUpRemainingLaps, "Round up remaining fuel laps to include the current lap?" );
        
        writer.writeProperty( displayFuelBar, "Render the fuel bar?" );
        writer.writeProperty( displayTankSize, "Display the tank size information?" );
        writer.writeProperty( displayFuelLoad, "Display fuel load information?" );
        writer.writeProperty( displayFuelWeight, "Display fuel weight information?" );
        writer.writeProperty( displayFuelLaps, "Display fuel load in laps?" );
        writer.writeProperty( displayFuelUsage, "Display fuel usage information?" );
        writer.writeProperty( displayPitstopInfo, "Display pitstop calculation information?" );
        
        writer.writeProperty( lowFuelWarningImageNameOff, "Image name for the off-state of the low fuel warning." );
        writer.writeProperty( lowFuelWarningImageNameOn, "Image name for the on-state of the low fuel warning." );
        writer.writeProperty( lowFuelWarningImagePosition.getPositioningProperty( "lowFuelWarningImagePositioning" ), "Positioning type for the low-fuel-warning image." );
        writer.writeProperty( lowFuelWarningImagePosition.getXProperty( "lowFuelWarningImagePositionX" ), "X-position for the low-fuel-warning image." );
        writer.writeProperty( lowFuelWarningImagePosition.getYProperty( "lowFuelWarningImagePositionY" ), "Y-position for the low-fuel-warning image." );
        //writer.writeProperty( lowFuelWarningImageSize.getWidthProperty( "lowFuelWarningImageWidth" ), "Width for the low-fuel-warning image." );
        writer.writeProperty( lowFuelWarningImageSize.getHeightProperty( "lowFuelWarningImageHeight" ), "Height for the low-fuel-warning image." );
        writer.writeProperty( lowFuelBlinkTime, "Blink time in milli seconds for low fuel warning (0 to disable)." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( font2 ) );
        else if ( loader.loadProperty( fuelBarBackground ) );
        else if ( loader.loadProperty( fuelBarColor ) );
        else if ( loader.loadProperty( tankSizeFont ) );
        else if ( loader.loadProperty( fuelFont ) );
        else if ( loader.loadProperty( fuelFontColor ) );
        else if ( loader.loadProperty( roundUpRemainingLaps ) );
        else if ( loader.loadProperty( displayFuelBar ) );
        else if ( loader.loadProperty( displayTankSize ) );
        else if ( loader.loadProperty( displayFuelLoad ) );
        else if ( loader.loadProperty( displayFuelWeight ) );
        else if ( loader.loadProperty( displayFuelLaps ) );
        else if ( loader.loadProperty( displayFuelUsage ) );
        else if ( loader.loadProperty( displayPitstopInfo ) );
        else if ( loader.loadProperty( lowFuelWarningImageNameOff ) );
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
    protected void getPropertiesForParentGroup( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( font2 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( fuelBarBackground );
        propsCont.addProperty( fuelBarColor );
        propsCont.addProperty( tankSizeFont );
        propsCont.addProperty( fuelFont );
        propsCont.addProperty( fuelFontColor );
        propsCont.addProperty( roundUpRemainingLaps );
        
        propsCont.addGroup( "Optional Content" );
        
        propsCont.addProperty( displayFuelBar );
        propsCont.addProperty( displayTankSize );
        propsCont.addProperty( displayFuelLoad );
        propsCont.addProperty( displayFuelWeight );
        propsCont.addProperty( displayFuelLaps );
        propsCont.addProperty( displayFuelUsage );
        propsCont.addProperty( displayPitstopInfo );
        
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
    
    public FuelWidget( String name )
    {
        super( name, 17.8f, true, 13.5f, true );
        
        this.fuelBarWidth = Size.newLocalSize( this, 26.f, true, 0f, true );
        
        getFontProperty().setFont( "StandardFont2" );
        
        setPadding( 4, 4, 4, 4 );
    }
}
