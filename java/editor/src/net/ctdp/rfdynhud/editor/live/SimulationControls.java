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
package net.ctdp.rfdynhud.editor.live;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SimulationControls extends JPanel
{
    private static final long serialVersionUID = 6287184738284967171L;
    
    private final RFDynHUDEditor editor;
    private final SimulationPlaybackControl control;
    
    private JTextField txtFile = null;
    
    public JButton btnPlay = null;
    private JButton btnStop = null;
    
    public void setFile( File file )
    {
        if ( file == null )
            txtFile.setText( "" );
        else
            txtFile.setText( file.getAbsolutePath() );
    }
    
    public final File getFile()
    {
        if ( txtFile.getText().trim().isEmpty() )
            return ( null );
        
        return ( new File( txtFile.getText().trim() ) );
    }
    
    private void browseFile()
    {
        JFileChooser fc = new JFileChooser();
        if ( txtFile.getText().trim().isEmpty() )
        {
            fc.setCurrentDirectory( editor.getGameData().getFileSystem().getConfigFolder() );
        }
        else
        {
            File initialFile = getFile();
            
            if ( initialFile.isFile() )
            {
                fc.setCurrentDirectory( initialFile.getParentFile() );
                fc.setSelectedFile( initialFile );
            }
            else
            {
                fc.setCurrentDirectory( initialFile );
            }
        }
        
        fc.setMultiSelectionEnabled( false );
        fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
        //fc.setFileFilter(  );
        
        if ( fc.showOpenDialog( editor.getMainWindow() ) == JFileChooser.APPROVE_OPTION )
            txtFile.setText( fc.getSelectedFile().getAbsolutePath() );
    }
    
    private void play()
    {
        control.cancelled = false;
        
        btnPlay.setEnabled( false );
        
        if ( editor.startSimulation( getFile(), control ) )
            btnStop.setEnabled( true );
        else
            btnPlay.setEnabled( true );
    }
    
    private void setTimeScale( float timeScale )
    {
        control.setTimeScale( timeScale );
    }
    
    private void stop()
    {
        control.cancelled = true;
        editor.stopSimulation( false );
        btnStop.setEnabled( false );
    }
    
    private void exit()
    {
        editor.switchToEditorMode();
    }
    
    public SimulationControls( RFDynHUDEditor editor, SimulationPlaybackControl control )
    {
        super();
        
        this.editor = editor;
        this.control = control;
        
        this.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        this.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        
        JLabel header = new JLabel( "Simulation Mode", new ImageIcon( this.getClass().getClassLoader().getResource( this.getClass().getPackage().getName().replace( '.', '/' ) + "/simulation-big.png" ) ), JLabel.LEFT );
        header.setBorder( new EmptyBorder( 0, 0, 5, 0 ) );
        header.setHorizontalTextPosition( JLabel.RIGHT );
        header.setIconTextGap( 5 );
        header.setFont( new Font( "Verdana", Font.BOLD, 18 ) );
        JPanel headerPanel = new JPanel( new BorderLayout() );
        headerPanel.add( header, BorderLayout.WEST );
        JPanel headerPadder = new JPanel();
        headerPadder.setPreferredSize( new Dimension( Integer.MAX_VALUE, 10 ) );
        headerPanel.add( headerPadder, BorderLayout.CENTER );
        
        this.add( headerPanel );
        
        JPanel filePanel = new JPanel( new BorderLayout( 5, 0 ) );
        filePanel.setBorder( new EmptyBorder( 0, 0, 5, 0 ) );
        txtFile = new JTextField();
        txtFile.setPreferredSize( new Dimension( Integer.MAX_VALUE, 22 ) );
        filePanel.add( txtFile, BorderLayout.CENTER );
        JButton btnBrowse = new JButton( "..." );
        btnBrowse.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                browseFile();
            }
        } );
        filePanel.add( btnBrowse, BorderLayout.EAST );
        
        this.add( filePanel );
        
        JPanel playPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
        playPanel.setBorder( new EmptyBorder( 0, 0, 5, 0 ) );
        btnPlay = new JButton( new ImageIcon( this.getClass().getClassLoader().getResource( this.getClass().getPackage().getName().replace( '.', '/' ) + "/play.png" ) ) );
        btnPlay.setToolTipText( "Play" );
        btnPlay.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                play();
            }
        } );
        playPanel.add( btnPlay );
        btnStop = new JButton( new ImageIcon( this.getClass().getClassLoader().getResource( this.getClass().getPackage().getName().replace( '.', '/' ) + "/stop.png" ) ) );
        btnStop.setEnabled( false );
        btnStop.setToolTipText( "Stop" );
        btnStop.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                stop();
            }
        } );
        playPanel.add( btnStop );
        JButton btnExit = new JButton( new ImageIcon( this.getClass().getClassLoader().getResource( this.getClass().getPackage().getName().replace( '.', '/' ) + "/exit.png" ) ) );
        btnExit.setToolTipText( "Exit Simulation Mode" );
        btnExit.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                exit();
            }
        } );
        playPanel.add( btnExit );
        
        this.add( playPanel );
        
        JPanel sliderPanel = new JPanel( new BorderLayout( 5, 0 ) );
        final JLabel sliderLabel = new JLabel( "1.0" );
        sliderLabel.setPreferredSize( new Dimension( 50, 10 ) );
        sliderPanel.setBorder( new EmptyBorder( 0, 0, 5, 0 ) );
        JSlider timeScaleSlider = new JSlider( JSlider.HORIZONTAL, -5, 4, 0 );
        timeScaleSlider.setPaintTicks( false );
        timeScaleSlider.setSnapToTicks( true );
        timeScaleSlider.setToolTipText( "Timescale: 1.0" );
        timeScaleSlider.addChangeListener( new ChangeListener()
        {
            @Override
            public void stateChanged( ChangeEvent e )
            {
                int sliderVal = ( (JSlider)e.getSource() ).getValue();
                float value = 1.0f;
                
                if ( sliderVal < 0 )
                    value = (float)Math.pow( 2, sliderVal );
                else if ( sliderVal == 0 )
                    value = 1.0f;
                else
                    value = sliderVal + 1;
                
                ( (JSlider)e.getSource() ).setToolTipText( "Timescale: " + value );
                
                sliderLabel.setText( String.valueOf( value ) );
                
                setTimeScale( value );
            }
        } );
        sliderPanel.add( timeScaleSlider, BorderLayout.CENTER );
        sliderPanel.add( sliderLabel, BorderLayout.EAST );
        this.add( sliderPanel );
        
        JPanel rest = new JPanel();
        rest.setPreferredSize( new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE ) );
        this.add( rest );
    }
}
