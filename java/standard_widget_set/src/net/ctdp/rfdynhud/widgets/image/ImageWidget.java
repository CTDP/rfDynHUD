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
package net.ctdp.rfdynhud.widgets.image;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link ImageWidget} displays an image.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImageWidget extends Widget
{
    private final ImagePropertyWithTexture imageProp = new ImagePropertyWithTexture( this, "imageName", "ctdp.png" )
    {
        @Override
        public void onValueChanged( String oldValue, String newValue )
        {
            super.onValueChanged( oldValue, newValue );
            
            forceCompleteRedraw( true );
        }
    };
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        imageProp.updateSize( width, height, isEditorMode );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw )
        {
            texture.clear( imageProp.getTexture(), 0, 0, width, height, offsetX, offsetY, width, height, true, null );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( imageProp, "The displayed image's name." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( imageProp ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( imageProp );
    }
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    @Override
    protected boolean canHaveBackground()
    {
        return ( false );
    }
    
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    public ImageWidget( String name )
    {
        super( name, 17.0f, 8.6f );
        
        //getBackgroundProperty().setColorValue( "#00000000" );
    }
}
