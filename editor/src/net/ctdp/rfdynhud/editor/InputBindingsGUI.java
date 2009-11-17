package net.ctdp.rfdynhud.editor;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;

import net.ctdp.rfdynhud.editor.input.DirectInputConnection;
import net.ctdp.rfdynhud.editor.input.InputBindingsColumnModel;
import net.ctdp.rfdynhud.editor.input.InputBindingsTableModel;
import net.ctdp.rfdynhud.editor.input.DirectInputConnection.PollingListener;

public class InputBindingsGUI implements PollingListener
{
    private static InputBindingsGUI gui = null;
    
    private final DirectInputConnection directInputConnection;
    private final JFrame frame;
    private int pollingRow = -1;
    private final InputBindingsTableModel inputBindingsTableModel;
    private AWTEventListener listener;
    
    private boolean dirtyFlag = false;
    
    private void close()
    {
        if ( dirtyFlag )
        {
            switch ( JOptionPane.showConfirmDialog( frame, "Do you want to save changes?", "Input bindings", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE ) )
            {
                case JOptionPane.CANCEL_OPTION:
                    return;
                case JOptionPane.YES_OPTION:
                    inputBindingsTableModel.saveBindings();
                    dirtyFlag = false;
                    break;
            }
        }
        
        directInputConnection.interruptPolling();
        frame.dispose();
        Toolkit.getDefaultToolkit().removeAWTEventListener( listener );
        listener = null;
        gui = null;
    }
    
    public void onPollingFinished( boolean canceled, String deviceComponent )
    {
        if ( canceled )
            return;
        
        inputBindingsTableModel.setValueAt( deviceComponent, pollingRow, 2 );
        inputBindingsTableModel.fireTableCellUpdated( pollingRow, 2 );
        inputBindingsTableModel.setInputPollingRow( -1, pollingRow );
        pollingRow = -1;
        dirtyFlag = true;
    }
    
    private JMenu createFileMenu()
    {
        JMenu file = new JMenu( "File" );
        file.setDisplayedMnemonicIndex( 0 );
        
        /*
        JMenuItem open = new JMenuItem( "Open...", 0 );
        open.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                openConfig();
            }
        } );
        file.add( open );
        
        file.add( new JSeparator() );
        */
        
        JMenuItem save = new JMenuItem( "Save", 0 );
        save.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                inputBindingsTableModel.saveBindings();
                dirtyFlag = false;
            }
        } );
        file.add( save );
        
        file.add( new JSeparator() );
        
        JMenuItem close = new JMenuItem( "Close", 0 );
        close.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                close();
            }
        } );
        file.add( close );
        
        return ( file );
    }
    
    private void createMenu( JFrame frame )
    {
        JMenuBar menuBar = new JMenuBar();
        
        menuBar.add( createFileMenu() );
        
        frame.setJMenuBar( menuBar );
    }
    
    private InputBindingsGUI( RFDynHUDEditor editor )
    {
        this.frame = new JFrame( "rfDynHUD InputBindingsManager" );
        
        frame.setSize( 600, 480 );
        
        frame.setLocationRelativeTo( editor.getMainWindow() );
        
        createMenu( frame );
        
        this.directInputConnection = new DirectInputConnection();
        
        /*
        JButton b = new JButton( "Poll input" );
        b.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                directInputConnection.startInputPolling( frame.getTitle() );
            }
        } );
        
        frame.getContentPane().add( b );
        */
        
        this.inputBindingsTableModel = new InputBindingsTableModel( editor, editor.getEditorPanel().getWidgetsDrawingManager() );
        final JTable table = new JTable( inputBindingsTableModel, new InputBindingsColumnModel(), new DefaultListSelectionModel() );
        table.setRowHeight( 20 );
        
        table.addMouseListener( new MouseAdapter()
        {
            @Override
            public void mouseClicked( MouseEvent e )
            {
                if ( e.getClickCount() == 2 )
                {
                    if ( ( table.getSelectedColumn() == 2 ) && ( table.getSelectedRow() < table.getRowCount() - 1 ) )
                    {
                        if ( !directInputConnection.isPolling() )
                        {
                            pollingRow = table.getSelectedRow();
                            inputBindingsTableModel.setInputPollingRow( pollingRow, pollingRow );
                            directInputConnection.startInputPolling( frame.getTitle(), InputBindingsGUI.this );
                        }
                    }
                }
            }
        } );
        
        frame.getContentPane().add( new JScrollPane( table ) );
        
        this.listener = new AWTEventListener()
        {
            public void eventDispatched( AWTEvent event )
            {
                if ( event instanceof KeyEvent )
                {
                    KeyEvent ke = (KeyEvent)event;
                    
                    switch ( ke.getKeyCode() )
                    {
                        case KeyEvent.VK_ESCAPE:
                            directInputConnection.interruptPolling();
                            inputBindingsTableModel.setInputPollingRow( -1, pollingRow );
                            pollingRow = -1;
                            break;
                    }
                }
            }
        };
        
        Toolkit.getDefaultToolkit().addAWTEventListener( listener, AWTEvent.KEY_EVENT_MASK );
        
        frame.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                close();
            }
        } );
    }
    
    public static void showInputBindingsGUI( RFDynHUDEditor editor )
    {
        if ( gui == null )
        {
            gui = new InputBindingsGUI( editor );
        }
        
        gui.frame.setVisible( true );
    }
}
