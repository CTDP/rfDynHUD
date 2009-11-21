package net.ctdp.rfdynhud.editor.properties;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;

import org.jagatoo.gui.awt_swing.util.ColorChooser;
import org.jagatoo.gui.awt_swing.util.ColorChooser.ColorChooserDialog;

/**
 * 
 * @author Marvin Froehlich
 */
public class ColorCellEditor extends KeyValueCellRenderer<JPanel> implements TableCellRenderer, TableCellEditor
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JLabel label = new JLabel();
    private final JButton button = new JButton();
    
    private JTable table = null;
    private int row = -1;
    private int column = -1;
    private ColorProperty prop = null;
    
    @Override
    //public java.awt.Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    protected void prepareComponent( JPanel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        setComponent( panel );
        
        super.prepareComponent( panel, table, value, isSelected, hasFocus, row, column );
        
        //this.prop = ( (PropertiesEditor)table.getModel() ).getProperty( row );
        this.prop = (ColorProperty)( (HierarchicalTableModel)table.getModel() ).getRowAt( row );
        
        if ( prop.getButtonText() == null )
        {
            //button.setVisible( false );
            button.setVisible( true );
            button.setText( "Choose..." );
            button.setToolTipText( "Choose a Color" );
        }
        else
        {
            button.setVisible( true );
            button.setText( prop.getButtonText() );
            button.setToolTipText( prop.getButtonTooltip() );
        }
        
        if ( isSelected )
        {
            label.setBackground( table.getSelectionBackground() );
            label.setForeground( table.getSelectionForeground() );
        }
        else
        {
            label.setBackground( table.getBackground() );
            label.setForeground( table.getForeground() );
        }
        label.setFont( table.getFont() );
        
        label.setText( (String)value );
        
        this.table = table;
        this.row = row;
        this.column = column;
        
        //return ( panel );
    }
    
    public java.awt.Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        getTableCellRendererComponent( table, value, isSelected, true, row, column );
        
        //if ( isSelected )
        {
            label.setBackground( table.getSelectionBackground() );
            label.setForeground( table.getSelectionForeground() );
        }
        /*
        else
        {
            label.setBackground( table.getBackground() );
            label.setForeground( table.getForeground() );
        }
        */
        
        return ( panel );
    }
    
    @Override
    public Object getCellEditorValue()
    {
        return ( label.getText() );
    }
    
    public ColorCellEditor()
    {
        super( false, null );
        
        button.setMargin( new Insets( 0, 3, 0 , 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( prop != null )
                {
                    //model.setSelectedItem( prop.getValue() );
                    JFrame frame = (JFrame)button.getRootPane().getParent();
                    ColorChooserDialog d = ColorChooser.getAsDialog( frame, (String)prop.getValue(), prop.getWidget().getConfiguration() );
                    d.setLocationRelativeTo( (JFrame)button.getRootPane().getParent() );
                    d.setVisible( true );
                    
                    String selColor = d.getSelectedColor();
                    if ( selColor != null )
                    {
                        String selColorName = d.getSelectedColorName();
                        
                        if ( selColorName != null )
                            label.setText( selColorName );
                        else
                            label.setText( selColor );
                    }
                    else
                    {
                        label.setText( (String)prop.getValue() );
                    }
                    
                    if ( d.getValueChanged() )
                    {
                        table.setValueAt( getCellEditorValue(), row, column );
                        prop.setValue( getCellEditorValue() );
                        ( (EditorTable)table ).getRFDynHUDEditor().setDirtyFlag();
                    }
                    
                    frame.repaint();
                    
                    if ( prop.getButtonText() != null )
                        prop.onButtonClicked( button );
                }
                
                finalizeEdit( table );
            }
        } );
        
        panel.add( label, BorderLayout.CENTER );
        panel.add( button, BorderLayout.EAST );
        
        label.setOpaque( true );
    }
}
