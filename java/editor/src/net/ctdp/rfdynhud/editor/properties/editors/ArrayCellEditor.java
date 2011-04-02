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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.ValueCellEditor;
import net.ctdp.rfdynhud.properties.ArrayProperty;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ArrayCellEditor extends ValueCellEditor<Property, JPanel, JComboBox>
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JComboBox combobox = new JComboBox();
    private final DefaultComboBoxModel model = (DefaultComboBoxModel)combobox.getModel();
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
            combobox.setBackground( table.getSelectionBackground() );
            combobox.setForeground( table.getSelectionForeground() );
        }
        else
        {
            combobox.setBackground( table.getBackground() );
            combobox.setForeground( table.getStyle().getValueCellFontColor() );
        }
        
        if ( isSelected )
            panel.setBackground( table.getSelectionBackground() );
        else
            panel.setBackground( table.getBackground() );
        
        combobox.setFont( table.getStyle().getValueCellFont() );
        
        model.removeAllElements();
        for ( Object o : ( (ArrayProperty<?>)property ).getArray() )
            model.addElement( o );
        model.setSelectedItem( value );
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
    
    public ArrayCellEditor()
    {
        super();
        
        setComponent( panel, combobox );
        
        combobox.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                //if ( getTable() != null )
                //    getTable().editingStopped( null );
                stopCellEditing();
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
                    model.setSelectedItem( getProperty().getValue() );
                }
            }
        } );
        
        panel.add( combobox, BorderLayout.CENTER );
        panel.add( button, BorderLayout.EAST );
    }
}
