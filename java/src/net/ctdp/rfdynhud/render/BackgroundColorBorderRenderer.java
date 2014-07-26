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
package net.ctdp.rfdynhud.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BackgroundColorBorderRenderer implements BorderRenderer
{
    private final Map<Color, TextureImage2D> cache = new HashMap<Color, TextureImage2D>();
    
    private TextureImage2D createBorder( Color backgroundColor )
    {
        TextureImage2D borderTexture = TextureImage2D.createDrawTexture( 32, 32, true );
        borderTexture.clear( false, null );
        
        Texture2DCanvas texCanvas = borderTexture.getTextureCanvas();
        
        texCanvas.setColor( backgroundColor );
        texCanvas.setAntialiazingEnabled( true );
        texCanvas.fillArc( 0, 0, 32, 32, 0, 360 );
        
        return ( borderTexture );
    }
    
    /**
     * 
     * @param backgroundColor
     * @return the background image for the specified color.
     */
    public TextureImage2D getImage( Color backgroundColor )
    {
        TextureImage2D borderTexture = cache.get( backgroundColor );
        if ( borderTexture == null )
        {
            borderTexture = createBorder( backgroundColor );
            cache.put( backgroundColor, borderTexture );
        }
        
        return ( borderTexture );
    }
    
    @Override
    public void drawBorder( Color backgroundColor, BorderMeasures measures, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        //if ( ( backgroundColor == null ) || ( backgroundColor.getAlpha() == 255 ) )
        //    return;
        
        if ( backgroundColor == null )
            backgroundColor = Color.MAGENTA;
        
        TextureImage2D borderTexture = getImage( backgroundColor );
        
        ImageBorderRenderer.drawBorderFromTexture( borderTexture, measures, texture, offsetX, offsetY, width, height );
    }
}
