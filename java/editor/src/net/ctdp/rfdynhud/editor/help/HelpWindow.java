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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.strings.StringUtils;

public class HelpWindow extends JDialog
{
    private static final long serialVersionUID = -8011875510400863665L;
    
    private JCheckBox cbAlwaysShowOnStartup;
    private JScrollPane sp;
    
    public final boolean getAlwaysShowOnStartup()
    {
        return ( cbAlwaysShowOnStartup.isSelected() );
    }
    
    private Component createInfoPanel()
    {
        String s = "Unable to load readme";
        try
        {
            s = StringUtils.loadString( new File( GameFileSystem.INSTANCE.getPluginFolder(), "readme.html" ).toURI().toURL() );
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
        
        JButton btnClose = new JButton( "Close" );
        btnClose.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                HelpWindow.this.dispose();
            }
        } );
        buttons.add( btnClose, BorderLayout.EAST );
        
        this.add( buttons, BorderLayout.SOUTH );
        
        this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }
    
    public static HelpWindow showHelpWindow( final JFrame parent, boolean alwaysShowOnStartup )
    {
        final HelpWindow hw = new HelpWindow( parent, alwaysShowOnStartup );
        
        hw.addWindowListener( new WindowAdapter()
        {
            private boolean shot = false;
            
            @Override
            public void windowOpened( WindowEvent e )
            {
                if ( shot )
                    return;
                
                hw.pack();
                hw.setLocationRelativeTo( parent );
                hw.sp.getVerticalScrollBar().setValue( 0 );
                
                shot = true;
            }
        } );
        
        hw.setVisible( true );
        
        return ( hw );
    }
}
