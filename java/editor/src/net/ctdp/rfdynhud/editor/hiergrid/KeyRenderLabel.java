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
package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public class KeyRenderLabel extends JLabel
{
    private static final long serialVersionUID = 1L;
    
    private int level = 0;
    
    public void setLevel( int level )
    {
        this.level = level;
    }
    
    @Override
    protected void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        
        if ( level > 0 )
        {
            Graphics2D g2 = (Graphics2D)g;
            
            Color oldColor = g2.getColor();
            g2.setColor( Color.BLACK );
            
            int indent = 14;
            
            for ( int i = 0; i < level; i++ )
            {
                int x = ( i + 1 ) * indent - ( indent / 2 );
                
                g2.drawLine( x, 0, x, getHeight() );
            }
            
            g2.setColor( oldColor );
        }
    }
    
    public KeyRenderLabel()
    {
        super();
    }
}
