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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.gamedata.ModInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;

public class SaveAsDialog extends JDialog implements ActionListener
{
    private static final long serialVersionUID = 4097644328704959279L;
    
    private static enum MainGroupItem
    {
        CONSTRUCT,
        DIRECT,
        ;
    }
    
    private static enum Actions
    {
        CONSTRUCT,
        DIRECT,
        
        MOD,
        SITUATION,
        VEHICLE,
        SESSION,
        
        BROWSE,
        
        CANCEL,
        SAVE,
        ;
    }
    
    private static enum _Situation
    {
        All( null ),
        SmallSessionMonitorView( "monitor_small" ),
        FullscreenSessionMonitorView( "monitor_big" ),
        Garage( "garage" ),
        ;
        
        private static final int numItems = _Situation.values().length;
        private final String string;
        
        private _Situation( String string )
        {
            this.string = string;
        }
    }
    
    private static enum _SessionType
    {
        Any( null ),
        TestDay( SessionType.TEST_DAY.name() ),
        Practice( SessionType.PRACTICE_WILDCARD ),
        Practice1( SessionType.PRACTICE1.name() ),
        Practice2( SessionType.PRACTICE2.name() ),
        Practice3( SessionType.PRACTICE3.name() ),
        Practice4( SessionType.PRACTICE4.name() ),
        Qualifying( SessionType.QUALIFYING_WILDCARD ),
        Qualifying1( SessionType.QUALIFYING1.name() ),
        Qualifying2( SessionType.QUALIFYING2.name() ),
        Qualifying3( SessionType.QUALIFYING3.name() ),
        Qualifying4( SessionType.QUALIFYING4.name() ),
        Warmup( SessionType.WARMUP.name() ),
        Race( SessionType.RACE_WILDCARD ),
        Race1( SessionType.RACE1.name() ),
        Race2( SessionType.RACE2.name() ),
        Race3( SessionType.RACE3.name() ),
        Race4( SessionType.RACE4.name() ),
        ;
        
        private static final int numItems = _SessionType.values().length;
        private final String string;
        
        private _SessionType( String string )
        {
            this.string = string;
        }
    }
    
    private static final Color DISABLED_TEXT_COLOR = new JTextField().getDisabledTextColor();
    
    private final RFDynHUDEditor editor;
    
    private JRadioButton rdoConstruct = null;
    private JRadioButton rdoDirect = null;
    
    private JPanel constructFrame = null;
    private JPanel directFrame = null;
    
    private JLabel lblMod = null;
    private JLabel lblSituation = null;
    private JLabel lblVehicle = null;
    private JLabel lblSession = null;
    
    private JComboBox cbxMod = null;
    private ButtonGroup bgSituation = null;
    private JRadioButton[] rdoSituation = null;
    private JTextField tbxVehicle = null;
    private ButtonGroup bgSession = null;
    private JRadioButton[] rdoSession = null;
    
    private JLabel lblFilename = null;
    private JTextField tbxFilename = null;
    private JButton btnBrowse = null;
    
    private _Situation getSelectedSituation()
    {
        for ( int i = 0; i < rdoSituation.length; i++ )
        {
            if ( rdoSituation[i].isSelected() )
                return ( _Situation.valueOf( rdoSituation[i].getText() ) );
        }
        
        return ( null );
    }
    
    private _SessionType getSelectedSession()
    {
        for ( int i = 0; i < rdoSession.length; i++ )
        {
            if ( rdoSession[i].isSelected() )
                return ( _SessionType.valueOf( rdoSession[i].getText() ) );
        }
        
        return ( null );
    }
    
    private String constructFilename( boolean set )
    {
        String filename = "";
        
        String mod = String.valueOf( cbxMod.getEditor().getItem() ).trim();
        if ( !mod.equals( "" ) )
            filename += mod + File.separator;
        
        filename += "overlay";
        
        _Situation situation = getSelectedSituation();
        if ( situation != null )
        {
            if ( situation.string != null )
                filename += "_" + situation.string;
        }
        
        String vehicle = tbxVehicle.getText().trim();
        if ( !vehicle.equals( "" ) )
            filename += "_" + vehicle;
        
        _SessionType session = getSelectedSession();
        if ( session != null )
        {
            if ( session.string != null )
                filename += "_" + session.string;
        }
        
        filename += ".ini";
        
        if ( set )
            tbxFilename.setText( filename );
        
        return ( filename );
    }
    
