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
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class StringCellEditor extends KeyValueCellRenderer<Property, JPanel>
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JTextField textfield = new JTextField();
    private final JButton button = new JButton();
    
    private HierarchicalTable<Property> table = null;
    private Property prop = null;
    
    @Override
    protected void prepareComponent( JPanel component, HierarchicalTable<Property> table, Property property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        this.prop = property;
        this.table = table;
        
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
        
        /*
        if ( isSelected )
        {
            if ( forEditor )
            {
                Color c = table.getSelectionBackground();
                //c = new Color( 255 - (int)( ( 255 - c.getRed() ) * 0.8 ), 255 - (int)( ( 255 - c.getGreen() ) * 0.8 ), 255 - (int)( ( 255 - c.getBlue() ) * 0.8 ), c.getAlpha() );
                textfield.setBackground( c );
                textfield.setForeground( Color.BLACK );
            }
            else
            {
                textfield.setBackground( table.getSelectionBackground() );
                textfield.setForeground( table.getSelectionForeground() );
            }
        }
        else
        {
            textfield.setBackground( table.getBackground() );
            textfield.setForeground( table.getStyle().getValueCellFontColor() );
        }
        */
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
        return ( textfield.getText() );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
    }
    
    public StringCellEditor()
    {
        super( false, null );
        
        setComponent( panel );
        
        textfield.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                finalizeEdit( table );
            }
        } );
        
        button.setMargin( new Insets( 0, 3, 0, 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( prop != null )
                {
                    prop.onButtonClicked( button );
                    textfield.setText( String.valueOf( prop.getValue() ) );
                }
            }
        } );
        
        panel.add( textfield, BorderLayout.CENTER );
        panel.add( button, BorderLayout.EAST );
    }
}
