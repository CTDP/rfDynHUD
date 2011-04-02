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
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.ValueCellEditor;
import net.ctdp.rfdynhud.properties.PosSizeProperty;
import net.ctdp.rfdynhud.properties.Position;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.Size;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class PosSizeCellEditor extends ValueCellEditor<Property, JPanel, JTextField>
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JTextField textfield = new JTextField();
    private final JButton button1 = new JButton();
    private final JButton button2 = new JButton();
    
    @Override
    protected void prepareComponent( JPanel component, HierarchicalTable<Property> table, Property property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( panel, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        PosSizeProperty psPorperty = (PosSizeProperty)property;
        
        button2.setVisible( psPorperty.isSizeProp() );
        
        float fv = (Float)value;
        boolean isPerc = psPorperty.isPercentage();
        
        button1.setVisible( true );
        button1.setText( psPorperty.getButton1Text( isPerc ) );
        button1.setToolTipText( psPorperty.getButton1Tooltip( isPerc ) );
        button2.setText( psPorperty.getButton2Text( isPerc ) );
        button2.setToolTipText( psPorperty.getButton2Tooltip( isPerc ) );
        
        button1.setPreferredSize( new Dimension( 30, 5 ) );
        button2.setPreferredSize( new Dimension( 30, 5 ) );
        
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
        
        if ( psPorperty.isSizeProp() )
            textfield.setText( Size.unparseValue( fv ) );
        else
            textfield.setText( Position.unparseValue( fv ) );
    }
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        PosSizeProperty psPorperty = (PosSizeProperty)getProperty();
        
        if ( psPorperty.isSizeProp() )
            return ( Size.parseValue( textfield.getText(), psPorperty.isPercentage() ) );
        
        return ( Position.parseValue( textfield.getText(), psPorperty.isPercentage() ) );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
        PosSizeProperty psPorperty = (PosSizeProperty)getProperty();
        
        float fv = (Float)oldValue;
        
        if ( psPorperty.isSizeProp() )
            textfield.setText( Size.unparseValue( fv ) );
        else
            textfield.setText( Position.unparseValue( fv ) );
    }
    
    public PosSizeCellEditor()
    {
        super();
        
        setComponent( panel, textfield );
        
        textfield.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( getTable() != null )
                {
                    finalizeEdit( false );
                }
            }
        } );
        
        button1.setMargin( new Insets( 0, 3, 0, 3 ) );
        button2.setMargin( new Insets( 0, 3, 0, 3 ) );
        
        button1.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                PosSizeProperty psPorperty = (PosSizeProperty)getProperty();
                
                if ( psPorperty != null )
                {
                    //Float oldValue = psPorperty.getValue();
                    getProperty().onButtonClicked( button1 );
                    Float newValue = (Float)getProperty().getValue();
                    getProperty().onButtonClicked( button1 );
                    
                    if ( psPorperty.isSizeProp() )
                        textfield.setText( Size.unparseValue( newValue ) );
                    else
                        textfield.setText( Position.unparseValue( newValue ) );
                }
                
                finalizeEdit( false );
            }
        } );
        
        button2.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                PosSizeProperty psPorperty = (PosSizeProperty)getProperty();
                
                if ( psPorperty != null )
                {
                    //Float oldValue = psPorperty.getValue();
                    psPorperty.onButton2Clicked( button2 );
                    Float newValue = (Float)getProperty().getValue();
                    //psPorperty.onButton2Clicked( button2 );
                    
                    if ( psPorperty.isSizeProp() )
                        textfield.setText( Size.unparseValue( newValue ) );
                    else
                        textfield.setText( Position.unparseValue( newValue ) );
                }
                
                finalizeEdit( false );
            }
        } );
        
        panel.add( textfield, BorderLayout.CENTER );
        JPanel east = new JPanel( new BorderLayout() );
        east.add( button1, BorderLayout.WEST );
        east.add( button2, BorderLayout.EAST );
        panel.add( east, BorderLayout.EAST );
    }
}