    private void onMainGroupSelectionChanged( MainGroupItem item )
    {
        boolean isConstruct = ( item == MainGroupItem.CONSTRUCT );
        
        TitledBorder tbConstruct = new TitledBorder( "Construct" );
        if ( !isConstruct )
            tbConstruct.setTitleColor( DISABLED_TEXT_COLOR );
        
        constructFrame.setBorder( tbConstruct );
        
        lblMod.setEnabled( isConstruct );
        lblSituation.setEnabled( isConstruct );
        lblVehicle.setEnabled( isConstruct );
        lblSession.setEnabled( isConstruct );
        
        cbxMod.setEnabled( isConstruct );
        for ( int i = 0; i < rdoSituation.length; i++ )
            rdoSituation[i].setEnabled( isConstruct );
        tbxVehicle.setEnabled( isConstruct );
        for ( int i = 0; i < rdoSession.length; i++ )
            rdoSession[i].setEnabled( isConstruct );
        
        TitledBorder tbDirect = new TitledBorder( "Filename" );
        if ( isConstruct )
            tbDirect.setTitleColor( tbxFilename.getDisabledTextColor() );
        
        directFrame.setBorder( tbDirect );
        
        lblFilename.setEnabled( !isConstruct );
        tbxFilename.setEnabled( !isConstruct );
        btnBrowse.setEnabled( !isConstruct );
        
        if ( isConstruct )
        {
            constructFilename( true );
        }
    }
    
    private File parseSelectedFile( boolean useFallback )
    {
        String filename = tbxFilename.getText().trim();
        File file = null;
        
        if ( !filename.equals( "" ) )
        {
            file = new File( filename );
            if ( !file.isAbsolute() )
            {
                if ( !filename.startsWith( "/" ) && !filename.startsWith( "\\" ) )
                    filename = File.separator + filename;
                
                file = new File( RFDynHUDEditor.FILESYSTEM.getConfigPath() + filename );
            }
            
            File path = file.getParentFile();
            
            if ( path == null )
            {
                if ( useFallback )
                {
                    path = RFDynHUDEditor.FILESYSTEM.getConfigFolder();
                    file = new File( path, file.getName() );
                }
            }
            else if ( !path.exists() )
            {
                if ( useFallback )
                {
                    path = RFDynHUDEditor.FILESYSTEM.getConfigFolder();
                    file = new File( path, file.getName() );
                }
                else
                {
                    File path2 = path.getParentFile();
                    
                    if ( ( path2 == null ) || !path2.exists() )
                    {
                        file = null;
                    }
                }
            }
        }
        else if ( useFallback )
        {
            file = new File( RFDynHUDEditor.FILESYSTEM.getConfigFolder(), "overlay.ini" );
        }
        
        if ( file != null )
        {
            if ( !file.getName().toLowerCase().endsWith( ".ini" ) )
                file = new File( file.getAbsolutePath() + ".ini" );
            
            try
            {
                file = file.getCanonicalFile();
            }
            catch ( IOException e )
            {
                file = file.getAbsoluteFile();
            }
        }
        
        return ( file );
    }
    
    private boolean unparseFilename( File file )
    {
        if ( file == null )
        {
            tbxFilename.setText( "" );
            onMainGroupSelectionChanged( MainGroupItem.CONSTRUCT );
            
            return ( false );
        }
        
        File folder = file.getParentFile();
        
        if ( folder != null )
        {
            String path = folder.getAbsolutePath();
            
            if ( path.toLowerCase().equals( RFDynHUDEditor.FILESYSTEM.getConfigPath().toLowerCase() ) )
                path = "";
            else if ( path.toLowerCase().startsWith( RFDynHUDEditor.FILESYSTEM.getConfigPath().toLowerCase() ) )
                path = path.substring( RFDynHUDEditor.FILESYSTEM.getConfigPath().length() + 1 );
            
            int p = path.lastIndexOf( File.separatorChar );
            if ( p >= 0 )
                path = path.substring( p + 1 );
            
            String mod = path;
            boolean found = false;
            for ( int i = 0; !found && i < cbxMod.getItemCount(); i++ )
            {
                if ( ( (String)cbxMod.getItemAt( i ) ).equalsIgnoreCase( mod ) )
                {
                    cbxMod.setSelectedIndex( i );
                    found = true;
                }
            }
            
            if ( !found )
                cbxMod.getEditor().setItem( mod );
            
            if ( !folder.exists() )
            {
                folder = null;
            }
        }
        
        String filename = file.getName();
        
        if ( !filename.toLowerCase().endsWith( ".ini" ) )
            return ( false );
        
        if ( !filename.startsWith( "overlay" ) )
            return ( false );
        
        if ( filename.length() != "overlay".length() + ".ini".length() )
        {
            filename = filename.substring( "overlay".length(), filename.length() - ".ini".length() );
            
            int i = 0;
            for ( _Situation situation : _Situation.values() )
            {
                if ( ( situation.string != null ) && filename.startsWith( "_" + situation.string ) )
                {
                    rdoSituation[i].setSelected( true );
                    filename = filename.substring( 1 + situation.string.length() );
                    break;
                }
                
                i++;
            }
            
            i = 0;
            for ( _SessionType session : _SessionType.values() )
            {
                if ( ( session.string != null ) && filename.endsWith( "_" + session.string ) )
                {
                    rdoSession[i].setSelected( true );
                    filename = filename.substring( 0, filename.length() - 1 - session.string.length() );
                    break;
                }
                
                i++;
            }
            
            if ( filename.startsWith( "_" ) )
                tbxVehicle.setText( filename.substring( 1 ) );
            else
                tbxVehicle.setText( filename );
        }
        
        if ( folder == null )
            tbxFilename.setText( file.getName() );
        else
            tbxFilename.setText( folder + File.separator + file.getName() );
        
        return ( true );
    }
    
