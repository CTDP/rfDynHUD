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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * The {@link HierarchicalTable} is a JTable, that displays hierarchical (structured) data in a
 * nice looking and compressed form.
 * 
 * @param <P> the property type
 * 
 * @author Marvin Froehlich
 */
public class HierarchicalTable<P extends Object> extends JTable
{
    private static final long serialVersionUID = -8967196361910507049L;
    
    private final TableCellRendererProvider<P> rendererProvider;
    private final HierarchicalGridStyle style;
    
    private final ArrayList<PropertySelectionListener<P>> selectionListeners = new ArrayList<PropertySelectionListener<P>>();
    
    private class TableUI extends BasicTableUI
    {
        private void paintCell( Graphics g, Rectangle cellRect, int row, int column )
        {
            if ( table.isEditing() && ( table.getEditingRow() == row ) && ( table.getEditingColumn() == column ) )
            {
                Component component = table.getEditorComponent();
                component.setBounds( cellRect );
                component.validate();
            }
            else if ( column < table.getColumnModel().getColumnCount() )
            {
                TableCellRenderer renderer = table.getCellRenderer( row, column );
                Component component = table.prepareRenderer( renderer, row, column );
                rendererPane.paintComponent( g, component, table, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true );
            }
        }

        private void paintCells( Graphics g, int rMin, int rMax, int cMin, int cMax )
        {
            boolean hasHierarchy = ( (HierarchicalTableModel<?>)table.getModel() ).hasExpandableItems();
            int majorColumn = hasHierarchy ? 1 : 0;
            
            TableColumnModel cm = table.getColumnModel();
            int columnMargin = cm.getColumnMargin();
            
            cMax = Math.min( cMax, cm.getColumnCount() );
            
            Rectangle clip = g.getClipBounds();
            
            Rectangle cellRect;
            TableColumn aColumn;
            int columnWidth;
            for ( int row = rMin; row <= rMax; row++ )
            {
                cellRect = table.getCellRect( row, majorColumn + 1, false );
                if ( cellRect.width > 1 )
                {
                    cellRect = table.getCellRect( row, cMin, false );
                    
                    for ( int column = cMin; column <= cMax; column++ )
                    {
                        aColumn = cm.getColumn( column );
                        columnWidth = aColumn.getWidth();
                        cellRect.width = columnWidth - columnMargin;
                        paintCell( g, cellRect, row, column );
                        cellRect.x += columnWidth;
                    }
                }
                else if ( cMax >= 0 )
                {
                    cellRect = table.getCellRect( row, cMin, false );
                    
                    aColumn = cm.getColumn( 0 );
                    columnWidth = aColumn.getWidth();
                    cellRect.width = columnWidth - columnMargin;
                    paintCell( g, cellRect, row, 0 );
                    
                    cellRect.x += columnWidth;
                    columnWidth = clip.width - columnWidth;
                    cellRect.width = columnWidth - columnMargin;
                    paintCell( g, cellRect, row, 1 );
                }
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void paint( Graphics g, JComponent c )
        {
            Rectangle clip = g.getClipBounds();
            
            Rectangle bounds = table.getBounds();
            // account for the fact that the graphics has already been translated
            // into the table's bounds
            bounds.x = bounds.y = 0;
            
            // this check prevents us from painting the entire table
            // when the clip doesn't intersect our bounds at all
            if ( table.getRowCount() <= 0 || table.getColumnCount() <= 0 || !bounds.intersects( clip ) )
            {
                return;
            }
            
            Point upperLeft = clip.getLocation();
            Point lowerRight = new Point( clip.x + clip.width - 1, clip.y + clip.height - 1 );
            int rMin = table.rowAtPoint( upperLeft );
            int rMax = table.rowAtPoint( lowerRight );
            // This should never happen (as long as our bounds intersect the clip,
            // which is why we bail above if that is the case).
            if ( rMin == -1 )
            {
                rMin = 0;
            }
            // If the table does not have enough rows to fill the view we'll get -1.
            // (We could also get -1 if our bounds don't intersect the clip,
            // which is why we bail above if that is the case).
            // Replace this with the index of the last row.
            if ( rMax == -1 )
            {
                rMax = table.getRowCount() - 1;
            }
            
            boolean ltr = table.getComponentOrientation().isLeftToRight();
            int cMin = table.columnAtPoint( ltr ? upperLeft : lowerRight );
            int cMax = table.columnAtPoint( ltr ? lowerRight : upperLeft );
            // This should never happen.
            if ( cMin == -1 )
            {
                cMin = 0;
            }
            // If the table does not have enough columns to fill the view we'll get -1.
            // Replace this with the index of the last column.
            if ( cMax == -1 )
            {
                cMax = table.getColumnCount() - 1;
            }
            
            if ( cMax >= getColumnModel().getColumnCount() )
            {
                cMax = getColumnModel().getColumnCount() - 1;
            }
            
            // Paint the grid.
            //paintGrid(g, rMin, rMax, cMin, cMax);
            
            // Paint the cells.
            paintCells( g, rMin, rMax, cMin, cMax );
        }
        
        public TableUI()
        {
            super();
        }
    }
    
    public final HierarchicalGridStyle getStyle()
    {
        return ( style );
    }
    
    public void addPropertySelectionListener( PropertySelectionListener<P> l )
    {
        selectionListeners.add( l );
    }
    
    public void removePropertySelectionListener( PropertySelectionListener<P> l )
    {
        selectionListeners.remove( l );
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public HierarchicalTableModel<P> getModel()
    {
        return ( (HierarchicalTableModel<P>)super.getModel() );
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public HierarchicalTableColumnModel<P> getColumnModel()
    {
        return ( (HierarchicalTableColumnModel<P>)super.getColumnModel() );
    }
    
    public void applyToModel()
    {
        int selectedRow = getSelectedRow();
        
        this.getModel().apply( null, selectedRow );
        
        /*
        selectedRow = Math.min( selectedRow, getRowCount() - 1 );
        if ( selectedRow < 0 )
            clearSelection();
        else
            setRowSelectionInterval( selectedRow, selectedRow );
        */
    }
    
    @Override
    public Rectangle getCellRect( int row, int column, boolean includeSpacing )
    {
        HierarchicalTableModel<P> model = getModel();
        
        Rectangle rect = super.getCellRect( row, column, includeSpacing );
        
        if ( model.hasExpandableItems() && ( column == 0 ) )
        {
            return ( rect );
        }
        
        if ( !model.isDataRow( row ) )
        {
            int firstCol = getModel().getFirstNonExpanderColumn();
            
            if ( column == firstCol )
            {
                TableColumnModel cm = getColumnModel();
                
                int width = 0;
                for ( int i = column; i < cm.getColumnCount(); i++ )
                    width += cm.getColumn( i ).getWidth();
                
                rect.width = width;
            }
            else
            {
                rect.x += rect.width - 1;
                rect.width = 1;
            }
            
            return ( rect );
        }
        
        return ( rect );
    }
    
    protected TableCellRenderer createGroupHeaderRenderer()
    {
        return ( new GroupHeaderCellRenderer() );
    }
    
    private final TableCellRenderer groupHeaderRenderer = createGroupHeaderRenderer();
    
    @SuppressWarnings( "unchecked" )
    @Override
    public TableCellRenderer getCellRenderer( int row, int column )
    {
        boolean hasExpandables = getModel().hasExpandableItems();
        
        if ( hasExpandables && ( column == 0 ) )
            return ( super.getCellRenderer( row, column ) );
        
        if ( !getModel().isDataRow( row ) )
            return ( groupHeaderRenderer );
        
        TableCellRenderer renderer = getColumnModel().getColumn( column ).getCellRenderer();
        if ( renderer == null )
        {
            if ( rendererProvider == null )
                return ( super.getCellRenderer( row, column ) );
            
            renderer = rendererProvider.getDataCellRenderer( this, row, hasExpandables ? column - 2 : column - 1, (P)getModel().getRowAt( row ) );
            
            if ( renderer == null )
                renderer = super.getCellRenderer( row, column );
        }
        
        return ( renderer );
    }
    
    @Override
    public TableCellEditor getCellEditor( int row, int column )
    {
        boolean hasExpandables = getModel().hasExpandableItems();
        
        if ( column == 0 )
            return ( null );
        
        if ( ( hasExpandables ) && ( column == 1 ) )
            return ( null );
        
        if ( rendererProvider == null )
            return ( super.getCellEditor( row, column ) );
        
        @SuppressWarnings( "unchecked" )
        TableCellEditor editor = rendererProvider.getDataCellEditor( this, row, hasExpandables ? column - 2 : column - 1, (P)getModel().getRowAt( row ) );
        
        if ( editor == null )
            editor = super.getCellEditor( row, column );
        
        return ( editor );
    }
    
    boolean stopping = false;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void editingStopped( ChangeEvent e )
    {
        if ( !stopping )
        {
            stopping = true;
            
            try
            {
                super.editingStopped( e );
            }
            finally
            {
                stopping = false;
            }
        }
    }
    
    private void addSelectionListener()
    {
        getSelectionModel().addListSelectionListener( new ListSelectionListener()
        {
            @SuppressWarnings( "unchecked" )
            @Override
            public void valueChanged( ListSelectionEvent e )
            {
                if ( !e.getValueIsAdjusting() && ( selectionListeners != null ) )
                {
                    final P property;
                    if ( HierarchicalTable.this.getModel().isDataRow( HierarchicalTable.this.getSelectedRow() ) )
                        property = (P)HierarchicalTable.this.getModel().getRowAt( HierarchicalTable.this.getSelectedRow() );
                    else
                        property = null;
                    
                    for ( int i = 0; i < selectionListeners.size(); i++ )
                    {
                        selectionListeners.get( i ).onPropertySelected( property, HierarchicalTable.this.getSelectedRow() );
                    }
                }
            }
        } );
    }
    
    public JScrollPane createScrollPane()
    {
        JScrollPane scrollPane = new JScrollPane( this );
        scrollPane.getViewport().setBackground( java.awt.Color.WHITE );
        scrollPane.getVerticalScrollBar().setUnitIncrement( getStyle().getRowHeight() );
        
        return ( scrollPane );
    }
    
    @Override
    protected TableColumnModel createDefaultColumnModel()
    {
        return ( new HierarchicalTableColumnModel<P>( this ) );
    }
    
    public HierarchicalTable( HierarchicalTableModel<P> model, TableCellRendererProvider<P> rendererProvider, HierarchicalGridStyle style )
    {
        super( model, null );
        
        if ( model == null )
            throw new IllegalArgumentException( "model must not be null." );
        
        model.setTable( this );
        
        this.rendererProvider = rendererProvider;
        this.style = style;
        
        getColumnModel().init();
        
        getTableHeader().setReorderingAllowed( false );
        getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        setShowGrid( false );
        setShowHorizontalLines( false );
        setShowVerticalLines( false );
        setRowMargin( 0 );
        setRowHeight( style.getRowHeight() );
        setIntercellSpacing( new Dimension( 0, 0 ) );
        
        setUI( new TableUI() );
        
        style.applyDefaults( this );
        
        if ( style.getTableBackgroundColor() != null )
            this.setBackground( style.getTableBackgroundColor() );
        
        addSelectionListener();
    }
    
    public HierarchicalTable( HierarchicalTableModel<P> model, TableCellRendererProvider<P> rendererProvider )
    {
        this( model, rendererProvider, new HierarchicalGridStyle() );
    }
}
