package net.ctdp.rfdynhud.widgets.image;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link ImageWidget} displays an image.
 * 
 * @author Marvin Froehlich
 */
public class ImageWidget extends Widget
{
    private TextureImage2D image = null;
    private final ImageProperty imageProp = new ImageProperty( this, "imageName", "ctdp-fat-1994.png" )
    {
        @Override
        public void setValue( Object value )
        {
            super.setValue( value );
            
            image = null;
        }
    };
    
    @Override
    public String getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( ( editorPresets != null ) || ( image == null ) )
        {
            try
            {
                ImageTemplate it = imageProp.getImage();
                if ( ( image == null ) || ( it.getBaseWidth() != width ) || ( it.getBaseHeight() != height ) )
                {
                    image = it.getScaledTextureImage( width, height );
                }
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        texture.clear( image, offsetX, offsetY, width, height, true, null );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( imageProp, "The displayed image's name." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( imageProp.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( imageProp );
    }
    
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    public ImageWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.17f, Size.PERCENT_OFFSET + 0.086f );
        
        getBackgroundColorProperty().setColor( (String)null );
    }
}
