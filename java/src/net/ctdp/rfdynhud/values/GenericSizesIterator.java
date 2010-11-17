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
package net.ctdp.rfdynhud.values;

import java.lang.reflect.Field;

import net.ctdp.rfdynhud.properties.GenericFieldsIterator;
import net.ctdp.rfdynhud.properties.Size;

/**
 * <p>
 * Iterates all {@link Size} fields of a given {@link Class}.
 * This includes all super classes and even private fields.
 * </p>
 * 
 * </p>
 * It is implemented using generics and a dirty trick to access private fields.
 * </p>
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class GenericSizesIterator extends GenericFieldsIterator<Size>
{
    @Override
    protected boolean checkFieldType( Field field )
    {
        return ( Size.class.isAssignableFrom( field.getType() ) );
    }
    
    public GenericSizesIterator( Object object, boolean includeStatic )
    {
        super( object, includeStatic );
    }
    
    public GenericSizesIterator( Object object )
    {
        super( object );
    }
}
