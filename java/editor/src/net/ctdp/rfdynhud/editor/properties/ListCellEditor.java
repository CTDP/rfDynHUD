package net.ctdp.rfdynhud.editor.properties;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;
import net.ctdp.rfdynhud.properties.ListProperty;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich
 */
public class ListCellEditor extends KeyValueCellRenderer<JPanel> implements TableCellEditor
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JComboBox combobox = new JComboBox();
    private final DefaultComboBoxModel model = (DefaultComboBoxModel)combobox.getModel();
    private final JButton button = new JButton();
    
    private JTable table = null;
    private Property prop = null;
    
    @Override
    //public java.awt.Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    protected void prepareComponent( JPanel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        setComponent( panel );
        
        super.prepareComponent( panel, table, value, isSelected, hasFocus, row, column );
        
        //this.prop = ( (PropertiesEditor)table.getModel() ).getProperty( row );
        this.prop = (Property)( (HierarchicalTableModel)table.getModel() ).getRowAt( row );
        
        if ( prop.getButtonText() == null )
        {
            button.setVisible( false );
        }
        else
        {
            button.setVisible( true );
            button.setText( prop.getButtonText() );
            button.setToolTipText( prop.getButtonTooltip() );
        }
        
        combobox.setBackground( table.getBackground() );
        combobox.setForeground( table.getForeground() );
        combobox.setFont( table.getFont() );
        
        model.removeAllElements();
        for ( Object o : ( (ListProperty<?, ?>)prop ).getList() )
            model.addElement( o );
        model.setSelectedItem( value );
        
        this.table = table;
        
        //return ( panel );
    }
    
    public java.awt.Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        getTableCellRendererComponent( table, value, isSelected, true, row, column );
        
        combobox.setBackground( table.getSelectionBackground() );
        combobox.setForeground( table.getSelectionForeground() );
        
        return ( panel );
    }
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        return ( combobox.getSelectedItem() );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
    }
    
    public ListCellEditor()
    {
        super( false, null );
        
        combobox.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( table != null )
                    table.editingStopped( null );
            }
        } );
        
        button.setMargin( new Insets( 0, 3, 0 , 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( prop != null )
                {
                    prop.onButtonClicked( button );
                    model.setSelectedItem( prop.getValue() );
                }
            }
        } );
        
        panel.add( combobox, BorderLayout.CENTER );
        panel.add( button, BorderLayout.EAST );
    }
}
