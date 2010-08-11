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
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BooleanCellEditor extends KeyValueCellRenderer<JPanel> implements TableCellEditor
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private static EmptyBorder border = new EmptyBorder( 2, 2, 2, 2 );
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JCheckBox checkbox = new JCheckBox();
    private final JButton button = new JButton();
    
    private JTable table = null;
    private Property prop = null;
    
    @Override
    //public java.awt.Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    protected void prepareComponent( JPanel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        setComponent( panel );
        
        super.prepareComponent( panel, table, value, isSelected, hasFocus, row, column );
        
        //this.prop = ( (PropertiesEditor)table.getModel() ).getProperty( row );
        this.prop = (Property)( (HierarchicalTableModel)table.getModel() ).getRowAt( row );
        
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
        
        if ( isSelected )
        {
            panel.setBackground( table.getSelectionBackground() );
            panel.setForeground( table.getSelectionForeground() );
            checkbox.setBackground( table.getSelectionBackground() );
            checkbox.setForeground( table.getSelectionForeground() );
        }
        else
        {
            panel.setBackground( table.getBackground() );
            panel.setForeground( table.getForeground() );
            checkbox.setBackground( table.getBackground() );
            checkbox.setForeground( table.getForeground() );
        }
        checkbox.setFont( table.getFont() );
        
        checkbox.setBorder( border );
        
        if ( value instanceof Boolean )
            checkbox.setSelected( (Boolean)value );
        else
            checkbox.setSelected( Boolean.parseBoolean( String.valueOf( value ) ) );
        
        this.table = table;
        
        //return ( panel );
    }
    
    @Override
    public java.awt.Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        getTableCellRendererComponent( table, value, isSelected, true, row, column );
        
        checkbox.setBackground( table.getSelectionBackground() );
        checkbox.setForeground( table.getSelectionForeground() );
        
        setComponent( panel );
        
        return ( panel );
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
        super( false, null );
        
        checkbox.setHorizontalAlignment( JCheckBox.CENTER );
        
        checkbox.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                finalizeEdit( table );
            }
        } );
        
        button.setMargin( new Insets( 0, 3, 0 , 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( prop != null )
                {
                    prop.onButtonClicked( button );
                    
                    Object value = prop.getValue();
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
