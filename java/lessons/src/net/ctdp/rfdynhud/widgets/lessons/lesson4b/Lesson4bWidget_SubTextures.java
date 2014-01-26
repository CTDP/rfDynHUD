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
package net.ctdp.rfdynhud.widgets.lessons.lesson4b;

import java.awt.Font;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.lessons._util.LessonsWidgetSet;

/**
 * This Widget shows, how to use custom images as sub textures.
 * Sub textures can be positioned anywhere even outside of the Widget area
 * and define and overlay an arbitrary region on the screen.
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
    private final ImagePropertyWithTexture subImage = new ImagePropertyWithTexture( "subImage", "standard/cyan_circle.png" );
    
    private final ColorProperty myFontColor = new ColorProperty( "myFontColor", "#000000" );
    
    /*
     * A sub texture is represented by a TransformableTexture.
     */
    private TransformableTexture subTexture = null;
    
    private final IntValue lapNumber = new IntValue();
    
    private DrawnString ds = null;
    private DrawnString lapString = null;
    
    public Lesson4bWidget_SubTextures()
    {
        super( LessonsWidgetSet.INSTANCE, LessonsWidgetSet.WIDGET_PACKAGE, 27.0f, 6.0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        getFontProperty().setFont( "Dialog", Font.PLAIN, 6, false, true );
    }
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
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
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "My own Properties" );
        
        propsCont.addProperty( subImage );
        propsCont.addProperty( myFontColor );
    }
    
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
        
        lapNumber.reset();
    }
    
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        /*
         * This loads the image as defined in the property and gets a scaled instance as a TransformableTexture.
         */
        subTexture = subImage.getImage().getScaledTransformableTexture( 32, 32, subTexture, isEditorMode );
        subImage.updateSize( 32, 32, isEditorMode );
        
        collector.add( subTexture );
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        /*
         * Just to play around with the parameters we define the text to be drawn at the center location this time.
         */
        int h = TextureImage2D.getStringHeight( "Ay", getFont(), isFontAntiAliased() );
        ds = drawnStringFactory.newDrawnString( "ds", width / 2, ( height - h ) / 2, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        lapString = drawnStringFactory.newDrawnString( "lapString", subTexture.getWidth() / 2, ( subTexture.getHeight() - h ) / 2, Alignment.CENTER, false, getFont(), isFontAntiAliased(), myFontColor.getColor() );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw )
        {
            ds.draw( offsetX, offsetY, "Quick brown fox jumps over the lazy dog.", texture );
        }
        
        lapNumber.update( gameData.getScoringInfo().getPlayersVehicleScoringInfo().getCurrentLap() );
        
        if ( needsCompleteRedraw || ( clock.c() && lapNumber.hasChanged() ) )
        {
            subTexture.setVisible( true ); // Yes, it is visible. ;)
            
            /*
             * The position of the sub texture is always relative to the Widget's position.
             * So -16, -16 is half outside of the Widget.
             * Note, that the editor is currently not able to render the outside parts of subtextures.
             * But it is display just fine ingame.
             */
            subTexture.setTranslation( -16, -16 );
            
            /*
             * We draw the lap number on the sub texture using our cached texture as clear image.
             */
            lapString.draw( 0, 0, lapNumber.getValueAsString(), subImage.getTexture(), subTexture.getTexture() );
        }
    }
}
