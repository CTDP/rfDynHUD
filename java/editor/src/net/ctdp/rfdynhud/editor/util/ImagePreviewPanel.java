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
package net.ctdp.rfdynhud.editor.util;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * Shows a full size preview of an image;
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImagePreviewPanel extends JPanel
{
    private static final long serialVersionUID = -5645174570084578918L;
    
    private BufferedImage image = null;
    
    public void setImage( File imageFile )
    {
        if ( imageFile == null )
        {
            image = null;
        }
        else
        {
            try
            {
                image = ImageIO.read( imageFile );
            }
            catch ( Throwable t )
            {
                RFDHLog.exception( t );
                
                image = null;
            }
        }
    }
    
    @Override
    protected void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        
        if ( image != null )
        {
            int maxWidth = getWidth() - 2;
            int maxHeight = getHeight() - 2;
            
            if ( ( image.getWidth() > maxWidth ) || ( image.getHeight() > maxHeight ) )
            {
                float scaleX = maxWidth / (float)image.getWidth();
                float scaleY = maxHeight / (float)image.getHeight();
                float scale = Math.min( scaleX, scaleY );
                
                int width = Math.min( Math.round( image.getWidth() * scale ), maxWidth );
                int height = Math.min( Math.round( image.getHeight() * scale ), maxHeight );
                
                g.drawImage( image, ( getWidth() - width ) / 2, ( getHeight() - height ) / 2, width, height, null );
            }
            else
            {
                g.drawImage( image, ( getWidth() - image.getWidth() ) / 2, ( getHeight() - image.getHeight() ) / 2, null );
            }
        }
    }
}
