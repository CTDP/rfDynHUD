package net.ctdp.rfdynhud.widgets.startinglight;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link StartingLightWidget} displays a starting light for the race.
 * 
 * @author Marvin Froehlich
 */
public class StartingLightWidget extends Widget
{
    private static final int MAX_LIGHTS = 5;
    
    private TextureImage2D offImage = null;
    private final ImageProperty offImageProp = new ImageProperty( this, "offImageName", "starting_light_off.png" )
    {
        @Override
        public void setValue( Object value )
        {
            super.setValue( value );
            
            offImage = null;
            onImage = null;
        }
    };
    
    private TextureImage2D onImage = null;
    private final ImageProperty onImageProp = new ImageProperty( this, "onImageName", "starting_light_on.png" )
    {
        @Override
        public void setValue( Object value )
        {
            super.setValue( value );
            
            offImage = null;
            onImage = null;
        }
    };
    
    private final IntProperty numRows = new IntProperty( this, "numRows", 2, 1, 4 );
    
    private final FloatProperty visibleTimeAfterLightsOff = new FloatProperty( this, "visibleTimeAfterLightsOff", 4.0f, 0f, 60f );
    
    private final EnumValue<GamePhase> gamePhase = new EnumValue<GamePhase>();
    private final IntValue numLights = new IntValue();
    private float visibleTime = -1f;
    
    @Override
    public String getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        gamePhase.reset();
        numLights.reset();
        visibleTime = -1f;
        
        setUserVisible2( false );
    }
    
    @Override
    public void updateVisibility( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.updateVisibility( clock1, clock2, gameData, editorPresets );
        
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( !scoringInfo.getSessionType().isRace() )
        {
            setUserVisible2( false );
            return;
        }
        
        boolean visible = true;
        
        gamePhase.update( scoringInfo.getGamePhase() );
        float sessionTime = scoringInfo.getSessionTime();
        
        if ( gamePhase.hasChanged( false ) )
        {
            if ( gamePhase.getValue() == GamePhase.STARTING_LIGHT_COUNTDOWN_HAS_BEGUN )
            {
                visible = true;
            }
            else
            {
                if ( gamePhase.getOldValue() == GamePhase.STARTING_LIGHT_COUNTDOWN_HAS_BEGUN )
                    visibleTime = sessionTime + visibleTimeAfterLightsOff.getFloatValue();
                
                visible = ( sessionTime <= visibleTime );
            }
        }
        else if ( gamePhase.getValue() != GamePhase.STARTING_LIGHT_COUNTDOWN_HAS_BEGUN )
        {
            visible = ( sessionTime <= visibleTime );
        }
        
        gamePhase.setUnchanged();
        if ( sessionTime > visibleTime )
            visibleTime = -1f;
        
        setUserVisible2( visible );
    }
    
    private void loadImages( int innerHeight )
    {
        final int imageHeight = innerHeight / numRows.getIntValue();
        
        if ( ( offImage == null ) || ( offImage.getHeight() != imageHeight ) )
        {
            try
            {
                ImageTemplate it = offImageProp.getImage();
                
                float scale = (float)imageHeight / (float)it.getBaseHeight();
                int imageWidth = (int)( it.getBaseWidth() * scale );
                
                offImage = it.getScaledTextureImage( imageWidth, imageHeight );
                onImage = onImageProp.getImage().getScaledTextureImage( imageWidth, imageHeight );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        loadImages( height );
    }
    
    @Override
    public int getMaxWidth( LiveGameData gameData, TextureImage2D texture )
    {
        loadImages( getEffectiveInnerHeight() );
        
        return ( offImage.getWidth() * MAX_LIGHTS + getEffectiveWidth() - getEffectiveInnerWidth() );
    }
    
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( ( editorPresets == null ) && ( offImage != null ) )
        {
            int newWidth = offImage.getWidth() * Math.min( gameData.getScoringInfo().getNumRedLights(), MAX_LIGHTS );
            
            if ( newWidth != width )
            {
                getSize().setEffectiveSize( getEffectiveWidth() - getEffectiveInnerWidth() + newWidth, getEffectiveHeight() );
                
                return ( true );
            }
        }
        
        return ( false );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        int m = scoringInfo.getNumRedLights();
        int numIgnoredLights = ( m <= MAX_LIGHTS ) ? 0 : m - MAX_LIGHTS;
        m = Math.min( m, MAX_LIGHTS );
        
        numLights.update( scoringInfo.getStartLightFrame() - numIgnoredLights );
        
        if ( needsCompleteRedraw || numLights.hasChanged() )
        {
            int n = numLights.getValue();
            
            if ( editorPresets != null )
                n = m / 2;
            
            if ( n > m )
                n = 0;
            
            int offX2 = 0;
            TextureImage2D img;
            
            for ( int i = 0; i < m; i++ )
            {
                if ( i < n )
                    img = onImage;
                else
                    img = offImage;
                
                texture.clear( getBackgroundColor(), offsetX + offX2, offsetY, img.getWidth(), height, true, null );
                
                for ( int j = 0; j < numRows.getIntValue(); j++ )
                    texture.drawImage( img, offsetX + offX2, offsetY + j * img.getHeight(), false, null );
                
                offX2 += img.getWidth();
            }
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( offImageProp, "The image name for the off-lights." );
        writer.writeProperty( onImageProp, "The image name for the on-lights." );
        writer.writeProperty( numRows, "The number of light rows." );
        writer.writeProperty( visibleTimeAfterLightsOff, "Amount of seconds, the Widget stays visible after all lights have gone off." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( offImageProp.loadProperty( key, value ) );
        else if ( onImageProp.loadProperty( key, value ) );
        else if ( numRows.loadProperty( key, value ) );
        else if ( visibleTimeAfterLightsOff.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( offImageProp );
        propsCont.addProperty( onImageProp );
        propsCont.addProperty( numRows );
        propsCont.addProperty( visibleTimeAfterLightsOff );
    }
    
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    public StartingLightWidget( String name )
    {
        super( name, 11.328125f, true, 8.3984375f, true );
    }
}
