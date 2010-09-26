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
package net.ctdp.rfdynhud.editor;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.openmali.types.twodee.Rect2i;

public class WidgetMenuItem extends JMenuItem
{
    private static final long serialVersionUID = -94966687191731871L;
    
    private static final class FakeClock extends Clock
    {
        @Override
        protected void initImpl( long nanoTime )
        {
        }

        @Override
        protected boolean updateImpl( long nanoTime, long frameCounter, boolean force )
        {
            return ( true );
        }
        
        public FakeClock()
        {
            update( 0L, 0L, true );
        }
    }
    
    private static final Clock fakeClock = new FakeClock();
    
    public static final int ICON_WIDTH = 64;
    public static final int ICON_HEIGHT = 50;
    public static final float ICON_ASPECT = (float)ICON_WIDTH / (float)ICON_HEIGHT;
    private static final WidgetsDrawingManager tempWidgetsConfig = new WidgetsDrawingManager( true, 1920, 1200, false );
    
    private final RFDynHUDEditor editor;
    //private final Class<Widget> widgetClass;
    private final Widget widget;
    
    private TextureImage2D texture;
    private boolean iconDrawn = false;
    private Boolean lastCheckState = null;
    
    private static BufferedImage loadCheckImage()
    {
        try
        {
            return ( ImageIO.read( WidgetMenuItem.class.getClassLoader().getResource( "data/widget_menu_check.png" ) ) );
        }
        catch ( IOException e )
        {
            Logger.log( e );
            
            return ( null );
        }
    }
    
    private static final BufferedImage CHECK_IMAGE = loadCheckImage();
    
    @Override
    protected void paintComponent( Graphics g )
    {
        Boolean checkState = Boolean.valueOf( this.isSelected() );
        
        if ( !iconDrawn || ( checkState != lastCheckState ) )
        {
            widget.prepareForMenuItem();
            
            Rect2i innerRect = EditorPanel.getWidgetInnerRect( widget );
            TransformableTexture[] subTextures = widget.getSubTextures( editor.getGameData(), true, innerRect.getWidth(), innerRect.getHeight() );
            
            widget.updateVisibility( editor.getGameData(), true );
            
            texture.clear( true, null );
            widget.drawWidget( fakeClock, true, editor.getGameData(), true, texture, false );
            
            EditorPanel.drawSubTextures( widget, subTextures, texture.getTextureCanvas() );
            
            if ( checkState )
            {
                texture.getTextureCanvas().setClip( (Rect2i)null );
                
                //texture.copyImageDataFrom( CHECK_IMAGE, 0, 0, CHECK_IMAGE.getWidth(), CHECK_IMAGE.getHeight(), 2, texture.getHeight() - 2 - CHECK_IMAGE.getHeight(), CHECK_IMAGE.getWidth(), CHECK_IMAGE.getHeight(), false, false, null );
                texture.getTextureCanvas().drawImage( CHECK_IMAGE, 2, texture.getHeight() - 2 - CHECK_IMAGE.getHeight() );
            }
            
            iconDrawn = true;
            lastCheckState = checkState;
        }
        
        super.paintComponent( g );
    }
    
    public WidgetMenuItem( RFDynHUDEditor editor, Class<Widget> widgetClass, WidgetsConfiguration widgetsConfig )
    {
        super( widgetClass.getSimpleName() );
        
        this.editor = editor;
        
        //this.widgetClass = widgetClass;
        try
        {
            this.widget = RFDynHUDEditor.createWidgetInstance( widgetClass, null, true );
        }
        catch ( Throwable t )
        {
            throw new RuntimeException( t );
        }
        
        if ( widget != null )
        {
            __WCPrivilegedAccess.addWidget( tempWidgetsConfig, widget, false );
            
            float aspect = (float)widget.getSize().getEffectiveWidth() / (float)widget.getSize().getEffectiveHeight();
            
            __WCPrivilegedAccess.removeWidget( tempWidgetsConfig, widget );
            __WCPrivilegedAccess.addWidget( widgetsConfig, widget, false );
            
            if ( aspect > ICON_ASPECT )
                widget.getSize().setEffectiveSize( ICON_WIDTH, (int)( ICON_WIDTH / aspect ) );
            else
                widget.getSize().setEffectiveSize( (int)( ICON_HEIGHT * aspect ), ICON_HEIGHT );
            
            widget.getPosition().setEffectivePosition( RelativePositioning.TOP_LEFT, 0, ( ICON_HEIGHT - widget.getEffectiveHeight() ) / 2 );
        }
        
        this.texture = TextureImage2D.createDrawTexture( ICON_WIDTH, ICON_HEIGHT, true );
        
        this.setIcon( new ImageIcon( texture.getBufferedImage() ) );
    }
}