    public void setSelectedFile( File file )
    {
        if ( file == null )
        {
            rdoConstruct.setSelected( true );
            onMainGroupSelectionChanged( MainGroupItem.CONSTRUCT );
            tbxFilename.setText( "" );
        }
        else
        {
            File path = file.getParentFile();
            if ( ( path == null ) || !path.exists() )
            {
                path = RFDynHUDEditor.FILESYSTEM.getConfigFolder();
                file = new File( path, file.getName() );
            }
            
            if ( file.getAbsolutePath().toLowerCase().equals( RFDynHUDEditor.FILESYSTEM.getConfigPath().toLowerCase() ) )
                tbxFilename.setText( "" );
            else if ( file.getAbsolutePath().toLowerCase().startsWith( RFDynHUDEditor.FILESYSTEM.getConfigPath().toLowerCase() ) )
                tbxFilename.setText( file.getAbsolutePath().substring( RFDynHUDEditor.FILESYSTEM.getConfigPath().length() + 1 ) );
            else
                tbxFilename.setText( file.getAbsolutePath() );
            
            if ( unparseFilename( file ) )
            {
                rdoConstruct.setSelected( true );
                onMainGroupSelectionChanged( MainGroupItem.CONSTRUCT );
            }
            else
            {
                rdoDirect.setSelected( true );
                onMainGroupSelectionChanged( MainGroupItem.DIRECT );
            }
        }
    }
    
    public final File getSelectedFile()
    {
        return ( parseSelectedFile( false ) );
    }
    
    private void browse()
    {
        JFileChooser fc = new JFileChooser();
        File file = parseSelectedFile( true );
        if ( file != null )
        {
            fc.setCurrentDirectory( file.getParentFile() );
            fc.setSelectedFile( file );
        }
        
        fc.setMultiSelectionEnabled( false );
        fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
        fc.setFileFilter( new FileNameExtensionFilter( "ini files", "ini" ) );
        
        if ( fc.showSaveDialog( this ) != JFileChooser.APPROVE_OPTION )
            return;
        
        if ( !fc.getSelectedFile().getName().toLowerCase().endsWith( ".ini" ) )
            fc.setSelectedFile( new File( fc.getSelectedFile().getAbsolutePath() + ".ini" ) );
        
        file = fc.getSelectedFile();
        
        if ( file.getAbsolutePath().toLowerCase().equals( RFDynHUDEditor.FILESYSTEM.getConfigPath().toLowerCase() ) )
            tbxFilename.setText( "" );
        else if ( file.getAbsolutePath().toLowerCase().startsWith( RFDynHUDEditor.FILESYSTEM.getConfigPath().toLowerCase() ) )
            tbxFilename.setText( file.getAbsolutePath().substring( RFDynHUDEditor.FILESYSTEM.getConfigPath().length() + 1 ) );
        else
            tbxFilename.setText( file.getAbsolutePath() );
    }
    
    private void cancel()
    {
        tbxFilename.setText( "" );
        
        this.setVisible( false );
    }
    
