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
package net.ctdp.rfdynhud.editor.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.strings.StringUtils;

public class HelpWindow extends JDialog
{
    private static final long serialVersionUID = -8011875510400863665L;
    
    private JCheckBox cbAlwaysShowOnStartup;
    private JScrollPane sp;
    private final JButton closeButton;
    
    public Long waitEndTime = null;
    
    private final WindowListener closeListener = new WindowAdapter()
    {
        @Override
        public void windowClosing( WindowEvent e )
        {
            HelpWindow.this.setVisible( false );
            HelpWindow.this.removeWindowListener( this );
        }
    };
    
    public final boolean getAlwaysShowOnStartup()
    {
        return ( cbAlwaysShowOnStartup.isSelected() );
    }
    
    private Component createInfoPanel()
    {
        String s = "Unable to load readme";
        try
        {
            s = StringUtils.loadString( new File( RFDynHUDEditor.FILESYSTEM.getPluginFolder(), "readme.html" ).toURI().toURL() );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        JEditorPane p = new JEditorPane( "text/html", s );
        //p.setBorder( new javax.swing.border.BevelBorder( javax.swing.border.BevelBorder.LOWERED ) );
        p.setEditable( false );
        
        sp = new JScrollPane( p );
        
        sp.setPreferredSize( new Dimension( 800, 600 ) );
        
        return ( sp );
    }
    
    public HelpWindow( JFrame parent, boolean alwaysShowOnStartup )
    {
        super( parent, "rfDynHUD (Editor) Help", true );
        
        JPanel cp = (JPanel)this.getContentPane();
        
        cp.setLayout( new BorderLayout( 5, 5 ) );
        
        cp.add( createInfoPanel(), BorderLayout.CENTER );
        
        JPanel buttons = new JPanel( new BorderLayout() );
        cbAlwaysShowOnStartup = new JCheckBox( "Always show this window on startup", alwaysShowOnStartup );
        buttons.add( cbAlwaysShowOnStartup, BorderLayout.WEST );
        
        JPanel p = new JPanel();
        buttons.add( p, BorderLayout.CENTER );
        
        closeButton = new JButton( "Close" );
        closeButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                HelpWindow.this.dispose();
            }
        } );
        buttons.add( closeButton, BorderLayout.EAST );
        
        this.add( buttons, BorderLayout.SOUTH );
        
        this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
    }
    
    public static HelpWindow instance = null;
    
    public static HelpWindow showHelpWindow( final JFrame parent, boolean alwaysShowOnStartup, boolean waitFor60Seconds )
    {
        if ( instance == null )
        {
            instance = new HelpWindow( parent, alwaysShowOnStartup );
            
            instance.addWindowListener( new WindowAdapter()
            {
                @Override
                public void windowOpened( WindowEvent e )
                {
                    instance.pack();
                    instance.setLocationRelativeTo( parent );
                    instance.sp.getVerticalScrollBar().setValue( 0 );
                    
                    instance.removeWindowListener( this );
                }
            } );
        }
        
        if ( waitFor60Seconds )
        {
            instance.removeWindowListener( instance.closeListener );
            instance.waitEndTime = System.nanoTime() + 60000000000L;
            
            new Thread()
            {
                private int seconds = 60;
                
                @Override
                public void run()
                {
                    instance.closeButton.setEnabled( false );
                    instance.closeButton.setText( "Waiting for 60 seconds" );
                    
                    while ( System.nanoTime() < instance.waitEndTime )
                    {
                        int seconds2 = (int)( ( instance.waitEndTime - System.nanoTime() ) / 1000000000L );
                        
                        if ( seconds2 != seconds )
                        {
                            seconds = seconds2;
                            instance.closeButton.setText( "Waiting for " + seconds + " seconds" );
                        }
                        
                        try
                        {
                            Thread.sleep( 50L );
                        }
                        catch ( InterruptedException e )
                        {
                        }
                    }
                    
                    instance.addWindowListener( instance.closeListener );
                    instance.closeButton.setEnabled( true );
                    instance.closeButton.setText( "Close" );
                }
            }.start();
        }
        else
        {
            instance.addWindowListener( instance.closeListener );
            
            instance.closeButton.setEnabled( true );
            instance.closeButton.setText( "Close" );
            instance.waitEndTime = null;
        }
        
        instance.setVisible( true );
        
        return ( instance );
    }
}
