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
    
    private FlaggedList data;
    private final ArrayList< Integer > levels = new ArrayList< Integer >();
    
    private int rowCount;
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
    
    private int computeRowCount( FlaggedList items, int level )
    {
        if ( level == 0 )
        {
            levels.clear();
        }
        
        int rowCount = 0;
        
        for ( int i = 0; i < items.size(); i++ )
        {
            Object item = items.get( i );
            
            rowCount++;
            
            levels.add( level );
            
            if ( item instanceof FlaggedList )
            {
                FlaggedList list = (FlaggedList)item;
                
                if ( list.getExpandFlag() )
                {
                    rowCount += computeRowCount( list, level + 1 );
                }
            }
        }
        
        return ( rowCount );
    }
    
    public void setData( FlaggedList data )
    {
        this.data = data;
        this.rowCount = computeRowCount( data, 0 );
        this.columnCount = columnNames.length;
        this.hasExpandableItems = false;
        
        for ( int i = 0; i < data.size(); i++ )
        {
            if ( data.get( i ) instanceof FlaggedList )
            {
                this.hasExpandableItems = true;
                
                return;
            }
        }
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
    
    public int getRowCount()
    {
        return ( rowCount );
    }
    
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
    
    @Override
    public void setValueAt( Object value, int row, int column )
    {
        if ( value == null )
            throw new IllegalArgumentException( "value must not be null." );
        
        Object rowData = getValueAtRow( data, 0, row );
        
        if ( rowData instanceof Integer )
            return;
        
        if ( hasExpandableItems )
        {
            if ( column == 0 )
            {
                if ( rowData instanceof FlaggedList )
                {
                    ( (FlaggedList)rowData ).setExpandFlag( (Boolean)value );
                    this.rowCount = computeRowCount( data, 0 );
                    
                    fireTableDataChanged();
                }
                
                return;
            }
            
            if ( rowData instanceof FlaggedList )
            {
                if ( column == 1 )
                    return;
                
                return;
            }
            
            //accessor.setValue( (JTable)this.getTableModelListeners()[this.getTableModelListeners().length - 1], this, rowData, column - 1, value );
            
            //fireTableDataChanged();
        }
        
        accessor.setValue( (JTable)this.getTableModelListeners()[this.getTableModelListeners().length - 1], this, rowData, column - 1, value );
        
        fireTableDataChanged();
    }
    
    public Object getValueAt( int row, int column )
    {
        Object rowData = getValueAtRow( data, 0, row );
        
        if ( rowData instanceof Integer )
            return ( null );
        
        if ( hasExpandableItems )
        {
            if ( column == 0 )
            {
                if ( rowData instanceof FlaggedList )
                {
                    return ( ( (FlaggedList)rowData ).getExpandFlag() );
                }
                
                return ( null );
            }
            
            if ( rowData instanceof FlaggedList )
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
        Object rowData = getValueAtRow( data, 0, row );
        
        if ( rowData instanceof Integer )
            return ( null );
        
        return ( rowData );
    }
    
    public boolean isDataRow( int row )
    {
        Object rowData = getValueAtRow( data, 0, row );
        
        return ( !( rowData instanceof FlaggedList ) );
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
    
    public void mouseEntered( MouseEvent e )
    {
    }
    
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
    
    public void mouseReleased( MouseEvent e )
    {
    }
    
    public void mouseClicked( MouseEvent e )
    {
    }
    
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
    
    public HierarchicalTableModel( FlaggedList data, ValueAccessor accessor, int columnCount )
    {
        super();
        
        this.columnNames = new String[ columnCount ];
        
        setData( data );
        
        this.accessor = accessor;
    }
    
    public HierarchicalTableModel( FlaggedList data, ValueAccessor accessor, String... columnNames )
    {
        super();
        
        this.columnNames = columnNames;
        
        setData( data );
        
        this.accessor = accessor;
    }
}
