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
package net.ctdp.rfdynhud.widgets.revmeter;

import java.io.IOException;

import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;

public class ShiftLight
{
    private static final String[] default_shift_light_on_images =
    {
        "shiftlight_on_red.png",
        "shiftlight_on_orange.png",
        "shiftlight_on_yellow.png",
        "shiftlight_on_lightgreen.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
        "shiftlight_on_green.png",
    };
    
    public static final ShiftLight DEFAULT_SHIFT_LIGHT1 = new ShiftLight( null, 1 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT2 = new ShiftLight( null, 2 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT3 = new ShiftLight( null, 3 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT4 = new ShiftLight( null, 4 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT5 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT6 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT7 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT8 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT9 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT10 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT11 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT12 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT13 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT14 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT15 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT16 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT17 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT18 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT19 = new ShiftLight( null, 5 );
    public static final ShiftLight DEFAULT_SHIFT_LIGHT20 = new ShiftLight( null, 5 );
    
    private final RevMeterWidget widget;
    private final int indexOneBased;
    
    private final ImageProperty imageNameOff;
    private final ImageProperty imageNameOn;
    private final IntProperty posX;
    private final IntProperty posY;
    final IntProperty activationRPM;
    
    private TransformableTexture textureOff = null;
    private TransformableTexture textureOn = null;
    
    public void resetTextures()
    {
        this.textureOff = null;
        this.textureOn = null;
    }
    
    public void onBackgroundChanged( float deltaScaleX, float deltaScaleY )
    {
        resetTextures();
        
        posX.setIntValue( Math.round( posX.getIntValue() * deltaScaleX ) );
        posY.setIntValue( Math.round( posY.getIntValue() * deltaScaleY ) );
    }
    
    private final boolean isOffStatePartOfBackground()
    {
        return ( imageNameOff.getValue().equals( "" ) );
    }
    
    public int loadTextures( boolean isEditorMode )
    {
        int n = 0;
        
        if ( !isOffStatePartOfBackground() )
        {
            try
            {
                ImageTemplate it = imageNameOff.getImage();
                
                if ( it == null )
                {
                    textureOff = null;
                    return ( n );
                }
                
                float scale = widget.getBackground().getScaleX();
                int w = Math.round( it.getBaseWidth() * scale );
                int h = Math.round( it.getBaseHeight() * scale );
                if ( ( textureOff == null ) || ( textureOff.getWidth() != w ) || ( textureOff.getHeight() != h ) )
                {
                    textureOff = TransformableTexture.getOrCreate( w, h, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, textureOff, isEditorMode );
                    it.drawScaled( 0, 0, w, h, textureOff.getTexture(), true );
                }
                
                n++;
            }
            catch ( Throwable t )
            {
                Logger.log( t );
                
                return ( n );
            }
        }
        else if ( isOffStatePartOfBackground() )
        {
            textureOff = null;
        }
        
        try
        {
            ImageTemplate it = imageNameOn.getImage();
            
            if ( it == null )
            {
                textureOn = null;
                return ( n );
            }
            
            float scale = widget.getBackground().getScaleX();
            int w = Math.round( it.getBaseWidth() * scale );
            int h = Math.round( it.getBaseHeight() * scale );
            if ( isOffStatePartOfBackground() )
            {
                if ( ( textureOn == null ) || ( textureOn.getWidth() != w ) || ( textureOn.getHeight() != h * 2 ) )
                {
                    textureOn = TransformableTexture.getOrCreate( w, h * 2, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, textureOn, isEditorMode );
                    // TODO: Move this code to drawBackground() of RevMeterWidget!
                    if ( widget.getBackgroundProperty().getBackgroundType().isImage() )
                    {
                        ImageTemplate it0 = widget.getBackgroundProperty().getImageValue();
                        textureOn.getTexture().clear( false, null );
                        it0.drawScaled( posX.getIntValue(), posY.getIntValue(), it.getBaseWidth(), it.getBaseHeight(), 0, 0, w, h, textureOn.getTexture(), false );
                        it0.drawScaled( posX.getIntValue(), posY.getIntValue(), it.getBaseWidth(), it.getBaseHeight(), 0, h, w, h, textureOn.getTexture(), false );
                    }
                    else if ( widget.getBackgroundProperty().getBackgroundType().isColor() )
                    {
                        textureOn.getTexture().clear( widget.getBackground().getColor(), true, null );
                    }
                    it.drawScaled( 0, 0, w, h, textureOn.getTexture(), false );
                }
            }
            else
            {
                if ( ( textureOn == null ) || ( textureOn.getWidth() != w ) || ( textureOn.getHeight() != h ) )
                {
                    textureOn = TransformableTexture.getOrCreate( w, h, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, textureOn, isEditorMode );
                    it.drawScaled( 0, 0, w, h, textureOn.getTexture(), true );
                }
            }
            
            n++;
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( n );
        }
        
        return ( n );
    }
    
    public int writeTexturesToArray( TransformableTexture[] array, int offset )
    {
        if ( textureOff != null )
            array[offset++] = textureOff;
        
        if ( textureOn != null )
            array[offset++] = textureOn;
        
        return ( offset );
    }
    
    public void updateTextures( float rpm, float maxRPM, float backgroundScaleX, float backgroundScaleY )
    {
        boolean isOn = ( rpm >= maxRPM + activationRPM.getIntValue() );
        
        if ( isOffStatePartOfBackground() )
        {
            if ( textureOn != null )
            {
                if ( isOn )
                {
                    textureOn.setClipRect( 0, 0, textureOn.getWidth(), textureOn.getHeight() / 2, true );
                    textureOn.setTranslation( Math.round( posX.getIntValue() * backgroundScaleX ), Math.round( posY.getIntValue() * backgroundScaleY ) );
                }
                else
                {
                    textureOn.setClipRect( 0, textureOn.getHeight() / 2, textureOn.getWidth(), textureOn.getHeight() / 2, true );
                    textureOn.setTranslation( Math.round( posX.getIntValue() * backgroundScaleX ), Math.round( posY.getIntValue() * backgroundScaleY ) - textureOn.getHeight() / 2 );
                }
            }
        }
        else
        {
            if ( isOn )
            {
                if ( textureOn != null )
                {
                    textureOn.setClipRect( 0, 0, textureOn.getWidth(), textureOn.getHeight(), true );
                    textureOn.setTranslation( Math.round( posX.getIntValue() * backgroundScaleX ), Math.round( posY.getIntValue() * backgroundScaleY ) );
                    textureOn.setVisible( true );
                }
                if ( textureOff != null )
                    textureOff.setVisible( false );
            }
            else
            {
                if ( textureOff != null )
                {
                    textureOff.setClipRect( 0, 0, textureOff.getWidth(), textureOff.getHeight(), true );
                    textureOff.setTranslation( Math.round( posX.getIntValue() * backgroundScaleX ), Math.round( posY.getIntValue() * backgroundScaleY ) );
                    textureOff.setVisible( true );
                }
                if ( textureOn != null )
                    textureOn.setVisible( false );
            }
        }
    }
    
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( imageNameOff, "The name of the shift light image for \"off\" state." );
        writer.writeProperty( imageNameOn, "The name of the shift light image for \"on\" state." );
        writer.writeProperty( posX, "The x-offset in pixels to the gear label." );
        writer.writeProperty( posY, "The y-offset in pixels to the gear label." );
        writer.writeProperty( activationRPM, "The RPM (rounds per minute) to subtract from the maximum for the level to display shoft light on" );
    }
    
    public boolean loadProperty( PropertyLoader loader )
    {
        if ( loader.loadProperty( imageNameOff ) )
            return ( true );
        if ( loader.loadProperty( imageNameOn ) )
            return ( true );
        if ( loader.loadProperty( posX ) )
            return ( true );
        if ( loader.loadProperty( posY ) )
            return ( true );
        if ( loader.loadProperty( activationRPM ) )
            return ( true );
        
        return ( false );
    }
    
    /**
     * 
     * @param propsCont
     * @param forceAll
     */
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( "Shift Light " + indexOneBased );
        
        propsCont.addProperty( imageNameOff );
        propsCont.addProperty( imageNameOn );
        propsCont.addProperty( posX );
        propsCont.addProperty( posY );
        propsCont.addProperty( activationRPM );
    }
    
    public ShiftLight( RevMeterWidget widget, int indexOneBased )
    {
        this.widget = widget;
        this.indexOneBased = indexOneBased;
        
        this.imageNameOff = new ImageProperty( widget, "shiftLightImageNameOff" + indexOneBased, "imageNameOff", "shiftlight_off.png", false, true )
        {
            @Override
            protected void onValueChanged( String oldValue, String newValue )
            {
                textureOff = null;
            }
        };
        this.imageNameOn = new ImageProperty( widget, "shiftLightImageNameOn" + indexOneBased, "imageNameOn", default_shift_light_on_images[indexOneBased - 1] )
        {
            @Override
            protected void onValueChanged( String oldValue, String newValue )
            {
                textureOn = null;
            }
        };
        this.posX = new IntProperty( widget, "shiftLightPosX" + indexOneBased, "posX", 625 - 32 * ( indexOneBased - 1 ) )
        {
            @Override
            protected void onValueChanged( int oldValue, int newValue )
            {
                resetTextures();
            }
        };
        this.posY = new IntProperty( widget, "shiftLightPosY" + indexOneBased, "posY", 42 )
        {
            @Override
            protected void onValueChanged( int oldValue, int newValue )
            {
                resetTextures();
            }
        };
        this.activationRPM = new IntProperty( widget, "shiftLightRPM" + indexOneBased, "activationRPM", 100 - 250 * indexOneBased, -5000, 0, false );
    }
}
