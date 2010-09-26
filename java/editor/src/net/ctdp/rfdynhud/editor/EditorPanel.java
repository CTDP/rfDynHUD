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
package net.ctdp.rfdynhud.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JPanel;

import net.ctdp.rfdynhud.gamedata.GameResolution;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.TextureDirtyRectsManager;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.openmali.types.twodee.Rect2i;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class EditorPanel extends JPanel
{
    private static final long serialVersionUID = -4217992603083635127L;
    
    private final RFDynHUDEditor editor;
    
    private LiveGameData gameData;
    
    private TextureImage2D overlay;
    private final WidgetsDrawingManager drawingManager;
    private final GameResolution gameResolution;
    private final ByteBuffer dirtyRectsBuffer = TextureDirtyRectsManager.createByteBuffer( 1024 );
    private final ArrayList<Boolean> dirtyFlags = new ArrayList<Boolean>();
    private final Map<Widget, Rect2i> oldWidgetRects = new WeakHashMap<Widget, Rect2i>();
    private final Map<Widget, Rect2i[]> oldWidgetSubTexRects = new WeakHashMap<Widget, Rect2i[]>();
    
    private Widget selectedWidget = null;
    private static final Stroke SELECTION_STROKE = new BasicStroke( 2 );
    private static final java.awt.Color SELECTION_COLOR = new java.awt.Color( 255, 0, 0, 127 );
    
    private final ArrayList<WidgetSelectionListener> selectionListeners = new ArrayList<WidgetSelectionListener>();
    
    private BufferedImage backgroundImage;
    private BufferedImage cacheImage;
    private Graphics2D cacheGraphics;
    
    private boolean bgImageReloadSuppressed = false;
    
    public void setBGImageReloadSuppressed( boolean suppressed )
    {
        this.bgImageReloadSuppressed = suppressed;
    }
    
    private final IntProperty railDistanceX = new IntProperty( (Widget)null, "railDistanceX", 10, 0, 100 );
    private final IntProperty railDistanceY = new IntProperty( (Widget)null, "railDistanceY", 10, 0, 100 );
    
    private final BooleanProperty drawGrid = new BooleanProperty( (Widget)null, "drawGrid", false )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            if ( !bgImageReloadSuppressed && ( drawingManager != null ) )
                setBackgroundImage( editor.loadBackgroundImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() ) );
        }
    };
    
    private final IntProperty gridOffsetX = new IntProperty( (Widget)null, "gridOffsetX", 0 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            if ( !bgImageReloadSuppressed && ( drawingManager != null ) )
                setBackgroundImage( editor.loadBackgroundImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() ) );
        }
    };
    
    private final IntProperty gridOffsetY = new IntProperty( (Widget)null, "gridOffsetY", 0 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            if ( !bgImageReloadSuppressed && ( drawingManager != null ) )
                setBackgroundImage( editor.loadBackgroundImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() ) );
        }
    };
    
    private final IntProperty gridSizeX = new IntProperty((Widget) null, "gridSizeX", 10, 0, 5000 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            if ( !bgImageReloadSuppressed && ( drawingManager != null ) )
                setBackgroundImage( editor.loadBackgroundImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() ) );
        }
    };
    
    private final IntProperty gridSizeY = new IntProperty( (Widget)null, "gridSizeY", 10, 0, 5000 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            if ( !bgImageReloadSuppressed && ( drawingManager != null ) )
                setBackgroundImage( editor.loadBackgroundImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() ) );
        }
    };
    
    public void addWidgetSelectionListener( WidgetSelectionListener l )
    {
        selectionListeners.add( l );
    }
    
    public void removeWidgetSelectionListener( WidgetSelectionListener l )
    {
        selectionListeners.remove( l );
    }
    
    public void setDrawGrid( boolean drawGrid )
    {
        this.drawGrid.setBooleanValue( drawGrid );
    }
    
    public final int getRailDistanceX()
    {
        return ( railDistanceX.getIntValue() );
    }
    
    public final int getRailDistanceY()
    {
        return ( railDistanceY.getIntValue() );
    }
    
    public final boolean getDrawGrid()
    {
        return ( drawGrid.getBooleanValue() );
    }
    
    public final int getGridOffsetX()
    {
        return ( gridOffsetX.getIntValue() );
    }
    
    public final int getGridOffsetY()
    {
        return ( gridOffsetY.getIntValue() );
    }
    
    public final int getGridSizeX()
    {
        return ( gridSizeX.getIntValue() );
    }
    
    public final int getGridSizeY()
    {
        return ( gridSizeY.getIntValue() );
    }
    
    public void getProperties( WidgetPropertiesContainer propsCont )
    {
        propsCont.addProperty( railDistanceX );
        propsCont.addProperty( railDistanceY );
        propsCont.addProperty( drawGrid );
        propsCont.addProperty( gridOffsetX );
        propsCont.addProperty( gridOffsetY );
        propsCont.addProperty( gridSizeX );
        propsCont.addProperty( gridSizeY );
    }
    
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( railDistanceX, null );
        writer.writeProperty( railDistanceY, null );
        writer.writeProperty( drawGrid, null );
        writer.writeProperty( gridOffsetX, null );
        writer.writeProperty( gridOffsetY, null );
        writer.writeProperty( gridSizeX, null );
        writer.writeProperty( gridSizeY, null );
    }
    
    public void loadProperty( PropertyLoader loader )
    {
        bgImageReloadSuppressed = true;
        
        if ( loader.loadProperty( railDistanceX ) );
        else if ( loader.loadProperty( railDistanceY ) );
        else if ( loader.loadProperty( drawGrid ) );
        else if ( loader.loadProperty( gridOffsetX ) );
        else if ( loader.loadProperty( gridOffsetY ) );
        else if ( loader.loadProperty( gridSizeX ) );
        else if ( loader.loadProperty( gridSizeY ) );
        
        bgImageReloadSuppressed = false;
    }
    
    public final boolean isGridUsed()
    {
        return ( drawGrid.getBooleanValue() && ( this.gridSizeX.getIntValue() > 1 ) && ( this.gridSizeY.getIntValue() > 1 ) );
    }
    
    public final int snapXToGrid( int x )
    {
        if ( !isGridUsed() )
            return ( x );
        
        return ( gridOffsetX.getIntValue() + Math.min( Math.round( ( x - gridOffsetX.getIntValue() ) / (float)gridSizeX.getIntValue() ) * gridSizeX.getIntValue(), drawingManager.getGameResolution().getViewportWidth() - 1 ) );
    }
    
    public final int snapYToGrid( int y )
    {
        if ( !isGridUsed() )
            return ( y );
        
        return ( gridOffsetY.getIntValue() + Math.min( Math.round( ( y - gridOffsetY.getIntValue() ) / (float)gridSizeY.getIntValue() ) * gridSizeY.getIntValue(), drawingManager.getGameResolution().getViewportHeight() - 1 ) );
    }
    
    public void snapWidgetToGrid( Widget widget )
    {
        if ( !isGridUsed() )
            return;
        
        int x = widget.getPosition().getEffectiveX();
        int y = widget.getPosition().getEffectiveY();
        int w = widget.getSize().getEffectiveWidth();
        int h = widget.getSize().getEffectiveHeight();
        
        x = snapXToGrid( x );
        y = snapYToGrid( y );
        w = snapXToGrid( x + w - 1 ) + 1 - x;
        h = snapYToGrid( y + h - 1 ) + 1 - y;
        
        widget.getSize().setEffectiveSize( w, h );
        widget.getPosition().setEffectivePosition( x, y );
    }
    
    public void snapAllWidgetsToGrid()
    {
        final int n = drawingManager.getNumWidgets();
        Widget[] widgets = new Widget[ n ];
        for ( int i = 0; i < n; i++ )
            widgets[i] = drawingManager.getWidget( i );
        
        for ( int i = 0; i < n; i++ )
            snapWidgetToGrid( widgets[i] );
    }
    
    private void drawGrid()
    {
        if ( !isGridUsed() )
            return;
        
        Graphics2D g2 = backgroundImage.createGraphics();
        g2.setColor( Color.BLACK );
        
        final int gridOffsetX = this.gridOffsetX.getIntValue();
        final int gridOffsetY = this.gridOffsetY.getIntValue();
        final int gridSizeX = this.gridSizeX.getIntValue();
        final int gridSizeY = this.gridSizeY.getIntValue();
        final int gameResX = drawingManager.getGameResolution().getViewportWidth();
        final int gameResY = drawingManager.getGameResolution().getViewportHeight();
        
        for ( int x = gridSizeX - 1 + gridOffsetX; x < gameResX - gridOffsetX; x += gridSizeX )
        {
            for ( int y = gridSizeY - 1 + gridOffsetY; y < gameResY - gridOffsetY; y += gridSizeY )
            {
                g2.drawLine( x, y, x, y );
            }
        }
    }
    
    public void setBackgroundImage( BufferedImage image )
    {
        this.backgroundImage = image;
        
        if ( ( cacheImage == null ) || ( cacheImage.getWidth() != backgroundImage.getWidth() ) || ( cacheImage.getHeight() != backgroundImage.getHeight() ) )
        {
            cacheImage = new BufferedImage( backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR );
            cacheGraphics = cacheImage.createGraphics();
        }
        
        drawGrid();
        
        cacheGraphics.drawImage( backgroundImage, 0, 0, null );
        
        drawingManager.setAllDirtyFlags();
        oldWidgetRects.clear();
        oldWidgetSubTexRects.clear();
    }
    
    public final BufferedImage getBackgroundImage()
    {
        return ( backgroundImage );
    }
    
    public void setOverlayTexture( TextureImage2D texture )
    {
        this.overlay = texture;
    }
    
    public final TextureImage2D getOverlayTexture()
    {
        return ( overlay );
    }
    
    public void setSelectedWidget( Widget widget, boolean doubleClick )
    {
        boolean selectionChanged = ( widget != this.selectedWidget );
        
        if ( selectionChanged )
        {
            if ( ( this.selectedWidget != null ) && ( this.selectedWidget.getConfiguration() != null ) )
            {
                //selectedWidget.clearRegion( true, overlay );
            }
            
            this.selectedWidget = widget;
            
            this.repaint();
        }
        
        for ( int i = 0; i < selectionListeners.size(); i++ )
        {
            selectionListeners.get( i ).onWidgetSelected( selectedWidget, selectionChanged, doubleClick );
        }
        
        if ( selectionChanged )
        {
            drawingManager.setAllDirtyFlags();
        }
    }
    
    public final Widget getSelectedWidget()
    {
        return ( selectedWidget );
    }
    
    public void removeSelectedWidget()
    {
        if ( selectedWidget == null )
            return;
        
        Logger.log( "Removing selected Widget of type \"" + selectedWidget.getClass().getName() + "\" and name \"" + selectedWidget.getName() + "\"..." );
        
        selectedWidget.clearRegion( overlay, selectedWidget.getPosition().getEffectiveX(), selectedWidget.getPosition().getEffectiveY() );
        __WCPrivilegedAccess.removeWidget( drawingManager, selectedWidget );
        setSelectedWidget( null, false );
        editor.setDirtyFlag();
    }
    
    private void drawSelection( Widget widget, Rect2i[] subTextureRects, Graphics2D g )
    {
        int offsetX = widget.getPosition().getEffectiveX();
        int offsetY = widget.getPosition().getEffectiveY();
        int width = widget.getSize().getEffectiveWidth();
        int height = widget.getSize().getEffectiveHeight();
        
        //texture.getTextureCanvas().setClip( offsetX, offsetY, width, height );
        
        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();
        g.setStroke( SELECTION_STROKE );
        g.setColor( SELECTION_COLOR );
        
        //if ( widget.hasMasterCanvas( true ) )
        {
            g.drawRect( offsetX, offsetY, width, height );
        }
        
        if ( subTextureRects != null )
        {
            for ( int i = 0; i < subTextureRects.length; i++ )
            {
                Rect2i r = subTextureRects[i];
                
                if ( ( r != null ) && !r.isCoveredBy( offsetX, offsetY, width, height ) )
                    g.drawRect( r.getLeft(), r.getTop(), r.getWidth(), r.getHeight() );
            }
        }
        
        g.setStroke( oldStroke );
        g.setColor( oldColor );
        //texture.clearOutline( SELECTION_COLOR, offsetX, offsetY, width, height, 2, false, null );
    }
    
    public void clearWidgetRegion( Widget widget )
    {
        if ( widget == null )
            return;
        
        widget.clearRegion( overlay, widget.getPosition().getEffectiveX(), widget.getPosition().getEffectiveY() );
    }
    
    public void clearSelectedWidgetRegion()
    {
        clearWidgetRegion( selectedWidget );
    }
    
    private long frameIndex = 0;
    
    @Override
    public void repaint()
    {
        super.repaint();
    }
    
    public static Rect2i getWidgetInnerRect( Widget widget )
    {
        final boolean drawAtZero = false;
        int offsetX = drawAtZero ? 0 : widget.getPosition().getEffectiveX();
        int offsetY = drawAtZero ? 0 : widget.getPosition().getEffectiveY();
        int width = widget.getSize().getEffectiveWidth();
        int height = widget.getSize().getEffectiveHeight();
        
        int borderLW = widget.getBorder().getInnerLeftWidth();
        int borderTH = widget.getBorder().getInnerTopHeight();
        int borderRW = widget.getBorder().getInnerRightWidth();
        int borderBH = widget.getBorder().getInnerBottomHeight();
        
        int offsetX2 = offsetX + borderLW;
        int offsetY2 = offsetY + borderTH;
        int width2 = width - borderLW - borderRW;
        int height2 = height - borderTH - borderBH;
        
        return ( new Rect2i( offsetX2, offsetY2, width2, height2 ) );
    }
    
    private ArrayList<TransformableTexture[]> subTexs = new ArrayList<TransformableTexture[]>();
    
    private static void drawSubTextures( Widget widget, TransformableTexture[] subTextures, AffineTransform[] affineTransforms, Rect2i[] transformedSubRects, Graphics2D g2 )
    {
        if ( subTextures == null )
            return;
        
        Rect2i innerRect = getWidgetInnerRect( widget );
        
        for ( int i = 0; i < subTextures.length; i++ )
        {
            TransformableTexture tt = subTextures[i];
            
            AffineTransform at = null;
            if ( affineTransforms == null )
                at = tt.getTransformForEditor( innerRect.getLeft(), innerRect.getTop() );
            else
                at = affineTransforms[i];
            if ( at == null )
                at = tt.getTransformForEditor( innerRect.getLeft(), innerRect.getTop() );
            
            Rect2i rect = null;
            if ( transformedSubRects == null )
                rect = tt.getTransformedRectForEditor( at );
            else
                rect = transformedSubRects[i];
            if ( rect == null )
                rect = tt.getTransformedRectForEditor( at );
            
            tt.drawInEditor( g2, at, rect );
        }
    }
    
    public static void drawSubTextures( Widget widget, TransformableTexture[] subTextures, Graphics2D g2 )
    {
        drawSubTextures( widget, subTextures, null, null, g2 );
    }
    
    private boolean checkOverlappingWidgetsAndTransferDirtyFlags( Rect2i[][] transformedSubRects, Map<Widget, Rect2i> oldWidgetRects, Map<Widget, Rect2i[]> oldWidgetSubTexRects )
    {
        boolean needsRepeat = false;
        
        int n = drawingManager.getNumWidgets();
        
        for ( int i = 0; i < n; i++ )
        {
            if ( dirtyFlags.get( i ) )
            {
                Widget widget1 = drawingManager.getWidget( i );
                Rect2i r1 = null;
                
                for ( int j = 0; j < n; j++ )
                {
                    Widget widget2 = drawingManager.getWidget( j );
                    
                    if ( !dirtyFlags.get( j ) ) // This also avoids i == j.
                    {
                        if ( r1 == null )
                        {
                            r1 = new Rect2i( widget1.getPosition().getEffectiveX(), widget1.getPosition().getEffectiveY(), widget1.getSize().getEffectiveWidth(), widget1.getSize().getEffectiveHeight() );
                            //Rect2i innerRect1 = getWidgetInnerRect( widget1 );
                            Rect2i oldWidgetRect = oldWidgetRects.get( widget1 );
                            if ( oldWidgetRect != null )
                            {
                                r1.combine( oldWidgetRect );
                            }
                        }
                        
                        Rect2i r2 = new Rect2i( widget2.getPosition().getEffectiveX(), widget2.getPosition().getEffectiveY(), widget2.getSize().getEffectiveWidth(), widget2.getSize().getEffectiveHeight() );
                        
                        if ( r1.intersects( r2 ) )
                        {
                            dirtyFlags.set( j, true );
                            widget2.setDirtyFlag();
                            
                            needsRepeat = true;
                        }
                        else
                        {
                            //Rect2i innerRect2 = getWidgetInnerRect( widget2 );
                            
                            Rect2i r3 = new Rect2i();
                            Rect2i r4 = new Rect2i();
                            
                            // Check, if a sub texture of Widget 1 intersects with the other Widget or one of its sub testures.
                            boolean intersects = false;
                            
                            TransformableTexture[] tts1 = subTexs.get( i );
                            if ( tts1 != null )
                            {
                                for ( int k = 0; k < tts1.length && !intersects; k++ )
                                {
                                    if ( tts1[k].isVisibleInEditor() )
                                    {
                                        //r3.set( innerRect1.getLeft() + (int)tts1[k].getTransX(), innerRect1.getTop() + (int)tts1[k].getTransY(), tts1[k].getWidth(), tts1[k].getHeight() );
                                        r3.set( transformedSubRects[i][k] );
                                        
                                        Rect2i[] oldSubRects = oldWidgetSubTexRects.get( widget1 );
                                        if ( ( oldSubRects != null ) && ( oldSubRects.length > k ) && ( oldSubRects[k] != null ) )
                                        {
                                            r3.combine( oldSubRects[k] ); // The index may be wrong, if the number or order of sub textures in the Widget has changed!
                                        }
                                        
                                        if ( r3.intersects( r2 ) )
                                        {
                                            intersects = true;
                                        }
                                        else
                                        {
                                            TransformableTexture[] tts2 = subTexs.get( j );
                                            if ( tts2 != null )
                                            {
                                                for ( int l = 0; l < tts2.length && !intersects; l++ )
                                                {
                                                    if ( tts2[l].isVisibleInEditor() )
                                                    {
                                                        //r4.set( innerRect2.getLeft() + (int)tts2[l].getTransX(), innerRect2.getTop() + (int)tts2[l].getTransY(), tts2[l].getWidth(), tts2[l].getHeight() );
                                                        r4.set( transformedSubRects[j][l] );
                                                        
                                                        Rect2i[] oldSubRects2 = oldWidgetSubTexRects.get( widget2 );
                                                        if ( ( oldSubRects2 != null ) && ( oldSubRects2.length > l ) && ( oldSubRects2[l] != null ) )
                                                        {
                                                            r4.combine( oldSubRects2[l] ); // The index may be wrong, if the number or order of sub textures in the Widget has changed!
                                                        }
                                                        
                                                        if ( r4.intersects( r1 ) || r4.intersects( r3 ) )
                                                        {
                                                            intersects = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else
                            {
                                TransformableTexture[] tts2 = subTexs.get( j );
                                if ( tts2 != null )
                                {
                                    for ( int l = 0; l < tts2.length && !intersects; l++ )
                                    {
                                        if ( tts2[l].isVisibleInEditor() )
                                        {
                                            //r4.set( innerRect2.getLeft() + (int)tts2[l].getTransX(), innerRect2.getTop() + (int)tts2[l].getTransY(), tts2[l].getWidth(), tts2[l].getHeight() );
                                            r4.set( transformedSubRects[j][l] );
                                            
                                            Rect2i[] oldSubRects2 = oldWidgetSubTexRects.get( widget2 );
                                            if ( ( oldSubRects2 != null ) && ( oldSubRects2.length > l ) && ( oldSubRects2[l] != null ) )
                                            {
                                                r4.combine( oldSubRects2[l] ); // The index may be wrong, if the number or order of sub textures in the Widget has changed!
                                            }
                                            
                                            if ( r4.intersects( r1 ) )
                                            {
                                                intersects = true;
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if ( intersects )
                            {
                                dirtyFlags.set( j, true );
                                widget2.setDirtyFlag();
                                
                                needsRepeat = true;
                            }
                        }
                    }
                }
            }
        }
        
        return ( needsRepeat );
    }
    
    public void drawWidgets( Graphics2D g2, boolean drawEverything, boolean drawSelection )
    {
        Map<Widget, Rect2i> oldWidgetRects = this.oldWidgetRects;
        Map<Widget, Rect2i[]> oldWidgetSubTexRects = this.oldWidgetSubTexRects;
        
        if ( drawEverything )
        {
            oldWidgetRects = new HashMap<Widget, Rect2i>();
            oldWidgetSubTexRects = new HashMap<Widget, Rect2i[]>();
            
            drawingManager.setAllDirtyFlags();
        }
        
        frameIndex++;
        
        try
        {
            int n = drawingManager.getNumWidgets();
            AffineTransform[][] affineTransforms = new AffineTransform[ n ][];
            Rect2i[][] transformedSubRects = new Rect2i[ n ][];
            
            //g.drawImage( backgroundImage, 0, 0, backgroundImage.getWidth(), backgroundImage.getHeight(), 0, 0, backgroundImage.getWidth(), backgroundImage.getHeight(), null );
            
            dirtyFlags.clear();
            subTexs.clear();
            for ( int i = 0; i < n; i++ )
            {
                Widget widget = drawingManager.getWidget( i );
                
                dirtyFlags.add( widget.getDirtyFlag( false ) );
                
                Rect2i innerRect = getWidgetInnerRect( widget );
                TransformableTexture[] subTextures = widget.getSubTextures( gameData, true, innerRect.getWidth(), innerRect.getHeight() );
                subTexs.add( subTextures );
                
                if ( subTextures != null )
                {
                    affineTransforms[i] = new AffineTransform[ subTextures.length ];
                    transformedSubRects[i] = new Rect2i[ subTextures.length ];
                    
                    for ( int j = 0; j < subTextures.length; j++ )
                    {
                        TransformableTexture tt = subTextures[j];
                        
                        AffineTransform at = tt.getTransformForEditor( innerRect.getLeft() + tt.getOwnerWidget().getOffsetXToMasterWidget(), innerRect.getTop() +tt.getOwnerWidget().getOffsetYToMasterWidget() );
                        affineTransforms[i][j] = at;
                        
                        transformedSubRects[i][j] = tt.getTransformedRectForEditor( at );
                    }
                }
                else
                {
                    affineTransforms[i] = null;
                    transformedSubRects[i] = null;
                }
            }
            
            while ( checkOverlappingWidgetsAndTransferDirtyFlags( transformedSubRects, oldWidgetRects, oldWidgetSubTexRects ) );
            
            drawingManager.drawWidgets( gameData, true, true );
            
            for ( int i = 0; i < n; i++ )
            {
                Widget widget = drawingManager.getWidget( i );
                
                if ( dirtyFlags.get( i ) )
                {
                    Rect2i innerRect = getWidgetInnerRect( widget );
                    TransformableTexture[] subTextures = subTexs.get( i );
                    
                    if ( subTextures != null )
                    {
                        for ( int j = 0; j < subTextures.length; j++ )
                        {
                            TransformableTexture tt = subTextures[j];
                            
                            AffineTransform at = tt.getTransformForEditor( innerRect.getLeft() + tt.getOwnerWidget().getOffsetXToMasterWidget(), innerRect.getTop() +tt.getOwnerWidget().getOffsetYToMasterWidget() );
                            affineTransforms[i][j] = at;
                            
                            transformedSubRects[i][j] = tt.getTransformedRectForEditor( at );
                        }
                    }
                    else
                    {
                        affineTransforms[i] = null;
                        transformedSubRects[i] = null;
                    }
                }
            }
            
            // clear dirty rects...
            TextureDirtyRectsManager.getDirtyRects( frameIndex, overlay, dirtyRectsBuffer, true );
            //TextureDirtyRectsManager.drawDirtyRects( overlay );
            short numDirtyRects = dirtyRectsBuffer.getShort();
            for ( short i = 0; i < numDirtyRects; i++ )
            {
                int drX = dirtyRectsBuffer.getShort();
                int drY = dirtyRectsBuffer.getShort();
                int drW = dirtyRectsBuffer.getShort();
                int drH = dirtyRectsBuffer.getShort();
                
                cacheGraphics.drawImage( backgroundImage, drX, drY, drX + drW, drY + drH, drX, drY, drX + drW, drY + drH, null );
            }
            
            // clear old rectangles of dirty widgets' sub textures...
            for ( int i = 0; i < n; i++ )
            {
                Widget widget = drawingManager.getWidget( i );
                
                if ( dirtyFlags.get( i ) )
                {
                    Rect2i[] subRects = oldWidgetSubTexRects.get( widget );
                    if ( subRects != null )
                    {
                        for ( int j = 0; j < subRects.length; j++ )
                        {
                            if ( subRects[j] != null )
                            {
                                int drX = subRects[j].getLeft();
                                int drY = subRects[j].getTop();
                                int drW = subRects[j].getWidth();
                                int drH = subRects[j].getHeight();
                                
                                cacheGraphics.drawImage( backgroundImage, drX, drY, drX + drW, drY + drH, drX, drY, drX + drW, drY + drH, null );
                            }
                        }
                    }
                }
            }
            
            Rect2i[] selSubRects = null;
            
            // draw dirty widgets...
            BufferedImage bi = overlay.getBufferedImage();
            for ( int i = 0; i < n; i++ )
            {
                Widget widget = drawingManager.getWidget( i );
                TransformableTexture[] subTextures = subTexs.get( i );
                
                int offsetX = widget.getPosition().getEffectiveX();
                int offsetY = widget.getPosition().getEffectiveY();
                int width = widget.getSize().getEffectiveWidth();
                int height = widget.getSize().getEffectiveHeight();
                
                if ( dirtyFlags.get( i ) )
                {
                    //cacheGraphics.drawImage( backgroundImage, offsetX, offsetY, offsetX + width, offsetY + height, offsetX, offsetY, offsetX + width, offsetY + height, null );
                    cacheGraphics.drawImage( bi, offsetX, offsetY, offsetX + width, offsetY + height, offsetX, offsetY, offsetX + width, offsetY + height, null );
                    
                    if ( oldWidgetRects.get( widget ) == null ) // is rendered first?
                        drawSubTextures( widget, subTextures, null, null, cacheGraphics );
                    else
                        drawSubTextures( widget, subTextures, affineTransforms[i], transformedSubRects[i], cacheGraphics );
                    
                    if ( widget == selectedWidget )
                        selSubRects = transformedSubRects[i];
                    
                    oldWidgetSubTexRects.put( widget, transformedSubRects[i] );
                }
                
                // Remember this Widget's rect as old.
                Rect2i oldWidgetRect = oldWidgetRects.get( widget );
                if ( oldWidgetRect == null )
                {
                    oldWidgetRect = new Rect2i();
                    oldWidgetRects.put( widget, oldWidgetRect );
                }
                oldWidgetRect.set( offsetX, offsetY, width, height );
            }
            
            // Draw the whole thing to the panel.
            g2.drawImage( cacheImage, 0, 0, cacheImage.getWidth(), cacheImage.getHeight(), 0, 0, cacheImage.getWidth(), cacheImage.getHeight(), null );
            
            // Draw selection.
            if ( drawSelection && ( selectedWidget != null ) )
            {
                drawSelection( selectedWidget, selSubRects, g2 );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    @Override
    public void paintComponent( Graphics g )
    {
        //super.paintComponent( g );
        
        //System.out.println( "paintComponent()" );
        
        drawWidgets( (Graphics2D)g, false, true );
    }
    
    public EditorPanel( RFDynHUDEditor editor, LiveGameData gameData, WidgetsDrawingManager drawingManager )
    {
        super();
        
        this.editor = editor;
        
        this.gameData = gameData;
        
        this.overlay = drawingManager.getMainTexture( 0 );
        this.drawingManager = drawingManager;
        this.gameResolution = drawingManager.getGameResolution();
        
        EditorPanelInputHandler inputHandler = new EditorPanelInputHandler( editor, drawingManager );
        this.addMouseListener( inputHandler );
        this.addMouseMotionListener( inputHandler );
        this.addKeyListener( inputHandler );
        this.setFocusable( true );
    }
}
