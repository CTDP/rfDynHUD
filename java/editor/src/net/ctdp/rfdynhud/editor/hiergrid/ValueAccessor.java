package net.ctdp.rfdynhud.editor.hiergrid;

import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public interface ValueAccessor
{
    public void setValue( JTable table, TableModel model, Object prop, int index, Object newValue );
    
    public Object getValue( JTable table, TableModel model, Object prop, int index );
}
