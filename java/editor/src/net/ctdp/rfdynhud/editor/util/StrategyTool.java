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
package net.ctdp.rfdynhud.editor.util;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ModInfo;
import net.ctdp.rfdynhud.gamedata.ProfileInfo;
import net.ctdp.rfdynhud.gamedata.TrackInfo;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.gamedata.rfactor1._rf1_LiveGameDataObjectsFactory;
import net.ctdp.rfdynhud.util.RFDHLog;

public class StrategyTool
{
    private final JDialog frame;
    
    private JComboBox<Integer> cbxNumQualiLaps;
    private JComboBox<Integer> cbxNumReconnaissanceLaps;
    private JCheckBox chkFormationLap;
    private JTextField txtRaceLength100;
    private JComboBox<Integer> cbxRaceLengthMulti;
    private JTextField txtRaceLength;
    private JTextField txtAverageFuelUsage;
    private JComboBox<Integer> cbxNumPitstops;
    private JTextField txtFirstStop;
    private JTextField txtSecondStop;
    private JTextField txtThirdStop;
    private JTextField txtFourthStop;
    private JTextField txtAdditionalFuelSafety;
    private JTextField txtFirstFuel;
    private JTextField txtSecondFuel;
    private JTextField txtThirdFuel;
    private JTextField txtFourthFuel;
    private JTextField txtFifthFuel;
    
    private void compute()
    {
        try
        {
            int numQualiLaps = (Integer)cbxNumQualiLaps.getSelectedItem();
            int reconLaps = (Integer)cbxNumReconnaissanceLaps.getSelectedItem();
            boolean formationLap = chkFormationLap.isSelected();
            int raceLength = Integer.parseInt( txtRaceLength.getText() );
            float avgFuelUsage = Float.parseFloat( txtAverageFuelUsage.getText() );
            int numPitstops = (Integer)cbxNumPitstops.getSelectedItem();
            int firstStop = Integer.parseInt( txtFirstStop.getText() );
            int secondStop = Integer.parseInt( txtSecondStop.getText() );
            int thirdStop = Integer.parseInt( txtThirdStop.getText() );
            int fourthStop = Integer.parseInt( txtFourthStop.getText() );
            float additionalFuel = Float.parseFloat( txtAdditionalFuelSafety.getText() );
            
            float startingFuel = numQualiLaps * avgFuelUsage + reconLaps * avgFuelUsage + ( formationLap ? avgFuelUsage : 0f );
            if ( numPitstops == 0 )
                startingFuel += raceLength * avgFuelUsage;
            else
                startingFuel += firstStop * avgFuelUsage;
            
            startingFuel += additionalFuel;
            
            txtFirstFuel.setText( String.valueOf( (int)Math.ceil( startingFuel ) ) );
            
            if ( numPitstops <= 0 )
            {
                txtSecondFuel.setText( "N/A" );
                txtThirdFuel.setText( "N/A" );
                txtFourthFuel.setText( "N/A" );
                txtFifthFuel.setText( "N/A" );
                
                return;
            }
            
            float stop1Fuel = 0f;
            if ( numPitstops == 1 )
                stop1Fuel += ( raceLength - firstStop ) * avgFuelUsage;
            else
                stop1Fuel += ( secondStop - firstStop ) * avgFuelUsage;
            
            stop1Fuel += additionalFuel;
            
            txtSecondFuel.setText( String.valueOf( (int)Math.ceil( stop1Fuel ) ) );
            
            if ( numPitstops <= 1 )
            {
                txtThirdFuel.setText( "N/A" );
                txtFourthFuel.setText( "N/A" );
                txtFifthFuel.setText( "N/A" );
                
                return;
            }
            
            float stop2Fuel = 0f;
            if ( numPitstops == 2 )
                stop2Fuel += ( raceLength - secondStop ) * avgFuelUsage;
            else
                stop2Fuel += ( thirdStop - secondStop ) * avgFuelUsage;
            
            stop2Fuel += additionalFuel;
            
            txtThirdFuel.setText( String.valueOf( (int)Math.ceil( stop2Fuel ) ) );
            
            if ( numPitstops <= 2 )
            {
                txtFourthFuel.setText( "N/A" );
                txtFifthFuel.setText( "N/A" );
                
                return;
            }
            
            float stop3Fuel = 0f;
            if ( numPitstops == 3 )
                stop3Fuel += ( raceLength - thirdStop ) * avgFuelUsage;
            else
                stop3Fuel += ( fourthStop - thirdStop ) * avgFuelUsage;
            
            stop3Fuel += additionalFuel;
            
            txtFourthFuel.setText( String.valueOf( (int)Math.ceil( stop3Fuel ) ) );
            
            if ( numPitstops <= 3 )
            {
                txtFifthFuel.setText( "N/A" );
                
                return;
            }
            
            float stop4Fuel = ( raceLength - fourthStop ) * avgFuelUsage;
            
            stop4Fuel += additionalFuel;
            
            txtFifthFuel.setText( String.valueOf( (int)Math.ceil( stop4Fuel ) ) );
        }
        catch ( Throwable t )
        {
            JOptionPane.showMessageDialog( frame, t.getClass().getSimpleName() + ": " + t.getMessage(), "Error computing strategy", JOptionPane.ERROR_MESSAGE );
        }
    }
    
