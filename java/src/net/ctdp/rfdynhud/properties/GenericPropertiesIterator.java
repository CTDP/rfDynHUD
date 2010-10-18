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
package net.ctdp.rfdynhud.properties;

import java.lang.reflect.Field;

/**
 * <p>
 * Iterates all {@link Property} fields of a given {@link Class}.
 * This includes all super classes and even private fields.
 * </p>
 * 
 * </p>
 * It is implemented using generics and a dirty trick to access private fields.
 * </p>
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class GenericPropertiesIterator extends GenericFieldsIterator<Property>
{
    @Override
    protected boolean checkFieldType( Field field )
    {
        return ( Property.class.isAssignableFrom( field.getType() ) );
    }
    
    public GenericPropertiesIterator( Object object, boolean includeStatic )
    {
        super( object, includeStatic );
    }
    
    public GenericPropertiesIterator( Object object )
    {
        super( object );
    }
}
