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
package net.ctdp.rfdynhud.widgets.standard.devicestates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ctdp.rfdynhud.gamedata.DeviceLegalStatus;
import net.ctdp.rfdynhud.gamedata.IgnitionStatus;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.ListProperty;
import net.ctdp.rfdynhud.properties.ListProperty.ListPropertyValue;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link DeviceStatesWidget} displays icons for the current states of different devices.
 * 
 * TODO: Blink speed limiter when in pitlane with speed limiter disabled.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DeviceStatesWidget extends Widget
{
    private static class DeviceStateValue implements ListPropertyValue
    {
        final Integer state;
        final String stateString;
        final String caption;
        
        @Override
        public String toString()
        {
            return ( caption );
        }
        
        @Override
        public String getForConfigFile()
        {
            return ( stateString );
        }
        
        @Override
        public boolean parse( String valueFromConfigFile )
        {
            return ( stateString.equals( valueFromConfigFile ) );
        }
        
        DeviceStateValue( Integer state, String stateString, String caption )
        {
            this.state = state;
            this.stateString = stateString;
            this.caption = caption;
        }
        
        static final DeviceStateValue never = new DeviceStateValue( null, "N", "Never" );
        static final DeviceStateValue always = new DeviceStateValue( null, "A", "Always" );
    }
    
    private static enum Alignment
    {
        TOP_LEFT,
        CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        ;
    }
    
    private final FloatProperty columns = new FloatProperty( "columns", 2, 1, Integer.MAX_VALUE );
    private final IntProperty gap = new IntProperty( "gap", 5 );
    private final EnumProperty<Alignment> alignment = new EnumProperty<Alignment>( "alignment", Alignment.TOP_LEFT );
    
    private ListProperty<DeviceStateValue, List<DeviceStateValue>> speedLimiterVisibility = null;
    private ListProperty<DeviceStateValue, List<DeviceStateValue>> ignitionVisibility = null;
    private ListProperty<DeviceStateValue, List<DeviceStateValue>> frontFlapVisibility = null;
    private ListProperty<DeviceStateValue, List<DeviceStateValue>> rearFlapVisibility = null;
    
    private final ImagePropertyWithTexture imageBackgroundImageOff = new ImagePropertyWithTexture( "iconBackgroundOff", null, "standard/drivingaid_background_off.png", false, true );
    private final ImagePropertyWithTexture imageBackgroundImageLow = new ImagePropertyWithTexture( "iconBackgroundLow", null, "standard/drivingaid_background_low.png", false, true );
    private final ImagePropertyWithTexture imageBackgroundImageMedium = new ImagePropertyWithTexture( "iconBackgroundMedium", null, "standard/drivingaid_background_medium.png", false, true );
    private final ImagePropertyWithTexture imageBackgroundImageHigh = new ImagePropertyWithTexture( "iconBackgroundHigh", null, "standard/drivingaid_background_high.png", false, true );
    
    private final ImagePropertyWithTexture imageSpeedLimiterOff = new ImagePropertyWithTexture( "imageSpeedLimiterOff", "speedLimiterOff", "standard/speed-limiter-deactivated.png", false, false );
    private final ImagePropertyWithTexture imageSpeedLimiterOn = new ImagePropertyWithTexture( "imageSpeedLimiterOn", "speedLimiterOn", "standard/speed-limiter-activated.png", false, false );
    private final ImagePropertyWithTexture imageIgnitionOff = new ImagePropertyWithTexture( "imageIgnitionOff", "ignitionOff", "standard/ignition-off.png", false, false );
    private final ImagePropertyWithTexture imageIgnitionOn = new ImagePropertyWithTexture( "imageIgnitionOn", "ignitionOn", "standard/ignition-on.png", false, false );
    private final ImagePropertyWithTexture imageIgnitionStarter = new ImagePropertyWithTexture( "imageIgnitionStarter", "ignitionStarter", "standard/ignition-starter.png", false, false );
    private final ImagePropertyWithTexture imageFrontFlapDisallowed = new ImagePropertyWithTexture( "imageFrontFlapDisallowed", "frontFlapDisallowed", "standard/front-flap-disallowed.png", false, false );
    private final ImagePropertyWithTexture imageFrontFlapNotYetAllowed = new ImagePropertyWithTexture( "imageFrontFlapNotYetAllowed", "frontFlapNotYetAllowed", "standard/front-flap-not-yet-allowed.png", false, false );
    private final ImagePropertyWithTexture imageFrontFlapAllowed = new ImagePropertyWithTexture( "imageFrontFlapAllowed", "frontFlapAllowed", "standard/front-flap-allowed.png", false, false );
    private final ImagePropertyWithTexture imageFrontFlapActivated = new ImagePropertyWithTexture( "imageFrontFlapActivated", "frontFlapActivated", "standard/front-flap-activated.png", false, false );
    private final ImagePropertyWithTexture imageRearFlapDisallowed = new ImagePropertyWithTexture( "imageRearFlapDisallowed", "rearFlapDisallowed", "standard/rear-flap-disallowed.png", false, false );
    private final ImagePropertyWithTexture imageRearFlapNotYetAllowed = new ImagePropertyWithTexture( "imageRearFlapNotYetAllowed", "rearFlapNotYetAllowed", "standard/rear-flap-not-yet-allowed.png", false, false );
    private final ImagePropertyWithTexture imageRearFlapAllowed = new ImagePropertyWithTexture( "imageRearFlapAllowed", "rearFlapAllowed", "standard/rear-flap-allowed.png", false, false );
    private final ImagePropertyWithTexture imageRearFlapActivated = new ImagePropertyWithTexture( "imageRearFlapActivated", "rearFlapActivated", "standard/rear-flap-activated.png", false, false );
    
    private int iconSize = 0;
    private int innerIconSize = 0;
    private int innerIconOffset = 0;
    
    private final BoolValue speedLimiterValue = new BoolValue();
    private final EnumValue<IgnitionStatus> ignitionValue = new EnumValue<IgnitionStatus>();
    private final IntValue frontFlapValue = new IntValue();
    private final IntValue rearFlapValue = new IntValue();
    
    private int numRows = -1;
    
    public DeviceStatesWidget()
    {
        super( StandardWidgetSet.INSTANCE, StandardWidgetSet.WIDGET_PACKAGE, 8.125f, 34.417f );
        
        getBorderProperty().setBorder( null );
        getBackgroundProperty().setColorValue( "#00000000" );
        setPadding( 0 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( columns, "Number of columns to display" );
        writer.writeProperty( gap, "Gap between the icons" );
        writer.writeProperty( alignment, "Alignment of icons" );
        
        if ( speedLimiterVisibility != null )
            writer.writeProperty( speedLimiterVisibility, "Speed limiter visibility" );
        if ( ignitionVisibility != null )
            writer.writeProperty( ignitionVisibility, "Ignition visibility" );
        if ( frontFlapVisibility != null )
            writer.writeProperty( frontFlapVisibility, "Front flap device visibility" );
        if ( rearFlapVisibility != null )
            writer.writeProperty( rearFlapVisibility, "Rear flap device visibility" );
        
        writer.writeProperty( imageBackgroundImageOff, "Background image for an aid's 'off' state" );
        writer.writeProperty( imageBackgroundImageLow, "Background image for an aid's 'low' state" );
        writer.writeProperty( imageBackgroundImageMedium, "Background image for an aid's 'medium' state" );
        writer.writeProperty( imageBackgroundImageHigh, "Background image for an aid's 'high' state" );
        
        writer.writeProperty( imageSpeedLimiterOff, null );
        writer.writeProperty( imageSpeedLimiterOn, null );
        writer.writeProperty( imageIgnitionOff, null );
        writer.writeProperty( imageIgnitionOn, null );
        writer.writeProperty( imageIgnitionStarter, null );
        writer.writeProperty( imageFrontFlapDisallowed, null );
        writer.writeProperty( imageFrontFlapNotYetAllowed, null );
        writer.writeProperty( imageFrontFlapAllowed, null );
        writer.writeProperty( imageFrontFlapActivated, null );
        writer.writeProperty( imageRearFlapDisallowed, null );
        writer.writeProperty( imageRearFlapNotYetAllowed, null );
        writer.writeProperty( imageRearFlapAllowed, null );
        writer.writeProperty( imageRearFlapActivated, null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader, LiveGameData gameData )
    {
        super.loadProperty( loader, gameData );
        
        initStateVisibilityProperties();
        
        if ( loader.loadProperty( columns ) );
        else if ( loader.loadProperty( gap ) );
        else if ( loader.loadProperty( alignment ) );
        else if ( ( speedLimiterVisibility != null ) && loader.loadProperty( speedLimiterVisibility ) );
        else if ( ( ignitionVisibility != null ) && loader.loadProperty( ignitionVisibility ) );
        else if ( ( frontFlapVisibility != null ) && loader.loadProperty( frontFlapVisibility ) );
        else if ( ( rearFlapVisibility != null ) && loader.loadProperty( rearFlapVisibility ) );
        else if ( loader.loadProperty( imageBackgroundImageOff ) );
        else if ( loader.loadProperty( imageBackgroundImageLow ) );
        else if ( loader.loadProperty( imageBackgroundImageMedium ) );
        else if ( loader.loadProperty( imageBackgroundImageHigh ) );
        else if ( loader.loadProperty( imageSpeedLimiterOff ) );
        else if ( loader.loadProperty( imageSpeedLimiterOn ) );
        else if ( loader.loadProperty( imageIgnitionOff ) );
        else if ( loader.loadProperty( imageIgnitionOn ) );
        else if ( loader.loadProperty( imageIgnitionStarter ) );
        else if ( loader.loadProperty( imageFrontFlapDisallowed ) );
        else if ( loader.loadProperty( imageFrontFlapNotYetAllowed ) );
        else if ( loader.loadProperty( imageFrontFlapAllowed ) );
        else if ( loader.loadProperty( imageFrontFlapActivated ) );
        else if ( loader.loadProperty( imageRearFlapDisallowed ) );
        else if ( loader.loadProperty( imageRearFlapNotYetAllowed ) );
        else if ( loader.loadProperty( imageRearFlapAllowed ) );
        else if ( loader.loadProperty( imageRearFlapActivated ) );
    }
    
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        initStateVisibilityProperties();
        
        propsCont.addProperty( imageBackgroundImageOff );
        propsCont.addProperty( imageBackgroundImageLow );
        propsCont.addProperty( imageBackgroundImageMedium );
        propsCont.addProperty( imageBackgroundImageHigh );
        
        propsCont.addGroup( "Device icons" );
        
        propsCont.addProperty( imageSpeedLimiterOff  );
        propsCont.addProperty( imageSpeedLimiterOn );
        propsCont.addProperty( imageIgnitionOff );
        propsCont.addProperty( imageIgnitionOn );
        propsCont.addProperty( imageIgnitionStarter );
        propsCont.addProperty( imageFrontFlapDisallowed );
        propsCont.addProperty( imageFrontFlapNotYetAllowed );
        propsCont.addProperty( imageFrontFlapAllowed );
        propsCont.addProperty( imageFrontFlapActivated );
        propsCont.addProperty( imageRearFlapDisallowed );
        propsCont.addProperty( imageRearFlapNotYetAllowed );
        propsCont.addProperty( imageRearFlapAllowed );
        propsCont.addProperty( imageRearFlapActivated );
        
        propsCont.addGroup( "Misc" );
        
        propsCont.addProperty( columns );
        propsCont.addProperty( gap );
        propsCont.addProperty( alignment );
        
        propsCont.addGroup( "Device visibilities" );
        
        propsCont.addProperty( speedLimiterVisibility );
        propsCont.addProperty( ignitionVisibility );
        propsCont.addProperty( frontFlapVisibility );
        propsCont.addProperty( rearFlapVisibility );
    }
    
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        int smallerSize = Math.min( widgetInnerWidth, widgetInnerHeight );
        
        iconSize = (int)Math.floor( ( smallerSize - gap.getFloatValue() * ( columns.getFloatValue() - 1 ) ) / columns.getFloatValue() );
        innerIconSize = iconSize * 3 / 4;
        innerIconOffset = ( iconSize - innerIconSize ) / 2;
        
        imageBackgroundImageOff.updateSize( iconSize, iconSize, isEditorMode );
        imageBackgroundImageLow.updateSize( iconSize, iconSize, isEditorMode );
        imageBackgroundImageMedium.updateSize( iconSize, iconSize, isEditorMode );
        imageBackgroundImageHigh.updateSize( iconSize, iconSize, isEditorMode );
        
        imageSpeedLimiterOff.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageSpeedLimiterOn.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageIgnitionOff.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageIgnitionOn.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageIgnitionStarter.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageFrontFlapDisallowed.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageFrontFlapNotYetAllowed.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageFrontFlapAllowed.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageFrontFlapActivated.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageRearFlapDisallowed.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageRearFlapNotYetAllowed.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageRearFlapAllowed.updateSize( innerIconSize, innerIconSize, isEditorMode );
        imageRearFlapActivated.updateSize( innerIconSize, innerIconSize, isEditorMode );
    }
    
    private void initStateVisibilityProperties()
    {
        if ( speedLimiterVisibility == null )
        {
            {
                List<DeviceStateValue> values = new ArrayList<DeviceStateValue>();
                values.add( DeviceStateValue.never );
                values.add( DeviceStateValue.always );
                
                DeviceStateValue inoff = new DeviceStateValue( 0, "0", "If not Off" );
                DeviceStateValue inon = new DeviceStateValue( 1, "1", "If not On" );
                
                values.add( inoff );
                values.add( inon );
                
                speedLimiterVisibility = new ListProperty<DeviceStateValue, List<DeviceStateValue>>( "speedLimiterVisibility", "speedLimiter", inoff, values )
                {
                    @Override
                    protected void onValueChanged( DeviceStateValue oldValue, DeviceStateValue newValue )
                    {
                        forceCompleteRedraw( false );
                    }
                };
            }
            
            {
                List<DeviceStateValue> values = new ArrayList<DeviceStateValue>();
                values.add( DeviceStateValue.never );
                values.add( DeviceStateValue.always );
                
                DeviceStateValue inoff = new DeviceStateValue( 0, "0", "If not Off" );
                DeviceStateValue inon = new DeviceStateValue( 1, "1", "If not On" );
                
                values.add( inoff );
                values.add( inon );
                
                ignitionVisibility = new ListProperty<DeviceStateValue, List<DeviceStateValue>>( "ignitionVisibility", "ignition", inon, values )
                {
                    @Override
                    protected void onValueChanged( DeviceStateValue oldValue, DeviceStateValue newValue )
                    {
                        forceCompleteRedraw( false );
                    }
                };
            }
            
            {
                List<DeviceStateValue> values = new ArrayList<DeviceStateValue>();
                values.add( DeviceStateValue.never );
                values.add( DeviceStateValue.always );
                
                DeviceStateValue indis = new DeviceStateValue( 0, "0", "If not Disallowed" );
                values.add( indis );
                
                frontFlapVisibility = new ListProperty<DeviceStateValue, List<DeviceStateValue>>( "frontFlapVisibility", "frontFlap", indis, values )
                {
                    @Override
                    protected void onValueChanged( DeviceStateValue oldValue, DeviceStateValue newValue )
                    {
                        forceCompleteRedraw( false );
                    }
                };
            }
            
            {
                List<DeviceStateValue> values = new ArrayList<DeviceStateValue>();
                values.add( DeviceStateValue.never );
                values.add( DeviceStateValue.always );
                
                DeviceStateValue indis = new DeviceStateValue( 0, "0", "If not Disallowed" );
                values.add( indis );
                
                rearFlapVisibility = new ListProperty<DeviceStateValue, List<DeviceStateValue>>( "rearFlapVisibility", "rearFlap", indis, values )
                {
                    @Override
                    protected void onValueChanged( DeviceStateValue oldValue, DeviceStateValue newValue )
                    {
                        forceCompleteRedraw( false );
                    }
                };
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        initStateVisibilityProperties();
    }
    
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        initStateVisibilityProperties();
    }
    
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
        
        speedLimiterValue.reset( true );
        ignitionValue.reset( true );
        frontFlapValue.reset( true );
        rearFlapValue.reset( true );
    }
    
    private class RowCounter
    {
        int column = 0;
        int x = 0;
        int y = 0;
        
        void reset()
        {
            numRows = 0;
            column = 0;
            x = 0;
            y = 0;
        }
        
        void incColumn()
        {
            if ( numRows == 0 )
                numRows = 1;
            
            column++;
            
            if ( column < columns.getIntValue() )
            {
                x += iconSize;
                x += gap.getIntValue();
            }
            else
            {
                column = 0;
                numRows++;
                
                x = 0;
                y += iconSize;
                y += gap.getIntValue();
            }
        }
    }
    
    private final RowCounter rowCounter = new RowCounter();
    
    private void countRows( TelemetryData telemData, boolean isEditorMode )
    {
        numRows = -1;
        
        if ( alignment.getValue() == Alignment.CENTER )
        {
            rowCounter.reset();
            
            {
                DeviceStateValue dsv = speedLimiterVisibility.getValue();
                
                if ( dsv != DeviceStateValue.never )
                {
                    if ( isEditorMode || ( dsv == DeviceStateValue.always ) || ( ( dsv.state.intValue() == 1 ) != telemData.isSpeedLimiterOn() ) )
                    {
                        rowCounter.incColumn();
                    }
                }
            }
            
            {
                DeviceStateValue dsv = ignitionVisibility.getValue();
                
                if ( dsv != DeviceStateValue.never )
                {
                    int state2 = ( ( telemData.getIgnitionStatus() == IgnitionStatus.IGNITION_STARTER ) || ( telemData.getIgnitionStatus() == IgnitionStatus.IGNITION ) ) ? 1 : 0;
                    
                    if ( isEditorMode || ( dsv == DeviceStateValue.always ) || ( dsv.state.intValue() != state2 ) )
                    {
                        rowCounter.incColumn();
                    }
                }
            }
            
            {
                DeviceStateValue dsv = frontFlapVisibility.getValue();
                
                if ( dsv != DeviceStateValue.never )
                {
                    int state = getFlapState( telemData.getFrontFlapLegalStatus(), telemData.isFrontFlapActivated() );
                    int state2 = ( state == 0 ) ? 0 : 1;
                    
                    if ( isEditorMode || ( dsv == DeviceStateValue.always ) || ( dsv.state.intValue() != state2 ) )
                    {
                        rowCounter.incColumn();
                    }
                }
            }
            
            {
                DeviceStateValue dsv = rearFlapVisibility.getValue();
                
                if ( dsv != DeviceStateValue.never )
                {
                    int state = getFlapState( telemData.getRearFlapLegalStatus(), telemData.isRearFlapActivated() );
                    int state2 = ( state == 0 ) ? 0 : 1;
                    
                    if ( isEditorMode || ( dsv == DeviceStateValue.always ) || ( dsv.state.intValue() != state2 ) )
                    {
                        rowCounter.incColumn();
                    }
                }
            }
        }
    }
    
    private TextureImage2D getIconBackground( int state )
    {
        switch ( state )
        {
            case 0:
                return ( imageBackgroundImageOff.getTexture() );
            case 1:
                return ( imageBackgroundImageLow.getTexture() );
            case 2:
                return ( imageBackgroundImageMedium.getTexture() );
            case 3:
            default:
                return ( imageBackgroundImageHigh.getTexture() );
        }
    }
    
    private int getFlapState( DeviceLegalStatus legalStatus, boolean activated )
    {
        if ( activated )
            return ( 3 );
        
        if ( legalStatus != null )
        {
            switch ( legalStatus )
            {
                case CRITERIA_MET_BUT_NOT_YET_ALLOWED:
                    return ( 1 );
                
                case ALLOWED:
                    return ( 2 );
            }
        }
        
        return ( 0 );
    }
    
    private void renderIcon( TextureImage2D background, TextureImage2D icon, TextureImage2D texture, int offsetX, int offsetY, int x, int y, int numRows )
    {
        Alignment align = alignment.getValue();
        
        int x2 = x;
        int y2 = y;
        
        if ( getInnerSize().getEffectiveWidth() > getInnerSize().getEffectiveHeight() )
        {
            x2 = y;
            y2 = x;
        }
        
        int x3 = x2;
        int y3 = y2;
        if ( align == Alignment.CENTER )
        {
            if ( getInnerSize().getEffectiveWidth() > getInnerSize().getEffectiveHeight() )
                x3 = ( getInnerSize().getEffectiveWidth() - ( numRows * iconSize ) - ( numRows * gap.getIntValue() - gap.getIntValue() ) ) / 2 + x2;
            else
                y3 = ( getInnerSize().getEffectiveHeight() - ( numRows * iconSize ) - ( numRows * gap.getIntValue() - gap.getIntValue() ) ) / 2 + y2;
        }
        else if ( ( align == Alignment.TOP_RIGHT ) || ( align == Alignment.BOTTOM_RIGHT ) )
        {
            x3 = getInnerSize().getEffectiveWidth() - x2 - iconSize;
        }
        
        int x4 = x3;
        int y4 = y3;
        if ( ( align == Alignment.BOTTOM_LEFT ) || ( align == Alignment.BOTTOM_RIGHT ) )
        {
            y4 = getInnerSize().getEffectiveHeight() - y3 - iconSize;
        }
        
        //clearBackgroundRegion( texture, offsetX, offsetY, x4, y4, iconSize, iconSize, true, null );
        
        if ( background != null )
            texture.drawImage( background, offsetX + x4, offsetY + y4, false, null );
        
        if ( icon != null )
            texture.drawImage( icon, offsetX + x4 + innerIconOffset, offsetY + y4 + innerIconOffset, false, null );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final TelemetryData telemData = gameData.getTelemetryData();
        
        Alignment align = alignment.getValue();
        
        if ( ( align == Alignment.CENTER ) && ( ( numRows < 0 ) || isEditorMode ) )
            countRows( telemData, isEditorMode );
        
        int numRows = this.numRows;
        rowCounter.reset();
        
        boolean allCleared = false;
        boolean anythingRendered = false;
        int numSkipped = 0;
        
        while ( !anythingRendered && ( numSkipped < 4 ) )
        {
            numSkipped = 0;
            
            {
                boolean skipped = true;
                DeviceStateValue dsv = speedLimiterVisibility.getValue();
                
                if ( dsv != DeviceStateValue.never )
                {
                    if ( speedLimiterValue.update( telemData.isSpeedLimiterOn() ) || allCleared || needsCompleteRedraw )
                    {
                        if ( !allCleared )
                        {
                            clearBackgroundRegion( texture, offsetX, offsetY, 0, 0, getInnerSize().getEffectiveWidth(), getInnerSize().getEffectiveHeight(), true, null );
                            allCleared = true;
                            
                            continue;
                        }
                        
                        if ( isEditorMode || ( dsv == DeviceStateValue.always ) || ( ( dsv.state.intValue() == 1 ) != telemData.isSpeedLimiterOn() ) )
                        {
                            TextureImage2D background = getIconBackground( telemData.isSpeedLimiterOn() ? 3 : 0 );
                            TextureImage2D icon = telemData.isSpeedLimiterOn() ? imageSpeedLimiterOn.getTexture() : imageSpeedLimiterOff.getTexture();
                            
                            renderIcon( background, icon, texture, offsetX, offsetY, rowCounter.x, rowCounter.y, numRows );
                            
                            rowCounter.incColumn();
                            
                            skipped = false;
                            anythingRendered = true;
                        }
                        
                        speedLimiterValue.setUnchanged();
                    }
                }
                
                if ( skipped )
                    numSkipped++;
            }
            
            {
                boolean skipped = true;
                DeviceStateValue dsv = ignitionVisibility.getValue();
                
                if ( dsv != DeviceStateValue.never )
                {
                    int state2 = ( ( telemData.getIgnitionStatus() == IgnitionStatus.IGNITION_STARTER ) || ( telemData.getIgnitionStatus() == IgnitionStatus.IGNITION ) ) ? 1 : 0;
                    
                    if ( ignitionValue.update( telemData.getIgnitionStatus() ) || allCleared || needsCompleteRedraw )
                    {
                        if ( !allCleared )
                        {
                            clearBackgroundRegion( texture, offsetX, offsetY, 0, 0, getInnerSize().getEffectiveWidth(), getInnerSize().getEffectiveHeight(), true, null );
                            allCleared = true;
                            
                            continue;
                        }
                        
                        if ( isEditorMode || ( dsv == DeviceStateValue.always ) || ( dsv.state.intValue() != state2 ) )
                        {
                            TextureImage2D background;
                            TextureImage2D icon;
                            switch ( telemData.getIgnitionStatus() )
                            {
                                case IGNITION_STARTER:
                                    background = imageBackgroundImageHigh.getTexture();
                                    icon = imageIgnitionStarter.getTexture();
                                    break;
                                case IGNITION:
                                    background = imageBackgroundImageMedium.getTexture();
                                    icon = imageIgnitionOn.getTexture();
                                    break;
                                case OFF:
                                default:
                                    background = imageBackgroundImageOff.getTexture();
                                    icon = imageIgnitionOff.getTexture();
                                    break;
                            }
                            
                            renderIcon( background, icon, texture, offsetX, offsetY, rowCounter.x, rowCounter.y, numRows );
                            
                            rowCounter.incColumn();
                            
                            skipped = false;
                            anythingRendered = true;
                        }
                        
                        ignitionValue.setUnchanged();
                    }
                }
                
                if ( skipped )
                    numSkipped++;
            }
            
            {
                boolean skipped = true;
                DeviceStateValue dsv = frontFlapVisibility.getValue();
                
                if ( dsv != DeviceStateValue.never )
                {
                    int state = getFlapState( telemData.getFrontFlapLegalStatus(), telemData.isFrontFlapActivated() );
                    int state2 = ( state == 0 ) ? 0 : 1;
                    
                    if ( frontFlapValue.update( state ) || allCleared || needsCompleteRedraw )
                    {
                        if ( !allCleared )
                        {
                            clearBackgroundRegion( texture, offsetX, offsetY, 0, 0, getInnerSize().getEffectiveWidth(), getInnerSize().getEffectiveHeight(), true, null );
                            allCleared = true;
                            
                            continue;
                        }
                        
                        if ( isEditorMode || ( dsv == DeviceStateValue.always ) || ( dsv.state.intValue() != state2 ) )
                        {
                            TextureImage2D background = getIconBackground( state );
                            TextureImage2D icon;
                            switch ( state )
                            {
                                case 1:
                                    icon = imageFrontFlapNotYetAllowed.getTexture();
                                    break;
                                case 2:
                                    icon = imageFrontFlapAllowed.getTexture();
                                    break;
                                case 3:
                                    icon = imageFrontFlapActivated.getTexture();
                                    break;
                                case 0:
                                default:
                                    icon = imageFrontFlapDisallowed.getTexture();
                                    break;
                            }
                            
                            renderIcon( background, icon, texture, offsetX, offsetY, rowCounter.x, rowCounter.y, numRows );
                            
                            rowCounter.incColumn();
                            
                            skipped = false;
                            anythingRendered = true;
                        }
                        
                        frontFlapValue.setUnchanged();
                    }
                }
                
                if ( skipped )
                    numSkipped++;
            }
            
            {
                boolean skipped = true;
                DeviceStateValue dsv = rearFlapVisibility.getValue();
                
                if ( dsv != DeviceStateValue.never )
                {
                    int state = getFlapState( telemData.getRearFlapLegalStatus(), telemData.isRearFlapActivated() );
                    int state2 = ( state == 0 ) ? 0 : 1;
                    
                    if ( frontFlapValue.update( state ) || allCleared || needsCompleteRedraw )
                    {
                        if ( !allCleared )
                        {
                            clearBackgroundRegion( texture, offsetX, offsetY, 0, 0, getInnerSize().getEffectiveWidth(), getInnerSize().getEffectiveHeight(), true, null );
                            allCleared = true;
                            
                            continue;
                        }
                        
                        if ( isEditorMode || ( dsv == DeviceStateValue.always ) || ( dsv.state.intValue() != state2 ) )
                        {
                            TextureImage2D background = getIconBackground( state );
                            TextureImage2D icon;
                            switch ( state )
                            {
                                case 1:
                                    icon = imageRearFlapNotYetAllowed.getTexture();
                                    break;
                                case 2:
                                    icon = imageRearFlapAllowed.getTexture();
                                    break;
                                case 3:
                                    icon = imageRearFlapActivated.getTexture();
                                    break;
                                case 0:
                                default:
                                    icon = imageRearFlapDisallowed.getTexture();
                                    break;
                            }
                            
                            renderIcon( background, icon, texture, offsetX, offsetY, rowCounter.x, rowCounter.y, numRows );
                            
                            rowCounter.incColumn();
                            
                            skipped = false;
                            anythingRendered = true;
                        }
                        
                        rearFlapValue.setUnchanged();
                    }
                }
                
                if ( skipped )
                    numSkipped++;
            }
        }
    }
}
