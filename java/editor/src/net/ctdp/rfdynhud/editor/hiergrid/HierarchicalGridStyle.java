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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Stroke;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class keeps some values to style a hierachical table.
 * 
 * @author Marvin Froehlich
 */
public class HierarchicalGridStyle
{
    private static Image getDefaultCollapseImage()
    {
        try
        {
            return ( ImageIO.read( HierarchicalGridStyle.class.getClassLoader().getResource( HierarchicalGridStyle.class.getPackage().getName().replace( '.', '/' ) + "/collapse_16x16.gif" ) ) );
        }
        catch ( IOException e )
        {
            throw new Error( e );
        }
    }
    
    private static Image getDefaultExpandImage()
    {
        try
        {
            return ( ImageIO.read( HierarchicalGridStyle.class.getClassLoader().getResource( HierarchicalGridStyle.class.getPackage().getName().replace( '.', '/' ) + "/expand_16x16.gif" ) ) );
        }
        catch ( IOException e )
        {
            throw new Error( e );
        }
    }
    
    private Color tableBackgroundColor = null; //new Color( 212, 208, 200 )
    
    private int rowHeight = 20;
    private int levelIndentation = 14;
    
    private Image minusImage = getDefaultCollapseImage();
    private Image plusImage = getDefaultExpandImage();
    
    private Color groupHeaderBackgroundColor = new Color( 168, 193, 255 );
    private Color groupHeaderFontColor = Color.BLACK;
    private Font groupHeaderFont = null;
    
    private Color keyCellFontColor = Color.BLACK;
    private Font keyCellFont = null;
    
    private Color valueCellFontColor = Color.BLACK;
    private Font valueCellFont = null;
    
    private Color treeLinesColor = Color.BLACK;
    private Stroke treeLinesStroke = new BasicStroke( 1f );
    
    private boolean indentHeaders = true;
    private boolean indentKeyBorders = true;
    
    public void setTableBackgroundColor( Color color )
    {
        this.tableBackgroundColor = color;
    }
    
    public final Color getTableBackgroundColor()
    {
        return ( tableBackgroundColor );
    }
    
    public void setRowHeight( int rowHeight )
    {
        this.rowHeight = rowHeight;
    }
    
    public final int getRowHeight()
    {
        return ( rowHeight );
    }
    
    public void setLevelIndentation( int li )
    {
        this.levelIndentation = li;
    }
    
    public final int getLevelIndentation()
    {
        return ( levelIndentation );
    }
    
    public void setExpandedImage( Image image )
    {
        this.minusImage = image;
    }
    
    public final Image getExpandedImage()
    {
        return ( minusImage );
    }
    
    public void setCollapsedImage( Image image )
    {
        this.plusImage = image;
    }
    
    public final Image getCollapsedImage()
    {
        return ( plusImage );
    }
    
    public void setGroupHeaderBackgroundColor( Color color )
    {
        this.groupHeaderBackgroundColor = color;
    }
    
    public final Color getGroupHeaderBackgroundColor()
    {
        return ( groupHeaderBackgroundColor );
    }
    
    public void setGroupHeaderFontColor( Color color )
    {
        this.groupHeaderFontColor = color;
    }
    
    public final Color getGroupHeaderFontColor()
    {
        return ( groupHeaderFontColor );
    }
    
    public void setGroupHeaderFont( Font font )
    {
        this.groupHeaderFont = font;
    }
    
    public final Font getGroupHeaderFont()
    {
        return ( groupHeaderFont );
    }
    
    public void setKeyCellFontColor( Color color )
    {
        this.keyCellFontColor = color;
    }
    
    public final Color getKeyCellFontColor()
    {
        return ( keyCellFontColor );
    }
    
    public void setKeyCellFont( Font font )
    {
        this.keyCellFont = font;
    }
    
    public final Font getKeyCellFont()
    {
        return ( keyCellFont );
    }
    
    public void setValueCellFontColor( Color color )
    {
        this.valueCellFontColor = color;
    }
    
    public final Color getValueCellFontColor()
    {
        return ( valueCellFontColor );
    }
    
    public void setValueCellFont( Font font )
    {
        this.valueCellFont = font;
    }
    
    public final Font getValueCellFont()
    {
        return ( valueCellFont );
    }
    
    public void setTreeLinesColor( Color color )
    {
        this.treeLinesColor = color;
    }
    
    public final Color getTreeLinesColor()
    {
        return ( treeLinesColor );
    }
    
    public void setTreeLinesStroke( Stroke stroke )
    {
        this.treeLinesStroke = stroke;
    }
    
    public final Stroke getTreeLinesStroke()
    {
        return ( treeLinesStroke );
    }
    
    public void setIndentHeaders( boolean indent )
    {
        this.indentHeaders = indent;
    }
    
    public final boolean getIndentHeaders()
    {
        return ( indentHeaders );
    }
    
    public void setIndentKeyBorders( boolean indent )
    {
        this.indentKeyBorders = indent;
    }
    
    public final boolean getIndentKeyBorders()
    {
        return ( indentKeyBorders );
    }
    
    protected void applyDefaults( HierarchicalTable<?> table )
    {
        Font font = getValueCellFont();
        if ( font == null )
        {
            font = table.getFont();
            setValueCellFont( font );
        }
        
        font = getGroupHeaderFont();
        if ( font == null )
        {
            font = getValueCellFont().deriveFont( table.getFont().getStyle() | Font.BOLD );
            setGroupHeaderFont( font );
        }
        
        font = getKeyCellFont();
        if ( font == null )
        {
            font = getValueCellFont().deriveFont( table.getFont().getStyle() | Font.BOLD );
            setKeyCellFont( font );
        }
    }
    
    public HierarchicalGridStyle()
    {
    }
}
