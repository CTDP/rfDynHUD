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
package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 * @author Marvin Froehlich (CTDP) (aka Qudus)
 */
public class HierarchicalTableModel extends AbstractTableModel implements MouseListener
{
    private static final long serialVersionUID = -2560300004859208195L;
    
    private final String[] columnNames;
    
    private GridItemsContainer data;
    private final ArrayList<Object> flatData = new ArrayList<Object>();
    private final ArrayList<boolean[]> lastInGroup = new ArrayList<boolean[]>();
    private final ArrayList<Integer> levels = new ArrayList<Integer>();
    
    private final boolean[] ligTrace = new boolean[ 32 ];
    
    private int columnCount;
    private boolean hasExpandableItems = false;
    
    private final ValueAccessor accessor;
    
    public final boolean hasExpandableItems()
    {
        return ( hasExpandableItems );
    }
    
    public final int getFirstNonExpanderColumn()
    {
        if ( hasExpandableItems )
            return ( 1 );
        
        return ( 0 );
    }
    
    private boolean computeFlatData( GridItemsContainer items, int level, ArrayList<Object> flatData, boolean[] ligTrace, ArrayList<boolean[]> lastInGroup, ArrayList<Integer> levels )
    {
        boolean hasExpandableItems = false;
        
        for ( int i = 0; i < items.size(); i++ )
        {
            Object item = items.get( i );
            
            flatData.add( item );
            
            ligTrace[level] = ( i == items.size() - 1 );
            
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
            
            if ( item instanceof GridItemsContainer )
            {
                hasExpandableItems = true;
                
                GridItemsContainer list = (GridItemsContainer)item;
                
                if ( list.getExpandFlag() )
                {
                    /*boolean result = */computeFlatData( list, level + 1, flatData, ligTrace, lastInGroup, levels );
                }
            }
        }
        
        return ( hasExpandableItems );
    }
    
    private void computeFlatData( GridItemsContainer items )
    {
        flatData.clear();
        //lastInGroup.clear();
        levels.clear();
        
        this.hasExpandableItems = computeFlatData( items, 0, flatData, ligTrace, lastInGroup, levels );
    }
    
    public void setData( GridItemsContainer data )
    {
        this.data = data;
        this.columnCount = columnNames.length;
        
        computeFlatData( data );
    }
    
    /**
     * 
     * @param changedPropertyName
     * @param columnModel
     */
    public void apply( String changedPropertyName, HierarchicalTableColumnModel columnModel )
    {
        boolean oldHEI = hasExpandableItems;
        
        setData( this.data );
        columnModel.init();
        fireTableDataChanged();
        columnModel.init();
        
        if ( oldHEI != hasExpandableItems )
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
        if ( hasExpandableItems )
            return ( 1 + columnCount );
        
        return ( columnCount );
    }
    
    public final int getLevel( int row )
    {
        return ( levels.get( row ) );
    }
    
    /*
    @Override
    protected Object getValueAtRow( FlaggedList items, int rowOffset, int row )
    {
        for ( int i = 0; i < items.size(); i++ )
        {
            Object item = items.get( i );
            
            if ( rowOffset == row )
                return ( item );
            
            rowOffset++;
            
            if ( item instanceof FlaggedList )
            {
                FlaggedList list = (FlaggedList)item;
                
                if ( list.getExpandFlag() )
                {
                    Object result = getValueAtRow( list, rowOffset, row );
                    
                    if ( !( result instanceof Integer ) )
                        return ( result );
                    
                    rowOffset = ( (Integer)result ).intValue();
                }
            }
        }
        
        return ( rowOffset );
    }
    */
    
