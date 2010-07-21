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
 * @author Marvin Froehlich
 */
public abstract class StatefulWidget<GeneralStore, LocalStore> extends Widget
{
    @SuppressWarnings( "unchecked" )
    private static final HashMap<Class<? extends StatefulWidget>, Object> generalStores = new HashMap<Class<? extends StatefulWidget>, Object>();
    private GeneralStore generalStore = null;
    private LocalStore localStore = null;
    
    /**
     * Creates a store object for all widgets of this type.
     * 
     * @return the general store object. <code>null</code> is explicitly permitted and default implementation simply returns <code>null</code>.
     */
    protected abstract GeneralStore createGeneralStore();
    
    @SuppressWarnings( "unchecked" )
    protected Class<? extends StatefulWidget> getGeneralStoreKey()
    {
        return ( this.getClass() );
    }
    
    /**
     * Gets a value store object for all {@link StatefulWidget}s of this class.
     * 
     * @return a value store object for all {@link StatefulWidget}s of this class.
     */
    @SuppressWarnings( "unchecked" )
    public final GeneralStore getGeneralStore()
    {
        if ( generalStore == null )
        {
            final Class<? extends StatefulWidget> key = getGeneralStoreKey();
            if ( !generalStores.containsKey( key ) )
            {
                generalStore = createGeneralStore();
                generalStores.put( key, generalStore );
            }
            
            generalStore = (GeneralStore)generalStores.get( key );
        }
        
        return ( generalStore );
    }
    
    /**
     * Creates a store object for this Widget only.
     * 
     * @return the local store object. <code>null</code> is explicitly permitted and default implementation simply returns <code>null</code>.
     */
    protected abstract LocalStore createLocalStore();
    
    final void setLocalStore( LocalStore localStore )
    {
        this.localStore = localStore;
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
     * @param name
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
     * @param name
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     */
    protected StatefulWidget( String name, float width, float height )
    {
        this( name, width, true, height, true );
    }
}
