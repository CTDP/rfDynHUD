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
package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * @param <P> the property type
 * 
 * @author Marvin Froehlich
 */
public abstract class HierarchicalTableModel<P extends Object> extends AbstractTableModel implements MouseListener
{
    private static final long serialVersionUID = -2560300004859208195L;
    
    private HierarchicalTable<P> table = null;
    
    private final String[] columnNames;
    
    private final GridItemsHandler<P> gridItemsHandler;
    
    private GridItemsContainer<P> data;
    private final List<Object> flatData = new ArrayList<Object>();
    private final List<boolean[]> lastInGroup = new ArrayList<boolean[]>();
    private final List<Integer> levels = new ArrayList<Integer>();
    
    private final boolean[] ligTrace = new boolean[ 32 ];
    
    private int columnCount;
    private boolean hasExpandableItems = false;
    
    void setTable( HierarchicalTable<P> table )
    {
        this.table = table;
    }
    
    public final HierarchicalTable<P> getTable()
    {
        return ( table );
    }
    
    public final boolean hasExpandableItems()
    {
        return ( hasExpandableItems );
    }
    
    public final int getFirstNonExpanderColumn()
    {
        if ( hasExpandableItems() )
            return ( 1 );
        
        return ( 0 );
    }
    
    protected final boolean isGroup( Object item )
    {
        return ( gridItemsHandler.isGroup( item ) );
    }
    
    protected final GridItemsContainer<P> toGroup( Object item )
    {
        return ( gridItemsHandler.toGroup( item ) ); 
    }
    
    protected final String getGroupCaption( Object item )
    {
        return ( gridItemsHandler.getGroupCaption( item ) );
    }
    
    /**
     * 
     * @param item the row data item
     * 
     * @return <code>true</code>, if the item is to be ignored for the current view, <code>false</code> otherwise.
     */
    protected boolean isItemIgnored( Object item )
    {
        return ( false );
    }
    
    private boolean computeFlatData( GridItemsContainer<P> items, Boolean expandFlags, int level, List<Object> flatData, boolean[] ligTrace, List<boolean[]> lastInGroup, List<Integer> levels )
    {
        boolean hasExpandableItems = false;
        int n = items.getNumberOfItems();
        
        int lastVisible = -1;
        
        for ( int i = 0; i < n; i++ )
        {
            Object item = items.getItem( i );
            
            if ( !isItemIgnored( item ) )
                lastVisible = i;
        }
        
        for ( int i = 0; i < n; i++ )
        {
            Object item = items.getItem( i );
            
            if ( isItemIgnored( item ) )
                continue;
            
            flatData.add( item );
            
            if ( isGroup( item ) )
                ligTrace[level] = ( ( i == lastVisible ) && !toGroup( item ).getExpandFlag() && ( toGroup( item ).getNumberOfItems() > 0 ) );
            else
                ligTrace[level] = ( i == lastVisible );
            
            if ( lastInGroup.size() < flatData.size() )
            {
                boolean[] lig = new boolean[ ligTrace.length ];
                System.arraycopy( ligTrace, 0, lig, 0, level + 1 );
                lastInGroup.add( lig );
            }
            else
            {
                boolean[] lig = lastInGroup.get( flatData.size() - 1 );
                System.arraycopy( ligTrace, 0, lig, 0, level + 1 );
            }
            
            levels.add( level );
            
            if ( isGroup( item ) )
            {
                hasExpandableItems = true;
                
                GridItemsContainer<P> list = toGroup( item );
                
                if ( expandFlags != null )
                    list.setExpandFlag( expandFlags.booleanValue() );
                
                if ( list.getExpandFlag() )
                {
                    /*boolean result = */computeFlatData( list, expandFlags, level + 1, flatData, ligTrace, lastInGroup, levels );
                }
            }
        }
        
        if ( flatData.size() > 0 )
            lastInGroup.get( flatData.size() - 1 )[level] = true;
        
        return ( hasExpandableItems );
    }
    
