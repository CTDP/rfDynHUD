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
package net.ctdp.rfdynhud.lessons.widgets.lesson4c;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.lessons.widgets._util.LessonsWidgetSet;
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
 * Here you'll learn, how to rotate a TransformableTexture... pretty straight forward.
 * This also shows how you could use timing to create subtle animations.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Lesson4cWidget_SubTextures extends Widget
{
    /*
     * We need an image for our sub texture.
     */
    private final ImageProperty subImage = new ImageProperty( this, "subImage", "cyan_circle.png" );
    
    /*
     * A sub texture is represented by a TransformableTexture. They are pulled by rfDynHUD in an array,
     * So we create it here. As we know, that we have exactly one texture, we can create a fixed size here.
     */
    private final TransformableTexture[] subTextures = new TransformableTexture[ 1 ];
    private TextureImage2D cache = null;
    
    private final IntValue lapNumber = new IntValue();
    
    private DrawnString lapString = null;
    
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
    
    private void loadSubTextures( boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
    {
        /*
         * We scale the sub texture, so that it definitely fits into the widget area.
		 * Drawing over the edge would be no problem for sub textures, while regular widget contents would be clipped.
         */
        
        int w = Math.max( 10, widgetInnerWidth - 10 );
        int h = Math.max( 10, widgetInnerHeight - 10 );
        int s = Math.min( w, h );
        
        subTextures[0] = subImage.getImage().getScaledTransformableTexture( s, s, subTextures[0], isEditorMode );
        cache = subImage.getImage().getScaledTextureImage( s, s, cache, isEditorMode );
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        loadSubTextures( editorPresets != null, widgetInnerWidth, widgetInnerHeight );
        
        return ( subTextures );
    }
    
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        loadSubTextures( editorPresets != null, width, height );
        
        int h = texture.getStringHeight( "0", getFont(), isFontAntiAliased() );
        lapString = drawnStringFactory.newDrawnString( "lapString", subTextures[0].getWidth() / 2, ( subTextures[0].getHeight() - h ) / 2, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        lapNumber.update( gameData.getScoringInfo().getPlayersVehicleScoringInfo().getCurrentLap() );
        
        subTextures[0].setVisible( true );
        
        float t3 = ( ( gameData.getScoringInfo().getSessionNanos() % 3000000000L ) / 3000000000.0f );
        
        /*
         * Rotate at a frequency of 3 seconds.
         * We need to update the rotation each frame.
         * This is NOT performance critical.
         */
        subTextures[0].setRotation( TransformableTexture.TWO_PI * t3 );
        
        float t15 = ( ( gameData.getScoringInfo().getSessionNanos() % 1500000000L ) / 1500000000.0f );
        
        /*
         * And we add a little special effect through scaling.
         */
        float scale = 1.0f + 0.2f * (float)Math.sin( t15 * TransformableTexture.PI );
        
        subTextures[0].setScale( scale, scale );
        
        /*
         * Set the rotation center to the center of the texture.
         */
        subTextures[0].setRotationCenter( (int)( subTextures[0].getWidth() * scale ) / 2, (int)( subTextures[0].getHeight() * scale ) / 2 );
        
        /*
         * And finally move the texture to the center of the Widget.
         */
        subTextures[0].setTranslation( ( width - (int)( subTextures[0].getWidth() * scale ) ) / 2, ( height - (int)( subTextures[0].getHeight() * scale ) ) / 2 );
        
        if ( needsCompleteRedraw || ( clock1 && lapNumber.hasChanged() ) )
        {
            lapString.draw( 0, 0, lapNumber.getValueAsString(), cache, subTextures[0].getTexture() );
        }
    }
    
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( subImage, "An image." );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( subImage ) );
    }
    
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "My own Properties" );
        
        propsCont.addProperty( subImage );
    }
    
    public Lesson4cWidget_SubTextures( String name )
    {
        super( name, 10.0f, 10.0f );
        
        getFontColorProperty().setColor( "#000000" );
    }
}
