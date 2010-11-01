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
package net.ctdp.rfdynhud.widgets.lessons.lesson4a;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.lessons._util.LessonsWidgetSet;

/**
 * This Widget shows, how to use custom images.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Lesson4aWidget_Images extends Widget
{
    /*
     * This property provides a way to let the user choose an image for your background.
     * We pass null for the display-name, which means "use the name parameter" and we define
     * to allow no-image with the last parameter.
     */
    private final ImageProperty image1 = new ImageProperty( this, "image", null, "ctdp.png", false, true );
    
    /*
     * This is a simple image property for an image to be displayed somewhere on the Widget.
     */
    private final ImageProperty image2 = new ImageProperty( this, "image2", "shiftlight_on_red.png" );
    
    /*
     * These two TextureImage2Ds cache the property images in actual size
     * as chosen in the editor.
     */
    private TextureImage2D texImage1 = null;
    private TextureImage2D texImage2 = null;
    
    private DrawnString ds = null;
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( LessonsWidgetSet.WIDGET_PACKAGE );
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
        ds = drawnStringFactory.newDrawnString( "ds", 10, 20, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        /*
         * This loads the image as defined in the property and gets a scaled instance.
         */
        texImage2 = image2.getImage().getScaledTextureImage( 32, 32, texImage2, isEditorMode );
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        /*
         * If the user didn't select no-image, draw it onto our background.
         */
        if ( !image1.isNoImage() )
        {
            /*
             * This loads the image as defined in the property and gets a scaled instance using (inner) widget size.
             */
            texImage1 = image1.getImage().getScaledTextureImage( width / 2, height / 2, texImage1, isEditorMode );
            
            /*
             * And finally we draw the image using our selected image (scaled texture).
             */
            texture.drawImage( texImage1, offsetX + ( width / 4 ), offsetY + ( height / 4 ), true, null );
        }
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        /*
         * As this method is executed after the drawBackground() method, the background is already
         * cleared with our defined background image.
         */
        
        if ( needsCompleteRedraw )
        {
            ds.draw( offsetX, offsetY, "Text on a background image", texture );
            
            /*
             * Now we simply draw our second image 10 pixels from the lower-right corner
             * of the (inner) Widget area.
             */
            texture.drawImage( texImage2, offsetX + width - texImage2.getWidth() - 10, offsetY + height - texImage2.getHeight() - 10, true, null );
        }
    }
    
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( image1, "The background image." );
        writer.writeProperty( image2, "Another image." );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( image1 ) );
        else if ( loader.loadProperty( image2 ) );
    }
    
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "My own Properties" );
        
        propsCont.addProperty( image1 );
        propsCont.addProperty( image2 );
    }
    
    /*
     * We don't want a border for this Widget.
     */
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    public Lesson4aWidget_Images()
    {
        super( 20.0f, 10.0f );
        
        /*
         * If we don't want a background color for this Widget,
         * we can execute the following line.
         */
        //getBackgroundColorProperty().setValue( null );
    }
}
