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
package net.ctdp.rfdynhud.widgets.standard.image;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link ImageWidget} displays an image.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImageWidget extends Widget
{
    private final ImagePropertyWithTexture imageName = new ImagePropertyWithTexture( this, "imageName", "standard/ctdp.png" )
    {
        @Override
        public void onValueChanged( String oldValue, String newValue )
        {
            super.onValueChanged( oldValue, newValue );
            
            forceCompleteRedraw( true );
        }
    };
    
    private final StringProperty visibleIf = new StringProperty( this, "visibleIf", "" );
    
    private Widget visibleIfWidget = null;
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, isEditorMode );
        
        if ( !isEditorMode )
        {
            String visibleIfWidgetName = visibleIf.getStringValue().trim();
            
            if ( visibleIfWidgetName.equals( "" ) )
                visibleIfWidget = null;
            else
                visibleIfWidget = widgetsConfig.getWidget( visibleIfWidgetName );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.updateVisibility( gameData, isEditorMode );
        
        if ( visibleIfWidget != null )
        {
            result = visibleIfWidget.isVisible();
        }
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        imageName.updateSize( width, height, isEditorMode );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw )
        {
            if ( getMasterWidget() == null )
                texture.clear( imageName.getTexture(), 0, 0, width, height, offsetX, offsetY, width, height, true, null );
            else
                texture.drawImage( imageName.getTexture(), 0, 0, width, height, offsetX, offsetY, width, height, true, null );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( imageName, "The displayed image's name." );
        writer.writeProperty( visibleIf, "Name of the Widget, that needs to be visible for this Widget to be visible, too." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( imageName ) );
        else if ( loader.loadProperty( visibleIf ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( imageName );
        
        if ( getMasterWidget() == null )
        {
            propsCont.addProperty( visibleIf );
        }
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
    
    public ImageWidget()
    {
        super( 17.0f, 8.6f );
        
        //getBackgroundProperty().setColorValue( "#00000000" );
    }
}
