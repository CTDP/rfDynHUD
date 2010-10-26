package net.ctdp.rfdynhud.editor.properties;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.ValueCellEditor;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class IntegerCellEditor extends ValueCellEditor<Property, JPanel, JTextField>
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JTextField textfield = new JTextField();
    private final JButton button = new JButton();
    
    @Override
    protected void prepareComponent( JPanel component, HierarchicalTable<Property> table, Property property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        if ( property.getButtonText() == null )
        {
            button.setVisible( false );
        }
        else
        {
            button.setVisible( true );
            button.setText( property.getButtonText() );
            button.setToolTipText( property.getButtonTooltip() );
        }
        
        if ( isSelected && !forEditor )
        {
            textfield.setBackground( table.getSelectionBackground() );
            textfield.setForeground( table.getSelectionForeground() );
        }
        else
        {
            textfield.setBackground( table.getBackground() );
            textfield.setForeground( table.getStyle().getValueCellFontColor() );
        }
        panel.setBackground( textfield.getBackground() );
        textfield.setFont( table.getStyle().getValueCellFont() );
        textfield.setBorder( null );
        
        textfield.setText( String.valueOf( value ) );
    }
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        return ( Integer.parseInt( textfield.getText() ) );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
        textfield.setText( String.valueOf( oldValue ) );
    }
    
    public IntegerCellEditor()
    {
        super();
        
        setComponent( panel, textfield );
        
        textfield.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                finalizeEdit( false );
            }
        } );
        
        button.setMargin( new Insets( 0, 3, 0, 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( getProperty() != null )
                {
                    getProperty().onButtonClicked( button );
                    textfield.setText( String.valueOf( getProperty().getValue() ) );
                }
            }
        } );
        
        panel.add( textfield, BorderLayout.CENTER );
        panel.add( button, BorderLayout.EAST );
    }
}
