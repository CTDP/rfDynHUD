package net.ctdp.rfdynhud.editor.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
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
        Qualifying( SessionType.QUALIFYING.name() ),
        Warmup( SessionType.WARMUP.name() ),
        Race( SessionType.RACE.name() ),
        ;
        
        private static final int numItems = _SessionType.values().length;
        private final String string;
        
        private _SessionType( String string )
        {
            this.string = string;
        }
    }
    
    private final RFDynHUDEditor editor;
    
    private JRadioButton rdoConstruct = null;
    private JRadioButton rdoDirect = null;
    
    private JPanel constructFrame = null;
    private JPanel directFrame = null;
    
    private JLabel lblMod = null;
    private JLabel lblSituation = null;
    private JLabel lblVehicle = null;
    private JLabel lblSession = null;
    
    private JTextField tbxMod = null;
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
        
        String mod = tbxMod.getText().trim();
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
            tbConstruct.setTitleColor( tbxMod.getDisabledTextColor() );
        
        constructFrame.setBorder( tbConstruct );
        
        lblMod.setEnabled( isConstruct );
        lblSituation.setEnabled( isConstruct );
        lblVehicle.setEnabled( isConstruct );
        lblSession.setEnabled( isConstruct );
        
        tbxMod.setEnabled( isConstruct );
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
                
                file = new File( GameFileSystem.INSTANCE.getConfigPath() + filename );
            }
            
            File path = file.getParentFile();
            
            if ( path == null )
            {
                if ( useFallback )
                {
                    path = GameFileSystem.INSTANCE.getConfigFolder();
                    file = new File( path, file.getName() );
                }
            }
            else if ( !path.exists() )
            {
                if ( useFallback )
                {
                    path = GameFileSystem.INSTANCE.getConfigFolder();
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
            file = new File( GameFileSystem.INSTANCE.getConfigFolder(), "overlay.ini" );
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
            
            if ( path.toLowerCase().equals( GameFileSystem.INSTANCE.getConfigPath().toLowerCase() ) )
                path = "";
            else if ( path.toLowerCase().startsWith( GameFileSystem.INSTANCE.getConfigPath().toLowerCase() ) )
                path = path.substring( GameFileSystem.INSTANCE.getConfigPath().length() + 1 );
            
            int p = path.lastIndexOf( File.separatorChar );
            if ( p >= 0 )
                path = path.substring( p + 1 );
            
            String mod = path;
            tbxMod.setText( mod );
            
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
                path = GameFileSystem.INSTANCE.getConfigFolder();
                file = new File( path, file.getName() );
            }
            
            if ( file.getAbsolutePath().toLowerCase().equals( GameFileSystem.INSTANCE.getConfigPath().toLowerCase() ) )
                tbxFilename.setText( "" );
            else if ( file.getAbsolutePath().toLowerCase().startsWith( GameFileSystem.INSTANCE.getConfigPath().toLowerCase() ) )
                tbxFilename.setText( file.getAbsolutePath().substring( GameFileSystem.INSTANCE.getConfigPath().length() + 1 ) );
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
        
        if ( file.getAbsolutePath().toLowerCase().equals( GameFileSystem.INSTANCE.getConfigPath().toLowerCase() ) )
            tbxFilename.setText( "" );
        else if ( file.getAbsolutePath().toLowerCase().startsWith( GameFileSystem.INSTANCE.getConfigPath().toLowerCase() ) )
            tbxFilename.setText( file.getAbsolutePath().substring( GameFileSystem.INSTANCE.getConfigPath().length() + 1 ) );
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
        
        tbxMod = new JTextField( "" );
        tbxMod.setActionCommand( Actions.MOD.name() );
        tbxMod.addActionListener( this );
        tbxMod.addFocusListener( new FocusListener()
        {
            private String oldContent = null;
            
            @Override
            public void focusGained( FocusEvent e )
            {
                oldContent = ( (JTextField)e.getSource() ).getText();
            }
            
            @Override
            public void focusLost( FocusEvent e )
            {
                if ( !( (JTextField)e.getSource() ).getText().equals( oldContent ) )
                {
                    onActionPerformed( Actions.MOD );
                }
                
                oldContent = null;
            }
        } );
        pr.add( tbxMod );
        
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
        tbxVehicle.addFocusListener( new FocusListener()
        {
            private String oldContent = null;
            
            @Override
            public void focusGained( FocusEvent e )
            {
                oldContent = ( (JTextField)e.getSource() ).getText();
            }
            
            @Override
            public void focusLost( FocusEvent e )
            {
                if ( !( (JTextField)e.getSource() ).getText().equals( oldContent ) )
                {
                    onActionPerformed( Actions.VEHICLE );
                }
                
                oldContent = null;
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
