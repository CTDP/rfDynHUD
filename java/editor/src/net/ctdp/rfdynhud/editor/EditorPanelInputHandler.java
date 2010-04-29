package net.ctdp.rfdynhud.editor;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.values.RelativePositioning;
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
            editor.onWidgetSelected( selectedWidget );
        
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
    
    public void mouseDragged( MouseEvent e )
    {
        if ( selectedWidget != null )
        {
            selectedWidget.clearRegion( true, editor.getOverlayTexture() );
            
            int dx = ( e.getX() - mousePressedX );
            int dy = ( e.getY() - mousePressedY );
            
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
                
                int gameResX = selectedWidget.getConfiguration().getGameResX();
                int gameResY = selectedWidget.getConfiguration().getGameResY();
                int hundretPercentWidth = gameResY * 4 / 3;
                
                if ( ( selectedWidget.getSize().getWidth() > 0f ) && ( w > (int)( hundretPercentWidth * 0.95f ) ) )
                    selectedWidget.getSize().flipWidthSign();
                else if ( ( selectedWidget.getSize().getWidth() <= 0f ) && ( w < gameResX * 5 / 10 ) )
                    selectedWidget.getSize().flipWidthSign();
                
                if ( ( selectedWidget.getSize().getHeight() > 0f ) && ( h > gameResY * 9 / 10 ) )
                    selectedWidget.getSize().flipHeightSign();
                else if ( ( selectedWidget.getSize().getHeight() <= 0f ) && ( h < gameResY * 5 / 10 ) )
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
                            
                            if ( dw >= dh )
                                h = Math.round( w / aspect );
                            else
                                w = Math.round( h * aspect );
                            break;
                    }
                }
                
                if ( !isControlDown )
                {
                    EditorPanel panel = editor.getEditorPanel();
                    x = panel.snapXToGrid( x );
                    y = panel.snapYToGrid( y );
                    w = panel.snapXToGrid( x + w - 1 ) + 1 - x;
                    h = panel.snapYToGrid( y + h - 1 ) + 1 - y;
                }
                
                boolean b1 = selectedWidget.getSize().setEffectiveSize( w, h );
                boolean b2 = selectedWidget.getPosition().setEffectivePosition( x, y );
                
                changed = b1 || b2;
            }
            else if ( widgetDragStartX >= 0 )
            {
                int x = widgetDragStartX + dx;
                int y = widgetDragStartY + dy;
                
                int effWidth = selectedWidget.getSize().getEffectiveWidth();
                int effHeight = selectedWidget.getSize().getEffectiveHeight();
                
                x = Math.min( Math.max( 0, x ) + effWidth, editor.getGameResX() ) - effWidth;
                y = Math.min( Math.max( 0, y ) + effHeight, editor.getGameResY() ) - effHeight;
                
                
                RelativePositioning positioning = selectedWidget.getPosition().getPositioning();
                
                if ( positioning.isLeft() )
                {
                    int gameResX = selectedWidget.getConfiguration().getGameResX();
                    if ( x + effWidth / 2 >= gameResX / 2 + 50 )
                        positioning = positioning.deriveRight();
                }
                else if ( positioning.isRight() )
                {
                    int gameResX = selectedWidget.getConfiguration().getGameResX();
                    if ( x + effWidth / 2 < gameResX / 2 - 50 )
                        positioning = positioning.deriveLeft();
                }
                
                if ( positioning.isTop() )
                {
                    int gameResY = selectedWidget.getConfiguration().getGameResY();
                    if ( y + effHeight / 2 >= gameResY * 8 / 10 )
                        positioning = positioning.deriveBottom();
                }
                else if ( positioning.isBottom() )
                {
                    int gameResY = selectedWidget.getConfiguration().getGameResY();
                    if ( y + effHeight / 2 < gameResY * 8 / 10 )
                        positioning = positioning.deriveTop();
                }
                
                if ( !isControlDown )
                {
                    EditorPanel panel = editor.getEditorPanel();
                    x = panel.snapXToGrid( x );
                    y = panel.snapYToGrid( y );
                }
                
                changed = selectedWidget.getPosition().setEffectivePosition( positioning, x, y );
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
                editor.onWidgetSelected( null );
                break;
        }
    }
    
    public void keyReleased( KeyEvent e )
    {
    }
    
    public void keyTyped( KeyEvent e )
    {
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
                KeyEvent kev = (KeyEvent)event;
                
                isShiftDown = kev.isShiftDown();
                isControlDown = kev.isControlDown();
            }
        }, AWTEvent.KEY_EVENT_MASK );
    }
}
