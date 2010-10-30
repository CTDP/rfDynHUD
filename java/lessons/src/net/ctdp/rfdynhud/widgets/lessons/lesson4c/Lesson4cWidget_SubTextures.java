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
package net.ctdp.rfdynhud.widgets.lessons.lesson4c;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.lessons._util.LessonsWidgetSet;

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
    private final ImagePropertyWithTexture subImage = new ImagePropertyWithTexture( this, "subImage", "cyan_circle.png" );
    
    /*
     * A sub texture is represented by a TransformableTexture.
     */
    private TransformableTexture subTexture = null;
    
    private final IntValue lapNumber = new IntValue();
    
    private DrawnString lapString = null;
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( LessonsWidgetSet.WIDGET_PACKAGE );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        lapNumber.reset();
    }
    
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        /*
         * We scale the sub texture, so that it definitely fits into the widget area.
         * Drawing over the edge would be no problem for sub textures, while regular widget contents would be clipped.
         */
        
        int w = Math.max( 10, widgetInnerWidth - 10 );
        int h = Math.max( 10, widgetInnerHeight - 10 );
        int s = Math.min( w, h );
        
        subTexture = subImage.getImage().getScaledTransformableTexture( s, s, subTexture, isEditorMode );
        subImage.updateSize( s, s, isEditorMode );
        
        collector.add( subTexture );
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        int h = TextureImage2D.getStringHeight( "0", getFont(), isFontAntiAliased() );
        lapString = drawnStringFactory.newDrawnString( "lapString", subTexture.getWidth() / 2, ( subTexture.getHeight() - h ) / 2, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        lapNumber.update( gameData.getScoringInfo().getPlayersVehicleScoringInfo().getCurrentLap() );
        
        subTexture.setVisible( true );
        
        float t3 = ( ( gameData.getScoringInfo().getSessionNanos() % 3000000000L ) / 3000000000.0f );
        
        /*
         * Rotate at a frequency of 3 seconds.
         * We need to update the rotation each frame.
         * This is NOT performance critical.
         */
        subTexture.setRotation( TransformableTexture.TWO_PI * t3 );
        
        float t15 = ( ( gameData.getScoringInfo().getSessionNanos() % 1500000000L ) / 1500000000.0f );
        
        /*
         * And we add a little special effect through scaling.
         */
        float scale = 1.0f + 0.2f * (float)Math.sin( t15 * TransformableTexture.PI );
        
        subTexture.setScale( scale, scale );
        
        /*
         * Set the rotation center to the center of the texture.
         */
        subTexture.setRotationCenter( (int)( subTexture.getWidth() * scale ) / 2, (int)( subTexture.getHeight() * scale ) / 2 );
        
        /*
         * And finally move the texture to the center of the Widget.
         */
        subTexture.setTranslation( ( width - (int)( subTexture.getWidth() * scale ) ) / 2, ( height - (int)( subTexture.getHeight() * scale ) ) / 2 );
        
        if ( needsCompleteRedraw || ( clock.c() && lapNumber.hasChanged() ) )
        {
            lapString.draw( 0, 0, lapNumber.getValueAsString(), subImage.getTexture(), subTexture.getTexture() );
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
    
    public Lesson4cWidget_SubTextures()
    {
        super( 10.0f, 10.0f );
        
        getFontColorProperty().setColor( "#000000" );
    }
}
