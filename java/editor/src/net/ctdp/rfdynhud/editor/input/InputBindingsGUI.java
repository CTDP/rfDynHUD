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
package net.ctdp.rfdynhud.editor.input;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.strings.StringUtils;

public class InputBindingsGUI implements DirectInputConnection.PollingListener
{
    private static InputBindingsGUI gui = null;
    
    private final RFDynHUDEditor editor;
    
    private final DirectInputConnection directInputConnection;
    private final JDialog frame;
    private int pollingRow = -1;
    private final InputBindingsTableModel inputBindingsTableModel;
    private AWTEventListener listener;
    
    private JEditorPane actionDocPanel;
    
    private void close()
    {
        if ( inputBindingsTableModel.getDirtyFlag() )
        {
            switch ( JOptionPane.showConfirmDialog( frame, "Do you want to save changes?", "Input bindings", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE ) )
            {
                case JOptionPane.CANCEL_OPTION:
                    return;
                case JOptionPane.YES_OPTION:
                    inputBindingsTableModel.saveBindings( directInputConnection.getInputDeviceManager() );
                    break;
            }
        }
        
        RFDHLog.printlnEx( "Closing InputBindingsManager" );
        
        directInputConnection.interruptPolling();
        frame.dispose();
        Toolkit.getDefaultToolkit().removeAWTEventListener( listener );
        listener = null;
        gui = null;
    }
    
    @Override
    public void onPollingFinished( boolean canceled, String deviceComponent, int keyCode, int modifierMask )
    {
        if ( canceled )
            return;
        
        //inputBindingsTableModel.setValueAt( deviceComponent, pollingRow, 2 );
        inputBindingsTableModel.updateDeviceComponent( deviceComponent, keyCode, modifierMask, pollingRow );
        inputBindingsTableModel.fireTableCellUpdated( pollingRow, 2 );
        inputBindingsTableModel.setInputPollingRow( -1, pollingRow );
        pollingRow = -1;
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
        save.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_MASK ) );
        save.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                if ( inputBindingsTableModel.getDirtyFlag() )
                {
                    inputBindingsTableModel.saveBindings( directInputConnection.getInputDeviceManager() );
                }
            }
        } );
        file.add( save );
        
        file.add( new JSeparator() );
        
        JMenuItem close = new JMenuItem( "Close", 0 );
        close.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                close();
            }
        } );
        file.add( close );
        
        return ( file );
    }
    
    private void createMenu( JDialog frame )
    {
        JMenuBar menuBar = new JMenuBar();
        
        menuBar.add( createFileMenu() );
        
        frame.setJMenuBar( menuBar );
    }
    
    private Component createHelpPanel()
    {
        String s = "Unable to load readme";
        try
        {
            s = StringUtils.loadString( InputBindingsGUI.class.getClassLoader().getResource( InputBindingsGUI.class.getPackage().getName().replace( '.', '/' ) + "/documentation.html" ) );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        JEditorPane p = new JEditorPane( "text/html", s );
        //p.setBorder( new javax.swing.border.BevelBorder( javax.swing.border.BevelBorder.LOWERED ) );
        p.setEditable( false );
        p.setAutoscrolls( false );
        p.setCaretPosition( 0 );
        
        JScrollPane helpSP = new JScrollPane( p );
        
        helpSP.setPreferredSize( new Dimension( 300, 100 ) );
        
        return ( helpSP );
    }
    
    private Component createActionDocPanel()
    {
        this.actionDocPanel = new JEditorPane( "text/html", "" );
        //actionDocPanel.setBorder( new javax.swing.border.BevelBorder( javax.swing.border.BevelBorder.LOWERED ) );
        actionDocPanel.setEditable( false );
        actionDocPanel.setAutoscrolls( false );
        actionDocPanel.setCaretPosition( 0 );
        
        JScrollPane helpSP = new JScrollPane( actionDocPanel );
        
        helpSP.setPreferredSize( new Dimension( 300, 150 ) );
        
        return ( helpSP );
    }
    
    public void showInputActionDoc( InputAction action )
    {
        if ( ( action == null ) || ( action.getDoc() == null ) )
            actionDocPanel.setText( "" );
        else
            actionDocPanel.setText( action.getDoc() );
        
        actionDocPanel.setCaretPosition( 0 );
    }
    
    private InputBindingsGUI( RFDynHUDEditor editor )
    {
        this.editor = editor;
        
        this.frame = new JDialog( editor.getMainWindow(), "rfDynHUD InputBindingsManager" );
        
        frame.setSize( 900, 480 );
        
        frame.setLocationRelativeTo( editor.getMainWindow() );
        
        createMenu( frame );
        
        this.directInputConnection = new DirectInputConnection( editor.getMainWindow().getTitle() );
        
        Container contentPane = frame.getContentPane();
        contentPane.setLayout( new BorderLayout( 5, 5 ) );
        
        JSplitPane split = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
        split.setDividerLocation( frame.getSize().width - 300 );
        split.setResizeWeight( 1 );
        
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
        
        this.inputBindingsTableModel = new InputBindingsTableModel( editor, editor.getWidgetsConfiguration(), directInputConnection.getInputDeviceManager() );
        final JTable table = new InputBindingsTable( inputBindingsTableModel, new InputBindingsColumnModel( this ), new DefaultListSelectionModel(), this );
        inputBindingsTableModel.setTable( table );
        table.setRowHeight( 20 );
        
        table.addMouseListener( new MouseAdapter()
        {
            @Override
            public void mouseClicked( MouseEvent e )
            {
                if ( e.getClickCount() == 2 )
                {
                    if ( ( table.getSelectedColumn() == 2 ) && table.isCellEditable( table.getSelectedRow(), 2 ) && ( table.getSelectedRow() < table.getRowCount() - 1 ) )
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
        
        split.add( new JScrollPane( table ) );
        
        JSplitPane split2 = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        split2.setResizeWeight( 1 );
        split2.add( createHelpPanel() );
        split2.add( createActionDocPanel() );
        split.add( split2 );
        
        contentPane.add( split, BorderLayout.CENTER );
        
        this.listener = new AWTEventListener()
        {
            @Override
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
        
        frame.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        
        frame.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowOpened( WindowEvent e )
            {
                InputBindingsGUI.this.editor.getEditorPanel().addWidgetSelectionListener( inputBindingsTableModel );
            }
            
            @Override
            public void windowClosing( WindowEvent e )
            {
                close();
            }
            
            @Override
            public void windowClosed( WindowEvent e )
            {
                InputBindingsGUI.this.editor.getEditorPanel().removeWidgetSelectionListener( inputBindingsTableModel );
            }
        } );
    }
    
    public static void showInputBindingsGUI( RFDynHUDEditor editor )
    {
        RFDHLog.printlnEx( "Opening InputBindingsManager" );
        
        if ( gui == null )
        {
            gui = new InputBindingsGUI( editor );
        }
        
        gui.frame.setVisible( true );
    }
}
