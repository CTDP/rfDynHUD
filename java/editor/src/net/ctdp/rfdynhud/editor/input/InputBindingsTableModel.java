package net.ctdp.rfdynhud.editor.input;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.input.InputMapping;
import net.ctdp.rfdynhud.input.KnownInputActions;
import net.ctdp.rfdynhud.util.RFactorTools;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.ini.IniWriter;

public class InputBindingsTableModel extends DefaultTableModel
{
    private static final long serialVersionUID = -6979661218242852874L;
    
    private static final String DEFAULT_INPUT_DEVICE = "NO_DEVICE::NO_COMPONENT";
    
    private final RFDynHUDEditor editor;
    
    private final WidgetsConfiguration widgetsConfig;
    
    private final ArrayList<Object[]> rows = new ArrayList<Object[]>();
    
    private int currentInputPollingRow = -1;
    
    public void setInputPollingRow( int row, int updatedRow )
    {
        this.currentInputPollingRow = row;
        fireTableCellUpdated( updatedRow, 2 );
    }
    
    @Override
    public int getColumnCount()
    {
        return ( 3 );
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
        if ( row == rows.size() )
            return ( null );
        
        if ( column < 2 )
        {
            InputMapping mapping = (InputMapping)rows.get( row )[0];
            
            if ( column == 0 )
                return ( mapping.getWidgetName() );
            
            return ( mapping.getAction() );
        }
        
        if ( ( column == 2 ) && ( row == currentInputPollingRow ) )
            return ( "[Press a key or button]" );
        
        return ( rows.get( row )[column - 1] );
    }
    
    public InputAction getActionFromRow( int row )
    {
        if ( ( row < 0 ) || ( row >= rows.size() ) )
            return ( null );
        
        return ( ( (InputMapping)rows.get( row )[0] ).getAction() );
    }
    
    private String getDefaultWidgetName( InputAction action )
    {
        final int numWidgets = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < numWidgets; i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            InputAction[] actions = widget.getInputActions();
            
            if ( actions != null )
            {
                for ( InputAction widgetAction : actions )
                {
                    if ( widgetAction.equals( action ) )
                        return ( widget.getName() );
                }
            }
        }
        
        if ( !action.isWidgetAction() )
            return ( "GLOBAL" );
        
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
                return ( true );
            
            return ( action.isWidgetAction() );
        }
        
        return ( true );
    }
    
    @Override
    public void setValueAt( Object value, int row, int column )
    {
        if ( column == 0 )
        {
            String widgetName = String.valueOf( value );
            
            if ( row == rows.size() )
            {
                if ( widgetName.equals( "" ) )
                    return;
                
                rows.add( new Object[] { new InputMapping( widgetName, null ), DEFAULT_INPUT_DEVICE } );
            }
            else
            {
                rows.get( row )[0] = new InputMapping( widgetName, getActionFromRow( row ) );
            }
        }
        else if ( column == 1 )
        {
            InputAction action = (InputAction)value;
            
            if ( row == rows.size() )
            {
                rows.add( new Object[] { new InputMapping( getDefaultWidgetName( action ), action ), DEFAULT_INPUT_DEVICE } );
            }
            else
            {
                InputMapping mapping = (InputMapping)rows.get( row )[0];
                
                String widgetName = mapping.getWidgetName();
                
                rows.get( row )[0] = new InputMapping( widgetName, action );
            }
        }
        else
        {
            String device_comp = String.valueOf( value );
            
            if ( row == rows.size() )
            {
                rows.add( new Object[] { new InputMapping( "WIDGET_NAME", null ), device_comp } );
            }
            else
            {
                rows.get( row )[1] = device_comp;
            }
        }
    }
    
    private void loadBindings()
    {
        File configFile = new File( RFactorTools.CONFIG_PATH + File.separator + "input_bindings.ini" );
        
        if ( !configFile.exists() )
        {
            //JOptionPane.showMessageDialog( null, "No input_bindings.ini config file found in the config folder.", "Error", JOptionPane.ERROR_MESSAGE );
            return;
        }
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    key = key.trim();
                    value = value.trim();
                    
                    /*
                    String[] keyParts = key.split( "::", 2 );
                    String device;
                    if ( keyParts.length == 1 )
                        device = "Keyboard";
                    else
                        device = keyParts[0];
                    String component = keyParts[1];
                    */
                    
                    String[] valueParts = value.split( "::", 2 );
                    String widgetName = valueParts[0];
                    String actionName;
                    if ( valueParts.length == 1 )
                        actionName = null;
                    else
                        actionName = valueParts[1];
                    
                    InputAction action = KnownInputActions.get( actionName );
                    
                    rows.add( new Object[] { new InputMapping( widgetName, action ), key } );
                    
                    return ( true );
                }
            }.parse( configFile );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
    }
    
    public void saveBindings()
    {
        try
        {
            IniWriter writer = new IniWriter( RFactorTools.CONFIG_PATH + File.separator + "input_bindings.ini" );
            
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
                
                String device_comp = (String)row[1];
                
                if ( ( device_comp == null ) || ( device_comp.trim().length() == 0 ) )
                    continue;
                
                device_comp = device_comp.trim();
                
                
                if ( action.isWidgetAction() )
                    writer.writeSetting( device_comp, widgetName + "::" + action.getName() );
                else
                    writer.writeSetting( device_comp, "GLOBAL::" + action.getName() );
            }
            
            writer.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
    
    public InputBindingsTableModel( RFDynHUDEditor editor, WidgetsConfiguration widgetsConfig )
    {
        this.editor = editor;
        this.widgetsConfig = widgetsConfig;
        
        loadBindings();
    }
}
