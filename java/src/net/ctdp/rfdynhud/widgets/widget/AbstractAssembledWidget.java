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
import java.util.Arrays;
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
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.WidgetTools;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

/**
 * An assembled {@link Widget} is a master {@link Widget} for other client {@link Widget}s.
 * The client {@link Widget}s define its actual display.
 * 
 * @author Marvin Froehlich
 */
public abstract class AbstractAssembledWidget extends StatefulWidget<Object, Object>
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
    
    private boolean _initParts;
    private Widget[] initialParts;
    private Widget[] parts;
    
    private void makeWidgetPart( Widget part )
    {
        part.setMasterWidget( this );
        part.setConfiguration( getConfiguration() );
        
        part.getBorderProperty().setBorder( null );
        part.setPadding( 0, 0, 0, 0 );
        //part.getBackgroundColorProperty().setColor( (String)null );
    }
    
    void sortParts()
    {
        Arrays.sort( parts, WidgetTools.WIDGET_Z_Y_X_COMPARATOR );
    }
    
    /**
     * Finds a free name starting with 'baseName'.
     * 
     * @param baseName the name prefix
     * 
     * @return the found free name.
     */
    public String findFreePartName( String baseName )
    {
        for ( int i = 1; i < Integer.MAX_VALUE; i++ )
        {
            String name = baseName + i;
            boolean isFree = true;
            for ( int j = 0; j < parts.length; j++ )
            {
                if ( name.equals( parts[j].getName() ) )
                {
                    isFree = false;
                    break;
                }
            }
            
            if ( isFree )
                return ( name );
        }
        
        // Theoretically unreachable code!
        return ( null );
    }
    
    void addPart( Widget widget )
    {
        Widget[] tmp = new Widget[ parts.length +  1 ];
        System.arraycopy( parts, 0, tmp, 0, parts.length );
        parts = tmp;
        parts[parts.length - 1] = widget;
        
        makeWidgetPart( widget );
        
        if ( getConfiguration() != null )
            sortParts();
        
        forceAndSetDirty( true );
    }
    
    void removePart( Widget widget )
    {
        if ( parts.length == 0 )
            throw new IllegalArgumentException( "The passed Widget is not a part of this." );
        
        if ( parts.length == 1 )
        {
            if ( parts[0] != widget )
                throw new IllegalArgumentException( "The passed Widget is not a part of this." );
            
            widget.setMasterWidget( null );
            widget.setConfiguration( null );
            parts = new Widget[ 0 ];
        }
        else
        {
            int index = -1;
            for ( int i = 0; i < parts.length; i++ )
            {
                if ( parts[i] == widget )
                {
                    index = i;
                    break;
                }
            }
            
            if ( index == -1 )
                throw new IllegalArgumentException( "The passed Widget is not a part of this." );
            
            widget.setMasterWidget( null );
            widget.setConfiguration( null );
            Widget[] tmp = new Widget[ parts.length - 1 ];
            
            if ( index == 0 )
            {
                System.arraycopy( parts, 1, tmp, 0, parts.length - 1 );
            }
            else if ( index == parts.length - 1 )
            {
                System.arraycopy( parts, 0, tmp, 0, parts.length - 1 );
            }
            else
            {
                System.arraycopy( parts, 0, tmp, 0, index );
                System.arraycopy( parts, index + 1, tmp, index, parts.length - index - 1 );
            }
            
            parts = tmp;
        }
        
        forceAndSetDirty( true );
    }
    
    /**
     * Gets the number of {@link Widget} parts in this {@link AbstractAssembledWidget}.
     * 
     * @return the number of {@link Widget} parts in this {@link AbstractAssembledWidget}.
     */
    public final int getNumParts()
    {
        return ( parts.length );
    }
    
    /**
     * Gets the i-th {@link Widget}-part in this {@link AbstractAssembledWidget}.
     * 
     * @param index the index
     * 
     * @return the i-th {@link Widget}-part in this {@link AbstractAssembledWidget}.
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
     * @param parts the parts to arrange
     */
    protected void arrangeParts( Widget[] parts )
    {
    }
    
    /**
     * This method is called when the configuration has been loaded.
     * 
     * @see #arrangeParts(Widget[])
     */
    final void arrangeParts()
    {
        if ( _initParts )
            arrangeParts( initialParts );
        
        initialParts = null;
        _initParts = false;
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
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        for ( int i = 0; i < parts.length; i++ )
        {
            int baseIndex = collector.getNumberOf();
            parts[i].initSubTextures( gameData, isEditorMode, parts[i].getInnerSize().getEffectiveWidth(), parts[i].getInnerSize().getEffectiveHeight(), collector );
            for ( int j = baseIndex; j < collector.getNumberOf(); j++ )
            {
                TransformableTexture tt = collector.get( j );
                if ( tt.getOwnerWidget() == null )
                    __RenderPrivilegedAccess.setOwnerWidget( parts[i], tt );
            }
        }
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
        
        /*
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mw = Math.max( mw, part.getPosition().getEffectiveX() + part.getMinWidth( gameData, isEditorMode ) );
        }
        */
        
        return ( mw );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinHeight( LiveGameData gameData, boolean isEditorMode )
    {
        int mh = super.getMinHeight( gameData, isEditorMode );
        
        /*
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mh = Math.max( mh, part.getPosition().getEffectiveY() + part.getMinHeight( gameData, isEditorMode ) );
        }
        */
        
        return ( mh );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxWidth( LiveGameData gameData, boolean isEditorMode )
    {
        //if ( parts.length == 0 )
        return ( super.getMaxWidth( gameData, isEditorMode ) );
        
        /*
        int mw = 0;
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mw = Math.max( mw, part.getPosition().getEffectiveX() + part.getMaxWidth( gameData, isEditorMode ) );
        }
        
        return ( mw );
        */
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxHeight( LiveGameData gameData, boolean isEditorMode )
    {
        //if ( parts.length == 0 )
        return ( super.getMaxHeight( gameData, isEditorMode ) );
        
        /*
        int mh = 0;
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mh = Math.max( mh, part.getPosition().getEffectiveY() + part.getMaxHeight( gameData, isEditorMode ) );
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
    public Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.updateVisibility( gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].updateVisibility( gameData, isEditorMode );
        }
        
        return ( result );
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
    protected Boolean onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            Boolean result2 = parts[i].onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
            if ( result2 != null )
                result = result2;
        }
        
        return ( result );
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
    public Boolean onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.onBoundInputStateChanged( action, state, modifierMask, when, gameData, isEditorMode );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            Boolean result2 = parts[i].onBoundInputStateChanged( action, state, modifierMask, when, gameData, isEditorMode );
            
            if ( result2 != null )
                result = result2;
        }
        
        return ( result );
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
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            int width2 = part.getEffectiveWidth();
            int height2 = part.getEffectiveHeight();
            
            part.initialize( gameData, isEditorMode, drawnStringFactory, texture, width2, height2 );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int width, int height )
    {
        boolean result = false;
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            int width2 = part.getEffectiveWidth();
            int height2 = part.getEffectiveHeight();
            
            result = part.checkForChanges( gameData, isEditorMode, texture, width2, height2 ) || result;
        }
        
        return ( result );
    }
    
    @Override
    void _drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super._drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            int offsetX2 = offsetX + part.getPosition().getEffectiveX();
            int offsetY2 = offsetY + part.getPosition().getEffectiveY();
            int width2 = part.getEffectiveWidth();
            int height2 = part.getEffectiveHeight();
            
            part._drawBackground( gameData, isEditorMode, texture, offsetX2, offsetY2, width2, height2, false );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            int offsetX2 = offsetX +  part.getPosition().getEffectiveX()/* + part.getBorder().getInnerLeftWidth()*/;
            int offsetY2 = offsetY + part.getPosition().getEffectiveY()/* + part.getBorder().getInnerTopHeight()*/;
            //int width2 = part.getEffectiveInnerWidth();
            //int height2 = part.getEffectiveInnerHeight();
            int width2 = part.getEffectiveWidth();
            int height2 = part.getEffectiveHeight();
            
            if ( texture != null )
                texture.getTextureCanvas().pushClip( offsetX2, offsetY2, width2, height2, true );
            
            try
            {
                part.drawWidget( clock, needsCompleteRedraw, gameData, isEditorMode, texture, offsetX2, offsetY2, width2, height2 );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
            finally
            {
                if ( texture != null )
                    texture.getTextureCanvas().popClip();
            }
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
    
    
    @Override
    protected Widget getNewInstanceForClone()
    {
        AbstractAssembledWidget newWidget = WidgetFactory.createAssembledWidget( getClass().getName(), "CloneOf" + getName() );
        
        for ( int i = 0; i < getNumParts(); i++ )
        {
            newWidget.addPart( getPart( i ).getNewInstanceForClone() );
        }
        
        return ( newWidget );
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
     * @param width negative numbers for (screen_width - width)
     * @param widthPercent width parameter treated as percents
     * @param height negative numbers for (screen_height - height)
     * @param heightPercent height parameter treated as percents
     * @param initParts this parameter must exist in your contructor and has to be forwarded to this
     */
    protected AbstractAssembledWidget( float width, boolean widthPercent, float height, boolean heightPercent, boolean initParts )
    {
        super( width, widthPercent, height, heightPercent );
        
        this._initParts = initParts;
        if ( initParts )
            this.parts = initParts( width, widthPercent, height, heightPercent );
        else
            this.parts = new Widget[ 0 ];
        this.initialParts = new Widget[ parts.length ];
        System.arraycopy( parts, 0, initialParts, 0, parts.length );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            makeWidgetPart( parts[i] );
        }
    }
    
    /**
     * Creates a new Widget.
     * 
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     * @param initParts this parameter must exist in your contructor and has to be forwarded to this
     */
    protected AbstractAssembledWidget( float width, float height, boolean initParts )
    {
        this( width, true, height, true, initParts );
    }
}
