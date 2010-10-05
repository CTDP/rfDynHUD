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

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;

import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetsEditorPanelInputHandler implements MouseListener, MouseMotionListener, KeyListener
{
    private static enum BorderPart
    {
        NONE,
        TOP_LEFT,
        TOP,
        TOP_RIGHT,
        LEFT,
        RIGHT,
        BOTTOM_LEFT,
        BOTTOM,
        BOTTOM_RIGHT,
        ;
    }
    
    private static final int RESIZE_BORDER = 10;
    
    private final WidgetsEditorPanel editorPanel;
    
    private WidgetsDrawingManager widgetsManager;
    
    private BorderPart overBorderPart = null;
    
    private int mousePressedX = -1;
    private int mousePressedY = -1;
    private int widgetDragStartX = -1;
    private int widgetDragStartY = -1;
    private int widgetDragStartWidth = -1;
    private int widgetDragStartHeight = -1;
    private Widget draggedWidget = null;
    
    private boolean isShiftDown = false;
    private boolean isControlDown = false;
    
    private Widget getWidgetUnderMouse( int x, int y )
    {
        for ( int i = widgetsManager.getNumWidgets() - 1; i >= 0; i-- )
        {
            Widget widget = widgetsManager.getWidget( i );
            
            int wx = widget.getPosition().getEffectiveX();
            int wy = widget.getPosition().getEffectiveY();
            int ww = widget.getSize().getEffectiveWidth();
            int wh = widget.getSize().getEffectiveHeight();
            
            if ( ( wx <= x ) && ( wx + ww > x ) && ( wy <= y ) && ( wy + wh > y ) )
            {
                return ( widget );
            }
        }
        
        return ( null );
    }
    
    @Override
    public void mousePressed( MouseEvent e )
    {
        // I have no idea, why this is necessary.
        editorPanel.requestFocus();
        
        int x = Math.round( e.getX() * editorPanel.getRecipScaleFactor() );
        int y = Math.round( e.getY() * editorPanel.getRecipScaleFactor() );
        
        draggedWidget = getWidgetUnderMouse( x, y );
        //boolean widgetChanged = ( selectedWidget != editor.getEditorPanel().getSelectedWidget() );
        
        //if ( widgetChanged )
            editorPanel.setSelectedWidget( draggedWidget, false );
        
        if ( e.getButton() == MouseEvent.BUTTON1 )
        {
            if ( draggedWidget != null )
            {
                mousePressedX = x;
                mousePressedY = y;
                
                widgetDragStartX = draggedWidget.getPosition().getEffectiveX();
                widgetDragStartY = draggedWidget.getPosition().getEffectiveY();
                
                if ( editorPanel.getCursor().getType() == Cursor.DEFAULT_CURSOR )
                {
                    editorPanel.setCursor( Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR ) );
                }
                else
                {
                    widgetDragStartWidth = draggedWidget.getSize().getEffectiveWidth();
                    widgetDragStartHeight = draggedWidget.getSize().getEffectiveHeight();
                }
            }
        }
        else if ( e.getButton() == MouseEvent.BUTTON3 )
        {
            editorPanel.requestContextMenu();
        }
    }
    
    @Override
    public void mouseReleased( MouseEvent e )
    {
        if ( e.getButton() == MouseEvent.BUTTON1 )
        {
            if ( draggedWidget != null )
            {
                draggedWidget = null;
                mousePressedX = -1;
                mousePressedY = -1;
                widgetDragStartX = -1;
                widgetDragStartY = -1;
                widgetDragStartWidth = -1;
                widgetDragStartHeight = -1;
                
                mouseMoved( e );
                
                widgetsManager.setAllDirtyFlags();
                
                editorPanel.repaint();
            }
        }
    }
    
    @Override
    public void mouseClicked( MouseEvent e )
    {
        if ( ( e.getButton() == MouseEvent.BUTTON1 ) && ( editorPanel.getSelectedWidget() != null ) )
        {
            editorPanel.setSelectedWidget( editorPanel.getSelectedWidget(), e.getClickCount() == 2 );
        }
    }
    
    @Override
    public void mouseEntered( MouseEvent e )
    {
    }
    
    @Override
    public void mouseExited( MouseEvent e )
    {
    }
    
    @Override
    public void mouseMoved( MouseEvent e )
    {
        int x = Math.round( e.getX() * editorPanel.getRecipScaleFactor() );
        int y = Math.round( e.getY() * editorPanel.getRecipScaleFactor() );
        
        Widget widget = getWidgetUnderMouse( x, y );
        
        if ( ( widget != null ) && !widget.hasFixedSize() )
        {
            int effX = widget.getPosition().getEffectiveX();
            int effY = widget.getPosition().getEffectiveY();
            int effW = widget.getSize().getEffectiveWidth();
            int effH = widget.getSize().getEffectiveHeight();
            
            Cursor cursor = editorPanel.getCursor();
            
            if ( x < effX + RESIZE_BORDER )
            {
                if ( y < effY + RESIZE_BORDER )
                {
                    overBorderPart = BorderPart.TOP_LEFT;
                    cursor = Cursor.getPredefinedCursor( Cursor.NW_RESIZE_CURSOR );
                }
                else if ( y < effY + effH - RESIZE_BORDER )
                {
                    overBorderPart = BorderPart.LEFT;
                    cursor = Cursor.getPredefinedCursor( Cursor.W_RESIZE_CURSOR );
                }
                else
                {
                    overBorderPart = BorderPart.BOTTOM_LEFT;
                    cursor = Cursor.getPredefinedCursor( Cursor.SW_RESIZE_CURSOR );
                }
            }
            else if ( x > effX + effW - RESIZE_BORDER )
            {
                if ( y < effY + RESIZE_BORDER )
                {
                    overBorderPart = BorderPart.TOP_RIGHT;
                    cursor = Cursor.getPredefinedCursor( Cursor.NE_RESIZE_CURSOR );
                }
                else if ( y < effY + effH - RESIZE_BORDER )
                {
                    overBorderPart = BorderPart.RIGHT;
                    cursor = Cursor.getPredefinedCursor( Cursor.E_RESIZE_CURSOR );
                }
                else
                {
                    overBorderPart = BorderPart.BOTTOM_RIGHT;
                    cursor = Cursor.getPredefinedCursor( Cursor.SE_RESIZE_CURSOR );
                }
            }
            else if ( y < effY + RESIZE_BORDER )
            {
                overBorderPart = BorderPart.TOP;
                cursor = Cursor.getPredefinedCursor( Cursor.N_RESIZE_CURSOR );
            }
            else if ( y > effY + effH - RESIZE_BORDER )
            {
                overBorderPart = BorderPart.BOTTOM;
                cursor = Cursor.getPredefinedCursor( Cursor.S_RESIZE_CURSOR );
            }
            else
            {
                overBorderPart = BorderPart.NONE;
                cursor = Cursor.getDefaultCursor();
            }
            
            if ( cursor != editorPanel.getCursor() )
            {
                editorPanel.setCursor( cursor );
            }
        }
        else
        {
            overBorderPart = null;
            editorPanel.setCursor( Cursor.getDefaultCursor() );
        }
    }
    
    private static final byte FLAG_SNAP_X = 1;
    private static final byte FLAG_SNAP_Y = 2;
    
    private byte snapPositionToRail( Widget widget, Point point )
    {
        byte result = 0;
        
        if ( !isControlDown )
        {
            if ( ( editorPanel.getSettings().getRailDistanceX() > 0 ) || ( editorPanel.getSettings().getRailDistanceY() > 0 ) )
            {
                WidgetsConfiguration wc = widgetsManager;
                int closestRailX = Integer.MAX_VALUE;
                int closestRailY = Integer.MAX_VALUE;
                int rdx, rdy;
                for ( int i = 0; i < wc.getNumWidgets(); i++ )
                {
                    Widget w = wc.getWidget( i );
                    if ( w != widget )
                    {
                        rdx = w.getPosition().getEffectiveX() - point.x;
                        rdy = w.getPosition().getEffectiveY() - point.y;
                        
                        if ( Math.abs( rdx ) < Math.abs( closestRailX ) )
                            closestRailX = rdx;
                        if ( Math.abs( rdy ) < Math.abs( closestRailY ) )
                            closestRailY = rdy;
                        
                        rdx = w.getPosition().getEffectiveX() + w.getSize().getEffectiveWidth() - point.x;
                        rdy = w.getPosition().getEffectiveY() + w.getSize().getEffectiveHeight() - point.y;
                        
                        if ( Math.abs( rdx ) < Math.abs( closestRailX ) )
                            closestRailX = rdx;
                        if ( Math.abs( rdy ) < Math.abs( closestRailY ) )
                            closestRailY = rdy;
                    }
                }
                
                if ( Math.abs( closestRailX ) < editorPanel.getSettings().getRailDistanceX() )
                {
                    point.x += closestRailX;
                    result |= FLAG_SNAP_X;
                }
                
                if ( Math.abs( closestRailY ) < editorPanel.getSettings().getRailDistanceY() )
                {
                    point.y += closestRailY;
                    result |= FLAG_SNAP_Y;
                }
            }
            
            if ( editorPanel.getSettings().isGridUsed() )
            {
                point.x = editorPanel.getSettings().snapXToGrid( point.x );
                point.y = editorPanel.getSettings().snapYToGrid( point.y );
                
                result = FLAG_SNAP_X | FLAG_SNAP_Y;
            }
        }
        
        return ( result );
    }
    
    private RelativePositioning fixPositioning( Widget widget, int x, int y, int w, int h )
    {
        final int gameResX = widget.getConfiguration().getGameResolution().getViewportWidth();
        final int gameResY = widget.getConfiguration().getGameResolution().getViewportHeight();
        
        RelativePositioning positioning = widget.getPosition().getPositioning();
        
        if ( positioning.isLeft() )
        {
            if ( x / (float)( gameResX - x - w ) > 0.4f )
                positioning = positioning.deriveHCenter();
            else if ( x + w / 2 >= gameResX / 2 + 50 )
                positioning = positioning.deriveRight();
        }
        else if ( positioning.isRight() )
        {
            if ( (float)( gameResX - x - w ) / x > 0.4f )
                positioning = positioning.deriveHCenter();
            else if ( x + w / 2 < gameResX / 2 - 50 )
                positioning = positioning.deriveLeft();
        }
        else if ( positioning.isHCenter() )
        {
            int centerWidth = gameResX * 5 / 6;
            if ( x < ( gameResX - centerWidth ) / 2 + 50 )
                positioning = positioning.deriveLeft();
            else if ( x + w > gameResX - ( gameResX - centerWidth ) / 2 - 50 )
                positioning = positioning.deriveRight();
        }
        
        if ( positioning.isTop() )
        {
            if ( y / (float)( gameResY - y - h ) > 0.4f )
                positioning = positioning.deriveVCenter();
            //else if ( y + effHeight / 2 >= gameResY * 8 / 10 )
            //    positioning = positioning.deriveBottom();
        }
        else if ( positioning.isBottom() )
        {
            if ( (float)( gameResY - y - h ) / y > 0.4f )
                positioning = positioning.deriveVCenter();
            //else if ( y + effHeight / 2 < gameResY * 8 / 10 )
            //    positioning = positioning.deriveTop();
        }
        else if ( positioning.isVCenter() )
        {
            if ( y / (float)( gameResY - y - h ) < 0.3f )
                positioning = positioning.deriveTop();
            else if ( y / (float)( gameResY - y - h ) > 3.333f )
                positioning = positioning.deriveBottom();
        }
        
        return ( positioning );
    }
    
    private boolean setWidgetPosition( Widget widget, int x, int y )
    {
        Point p = new Point( x, y );
        byte snapped1 = snapPositionToRail( widget, p );
        
        int dx1 = ( ( snapped1 & FLAG_SNAP_X ) != 0 ) ? p.x - x : Integer.MAX_VALUE;
        int dy1 = ( ( snapped1 & FLAG_SNAP_Y ) != 0 ) ? p.y - y : Integer.MAX_VALUE;
        
        final int effWidth = widget.getSize().getEffectiveWidth();
        final int effHeight = widget.getSize().getEffectiveHeight();
        
        int x2 = x + effWidth - 1;
        int y2 = y + effHeight - 1;
        
        p.setLocation( x2, y2 );
        byte snapped2 = snapPositionToRail( widget, p );
        
        int dx2 = ( ( snapped2 & FLAG_SNAP_X ) != 0 ) ? p.x - x2 : Integer.MAX_VALUE;
        int dy2 = ( ( snapped2 & FLAG_SNAP_Y ) != 0 ) ? p.y - y2 : Integer.MAX_VALUE;
        
        if ( Math.abs( dx1 ) < Math.abs( dx2 ) )
        {
            if ( ( ( snapped1 & FLAG_SNAP_X ) != 0 ) )
                x += dx1;
        }
        else
        {
            if ( ( ( snapped2 & FLAG_SNAP_X ) != 0 ) )
                x += dx2;
        }
        
        if ( Math.abs( dy1 ) < Math.abs( dy2 ) )
        {
            if ( ( ( snapped1 & FLAG_SNAP_Y ) != 0 ) )
                y += dy1;
        }
        else
        {
            if ( ( ( snapped2 & FLAG_SNAP_Y ) != 0 ) )
                y += dy2;
        }
        
        RelativePositioning positioning = fixPositioning( widget, x, y, effWidth, effHeight );
        
        return ( widget.getPosition().setEffectivePosition( positioning, x, y ) );
    }
    
    @Override
    public void mouseDragged( MouseEvent e )
    {
        if ( draggedWidget != null )
        {
            draggedWidget.clearRegion( editorPanel.getOverlayTexture(), draggedWidget.getPosition().getEffectiveX(), draggedWidget.getPosition().getEffectiveY() );
            
            final int gameResX = draggedWidget.getConfiguration().getGameResolution().getViewportWidth();
            final int gameResY = draggedWidget.getConfiguration().getGameResolution().getViewportHeight();
            
            int dx = Math.round( e.getX() * editorPanel.getRecipScaleFactor() ) - mousePressedX;
            int dy = Math.round( e.getY() * editorPanel.getRecipScaleFactor() ) - mousePressedY;
            
            boolean changed = false;
            
            if ( widgetDragStartWidth >= 0 )
            {
                int x = widgetDragStartX;
                int y = widgetDragStartY;
                
                switch ( editorPanel.getCursor().getType() )
                {
                    case Cursor.NW_RESIZE_CURSOR:
                        x += dx;
                        y += dy;
                        break;
                    case Cursor.N_RESIZE_CURSOR:
                        y += dy;
                        break;
                    case Cursor.NE_RESIZE_CURSOR:
                        y += dy;
                        break;
                    case Cursor.W_RESIZE_CURSOR:
                        x += dx;
                        break;
                    case Cursor.E_RESIZE_CURSOR:
                        break;
                    case Cursor.SW_RESIZE_CURSOR:
                        x += dx;
                        break;
                    case Cursor.S_RESIZE_CURSOR:
                        break;
                    case Cursor.SE_RESIZE_CURSOR:
                        break;
                }
                
                Point p = new Point( x, y );
                snapPositionToRail( draggedWidget, p );
                if ( ( overBorderPart == BorderPart.TOP_LEFT ) || ( overBorderPart == BorderPart.LEFT ) || ( overBorderPart == BorderPart.BOTTOM_LEFT ) )
                {
                    dx += p.x - x;
                    x = p.x;
                }
                if ( ( overBorderPart == BorderPart.TOP_LEFT ) || ( overBorderPart == BorderPart.TOP ) || ( overBorderPart == BorderPart.TOP_RIGHT ) )
                {
                    dy += p.y - y;
                    y = p.y;
                }
                
                int w = widgetDragStartWidth;
                int h = widgetDragStartHeight;
                
                switch ( editorPanel.getCursor().getType() )
                {
                    case Cursor.NW_RESIZE_CURSOR:
                        w -= dx;
                        h -= dy;
                        break;
                    case Cursor.N_RESIZE_CURSOR:
                        h -= dy;
                        break;
                    case Cursor.NE_RESIZE_CURSOR:
                        w += dx;
                        h -= dy;
                        break;
                    case Cursor.W_RESIZE_CURSOR:
                        w -= dx;
                        break;
                    case Cursor.E_RESIZE_CURSOR:
                        w += dx;
                        break;
                    case Cursor.SW_RESIZE_CURSOR:
                        w -= dx;
                        h += dy;
                        break;
                    case Cursor.S_RESIZE_CURSOR:
                        h += dy;
                        break;
                    case Cursor.SE_RESIZE_CURSOR:
                        w += dx;
                        h += dy;
                        break;
                }
                
                int hundretPercentWidth = gameResY * 4 / 3;
                
                if ( !draggedWidget.getSize().isNegativeWidth() && ( w > (int)( hundretPercentWidth * 0.95f ) ) )
                    draggedWidget.getSize().flipWidthSign();
                else if ( draggedWidget.getSize().isNegativeWidth() && ( w < gameResX * 5 / 10 ) )
                    draggedWidget.getSize().flipWidthSign();
                
                if ( !draggedWidget.getSize().isNegativeHeight() && ( h > gameResY * 9 / 10 ) )
                    draggedWidget.getSize().flipHeightSign();
                else if ( draggedWidget.getSize().isNegativeHeight() && ( h < gameResY * 5 / 10 ) )
                    draggedWidget.getSize().flipHeightSign();
                
                w = Math.min( w, gameResX - x );
                h = Math.min( h, gameResY - y );
                
                if ( isShiftDown )
                {
                    float aspect = widgetDragStartWidth / (float)widgetDragStartHeight;
                    
                    switch ( overBorderPart )
                    {
                        case BOTTOM:
                        case TOP:
                            w = Math.round( h * aspect );
                            break;
                        case LEFT:
                        case RIGHT:
                            h = Math.round( w / aspect );
                            break;
                        default:
                            int dw = w - widgetDragStartWidth;
                            int dh = h - widgetDragStartHeight;
                            
                            if ( Math.abs( dw ) >= Math.abs( dh ) )
                                h = Math.round( w / aspect );
                            else
                                w = Math.round( h * aspect );
                            break;
                    }
                }
                
                Point s = new Point( x + w - 1, y + h - 1 );
                snapPositionToRail( draggedWidget, s );
                if ( ( w != widgetDragStartWidth ) && ( ( overBorderPart == BorderPart.TOP_RIGHT ) || ( overBorderPart == BorderPart.RIGHT ) || ( overBorderPart == BorderPart.BOTTOM_RIGHT ) ) )
                    w = s.x + 1 - x;
                if ( ( h != widgetDragStartHeight ) && ( ( overBorderPart == BorderPart.BOTTOM_LEFT ) || ( overBorderPart == BorderPart.BOTTOM ) || ( overBorderPart == BorderPart.BOTTOM_RIGHT ) ) )
                    h = s.y + 1 - y;
                
                RelativePositioning positioning = fixPositioning( draggedWidget, x, y, w, h );
                
                boolean b1 = draggedWidget.getSize().setEffectiveSize( w, h );
                int eh = draggedWidget.getSize().getEffectiveHeight();
                if ( ( h < eh ) && ( ( overBorderPart == BorderPart.TOP_LEFT ) || ( overBorderPart == BorderPart.TOP ) || ( overBorderPart == BorderPart.TOP_RIGHT ) ) )
                    y -= eh - h;
                int ew = draggedWidget.getSize().getEffectiveWidth();
                if ( ( w < ew ) && ( ( overBorderPart == BorderPart.BOTTOM_LEFT ) || ( overBorderPart == BorderPart.LEFT ) || ( overBorderPart == BorderPart.TOP_LEFT ) ) )
                    x -= ew - w;
                boolean b2 = draggedWidget.getPosition().setEffectivePosition( positioning, x, y );
                
                changed = b1 || b2;
            }
            else if ( widgetDragStartX >= 0 )
            {
                int x = widgetDragStartX + dx;
                int y = widgetDragStartY + dy;
                
                int effWidth = draggedWidget.getSize().getEffectiveWidth();
                int effHeight = draggedWidget.getSize().getEffectiveHeight();
                
                x = Math.min( Math.max( 0, x ) + effWidth, gameResX ) - effWidth;
                y = Math.min( Math.max( 0, y ) + effHeight, gameResY ) - effHeight;
                
                changed = setWidgetPosition( draggedWidget, x, y );
            }
            
            if ( changed )
                editorPanel.onSelectedWidgetPositionSizeChanged();
        }
    }
    
    @Override
    public void keyPressed( KeyEvent e )
    {
        switch ( e.getKeyCode() )
        {
            case KeyEvent.VK_DELETE:
                editorPanel.removeSelectedWidget();
                break;
        }
    }
    
    @Override
    public void keyReleased( KeyEvent e )
    {
    }
    
    @Override
    public void keyTyped( KeyEvent e )
    {
    }
    
    private boolean needsRedraw = false;
    
    private void handleGlobalKeyEvent( KeyEvent kev )
    {
        isShiftDown = kev.isShiftDown();
        isControlDown = kev.isControlDown();
        
        Widget w = editorPanel.getSelectedWidget();
        
        if ( ( w != null ) && editorPanel.hasFocus() )
        {
            if ( kev.getKeyCode() == KeyEvent.VK_UP )
            {
                if ( kev.getID() == KeyEvent.KEY_PRESSED )
                {
                    w.clearRegion( editorPanel.getOverlayTexture(), w.getPosition().getEffectiveX(), w.getPosition().getEffectiveY() );
                    if ( setWidgetPosition( w, w.getPosition().getEffectiveX(), w.getPosition().getEffectiveY() - 1 ) )
                    {
                        editorPanel.onSelectedWidgetPositionSizeChanged();
                        needsRedraw = true;
                    }
                }
                
                kev.consume();
            }
            else if ( kev.getKeyCode() == KeyEvent.VK_LEFT )
            {
                if ( kev.getID() == KeyEvent.KEY_PRESSED )
                {
                    w.clearRegion( editorPanel.getOverlayTexture(), w.getPosition().getEffectiveX(), w.getPosition().getEffectiveY() );
                    if ( setWidgetPosition( w, w.getPosition().getEffectiveX() - 1, w.getPosition().getEffectiveY() ) )
                    {
                        editorPanel.onSelectedWidgetPositionSizeChanged();
                        needsRedraw = true;
                    }
                }
                
                kev.consume();
            }
            else if ( kev.getKeyCode() == KeyEvent.VK_RIGHT )
            {
                if ( kev.getID() == KeyEvent.KEY_PRESSED )
                {
                    w.clearRegion( editorPanel.getOverlayTexture(), w.getPosition().getEffectiveX(), w.getPosition().getEffectiveY() );
                    if ( setWidgetPosition( w, w.getPosition().getEffectiveX() + 1, w.getPosition().getEffectiveY() ) )
                    {
                        editorPanel.onSelectedWidgetPositionSizeChanged();
                        needsRedraw = true;
                    }
                }
                
                kev.consume();
            }
            else if ( kev.getKeyCode() == KeyEvent.VK_DOWN )
            {
                if ( kev.getID() == KeyEvent.KEY_PRESSED )
                {
                    w.clearRegion( editorPanel.getOverlayTexture(), w.getPosition().getEffectiveX(), w.getPosition().getEffectiveY() );
                    if ( setWidgetPosition( w, w.getPosition().getEffectiveX(), w.getPosition().getEffectiveY() + 1 ) )
                    {
                        editorPanel.onSelectedWidgetPositionSizeChanged();
                        needsRedraw = true;
                    }
                }
                
                kev.consume();
            }
        }
    }
    
    /**
     * 
     * @param mwev
     */
    private void handleGlobalMouseWheelEvent( MouseWheelEvent mwev )
    {
        if ( editorPanel.hasFocus() )
        {
            if ( needsRedraw )
                widgetsManager.setAllDirtyFlags();
            
            needsRedraw = false;
            
        }
    }
    
    public WidgetsEditorPanelInputHandler( WidgetsEditorPanel editorPanel, WidgetsDrawingManager widgetsManager )
    {
        this.editorPanel = editorPanel;
        this.widgetsManager = widgetsManager;
        
        Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener()
        {
            @Override
            public void eventDispatched( AWTEvent event )
            {
                if ( event instanceof KeyEvent )
                {
                    handleGlobalKeyEvent( (KeyEvent)event );
                }
                else if ( event instanceof MouseWheelEvent )
                {
                    handleGlobalMouseWheelEvent( (MouseWheelEvent)event );
                }
            }
        }, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK );
    }
}
