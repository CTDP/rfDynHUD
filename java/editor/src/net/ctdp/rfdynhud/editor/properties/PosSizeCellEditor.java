package net.ctdp.rfdynhud.editor.properties;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;
import net.ctdp.rfdynhud.properties.PosSizeProperty;
import net.ctdp.rfdynhud.values.Position;
import net.ctdp.rfdynhud.values.Size;

/**
 * 
 * @author Marvin Froehlich
 */
public class PosSizeCellEditor extends KeyValueCellRenderer<JPanel> implements TableCellRenderer, TableCellEditor
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JTextField textfield = new JTextField();
    private final JButton button1 = new JButton();
    private final JButton button2 = new JButton();
    
    private JTable table = null;
    private PosSizeProperty prop = null;
    
    @Override
    //public java.awt.Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    protected void prepareComponent( JPanel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        setComponent( panel );
        
        super.prepareComponent( panel, table, value, isSelected, hasFocus, row, column );
        
        //this.prop = ( (PropertiesEditor)table.getModel() ).getProperty( row );
        this.prop = (PosSizeProperty)( (HierarchicalTableModel)table.getModel() ).getRowAt( row );
        
        button2.setVisible( prop.isSizeProp() );
        
        float fv = (Float)value;
        boolean isPerc = prop.isPercentage();
        
        button1.setVisible( true );
        button1.setText( prop.getButton1Text( isPerc ) );
        button1.setToolTipText( prop.getButton1Tooltip( isPerc ) );
        button2.setText( prop.getButton2Text( isPerc ) );
        button2.setToolTipText( prop.getButton2Tooltip( isPerc ) );
        
        button1.setPreferredSize( new Dimension( 30, 5 ) );
        button2.setPreferredSize( new Dimension( 30, 5 ) );
        
        if ( isSelected )
        {
            textfield.setBackground( table.getSelectionBackground() );
            textfield.setForeground( table.getSelectionForeground() );
        }
        else
        {
            textfield.setBackground( table.getBackground() );
            textfield.setForeground( table.getForeground() );
        }
        textfield.setFont( table.getFont() );
        textfield.setBorder( null );
        
        if ( prop.isSizeProp() )
            textfield.setText( Size.unparseValue( fv ) );
        else
            textfield.setText( Position.unparseValue( fv ) );
        
        this.table = table;
        
        //return ( panel );
    }
    
    public java.awt.Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        getTableCellRendererComponent( table, value, isSelected, true, row, column );
        
        //textfield.setBackground( table.getSelectionBackground() );
        //textfield.setForeground( table.getSelectionForeground() );
        textfield.setBackground( table.getBackground() );
        textfield.setForeground( table.getForeground() );
        
        return ( panel );
    }
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        if ( prop.isSizeProp() )
            return ( Size.parseValue( textfield.getText(), prop.isPercentage() ) );
        
        return ( Position.parseValue( textfield.getText(), prop.isPercentage() ) );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
        float fv = (Float)oldValue;
        
        if ( prop.isSizeProp() )
            textfield.setText( Size.unparseValue( fv ) );
        else
            textfield.setText( Position.unparseValue( fv ) );
    }
    
    public PosSizeCellEditor()
    {
        super( false, null );
        
        textfield.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( table != null )
                {
                    finalizeEdit( table );
                }
            }
        } );
        
        button1.setMargin( new Insets( 0, 3, 0, 3 ) );
        button2.setMargin( new Insets( 0, 3, 0, 3 ) );
        
        button1.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( prop != null )
                {
                    prop.onButtonClicked( button1 );
                    
                    float value = (Float)prop.getValue();
                    if ( prop.isSizeProp() )
                        textfield.setText( Size.unparseValue( value ) );
                    else
                        textfield.setText( Position.unparseValue( value ) );
                }
                
                finalizeEdit( table );
            }
        } );
        
        button2.addActionListener( new java.awt.event.ActionListener()
        {
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( prop != null )
                {
                    prop.onButton2Clicked( button2 );
                    
                    float value = (Float)prop.getValue();
                    if ( prop.isSizeProp() )
                        textfield.setText( Size.unparseValue( value ) );
                    else
                        textfield.setText( Position.unparseValue( value ) );
                }
                
                finalizeEdit( table );
            }
        } );
        
        panel.add( textfield, BorderLayout.CENTER );
        JPanel east = new JPanel( new BorderLayout() );
        east.add( button1, BorderLayout.WEST );
        east.add( button2, BorderLayout.EAST );
        panel.add( east, BorderLayout.EAST );
    }
}