    private int keyColumnWidth = -1;
    
    public final int getKeyColumnWidth()
    {
        return ( keyColumnWidth );
    }
    
    protected boolean includeIgnoredItemsInKeyColumnWidthCalculation()
    {
        return ( true );
    }
    
    protected int calcKeyColumnWidth()
    {
        if ( table == null )
            return ( 0 );
        
        FontMetrics metrics = table.getFontMetrics( table.getStyle().getKeyCellFont() );
        Graphics g = table.getGraphics();
        
        int keyColumnIndex = hasExpandableItems() ? 1 : 0;
        int indent = table.getStyle().getLevelIndentation();
        
        final boolean includeIgnored = includeIgnoredItemsInKeyColumnWidthCalculation();
        
        int keyColumnWidth = 0;
        for ( int i = 0; i < flatData.size(); i++ )
        {
            if ( isDataRow( i ) && ( includeIgnored || !isItemIgnored( getRowAt( i ) ) ) )
            {
                int keyWidth = (int)Math.ceil( metrics.getStringBounds( String.valueOf( getValueAt( i, keyColumnIndex ) ), g ).getWidth() );
                
                keyColumnWidth = Math.max( keyColumnWidth, keyWidth + getLevel( i ) * indent );
            }
        }
        
        keyColumnWidth += 4;
        
        return ( keyColumnWidth );
    }
    
    public int updateKeyColumnWidth()
    {
        this.keyColumnWidth = calcKeyColumnWidth();
        
        int keyColumnIndex = hasExpandableItems() ? 1 : 0;
        
        if ( ( table != null ) && ( table.getColumnModel().getColumnCount() > keyColumnIndex ) )
        {
            TableColumn keyColumn = table.getColumnModel().getColumn( keyColumnIndex );
            keyColumn.setMinWidth( keyColumnWidth );
            keyColumn.setMaxWidth( keyColumnWidth );
            keyColumn.setResizable( false );
        }
        
        return ( keyColumnWidth );
    }
    
    private void computeFlatData( Boolean expandFlags )
    {
        flatData.clear();
        //lastInGroup.clear();
        levels.clear();
        
        if ( data == null )
            this.hasExpandableItems = false;
        else
            this.hasExpandableItems = computeFlatData( data, expandFlags, 0, flatData, ligTrace, lastInGroup, levels );
        
        updateKeyColumnWidth();
    }
    
    public void setData( GridItemsContainer<P> data )
    {
        this.data = data;
        this.columnCount = columnNames.length;
        
        computeFlatData( null );
    }
    
    /**
     * 
     * @param changedPropertyName the name of the changed property
     * @param selectedRow the currently selected row
     */
    public void apply( String changedPropertyName, int selectedRow )
    {
        HierarchicalTableColumnModel<P> columnModel = table.getColumnModel();
        
        boolean oldHEI = hasExpandableItems();
        
        computeFlatData( null );
        if ( ( selectedRow >= 0 ) && ( selectedRow >= getRowCount() ) )
        {
            fireTableStructureChanged();
        }
        
        columnModel.init();
        fireTableDataChanged();
        columnModel.init();
        
        if ( oldHEI != hasExpandableItems() )
        {
            fireTableStructureChanged();
            columnModel.init();
        }
    }
    
    @Override
    public String getColumnName( int column )
    {
        /*
        if ( hasExpandableItems )
        {
            if ( column == 0 )
                return ( "" );
            
            return ( columnNames[ column - 1 ] );
        }
        
        return ( columnNames[ column ] );
        */
        
        return( null );
    }
    
    @Override
    public int getRowCount()
    {
        return ( flatData.size() );
    }
    
    @Override
    public int getColumnCount()
    {
        if ( hasExpandableItems() )
            return ( 1 + columnCount );
        
        return ( columnCount );
    }
    
