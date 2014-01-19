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
package net.ctdp.rfdynhud.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;

/**
 * <p>
 * Iterates all {@link Field} values of a given {@link Class}.
 * This includes all super classes and even private fields.
 * </p>
 * 
 * </p>
 * It is implemented using generics and a dirty trick to access private fields.
 * </p>
 * 
 * @param <T> the runtime type of the iterated fields
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class GenericFieldsIterator<T> implements Iterator<T>
{
    private final Object object;
    private final boolean includeStatic;
    
    private final Class<?>[] clazzes;
    private int clazzIndex = 0;
    private Field[] fields;
    
    private int nextIndex;
    private T next = null;
    
    /**
     * Checks, if the field is of a type, that we want to iterate.
     * 
     * @param field
     * 
     * @return <code>true</code>, if the field is interesting, <code>false</code> otherwise.
     */
    protected abstract boolean checkFieldType( Field field );
    
    @SuppressWarnings( "unchecked" )
    private void findNext()
    {
        next = null;
        
        Field nextField = null;
        for ( int i = nextIndex; i < fields.length; i++ )
        {
            if ( checkFieldType( fields[i] ) )
            {
                nextField = fields[i];
                nextIndex = i + 1;
                break;
            }
        }
        
        if ( nextField != null )
        {
            if ( !includeStatic && ( ( nextField.getModifiers() & Modifier.STATIC ) != 0 ) )
            {
                findNext();
                return;
            }
            
            boolean acc = nextField.isAccessible();
            nextField.setAccessible( true );
            try
            {
                next = (T)nextField.get( object );
            }
            catch ( IllegalAccessException e )
            {
                throw new RuntimeException( e );
            }
            catch ( ExceptionInInitializerError e )
            {
                throw new RuntimeException( e );
            }
            finally
            {
                if ( !acc )
                    nextField.setAccessible( acc );
            }
            
            if ( next == null )
            {
                findNext();
            }
            
            return;
        }
        
        nextIndex = fields.length;
        
        if ( clazzIndex < clazzes.length - 1 )
        {
            fields = clazzes[++clazzIndex].getDeclaredFields();
            nextIndex = 0;
            
            findNext();
        }
    }
    
    @Override
    public boolean hasNext()
    {
        return ( next != null );
    }

    @Override
    public T next()
    {
        T result = next;
        
        findNext();
        
        return ( result );
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException( "remove() not implemented on this iterator" );
    }
    
    private static Class<?>[] getClasses( Object object )
    {
        Class<?> clazz = object.getClass();
        
        int n = 0;
        while ( clazz != Object.class )
        {
            n++;
            
            clazz = clazz.getSuperclass();
        }
        
        Class<?>[] clazzes = new Class[ n ];
        
        clazz = object.getClass();
        
        while ( clazz != Object.class )
        {
            clazzes[--n] = clazz;
            
            clazz = clazz.getSuperclass();
        }
        
        return ( clazzes );
    }
    
    public GenericFieldsIterator( Object object, boolean includeStatic )
    {
        this.object = object;
        this.includeStatic = includeStatic;
        this.clazzes = getClasses( object );
        this.fields = clazzes[0].getDeclaredFields();
        this.nextIndex = 0;
        
        findNext();
    }
    
    public GenericFieldsIterator( Object object )
    {
        this( object, true );
    }
}