    private void updateRaceLength()
    {
        try
        {
            float multi = (Integer)cbxRaceLengthMulti.getSelectedItem() / 100f;
            
            txtRaceLength.setText( String.valueOf( Math.round( Integer.parseInt( txtRaceLength100.getText() ) * multi ) ) ); 
        }
        catch ( Throwable t )
        {
            JOptionPane.showMessageDialog( frame, t.getClass().getSimpleName() + ": " + t.getMessage(), "Error computing strategy", JOptionPane.ERROR_MESSAGE );
        }
    }
    
    private void requestClose()
    {
        RFDHLog.exception( "Closing Strategy Calculator" );
        
        frame.setVisible( false );
    }
    
    private static float getInitialFuelUsage( String modName, String trackName, String vehicleName )
    {
        try
        {
            Float cached = __GDPrivilegedAccess.loadFuelUsageFromCache( RFDynHUDEditor.FILESYSTEM, modName, trackName, vehicleName );
            
            if ( cached != null )
                return ( cached.floatValue() );
        }
        catch ( Throwable t )
        {
        }
        
        return ( 3.123f );
    }
    
    private StrategyTool( JFrame owner )
    {
        LiveGameData gameData = new LiveGameData( null, new _rf1_LiveGameDataObjectsFactory() );
        
        ProfileInfo profileInfo = gameData.getProfileInfo();
        __GDPrivilegedAccess.updateProfileInfo( profileInfo );
        ModInfo modInfo = gameData.getModInfo();
        __GDPrivilegedAccess.updateModInfo( modInfo );
        TrackInfo trackInfo = gameData.getTrackInfo();
        __GDPrivilegedAccess.updateTrackInfo( trackInfo );
        
        this.frame = new JDialog( owner, "Strategy Calculator" );
        frame.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
        frame.setModal( true );
        
        JPanel pane = (JPanel)frame.getContentPane();
        pane.setLayout( new BorderLayout() );
        
        JPanel table = new JPanel( new GridLayout( 18, 2 ) );
        table.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        
        JLabel lblNumQualiLaps = new JLabel( "Num quali laps:" );
        table.add( lblNumQualiLaps );
        
        cbxNumQualiLaps = new JComboBox<Integer>( new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 } );
        cbxNumQualiLaps.setSelectedIndex( 2 );
        table.add( cbxNumQualiLaps );
        
        JLabel lblReconnaissanceLaps = new JLabel( "Reconnaissance laps:" );
        table.add( lblReconnaissanceLaps );
        
        cbxNumReconnaissanceLaps = new JComboBox<Integer>( new Integer[] { 0, 1, 2, 3, 4 } );
        Integer numReconLaps = profileInfo.getNumReconLaps();
        if ( numReconLaps == null )
            cbxNumReconnaissanceLaps.setSelectedIndex( 0 );
        else
            cbxNumReconnaissanceLaps.setSelectedIndex( numReconLaps.intValue() );
        table.add( cbxNumReconnaissanceLaps );
        
        JLabel lblFormationLap = new JLabel( "Formation lap:" );
        table.add( lblFormationLap );
        
        Boolean formationLap = profileInfo.getFormationLap();
        if ( formationLap != Boolean.TRUE )
            chkFormationLap = new JCheckBox( "", false );
        else
            chkFormationLap = new JCheckBox( "", true );
        table.add( chkFormationLap );
        
        JLabel lblRaceLength100 = new JLabel( "Race length (laps at 100%):" );
        table.add( lblRaceLength100 );
        
        int trackRaceLaps = trackInfo.getRaceLaps();
        
        if ( trackRaceLaps < 0 )
            txtRaceLength100 = new JTextField( "70" );
        else
            txtRaceLength100 = new JTextField( String.valueOf( trackRaceLaps ) );
        table.add( txtRaceLength100 );
        
        JLabel lblRaceLengthMulti = new JLabel( "Race length (%):" );
        table.add( lblRaceLengthMulti );
        
        Integer[] multis = new Integer[ 200 ];
        for ( int i = 1; i <= 200; i++ )
        {
            multis[i - 1] = i;
        }
        
        cbxRaceLengthMulti = new JComboBox<Integer>( multis );
        
        Float raceLengthMulti = profileInfo.getRaceLengthMultiplier();
        if ( raceLengthMulti == null )
            cbxRaceLengthMulti.setSelectedIndex( 99 );
        else
            cbxRaceLengthMulti.setSelectedIndex( Math.round( raceLengthMulti.floatValue() * 100f ) - 1 );
        table.add( cbxRaceLengthMulti );
        
