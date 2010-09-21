/**
 * This piece of code has been provided by and with kind
 * permission of INFOLOG GmbH from Germany.
 * It is released under the terms of the GPL, but INFOLOG
 * is still permitted to use it in closed source software.
 */
package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.SwingConstants;

/**
 * @author Marvin Froehlich (CTDP) (aka Qudus)
 */
public class GroupHeaderCellRenderer extends KeyValueCellRenderer<Object, GroupHeaderRenderLabel>
{
    private static final long serialVersionUID = -1986974044855186348L;
    
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
    protected void prepareComponent( GroupHeaderRenderLabel component, HierarchicalTable<Object> table, Object property, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column );
        
        HierarchicalGridStyle style = ( (HierarchicalTable<?>)table ).getStyle();
        
        component.setBackground( style.getGroupHeaderBackgroundColor() );
        component.setForeground( style.getGroupHeaderFontColor() );
        
        HierarchicalTableModel<?> tm = (HierarchicalTableModel<?>)table.getModel();
        
        component.setLevel( tm.getLevel( row ) );
        component.setLastInGroup( tm.getLastInGroup( row ) );
        component.setExpanded( tm.getValueAt( row, 0 ) == Boolean.TRUE );
        
        component.setFont( style.getGroupHeaderFont() );
        
        //component.setText( String.valueOf( value ) );
        if ( value == null )
            component.setText( null );
        else
            component.setText( ( (GridItemsContainer<?>)value ).getNameForGrid() );
        component.setHorizontalAlignment( SwingConstants.CENTER );
    }
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        return ( getComponent().toString() );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
    }
    
    public GroupHeaderCellRenderer()
    {
        super( true, new GroupHeaderRenderLabel() );
    }
}
