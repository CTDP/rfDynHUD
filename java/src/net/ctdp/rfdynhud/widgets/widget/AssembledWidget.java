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

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

public abstract class AssembledWidget extends Widget
{
    protected final Widget[] parts;
    
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
     * @param parts
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
    public InputAction[] getInputActions()
    {
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        ArrayList<TransformableTexture> list = new ArrayList<TransformableTexture>();
        
        for ( int i = 0; i < parts.length; i++ )
        {
            TransformableTexture[] tts = parts[i].getSubTexturesImpl( gameData, editorPresets, widgetInnerWidth, widgetInnerHeight );
            if ( ( tts != null ) && ( tts.length > 0 ) )
            {
                for ( int j = 0; j < tts.length; j++ )
                    list.add( tts[j] );
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
    public int getMinWidth( LiveGameData gameData )
    {
        int mw = 0;
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mw = Math.max( mw, part.getPosition().getEffectiveX() + part.getMinWidth( gameData ) );
        }
        
        return ( mw );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinHeight( LiveGameData gameData )
    {
        int mh = 0;
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mh = Math.max( mh, part.getPosition().getEffectiveY() + part.getMinHeight( gameData ) );
        }
        
        return ( mh );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxWidth( LiveGameData gameData, TextureImage2D texture )
    {
        int mw = 0;
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mw = Math.max( mw, part.getPosition().getEffectiveX() + part.getMaxWidth( gameData, texture ) );
        }
        
        return ( mw );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxHeight( LiveGameData gameData, TextureImage2D texture )
    {
        int mh = 0;
        
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            mh = Math.max( mh, part.getPosition().getEffectiveY() + part.getMaxHeight( gameData, texture ) );
        }
        
        return ( mh );
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
    public void forceCompleteRedraw( boolean forwardCall )
    {
        super.forceCompleteRedraw( true );
        
        if ( forwardCall && ( parts != null ) )
        {
            for ( int i = 0; i < parts.length; i++ )
            {
                parts[i].forceCompleteRedraw( false );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVisibility( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.updateVisibility( clock1, clock2, gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].updateVisibility( clock1, clock2, gameData, editorPresets );
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
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].afterConfigurationLoaded( widgetsConfig, gameData, editorPresets );
        }
        
        arrangeParts();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeConfigurationCleared( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.beforeConfigurationCleared( widgetsConfig, gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].beforeConfigurationCleared( widgetsConfig, gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTrackChanged( String trackname, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onTrackChanged( trackname, gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onTrackChanged( trackname, gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onSessionStarted( sessionType, gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onSessionStarted( sessionType, gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onRealtimeEntered( gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onScoringInfoUpdated( gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onScoringInfoUpdated( gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleSetupUpdated( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onVehicleSetupUpdated( gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onVehicleSetupUpdated( gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onNeededDataComplete( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onNeededDataComplete( gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onNeededDataComplete( gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPitsEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onPitsEntered( gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onPitsEntered( gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onGarageEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onGarageEntered( gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onGarageEntered( gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onGarageExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onGarageExited( gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onGarageExited( gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPitsExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onPitsExited( gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onPitsExited( gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeExited( gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onRealtimeExited( gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onVehicleControlChanged( viewedVSI, gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onLapStarted( vsi, gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onLapStarted( vsi, gameData, editorPresets );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onEngineBoostChanged( int oldBoost, int newBoost, boolean oldTempBoost, boolean newTempBoost )
    {
        super.onEngineBoostChanged( oldBoost, newBoost, oldTempBoost, newTempBoost );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onEngineBoostChanged( oldBoost, newBoost, oldTempBoost, newTempBoost );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onBoundInputStateChanged( action, state, modifierMask, when, gameData, editorPresets );
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].onBoundInputStateChanged( action, state, modifierMask, when, gameData, editorPresets );
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
    public void clearRegion( boolean isEditorMode, TextureImage2D texture )
    {
        super.clearRegion( isEditorMode, texture );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        Widget part;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            part = parts[i];
            
            int offsetX2 = offsetX + part.getPosition().getEffectiveX();
            int offsetY2 = offsetY + part.getPosition().getEffectiveY();
            int width2 = part.getEffectiveWidth();
            int height2 = part.getEffectiveHeight();
            
            part.initialize( clock1, clock2, gameData, editorPresets, drawnStringFactory, texture, offsetX2, offsetY2, width2, height2 );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
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
            
            result = part.checkForChanges( clock1, clock2, gameData, editorPresets, texture, offsetX2, offsetY2, width2, height2 ) || result;
        }
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawBorder( boolean isEditorMode, BorderWrapper border, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        super.drawBorder( isEditorMode, border, texture, offsetX, offsetY, width, height );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( hasBackgroundColor() && ( texture != null ) )
        {
            texture.clear( getBackgroundColor(), offsetX, offsetY, width, height, true, null );
            
            /*
            Widget part;
            
            for ( int i = 0; i < parts.length; i++ )
            {
                part = parts[i];
                
                int offsetX2 = offsetX + part.getPosition().getEffectiveX();
                int offsetY2 = offsetY + part.getPosition().getEffectiveY();
                int width2 = part.getEffectiveWidth();
                int height2 = part.getEffectiveHeight();
                
                part.clearBackground( gameData, editorPresets, texture, offsetX2, offsetY2, width2, height2 );
            }
            */
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
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
            
            part.drawWidget( clock1, clock2, needsCompleteRedraw, gameData, editorPresets, texture, offsetX2, offsetY2, width2, height2 );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
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
     * Creates a new Widget.
     * 
     * @param name
     * @param width negative numbers for (screen_width - width)
     * @param widthPercent width parameter treated as percents
     * @param height negative numbers for (screen_height - height)
     * @param heightPercent height parameter treated as percents
     * @param parts
     */
    protected AssembledWidget( String name, float width, boolean widthPercent, float height, boolean heightPercent, Widget[] parts )
    {
        super( name, width, widthPercent, height, heightPercent );
        
        this.parts = parts;
        
        for ( int i = 0; i < parts.length; i++ )
        {
            parts[i].setMasterWidget( this );
            
            parts[i].getBorderProperty().setBorder( null );
            parts[i].setPadding( 0, 0, 0, 0 );
            parts[i].getBackgroundColorProperty().setColor( (String)null );
        }
    }
    
    /**
     * Creates a new Widget.
     * 
     * @param name
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     * @param parts
     */
    protected AssembledWidget( String name, float width, float height, Widget[] parts )
    {
        this( name, width, true, height, true, parts );
    }
}
