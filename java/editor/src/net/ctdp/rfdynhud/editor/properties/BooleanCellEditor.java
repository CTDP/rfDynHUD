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
package net.ctdp.rfdynhud.editor.properties;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.ValueCellEditor;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BooleanCellEditor extends ValueCellEditor<Property, JPanel, JCheckBox>
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private static EmptyBorder border = new EmptyBorder( 2, 2, 2, 2 );
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JCheckBox checkbox = new JCheckBox();
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
        
        if ( isSelected || forEditor )
        {
            checkbox.setBackground( table.getSelectionBackground() );
            checkbox.setForeground( table.getSelectionForeground() );
        }
        else
        {
            checkbox.setBackground( table.getBackground() );
            checkbox.setForeground( table.getStyle().getValueCellFontColor() );
        }
        panel.setBackground( checkbox.getBackground() );
        checkbox.setFont( table.getStyle().getValueCellFont() );
        
        checkbox.setBorder( border );
        
        if ( value instanceof Boolean )
            checkbox.setSelected( (Boolean)value );
        else
            checkbox.setSelected( Boolean.parseBoolean( String.valueOf( value ) ) );
    }
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        return ( checkbox.isSelected() );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
    }
    
    public BooleanCellEditor()
    {
        super();
        
        setComponent( panel, checkbox );
        
        checkbox.setHorizontalAlignment( JCheckBox.CENTER );
        
        checkbox.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                finalizeEdit( false );
            }
        } );
        
        button.setMargin( new Insets( 0, 3, 0 , 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( getProperty() != null )
                {
                    getProperty().onButtonClicked( button );
                    
                    Object value = getProperty().getValue();
                    if ( value instanceof Boolean )
                        checkbox.setSelected( (Boolean)value );
                    else
                        checkbox.setSelected( Boolean.parseBoolean( String.valueOf( value ) ) );
                }
            }
        } );
        
        panel.add( checkbox, BorderLayout.CENTER );
        panel.add( button, BorderLayout.EAST );
    }
}