    @Override
    public void setValueAt( Object value, int row, int column )
    {
        if ( value == null )
            throw new IllegalArgumentException( "value must not be null." );
        
        /*
        Object rowData = getValueAtRow( data, 0, row );
        
        if ( rowData instanceof Integer )
            return;
        */
        
        Object rowData = flatData.get( row );
        
        if ( hasExpandableItems )
        {
            if ( column == 0 )
            {
                if ( rowData instanceof GridItemsContainer )
                {
                    ( (GridItemsContainer)rowData ).setExpandFlag( (Boolean)value );
                    computeFlatData( data );
                    
                    fireTableDataChanged();
                }
                
                return;
            }
            
            if ( rowData instanceof GridItemsContainer )
            {
                if ( column == 1 )
                    return;
                
                return;
            }
            
            //accessor.setValue( (JTable)this.getTableModelListeners()[this.getTableModelListeners().length - 1], this, rowData, column - 1, value );
            
            //fireTableDataChanged();
        }
        
        if ( value.equals( getValueAt( row, column ) ) )
            return;
        
        accessor.setValue( (JTable)this.getTableModelListeners()[this.getTableModelListeners().length - 1], this, rowData, column - 1, value );
        
        fireTableDataChanged();
    }
    
    @Override
    public Object getValueAt( int row, int column )
    {
        /*
        Object rowData = getValueAtRow( data, 0, row );
        
        if ( rowData instanceof Integer )
            return ( null );
        */
        
        Object rowData = flatData.get( row );
        
        if ( hasExpandableItems )
        {
            if ( column == 0 )
            {
                if ( rowData instanceof GridItemsContainer )
                {
                    return ( ( (GridItemsContainer)rowData ).getExpandFlag() );
                }
                
                return ( null );
            }
            
            if ( rowData instanceof GridItemsContainer )
            {
                if ( column == 1 )
                    return ( rowData );
                
                return ( null );
            }
            
            return ( accessor.getValue( (JTable)this.getTableModelListeners()[this.getTableModelListeners().length - 1], this, rowData, column - 1 ) );
        }
        
        return ( accessor.getValue( (JTable)this.getTableModelListeners()[this.getTableModelListeners().length - 1], this, rowData, column - 1 ) ); // Why minus 1 ???
    }
    
    public Object getRowAt( int row )
    {
        /*
        Object rowData = getValueAtRow( data, 0, row );
        
        if ( rowData instanceof Integer )
            return ( null );
        */
        
        Object rowData = flatData.get( row );
        
        return ( rowData );
    }
    
    public boolean isDataRow( int row )
    {
        if ( ( row < 0 ) || ( row > flatData.size() ) )
            return ( false );
        
        //Object rowData = getValueAtRow( data, 0, row );
        Object rowData = flatData.get( row );
        
        return ( !( rowData instanceof GridItemsContainer ) );
    }
    
    public boolean[] getLastInGroup( int row )
    {
        return ( lastInGroup.get( row ) );
    }
    
    @Override
    public boolean isCellEditable( int row, int column )
    {
        if ( column != getColumnCount() - 1 )
        {
            return ( false );
        }
        
        Object value = getValueAt( row, column );
        
        return ( value != null );
    }
    
    @Override
    public void mouseEntered( MouseEvent e )
    {
    }
    
    @Override
    public void mousePressed( MouseEvent e )
    {
        JTable table = (JTable)e.getSource();
        
        if ( hasExpandableItems() )
        {
            int column = table.columnAtPoint( e.getPoint() );
            
            if ( column == 0 )
            {
                int row = table.rowAtPoint( e.getPoint() );
                
                Object value = getValueAt( row, column );
                
                if ( value != null )
                {
                    boolean b = ( (Boolean)value ).booleanValue();
                    
                    setValueAt( !b, row, column );
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
    
    public HierarchicalTableModel( GridItemsContainer data, ValueAccessor accessor, int columnCount )
    {
        super();
        
        this.columnNames = new String[ columnCount ];
        
        setData( data );
        
        this.accessor = accessor;
    }
    
    public HierarchicalTableModel( GridItemsContainer data, ValueAccessor accessor, String... columnNames )
    {
        super();
        
        this.columnNames = columnNames;
        
        setData( data );
        
        this.accessor = accessor;
    }
}
