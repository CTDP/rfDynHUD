package net.ctdp.rfdynhud.editor.properties;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;
import net.ctdp.rfdynhud.editor.util.BorderSelector;
import net.ctdp.rfdynhud.util.TextureLoader;

/**
 * 
 * @author Marvin Froehlich
 */
public class BorderCellEditor extends KeyValueCellRenderer<JPanel> implements TableCellRenderer, TableCellEditor
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private static final String NONE = "<NONE>";
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JLabel label = new JLabel();
    private final JButton button = new JButton();
    
    private JTable table = null;
    private int row = -1;
    private int column = -1;
    private BorderProperty prop = null;
    
    @Override
    //public java.awt.Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    protected void prepareComponent( JPanel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        setComponent( panel );
        
        super.prepareComponent( panel, table, value, isSelected, hasFocus, row, column );
        
        //this.prop = ( (PropertiesEditor)table.getModel() ).getProperty( row );
        this.prop = (BorderProperty)( (HierarchicalTableModel)table.getModel() ).getRowAt( row );
        
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
        
        if ( value == null )
            label.setText( NONE );
        else
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
    
    public BorderCellEditor()
    {
        super( false, null );
        
        button.setMargin( new Insets( 0, 3, 0, 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( prop != null )
                {
                    BorderSelector bs = new BorderSelector( new File( TextureLoader.IMAGES_FOLDER, "borders" ) );
                    JFrame frame = (JFrame)button.getRootPane().getParent();
                    String selBorder = bs.showDialog( frame, prop.getWidgetsConfiguration(), (String)prop.getValue() );
                    
                    if ( selBorder != null )
                    {
                        if ( selBorder.equals( "" ) )
                        {
                            if ( !getCellEditorValue().equals( NONE ) )
                            {
                                label.setText( NONE );
                            }
                        }
                        else if ( !getCellEditorValue().equals( selBorder ) )
                        {
                            label.setText( selBorder );
                        }
                        
                        if ( bs.getSomethingChanged() )
                        {
                            table.setValueAt( getCellEditorValue(), row, column );
                            if ( getCellEditorValue().equals( NONE ) )
                                prop.setValue( "" );
                            else
                                prop.setValue( getCellEditorValue() );
                            ( (EditorTable)table ).getRFDynHUDEditor().setDirtyFlag();
                        }
                        
                        frame.repaint();
                    }
                    
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
