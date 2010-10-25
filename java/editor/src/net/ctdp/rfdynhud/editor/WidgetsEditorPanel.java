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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.render.TextureDirtyRectsManager;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.widgets.widget.AbstractAssembledWidget;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

import org.openmali.types.twodee.Rect2i;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetsEditorPanel extends JPanel
{
    private static final long serialVersionUID = -4217992603083635127L;
    
    private final WidgetsEditorPanelSettings settings;
    
    private final LiveGameData gameData;
    
    private TextureImage2D overlay;
    private final WidgetsDrawingManager drawingManager;
    private final ByteBuffer dirtyRectsBuffer = TextureDirtyRectsManager.createByteBuffer( 1024 );
    private final ArrayList<Boolean> dirtyFlags = new ArrayList<Boolean>();
    private final Map<Widget, Rect2i> oldWidgetRects = new WeakHashMap<Widget, Rect2i>();
    private final Map<Widget, Rect2i[]> oldWidgetSubTexRects = new WeakHashMap<Widget, Rect2i[]>();
    
    private AbstractAssembledWidget scopeWidget = null;
    private Widget selectedWidget = null;
    private static final Stroke SELECTION_STROKE = new BasicStroke( 2 );
    private static final java.awt.Color SELECTION_COLOR = new java.awt.Color( 255, 0, 0, 127 );
    private static final java.awt.Color SUB_SELECTION_COLOR = new java.awt.Color( 255, 64, 0, 64 );
    private static final java.awt.Color MASTER_SELECTION_COLOR = new java.awt.Color( 0, 255, 0, 127 );
    
    private final ArrayList<WidgetSelectionListener> selectionListeners = new ArrayList<WidgetSelectionListener>();
    private final ArrayList<WidgetsEditorPanelListener> listeners = new ArrayList<WidgetsEditorPanelListener>();
    
    private BufferedImage backgroundImage = null;
    private BufferedImage cacheImage = null;
    private Graphics2D cacheGraphics = null;
    
    private float scaleFactor = 1.0f;
    private float recipScaleFactor = 1.0f;
    
    private int repaintCounter = 0;
    
    public final WidgetsDrawingManager getWidgetsDrawingManager()
    {
        return ( drawingManager );
    }
    
    public final WidgetsEditorPanelSettings getSettings()
    {
        return ( settings );
    }
    
    public boolean setScaleFactor( float scale )
    {
        float oldZoomLevel = this.scaleFactor;
        
        scale = Math.max( 0.1f, scale );
        
        if ( scale == this.scaleFactor )
            return ( false );
        
        this.scaleFactor = scale;
        this.recipScaleFactor = 1.0f / scale;
        
        if ( backgroundImage != null )
        {
            Dimension size = new Dimension( Math.round( backgroundImage.getWidth() * scaleFactor ), Math.round( backgroundImage.getHeight() * scaleFactor ) );
            setPreferredSize( size );
            setMaximumSize( size );
            setMinimumSize( size );
            
            if ( ( getParent() != null ) && ( getParent().getParent() instanceof JScrollPane ) )
            {
                getParent().doLayout();
                getParent().getParent().repaint();
            }
        }
        
        for ( int i = 0; i < listeners.size(); i++ )
        {
            listeners.get( i ).onZoomLevelChanged( oldZoomLevel, this.scaleFactor );
        }
        
        return ( true );
    }
    
    public final float getScaleFactor()
    {
        return ( scaleFactor );
    }
    
    public final float getRecipScaleFactor()
    {
        return ( recipScaleFactor );
    }
    
    public void switchToGameResolution( int resX, int resY )
    {
        drawingManager.resizeMainTexture( resX, resY );
        
        for ( int i = 0; i < drawingManager.getNumWidgets(); i++ )
        {
            drawingManager.getWidget( i ).forceAndSetDirty( true );
            __WPrivilegedAccess.onCanvasSizeChanged( drawingManager.getWidget( i ) );
        }
        
        setBackgroundImage( settings.loadBackgroundImage() );
        
        setSelectedWidget( selectedWidget, false );
        
        getRootPane().getParent().validate(); // window
        repaint();
    }
    
    public void addWidgetSelectionListener( WidgetSelectionListener l )
    {
        selectionListeners.add( l );
    }
    
    public void removeWidgetSelectionListener( WidgetSelectionListener l )
    {
        selectionListeners.remove( l );
    }
    
    public void addWidgetsEditorPanelListener( WidgetsEditorPanelListener l )
    {
        listeners.add( l );
    }
    
    public void removeWidgetsEditorPanelListener( WidgetsEditorPanelListener l )
    {
        listeners.remove( l );
    }
    
    private void drawGrid()
    {
        if ( ( settings == null ) || !settings.isGridUsed() )
            return;
        
        Graphics2D g2 = backgroundImage.createGraphics();
        g2.setColor( Color.BLACK );
        
        final int gridOffsetX = settings.getGridOffsetX();
        final int gridOffsetY = settings.getGridOffsetY();
        final int gridSizeX = settings.getGridSizeX();
        final int gridSizeY = settings.getGridSizeY();
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
    
    public boolean fixSelectedWidgetWidthByBackgroundAspect()
    {
        Widget widget = getSelectedWidget();
        
        float aspect = widget.getBackgroundProperty().getImageProperty().getImage().getBaseAspect();
        
        clearWidgetRegion( widget );
        
        widget.getSize().setEffectiveSize( Math.round( widget.getSize().getEffectiveHeight() * aspect ), widget.getSize().getEffectiveHeight() );
        
        repaint();
        
        for ( int i = 0; i < listeners.size(); i++ )
            listeners.get( i ).onWidgetPositionSizeChanged( widget );
        
        return ( true );
    }
    
    public boolean fixSelectedWidgetHeightByBackgroundAspect()
    {
        Widget widget = getSelectedWidget();
        
        float aspect = widget.getBackgroundProperty().getImageProperty().getImage().getBaseAspect();
        
        clearWidgetRegion( widget );
        
        widget.getSize().setEffectiveSize( widget.getSize().getEffectiveWidth(), Math.round( widget.getSize().getEffectiveWidth() / aspect ) );
        
        repaint();
        
        for ( int i = 0; i < listeners.size(); i++ )
            listeners.get( i ).onWidgetPositionSizeChanged( widget );
        
        return ( true );
    }
    
    public boolean snapSelectedWidgetToGrid()
    {
        if ( selectedWidget != null )
        {
            clearWidgetRegion( selectedWidget );
            getSettings().snapWidgetToGrid( selectedWidget );
            setSelectedWidget( selectedWidget, false );
            repaint();
            
            return ( true );
        }
        
        return ( false );
    }
    
    public boolean snapAllWidgetsToGrid()
    {
        for ( int i = 0; i < drawingManager.getNumWidgets(); i++ )
            clearWidgetRegion( drawingManager.getWidget( i ) );
        settings.snapAllWidgetsToGrid();
        setSelectedWidget( selectedWidget, false );
        repaint();
        
        return ( true );
    }
    
    private void clearEverything()
    {
        cacheGraphics.drawImage( backgroundImage, 0, 0, null );
        
        drawingManager.setAllDirtyFlags();
        oldWidgetRects.clear();
        oldWidgetSubTexRects.clear();
    }
    
    public void setBackgroundImage( BufferedImage image )
    {
        boolean needsClear = ( this.backgroundImage != null );
        
        this.backgroundImage = image;
        
        if ( ( cacheImage == null ) || ( cacheImage.getWidth() != backgroundImage.getWidth() ) || ( cacheImage.getHeight() != backgroundImage.getHeight() ) )
        {
            cacheImage = new BufferedImage( backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR );
            cacheGraphics = cacheImage.createGraphics();
        }
        
        drawGrid();
        
        if ( needsClear )
            clearEverything();
        
        this.overlay = drawingManager.getMainTexture( 0 );
        
        Dimension size = new Dimension( Math.round( backgroundImage.getWidth() * scaleFactor ), Math.round( backgroundImage.getHeight() * scaleFactor ) );
        setPreferredSize( size );
        setMaximumSize( size );
        setMinimumSize( size );
        
        if ( ( this.getParent() != null )  && ( this.getParent().getParent() instanceof JScrollPane ) )
            ( (JScrollPane)this.getParent().getParent() ).doLayout();
    }
    
    public BufferedImage initBackgroundImage()
    {
        setBackgroundImage( getSettings().loadBackgroundImage() );
        
        return ( getBackgroundImage() );
    }
    
    public final BufferedImage getBackgroundImage()
    {
        return ( backgroundImage );
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
        
        for ( int i = 0; i < listeners.size(); i++ )
        {
            listeners.get( i ).onWidgetSelected( selectedWidget, selectionChanged, doubleClick );
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
        boolean accepted = true;
        
        for ( int i = 0; i < listeners.size(); i++ )
        {
            if ( !listeners.get( i ).onWidgetRemoved( selectedWidget ) )
                accepted = false;
        }
        
        if ( accepted )
        {
            setSelectedWidget( null, false );
            
            clearEverything();
            repaint();
        }
    }
    
    public final AbstractAssembledWidget getScopeWidget()
    {
        return ( scopeWidget );
    }
    
    public void goInto( AbstractAssembledWidget widget )
    {
        if ( widget == null )
        {
            Widget selWidget = scopeWidget;
            this.scopeWidget = widget;
            if ( selWidget != null )
                while ( selWidget.getMasterWidget() != null )
                    selWidget = selWidget.getMasterWidget();
            setSelectedWidget( selWidget, false );
        }
        else
        {
            this.scopeWidget = widget;
            setSelectedWidget( null, false );
        }
        
        for ( int i = 0; i < listeners.size(); i++ )
        {
            listeners.get( i ).onScopeWidgetChanged( widget );
        }
        
        repaint();
    }
    
    public void requestContextMenu( Widget[] hoveredWidgets )
    {
        for ( int i = 0; i < listeners.size(); i++ )
        {
            listeners.get( i ).onContextMenuRequested( hoveredWidgets, scopeWidget );
        }
    }
    
    public void onSelectedWidgetPositionSizeChanged()
    {
        int repaintCounter = this.repaintCounter;
        
        for ( int i = 0; i < listeners.size(); i++ )
        {
            listeners.get( i ).onWidgetPositionSizeChanged( selectedWidget );
        }
        
        if ( repaintCounter >= this.repaintCounter )
            repaint();
    }
    
    private void drawSelection( AbstractAssembledWidget master, Widget widget, Rect2i[] subTextureRects, Graphics2D g )
    {
        //texture.getTextureCanvas().setClip( offsetX, offsetY, width, height );
        
        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();
        g.setStroke( SELECTION_STROKE );
        
        if ( master != null )
        {
            g.setColor( MASTER_SELECTION_COLOR );
            
            while ( master != null )
            {
                g.drawRect( Math.round( master.getAbsoluteOffsetX() * scaleFactor ), Math.round( master.getAbsoluteOffsetY() * scaleFactor ), Math.round( master.getSize().getEffectiveWidth() * scaleFactor ), Math.round( master.getSize().getEffectiveHeight() * scaleFactor ) );
                
                master = master.getMasterWidget();
            }
        }
        
        g.setColor( SELECTION_COLOR );
        
        if ( widget != null )
        {
            int offsetX = widget.getAbsoluteOffsetX();
            int offsetY = widget.getAbsoluteOffsetY();
            int width = widget.getSize().getEffectiveWidth();
            int height = widget.getSize().getEffectiveHeight();
            
            //if ( widget.hasMasterCanvas( true ) )
            {
                g.drawRect( Math.round( offsetX * scaleFactor ), Math.round( offsetY * scaleFactor ), Math.round( width * scaleFactor ), Math.round( height * scaleFactor ) );
            }
            
            if ( subTextureRects != null )
            {
                g.setColor( SUB_SELECTION_COLOR );
                
                for ( int i = 0; i < subTextureRects.length; i++ )
                {
                    Rect2i r = subTextureRects[i];
                    
                    if ( ( r != null ) && !r.isCoveredBy( offsetX, offsetY, width, height ) )
                        g.drawRect( Math.round( r.getLeft() * scaleFactor ), Math.round( r.getTop() * scaleFactor ), Math.round( r.getWidth() * scaleFactor ), Math.round( r.getHeight() * scaleFactor ) );
                }
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
        
        widget.clearRegion( overlay, widget.getAbsoluteOffsetX(), widget.getAbsoluteOffsetY() );
    }
    
    public void clearSelectedWidgetRegion()
    {
        clearWidgetRegion( selectedWidget );
    }
    
    @Override
    public void repaint()
    {
        super.repaint();
        
        repaintCounter++;
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
                at = tt.getTransformForEditor( innerRect.getLeft() + tt.getOffsetXToRootMasterWidget(), innerRect.getTop() + tt.getOffsetYToRootMasterWidget() );
            else
                at = affineTransforms[i];
            if ( at == null )
                at = tt.getTransformForEditor( innerRect.getLeft() + tt.getOffsetXToRootMasterWidget(), innerRect.getTop() + tt.getOffsetYToRootMasterWidget() );
            
            Rect2i rect = null;
            if ( transformedSubRects == null )
                rect = tt.getTransformedRectForEditor( at );
            else
                rect = transformedSubRects[i];
            if ( rect == null )
                rect = tt.getTransformedRectForEditor( at );
            
            tt.drawInEditor( g2, at, rect );
            
            TextureDirtyRectsManager.getDirtyRects( tt.getTexture(), null, false );
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
                        
                        AffineTransform at = tt.getTransformForEditor( innerRect.getLeft() + tt.getOffsetXToRootMasterWidget(), innerRect.getTop() + tt.getOffsetYToRootMasterWidget() );
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
                            
                            AffineTransform at = tt.getTransformForEditor( innerRect.getLeft() + tt.getOffsetXToRootMasterWidget(), innerRect.getTop() + tt.getOffsetYToRootMasterWidget() );
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
            TextureDirtyRectsManager.getDirtyRects( overlay, dirtyRectsBuffer, true );
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
                    
                    oldWidgetSubTexRects.put( widget, transformedSubRects[i] );
                }
                
                if ( widget == selectedWidget )
                    selSubRects = transformedSubRects[i];
                
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
            g2.drawImage( cacheImage, 0, 0, Math.round( cacheImage.getWidth() * scaleFactor ), Math.round( cacheImage.getHeight() * scaleFactor ), 0, 0, cacheImage.getWidth(), cacheImage.getHeight(), null );
            
            // Draw selection.
            if ( drawSelection )
            {
                if ( selectedWidget == null )
                {
                    if ( scopeWidget != null )
                        drawSelection( scopeWidget, null, selSubRects, g2 );
                }
                else
                {
                    if ( selectedWidget.getMasterWidget() == null )
                        drawSelection( null, selectedWidget, selSubRects, g2 );
                    else
                        drawSelection( selectedWidget.getMasterWidget(), selectedWidget, selSubRects, g2 );
                }
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
        boolean paintSuper = false;
        if ( ( getParent() != null ) && ( getParent().getParent() instanceof JScrollPane ) )
        {
            JScrollPane sp = (JScrollPane)getParent().getParent();
            
            paintSuper = !sp.getHorizontalScrollBar().isVisible() && !sp.getVerticalScrollBar().isVisible();
        }
        
        if ( paintSuper )
            super.paintComponent( g );
        
        //System.out.println( "paintComponent()" );
        
        drawWidgets( (Graphics2D)g, false, true );
    }
    
    public WidgetsEditorPanel( WidgetsEditorPanelSettings settings, RFDynHUDEditor editor, LiveGameData gameData, WidgetsDrawingManager drawingManager )
    {
        super();
        
        this.settings = ( settings == null ) ? new WidgetsEditorPanelSettings( drawingManager, editor, this ) : settings;
        
        this.gameData = gameData;
        
        this.overlay = drawingManager.getMainTexture( 0 );
        this.drawingManager = drawingManager;
        
        this.setFocusable( true );
    }
    
    public WidgetsEditorPanel( RFDynHUDEditor editor, LiveGameData gameData, WidgetsDrawingManager drawingManager )
    {
        this( null, editor, gameData, drawingManager );
    }
}
