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
package net.ctdp.rfdynhud.lessons.widgets.lesson8;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.lessons.widgets._util.LessonsWidgetSet;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * This Widget shows the order of execution of a Widget's callback methods.
 * 
 * There are several event methods, that are not executed in a specific order,
 * but when events occur. Check all the methods with an "on" prefix.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Lesson8Widget_ExecutionOrder extends Widget
{
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( LessonsWidgetSet.WIDGET_PACKAGE );
    }
    
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, isEditorMode );
        
        //log( "afterConfigurationLoaded()" );
        
        // This method is executed at the beginning and only once.
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
    {
        //log( "getSubTextures()" );
        
        // This method is executed after afterConfigurationLoaded() and only once.
        
        return ( null );
    }
    
    @Override
    protected Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
    {
        //log( "updateVisibility()" );
        
        // This method is executed 1st and each frame.
        
        return ( super.updateVisibility( gameData, isEditorMode ) );
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        //log( "initialize()" );
        
        // This method is executed 2nd and only once, unless forced.
    }
    
    @Override
    protected boolean checkForChanges( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int width, int height )
    {
        //log( "checkForChanges()" );
        
        // This method is executed 3rd and each frame.
        
        return ( false );
    }
    
    @Override
    protected void drawBorder( boolean isEditorMode, BorderWrapper border, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        //log( "drawBorder()" );
        
        // This method is executed 4th and each frame.
        
        super.drawBorder( isEditorMode, border, texture, offsetX, offsetY, width, height );
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        //log( "drawBackground()" );
        
        // This method is executed 5th and each frame.
        
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        //log( "drawWidget()" );
        
        // This method is executed 6th and last and each frame.
    }
    
    @Override
    public void beforeConfigurationCleared( WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode )
    {
        //log( "beforeConfigurationCleared()" );
        
        // This method is executed when the configuration is unloaded.
        
        super.beforeConfigurationCleared( widgetsConfig, gameData, isEditorMode );
    }
    
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
    }
    
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
    }
    
    public Lesson8Widget_ExecutionOrder()
    {
        super( 5.0f, 5.0f );
    }
}
