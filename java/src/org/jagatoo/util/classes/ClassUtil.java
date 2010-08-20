/**
 * Copyright (c) 2007-2009, JAGaToo Project Group all rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of the 'Xith3D Project Group' nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
 * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE
 */
package org.jagatoo.util.classes;

import java.lang.reflect.Method;

/**
 * The {@link ClassUtil} provides utility methods to deal with classes.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class ClassUtil
{
    /**
     * Gets the public method from the given class without exceptions.
     * 
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * 
     * @return the method or null, if it doesn't exist or other problems occurr.
     */
    public static Method getPublicMethod( Class<?> clazz, String methodName, Class<?>... parameterTypes )
    {
        Method m = null;
        
        try
        {
            m = clazz.getMethod( methodName, parameterTypes );
        }
        catch ( Throwable t )
        {
        }
        
        return ( m );
    }
    
    private static Method findMethod_( Class<?> clazz, String methodName, Class<?>... parameterTypes )
    {
        Method m = null;
        
        try
        {
            m = clazz.getDeclaredMethod( methodName, parameterTypes );
        }
        catch ( Throwable t )
        {
        }
        
        if ( ( m == null ) && ( clazz.getSuperclass() != null ) )
            return ( findMethod_( clazz.getSuperclass(), methodName, parameterTypes ) );
        
        return ( m );
    }
    
    /**
     * Gets the method from the given class without exceptions and also searches super classes.
     * 
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * 
     * @return the method or null, if it doesn't exist or other problems occurr.
     */
    public static Method findMethod( Class<?> clazz, String methodName, Class<?>... parameterTypes )
    {
        Method m = getPublicMethod( clazz, methodName, parameterTypes );
        
        if ( m != null )
            return ( m );
        
        return ( findMethod_( clazz, methodName, parameterTypes ) );
    }
    
    /**
     * Checks, whether the given Class 'clazz' overrides the given method in the Class 'baseClazz'.
     * 
     * @param baseClazz the Class declaring the base method
     * @param clazz the sub class possibly overriding the method
     * @param methodName the name of the method
     * @param parameterTypes the parameters of the method
     * 
     * @return <code>true</code>, if the class overrides the given method, <code>false</code> if not, null, if the method doesn't exist or another error occurrs.
     */
    public static Boolean overridesMethod( Class<?> baseClazz, Class<?> clazz, String methodName, Class<?>... parameterTypes )
    {
        Method m = findMethod( clazz, methodName, parameterTypes );
        
        if ( m == null )
            return ( null );
        
        return ( m.getDeclaringClass() != baseClazz );
    }
}
