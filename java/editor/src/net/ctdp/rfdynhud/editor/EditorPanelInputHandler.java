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
 * @author Marvin Froehlich
 */
public class EditorPanelInputHandler implements MouseListener, MouseMotionListener, KeyListener
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
    
    private final RFDynHUDEditor editor;
    
    private WidgetsDrawingManager widgetsManager;
    
    private BorderPart overBorderPart = null;
    
    private int mousePressedX = -1;
    private int mousePressedY = -1;
    private int widgetDragStartX = -1;
    private int widgetDragStartY = -1;
    private int widgetDragStartWidth = -1;
    private int widgetDragStartHeight = -1;
    private Widget selectedWidget = null;
    
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
    
    public void mousePressed( MouseEvent e )
    {
        // I have no idea, why this is necessary.
        editor.getEditorPanel().requestFocus();
        
        int x = e.getX();
        int y = e.getY();
        
        selectedWidget = getWidgetUnderMouse( x, y );
        //boolean widgetChanged = ( selectedWidget != editor.getEditorPanel().getSelectedWidget() );
        
        //if ( widgetChanged )
            editor.onWidgetSelected( selectedWidget, false );
        
        if ( e.getButton() == MouseEvent.BUTTON1 )
        {
            if ( selectedWidget != null )
            {
                mousePressedX = x;
                mousePressedY = y;
                
                widgetDragStartX = selectedWidget.getPosition().getEffectiveX();
                widgetDragStartY = selectedWidget.getPosition().getEffectiveY();
                
                if ( editor.getEditorPanel().getCursor().getType() == Cursor.DEFAULT_CURSOR )
                {
                    editor.getEditorPanel().setCursor( Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR ) );
                }
                else
                {
                    widgetDragStartWidth = selectedWidget.getSize().getEffectiveWidth();
                    widgetDragStartHeight = selectedWidget.getSize().getEffectiveHeight();
                }
            }
        }
        else if ( e.getButton() == MouseEvent.BUTTON3 )
        {
            editor.initContextMenu();
        }
    }
    
    public void mouseReleased( MouseEvent e )
    {
        if ( e.getButton() == MouseEvent.BUTTON1 )
        {
            if ( selectedWidget != null )
            {
                selectedWidget = null;
                mousePressedX = -1;
                mousePressedY = -1;
                widgetDragStartX = -1;
                widgetDragStartY = -1;
                widgetDragStartWidth = -1;
                widgetDragStartHeight = -1;
                
                mouseMoved( e );
                
                widgetsManager.setAllDirtyFlags();
                
                editor.getEditorPanel().repaint();
            }
        }
    }
    
    public void mouseClicked( MouseEvent e )
    {
        if ( ( e.getButton() == MouseEvent.BUTTON1 ) && ( editor.getEditorPanel().getSelectedWidget() != null ) )
        {
            editor.onWidgetSelected( editor.getEditorPanel().getSelectedWidget(), e.getClickCount() == 2 );
        }
    }
    
    public void mouseEntered( MouseEvent e )
    {
    }
    
    public void mouseExited( MouseEvent e )
    {
    }
    
    public void mouseMoved( MouseEvent e )
    {
        int x = e.getX();
        int y = e.getY();
        
        Widget widget = getWidgetUnderMouse( x, y );
        
        if ( ( widget != null ) && !widget.hasFixedSize() )
        {
            int effX = widget.getPosition().getEffectiveX();
            int effY = widget.getPosition().getEffectiveY();
            int effW = widget.getSize().getEffectiveWidth();
            int effH = widget.getSize().getEffectiveHeight();
            
            Cursor cursor = editor.getEditorPanel().getCursor();
            
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
            
            if ( cursor != editor.getEditorPanel().getCursor() )
            {
                editor.getEditorPanel().setCursor( cursor );
            }
        }
        else
        {
            overBorderPart = null;
            editor.getEditorPanel().setCursor( Cursor.getDefaultCursor() );
        }
    }
    
    private void snapPositionToRail( Widget widget, Point point )
    {
        if ( !isControlDown )
        {
            EditorPanel panel = editor.getEditorPanel();
            
            if ( ( panel.getRailDistanceX() > 0 ) || ( panel.getRailDistanceY() > 0 ) )
            {
                WidgetsConfiguration wc = panel.getWidgetsDrawingManager();
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
                
                if ( Math.abs( closestRailX ) < panel.getRailDistanceX() )
                    point.x += closestRailX;
                
                if ( Math.abs( closestRailY ) < panel.getRailDistanceY() )
                    point.y += closestRailY;
            }
            
            point.x = panel.snapXToGrid( point.x );
            point.y = panel.snapYToGrid( point.y );
        }
    }
    
    private RelativePositioning fixPositioning( Widget widget, int x, int y, int w, int h )
    {
        final int gameResX = widget.getConfiguration().getGameResolution().getViewportWidth();
        final int gameResY = widget.getConfiguration().getGameResolution().getViewportHeight();
        
        RelativePositioning positioning = selectedWidget.getPosition().getPositioning();
        
        if ( positioning.isLeft() )
        {
            if ( x / (float)( gameResX - x - w ) > 0.4f )
                positioning = positioning.deriveHCenter();
            else if ( x + w / 2 >= gameResX / 2 + 50 )
                positioning = positioning.deriveRight();
        }
        else if ( positioning.isRight() )
        {
            if ( x / (float)( gameResX - x - w ) < 2.5f )
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
            if ( y / (float)( gameResY - y - h ) < 2.5f )
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
        
        snapPositionToRail( widget, p );
        
        x = p.x;
        y = p.y;
        
        final int effWidth = widget.getSize().getEffectiveWidth();
        final int effHeight = widget.getSize().getEffectiveHeight();
        
        RelativePositioning positioning = fixPositioning( widget, x, y, effWidth, effHeight );
        
        return ( widget.getPosition().setEffectivePosition( positioning, x, y ) );
    }
    
    public void mouseDragged( MouseEvent e )
    {
        if ( selectedWidget != null )
        {
            selectedWidget.clearRegion( true, editor.getOverlayTexture() );
            
            final int gameResX = selectedWidget.getConfiguration().getGameResolution().getViewportWidth();
            final int gameResY = selectedWidget.getConfiguration().getGameResolution().getViewportHeight();
            
            final int dx = ( e.getX() - mousePressedX );
            final int dy = ( e.getY() - mousePressedY );
            
            boolean changed = false;
            
            if ( widgetDragStartWidth >= 0 )
            {
                int x = widgetDragStartX;
                int y = widgetDragStartY;
                
                int w = widgetDragStartWidth;
                int h = widgetDragStartHeight;
                
                switch ( editor.getEditorPanel().getCursor().getType() )
                {
                    case Cursor.NW_RESIZE_CURSOR:
                        x += dx;
                        y += dy;
                        w -= dx;
                        h -= dy;
                        break;
                    case Cursor.N_RESIZE_CURSOR:
                        y += dy;
                        h -= dy;
                        break;
                    case Cursor.NE_RESIZE_CURSOR:
                        y += dy;
                        w += dx;
                        h -= dy;
                        break;
                    case Cursor.W_RESIZE_CURSOR:
                        x += dx;
                        w -= dx;
                        break;
                    case Cursor.E_RESIZE_CURSOR:
                        w += dx;
                        break;
                    case Cursor.SW_RESIZE_CURSOR:
                        x += dx;
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
                
                if ( !selectedWidget.getSize().isNegativeWidth() && ( w > (int)( hundretPercentWidth * 0.95f ) ) )
                    selectedWidget.getSize().flipWidthSign();
                else if ( selectedWidget.getSize().isNegativeWidth() && ( w < gameResX * 5 / 10 ) )
                    selectedWidget.getSize().flipWidthSign();
                
                if ( !selectedWidget.getSize().isNegativeHeight() && ( h > gameResY * 9 / 10 ) )
                    selectedWidget.getSize().flipHeightSign();
                else if ( selectedWidget.getSize().isNegativeHeight() && ( h < gameResY * 5 / 10 ) )
                    selectedWidget.getSize().flipHeightSign();
                
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
                
                Point p = new Point( x, y );
                Point s = new Point( x + w - 1, y + h - 1 );
                snapPositionToRail( selectedWidget, p );
                x = p.x;
                y = p.y;
                snapPositionToRail( selectedWidget, s );
                w = s.x + 1 - x;
                h = s.y + 1 - y;
                
                RelativePositioning positioning = fixPositioning( selectedWidget, x, y, w, h );
                
                boolean b1 = selectedWidget.getSize().setEffectiveSize( w, h );
                boolean b2 = selectedWidget.getPosition().setEffectivePosition( positioning, x, y );
                
                changed = b1 || b2;
            }
            else if ( widgetDragStartX >= 0 )
            {
                int x = widgetDragStartX + dx;
                int y = widgetDragStartY + dy;
                
                int effWidth = selectedWidget.getSize().getEffectiveWidth();
                int effHeight = selectedWidget.getSize().getEffectiveHeight();
                
                x = Math.min( Math.max( 0, x ) + effWidth, gameResX ) - effWidth;
                y = Math.min( Math.max( 0, y ) + effHeight, gameResY ) - effHeight;
                
                changed = setWidgetPosition( selectedWidget, x, y );
            }
            
            if ( changed )
                editor.onWidgetChanged( selectedWidget, "POSITIONAL" );
        }
    }
    
    public void keyPressed( KeyEvent e )
    {
        switch ( e.getKeyCode() )
        {
            case KeyEvent.VK_DELETE:
                editor.getEditorPanel().removeSelectedWidget();
                editor.onWidgetSelected( null, false );
                break;
        }
    }
    
    public void keyReleased( KeyEvent e )
    {
    }
    
    public void keyTyped( KeyEvent e )
    {
    }
    
    private boolean needsRedraw = false;
    
    private void handleGlobalKeyEvent( KeyEvent kev )
    {
        isShiftDown = kev.isShiftDown();
        isControlDown = kev.isControlDown();
        
        Widget w = editor.getEditorPanel().getSelectedWidget();
        
        if ( ( w != null ) && editor.getEditorPanel().hasFocus() )
        {
            if ( kev.getKeyCode() == KeyEvent.VK_UP )
            {
                if ( kev.getID() == KeyEvent.KEY_PRESSED )
                {
                    w.clearRegion( true, editor.getOverlayTexture() );
                    if ( setWidgetPosition( w, w.getPosition().getEffectiveX(), w.getPosition().getEffectiveY() - 1 ) )
                    {
                        editor.onWidgetChanged( w, "POSITIONAL" );
                        needsRedraw = true;
                    }
                }
                
                kev.consume();
            }
            else if ( kev.getKeyCode() == KeyEvent.VK_LEFT )
            {
                if ( kev.getID() == KeyEvent.KEY_PRESSED )
                {
                    w.clearRegion( true, editor.getOverlayTexture() );
                    if ( setWidgetPosition( w, w.getPosition().getEffectiveX() - 1, w.getPosition().getEffectiveY() ) )
                    {
                        editor.onWidgetChanged( w, "POSITIONAL" );
                        needsRedraw = true;
                    }
                }
                
                kev.consume();
            }
            else if ( kev.getKeyCode() == KeyEvent.VK_RIGHT )
            {
                if ( kev.getID() == KeyEvent.KEY_PRESSED )
                {
                    w.clearRegion( true, editor.getOverlayTexture() );
                    if ( setWidgetPosition( w, w.getPosition().getEffectiveX() + 1, w.getPosition().getEffectiveY() ) )
                    {
                        editor.onWidgetChanged( w, "POSITIONAL" );
                        needsRedraw = true;
                    }
                }
                
                kev.consume();
            }
            else if ( kev.getKeyCode() == KeyEvent.VK_DOWN )
            {
                if ( kev.getID() == KeyEvent.KEY_PRESSED )
                {
                    w.clearRegion( true, editor.getOverlayTexture() );
                    if ( setWidgetPosition( w, w.getPosition().getEffectiveX(), w.getPosition().getEffectiveY() + 1 ) )
                    {
                        editor.onWidgetChanged( w, "POSITIONAL" );
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
        if ( editor.getEditorPanel().hasFocus() )
        {
            if ( needsRedraw )
                widgetsManager.setAllDirtyFlags();
            
            needsRedraw = false;
            
        }
    }
    
    public EditorPanelInputHandler( RFDynHUDEditor editor, WidgetsDrawingManager widgetsManager )
    {
        this.editor = editor;
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
