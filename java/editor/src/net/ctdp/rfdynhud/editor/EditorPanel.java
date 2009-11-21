package net.ctdp.rfdynhud.editor;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import org.openmali.types.twodee.Rect2i;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.render.TextureDirtyRectsManager;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * 
 * @author Marvin Froehlich
 */
public class EditorPanel extends JPanel
{
    private static final long serialVersionUID = -4217992603083635127L;
    
    private final RFDynHUDEditor editor;
    
    private BufferedImage backgroundImage;
    private BufferedImage cacheImage;
    private Graphics2D cacheGraphics;
    
    private LiveGameData gameData;
    
    private TextureImage2D overlay;
    private final WidgetsDrawingManager drawingManager;
    private final ByteBuffer dirtyRectsBuffer = TextureDirtyRectsManager.createByteBuffer( 1024 );
    private final ArrayList<Boolean> dirtyFlags = new ArrayList<Boolean>();
    private final HashMap<Widget, Rect2i> oldWidgetRects = new HashMap<Widget, Rect2i>();
    
    private Widget selectedWidget = null;
    private static final java.awt.Color SELECTION_COLOR = new java.awt.Color( 255, 0, 0, 127 );
    
    public void setBackgroundImage( BufferedImage image )
    {
        this.backgroundImage = image;
        
        if ( ( cacheImage == null ) || ( cacheImage.getWidth() != backgroundImage.getWidth() ) || ( cacheImage.getHeight() != backgroundImage.getHeight() ) )
        {
            cacheImage = new BufferedImage( backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR );
            cacheGraphics = cacheImage.createGraphics();
        }
        
        cacheGraphics.drawImage( backgroundImage, 0, 0, null );
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
    
    public final WidgetsDrawingManager getWidgetsDrawingManager()
    {
        return ( drawingManager );
    }
    
    public void setSelectedWidget( Widget widget )
    {
        if ( widget == this.selectedWidget )
            return;
        
        if ( ( this.selectedWidget != null ) && ( this.selectedWidget.getConfiguration() != null ) )
        {
            //selectedWidget.clearRegion( true, overlay );
        }
        
        this.selectedWidget = widget;
        
        this.repaint();
    }
    
    public final Widget getSelectedWidget()
    {
        return ( selectedWidget );
    }
    
    public void removeSelectedWidget()
    {
        if ( selectedWidget == null )
            return;
        
        selectedWidget.clearRegion( true, overlay );
        drawingManager.removeWidget( selectedWidget );
        setSelectedWidget( null );
        editor.setDirtyFlag();
    }
    
    private void drawSelection( Widget widget, Graphics2D g )
    {
        int offsetX = widget.getPosition().getEffectiveX();
        int offsetY = widget.getPosition().getEffectiveY();
        int width = widget.getSize().getEffectiveWidth();
        int height = widget.getSize().getEffectiveHeight();
        
        //texture.getTextureCanvas().setClip( offsetX, offsetY, width, height );
        
        Stroke oldStroke = g.getStroke();
        g.setStroke( new BasicStroke( 2 ) );
        g.setColor( SELECTION_COLOR );
        g.drawRect( offsetX, offsetY, width, height );
        g.setStroke( oldStroke );
        //texture.clearOutline( SELECTION_COLOR, offsetX, offsetY, width, height, 2, false, null );
    }
    
    public void clearSelectedWidget()
    {
        if ( selectedWidget == null )
            return;
        
        selectedWidget.clearRegion( true, overlay );
    }
    
    private long frameIndex = 0;
    
    @Override
    public void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        
        //System.out.println( "paintComponent()" );
        
        frameIndex++;
        
        try
        {
            int n = drawingManager.getNumWidgets();
            
            //g.drawImage( backgroundImage, 0, 0, backgroundImage.getWidth(), backgroundImage.getHeight(), 0, 0, backgroundImage.getWidth(), backgroundImage.getHeight(), null );
            
            dirtyFlags.clear();
            for ( int i = 0; i < n; i++ )
            {
                dirtyFlags.add( drawingManager.getWidget( i ).getDirtyFlag( false ) );
            }
            for ( int i = 0; i < n; i++ )
            {
                if ( dirtyFlags.get( i ) )
                {
                    Widget widget = drawingManager.getWidget( i );
                    Rect2i r = new Rect2i( widget.getPosition().getEffectiveX(), widget.getPosition().getEffectiveY(), widget.getSize().getEffectiveWidth(), widget.getSize().getEffectiveHeight() );
                    Rect2i oldWidgetRect = oldWidgetRects.get( widget );
                    if ( oldWidgetRect != null )
                    {
                        r.combine( oldWidgetRect );
                    }
                    
                    for ( int j = 0; j < n; j++ )
                    {
                        Widget widget2 = drawingManager.getWidget( j );
                        Rect2i r2 = new Rect2i( widget2.getPosition().getEffectiveX(), widget2.getPosition().getEffectiveY(), widget2.getSize().getEffectiveWidth(), widget2.getSize().getEffectiveHeight() );
                        
                        if ( !dirtyFlags.get( j ) && r.intersects( r2 ) )
                        {
                            dirtyFlags.set( j, true );
                            widget2.setDirtyFlag();
                        }
                    }
                }
            }
            
            drawingManager.drawWidgets( true, gameData, true, overlay );
            //TextureDirtyRectsManager.drawDirtyRects( overlay );
            TextureDirtyRectsManager.getDirtyRects( frameIndex, overlay, dirtyRectsBuffer, true );
            
            short numDirtyRecty = dirtyRectsBuffer.getShort();
            for ( short i = 0; i < numDirtyRecty; i++ )
            {
                int drX = dirtyRectsBuffer.getShort();
                int drY = dirtyRectsBuffer.getShort();
                int drW = dirtyRectsBuffer.getShort();
                int drH = dirtyRectsBuffer.getShort();
                
                cacheGraphics.drawImage( backgroundImage, drX, drY, drX + drW, drY + drH, drX, drY, drX + drW, drY + drH, null );
            }
            
            BufferedImage bi = overlay.getBufferedImage();
            for ( int i = 0; i < n; i++ )
            {
                Widget widget = drawingManager.getWidget( i );
                
                int offsetX = widget.getPosition().getEffectiveX();
                int offsetY = widget.getPosition().getEffectiveY();
                int width = widget.getSize().getEffectiveWidth();
                int height = widget.getSize().getEffectiveHeight();
                
                if ( dirtyFlags.get( i ) )
                {
                    //cacheGraphics.drawImage( backgroundImage, offsetX, offsetY, offsetX + width, offsetY + height, offsetX, offsetY, offsetX + width, offsetY + height, null );
                    cacheGraphics.drawImage( bi, offsetX, offsetY, offsetX + width, offsetY + height, offsetX, offsetY, offsetX + width, offsetY + height, null );
                }
                
                Rect2i oldWidgetRect = oldWidgetRects.get( widget );
                if ( oldWidgetRect == null )
                {
                    oldWidgetRect = new Rect2i();
                    oldWidgetRects.put( widget, oldWidgetRect );
                }
                oldWidgetRect.set( offsetX, offsetY, width, height );
            }
            
            g.drawImage( cacheImage, 0, 0, cacheImage.getWidth(), cacheImage.getHeight(), 0, 0, cacheImage.getWidth(), cacheImage.getHeight(), null );
            
            if ( selectedWidget != null )
            {
                drawSelection( selectedWidget, (Graphics2D)g );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    public EditorPanel( RFDynHUDEditor editor, BufferedImage backgroundImage, LiveGameData gameData, TextureImage2D overlay, WidgetsDrawingManager drawingManager )
    {
        this.editor = editor;
        
        setBackgroundImage( backgroundImage );
        
        this.gameData = gameData;
        
        this.overlay = overlay;
        this.drawingManager = drawingManager;
        
        EditorPanelInputHandler inputHandler = new EditorPanelInputHandler( editor, drawingManager );
        this.addMouseListener( inputHandler );
        this.addMouseMotionListener( inputHandler );
        this.addKeyListener( inputHandler );
        this.setFocusable( true );
    }
}
