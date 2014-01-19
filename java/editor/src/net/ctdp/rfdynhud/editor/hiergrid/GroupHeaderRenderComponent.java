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
package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

/**
 * @author Marvin Froehlich
 */
public class GroupHeaderRenderComponent extends JPanel
{
    private static final long serialVersionUID = 1L;
    
    private int level = 0;
    private int indent = 14;
    private boolean[] lastInGroup = null;
    private boolean expanded = false;
    private String caption = "";
    
    public void setLevel( int level, int indent )
    {
        this.level = level;
        this.indent = indent;
    }
    
    public void setLastInGroup( boolean[] lig )
    {
        this.lastInGroup = lig;
    }
    
    public void setExpanded( boolean expanded )
    {
        this.expanded = expanded;
    }
    
    public void setCaption( String caption )
    {
        this.caption = caption;
    }
    
    public final String getCaption()
    {
        return ( caption );
    }
    
    @Override
    protected void paintComponent( Graphics g )
    {
        HierarchicalTable<?> table = (HierarchicalTable<?>)this.getParent().getParent();
        
        Color bgc = getBackground();
        Color fgc = getForeground();
        setBackground( table.getBackground() );
        
        super.paintComponent( g );
        
        setBackground( bgc );
        
        Graphics2D g2 = (Graphics2D)g;
        
        g2.setColor( bgc );
        int offsetX = table.getStyle().getIndentHeaders() ? level * indent : 0;
        g2.fillRect( offsetX, 0, getWidth(), getHeight() );
        
        g2.setColor( fgc );
        
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D bounds = fm.getStringBounds( caption, g2 );
        
        int x = offsetX + ( getWidth() - offsetX - (int)bounds.getWidth() ) / 2;
        int y = ( getHeight() - (int)bounds.getHeight() ) / 2 - (int)bounds.getY();
        
        g2.drawString( caption, x, y );
        
        if ( level > 0 )
        {
            g2.setColor( table.getStyle().getTreeLinesColor() );
            
            Stroke oldStroke = g2.getStroke();
            g2.setStroke( table.getStyle().getTreeLinesStroke() );
            
            for ( int i = 1; i <= level; i++ )
            {
                x = i * indent - ( indent / 2 );
                
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
            
            g2.setStroke( oldStroke );
        }
    }
    
    public GroupHeaderRenderComponent()
    {
        super();
    }
}
