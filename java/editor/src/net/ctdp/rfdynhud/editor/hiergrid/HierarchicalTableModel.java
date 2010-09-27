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
 * @param <P> the property type
 * 
 * @author Marvin Froehlich
 */
public abstract class HierarchicalTableModel<P extends Object> extends AbstractTableModel implements MouseListener
{
    private static final long serialVersionUID = -2560300004859208195L;
    
    private HierarchicalTable<P> table = null;
    
    private final String[] columnNames;
    
    private GridItemsContainer<P> data;
    private final ArrayList<Object> flatData = new ArrayList<Object>();
    private final ArrayList<boolean[]> lastInGroup = new ArrayList<boolean[]>();
    private final ArrayList<Integer> levels = new ArrayList<Integer>();
    
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
    
    protected boolean isGroup( Object item )
    {
        return ( item instanceof GridItemsContainer<?> );
    }
    
    @SuppressWarnings( "unchecked" )
    protected GridItemsContainer<P> toGroup( Object item )
    {
        return ( (GridItemsContainer<P>)item ); 
    }
    
    protected String getGroupCaption( Object item )
    {
        return ( toGroup( item ).getNameForGrid() );
    }
    
    private boolean computeFlatData( GridItemsContainer<P> items, Boolean expandFlags, int level, ArrayList<Object> flatData, boolean[] ligTrace, ArrayList<boolean[]> lastInGroup, ArrayList<Integer> levels )
    {
        boolean hasExpandableItems = false;
        
        for ( int i = 0; i < items.getNumberOfItems(); i++ )
        {
            Object item = items.getItem( i );
            
            flatData.add( item );
            
            ligTrace[level] = ( i == items.getNumberOfItems() - 1 );
            
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
        
        return ( hasExpandableItems );
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
    }
    
    public void setData( GridItemsContainer<P> data )
    {
        this.data = data;
        this.columnCount = columnNames.length;
        
        computeFlatData( null );
    }
    
    /**
     * 
     * @param changedPropertyName
     * @param selectedRow
     */
    public void apply( String changedPropertyName, int selectedRow )
    {
        @SuppressWarnings( "unchecked" )
        HierarchicalTable<P>.HierarchicalTableColumnModel columnModel = (HierarchicalTable<P>.HierarchicalTableColumnModel)table.getColumnModel();
        
        boolean oldHEI = hasExpandableItems();
        
        setData( this.data );
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
        return ( levels.get( row ) );
    }
    
    protected abstract void setValueImpl( HierarchicalTable<P> table, P property, int index, Object newValue );
    
    @SuppressWarnings( "unchecked" )
    @Override
    public final void setValueAt( Object value, int row, int column )
    {
        if ( value == null )
            throw new IllegalArgumentException( "value must not be null." );
        
        /*
        Object rowData = getValueAtRow( data, 0, row );
        
        if ( rowData instanceof Integer )
            return;
        */
        
        Object rowData = flatData.get( row );
        
        if ( hasExpandableItems() )
        {
            if ( column == 0 )
            {
                if ( isGroup( rowData ) )
                {
                    toGroup( rowData ).setExpandFlag( (Boolean)value );
                    computeFlatData( null );
                    
                    fireTableDataChanged();
                }
                
                return;
            }
            
            if ( isGroup( rowData ) )
            {
                if ( column == 1 )
                    return;
                
                return;
            }
            
            //setValueImpl( table, (P)rowData, column - 1, value );
            
            //fireTableDataChanged();
        }
        
        if ( value.equals( getValueAt( row, column ) ) )
            return;
        
        setValueImpl( table, (P)rowData, column - 1, value );
        
        fireTableDataChanged();
    }
    
    protected abstract Object getValueImpl( HierarchicalTable<P> table, P property, int index );
    
    @SuppressWarnings( "unchecked" )
    @Override
    public final Object getValueAt( int row, int column )
    {
        /*
        Object rowData = getValueAtRow( data, 0, row );
        
        if ( rowData instanceof Integer )
            return ( null );
        */
        
        Object rowData = flatData.get( row );
        
        if ( hasExpandableItems() )
        {
            if ( column == 0 )
            {
                if ( isGroup( rowData ) )
                {
                    return ( toGroup( rowData ).getExpandFlag() );
                }
                
                return ( null );
            }
            
            if ( isGroup( rowData ) )
            {
                if ( column == 1 )
                    return ( getGroupCaption( rowData ) );
                
                return ( null );
            }
            
            return ( getValueImpl( table, (P)rowData, column - 1 ) );
        }
        
        return ( getValueImpl( table, (P)rowData, column - 1 ) ); // Why minus 1 ???
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
        
        return ( !isGroup( rowData ) );
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
        
        for ( int i = 0; i < flatData.size(); i++ )
        {
            Object item = flatData.get( i );
            if ( isGroup( item ) )
                toGroup( item ).setExpandFlag( true );
        }
        
        fireTableDataChanged();
    }
    
    public void collapseAll()
    {
        if ( !hasExpandableItems() )
            return;
        
        for ( int i = 0; i < flatData.size(); i++ )
        {
            Object item = flatData.get( i );
            if ( isGroup( item ) )
                toGroup( item ).setExpandFlag( false );
        }
        
        fireTableDataChanged();
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
    
    public HierarchicalTableModel( GridItemsContainer<P> data, int columnCount )
    {
        super();
        
        this.columnNames = new String[ columnCount ];
        
        setData( data );
    }
    
    public HierarchicalTableModel( GridItemsContainer<P> data, String... columnNames )
    {
        super();
        
        this.columnNames = columnNames;
        
        setData( data );
    }
}
