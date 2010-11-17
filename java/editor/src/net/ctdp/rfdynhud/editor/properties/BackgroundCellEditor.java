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
import net.ctdp.rfdynhud.editor.hiergrid.ValueCellEditor;
import net.ctdp.rfdynhud.editor.util.BackgroundSelector;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BackgroundProperty.BackgroundType;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BackgroundCellEditor extends ValueCellEditor<Property, JPanel, JButton>
{
    private static final long serialVersionUID = -7299720233662747237L;
    
    private final JPanel panel = new JPanel( new BorderLayout() );
    private final JLabel label = new JLabel();
    private final JButton button = new JButton();
    
    private int row = -1;
    private int column = -1;
    
    private static BackgroundSelector backgroundSelector = null;
    
    @Override
    protected void prepareComponent( JPanel component, HierarchicalTable<Property> table, Property property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        if ( property.getButtonText() == null )
        {
            //button.setVisible( false );
            button.setVisible( true );
            button.setText( "..." );
            button.setToolTipText( "Choose a Background" );
        }
        else
        {
            button.setVisible( true );
            button.setText( property.getButtonText() );
            button.setToolTipText( property.getButtonTooltip() );
        }
        
        /*
        if ( isSelected )
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
        
        BackgroundProperty prop = (BackgroundProperty)property;
        
        if ( prop.getBackgroundType().isColor() )
        {
            Color color = prop.getColorValue();
            
            if ( ( color.getRed() < 50 ) && ( color.getGreen() < 50 ) && ( color.getBlue() < 50 ) && ( color.getAlpha() > 50 ) )
                label.setForeground( Color.WHITE );
            else
                label.setForeground( Color.BLACK );
            
            label.setBackground( color );
        }
        else if ( isSelected || forEditor )
        {
            label.setBackground( table.getSelectionBackground() );
            label.setForeground( table.getSelectionForeground() );
        }
        else
        {
            label.setBackground( table.getBackground() );
            label.setForeground( table.getStyle().getValueCellFontColor() );
        }
        /*
        {
            label.setBackground( Color.WHITE );
            label.setForeground( Color.BLACK );
        }
        */
        
        if ( isSelected || forEditor )
            panel.setBackground( table.getSelectionBackground() );
        else
            panel.setBackground( table.getBackground() );
        
        label.setFont( table.getStyle().getValueCellFont() );
        
        if ( prop.getBackgroundType().isColor() )
            label.setText( String.valueOf( value ).substring( BackgroundProperty.COLOR_INDICATOR.length() ) );
        else
            label.setText( String.valueOf( value ).substring( BackgroundProperty.IMAGE_INDICATOR.length() ) );
        
        this.row = row;
        this.column = column;
    }
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        if ( ( (BackgroundProperty)getProperty() ).getBackgroundType().isColor() )
            return ( BackgroundProperty.COLOR_INDICATOR + label.getText() );
        
        return ( BackgroundProperty.IMAGE_INDICATOR + label.getText() );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
    }
    
    public BackgroundCellEditor()
    {
        super();
        
        setComponent( panel, button );
        
        label.setBorder( new EmptyBorder( 0, 3, 0, 0 ) );
        
        button.setMargin( new Insets( 0, 3, 0 , 3 ) );
        
        button.addActionListener( new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed( java.awt.event.ActionEvent e )
            {
                BackgroundProperty prop = (BackgroundProperty)getProperty();
                if ( prop != null )
                {
                    if ( backgroundSelector == null )
                    {
                        backgroundSelector = new BackgroundSelector( null, null, ( (Widget)prop.getKeeper() ).getConfiguration() );
                    }
                    
                    JFrame frame = (JFrame)getTable().getRootPane().getParent();
                    
                    String startColor = ( prop.getColorProperty() != null ) ? prop.getColorProperty().getColorKey() : null;
                    String startImage = ( prop.getImageProperty() != null ) ? prop.getImageProperty().getImageName() : null;
                    
                    Object[] result = backgroundSelector.showDialog( frame, prop.getBackgroundType(), startColor, startImage, ( (Widget)prop.getKeeper() ).getConfiguration() );
                    
                    if ( result != null )
                    {
                        BackgroundType backgroundType = (BackgroundType)result[0];
                        String selColor = (String)result[1];
                        String selImage = (String)result[2];
                        
                        prop.setValues( backgroundType, selColor, selImage );
                        
                        if ( backgroundType.isColor() )
                            label.setText( selColor );
                        else
                            label.setText( selImage );
                        
                        getTable().setValueAt( getCellEditorValue(), row, column );
                        prop.setValue( getCellEditorValue() );
                        ( (PropertiesEditorTable)getTable() ).getRFDynHUDEditor().setDirtyFlag();
                    }
                    
                    frame.repaint();
                    
                    if ( prop.getButtonText() != null )
                        prop.onButtonClicked( button );
                }
                
                finalizeEdit( false );
            }
        } );
        
        panel.add( label, BorderLayout.CENTER );
        panel.add( button, BorderLayout.EAST );
        
        label.setOpaque( true );
    }
}
