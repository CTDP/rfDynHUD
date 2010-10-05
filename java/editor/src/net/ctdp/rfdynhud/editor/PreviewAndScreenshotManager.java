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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.ctdp.rfdynhud.editor.util.AvailableDisplayModes;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.GameResolution;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.Tools;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class PreviewAndScreenshotManager
{
    public static void takeScreenshot( WidgetsEditorPanel editorPanel, GameResolution gameResolution, File currentConfigFile )
    {
        BufferedImage img = new BufferedImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight(), BufferedImage.TYPE_3BYTE_BGR );
        
        boolean gridSuppressed = false;
        
        if ( editorPanel.getSettings().getDrawGrid() && ( editorPanel.getSettings().getGridSizeX() > 1 ) && ( editorPanel.getSettings().getGridSizeY() > 1 ) )
        {
            editorPanel.getSettings().setDrawGrid( false );
            
            gridSuppressed = true;
        }
        
        editorPanel.drawWidgets( img.createGraphics(), true, false );
        
        if ( gridSuppressed )
        {
            editorPanel.getSettings().setDrawGrid( true );
        }
        
        editorPanel.repaint();
        editorPanel.setSelectedWidget( editorPanel.getSelectedWidget(), false );
        
        try
        {
            File folder = GameFileSystem.INSTANCE.getGameScreenshotsFolder();
            folder.mkdirs();
            
            String filenameBase = ( currentConfigFile == null ) ? "rfDynHUD_screenshot_" : "rfDynHUD_" + currentConfigFile.getName().replace( ".", "_" ) + "_";
            int i = 0;
            File f = new File( folder, filenameBase + Tools.padLeft( i, 3, "0" ) + ".png" );
            while ( f.exists() )
            {
                i++;
                f = new File( folder, filenameBase + Tools.padLeft( i, 3, "0" ) + ".png" );
            }
            
            Logger.log( "Saving screenshot to file " + f.getPath() );
            
            ImageIO.write( img, "PNG", f );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    public static void showFullscreenPreview( final JFrame mainWindow, final WidgetsEditorPanel editorPanel, final GameResolution gameResolution, final File currentConfigFile )
    {
        Logger.log( "Showing fullscreen preview" );
        
        final GraphicsDevice graphDev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        final DisplayMode desktopDM = graphDev.getDisplayMode();
        
        JPanel p = new JPanel()
        {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent( Graphics g )
            {
                editorPanel.drawWidgets( (Graphics2D)g, true, false );
            }
        };
        p.setBackground( Color.BLACK );
        
        DisplayMode dm = AvailableDisplayModes.getDisplayMode( gameResolution.getResolutionString() );
        
        //boolean isSameMode = dm.equals( desktopDM );
        boolean isSameMode = ( ( dm.getWidth() == desktopDM.getWidth() ) && ( dm.getHeight() == desktopDM.getHeight() ) );
        java.awt.Window w;
        //if ( isSameMode )
        {
            javax.swing.JDialog d = new javax.swing.JDialog( mainWindow, isSameMode );
            
            d.setUndecorated( true );
            d.setSize( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() );
            d.setResizable( false );
            d.setContentPane( p );
            
            w = d;
        }
        /*
        else
        {
            java.awt.Dialog d = new java.awt.Dialog( getMainWindow() );
            //java.awt.Frame d = new java.awt.Frame();
            
            d.setLayout( new java.awt.GridLayout( 1, 1 ) );
            d.add( p );
            d.setUndecorated( true );
            d.setSize( gameResX, gameResY );
            d.setResizable( false );
            
            w = d;
        }
        */
        
        w.setName( "preview frame" );
        w.setBackground( Color.BLACK );
        w.setLocation( 0, 0 );
        
        Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener()
        {
            @Override
            public void eventDispatched( AWTEvent event )
            {
                KeyEvent kev = (KeyEvent)event;
                
                if ( kev.getID() == KeyEvent.KEY_PRESSED )
                {
                    if ( ( kev.getKeyCode() == KeyEvent.VK_ESCAPE ) || ( kev.getKeyCode() == KeyEvent.VK_F11 ) )
                    {
                        Toolkit.getDefaultToolkit().removeAWTEventListener( this );
                        java.awt.Window w = null;
                        if ( event.getSource() instanceof java.awt.Window )
                            w = (java.awt.Window)event.getSource();
                        else if ( "preview frame".equals( ( (JComponent)event.getSource() ).getRootPane().getParent().getName() ) )
                            w = (java.awt.Window)( (JComponent)event.getSource() ).getRootPane().getParent();
                        
                        //graphDev.setDisplayMode( desktopDM );
                        
                        if ( graphDev.getFullScreenWindow() == w )
                            graphDev.setFullScreenWindow( null );
                        
                        //w.setVisible( false );
                        w.dispose();
                    }
                    else if ( kev.getKeyCode() == KeyEvent.VK_F12 )
                    {
                        takeScreenshot( editorPanel, gameResolution, currentConfigFile );
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK );
        
        w.addWindowListener( new WindowAdapter()
        {
            private boolean gridSuppressed = false;
            
            @Override
            public void windowOpened( WindowEvent e )
            {
                if ( editorPanel.getSettings().getDrawGrid() && ( editorPanel.getSettings().getGridSizeX() > 1 ) && ( editorPanel.getSettings().getGridSizeY() > 1 ) )
                {
                    editorPanel.getSettings().setDrawGrid( false );
                    
                    gridSuppressed = true;
                }
            }
            
            @Override
            public void windowClosed( WindowEvent e )
            {
                Logger.log( "Closing fullscreen preview" );
                
                if ( gridSuppressed )
                {
                    editorPanel.getSettings().setDrawGrid( true );
                }
                
                editorPanel.repaint();
                editorPanel.setSelectedWidget( editorPanel.getSelectedWidget(), false );
            }
        } );
        
        if ( isSameMode )
        {
            w.setVisible( true );
        }
        else
        {
            graphDev.setFullScreenWindow( w );
            graphDev.setDisplayMode( dm );
        }
    }
}
