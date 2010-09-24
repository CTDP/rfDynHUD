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
package net.ctdp.rfdynhud.editor.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BackgroundProperty.BackgroundType;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

import org.jagatoo.gui.awt_swing.util.ColorChooser;

/**
 * The {@link BackgroundSelector} selects the value of a {@link BackgroundProperty}.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BackgroundSelector extends JTabbedPane implements ChangeListener
{
    private static final long serialVersionUID = -7356477183079086811L;
    
    private static final Dimension COLOR_CHOOSER_SIZE = new Dimension( 416, 341 );
    
    private final ColorChooser colorChooser;
    private final ImageSelector imageSelector;
    
    private Dimension imageSelectorSize = null;
    
    private String startColor = null;
    private boolean cancelled = false;
    
    private JDialog dialog = null;
    
    @Override
    public void stateChanged( ChangeEvent e )
    {
        if ( this.getRootPane() == null )
            return;
        
        JDialog frame = (JDialog)this.getRootPane().getParent();
        
        if ( getSelectedIndex() == 0 )
        {
            frame.setResizable( false );
            
            imageSelectorSize = frame.getSize();
            frame.setSize( COLOR_CHOOSER_SIZE );
        }
        else
        {
            frame.setResizable( true );
            
            if ( imageSelectorSize != null )
                frame.setSize( imageSelectorSize );
        }
    }
    
    private JDialog initDialog( Window owner, String title )
    {
        if ( owner instanceof java.awt.Dialog )
            dialog = new JDialog( (java.awt.Dialog)owner, title );
        else if ( owner instanceof java.awt.Frame )
            dialog = new JDialog( (java.awt.Frame)owner, title );
        else
            dialog = new JDialog( owner, title );
        
        dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
        
        return ( dialog );
    }
    
    public Object[] showDialog( Window owner, BackgroundType startType, String startColor, String startImage, final WidgetsConfiguration widgetsConfig )
    {
        imageSelector.update();
        
        if ( ( dialog == null ) || ( dialog.getOwner() != owner ) )
        {
            dialog = initDialog( owner, "Select a background..." );
            
            Container contentPane = dialog.getContentPane();
            contentPane.setLayout( new BorderLayout() );
            
            contentPane.add( this, BorderLayout.CENTER );
            
            JPanel footer = new JPanel( new BorderLayout() );
            JPanel footer3 = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 5 ) );
            JButton ok = new JButton( "OK" );
            ok.addActionListener( new ActionListener()
            {
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    if ( !cancelled )
                        colorChooser.checkSelection( BackgroundSelector.this.startColor, widgetsConfig );
                    
                    dialog.setVisible( false );
                }
            } );
            footer3.add( ok );
            JButton cancel = new JButton( "Cancel" );
            cancel.addActionListener( new ActionListener()
            {
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    cancelled = true;
                    dialog.setVisible( false );
                }
            } );
            footer3.add( cancel );
            footer.add( footer3, BorderLayout.EAST );
            
            contentPane.add( footer, BorderLayout.SOUTH );
            
            dialog.addWindowListener( new WindowAdapter()
            {
                @Override
                public void windowClosing( WindowEvent e )
                {
                    cancelled = true;
                    dialog.setVisible( false );
                }
            } );
            
            imageSelectorSize = new Dimension( 416, 500 );
            
            if ( startType.isColor() )
                dialog.setSize( COLOR_CHOOSER_SIZE );
            else if ( startType.isImage() )
                dialog.setSize( imageSelectorSize );
            
            dialog.setModal( true );
            dialog.setLocationRelativeTo( owner );
        }
        
        if ( startType == null )
            startType = BackgroundType.COLOR;
        
        if ( startColor == null )
            startColor = "StandardBackground";
        
        if ( startImage == null )
            startImage = "default_rev_meter_bg.png";
        
        this.startColor = startColor;
        
        imageSelectorSize = new Dimension( 416, 500 );
        
        if ( startType.isColor() )
        {
            setSelectedIndex( 0 );
            dialog.setSize( COLOR_CHOOSER_SIZE );
        }
        else if ( startType.isImage() )
        {
            setSelectedIndex( 1 );
            dialog.setSize( imageSelectorSize );
        }
        
        colorChooser.setSelectedColorFromKey( startColor, widgetsConfig );
        imageSelector.setSelectedFile( startImage );
        
        cancelled = false;
        
        dialog.setVisible( true );
        
        if ( cancelled )
            return ( null );
        
        Object[] result = new Object[ 3 ];
        
        if ( getSelectedIndex() == 0 )
            result[0] = BackgroundType.COLOR;
        else if ( getSelectedIndex() == 1 )
            result[0] = BackgroundType.IMAGE;
        
        result[1] = colorChooser.getSelectedValue();
        result[2] = imageSelector.getSelectedFile();
        
        return ( result );
    }
    
    public BackgroundSelector( BackgroundType startType, String startColor, String startImage, WidgetsConfiguration widgetsConfig )
    {
        super();
        
        if ( startType == null )
            startType = BackgroundType.COLOR;
        
        if ( startColor == null )
            startColor = "StandardBackground";
        
        if ( startImage == null )
            startImage = "default_rev_meter_bg.png";
        
        addTab( "Color", colorChooser = new ColorChooser( startColor, widgetsConfig ) );
        addTab( "Image", imageSelector = new ImageSelector( GameFileSystem.INSTANCE.getImagesFolder(), startImage ) );
        
        if ( startType.isColor() )
            setSelectedIndex( 0 );
        else if ( startType.isImage() )
            setSelectedIndex( 1 );
        
        this.addChangeListener( this );
    }
}
