package net.ctdp.rfdynhud.editor.hiergrid;

import java.util.ArrayList;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public class FlaggedList extends ArrayList< Object >
{
    private static final long serialVersionUID = -2178071328264336996L;
    
    private final String name;
    private boolean expandFlag;
    
    public final String getName()
    {
        return ( name );
    }
    
    public void setExpandFlag( boolean flag )
    {
        this.expandFlag = flag;
    }
    
    public final boolean getExpandFlag()
    {
        return ( expandFlag );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( "FlaggedList \"" + getName() + "\"" );
    }
    
    public FlaggedList( String name, boolean expandFlag, int initialCapacity )
    {
        super( initialCapacity );
        
        this.name = name;
        this.expandFlag = expandFlag;
    }
    
    public FlaggedList( String name, boolean expandFlag )
    {
        this( name, expandFlag, 16 );
    }
    
    public FlaggedList( String name, int initialCapacity )
    {
        this( name, false, initialCapacity );
    }
    
    public FlaggedList( String name )
    {
        this( name, false );
    }
}
