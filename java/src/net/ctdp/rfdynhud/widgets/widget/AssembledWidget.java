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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.__RenderPrivilegedAccess;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

public abstract class AssembledWidget extends StatefulWidget<Object, Object>
{
    static class AssembledGeneralStore
    {
        @SuppressWarnings( "rawtypes" )
        private final HashMap<StatefulWidget, Object> generalStores = new HashMap<StatefulWidget, Object>();
    }
    
    static class AssembledLocalStore
    {
        @SuppressWarnings( "rawtypes" )
        private final HashMap<StatefulWidget, Object> localStores = new HashMap<StatefulWidget, Object>();
    }
    
    private final Widget[] parts;
    
    /**
     * Gets the number of {@link Widget} parts in this {@link AssembledWidget}.
     * 
     * @return the number of {@link Widget} parts in this {@link AssembledWidget}.
     */
    public final int getNumParts()
    {
        return ( parts.length );
    }
    
    /**
     * Gets the i-th {@link Widget}-part in this {@link AssembledWidget}.
     * 
     * @return the i-th {@link Widget}-part in this {@link AssembledWidget}.
     */
    public final Widget getPart( int index )
    {
        return ( parts[index] );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings( "rawtypes" )
    boolean hasGeneralStore()
    {
        if ( super.hasGeneralStore() )
            return ( true );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            Widget part = parts[i];
            
            if ( ( part instanceof StatefulWidget ) && ( (StatefulWidget)part ).hasGeneralStore() )
                return ( true );
        }
        
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final AssembledGeneralStore createGeneralStore()
    {
        return ( new AssembledGeneralStore() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    void setGeneralStore( Object generalStore )
    {
        super.setGeneralStore( generalStore );
        
        if ( generalStore != null )
        {
            AssembledGeneralStore ags = (AssembledGeneralStore)generalStore;
            
            for ( int i = 0; i < parts.length; i++ )
            {
                Widget part = parts[i];
                
                if ( part instanceof StatefulWidget )
                {
                    StatefulWidget sw = (StatefulWidget)part;
                    
                    sw.setGeneralStore( ags.generalStores.get( sw ) );
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings( "rawtypes" )
    boolean hasLocalStore()
    {
        if ( super.hasLocalStore() )
            return ( true );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            Widget part = parts[i];
            
            if ( ( part instanceof StatefulWidget ) && ( (StatefulWidget)part ).hasLocalStore() )
                return ( true );
        }
        
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final AssembledLocalStore createLocalStore()
    {
        return ( new AssembledLocalStore() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    void setLocalStore( Object localStore )
    {
        super.setLocalStore( localStore );
        
        if ( localStore != null )
        {
            AssembledLocalStore als = (AssembledLocalStore)localStore;
            
            for ( int i = 0; i < parts.length; i++ )
            {
                Widget part = parts[i];
                
                if ( part instanceof StatefulWidget )
                {
                    StatefulWidget sw = (StatefulWidget)part;
                    
                    sw.setLocalStore( als.localStores.get( sw ) );
                }
            }
        }
    }
    
    /**
     * This method is called when the configuration has been loaded.
     * 
     * @param parts
     */
    protected void arrangeParts( Widget[] parts )
    {
    }
    
    /**
     * This method is called when the configuration has been loaded.
     * 
     * @see #arrangeParts(Widget[])
     */
    protected final void arrangeParts()
    {
        arrangeParts( parts );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultBorderValue( String name )
    {
        return ( BorderProperty.getDefaultBorderValue( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedColorValue( String name )
    {
        return ( ColorProperty.getDefaultNamedColorValue( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        return ( FontProperty.getDefaultNamedFontValue( name ) );
    }
    
    @Override
    final void setConfiguration( WidgetsConfiguration config )
    {
        super.setConfiguration( config );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].setConfiguration( config );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
    {
        ArrayList<TransformableTexture> list = new ArrayList<TransformableTexture>();
        
        for ( int i = 0; i < parts.length; i++ )
        {
            TransformableTexture[] tts = parts[i].getSubTexturesImpl( gameData, isEditorMode, widgetInnerWidth, widgetInnerHeight );
            if ( ( tts != null ) && ( tts.length > 0 ) )
            {
                for ( int j = 0; j < tts.length; j++ )
                {
                    if ( tts[j].getOwnerWidget() == null )
                        __RenderPrivilegedAccess.setOwnerWidget( parts[i], tts[j] );
                    list.add( tts[j] );
                }
            }
        }
        
        if ( list.size() == 0 )
            return ( null );
        
        TransformableTexture[] result = new TransformableTexture[ list.size() ];
        list.toArray( result );
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setDirtyFlag( boolean forwardCall )
    {
        super.setDirtyFlag( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void forceReinitialization( boolean forwardCall )
    {
        super.forceReinitialization( true );
        
        if ( forwardCall && ( parts != null ) )
        {
            for ( int i = 0; i < parts.length; i++ )
            {
                parts[i].forceReinitialization( false );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinWidth( LiveGameData gameData, boolean isEditorMode )
    {
        int mw = super.getMinWidth( gameData, isEditorMode );
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mw = Math.max( mw, part.getPosition().getEffectiveX() + part.getMinWidth( gameData, isEditorMode ) );
        }
        
        return ( mw );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinHeight( LiveGameData gameData, boolean isEditorMode )
    {
        int mh = super.getMinHeight( gameData, isEditorMode );
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mh = Math.max( mh, part.getPosition().getEffectiveY() + part.getMinHeight( gameData, isEditorMode ) );
        }
        
        return ( mh );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxWidth( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture )
    {
        //if ( parts.length == 0 )
            return ( getEffectiveWidth() );
        
        /*
        int mw = 0;
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mw = Math.max( mw, part.getPosition().getEffectiveX() + part.getMaxWidth( gameData, isEditorMode, texture ) );
        }
        
        return ( mw );
        */
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxHeight( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture )
    {
        //if ( parts.length == 0 )
            return ( getEffectiveHeight() );
        
        /*
        int mh = 0;
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mh = Math.max( mh, part.getPosition().getEffectiveY() + part.getMaxHeight( gameData, isEditorMode, texture ) );
        }
        
        return ( mh );
        */
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void bake()
    {
        super.bake();
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].bake();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllPosAndSizeToPercents()
    {
        super.setAllPosAndSizeToPercents();
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].setAllPosAndSizeToPercents();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllPosAndSizeToPixels()
    {
        super.setAllPosAndSizeToPixels();
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].setAllPosAndSizeToPixels();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    void forceCompleteRedraw_( boolean mergedBackgroundToo, boolean forwardCall )
    {
        super.forceCompleteRedraw_( mergedBackgroundToo, true );
        
        if ( forwardCall && ( parts != null ) )
        {
            for ( int i = 0; i < parts.length; i++ )
            {
                parts[i].forceCompleteRedraw_( mergedBackgroundToo, false );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVisibility( boolean clock1, boolean clock2, LiveGameData gameData, boolean isEditorMode )
    {
        super.updateVisibility( clock1, clock2, gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].updateVisibility( clock1, clock2, gameData, isEditorMode );
        }
    }
    
    private int neededData = -1;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNeededData()
    {
        if ( neededData == -1 )
        {
            neededData = 0;
            
            for ( int i = 0; i < parts.length; i++ )
            {
                neededData |= parts[i].getNeededData();
            }
        }
        
        return ( neededData );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].afterConfigurationLoaded( widgetsConfig, gameData, isEditorMode );
        }
        
        arrangeParts();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeConfigurationCleared( WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode )
    {
        super.beforeConfigurationCleared( widgetsConfig, gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].beforeConfigurationCleared( widgetsConfig, gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTrackChanged( String trackname, LiveGameData gameData, boolean isEditorMode )
    {
        super.onTrackChanged( trackname, gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onTrackChanged( trackname, gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, boolean isEditorMode )
    {
        super.onSessionStarted( sessionType, gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onSessionStarted( sessionType, gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onRealtimeEntered( gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        super.onScoringInfoUpdated( gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onScoringInfoUpdated( gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleSetupUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        super.onVehicleSetupUpdated( gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onVehicleSetupUpdated( gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onNeededDataComplete( LiveGameData gameData, boolean isEditorMode )
    {
        super.onNeededDataComplete( gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onNeededDataComplete( gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPitsEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onPitsEntered( gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onPitsEntered( gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onGarageEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onGarageEntered( gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onGarageEntered( gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onGarageExited( LiveGameData gameData, boolean isEditorMode )
    {
        super.onGarageExited( gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onGarageExited( gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPitsExited( LiveGameData gameData, boolean isEditorMode )
    {
        super.onPitsExited( gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onPitsExited( gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeExited( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeExited( gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onRealtimeExited( gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, boolean isEditorMode )
    {
        super.onLapStarted( vsi, gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onLapStarted( vsi, gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, boolean isEditorMode )
    {
        super.onBoundInputStateChanged( action, state, modifierMask, when, gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onBoundInputStateChanged( action, state, modifierMask, when, gameData, isEditorMode );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMasterCanvas( boolean isEditorMode )
    {
        return ( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            int offsetX2 = offsetX + part.getPosition().getEffectiveX();
            int offsetY2 = offsetY + part.getPosition().getEffectiveY();
            int width2 = part.getEffectiveWidth();
            int height2 = part.getEffectiveHeight();
            
            part.initialize( clock1, clock2, gameData, isEditorMode, drawnStringFactory, texture, offsetX2, offsetY2, width2, height2 );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        boolean result = false;
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            int offsetX2 = offsetX + part.getPosition().getEffectiveX();
            int offsetY2 = offsetY + part.getPosition().getEffectiveY();
            int width2 = part.getEffectiveWidth();
            int height2 = part.getEffectiveHeight();
            
            result = part.checkForChanges( clock1, clock2, gameData, isEditorMode, texture, offsetX2, offsetY2, width2, height2 ) || result;
        }
        
        return ( result );
    }
    
    @Override
    void drawBackground_( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground_( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            int offsetX2 = offsetX + part.getPosition().getEffectiveX();
            int offsetY2 = offsetY + part.getPosition().getEffectiveY();
            int width2 = part.getEffectiveWidth();
            int height2 = part.getEffectiveHeight();
            
            part.drawBackground_( gameData, isEditorMode, texture, offsetX2, offsetY2, width2, height2, false );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            int offsetX2 = offsetX + part.getPosition().getEffectiveX()/* + part.getBorder().getInnerLeftWidth()*/;
            int offsetY2 = offsetY + part.getPosition().getEffectiveY()/* + part.getBorder().getInnerTopHeight()*/;
            //int width2 = part.getEffectiveInnerWidth();
            //int height2 = part.getEffectiveInnerHeight();
            int width2 = part.getEffectiveWidth();
            int height2 = part.getEffectiveHeight();
            
            part.drawWidget( clock1, clock2, needsCompleteRedraw, gameData, isEditorMode, texture, offsetX2, offsetY2, width2, height2 );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        /*
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].saveProperties( writer );
        }
        */
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        /*
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].loadProperty( loader );
        }
        */
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            propsCont.pushGroup( parts[i].getName(), true );
            
            parts[i].getProperties( propsCont, forceAll );
            
            propsCont.popGroup();
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean canHaveBackground()
    {
        return ( true );
    }
    
    protected abstract Widget[] initParts( float width, boolean widthPercent, float height, boolean heightPercent );
    
    /**
     * Creates a new Widget.
     * 
     * @param name
     * @param width negative numbers for (screen_width - width)
     * @param widthPercent width parameter treated as percents
     * @param height negative numbers for (screen_height - height)
     * @param heightPercent height parameter treated as percents
     */
    protected AssembledWidget( String name, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        super( name, width, widthPercent, height, heightPercent );
        
        this.parts = initParts( width, widthPercent, height, heightPercent );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            Widget part = parts[i];
            
            part.setMasterWidget( this );
            
            part.getBorderProperty().setBorder( null );
            part.setPadding( 0, 0, 0, 0 );
            //part.getBackgroundColorProperty().setColor( (String)null );
        }
    }
    
    /**
     * Creates a new Widget.
     * 
     * @param name
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     */
    protected AssembledWidget( String name, float width, float height )
    {
        this( name, width, true, height, true );
    }
}
