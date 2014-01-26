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
package net.ctdp.rfdynhud.widgets.standard.drivingaids;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.DrivingAids;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link DrivingAidsWidget} displays driving aids' states.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DrivingAidsWidget extends Widget implements DrivingAids.DrivingAidStateChangeListener
{
    private final IntProperty columns = new IntProperty( "columns", 2, 1, Integer.MAX_VALUE );
    private final IntProperty gap = new IntProperty( "gap", 5 );
    
    private final ImageProperty imageBackgroundImageOff = new ImageProperty( "iconBackgroundOff", null, "standard/drivingaid_background_off.png", false, true );
    private final ImageProperty imageBackgroundImageLow = new ImageProperty( "iconBackgroundLow", null, "standard/drivingaid_background_low.png", false, true );
    private final ImageProperty imageBackgroundImageMedium = new ImageProperty( "iconBackgroundMedium", null, "standard/drivingaid_background_medium.png", false, true );
    private final ImageProperty imageBackgroundImageHigh = new ImageProperty( "iconBackgroundHigh", null, "standard/drivingaid_background_high.png", false, true );
    
    private TextureImage2D iconBackgroundImageOff = null;
    private TextureImage2D iconBackgroundImageLow = null;
    private TextureImage2D iconBackgroundImageMedium = null;
    private TextureImage2D iconBackgroundImageHigh = null;
    
    private TextureImage2D[][] icons = null;
    private int iconSize = 0;
    private int innerIconSize = 0;
    private int innerIconOffset = 0;
    private boolean[] stateChanged = null;
    
    public DrivingAidsWidget()
    {
        super( StandardWidgetSet.INSTANCE, StandardWidgetSet.WIDGET_PACKAGE, 9.9f, 16.5f );
        
        setPadding( 3, 3, 3, 3 );
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
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( columns ) );
        else if ( loader.loadProperty( gap ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Misc" );
        
        propsCont.addProperty( columns );
        propsCont.addProperty( gap );
    }
    
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        final DrivingAids drivingAids = gameData.getDrivingAids();
        
        int numAids = drivingAids.getNumAids();
        //int numRows = (int)Math.ceil( numAids / columns.getFloatValue() );
        
        iconSize = (int)Math.floor( widgetInnerWidth / columns.getFloatValue() );
        innerIconSize = iconSize * 3 / 4;
        innerIconOffset = ( iconSize - innerIconSize ) / 2;
        
        iconBackgroundImageOff = imageBackgroundImageOff.getImage().getScaledTextureImage( iconSize, iconSize, iconBackgroundImageOff, isEditorMode );
        iconBackgroundImageLow = imageBackgroundImageLow.getImage().getScaledTextureImage( iconSize, iconSize, iconBackgroundImageLow, isEditorMode );
        iconBackgroundImageMedium = imageBackgroundImageMedium.getImage().getScaledTextureImage( iconSize, iconSize, iconBackgroundImageMedium, isEditorMode );
        iconBackgroundImageHigh = imageBackgroundImageHigh.getImage().getScaledTextureImage( iconSize, iconSize, iconBackgroundImageHigh, isEditorMode );
        
        if ( icons == null )
            icons = new TextureImage2D[ numAids ][];
        
        stateChanged = new boolean[ numAids ];
        
        for ( int i = 0; i < numAids; i++ )
        {
            int numStates = drivingAids.getNumStates( i );
            icons[i] = new TextureImage2D[ numStates ];
            
            for ( int j = 0; j < numStates; j++ )
            {
                icons[i][j] = drivingAids.getAidIcon( i, j ).getScaledTextureImage( innerIconSize, innerIconSize, icons[i][j], isEditorMode );
            }
            
            stateChanged[i] = true;
        }
    }
    
    @Override
    protected void onWidgetAttached( WidgetsConfiguration config, LiveGameData gameData )
    {
        gameData.getDrivingAids().registerListener( this );
    }
    
    @Override
    protected void onWidgetDetached( WidgetsConfiguration config, LiveGameData gameData )
    {
        gameData.getDrivingAids().unregisterListener( this );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
    }
    
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode )
    {
    }
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused )
    {
    }
    
    @Override
    public void onDrivingAidsUpdated( LiveGameData gameData, boolean isEditorMode )
    {
    }
    
    @Override
    public void onDrivingAidStateChanged( LiveGameData gameData, int aidIndex, int oldState, int newState )
    {
        if ( stateChanged != null )
            stateChanged[aidIndex] = true;
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final DrivingAids drivingAids = gameData.getDrivingAids();
        int numAids = drivingAids.getNumAids();
        
        int x = 0;
        int y = 0;
        int column = 0;
        
        for ( int i = 0; i < numAids; i++ )
        {
            if ( stateChanged[i] )
            {
                clearBackgroundRegion( texture, offsetX, offsetY, x, y, iconSize, iconSize, true, null );
                
                int state = drivingAids.getAidState( i );
                int state2 = state;
                int numStates = drivingAids.getNumStates( i );
                if ( numStates == 2 )
                    state2 = ( state == 0 ) ? 0 : 3;
                else if ( numStates == 3 )
                    state2 = ( state == 0 ) ? 0 : ( ( state == 1 ) ? 1 : 3 );
                
                switch ( state2 )
                {
                    case 0:
                        texture.drawImage( iconBackgroundImageOff, offsetX + x, offsetY + y, false, null );
                        break;
                    case 1:
                        texture.drawImage( iconBackgroundImageLow, offsetX + x, offsetY + y, false, null );
                        break;
                    case 2:
                        texture.drawImage( iconBackgroundImageMedium, offsetX + x, offsetY + y, false, null );
                        break;
                    case 3:
                    default:
                        texture.drawImage( iconBackgroundImageHigh, offsetX + x, offsetY + y, false, null );
                        break;
                }
                
                texture.drawImage( icons[i][state], offsetX + x + innerIconOffset, offsetY + y + innerIconOffset, false, null );
            }
            
            column++;
            
            if ( column < columns.getIntValue() )
            {
                x += iconSize;
                x += gap.getIntValue();
            }
            else
            {
                column = 0;
                
                x = 0;
                y += iconSize;
                y += gap.getIntValue();
            }
        }
    }
}
