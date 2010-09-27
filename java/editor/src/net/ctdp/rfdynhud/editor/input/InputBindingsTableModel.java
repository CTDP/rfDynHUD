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

import java.io.File;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.editor.WidgetSelectionListener;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.input.InputDeviceManager;
import net.ctdp.rfdynhud.input.InputMapping;
import net.ctdp.rfdynhud.input.InputMappingsManager;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.Tools;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.AssembledWidget;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.ini.IniWriter;

public class InputBindingsTableModel extends DefaultTableModel implements WidgetSelectionListener
{
    private static final long serialVersionUID = -6979661218242852874L;
    
    private static final String GLOBAL = "GLOBAL";
    
    private static final String DEFAULT_INPUT_DEVICE = "NO_DEVICE::NO_COMPONENT";
    private static final String PRESS_KEY_OR_BUTTON = "[Press a key or button]";
    
    private final RFDynHUDEditor editor;
    
    private final WidgetsConfiguration widgetsConfig;
    
    private final ArrayList<Object[]> rows = new ArrayList<Object[]>();
    
    private int currentInputPollingRow = -1;
    
    private JTable table = null;
    
    private boolean dirtyFlag = false;
    
    public void setTable( JTable table )
    {
        this.table = table;
    }
    
    public void setDirtyFlag()
    {
        this.dirtyFlag = true;
    }
    
    public void resetDirtyFlag()
    {
        this.dirtyFlag = false;
    }
    
    public final boolean getDirtyFlag()
    {
        return ( dirtyFlag );
    }
    
    public void setInputPollingRow( int row, int updatedRow )
    {
        this.currentInputPollingRow = row;
        fireTableCellUpdated( updatedRow, 2 );
    }
    
    @Override
    public int getColumnCount()
    {
        return ( 4 );
    }
    
    @Override
    public int getRowCount()
    {
        if ( rows == null )
            return ( 1 );
        
        return ( rows.size() + 1 );
    }
    
    @Override
    public Object getValueAt( int row, int column )
    {
        if ( row != rows.size() )
        {
            if ( column == 0 )
            {
                InputMapping mapping = (InputMapping)rows.get( row )[0];
                
                if ( mapping.getAction() == null )
                    return ( "" );
                
                if ( !mapping.getAction().isWidgetAction() )
                    return ( GLOBAL );
                
                return ( mapping.getWidgetName() );
            }
            
            if ( column == 1 )
            {
                InputMapping mapping = (InputMapping)rows.get( row )[0];
                
                return ( mapping.getAction() );
            }
            
            if ( column == 2 )
            {
                if ( row == currentInputPollingRow )
                    return ( PRESS_KEY_OR_BUTTON );
                
                return ( rows.get( row )[1] );
            }
            
            if ( column == 3 )
            {
                InputMapping mapping = (InputMapping)rows.get( row )[0];
                
                return ( mapping.getHitTimes() );
            }
        }
        
        return ( null );
    }
    
    public InputAction getActionFromRow( int row )
    {
        if ( ( row < 0 ) || ( row >= rows.size() ) )
            return ( null );
        
        return ( ( (InputMapping)rows.get( row )[0] ).getAction() );
    }
    
    private boolean widgetHostsAction( Widget widget, InputAction action )
    {
        InputAction[] actions = widget.getInputActions();
        
        if ( actions != null )
        {
            for ( InputAction widgetAction : actions )
            {
                if ( widgetAction.equals( action ) )
                    return ( true );
            }
        }
        
        if ( widget instanceof AssembledWidget )
        {
            for ( int i = 0; i < ( (AssembledWidget)widget ).getNumParts(); i++ )
            {
                if ( widgetHostsAction( ( (AssembledWidget)widget ).getPart( i ), action ) )
                    return ( true );
            }
        }
        
        return ( false );
    }
    
    private String getDefaultWidgetName( InputAction action )
    {
        if ( ( action == null ) || !action.isWidgetAction() )
            return ( GLOBAL );
        
        final int numWidgets = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < numWidgets; i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            if ( widgetHostsAction( widget, action ) )
                return ( widget.getName() );
        }
        
        if ( editor.getEditorPanel().getSelectedWidget() != null )
            return ( editor.getEditorPanel().getSelectedWidget().getName() );
        
