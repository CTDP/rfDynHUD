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
package net.ctdp.rfdynhud.widgets.lessons.lesson7;

import java.awt.Font;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.base.widget.StatefulWidget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.lessons._util.LessonsWidgetSet;

/**
 * This Widget shows, how to preserve states over configuration reloads.
 * 
 * You must inherit from a different base class to do so.
 * The generic parameters will help you to save some casts.
 * If you only need one of the two stores, just use 'Object' as generic parameter for the other one.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Lesson7Widget_Stateful extends StatefulWidget<MyGeneralStore, MyLocalStore> // Note the different base class!
{
    private DrawnString ds_general = null;
    private DrawnString ds_local = null;
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( LessonsWidgetSet.WIDGET_PACKAGE );
    }
    
    @Override
    protected MyGeneralStore createGeneralStore()
    {
        return ( new MyGeneralStore() );
    }
    
    @Override
    protected MyLocalStore createLocalStore()
    {
        return ( new MyLocalStore() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        int h = TextureImage2D.getStringHeight( "XXX", getFontProperty() );
        
        ds_general = drawnStringFactory.newDrawnString( "ds_general", 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), "General: ", null );
        ds_local = drawnStringFactory.newDrawnString( "ds_local", 0, h, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), "Local: ", null );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw )
        {
            /*
             * Any instance of this Widget will show the same value for General,
             * but different values for Local.
             */
            
            ds_general.draw( offsetX, offsetY, String.valueOf( getGeneralStore().value ), texture );
            ds_local.draw( offsetX, offsetY, String.valueOf( getLocalStore().value ), texture );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        getFontProperty().setFont( "Dialog", Font.PLAIN, 9, false, true );
    }
    
    public Lesson7Widget_Stateful()
    {
        super( 14.0f, 5.0f );
    }
}
