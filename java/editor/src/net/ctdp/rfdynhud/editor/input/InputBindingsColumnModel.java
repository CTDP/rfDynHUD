package net.ctdp.rfdynhud.editor.input;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class InputBindingsColumnModel extends DefaultTableColumnModel
{
    private static final long serialVersionUID = -8316456120753822033L;
    
    public void init()
    {
        TableColumn column = new TableColumn( 0, 250, new WidgetNameEditor(), new WidgetNameEditor() );
        column.setHeaderValue( "Widget Name" );
        addColumn( column );
        
        column = new TableColumn( 1, 250, new InputActionEditor(), new InputActionEditor() );
        column.setHeaderValue( "Action" );
        addColumn( column );
        
        column = new TableColumn( 2, 350, new DeviceComponentEditor(), new DeviceComponentEditor() );
        column.setHeaderValue( "Input Component" );
        addColumn( column );
    }
    
    public InputBindingsColumnModel()
    {
        init();
    }
}