    public final int getLevel( int row )
    {
        if ( ( row < 0 ) || ( row >= levels.size() ) )
            return ( -1 );
        
        return ( levels.get( row ) );
    }
    
    private static final boolean objectsEqual( Object o1, Object o2 )
    {
        if ( o1 == o2 )
            return ( true );
        
        if ( ( o1 == null ) && ( o2 != null ) )
            return ( false );
        
        if ( ( o1 != null ) && ( o2 == null ) )
            return ( false );
        
        return ( o1.equals( o2 ) );
    }
    
    protected abstract void setValueImpl( HierarchicalTable<P> table, P property, int index, Object newValue );
    
    @SuppressWarnings( "unchecked" )
    @Override
    public final void setValueAt( Object value, int row, int column )
    {
        Object rowData = flatData.get( row );
        
        if ( hasExpandableItems() )
        {
            if ( isGroup( rowData ) )
            {
                if ( column == 0 )
                {
                    GridItemsContainer<P> group = toGroup( rowData );
                    
                    if ( group.getExpandFlag() == (Boolean)value )
                        return;
                    
                    group.setExpandFlag( (Boolean)value );
                    computeFlatData( null );
                    
                    fireTableDataChanged();
                }
                else if ( column == 1 )
                {
                    System.out.println( "Cannot set the group caption in a hierarchical grid!" );
                }
                
                return;
            }
            
            if ( column == 0 )
            {
                System.out.println( "Cannot set the value of the expander cell on a non group row in a hierarchical grid!" );
                return;
            }
        }
        
        if ( objectsEqual( value, getValueAt( row, column ) ) )
            return;
        
        setValueImpl( table, (P)rowData, hasExpandableItems() ? column - 1 : column, value );
        
        fireTableDataChanged();
    }
    
    protected abstract Object getValueImpl( HierarchicalTable<P> table, P property, int index );
    
    @SuppressWarnings( "unchecked" )
    @Override
    public final Object getValueAt( int row, int column )
    {
        Object rowData = flatData.get( row );
        
        if ( hasExpandableItems() )
        {
            if ( isGroup( rowData ) )
            {
                if ( column == 0 )
                    return ( toGroup( rowData ).getExpandFlag() );
                
                if ( column == 1 )
                    return ( getGroupCaption( rowData ) );
                
                return ( null );
            }
            
            if ( column == 0 )
                return ( null );
        }
        
        return ( getValueImpl( table, (P)rowData, hasExpandableItems() ? column - 1 : column ) );
    }
    
    public Object getRowAt( int row )
    {
        return ( flatData.get( row ) );
    }
    
    public boolean isDataRow( int row )
    {
        if ( ( row < 0 ) || ( row > flatData.size() ) )
            return ( false );
        
        return ( !isGroup( flatData.get( row ) ) );
    }
    
    public boolean[] getLastInGroup( int row )
    {
        return ( lastInGroup.get( row ) );
    }
    
    @Override
    public boolean isCellEditable( int row, int column )
    {
        /*
        if ( column != getColumnCount() - 1 )
        {
            return ( false );
        }
        
        Object value = getValueAt( row, column );
        
        return ( value != null );
        */
        
        return ( column >= getFirstNonExpanderColumn() + 1 );
    }
    
    public void expandAll()
    {
        if ( !hasExpandableItems() )
            return;
        
        computeFlatData( true );
        
        fireTableDataChanged();
    }
    
    public void collapseAll()
    {
        if ( !hasExpandableItems() )
            return;
        
        computeFlatData( false );
        
        fireTableDataChanged();
    }
    
