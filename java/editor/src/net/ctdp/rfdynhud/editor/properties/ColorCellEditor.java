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
import java.awt.Color;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;
import net.ctdp.rfdynhud.editor.util.ColorChooser;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.Property;


/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ColorCellEditor extends KeyValueCellRenderer<Property, JPanel>
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JLabel label = new JLabel();
    private final JButton button = new JButton();
    
    private HierarchicalTable<Property> table = null;
    private int row = -1;
    private int column = -1;
    private ColorProperty prop = null;
    
    private static ColorChooser colorChooser = null;
    
    @Override
    protected void prepareComponent( JPanel component, HierarchicalTable<Property> table, Property property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        this.table = table;
        this.row = row;
        this.column = column;
        this.prop = (ColorProperty)property;
        
        if ( prop.getButtonText() == null )
        {
            //button.setVisible( false );
            button.setVisible( true );
            button.setText( "..." );
            button.setToolTipText( "Choose a Color" );
        }
        else
        {
            button.setVisible( true );
            button.setText( prop.getButtonText() );
            button.setToolTipText( prop.getButtonTooltip() );
        }
        
        /*
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
        */
        
        Color color = prop.getColor();
        
        if ( ( color.getRed() < 50 ) && ( color.getGreen() < 50 ) && ( color.getBlue() < 50 ) && ( color.getAlpha() > 50 ) )
            label.setForeground( Color.WHITE );
        else
            label.setForeground( Color.BLACK );
        
        label.setBackground( color );
        if ( isSelected || forEditor )
            panel.setBackground( table.getSelectionBackground() );
        else
            panel.setBackground( table.getBackground() );
        label.setFont( table.getStyle().getValueCellFont() );
        
        label.setText( (String)value );
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
    
    public ColorCellEditor()
    {
        super( false, null );
        
        setComponent( panel );
        
        label.setBorder( new EmptyBorder( 0, 3, 0, 0 ) );
        
        button.setMargin( new Insets( 0, 3, 0, 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( prop != null )
                {
                    JFrame frame = (JFrame)table.getRootPane().getParent();
                    if ( colorChooser == null )
                    {
                        colorChooser = new ColorChooser( (String)prop.getValue(), prop.getWidget().getConfiguration() );
                    }
                    
                    String result = colorChooser.showDialog( frame, (String)prop.getValue(), prop.getWidget().getConfiguration() );
                    
                    if ( result != null )
                    {
                        prop.setValue( result );
                        
                        label.setText( (String)prop.getValue() );
                        table.setValueAt( getCellEditorValue(), row, column );
                        ( (PropertiesEditorTable)table ).getRFDynHUDEditor().setDirtyFlag();
                    }
                    
                    frame.repaint();
                    
                    if ( prop.getButtonText() != null )
                        prop.onButtonClicked( button );
                }
                
                finalizeEdit( table, false );
            }
        } );
        
        panel.add( label, BorderLayout.CENTER );
        panel.add( button, BorderLayout.EAST );
        
        label.setOpaque( true );
    }
}
