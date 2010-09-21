/**
 * This piece of code has been provided by and with kind
 * permission of INFOLOG GmbH from Germany.
 * It is released under the terms of the GPL, but INFOLOG
 * is still permitted to use it in closed source software.
 */
package net.ctdp.rfdynhud.editor.hiergrid;

/**
 * @param <P> the property type
 * 
 * @author Marvin Froehlich (CTDP) (aka Qudus)
 */
public class KeyCellRenderer<P extends Object> extends KeyValueCellRenderer<P, KeyRenderLabel>
{
    private static final long serialVersionUID = 663331747917701155L;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareComponent( KeyRenderLabel component, HierarchicalTable<P> table, P property, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column );
        
        HierarchicalGridStyle style = table.getStyle();
        HierarchicalTableModel<P> tm = table.getModel();
        
        component.setLevel( tm.getLevel( row ) );
        component.setLastInGroup( tm.getLastInGroup( row ) );
        
        component.setForeground( style.getKeyCellFontColor() );
        //if ( !component.getFont().isBold() )
        //    component.setFont( component.getFont().deriveFont( component.getFont().getStyle() | java.awt.Font.BOLD ) );
        component.setFont( style.getKeyCellFont() );
        
        component.setText( String.valueOf( value ) );
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
    
    public KeyCellRenderer()
    {
        super( true, new KeyRenderLabel() );
    }
}
