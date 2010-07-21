package net.ctdp.rfdynhud.editor;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.openmali.types.twodee.Rect2i;

public class WidgetMenuItem extends JMenuItem
{
    private static final long serialVersionUID = -94966687191731871L;
    
    static final int ICON_WIDTH = 64;
    static final int ICON_HEIGHT = 50;
    static final float ICON_ASPECT = (float)ICON_WIDTH / (float)ICON_HEIGHT;
    private static final WidgetsDrawingManager widgetsConfig1 = new WidgetsDrawingManager( 1920, 1200 );
    private static final WidgetsDrawingManager widgetsConfig2 = new WidgetsDrawingManager( ICON_WIDTH, ICON_HEIGHT );
    
    private final RFDynHUDEditor editor;
    //private final Class<Widget> widgetClass;
    private final Widget widget;
    
    private TextureImage2D texture;
    private boolean iconDrawn = false;
    private Boolean lastCheckState = null;
    
    private static BufferedImage loadCheckImage()
    {
        try
        {
            return ( ImageIO.read( WidgetMenuItem.class.getClassLoader().getResource( "data/widget_menu_check.png" ) ) );
        }
        catch ( IOException e )
        {
            Logger.log( e );
            
            return ( null );
        }
    }
    
    private static final BufferedImage CHECK_IMAGE = loadCheckImage();
    
    @Override
    protected void paintComponent( Graphics g )
    {
        Boolean checkState = Boolean.valueOf( this.isSelected() );
        
        if ( !iconDrawn || ( checkState != lastCheckState ) )
        {
            widget.updateVisibility( true, true, editor.getGameData(), editor.getEditorPresets() );
            
            texture.clear( true, null );
            widget.drawWidget( true, true, true, editor.getGameData(), editor.getEditorPresets(), texture );
            
            if ( checkState )
            {
                texture.getTextureCanvas().setClip( (Rect2i)null );
                
                //texture.copyImageDataFrom( CHECK_IMAGE, 0, 0, CHECK_IMAGE.getWidth(), CHECK_IMAGE.getHeight(), 2, texture.getHeight() - 2 - CHECK_IMAGE.getHeight(), CHECK_IMAGE.getWidth(), CHECK_IMAGE.getHeight(), false, false, null );
                texture.getTextureCanvas().drawImage( CHECK_IMAGE, 2, texture.getHeight() - 2 - CHECK_IMAGE.getHeight() );
            }
            
            iconDrawn = true;
            lastCheckState = checkState;
        }
        
        super.paintComponent( g );
    }
    
    public WidgetMenuItem( RFDynHUDEditor editor, Class<Widget> widgetClass )
    {
        super( widgetClass.getSimpleName() );
        
        this.editor = editor;
        
        //this.widgetClass = widgetClass;
        try
        {
            this.widget = RFDynHUDEditor.createWidgetInstance( widgetClass, null );
        }
        catch ( Throwable t )
        {
            throw new RuntimeException( t );
        }
        
        widgetsConfig1.addWidget( widget );
        
        float aspect = (float)widget.getSize().getEffectiveWidth() / (float)widget.getSize().getEffectiveHeight();
        
        widgetsConfig1.removeWidget( widget );
        widgetsConfig2.addWidget( widget );
        
        if ( aspect > ICON_ASPECT )
            widget.getSize().setEffectiveSize( ICON_WIDTH, (int)( ICON_WIDTH / aspect ) );
        else
            widget.getSize().setEffectiveSize( (int)( ICON_HEIGHT * aspect ), ICON_HEIGHT );
        
        widget.getPosition().setEffectivePosition( RelativePositioning.TOP_LEFT, 0, ( ICON_HEIGHT - widget.getEffectiveHeight() ) / 2 );
        
        this.texture = TextureImage2D.createOfflineTexture( ICON_WIDTH, ICON_HEIGHT, true );
        
        this.setIcon( new ImageIcon( texture.getBufferedImage() ) );
    }
}
