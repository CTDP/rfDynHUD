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
package net.ctdp.rfdynhud.lessons.widgets.lesson4a;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.lessons.widgets._util.LessonsWidgetSet;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

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
    private final ImageProperty backgroundImage = new ImageProperty( this, "backgroundImage", null, "ctdp-fat-1994.png", false, true );
    
    /*
     * This is a simple image property for an image to be displayed somewhere on the Widget.
     */
    private final ImageProperty image2 = new ImageProperty( this, "image2", "shiftlight_on_red.png" );
    
    /*
     * These two TextureImage2Ds cache the property images in actual size
     * as chosen in the editor.
     */
    private TextureImage2D backgroundTexImage = null;
    private TextureImage2D texImage2 = null;
    
    private DrawnString ds = null;
    
    @Override
    public int getVersion()
    {
        return ( composeVersion( 1, 0, 0 ) );
    }
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( LessonsWidgetSet.WIDGET_PACKAGE );
    }
    
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ds = drawnStringFactory.newDrawnString( "ds", 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        /*
         * This loads the image as defined in the property and gets a scaled instance.
         */
        texImage2 = image2.getImage().getScaledTextureImage( 32, 32, texImage2 );
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        /*
         * If the user selcted no-image, we use the default background.
         */
        if ( backgroundImage.isNoImage() )
        {
            super.clearBackground( gameData, editorPresets, texture, offsetX, offsetY, width, height );
        }
        else
        {
            /*
             * This loads the image as defined in the property and gets a scaled instance using (inner) widget size.
             */
            backgroundTexImage = backgroundImage.getImage().getScaledTextureImage( width, height, backgroundTexImage );
            
            /*
             * And finally we clear the background using out background image (scaled texture).
             */
            texture.clear( backgroundTexImage, offsetX, offsetY, true, null );
        }
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        /*
         * As this method is executed after the clearBackground() method, the background is already
         * cleared with our defined background image.
         */
        
        if ( needsCompleteRedraw )
        {
            /*
             * We need to pass in the background image as clear image, so that the
             * DrawnString knows, what to use to clear the part of the background.
             */
            ds.draw( offsetX, offsetY, "Text on a background image", backgroundTexImage, texture );
            
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
        
        writer.writeProperty( backgroundImage, "The background image." );
        writer.writeProperty( image2, "Another image." );
    }
    
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( backgroundImage.loadProperty( key, value ) );
        else if ( image2.loadProperty( key, value ) );
    }
    
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "My own Properties" );
        
        propsCont.addProperty( backgroundImage );
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
    
    public Lesson4aWidget_Images( String name )
    {
        super( name, 20.0f, 10.0f );
        
        /*
         * If we don't want a background color for this Widget,
         * we can execute the following line.
         */
        //getBackgroundColorProperty().setValue( null );
    }
}
