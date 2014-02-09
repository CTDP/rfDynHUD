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
package net.ctdp.rfdynhud.widgets.standard.statusflags;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.StatusFlag;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link StatusFlagsWidget} displays status flags when ever they're thrown.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class StatusFlagsWidget extends Widget
{
    private final BooleanProperty showGreenAfterBlue = new BooleanProperty( "showGreenAfterBlue", false );
    private final FloatProperty showGreenTime = new FloatProperty( "showGreenTime", 5f, -1f, Float.MAX_VALUE );
    
    private final ImagePropertyWithTexture imageGreen = new ImagePropertyWithTexture( "imageGreen", null, "standard/flag_green.png", false, false );
    private final ImagePropertyWithTexture imageYellow = new ImagePropertyWithTexture( "imageYellow", null, "standard/flag_yellow.png", false, false );
    private final ImagePropertyWithTexture imageBlue = new ImagePropertyWithTexture( "imageBlue", null, "standard/flag_blue.png", false, false );
    private final ImagePropertyWithTexture imageRed = new ImagePropertyWithTexture( "imageRed", null, "standard/flag_red.png", false, false );
    private final ImagePropertyWithTexture imageRedYellow = new ImagePropertyWithTexture( "imageRedYellow", null, "standard/flag_red_yellow_striped.png", false, false );
    private final ImagePropertyWithTexture imageWhite = new ImagePropertyWithTexture( "imageWhite", null, "standard/flag_white.png", false, false );
    private final ImagePropertyWithTexture imageBlack = new ImagePropertyWithTexture( "imageBlack", null, "standard/flag_black.png", false, false );
    private final ImagePropertyWithTexture imageChequered = new ImagePropertyWithTexture( "imageChequered", null, "standard/flag_chequered.png", false, false );
    
    private final EnumValue<StatusFlag> flagValue = new EnumValue<StatusFlag>();
    private float sessionTimeForGreenFlag = -1f;
    private boolean greenFlagShown = false;
    
    public StatusFlagsWidget()
    {
        super( StandardWidgetSet.INSTANCE, StandardWidgetSet.WIDGET_PACKAGE, 6.0f, 5.333f );
        
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
        
        writer.writeProperty( showGreenAfterBlue, "Whether to show the green flag after the blue flag disappeared" );
        writer.writeProperty( showGreenTime, "Number of seconds to display the green flag after thrown (-1 for forever)" );
        writer.writeProperty( imageGreen, "" );
        writer.writeProperty( imageYellow, "" );
        writer.writeProperty( imageBlue, "" );
        writer.writeProperty( imageRed, "" );
        writer.writeProperty( imageRedYellow, "" );
        writer.writeProperty( imageWhite, "" );
        writer.writeProperty( imageBlack, "" );
        writer.writeProperty( imageChequered, "" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader, LiveGameData gameData )
    {
        super.loadProperty( loader, gameData );
        
        if ( loader.loadProperty( showGreenAfterBlue ) );
        else if ( loader.loadProperty( showGreenTime ) );
        else if ( loader.loadProperty( imageGreen ) );
        else if ( loader.loadProperty( imageYellow ) );
        else if ( loader.loadProperty( imageBlue ) );
        else if ( loader.loadProperty( imageRed ) );
        else if ( loader.loadProperty( imageRedYellow ) );
        else if ( loader.loadProperty( imageWhite ) );
        else if ( loader.loadProperty( imageBlack ) );
        else if ( loader.loadProperty( imageChequered ) );
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
        
        propsCont.addGroup( "Misc" );
        
        propsCont.addProperty( showGreenAfterBlue );
        propsCont.addProperty( showGreenTime );
        
        propsCont.addGroup( "Flag images" );
        
        propsCont.addProperty( imageGreen );
        propsCont.addProperty( imageYellow );
        propsCont.addProperty( imageBlue );
        propsCont.addProperty( imageRed );
        propsCont.addProperty( imageRedYellow );
        propsCont.addProperty( imageWhite );
        propsCont.addProperty( imageBlack );
        propsCont.addProperty( imageChequered );
    }
    
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        /*
        imageGreen.updateSize( widgetInnerWidth, widgetInnerHeight, isEditorMode );
        imageYellow.updateSize( widgetInnerWidth, widgetInnerHeight, isEditorMode );
        imageBlue.updateSize( widgetInnerWidth, widgetInnerHeight, isEditorMode );
        imageRed.updateSize( widgetInnerWidth, widgetInnerHeight, isEditorMode );
        imageRedYellow.updateSize( widgetInnerWidth, widgetInnerHeight, isEditorMode );
        imageWhite.updateSize( widgetInnerWidth, widgetInnerHeight, isEditorMode );
        imageBlack.updateSize( widgetInnerWidth, widgetInnerHeight, isEditorMode );
        imageChequered.updateSize( widgetInnerWidth, widgetInnerHeight, isEditorMode );
        */
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
    }
    
    private static StatusFlag getFlag( LiveGameData gameData, boolean isEditorMode )
    {
        StatusFlag flag = gameData.getScoringInfo().getViewedVehicleScoringInfo().getStatusFlag();
        if ( isEditorMode )
            flag = StatusFlag.CHEQUERED;
        else if ( flag == null )
            flag = StatusFlag.GREEN;
        
        return ( flag );
    }
    
    private void updateSessionTimeForGreenFlag( LiveGameData gameData, StatusFlag flag, boolean wasBlue, boolean isEditorMode )
    {
        if ( flag == null )
            flag = getFlag( gameData, isEditorMode );
        
        if ( flag == StatusFlag.GREEN )
        {
            if ( wasBlue && !showGreenAfterBlue.getBooleanValue() )
                sessionTimeForGreenFlag = -Float.MAX_VALUE;
            else if ( showGreenTime.getFloatValue() < 0f )
                sessionTimeForGreenFlag = Float.MAX_VALUE;
            else
                sessionTimeForGreenFlag = gameData.getScoringInfo().getSessionTime() + showGreenTime.getFloatValue();
        }
        else
        {
            sessionTimeForGreenFlag = -Float.MAX_VALUE;
        }
    }
    
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, boolean isEditorMode )
    {
        super.onSessionStarted( sessionType, gameData, isEditorMode );
        
        updateSessionTimeForGreenFlag( gameData, null, false, isEditorMode );
    }
    
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
        
        flagValue.reset( true );
        updateSessionTimeForGreenFlag( gameData, null, false, isEditorMode );
    }
    
    @Override
    public void onCockpitExited( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitExited( gameData, isEditorMode );
        
        updateSessionTimeForGreenFlag( gameData, null, false, isEditorMode );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        StatusFlag flag = getFlag( gameData, isEditorMode );
        
        if ( flagValue.update( flag ) || needsCompleteRedraw )
        {
            clearBackgroundRegion( texture, offsetX, offsetY, 0, 0, width, height, true, null );
            
            ImagePropertyWithTexture image = null;
            switch ( flag )
            {
                case GREEN:
                    image = imageGreen;
                    break;
                case YELLOW:
                    image = imageYellow;
                    break;
                case BLUE:
                    image = imageBlue;
                    break;
                case RED:
                    image = imageRed;
                    break;
                case RED_YELLOW_STRIPED:
                    image = imageRedYellow;
                    break;
                case WHITE:
                    image = imageWhite;
                    break;
                case BLACK:
                    image = imageBlack;
                    break;
                case CHEQUERED:
                    image = imageChequered;
                    break;
            }
            
            if ( ( flag == StatusFlag.GREEN ) && flagValue.hasChanged( false ) )
                updateSessionTimeForGreenFlag( gameData, flag, ( flagValue.getOldValue() == StatusFlag.BLUE ), isEditorMode );
            
            if ( ( flag != StatusFlag.GREEN ) || ( gameData.getScoringInfo().getSessionTime() <= sessionTimeForGreenFlag ) )
            {
                if ( image != null )
                {
                    image.updateSize( getInnerSize().getEffectiveWidth(), getInnerSize().getEffectiveHeight(), isEditorMode );
                    
                    texture.drawImage( image.getTexture(), offsetX, offsetY, true, null );
                    
                    if ( flag == StatusFlag.GREEN )
                        greenFlagShown = true;
                }
            }
            
            flagValue.setUnchanged();
        }
        else if ( ( flag == StatusFlag.GREEN ) && ( gameData.getScoringInfo().getSessionTime() > sessionTimeForGreenFlag ) )
        {
            if ( greenFlagShown )
                clearBackgroundRegion( texture, offsetX, offsetY, 0, 0, width, height, true, null );
            
            greenFlagShown = false;
        }
    }
}
