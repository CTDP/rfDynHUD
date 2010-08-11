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

import java.lang.reflect.Modifier;

/**
 * This is a ClassSearcher Criterium, that ensures, that checked class
 * is a sub type of a certain super class.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class SuperClassCriterium implements ClassSearchCriterium
{
    private final Class<?> supa;
    private final boolean allowAbstract;
    
    public Class<?> getSuper()
    {
        return ( supa );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean check( Class<?> clazz )
    {
        if ( ( getSuper().isAssignableFrom( clazz ) ) && ( clazz != getSuper() ) )
        {
            if ( ( clazz.getModifiers() & ( Modifier.ABSTRACT | Modifier.INTERFACE ) ) != 0 )
                return ( allowAbstract );
            
            return ( true );
        }
        
        return ( false );
    }
    
    public SuperClassCriterium( Class<?> supa, boolean allowAbstract )
    {
        this.supa = supa;
        this.allowAbstract = allowAbstract;
    }
}
