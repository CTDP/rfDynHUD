/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.editor.properties.editors;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.ValueCellEditor;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditorTableModel;
import net.ctdp.rfdynhud.editor.util.BorderSelector;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BorderCellEditor extends ValueCellEditor<Property, JPanel, JButton>
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private static final String NONE = "<NONE>";
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JLabel label = new JLabel();
    private final JButton button = new JButton();
    
    private int row = -1;
    private int column = -1;
    
    private static BorderSelector borderSelector = null;
    
    @Override
    protected void prepareComponent( JPanel component, HierarchicalTable<Property> table, Property property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        this.row = row;
        this.column = column;
        
        if ( property.getButtonText() == null )
        {
            //button.setVisible( false );
            button.setVisible( true );
            button.setText( "..." );
            button.setToolTipText( "Choose a Color" );
        }
        else
        {
            button.setVisible( true );
            button.setText( property.getButtonText() );
            button.setToolTipText( property.getButtonTooltip() );
        }
        
        if ( isSelected || forEditor )
        {
            label.setBackground( table.getSelectionBackground() );
            label.setForeground( table.getSelectionForeground() );
        }
        else
        {
            label.setBackground( table.getBackground() );
            label.setForeground( table.getStyle().getValueCellFontColor() );
        }
        panel.setBackground( label.getBackground() );
        label.setFont( table.getStyle().getValueCellFont() );
        
        if ( ( value == null ) || value.equals( "" ) )
            label.setText( NONE );
        else
            label.setText( (String)value );
    }
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        String value = label.getText();
        
        //if ( value.equals( NONE ) )
        //    return ( "" );
        
        return ( value );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
    }
    
    public BorderCellEditor()
    {
        super();
        
        setComponent( panel, button );
        
        label.setBorder( new EmptyBorder( 0, 3, 0, 0 ) );
        
        button.setMargin( new Insets( 0, 3, 0, 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( getProperty() != null )
                {
                    JFrame frame = (JFrame)getTable().getRootPane().getParent();
                    if ( borderSelector == null )
                    {
                        borderSelector = new BorderSelector( RFDynHUDEditor.FILESYSTEM.getBordersFolder() );
                    }
                    
                    String result = borderSelector.showDialog( frame, ( (Widget)getProperty().getKeeper() ).getConfiguration(), (String)getProperty().getValue() );
                    
                    if ( result != null )
                    {
                        if ( result.equals( "" ) )
                        {
                            getProperty().setValue( NONE );
                            label.setText( NONE );
                            getTable().setValueAt( NONE, row, column );
                        }
                        else
                        {
                            getProperty().setValue( result );
                            label.setText( result );
                            getTable().setValueAt( result, row, column );
                        }
                        
                        ( (PropertiesEditorTableModel)getTable().getModel() ).getRFDynHUDEditor().setDirtyFlag();
                        
                        frame.repaint();
                    }
                    
                    if ( getProperty().getButtonText() != null )
                        getProperty().onButtonClicked( button );
                }
                
                finalizeEdit( false );
            }
        } );
        
        panel.add( label, BorderLayout.CENTER );
        panel.add( button, BorderLayout.EAST );
        
        label.setOpaque( true );
    }
}
