package net.ctdp.rfdynhud.editor.properties;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;
import net.ctdp.rfdynhud.properties.FontProperty;

import org.jagatoo.gui.awt_swing.util.FontChooser;
import org.jagatoo.gui.awt_swing.util.FontChooser.FontChooserDialog;

/**
 * 
 * @author Marvin Froehlich
 */
public class FontCellEditor extends KeyValueCellRenderer<JPanel> implements TableCellEditor
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JLabel label = new JLabel();
    private final JButton button = new JButton();
    
    private JTable table = null;
    private int row = -1;
    private int column = -1;
    private FontProperty prop = null;
    
    @Override
    //public java.awt.Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    protected void prepareComponent( JPanel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        setComponent( panel );
        
        super.prepareComponent( panel, table, value, isSelected, hasFocus, row, column );
        
        //this.prop = ( (PropertiesEditor)table.getModel() ).getProperty( row );
        this.prop = (FontProperty)( (HierarchicalTableModel)table.getModel() ).getRowAt( row );
        
        if ( prop.getButtonText() == null )
        {
            //button.setVisible( false );
            button.setVisible( true );
            button.setText( "..." );
            button.setToolTipText( "Choose a Font" );
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
    protected Object getCellEditorValueImpl() throws Throwable
    {
        return ( label.getText() );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
    }
    
    public FontCellEditor()
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
                    //JFrame frame = (JFrame)button.getRootPane().getParent();
                    JFrame frame = (JFrame)table.getRootPane().getParent();
                    FontChooserDialog d = FontChooser.getAsDialog( frame, (String)prop.getValue(), prop.getWidget().getConfiguration() );
                    d.setLocationRelativeTo( frame );
                    d.setVisible( true );
                    
                    String selFont = d.getSelectedFont();
                    if ( selFont != null )
                    {
                        String selFontName = d.getSelectedFontName();
                        
                        if ( selFontName != null )
                            label.setText( selFontName );
                        else
                            label.setText( selFont );
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
