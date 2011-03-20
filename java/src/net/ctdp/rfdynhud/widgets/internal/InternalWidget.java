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
package net.ctdp.rfdynhud.widgets.internal;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.base.widget.Hidden;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * This {@link Widget} displays information, if something went wrong
 * or the plugin is in beta state or something.
 * 
 * @author Marvin Froehlich (CTDP)
 */
@Hidden
public class InternalWidget extends Widget
{
    private DrawnString[] ds = null;
    
    private String[] message = null;
    
    public void setMessage( String... message )
    {
        this.message = message;
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
        if ( ( message == null ) || ( message.length == 0 ) )
        {
            ds = null;
        }
        else
        {
            ds = new DrawnString[ message.length ];
            for ( int i = 0; i < message.length; i++ )
                ds[i] = drawnStringFactory.newDrawnString( "ds" + i, 0, i * 20, Alignment.LEFT, false, getFont(), isFontAntiAliased(), java.awt.Color.RED );
        }
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw && ( ds != null ) )
        {
            for ( int i = 0; i < ds.length; i++ )
                ds[i].draw( offsetX, offsetY, message[i], texture );
        }
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        if ( forceAll )
            super.getProperties( propsCont, forceAll );
    }
    
    public InternalWidget()
    {
        super( null, null, 30.0f, 10.0f );
    }
}
