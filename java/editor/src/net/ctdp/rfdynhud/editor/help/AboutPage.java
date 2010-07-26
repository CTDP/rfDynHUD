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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.ctdp.rfdynhud.RFDynHUD;

public class AboutPage extends JDialog
{
    private static final long serialVersionUID = 3670847857825791395L;
    
    private Component createInfoPanel()
    {
        StringBuilder sb = new StringBuilder( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" );
        sb.append( "<html>\n");
        sb.append( "<head>\n");
        sb.append( "<title>About rfDynHUD Editor</title>\n");
        sb.append( "<style type=\"text/css\">\n");
        sb.append( "p, li { font-family: Arial; font-size: 12pt; }\n");
        sb.append( "</style>\n");
        sb.append( "</head>\n");
        sb.append( "<body>\n" );
        sb.append( "<html>\n<body>\n" );
        
        sb.append( "<p style=\"margin-top: 0;\">" );
        sb.append( "Editor for rfDynHUD v" + RFDynHUD.VERSION.toString() + " plugin for a dynamic rFactor HUD." );
        sb.append( "</p>" );
        
        sb.append( "<p>" );
        sb.append( "Copyright &copy; by CTDP, Author: Marvin Fr&ouml;hlich<br />" );
        sb.append( "Special Thanks to Marcel Offermans" );
        sb.append( "</p>" );
        
        sb.append( "<p>" );
        sb.append( "Please visit us at <a href=\"http://www.ctdp.net/\">http://www.ctdp.net/</a>." );
        sb.append( "</p>" );
        
        sb.append( "</body>\n</html>\n" );
        
        JEditorPane p = new JEditorPane( "text/html", sb.toString() );
        //p.setBorder( new javax.swing.border.BevelBorder( javax.swing.border.BevelBorder.LOWERED ) );
        p.setEditable( false );
        
        JScrollPane sp = new JScrollPane( p );
        
        sp.setPreferredSize( new Dimension( 300, 150 ) );
        
        return ( sp );
    }
    
    public AboutPage( JFrame parent )
    {
        super( parent, "About rfDynHUD (Editor)", true );
        
        JPanel cp = (JPanel)this.getContentPane();
        
        cp.setLayout( new BorderLayout( 5, 5 ) );
        
        ImageIcon icon = new ImageIcon( AboutPage.class.getResource( "/data/config/images/ctdp-fat-1994.png" ) );
        JLabel label = new JLabel( icon );
        cp.add( label, BorderLayout.WEST );
        
        cp.add( createInfoPanel(), BorderLayout.CENTER );
        
        JPanel buttons = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JButton btnClose = new JButton( "Close" );
        btnClose.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                AboutPage.this.dispose();
            }
        } );
        buttons.add( btnClose );
        
        this.add( buttons, BorderLayout.SOUTH );
        
        this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }
    
    public static AboutPage showAboutPage( final JFrame parent )
    {
        final AboutPage ap = new AboutPage( parent );
        
        ap.addWindowListener( new WindowAdapter()
        {
            private boolean shot = false;
            
            @Override
            public void windowOpened( WindowEvent e )
            {
                if ( shot )
                    return;
                
                ap.pack();
                ap.setLocationRelativeTo( parent );
                
                shot = true;
            }
        } );
        
        ap.setVisible( true );
        
        return ( ap );
    }
}
