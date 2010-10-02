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
package net.ctdp.rfdynhud.widgets.widget;

import java.util.HashMap;

/**
 * <p>
 * Widgets extending this class can store state data,
 * that is restored when a Widget of this type and/or name
 * is (re-)loaded.
 * </p>
 * 
 * <p>
 * The general store object is used and restored for any {@link StatefulWidget} of the same class.
 * </p>
 * 
 * <p>
 * The local store object is used and restored for any {@link StatefulWidget} of the same class and name.
 * </p>
 * 
 * @param <GeneralStore> the type name of the general store class
 * @param <LocalStore> the type name of the local store class
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class StatefulWidget<GeneralStore, LocalStore> extends Widget
{
    @SuppressWarnings( "rawtypes" )
    private static final HashMap<Class<? extends StatefulWidget>, Object> generalStores = new HashMap<Class<? extends StatefulWidget>, Object>();
    private GeneralStore generalStore = null;
    private LocalStore localStore = null;
    
    /**
     * Creates a store object for all widgets of this type.
     * 
     * @return the general store object. <code>null</code> is explicitly permitted and default implementation simply returns <code>null</code>.
     */
    protected abstract GeneralStore createGeneralStore();
    
    @SuppressWarnings( "rawtypes" )
    protected Class<? extends StatefulWidget> getGeneralStoreKey()
    {
        return ( this.getClass() );
    }
    
    boolean hasGeneralStore()
    {
        return ( generalStore != null );
    }
    
    void setGeneralStore( GeneralStore generalStore )
    {
        this.generalStore = generalStore;
    }
    
    /**
     * Gets a value store object for all {@link StatefulWidget}s of this class.
     * 
     * @return a value store object for all {@link StatefulWidget}s of this class.
     */
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public final GeneralStore getGeneralStore()
    {
        if ( generalStore == null )
        {
            final Class<? extends StatefulWidget> key = getGeneralStoreKey();
            if ( !generalStores.containsKey( key ) )
            {
                GeneralStore generalStore = createGeneralStore();
                generalStores.put( key, generalStore );
            }
            
            setGeneralStore( (GeneralStore)generalStores.get( key ) );
        }
        
        return ( generalStore );
    }
    
    /**
     * Creates a store object for this Widget only.
     * 
     * @return the local store object. <code>null</code> is explicitly permitted and default implementation simply returns <code>null</code>.
     */
    protected abstract LocalStore createLocalStore();
    
    void setLocalStore( LocalStore localStore )
    {
        this.localStore = localStore;
    }
    
    boolean hasLocalStore()
    {
        return ( localStore != null );
    }
    
    /**
     * Gets a value store object for this {@link StatefulWidget}.
     * The store is restored when the widget configuration is reloaded.
     * The object is stored by the {@link StatefulWidget}'s class and name.
     * 
     * @return a value store object for this {@link StatefulWidget}.
     */
    public final LocalStore getLocalStore()
    {
        if ( localStore == null )
        {
            localStore = createLocalStore();
        }
        
        return ( localStore );
    }
    
    /**
     * Creates a new Widget.
     * 
     * @param name the new name for this Widget
     * @param width negative numbers for (screen_width - width)
     * @param widthPercent width parameter treated as percents
     * @param height negative numbers for (screen_height - height)
     * @param heightPercent height parameter treated as percents
     */
    protected StatefulWidget( String name, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        super( name, width, widthPercent, height, heightPercent );
    }
    
    /**
     * Creates a new Widget.
     * 
     * @param name the new name for this Widget
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     */
    protected StatefulWidget( String name, float width, float height )
    {
        this( name, width, true, height, true );
    }
}
