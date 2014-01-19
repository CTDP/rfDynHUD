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
package net.ctdp.rfdynhud.editor.director;

import java.util.List;

import javax.swing.JButton;

import net.ctdp.rfdynhud.editor.director.widgetstatesset.WidgetStatesSet;
import net.ctdp.rfdynhud.properties.AbstractPropertiesKeeper;
import net.ctdp.rfdynhud.properties.ListProperty;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class StateSetsListProperty extends ListProperty<WidgetStatesSet, List<WidgetStatesSet>>
{
    private final DirectorManager manager;
    
    @Override
    public String getButtonText()
    {
        return ( "New" );
    }
    
    @Override
    public String getButton2Text()
    {
        if ( getValue() == null )
            return ( null );
        
        return ( "Del" );
    }
    
    @Override
    public void onButtonClicked( Object button )
    {
        if ( ( (JButton)button ).getText().equals( "New" ) )
            manager.addNewWidgetStatesSet();
        else
            manager.removeWidgetStatesSet( getValue() );
    }
    
    public StateSetsListProperty( DirectorManager manager, List<WidgetStatesSet> list )
    {
        super( "states sets", null, list );
        
        AbstractPropertiesKeeper.setKeeper( this, null );
        
        this.manager = manager;
    }
}
