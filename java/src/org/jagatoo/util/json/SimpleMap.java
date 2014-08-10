package org.jagatoo.util.json;

/**
 * This class just serves for debugging purposes and can be used to easily strep through with the debugger and see the values.
 * 
 * @author Marvin Froehlich
 */
class SimpleMap
{
    private static class Entry
    {
        @SuppressWarnings( "unused" )
        final String name;
        @SuppressWarnings( "unused" )
        final Object value;
        
        public Entry( String name, Object value )
        {
            this.name = name;
            this.value = value;
        }
    }
    
    private Entry[] array = new Entry[ 0 ];
    
    public void put( String name, Object object )
    {
        Entry[] tmpArray = new Entry[ array.length + 1 ];
        System.arraycopy( array, 0, tmpArray, 0, array.length );
        tmpArray[array.length] = new Entry( name, object );
        this.array = tmpArray;
    }
}
