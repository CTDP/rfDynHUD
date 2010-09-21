/**
 * This piece of code has been provided by and with kind
 * permission of INFOLOG GmbH from Germany.
 * It is released under the terms of the GPL, but INFOLOG
 * is still permitted to use it in closed source software.
 */
package net.ctdp.rfdynhud.editor.hiergrid;

/**
 * The {@link GridItemsContainer} keeps items for the hierarchical grid (properties and groups, which are {@link GridItemsContainer}s themselfes).
 * 
 * @param <P> the property type
 * 
 * @author Marvin Froehlich (CTDP)
 */
public interface GridItemsContainer<P extends Object>
{
    /**
     * Gets the name of this container.
     * 
     * @return the name of this container.
     */
    public String getNameForGrid();
    
    /**
     * Sets the expand flag for this container.
     * 
     * @param flag
     */
    public void setExpandFlag( boolean flag );
    
    /**
     * Gets the expand flag for this container.
     * 
     * @return the expand flag for this container.
     */
    public boolean getExpandFlag();
    
    public int getNumberOfItems();
    
    public void addGroup( GridItemsContainer<P> group );
    
    public void addProperty( P property );
    
    public Object getItem( int index );
    
    public void clear();
}
