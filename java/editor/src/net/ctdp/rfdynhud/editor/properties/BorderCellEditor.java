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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;
import net.ctdp.rfdynhud.editor.util.BorderSelector;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BorderCellEditor extends KeyValueCellRenderer<Property, JPanel>
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private static final String NONE = "<NONE>";
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JLabel label = new JLabel();
    private final JButton button = new JButton();
    
    private HierarchicalTable<Property> table = null;
    private int row = -1;
    private int column = -1;
    private BorderProperty prop = null;
    
    private static BorderSelector borderSelector = null;
    
    @Override
    protected void prepareComponent( JPanel component, HierarchicalTable<Property> table, Property property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        this.table = table;
        this.row = row;
        this.column = column;
        this.prop = (BorderProperty)property;
        
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
        super( false, null );
        
        setComponent( panel );
        
        button.setMargin( new Insets( 0, 3, 0, 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                if ( prop != null )
                {
                    JFrame frame = (JFrame)table.getRootPane().getParent();
                    if ( borderSelector == null )
                    {
                        borderSelector = new BorderSelector( GameFileSystem.INSTANCE.getBordersFolder() );
                    }
                    
                    String result = borderSelector.showDialog( frame, prop.getWidget().getConfiguration(), (String)prop.getValue() );
                    
                    if ( result != null )
                    {
                        if ( result.equals( "" ) )
                        {
                            prop.setValue( NONE );
                            label.setText( NONE );
                            table.setValueAt( NONE, row, column );
                        }
                        else
                        {
                            prop.setValue( result );
                            label.setText( result );
                            table.setValueAt( result, row, column );
                        }
                        
                        ( (EditorTable)table ).getRFDynHUDEditor().setDirtyFlag();
                        
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
