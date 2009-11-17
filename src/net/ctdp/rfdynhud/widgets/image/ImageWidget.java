package net.ctdp.rfdynhud.widgets.image;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.TextureLoader;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link ImageWidget} displays an image.
 * 
 * @author Marvin Froehlich
 */
public class ImageWidget extends Widget
{
    private String imageName = "ctdp-fat-1994.png";
    
    private TextureImage2D texture = null;
    
    public void setImageName( String imageName )
    {
        this.imageName = imageName;
        
        this.texture = null;
        
        forceCompleteRedraw();
        forceReinitialization();
    }
    
    public final String getImageName()
    {
        return ( imageName );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBoundInputStateChanged( boolean isEditorMode, InputAction action, boolean state, int modifierMask )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean isEditorMode, long sessionNanos, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean isEditorMode, long sessionNanos, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        if ( isEditorMode || ( texture == null ) )
        {
            try
            {
                BufferedImage bi = TextureLoader.getImage( imageName );
                if ( ( texture == null ) || ( bi.getWidth() != width ) || ( bi.getHeight() != height ) )
                {
                    texture = TextureImage2D.createOfflineTexture( width, height, true );
                    texture.clear( false, null );
                    texture.getTextureCanvas().setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
                    texture.getTextureCanvas().drawImage( bi, 0, 0, width, height, 0, 0, bi.getWidth(), bi.getHeight() );
                }
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    @Override
    protected void clearBackground( boolean isEditorMode, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        texCanvas.getImage().clear( texture, offsetX, offsetY, width, height, true, null );
    }
    
    @Override
    protected void drawWidget( boolean isEditorMode, long sessionNanos, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, boolean needsCompleteRedraw )
    {
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( "imageName", getImageName(), "The displayed image's name." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( key.equals( "imageName" ) )
            this.imageName = value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( FlaggedList propsList )
    {
        super.getProperties( propsList );
        
        FlaggedList props = new FlaggedList( "Specific", true );
        
        props.add( new Property( "imageName", PropertyEditorType.IMAGE )
        {
            @Override
            public void setValue( Object value )
            {
                setImageName( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( getImageName() );
            }
        } );
        
        propsList.add( props );
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
        
        setBackgroundColor( (String)null );
    }
}
