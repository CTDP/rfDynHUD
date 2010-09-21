/**
 * This piece of code has been provided by and with kind
 * permission of INFOLOG GmbH from Germany.
 * It is released under the terms of the GPL, but INFOLOG
 * is still permitted to use it in closed source software.
 */
package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class keeps some values to style a hierachical table.
 * 
 * @author Marvin Froehlich (CTDP)
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
    
    private Image minusImage = getDefaultCollapseImage();
    private Image plusImage = getDefaultExpandImage();
    
    private Color groupHeaderBackgroundColor = new Color( 168, 193, 255 );
    private Color groupHeaderFontColor = Color.BLACK;
    private Font groupHeaderFont = null;
    
    private Color keyCellFontColor = Color.BLACK;
    private Font keyCellFont = null;
    
    private Color valueCellFontColor = Color.BLACK;
    private Font valueCellFont = null;
    
    public void setTableBackgroundColor( Color color )
    {
        this.tableBackgroundColor = color;
    }
    
    public final Color getTableBackgroundColor()
    {
        return ( tableBackgroundColor );
    }
    
    public void setrowHeight( int rowHeight )
    {
        this.rowHeight = rowHeight;
    }
    
    public final int getRowHeight()
    {
        return ( rowHeight );
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
