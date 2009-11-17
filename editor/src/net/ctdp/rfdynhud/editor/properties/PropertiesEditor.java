package net.ctdp.rfdynhud.editor.properties;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;


import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableColumnModel;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;

/**
 * 
 * @author Marvin Froehlich
 */
public class PropertiesEditor extends DefaultTableModel
{
    private static final long serialVersionUID = -1723298567515621091L;
    
    private final RFDynHUDEditor editor;
    
    private final EditorTable table;
    private final JScrollPane scrollPane;
    
    private FlaggedList properties;
    
    public final Component getGUI()
    {
        return ( scrollPane );
    }
    
    public final EditorTable getTable()
    {
        return ( table );
    }
    
    public void clear()
    {
        properties.clear();
    }
    
    public final FlaggedList getPropertiesList()
    {
        return ( properties );
    }
    
    public void addProperty( Property p )
    {
        properties.add( p );
    }
    
    public void addProperties( FlaggedList props )
    {
        properties.add( props );
    }
    
    public Property getProperty( int row )
    {
        Object obj = properties.get( row );
        
        if ( obj instanceof Property )
            return ( (Property)obj );
        
        return ( null );
    }
    
    public int getRowCount()
    {
        if ( properties == null )
            return ( 0 );
        
        return ( properties.size() );
    }
    
    public int getColumnCount()
    {
        return ( 2 );
    }
    
    @Override
    public String getColumnName( int column )
    {
        return ( null );
    }
    
    private static final boolean needsAreaClear( Property p )
    {
        if ( p.getKey().equals( "x" ) )
            return ( true );
        
        if ( p.getKey().equals( "y" ) )
            return ( true );
        
        if ( p.getKey().equals( "width" ) )
            return ( true );
        
        if ( p.getKey().equals( "height" ) )
            return ( true );
        
        if ( p.getKey().equals( "initialVisibility" ) )
            return ( true );
        
        return ( false );
    }
    
    @Override
    public void setValueAt( Object value, int row, int column )
    {
        if ( column == 2 )
        {
            Property prop = getProperty( row );
            if ( prop != null )
            {
                if ( needsAreaClear( prop ) )
                    editor.getEditorPanel().clearSelectedWidget();
                
                prop.setValue( value );
                
                editor.getEditorPanel().repaint();
            }
        }
    }
    
    @Override
    public Object getValueAt( int row, int column )
    {
        if ( column == 0 )
            return ( getProperty( row ).getKeyForDisplay() );
        
        return ( getProperty( row ).getValue() );
    }
    
    public void apply()
    {
        ( (HierarchicalTableModel)table.getModel() ).apply( null, (HierarchicalTableColumnModel)table.getColumnModel() );
    }
    
    public PropertiesEditor( RFDynHUDEditor editor )
    {
        super();
        
        this.editor = editor;
        
        this.properties = new FlaggedList( "properties::" );
        
        this.table = new EditorTable( editor, properties );
        table.setRowHeight( 20 );
        this.scrollPane = new JScrollPane( table );
        scrollPane.getViewport().setBackground( java.awt.Color.WHITE );
        scrollPane.getVerticalScrollBar().setUnitIncrement( 10 );
    }
}
