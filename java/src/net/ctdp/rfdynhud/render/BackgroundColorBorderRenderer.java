package net.ctdp.rfdynhud.render;

import java.awt.Color;
import java.util.HashMap;

public class BackgroundColorBorderRenderer extends BorderRenderer
{
    private final HashMap<Color, TextureImage2D> cache = new HashMap<Color, TextureImage2D>();
    
    private TextureImage2D createBorder( Color backgroundColor )
    {
        TextureImage2D borderTexture = TextureImage2D.createOfflineTexture( 32, 32, true );
        borderTexture.clear( false, null );
        
        Texture2DCanvas texCanvas = borderTexture.getTextureCanvas();
        
        texCanvas.setColor( backgroundColor );
        texCanvas.setAntialiazingEnabled( true );
        texCanvas.fillArc( 0, 0, 32, 32, 0, 360 );
        
        return ( borderTexture );
    }
    
    public TextureImage2D getImage( Color backgroundColor )
    {
        TextureImage2D borderTexture = cache.get( backgroundColor );
        if ( borderTexture == null )
        {
            borderTexture = createBorder( backgroundColor );
            cache.put( backgroundColor, borderTexture );
        }
        
        return ( borderTexture );
    }
    
    @Override
    public void drawBorder( Color backgroundColor, BorderMeasures measures, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( ( backgroundColor == null ) || ( backgroundColor.getAlpha() == 255 ) )
            return;
        
        TextureImage2D borderTexture = getImage( backgroundColor );
        
        ImageBorderRenderer.drawBorderFromTexture( borderTexture, measures, texture, offsetX, offsetY, width, height );
    }
}
