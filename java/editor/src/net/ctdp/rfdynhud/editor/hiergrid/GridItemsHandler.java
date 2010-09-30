package net.ctdp.rfdynhud.editor.hiergrid;

/**
 * Identifies groups.
 * 
 * @author Marvin Froehlich
 *
 * @param <P> the property type
 */
public class GridItemsHandler<P extends Object>
{
    public boolean isGroup( Object item )
    {
        return ( item instanceof GridItemsContainer<?> );
    }
    
    @SuppressWarnings( "unchecked" )
    public GridItemsContainer<P> toGroup( Object item )
    {
        return ( (GridItemsContainer<P>)item ); 
    }
    
    public String getGroupCaption( Object item )
    {
        return ( toGroup( item ).getNameForGrid() );
    }
}
