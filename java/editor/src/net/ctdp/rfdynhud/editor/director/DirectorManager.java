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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.editor.WidgetSelectionListener;
import net.ctdp.rfdynhud.editor.director.widgetstate.WidgetState;
import net.ctdp.rfdynhud.editor.director.widgetstate.WidgetState.VisibleType;
import net.ctdp.rfdynhud.editor.director.widgetstatesset.StatesSetsLoader;
import net.ctdp.rfdynhud.editor.director.widgetstatesset.WidgetStatesSet;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.PropertySelectionListener;
import net.ctdp.rfdynhud.editor.properties.DefaultPropertiesContainer;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditor;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditorTableModel;
import net.ctdp.rfdynhud.editor.properties.PropertyChangeListener;
import net.ctdp.rfdynhud.editor.util.DefaultPropertyWriter;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleControl;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

import org.jagatoo.logging.LogLevel;
import org.jagatoo.util.ini.IniWriter;
import org.jagatoo.util.strings.MD5Util;
import org.jagatoo.util.versioning.Version;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DirectorManager implements PropertyChangeListener, PropertySelectionListener<Property>, WidgetSelectionListener
{
    private final RFDynHUDEditor editor;
    
    private final DirectorCommunicator communicator = new DirectorCommunicator( this );
    
    private long sessionTime = -1L;
    private boolean isTimeDecreasing = false;
    
    private final List<DriverCapsule> driversList = new ArrayList<DriverCapsule>();
    private final Map<Integer, DriverCapsule> driversMap = new HashMap<Integer, DriverCapsule>();
    
    private final List<WidgetStatesSet> statesSets = new ArrayList<WidgetStatesSet>();
    
    private final StateSetsListProperty stateSetsListProp = new StateSetsListProperty( this, statesSets );
    
    private JSplitPane split0 = null;
    
    private JPanel stateSetsPanel = null;
    private PropertiesEditor stateSetsModel = null;
    private HierarchicalTable<Property> stateSetsTable;
    private JScrollPane stateSetsScrollPane;
    private JButton addStateButton = null;
    
    private JEditorPane docPanel = null;
    
    private File currentFile = null;
    
    private boolean dirty = false;
    
    public final boolean isDirty()
    {
        return ( dirty );
    }
    
    public final File getCurrentFile()
    {
        return ( currentFile );
    }
    
    public void log( LogLevel logLevel, Object... message )
    {
        RFDHLog.println( logLevel, message );
    }
    
    public void log( Object... message )
    {
        RFDHLog.println( message );
    }
    
    public void debug( Object... message )
    {
        RFDHLog.debug( message );
    }
    
    private void buildGUI()
    {
        this.split0 = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        split0.setContinuousLayout( true );
        split0.setResizeWeight( 0 );
        split0.setPreferredSize( new Dimension( 300, Integer.MAX_VALUE ) );
        split0.setMinimumSize( new Dimension( 300, 10 ) );
        
        this.stateSetsPanel = new JPanel( new BorderLayout() );
        this.stateSetsModel = new PropertiesEditor();
        stateSetsModel.addProperty( stateSetsListProp );
        stateSetsModel.addChangeListener( this );
        this.stateSetsTable = PropertiesEditorTableModel.newTable( null, stateSetsModel );
        stateSetsTable.addPropertySelectionListener( this );
        this.stateSetsScrollPane = stateSetsTable.createScrollPane();
        stateSetsPanel.add( stateSetsScrollPane, BorderLayout.CENTER );
        stateSetsTable.applyToModel();
        this.addStateButton = new JButton( "Add WidgetState " );
        addStateButton.setEnabled( false );
        addStateButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                addNewWidgetState();
            }
        } );
        stateSetsPanel.add( addStateButton, BorderLayout.SOUTH );
        
        split0.add( stateSetsPanel );
        
        docPanel = new JEditorPane( "text/html", "" );
        ( (HTMLDocument)docPanel.getDocument() ).getStyleSheet().importStyleSheet( RFDynHUDEditor.class.getClassLoader().getResource( "net/ctdp/rfdynhud/editor/properties/doc.css" ) );
        docPanel.setEditable( false );
        docPanel.setAutoscrolls( false );
        JScrollPane sp = new JScrollPane( docPanel );
        sp.setMinimumSize( new Dimension( 300, 10 ) );
        split0.add( sp );
        split0.setDividerLocation( 700 );
    }
    
    public final Component getComponent()
    {
        return ( split0 );
    }
    
    public final long getSessionTime()
    {
        return ( sessionTime );
    }
    
    public final boolean isTimeDecreasing()
    {
        return ( isTimeDecreasing );
    }
    
    private void updateStateSetsTable()
    {
        int r = stateSetsTable.getSelectedRow();
        
        stateSetsModel.clear();
        stateSetsModel.addProperty( stateSetsListProp );
        
        WidgetStatesSet wss = stateSetsListProp.getSelectedValue();
        if ( wss != null )
            wss.getProperties( new DefaultPropertiesContainer( stateSetsModel.getPropertiesList() ), false );
        
        stateSetsTable.applyToModel();
        
        if ( r < stateSetsTable.getRowCount() )
            stateSetsTable.getSelectionModel().setSelectionInterval( r, r );
        
        addStateButton.setEnabled( stateSetsListProp.getValue() != null );
    }
    
    public WidgetState getSelectedWidgetState()
    {
        WidgetStatesSet wss = stateSetsListProp.getValue();
        
        if ( ( wss == null ) || ( wss.getNumStates() == 0 ) )
            return ( null );
        
        if ( stateSetsTable.getSelectedRow() < 2 )
            return ( null );
        
        Property prop = null;
        
        if ( stateSetsTable.getModel().isDataRow( stateSetsTable.getSelectedRow() ) )
            prop = (Property)stateSetsTable.getModel().getRowAt( stateSetsTable.getSelectedRow() );
        else
            prop = (Property)stateSetsTable.getModel().getRowAt( stateSetsTable.getSelectedRow() + 1 );
        
        for ( int i = 0; i < wss.getNumStates(); i++ )
        {
            WidgetState ws = wss.getState( i );
            if ( ws.owns( prop ) )
                return ( ws );
        }
        
        return ( null );
    }
    
    private Property currentDocedProperty = null;
    
    @Override
    public void onPropertySelected( Property property, int row )
    {
        if ( property != currentDocedProperty )
        {
            docPanel.setText( RFDynHUDEditor.doc_header + property.getDocumentationSource() + RFDynHUDEditor.doc_footer );
            
            docPanel.setCaretPosition( 0 );
            
            currentDocedProperty = property;
        }
    }
    
    @Override
    public void onPropertyChanged( Property property, Object oldValue, Object newValue, int row, int column )
    {
        //if ( property == stateSetsListProp )
        {
            updateStateSetsTable();
        }
        
        if ( property.getName().equals( "name" ) )
            Collections.sort( statesSets );
        
        if ( !property.getName().equals( "forDriver" ) && !property.getName().equals( "compareDriver" ) )
            dirty = true;
    }
    
    private void applyWidgetToState( Widget widget, WidgetState state, boolean positionToo )
    {
        state.setWidgetName( widget.getName() );
        if ( positionToo )
        {
            state.setPosX( widget.getPosition().getEffectiveX() );
            state.setPosY( widget.getPosition().getEffectiveY() );
        }
    }
    
    @Override
    public void onWidgetSelected( Widget widget, boolean selectionChanged, boolean doubleClick )
    {
        if ( !doubleClick )
            return;
        
        WidgetState ws = getSelectedWidgetState();
        
        if ( ws == null )
            return;
        
        if ( JOptionPane.showConfirmDialog( editor.getMainWindow(), "Do you really want to apply this Widget's name and position to the selected WidgetState?", "Apply WidgetState", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION )
        {
            applyWidgetToState( widget, ws, true );
            
            updateStateSetsTable();
        }
    }
    
    public void addWidgetStatesSet( WidgetStatesSet wss )
    {
        statesSets.add( wss );
    }
    
    public void addNewWidgetStatesSet()
    {
        WidgetStatesSet wss = new WidgetStatesSet();
        
        statesSets.add( wss );
        wss.setName( WidgetStatesSet.class.getSimpleName() + statesSets.size() );
        Collections.sort( statesSets );
        stateSetsListProp.setValue( wss );
        
        updateStateSetsTable();
        
        dirty = true;
    }
    
    public void removeWidgetStatesSet( WidgetStatesSet wss )
    {
        statesSets.remove( wss );
        if ( statesSets.size() == 0 )
            stateSetsListProp.setValue( null );
        else
            stateSetsListProp.setValue( statesSets.get( 0 ) );
        
        updateStateSetsTable();
        
        dirty = true;
    }
    
    public void addNewWidgetState()
    {
        int rc0 = stateSetsTable.getRowCount();
        
        WidgetStatesSet wss = stateSetsListProp.getValue();
        
        WidgetState ws = wss.addState( driversList, this );
        
        if ( editor.getEditorPanel().getSelectedWidget() != null )
            applyWidgetToState( editor.getEditorPanel().getSelectedWidget(), ws, false );
        
        updateStateSetsTable();
        
        dirty = true;
        
        stateSetsTable.getSelectionModel().setSelectionInterval( rc0 + 1, rc0 + 1 );
    }
    
    public void removeWidgetState( WidgetState ws )
    {
        WidgetStatesSet wss = stateSetsListProp.getValue();
        
        wss.removeState( ws );
        
        updateStateSetsTable();
        
        dirty = true;
    }
    
    private static final Version FORMAT_VERSION = new Version( 1, 0, 0, null, 1 );
    
    private void saveStatesSets( File file )
    {
        try
        {
            IniWriter iniWriter = new IniWriter( file );
            
            iniWriter.writeGroup( "[HEADER]" );
            iniWriter.writeSetting( "formatVersion", FORMAT_VERSION );
            
            PropertyWriter writer = new DefaultPropertyWriter( iniWriter, false );
            
            for ( int i = 0; i < statesSets.size(); i++ )
            {
                statesSets.get( i ).saveProperties( writer );
            }
            
            iniWriter.close();
            
            currentFile = file;
            
            dirty = false;
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
            JOptionPane.showMessageDialog( editor.getMainWindow(), "Error saving states sets.\nMessage:\n" + t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
        }
    }
    
    public boolean saveStatesSetsAs()
    {
        File file = getFile( currentFile, false );
        
        if ( file == null )
            return ( false );
        
        saveStatesSets( file );
        
        return ( true );
    }
    
    public boolean saveStatesSets()
    {
        if ( currentFile == null )
            return ( saveStatesSetsAs() );
        
        saveStatesSets( currentFile );
        
        return ( true );
    }
    
    public void loadStatesSets( File file )
    {
        statesSets.clear();
        
        try
        {
            StatesSetsLoader loader = new StatesSetsLoader( this, driversList );
            
            loader.parse( file );
            
            Collections.sort( statesSets );
            
            currentFile = file;
            
            dirty = false;
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
            JOptionPane.showMessageDialog( editor.getMainWindow(), "Error loading states sets.\nMessage:\n" + t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
        }
        
        if ( statesSets.size() > 0 )
            stateSetsListProp.setSelectedValue( statesSets.get( 0 ) );
        
        updateStateSetsTable();
    }
    
    private File getFile( File initialFile, boolean open )
    {
        JFileChooser fc = new JFileChooser();
        if ( initialFile == null )
        {
            fc.setCurrentDirectory( RFDynHUDEditor.FILESYSTEM.getConfigFolder() );
            fc.setSelectedFile( new File( RFDynHUDEditor.FILESYSTEM.getConfigFolder(), "director_widget_states_sets.ini" ) );
        }
        else if ( initialFile.isFile() )
        {
            fc.setCurrentDirectory( initialFile.getParentFile() );
            fc.setSelectedFile( initialFile );
        }
        else
        {
            fc.setCurrentDirectory( initialFile );
        }
        
        fc.setMultiSelectionEnabled( false );
        fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
        fc.setFileFilter( new FileNameExtensionFilter( "ini files", "ini" ) );
        
        if ( open )
        {
            if ( fc.showOpenDialog( editor.getMainWindow() ) != JFileChooser.APPROVE_OPTION )
                return ( null );
        }
        else
        {
            if ( fc.showSaveDialog( editor.getMainWindow() ) != JFileChooser.APPROVE_OPTION )
                return ( null );
        }
        
        return ( fc.getSelectedFile() );
    }
    
    public boolean checkUnsavedChanges()
    {
        if ( dirty )
        {
            int result = JOptionPane.showConfirmDialog( editor.getMainWindow(), "The current states sets have changes. Do you want to save them?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE );
            
            if ( result == JOptionPane.CANCEL_OPTION )
                return ( false );
            
            if ( result == JOptionPane.YES_OPTION )
            {
                if ( !saveStatesSets() )
                    return ( false );
            }
        }
        
        return ( true );
    }
    
    public void loadStatesSets()
    {
        if ( !checkUnsavedChanges() )
            return;
        
        File file = getFile( currentFile, true );
        
        if ( file == null )
            return;
        
        loadStatesSets( file );
    }
    
    private void sendWidgetState( EffectiveWidgetState ews )
    {
        if ( ews.getWidgetName().startsWith( "(" ) )
        {
            if ( !ews.getWidgetName().endsWith( ")" ) || ( ews.getWidgetName().length() < 2 ) )
            {
                log( LogLevel.EXHAUSTIVE, "WARNING: Illegal Widget Name of WidgetState (\"" + ews.getWidgetName() + "\")." );
            }
            
            String widgetClassName = ews.getWidgetName().substring( 1, ews.getWidgetName().length() - 1 );
            for ( int j = 0; j < editor.getWidgetsConfiguration().getNumWidgets(); j++ )
            {
                Widget widget = editor.getWidgetsConfiguration().getWidget( j );
                if ( widget.getClass().getSimpleName().equalsIgnoreCase( widgetClassName ) )
                    communicator.sendWidgetState( widget.getName(), ews );
            }
        }
        else
        {
            communicator.sendWidgetState( ews.getWidgetName(), ews );
        }
    }
    
    public void applyEffectiveStates( boolean quietMode )
    {
        WidgetStatesSet wss = stateSetsListProp.getSelectedValue();
        
        if ( wss == null )
            return;
        
        communicator.startCommand( DirectorConstants.RESET_WIDGET_STATES );
        
        for ( int i = 0; i < wss.getNumStates(); i++ )
        {
            WidgetState ws = wss.getState( i );
            
            EffectiveWidgetState ews = ws.applyToEffective();
            
            if ( ews.getVisibleType() == VisibleType.NEXT_LAP )
            {
                ews.setVisibleStart( Long.MIN_VALUE );
                ews.setVisibleTime( 0L );
            }
            
            sendWidgetState( ews );
        }
        
        communicator.endCommand();
        
        if ( !quietMode )
            JOptionPane.showMessageDialog( editor.getMainWindow(), "Applied Widget States." );
    }
    
    public boolean startDirecting( String connectionStrings )
    {
        DirectorConnector conn = new DirectorConnector( editor.getMainWindow(), connectionStrings );
        conn.setVisible( true );
        
        if ( conn.isCancelled() )
            return ( false );
        
        communicator.connect( conn.getConnectionString() );
        
        return ( communicator.isConnected() );
    }
    
    public final boolean isConnected()
    {
        return ( communicator.isConnected() );
    }
    
    public void close()
    {
        communicator.close();
    }
    
    public byte[] onPasswordRequested()
    {
        String password = JOptionPane.showInputDialog( editor.getMainWindow(), "Password" );
        
        if ( password == null )
            return ( null );
        
        return ( MD5Util.md5Bytes( password ) );
    }
    
    public void sendCurrentConfiguration( boolean quietMode )
    {
        communicator.sendWidgetsConfiguration( editor.getWidgetsConfiguration(), editor.getGameResolution() );
        
        if ( !quietMode )
            JOptionPane.showMessageDialog( editor.getMainWindow(), "Sent WidgetsConfiguration." );
    }
    
    public void onConnectionEsteblished( boolean isInCockpitMode )
    {
        if ( isInCockpitMode )
            sendCurrentConfiguration( false );
        
        editor.mergeDirectorConnectionString( communicator.getLastConnectionString() );
        
        JOptionPane.showMessageDialog( editor.getMainWindow(), "Connection esteblished" );
    }
    
    public void onConnectionRefused( String message )
    {
        editor.switchToEditorMode();
        
        JOptionPane.showMessageDialog( editor.getMainWindow(), message );
    }
    
    public void onConnectionClosed()
    {
        editor.switchToEditorMode();
        
        JOptionPane.showMessageDialog( editor.getMainWindow(), "Connection closed" );
    }
    
    public void onDriversListReceived( DriverCapsule[] dcs )
    {
        synchronized ( driversList )
        {
            driversList.clear();
            driversMap.clear();
            
            driversList.add( DriverCapsule.DEFAULT_DRIVER );
            
            for ( int i = 0; i < dcs.length; i++ )
            {
                DriverCapsule dc = dcs[i];
                
                driversList.add( dc );
                driversMap.put( dc.getId(), dc );
                
                //System.out.println( "Received driver: " + dc.getName() + " (" + dc.getId() + ")" );
            }
        }
    }
    
    public void onDriversPositionsReceived( int[] ids )
    {
        synchronized ( driversList )
        {
            driversList.clear();
            
            driversList.add( DriverCapsule.DEFAULT_DRIVER );
            
            for ( int i = 0; i < ids.length; i++ )
            {
                driversList.add( driversMap.get( ids[i] ) );
            }
        }
    }
    
    public void onPitsEntered()
    {
    }
    
    public void onPitsExited()
    {
    }
    
    public void onGarageEntered()
    {
    }
    
    public void onGarageExited()
    {
    }
    
    private int viewedVehicleID = -1;
    
    /**
     * 
     * @param driverID
     * @param control
     */
    public void onVehicleControlChanged( int driverID, VehicleControl control )
    {
        viewedVehicleID = driverID;
    }
    
    /**
     * 
     * @param driverID
     * @param lap
     */
    public void onLapStarted( int driverID, short lap )
    {
        WidgetStatesSet wss = stateSetsListProp.getSelectedValue();
        
        if ( wss == null )
            return;
        
        for ( int i = 0; i < wss.getNumStates(); i++ )
        {
            WidgetState ws = wss.getState( i );
            EffectiveWidgetState ews = ws.getEffectiveState();
            
            if ( ews.getVisibleType() == VisibleType.NEXT_LAP )
            {
                boolean isViewingAuto = ( ews.getForDriver() == null ) || ( ews.getForDriver() == DriverCapsule.DEFAULT_DRIVER );
                
                if ( isViewingAuto && ( driverID == viewedVehicleID ) )
                {
                    ews.setVisibleStart( sessionTime );
                    ews.setVisibleTime( ws.getVisibleTime() );
                    sendWidgetState( ews );
                }
                else if ( !isViewingAuto && ( driverID == ews.getForDriverID() ) )
                {
                    ews.setVisibleStart( sessionTime );
                    ews.setVisibleTime( ws.getVisibleTime() );
                    sendWidgetState( ews );
                }
            }
        }
    }
    
    /**
     * 
     * @param sessionType
     */
    public void onSessionStarted( SessionType sessionType )
    {
        sessionTime = 0L;
        isTimeDecreasing = false;
    }
    
    public void onCockpitEntered()
    {
        sendCurrentConfiguration( true );
    }
    
    /**
     * 
     * @param isPaused
     */
    public void onGamePauseStateChanged( boolean isPaused )
    {
    }
    
    public void onCockpitExited()
    {
    }
    
    public void onPlayerJoined( DriverCapsule dc, short place )
    {
        driversList.add( place - 1, dc );
        driversMap.put( dc.getId(), dc );
    }
    
    public void onPlayerLeft( int id )
    {
        DriverCapsule dc = driversMap.remove( id );
        if ( dc != null )
            driversList.remove( dc );
    }
    
    public void onSessionTimeReceived( long sessionTime )
    {
        isTimeDecreasing = ( sessionTime < this.sessionTime );
        
        this.sessionTime = sessionTime;
    }
    
    public DirectorManager( RFDynHUDEditor editor )
    {
        this.editor = editor;
        editor.getEditorPanel().addWidgetSelectionListener( this );
        
        buildGUI();
    }
}
