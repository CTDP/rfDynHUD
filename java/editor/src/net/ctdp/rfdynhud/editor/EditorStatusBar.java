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

import java.awt.BorderLayout;
import java.awt.Dimension;

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
    
    private final JPanel main = new JPanel();
    
    public final JPanel getMainPanel()
    {
        return ( main );
    }
    
    public EditorStatusBar( EditorRunningIndicator runningIndicator )
    {
        super( new BorderLayout() );
        
        main.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        this.add( main, BorderLayout.CENTER );
        
        JPanel mem = new MemoryPanel( runningIndicator );
        mem.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        mem.setPreferredSize( new Dimension( 150, 20 ) );
        this.add( mem, BorderLayout.EAST );
    }
}
