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
package net.ctdp.rfdynhud.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * The status bar displays important status information at the bottom of the editor window.
 * 
 * @author Marvn Froehlich
 */
public class EditorStatusBar extends JPanel
{
    private static final long serialVersionUID = -325500570902054489L;
    
    private final JLabel statusLabel;
    private final JLabel zoomLabel;
    private final MemoryPanel memoryPanel;
    
    public final MemoryPanel getMemoryPanel()
    {
        return ( memoryPanel );
    }
    
    public void setZoomLevel( float zoomLevel )
    {
        zoomLabel.setText( "Zoom: " + Math.round( zoomLevel * 100 ) + "%" );
    }
    
    public void setStatusText( String text )
    {
        statusLabel.setText( text );
    }
    
    public EditorStatusBar( EditorRunningIndicator runningIndicator )
    {
        super( new BorderLayout() );
        
        final int height = 20;
        
        this.memoryPanel = new MemoryPanel( runningIndicator );
        memoryPanel.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        memoryPanel.setPreferredSize( new Dimension( 150, height ) );
        this.add( memoryPanel, BorderLayout.EAST );
        
        JPanel main2 = new JPanel( new BorderLayout() );
        
        this.zoomLabel = new JLabel();
        zoomLabel.setToolTipText( "The current zoom level. CTRL+Mouse Wheel to change, CTRL+Wheel Click to reset." );
        setZoomLevel( 1.0f );
        zoomLabel.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        zoomLabel.setPreferredSize( new Dimension( 80, height ) );
        main2.add( zoomLabel, BorderLayout.EAST );
        
        this.add( main2, BorderLayout.CENTER );
        
        this.statusLabel = new JLabel();
        statusLabel.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        main2.add( statusLabel, BorderLayout.CENTER );
    }
}