        cbxRaceLengthMulti.addItemListener( new ItemListener()
        {
            @Override
            public void itemStateChanged( ItemEvent e )
            {
                if ( e.getStateChange() == ItemEvent.SELECTED )
                    updateRaceLength();
            }
        } );
        
        JLabel lblRaceLength = new JLabel( "Race length (laps):" );
        table.add( lblRaceLength );
        
        txtRaceLength = new JTextField( "0" );
        txtRaceLength.setEditable( false );
        table.add( txtRaceLength );
        
        updateRaceLength();
        
        JLabel lblAverageFuelUsage = new JLabel( "Average fuel usage:" );
        table.add( lblAverageFuelUsage );
        
        txtAverageFuelUsage = new JTextField( String.valueOf( getInitialFuelUsage( modInfo.getName(), trackInfo.getTrackName(), profileInfo.getTeamName() ) ) );
        table.add( txtAverageFuelUsage );
        
        JLabel lblNumPitstops = new JLabel( "Number of pitstops:" );
        table.add( lblNumPitstops );
        
        cbxNumPitstops = new JComboBox<Integer>( new Integer[] { 0, 1, 2, 3, 4 } );
        cbxNumPitstops.setSelectedIndex( 2 );
        table.add( cbxNumPitstops );
        
        JLabel lblFirstStop = new JLabel( "First stop (in-lap):" );
        table.add( lblFirstStop );
        
        txtFirstStop = new JTextField( "16" );
        table.add( txtFirstStop );
        
        JLabel lblSecondStop = new JLabel( "Second stop (in-lap):" );
        table.add( lblSecondStop );
        
        txtSecondStop = new JTextField( "31" );
        table.add( txtSecondStop );
        
        JLabel lblThirdStop = new JLabel( "Third stop (in-lap):" );
        table.add( lblThirdStop );
        
        txtThirdStop = new JTextField( "0" );
        table.add( txtThirdStop );
        
        JLabel lblFourthStop = new JLabel( "Fourth stop (in-lap):" );
        table.add( lblFourthStop );
        
        txtFourthStop = new JTextField( "0" );
        table.add( txtFourthStop );
        
        JLabel lblAdditionalFuelSafety = new JLabel( "Additional fuel per stint:" );
        table.add( lblAdditionalFuelSafety );
        
        txtAdditionalFuelSafety = new JTextField( "2" );
        table.add( txtAdditionalFuelSafety );
        
        JLabel lblFirstFuel = new JLabel( "Starting fuel:" );
        table.add( lblFirstFuel );
        
        txtFirstFuel = new JTextField( "0" );
        txtFirstFuel.setEditable( false );
        table.add( txtFirstFuel );
        
        JLabel lblSecondFuel = new JLabel( "Fuel at first stop:" );
        table.add( lblSecondFuel );
        
        txtSecondFuel = new JTextField( "0" );
        txtSecondFuel.setEditable( false );
        table.add( txtSecondFuel );
        
        JLabel lblThirdFuel = new JLabel( "Fuel at second stop:" );
        table.add( lblThirdFuel );
        
        txtThirdFuel = new JTextField( "0" );
        txtThirdFuel.setEditable( false );
        table.add( txtThirdFuel );
        
        JLabel lblFourthFuel = new JLabel( "Fuel at third stop:" );
        table.add( lblFourthFuel );
        
        txtFourthFuel = new JTextField( "0" );
        txtFourthFuel.setEditable( false );
        table.add( txtFourthFuel );
        
        JLabel lblFifthFuel = new JLabel( "Fuel at fourth stop:" );
        table.add( lblFifthFuel );
        
        txtFifthFuel = new JTextField( "0" );
        txtFifthFuel.setEditable( false );
        table.add( txtFifthFuel );
        
        pane.add( table, BorderLayout.NORTH );
        
        JPanel buttons = new JPanel( new BorderLayout() );
        
        JPanel buttons2 = new JPanel();
        buttons.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        buttons2.setLayout( new BoxLayout( buttons2, BoxLayout.X_AXIS ) );
        buttons2.add( Box.createHorizontalGlue() );
        JButton computeButton = new JButton( "Compute" );
        computeButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                compute();
            }
        } );
        buttons2.add( computeButton );
        buttons2.add( Box.createHorizontalGlue() );
        buttons.add( buttons2, BorderLayout.CENTER );
        
        JButton closeButton = new JButton( "Close" );
        closeButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                requestClose();
            }
        } );
        buttons.add( closeButton, BorderLayout.EAST );
        
        pane.add( buttons, BorderLayout.SOUTH );
        
        frame.setSize( 460, 480 );
        frame.setLocationRelativeTo( owner );
        
        frame.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                requestClose();
            }
        } );
    }
    
    public static void showStrategyTool( JFrame owner )
    {
        RFDHLog.printlnEx( "Opening Strategy Calculator" );
        
        new StrategyTool( owner ).frame.setVisible( true );
    }
}
