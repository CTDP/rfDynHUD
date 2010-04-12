package net.ctdp.rfdynhud.etv2010.widgets.standings;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.FloatValue;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.StringValue;
import net.ctdp.rfdynhud.widgets._util.TimingUtil;
import net.ctdp.rfdynhud.widgets._util.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link ETVStandingsWidget} displays the list of drivers and gaps.
 * 
 * @author Marvin Froehlich
 */
public class ETVStandingsWidget extends Widget
{
    private static final int MAX_NUM_DRIVERS = 22;
    
    private final ColorProperty captionBackgroundColor = new ColorProperty( this, "captionBackgroundColor", ETVUtils.TV_STYLE_CAPTION_BACKGROUND_COLOR );
    private final ColorProperty captionBackgroundColor1st = new ColorProperty( this, "captionBackgroundColor1st", "#FF0000" );
    private final ColorProperty captionColor = new ColorProperty( this, "captionColor", ETVUtils.TV_STYLE_CAPTION_FONT_COLOR );
    
    private DrawnString[] captionStrings = null;
    private DrawnString[] nameStrings = null;
    private DrawnString[] gapStrings = null;
    
    private TransformableTexture[] textures = null;
    
    private StringValue[] driverNames = null;
    private FloatValue[] gaps = null;
    
    @Override
    public String getWidgetPackage()
    {
        return ( ETVUtils.WIDGET_PACKAGE );
    }
    
    @Override
    public void onSessionStarted( boolean isEditorMode, SessionType sessionType, LiveGameData gameData )
    {
        super.onSessionStarted( isEditorMode, sessionType, gameData );
        
        if ( driverNames != null )
        {
            for ( int i = 0; i < driverNames.length; i++ )
            {
                driverNames[i].reset();
                gaps[i].reset();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransformableTexture[] getSubTexturesImpl( boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
    {
        if ( ( textures != null ) && ( textures[0].getWidth() == widgetInnerWidth ) && ( textures[0].getHeight() == widgetInnerHeight ) )
            return ( textures );
        
        textures = new TransformableTexture[ MAX_NUM_DRIVERS ];
        
        for ( int i = 0; i < MAX_NUM_DRIVERS; i++ )
        {
            textures[i] = new TransformableTexture( widgetInnerWidth, widgetInnerHeight );
            textures[i].setVisible( false );
        }
        
        return ( textures );
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
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        texCanvas.setFont( getFont() );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D numBounds = metrics.getStringBounds( "00", texCanvas );
        
        int capWidth = (int)Math.ceil( numBounds.getWidth() );
        int dataAreaLeft = ETVUtils.getLabeledDataDataLeft( width, numBounds );
        int dataAreaRight = ETVUtils.getLabeledDataDataRight( width );
        int vMiddle = ETVUtils.getLabeledDataVMiddle( height, numBounds );
        
        TransformableTexture[] tt = getSubTextures( ( editorPresets != null ), width, height );
        
        captionStrings = new DrawnString[ tt.length ];
        nameStrings = new DrawnString[ tt.length ];
        gapStrings = new DrawnString[ tt.length ];
        
        driverNames = new StringValue[ tt.length ];
        gaps = new FloatValue[ tt.length ];
        
        for ( int i = 0; i < tt.length; i++ )
        {
            textures[i].setVisible( false );
            
            captionStrings[i] = new DrawnString( ETVUtils.TRIANGLE_WIDTH + capWidth, vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
            nameStrings[i] = new DrawnString( dataAreaLeft, vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
            gapStrings[i] = new DrawnString( dataAreaRight, vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
            
            driverNames[i] = new StringValue();
            gaps[i] = new FloatValue();
        }
    }
    
    @Override
    protected void clearBackground( boolean isEditorMode, LiveGameData gameData, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        texture.clear( offsetX, offsetY, width, height, true, null );
        
        TransformableTexture[] tt = getSubTextures( isEditorMode, width, height );
        
        for ( int i = 0; i < tt.length; i++ )
        {
            Color bgColor = ( i == 0 ) ? captionBackgroundColor1st.getColor() : captionBackgroundColor.getColor();
            
            ETVUtils.drawLabeledDataBackground( 0, 0, tt[i].getWidth(), tt[i].getHeight(), "00", getFont(), bgColor, getBackgroundColor(), tt[i].getTexture(), true );
        }
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        TransformableTexture[] tt = textures;
        
        if ( needsCompleteRedraw )
        {
            for ( int i = 0; i < tt.length; i++ )
            {
                Color bgColor = ( i == 0 ) ? captionBackgroundColor1st.getColor() : captionBackgroundColor.getColor();
                
                captionStrings[i].draw( 0, 0, String.valueOf( i + 1 ), bgColor, tt[i].getTexture() );
            }
        }
        
        final int numDrivers = isEditorMode ? 1 : scoringInfo.getNumVehicles();
        
        for ( int i = 0; i < numDrivers; i++ )
        {
            tt[i].setTranslation( 0, 0 + ( height + 3 ) * i );
            
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            driverNames[i].update( vsi.getDriverNameTLC() );
            
            if ( needsCompleteRedraw || driverNames[i].hasChanged() )
            {
                nameStrings[i].draw( 0, 0, driverNames[i].getValue(), getBackgroundColor(), getFontColor(), tt[i].getTexture() );
            }
            
            if ( isEditorMode || ( i > 0 ) )
            {
                if ( scoringInfo.getSessionType() == SessionType.RACE )
                    gaps[i].update( ( vsi.getLapsBehindLeader() > 0 ) ? -vsi.getLapsBehindLeader() : -vsi.getTimeBehindLeader() );
                else
                    gaps[i].update( vsi.getBestLapTime() - scoringInfo.getVehicleScoringInfo( 0 ).getBestLapTime() );
                
                if ( needsCompleteRedraw || gaps[i].hasChanged() )
                {
                    String s;
                    if ( vsi.getBestLapTime() < 0.0f )
                        s = "";
                    else
                        s = ( gaps[i].getValue() < 0f ) ? "+" + ( (int)-gaps[i].getValue() ) + "Lap(s)" : TimingUtil.getTimeAsGapString( gaps[i].getValue() );
                    
                    gapStrings[i].draw( 0, 0, s, getBackgroundColor(), getFontColor(), tt[i].getTexture() );
                }
            }
            
            tt[i].setVisible( true );
        }
        
        for ( int i = numDrivers; i < tt.length; i++ )
        {
            tt[i].setVisible( false );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( captionBackgroundColor, "The background color for the \"Position\" caption." );
        writer.writeProperty( captionBackgroundColor1st, "The background color for the \"Position\" caption for first place." );
        writer.writeProperty( captionColor, "The font color for the \"Lap\" caption." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( captionBackgroundColor.loadProperty( key, value ) );
        else if ( captionBackgroundColor1st.loadProperty( key, value ) );
        else if ( captionColor.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont )
    {
        super.getProperties( propsCont );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( captionBackgroundColor );
        propsCont.addProperty( captionBackgroundColor1st );
        propsCont.addProperty( captionColor );
    }
    
    /*
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    */
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    public ETVStandingsWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.14f, Size.PERCENT_OFFSET + 0.0254f );
        
        getBackgroundColorProperty().setValue( ETVUtils.TV_STYLE_DATA_BACKGROUND_COLOR );
        getFontColorProperty().setValue( ETVUtils.TV_STYLE_DATA_FONT_COLOR );
        getFontProperty().setValue( ETVUtils.TV_STYLE_FONT );
    }
}
