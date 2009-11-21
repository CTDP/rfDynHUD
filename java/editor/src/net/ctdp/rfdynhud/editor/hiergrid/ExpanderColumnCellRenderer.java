package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public class ExpanderColumnCellRenderer extends JPanel implements TableCellRenderer
{
    private static final long serialVersionUID = 2232413953910659262L;
    
    private final Image minusImage;
    private final Image plusImage;
    
    private Boolean value = null;
    
    @Override
    protected void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        
        if ( value != null )
        {
            if ( value.booleanValue() )
            {
                int x = ( this.getWidth() - minusImage.getWidth( this ) ) / 2;
                int y = ( this.getHeight() - minusImage.getHeight( this ) ) / 2;
                
                g.drawImage( minusImage, x, y, this );
            }
            else
            {
                int x = ( this.getWidth() - plusImage.getWidth( this ) ) / 2;
                int y = ( this.getHeight() - plusImage.getHeight( this ) ) / 2;
                
                g.drawImage( plusImage, x, y, this );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        if ( value == null )
        {
            this.value = null;
        }
        else
        {
            this.value = (Boolean)value;
        }
        
        return( this );
    }
    
    public ExpanderColumnCellRenderer( Image minusImage, Image plusImage )
    {
        this.minusImage = minusImage;
        this.plusImage = plusImage;
        
        setOpaque( true );
    }
}
