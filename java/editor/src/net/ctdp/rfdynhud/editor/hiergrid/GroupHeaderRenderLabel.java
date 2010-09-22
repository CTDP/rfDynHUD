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
 * @author Marvin Froehlich
 */
public class GroupHeaderRenderLabel extends JLabel
{
    private static final long serialVersionUID = 1L;
    
    private int level = 0;
    private boolean[] lastInGroup = null;
    private boolean expanded = false;
    
    public void setLevel( int level )
    {
        this.level = level;
    }
    
    public void setLastInGroup( boolean[] lig )
    {
        this.lastInGroup = lig;
    }
    
    public void setExpanded( boolean expanded )
    {
        this.expanded = expanded;
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
            
            for ( int i = 1; i <= level; i++ )
            {
                int x = i * indent - ( indent / 2 );
                
                boolean lig = lastInGroup[i];
                if ( i < level )
                    lig = lig && lastInGroup[level];
                
                if ( lig && !expanded )
                {
                    g2.drawLine( x, 0, x, getHeight() - 4 );
                    g2.drawLine( x, getHeight() - 4, x + 2, getHeight() - 4 );
                }
                else
                {
                    g2.drawLine( x, 0, x, getHeight() );
                }
            }
            
            g2.setColor( oldColor );
        }
    }
    
    public GroupHeaderRenderLabel()
    {
        super();
    }
}
