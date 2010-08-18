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
package net.ctdp.rfdynhud.lessons.widgets.lesson4b;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.lessons.widgets._util.LessonsWidgetSet;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * This Widget shows, how to use custom images as sub textures.
 * Sub textures can be drawn anywhere even outside of the Widget area.
 * They are always drawn after the Widget itself and can hence overpaint
 * parts of the Widget without problems.
 * You can draw on Subtextures the same way as on the Widget area itself.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Lesson4bWidget_SubTextures extends Widget
{
    /*
     * We need an image for our sub texture.
     */
    private final ImageProperty subImage = new ImageProperty( this, "subImage", "cyan_circle.png" );
    
    private final ColorProperty myFontColor = new ColorProperty( this, "myFontColor", "#000000" );
    
    /*
     * A sub texture is represented by a TransformableTexture. They are pulled by rfDynHUD in an array,
     * So we create it here. As we know, that we have exactly one texture, we can create a fixed size here.
     */
    private final TransformableTexture[] subTextures = new TransformableTexture[ 1 ];
    private TextureImage2D cache = null;
    
    private final IntValue lapNumber = new IntValue();
    
    private DrawnString ds = null;
    private DrawnString lapString = null;
    
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
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        lapNumber.reset();
    }
    
    private void loadSubTextures()
    {
        /*
         * This loads the image as defined in the property and gets a scaled instance as a TransformableTexture.
         */
        subTextures[0] = subImage.getImage().getScaledTransformableTexture( 32, 32, subTextures[0] );
        cache = subImage.getImage().getScaledTextureImage( 32, 32, cache );
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        loadSubTextures();
        
        return ( subTextures );
    }
    
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        loadSubTextures();
        
        /*
         * Just to play around with the parameters we define the text to be drawn at hte center location this time.
         */
        int h = texture.getStringHeight( "Ay", getFont(), isFontAntiAliased() );
        ds = drawnStringFactory.newDrawnString( "ds", width / 2, ( height - h ) / 2, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        lapString = drawnStringFactory.newDrawnString( "lapString", subTextures[0].getWidth() / 2, ( subTextures[0].getHeight() - h ) / 2, Alignment.CENTER, false, getFont(), isFontAntiAliased(), myFontColor.getColor() );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw )
        {
            ds.draw( offsetX, offsetY, "Quick brown fox jumps over the lazy dog.", texture );
        }
        
        lapNumber.update( gameData.getScoringInfo().getPlayersVehicleScoringInfo().getCurrentLap() );
        
        if ( needsCompleteRedraw || ( clock1 && lapNumber.hasChanged() ) )
        {
            subTextures[0].setVisible( true ); // Yes, it is visible. ;)
            
            /*
             * The position of the sub texture is always relative to the Widget's position.
             * So -16, -16 is half outside of the Widget.
             * Note, that the editor is currently not able to render the outside parts of subtextures.
             * But it is display just fine in game.
             */
            subTextures[0].setTranslation( -16, -16 );
            
            /*
             * We draw the lap number on the sub texture using our cached texture as clear image.
             */
            lapString.draw( 0, 0, lapNumber.getValueAsString(), cache, subTextures[0].getTexture() );
        }
    }
    
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( subImage, "An image." );
        writer.writeProperty( myFontColor, "Font color for my custom text." );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( subImage ) );
        else if ( loader.loadProperty( myFontColor ) );
    }
    
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "My own Properties" );
        
        propsCont.addProperty( subImage );
        propsCont.addProperty( myFontColor );
    }
    
    public Lesson4bWidget_SubTextures( String name )
    {
        super( name, 27.0f, 6.0f );
    }
}
