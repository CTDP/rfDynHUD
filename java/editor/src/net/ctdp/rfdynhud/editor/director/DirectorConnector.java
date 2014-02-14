/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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
package net.ctdp.rfdynhud.editor.director;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DirectorConnector extends JDialog
{
    private static final long serialVersionUID = -9168788967185981419L;
    
    private final JComboBox<String> combo;
    
    private boolean isCancelled = true;
    
    public final boolean isCancelled()
    {
        return ( isCancelled );
    }
    
    public final String getConnectionString()
    {
        return ( (String)combo.getEditor().getItem() );
    }
    
    private void connect()
    {
        try
        {
            DirectorClientCommunicator.parseConnectionString( getConnectionString() );
            
            isCancelled = false;
            setVisible( false );
        }
        catch ( Throwable t )
        {
            JOptionPane.showMessageDialog( this, "Illegal connection string.\n\nMessage:\n" + t.getMessage(), "Parsing error", JOptionPane.ERROR_MESSAGE );
        }
    }
    
    public DirectorConnector( JFrame parent, String connectionStrings )
    {
        super( parent, "Connect to Game", true );
        
        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        
        setSize( 250, 135 );
        
        setLayout( new BorderLayout() );
        
        JPanel wrapper = new JPanel( new BorderLayout() );
        wrapper.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
        setContentPane( wrapper );
        
        JPanel pp = new JPanel( new BorderLayout( 0, 3 ) );
        
        JPanel p1 = new JPanel( new BorderLayout( 5, 0 ) );
        JLabel l1 = new JLabel( "Address:" );
        l1.setForeground( pp.getBackground() );
        p1.add( l1, BorderLayout.WEST );
        p1.add( new JLabel( "(Format: \"host:port\")" ), BorderLayout.CENTER );
        
        pp.add( p1, BorderLayout.CENTER );
        
        JPanel p2 = new JPanel( new BorderLayout( 5, 0 ) );
        
        JLabel caption = new JLabel( "Address:" );
        p2.add( caption, BorderLayout.WEST );
        
        if ( connectionStrings == null )
            this.combo = new JComboBox<String>();
        else
            this.combo = new JComboBox<String>( connectionStrings.split( ";" ) );
        combo.setEditable( true );
        p2.add( combo, BorderLayout.CENTER );
        
        combo.getEditor().getEditorComponent().addKeyListener( new KeyAdapter()
        {
            @Override
            public void keyPressed( KeyEvent e )
            {
                if ( e.getKeyCode() == KeyEvent.VK_ENTER )
                    connect();
            }
        } );
        
        pp.add( p2, BorderLayout.SOUTH );
        
        wrapper.add( pp, BorderLayout.NORTH );
        
        wrapper.add( new JPanel(), BorderLayout.CENTER );
        
        JPanel buttons = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        
        JButton btnConnect = new JButton( "Connect" );
        btnConnect.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                connect();
            }
        } );
        buttons.add( btnConnect );
        
        JButton btnCancel = new JButton( "Cancel" );
        btnCancel.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                isCancelled = true;
                DirectorConnector.this.setVisible( false );
            }
        } );
        buttons.add( btnCancel );
        
        wrapper.add( buttons, BorderLayout.SOUTH );
        
        combo.requestFocus();
        
        setLocationRelativeTo( parent );
    }
}