    private void save()
    {
        if ( tbxFilename.getText().trim().equals( "" ) )
            return;
        
        File file = getSelectedFile();
        
        boolean doIt = true;
        
        if ( file == null )
        {
            doIt = false;
        }
        else if ( file.exists() )
        {
            int result = JOptionPane.showConfirmDialog( this, "Do you want to overwrite the existing file \"" + file.getAbsolutePath() + "\"?", editor.getMainWindow().getTitle(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
            
            if ( result == JOptionPane.CANCEL_OPTION )
                return;
            
            if ( result == JOptionPane.NO_OPTION )
                return;
        }
        
        if ( doIt )
        {
            //System.out.println( file );
            this.setVisible( false );
        }
    }
    
    private void onActionPerformed( Actions action )
    {
        switch ( action )
        {
            case CONSTRUCT:
                if ( !lblMod.isEnabled() )
                {
                    if ( !tbxFilename.getText().trim().equals( "" ) )
                        unparseFilename( new File( tbxFilename.getText().trim() ) );
                    onMainGroupSelectionChanged( MainGroupItem.CONSTRUCT );
                }
                break;
            case DIRECT:
                if ( !lblFilename.isEnabled() )
                {
                    onMainGroupSelectionChanged( MainGroupItem.DIRECT );
                }
                break;
            
            case MOD:
            case SITUATION:
            case VEHICLE:
            case SESSION:
                constructFilename( true );
                break;
            
            case BROWSE:
                browse();
                break;
            
            case CANCEL:
                cancel();
                break;
            case SAVE:
                save();
                break;
        }
    }
    
    @Override
    public void actionPerformed( ActionEvent e )
    {
        if ( e.getActionCommand() == null )
            return;
        
        onActionPerformed( Actions.valueOf( e.getActionCommand() ) );
    }
    
    private JPanel createConstructPanel()
    {
        int i = 0;
        
        JPanel p0 = new JPanel( new BorderLayout() );
        p0.setBorder( new TitledBorder( "Construct" ) );
        
        JPanel p1 = new JPanel( new BorderLayout() );
        p1.setBorder( new EmptyBorder( 0, 5, 5, 5 ) );
        
        JPanel p2 = new JPanel( new BorderLayout( 5, 0 ) );
        
        final int numRows = 1 + _Situation.numItems + 1 + _SessionType.numItems;
        
        JPanel pl = new JPanel( new GridLayout( numRows, 1 ) );
        
        lblMod = new JLabel( "Mod:" );
        pl.add( lblMod );
        
        lblSituation = new JLabel( "Situation:" );
        pl.add( lblSituation );
        
        for ( i = 0; i < _Situation.numItems - 1; i++ )
            pl.add( Box.createVerticalGlue() );
        
        lblVehicle = new JLabel( "Vehicle:" );
        pl.add( lblVehicle );
        
        lblSession = new JLabel( "Session:" );
        pl.add( lblSession );
        
        for ( i = 0; i < _SessionType.numItems - 1; i++ )
            pl.add( Box.createVerticalGlue() );
        
        p2.add( pl, BorderLayout.WEST );
        
        
        JPanel pr = new JPanel( new GridLayout( numRows, 1 ) );
        
        cbxMod = new JComboBox( ModInfo.getInstalledModNames( RFDynHUDEditor.FILESYSTEM ) );
        cbxMod.setEditable( true );
        cbxMod.setSelectedIndex( -1 );
        cbxMod.setActionCommand( Actions.MOD.name() );
        cbxMod.addActionListener( this );
        ( (JTextComponent)cbxMod.getEditor().getEditorComponent() ).getDocument().addDocumentListener( new DocumentListener()
        {
            @Override
            public void removeUpdate( DocumentEvent e )
            {
                onActionPerformed( Actions.MOD );
            }
            
            @Override
            public void insertUpdate( DocumentEvent e )
            {
                onActionPerformed( Actions.MOD );
            }
            
            @Override
            public void changedUpdate( DocumentEvent e )
            {
                onActionPerformed( Actions.MOD );
            }
        } );
        pr.add( cbxMod );
        
        bgSituation = new ButtonGroup();
        
        rdoSituation = new JRadioButton[ _Situation.numItems ];
        i = 0;
        for ( _Situation s : _Situation.values() )
        {
            rdoSituation[i] = new JRadioButton( s.name(), i == 0 );
            bgSituation.add( rdoSituation[i] );
            rdoSituation[i].setActionCommand( Actions.SITUATION.name() );
            rdoSituation[i].addActionListener( this );
            pr.add( rdoSituation[i] );
            i++;
        }
        
        tbxVehicle = new JTextField( "" );
        tbxVehicle.setActionCommand( Actions.VEHICLE.name() );
        tbxVehicle.addActionListener( this );
        tbxVehicle.getDocument().addDocumentListener( new DocumentListener()
        {
            @Override
            public void removeUpdate( DocumentEvent e )
            {
                onActionPerformed( Actions.VEHICLE );
            }
            
            @Override
            public void insertUpdate( DocumentEvent e )
            {
                onActionPerformed( Actions.VEHICLE );
            }
            
            @Override
            public void changedUpdate( DocumentEvent e )
            {
                onActionPerformed( Actions.VEHICLE );
            }
        } );
        pr.add( tbxVehicle );
        
        bgSession = new ButtonGroup();
        
        rdoSession = new JRadioButton[ _SessionType.numItems ];
        i = 0;
        for ( _SessionType st : _SessionType.values() )
        {
            rdoSession[i] = new JRadioButton( st.name(), i == 0 );
            bgSession.add( rdoSession[i] );
            rdoSession[i].setActionCommand( Actions.SESSION.name() );
            rdoSession[i].addActionListener( this );
            pr.add( rdoSession[i] );
            i++;
        }
        
        p2.add( pr, BorderLayout.CENTER );
        
        p1.add( p2, BorderLayout.NORTH );
        p1.add( new JPanel(), BorderLayout.CENTER );
        
        p0.add( p1, BorderLayout.CENTER );
        
        constructFrame = p0;
        
        return ( p0 );
    }
    
    private JPanel createDirectPanel()
    {
        JPanel p0 = new JPanel( new BorderLayout() );
        p0.setBorder( new TitledBorder( "Filename" ) );
        
        JPanel p1 = new JPanel( new BorderLayout( 5, 0 ) );
        p1.setBorder( new EmptyBorder( 0, 5, 5, 5 ) );
        
        lblFilename = new JLabel( "Filename:" );
        p1.add( lblFilename, BorderLayout.WEST );
        
        tbxFilename = new JTextField( "" );
        p1.add( tbxFilename, BorderLayout.CENTER );
        
        btnBrowse = new JButton( "..." );
        btnBrowse.setActionCommand( Actions.BROWSE.name() );
        btnBrowse.addActionListener( this );
        p1.add( btnBrowse, BorderLayout.EAST );
        
        p0.add( p1, BorderLayout.CENTER );
        
        directFrame = p0;
        
        return ( p0 );
    }
    
    private JPanel createButtonsPanel()
    {
        JPanel p = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        
        JButton btnOk = new JButton( "Save" );
        btnOk.setActionCommand( "SAVE" );
        btnOk.addActionListener( this );
        p.add( btnOk );
        
        p.add( Box.createHorizontalStrut( 5 ) );
        
        JButton btnCancel = new JButton( "Cancel" );
        btnCancel.setActionCommand( "CANCEL" );
        btnCancel.addActionListener( this );
        p.add( btnCancel );
        
        //p.add( Box.createHorizontalStrut( 5 ) );
        
        return ( p );
    }
    
    public SaveAsDialog( RFDynHUDEditor editor )
    {
        super( editor.getMainWindow(), "Save As...", true );
        
        this.editor = editor;
        
        this.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
        this.setSize( 640, 480 );
        this.setLocationRelativeTo( editor.getMainWindow() );
        
        getContentPane().setLayout( new BorderLayout() );
        ( (JPanel)getContentPane() ).setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        
        ButtonGroup bgMain = new ButtonGroup();
        
        JPanel center = new JPanel( new BorderLayout() );
        rdoConstruct = new JRadioButton( "", true );
        rdoConstruct.setActionCommand( "CONSTRUCT" );
        rdoConstruct.addActionListener( this );
        bgMain.add( rdoConstruct );
        JPanel p = new JPanel( new BorderLayout() );
        p.add( rdoConstruct, BorderLayout.NORTH );
        center.add( p, BorderLayout.WEST );
        center.add( createConstructPanel(), BorderLayout.CENTER );
        
        getContentPane().add( center, BorderLayout.CENTER );
        
        JPanel footer = new JPanel( new BorderLayout() );
        rdoDirect = new JRadioButton();
        rdoDirect.setActionCommand( "DIRECT" );
        rdoDirect.addActionListener( this );
        bgMain.add( rdoDirect );
        p = new JPanel( new BorderLayout() );
        p.add( rdoDirect, BorderLayout.NORTH );
        footer.add( p, BorderLayout.WEST );
        p = new JPanel( new BorderLayout() );
        p.add( createDirectPanel(), BorderLayout.NORTH );
        p.add( createButtonsPanel(), BorderLayout.SOUTH );
        footer.add( p, BorderLayout.CENTER );
        
        getContentPane().add( footer, BorderLayout.SOUTH );
        
        onMainGroupSelectionChanged( MainGroupItem.CONSTRUCT );
        
        this.pack();
        this.setSize( this.getWidth() * 2, this.getHeight() );
        
        this.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                cancel();
            }
        } );
    }
}
