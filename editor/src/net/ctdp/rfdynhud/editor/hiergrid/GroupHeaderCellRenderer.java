package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public class GroupHeaderCellRenderer extends KeyValueCellRenderer< JLabel >
{
    private static final long serialVersionUID = -1986974044855186348L;
    
    private static final Color backgroundColor = new Color( 168, 193, 255 );
    private static Font font = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Insets getBorderInsets( Component c )
    {
        Insets insets = super.getBorderInsets( c );
        
        insets.left = 2;
        
        return( insets );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareComponent( JLabel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.prepareComponent( component, table, value, isSelected, hasFocus, row, column );
        
        component.setForeground( Color.BLACK );
        component.setBackground( backgroundColor );
        
        if ( font == null )
        {
            font = table.getFont().deriveFont( Font.BOLD );
        }
        
        component.setFont( font );
        
        //component.setText( String.valueOf( value ) );
        if ( value == null )
            component.setText( null );
        else
            component.setText( ( (FlaggedList)value ).getName() );
        component.setHorizontalAlignment( SwingConstants.CENTER );
    }
    
    public Object getCellEditorValue()
    {
        return ( getComponent().toString() );
    }
    
    public GroupHeaderCellRenderer()
    {
        super( true, new JLabel() );
    }
}