        return ( "WIDGET_NAME" );
    }
    
    @Override
    public boolean isCellEditable( int row, int column )
    {
        if ( column == 0 )
        {
            InputAction action = getActionFromRow( row );
            if ( action == null )
                return ( row < rows.size() );
            
            return ( action.isWidgetAction() );
        }
        
        if ( ( column == 2 ) || ( column == 3 ) )
        {
            InputAction action = getActionFromRow( row );
            if ( action == null )
                return ( false );
        }
        
        return ( true );
    }
    
    public void updateDeviceComponent( String deviceComponent, int keyCode, int modifierMask, int row )
    {
        InputMapping oldM = (InputMapping)rows.get( row )[0];
        InputMapping newM = new InputMapping( oldM.getWidgetName(), oldM.getAction(), deviceComponent, keyCode, modifierMask, oldM.getHitTimes() );
        rows.get( row )[0] = newM;
        rows.get( row )[1] = InputMappingsManager.getComponentNameForTable( newM );
        fireTableRowsUpdated( row, row );
        setDirtyFlag();
    }
    
    @Override
    public void setValueAt( Object value, int row, int column )
    {
        if ( Tools.objectsEqual( value, getValueAt( row, column ) ) )
            return;
        
        if ( column == 0 )
        {
            String widgetName = String.valueOf( value );
            
            if ( row == rows.size() )
            {
                if ( widgetName.equals( "" ) )
                    return;
                
                rows.add( new Object[] { new InputMapping( widgetName, null, DEFAULT_INPUT_DEVICE, -1, 0, 1 ), DEFAULT_INPUT_DEVICE } );
                fireTableRowsUpdated( row, row );
                setDirtyFlag();
            }
            else
            {
                InputMapping oldM = (InputMapping)rows.get( row )[0];
                InputMapping newM = new InputMapping( widgetName, oldM.getAction(), oldM.getDeviceComponent(), oldM.getKeyCode(), oldM.getModifierMask(), oldM.getHitTimes() );
                rows.get( row )[0] = newM;
                rows.get( row )[1] = InputMappingsManager.getComponentNameForTable( newM );
                fireTableRowsUpdated( row, row );
                setDirtyFlag();
            }
        }
        else if ( column == 1 )
        {
            if ( value instanceof InputAction )
            {
                InputAction action = (InputAction)value;
                
                if ( row == rows.size() )
                {
                    rows.add( new Object[] { new InputMapping( getDefaultWidgetName( action ), action, DEFAULT_INPUT_DEVICE, -1, 0, 1 ), DEFAULT_INPUT_DEVICE } );
                    fireTableRowsInserted( row, row );
                    fireTableRowsUpdated( row, row );
                    setDirtyFlag();
                }
                else
                {
                    InputMapping oldM = (InputMapping)rows.get( row )[0];
                    InputMapping newM = new InputMapping( oldM.getWidgetName(), action, oldM.getDeviceComponent(), oldM.getKeyCode(), oldM.getModifierMask(), oldM.getHitTimes() );
                    rows.get( row )[0] = newM;
                    rows.get( row )[1] = InputMappingsManager.getComponentNameForTable( newM );
                    fireTableRowsUpdated( row, row );
                    setDirtyFlag();
                }
            }
            else
            {
                InputMapping oldM = (InputMapping)rows.get( row )[0];
                InputMapping newM = new InputMapping( oldM.getWidgetName(), null, oldM.getDeviceComponent(), oldM.getKeyCode(), oldM.getModifierMask(), oldM.getHitTimes() );
                rows.get( row )[0] = newM;
                rows.get( row )[1] = InputMappingsManager.getComponentNameForTable( newM );
                fireTableRowsUpdated( row, row );
                setDirtyFlag();
            }
        }
        else if ( column == 2 )
        {
            /*
            String device_comp = String.valueOf( value );
            
            if ( row == rows.size() )
            {
                rows.add( new Object[] { new InputMapping( "WIDGET_NAME", null, device_comp ), device_comp } );
                fireTableRowsUpdated( row, row );
                setDirtyFlag();
            }
            else
            {
                rows.get( row )[1] = device_comp;
                fireTableRowsUpdated( row, row );
                setDirtyFlag();
            }
            */
        }
        else if ( column == 3 )
        {
            int hitTimes = ( (Number)value ).intValue();
            
            InputMapping oldM = (InputMapping)rows.get( row )[0];
            InputMapping newM = new InputMapping( oldM.getWidgetName(), oldM.getAction(), oldM.getDeviceComponent(), oldM.getKeyCode(), oldM.getModifierMask(), hitTimes );
            rows.get( row )[0] = newM;
            rows.get( row )[1] = InputMappingsManager.getComponentNameForTable( newM );
            fireTableRowsUpdated( row, row );
            setDirtyFlag();
        }
    }
    
    @Override
    public void onWidgetSelected( Widget widget, boolean selectionChanged, boolean doubleClick )
    {
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        
        if ( ( widget != null ) && doubleClick && ( column == 0 ) )
        {
            InputAction action = getActionFromRow( row );
            
            if ( ( action != null ) && widgetHostsAction( widget, action ) )
            {
                if ( table.isEditing() && ( table.getEditingColumn() == 0 ) )
                {
                    table.getCellEditor().stopCellEditing();
                    setValueAt( widget.getName(), row, column );
                }
            }
        }
    }
    
    private void loadBindings( final InputDeviceManager devManager )
    {
        File configFile = new File( GameFileSystem.INSTANCE.getConfigFolder(), InputMappingsManager.CONFIG_FILE_NAME );
        
        if ( !configFile.exists() )
        {
            //JOptionPane.showMessageDialog( null, "No " + InputMappingsManager.CONFIG_FILE_NAME + " config file found in the config folder.", "Error", JOptionPane.ERROR_MESSAGE );
            return;
        }
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    Object[] row = InputMappingsManager.parseMapping( lineNr, key, value, devManager );
                    
                    if ( row != null )
                        rows.add( row );
                    
                    return ( true );
                }
            }.parse( configFile );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    public void saveBindings( final InputDeviceManager devManager )
    {
        try
        {
            IniWriter writer = new IniWriter( new File( GameFileSystem.INSTANCE.getConfigFolder(), InputMappingsManager.CONFIG_FILE_NAME ) );
            
            for ( int i = 0; i < rows.size(); i++ )
            {
                Object[] row = rows.get( i );
                
                if ( row == null )
                    continue;
                
                InputMapping mapping = (InputMapping)row[0];
                
                if ( mapping == null )
                    continue;
                
                String widgetName = mapping.getWidgetName();
                
                if ( ( widgetName == null ) || ( widgetName.trim().length() == 0 ) )
                    continue;
                
                widgetName = widgetName.trim();
                
                InputAction action = mapping.getAction();
                
                if ( action == null )
                    continue;
                
                String device_comp = mapping.getDeviceComponent();
                
                if ( ( device_comp == null ) || ( device_comp.trim().length() == 0 ) )
                    continue;
                
                device_comp = device_comp.trim();
                String[] parts = device_comp.split( "::" );
                if ( parts[0].equals( "Keyboard" ) )
                {
                    //int index = devManager.getKeyIndex( parts[1] );
                    int index = mapping.getKeyCode();
                    parts[1] = devManager.getEnglishKeyName( index );
                }
                else if ( parts[0].equals( "Mouse" ) )
                {
                }
                else // Joystick
                {
                    int index = devManager.getJoystickIndex( parts[0] );
                    if ( index >= 0 )
                    {
                        parts[0] = devManager.getJoystickNameForIni( index );
                        index = devManager.getJoystickButtonIndex( index, parts[1] );
                        if ( index >= 0 )
                            parts[1] = devManager.getJoystickButtonNameForIni( index );
                    }
                }
                
                device_comp = parts[0] + "::";
                device_comp += InputMappingsManager.unparseModifierMask( mapping.getModifierMask() );
                device_comp += parts[1];
                device_comp += "," + mapping.getHitTimes();
                
                
                if ( action.isWidgetAction() )
                    writer.writeSetting( device_comp, widgetName + "::" + action.getName() );
                else
                    writer.writeSetting( device_comp, GLOBAL + "::" + action.getName() );
            }
            
            writer.close();
            
            resetDirtyFlag();
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    public InputBindingsTableModel( RFDynHUDEditor editor, WidgetsConfiguration widgetsConfig, InputDeviceManager devManager )
    {
        this.editor = editor;
        this.widgetsConfig = widgetsConfig;
        
        loadBindings( devManager );
    }
}