    private static void readExpandFlags( GridItemsHandler<?> gridItemsHandler, GridItemsContainer<?> list, String keyPrefix, Map<String, Boolean> map )
    {
        for ( int i = 0; i < list.getNumberOfItems(); i++ )
        {
            if ( gridItemsHandler.isGroup( list.getItem( i ) ) )
            {
                GridItemsContainer<?> gic = gridItemsHandler.toGroup( list.getItem( i ) );
                map.put( keyPrefix + gic.getNameForGrid(), gic.getExpandFlag() );
                
                readExpandFlags( gridItemsHandler, gic, keyPrefix, map );
            }
        }
    }
    
    public static void readExpandFlags( GridItemsHandler<?> gridItemsHandler, GridItemsContainer<?> list, Map<String, Boolean> map )
    {
        readExpandFlags( gridItemsHandler, list, "", map );
    }
    
    public void readExpandFlags( Map<String, Boolean> map )
    {
        readExpandFlags( gridItemsHandler, data, "", map );
    }
    
    private static void restoreExpandFlags( GridItemsHandler<?> gridItemsHandler, GridItemsContainer<?> list, String keyPrefix, Map<String, Boolean> map )
    {
        for ( int i = 0; i < list.getNumberOfItems(); i++ )
        {
            if ( gridItemsHandler.isGroup( list.getItem( i ) ) )
            {
                GridItemsContainer<?> gic = gridItemsHandler.toGroup( list.getItem( i ) );
                Boolean b = map.get( keyPrefix + gic.getNameForGrid() );
                if ( b != null )
                    gic.setExpandFlag( b.booleanValue() );
                
                restoreExpandFlags( gridItemsHandler, gic, keyPrefix, map );
            }
        }
    }
    
    public static void restoreExpandFlags( GridItemsHandler<?> gridItemsHandler, GridItemsContainer<?> list, Map<String, Boolean> map )
    {
        restoreExpandFlags( gridItemsHandler, list, "", map );
    }
    
    public void restoreExpandFlags( Map<String, Boolean> map )
    {
        restoreExpandFlags( gridItemsHandler, data, "", map );
    }
    
    @Override
    public void mouseEntered( MouseEvent e )
    {
    }
    
    @Override
    public void mousePressed( MouseEvent e )
    {
        if ( hasExpandableItems() )
        {
            JTable table = (JTable)e.getSource();
            
            int column = table.columnAtPoint( e.getPoint() );
            
            if ( column == 0 )
            {
                int row = table.rowAtPoint( e.getPoint() );
                
                Object value = getValueAt( row, column );
                
                if ( value != null )
                {
                    boolean b = ( (Boolean)value ).booleanValue();
                    
                    if ( table.isEditing() )
                        table.getCellEditor().stopCellEditing();
                    
                    setValueAt( !b, row, column );
                    
                    table.editingCanceled( null );
                }
            }
        }
    }
    
    @Override
    public void mouseReleased( MouseEvent e )
    {
    }
    
    @Override
    public void mouseClicked( MouseEvent e )
    {
    }
    
    @Override
    public void mouseExited( MouseEvent e )
    {
    }
    
    @Override
    public void addTableModelListener( TableModelListener tml )
    {
        super.addTableModelListener( tml );
        
        if ( tml instanceof JTable )
        {
            ( (JTable)tml ).addMouseListener( this );
        }
    }
    
    @Override
    public void removeTableModelListener( TableModelListener tml )
    {
        super.removeTableModelListener( tml );
        
        if ( tml instanceof JTable )
        {
            ( (JTable)tml ).removeMouseListener( this );
        }
    }
    
    public HierarchicalTableModel( GridItemsHandler<P> gridItemsHandler, GridItemsContainer<P> data, int columnCount )
    {
        super();
        
        this.gridItemsHandler= gridItemsHandler;
        
        this.columnNames = new String[ columnCount ];
        
        setData( data );
    }
    
    public HierarchicalTableModel( GridItemsHandler<P> gridItemsHandler, GridItemsContainer<P> data, String... columnNames )
    {
        super();
        
        this.gridItemsHandler= gridItemsHandler;
        
        this.columnNames = columnNames;
        
        setData( data );
    }
}
