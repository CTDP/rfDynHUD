package net.ctdp.rfdynhud.widgets.trackposition;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link TrackPositionWidget} displays all driving vehicles on a line.
 * 
 * @author Marvin Froehlich
 */
public class TrackPositionWidget extends Widget
{
    private static final int LINE_THICKNESS = 1;
    private static final int LINE_PADDING = 30;
    
    private int baseItemRadius = 7;
    private int itemRadius = baseItemRadius;
    
    private final ColorProperty markColorNormal = new ColorProperty( this, "markColorNormal", "#FFFFFFC0" );
    private final ColorProperty markColorLeader = new ColorProperty( this, "markColorLeader", "#FF0000C0" );
    private final ColorProperty markColorMe = new ColorProperty( this, "markColorMe", "#00FF00C0" );
    private final ColorProperty markColorNextInFront = new ColorProperty( this, "markColorNextInFront", "#0000FFC0" );
    private final ColorProperty markColorNextBehind = new ColorProperty( this, "markColorNextBehind", "#FFFF00C0" );
    
    private final IntProperty maxDisplayedVehicles = new IntProperty( this, "maxDisplayedVehicles", "maxDisplayedVehicles", 22, 1, 50, false );
    
    private final BooleanProperty displayPositionNumbers = new BooleanProperty( this, "displayPosNumbers", true );
    
    private final FontProperty posNumberFont = new FontProperty( this, "posNumberFont", "Monospaced-PLAIN-9va" );
    private final ColorProperty posNumberFontColor = new ColorProperty( this, "posNumberFontColor", "#000000" );
    
    private static final int ANTI_ALIAS_RADIUS_OFFSET = 1;
    
    private TransformableTexture[] itemTextures = null;
    private int[] itemStates = null;
    
    private int lineLength = 0;
    
    @Override
    public String getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    public void setItemRadius( int radius )
    {
        this.baseItemRadius = radius;
        
        forceAndSetDirty();
    }
    
    private void initSubTextures( boolean isEditorMode )
    {
        final int maxDspVehicles = this.maxDisplayedVehicles.getIntValue();
        
        itemRadius = Math.round( baseItemRadius * getConfiguration().getGameResY() / 960f );
        
        if ( ( itemTextures != null ) && ( itemTextures.length == maxDspVehicles ) && ( itemTextures[0].getWidth() == itemRadius + itemRadius ) && ( itemTextures[0].getHeight() == itemRadius + itemRadius ) )
            return;
        
        itemTextures = new TransformableTexture[ maxDspVehicles ];
        itemStates = new int[ maxDspVehicles ];
        
        for ( int i = 0; i < maxDspVehicles; i++ )
        {
            itemTextures[i] = new TransformableTexture( itemRadius + itemRadius, itemRadius + itemRadius, isEditorMode );
            itemTextures[i].setVisible( false );
            
            itemStates[i] = -1;
        }
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        initSubTextures( editorPresets != null );
        
        return ( itemTextures );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        initSubTextures( isEditorMode );
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        super.clearBackground( gameData, editorPresets, texture, offsetX, offsetY, width, height );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        texCanvas.setAntialiazingEnabled( true );
        
        int lineHeight = LINE_THICKNESS;
        lineLength = width - 2 * LINE_PADDING;
        
        texCanvas.fillRect( offsetX + LINE_PADDING, offsetY + ( height - lineHeight ) / 2, lineLength, lineHeight );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        int off2 = ( posNumberFont.isAntiAliased() ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
        
        short ownPlace = scoringInfo.getOwnPlace();
        
        final Font font = posNumberFont.getFont();
        final boolean posNumberFontAntiAliased = posNumberFont.isAntiAliased();
        FontMetrics metrics = texture.getTextureCanvas().getFontMetrics( font );
        
        boolean normal = false;
        int n = Math.min( scoringInfo.getNumVehicles(), maxDisplayedVehicles.getIntValue() );
        for ( int i = 0; i < n; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            //if ( !vsi.isInPits() )
            {
                float lapDistance = ( ( vsi.getLapDistance() + vsi.getScalarVelocity() * scoringInfo.getExtrapolationTime() ) % scoringInfo.getTrackLength() ) / scoringInfo.getTrackLength();
                if ( lapDistance < 0f )
                    lapDistance = 0f;
                
                TransformableTexture tt = itemTextures[i];
                itemTextures[i].setVisible( true );
                int itemState = vsi.getPlace();
                
                if ( vsi.getPlace() == 1 )
                {
                    itemState |= 1 << 16;
                    tt.getTextureCanvas().setColor( markColorLeader.getColor() );
                    normal = false;
                }
                else if ( vsi.isPlayer() )
                {
                    itemState |= 2 << 16;
                    tt.getTextureCanvas().setColor( markColorMe.getColor() );
                    normal = false;
                }
                else if ( vsi.getPlace() == ownPlace + 1 )
                {
                    itemState |= 3 << 16;
                    tt.getTextureCanvas().setColor( markColorNextInFront.getColor() );
                    normal = false;
                }
                else if ( vsi.getPlace() == ownPlace - 1 )
                {
                    itemState |= 4 << 16;
                    tt.getTextureCanvas().setColor( markColorNextBehind.getColor() );
                    normal = false;
                }
                else if ( !normal )
                {
                    itemState |= 5 << 16;
                    tt.getTextureCanvas().setColor( markColorNormal.getColor() );
                    normal = true;
                }
                
                tt.setTranslation( LINE_PADDING + off2 + lapDistance * lineLength - itemRadius, off2 + height / 2 - itemRadius );
                
                if ( itemStates[i] != itemState )
                {
                    itemStates[i] = itemState;
                    
                    tt.getTexture().clear( true, null );
                    
                    tt.getTextureCanvas().setAntialiazingEnabled( true );
                    tt.getTextureCanvas().fillArc( 0, 0, itemRadius + itemRadius, itemRadius + itemRadius, 0, 360 );
                    
                    if ( displayPositionNumbers.getBooleanValue() )
                    {
                        String posStr = String.valueOf( vsi.getPlace() );
                        Rectangle2D bounds = metrics.getStringBounds( posStr, tt.getTextureCanvas() );
                        float fw = (float)bounds.getWidth();
                        float fh = (float)( metrics.getAscent() - metrics.getDescent() );
                        
                        tt.getTexture().drawString( posStr, itemRadius - (int)( fw / 2 ), itemRadius + (int)( fh / 2 ), bounds, font, posNumberFontAntiAliased, posNumberFontColor.getColor(), false, null );
                    }
                }
            }
        }
        
        for ( int i = n; i < maxDisplayedVehicles.getIntValue(); i++ )
            itemTextures[i].setVisible( false );
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( "itemRadius", baseItemRadius, "The abstract radius for any displayed driver item." );
        writer.writeProperty( markColorNormal, "The color used for all, but special cars in #RRGGBBAA (hex)." );
        writer.writeProperty( markColorLeader, "The color used for the leader's car in #RRGGBBAA (hex)." );
        writer.writeProperty( markColorMe, "The color used for your own car in #RRGGBBAA (hex)." );
        writer.writeProperty( markColorNextInFront, "The color used for the car in front of you in #RRGGBBAA (hex)." );
        writer.writeProperty( markColorNextBehind, "The color used for the car behind you in #RRGGBBAA (hex)." );
        writer.writeProperty( maxDisplayedVehicles, "The maximum number of displayed vehicles." );
        writer.writeProperty( displayPositionNumbers, "Display numbers on the position markers?" );
        writer.writeProperty( posNumberFont, "The font used for position numbers." );
        writer.writeProperty( posNumberFontColor, "The font color used for position numbers in the format #RRGGBB (hex)." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( key.equals( "itemRadius" ) )
            this.baseItemRadius = Integer.parseInt( value );
        
        else if ( markColorNormal.loadProperty( key, value ) );
        else if ( markColorLeader.loadProperty( key, value ) );
        else if ( markColorMe.loadProperty( key, value ) );
        else if ( markColorNextInFront.loadProperty( key, value ) );
        else if ( markColorNextBehind.loadProperty( key, value ) );
        else if ( maxDisplayedVehicles.loadProperty( key, value ) );
        else if ( displayPositionNumbers.loadProperty( key, value ) );
        else if ( posNumberFont.loadProperty( key, value ) );
        else if ( posNumberFontColor.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( new Property( this, "itemRadius", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setItemRadius( (Integer)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( baseItemRadius );
            }
        } );
        
        propsCont.addProperty( markColorNormal );
        propsCont.addProperty( markColorLeader );
        propsCont.addProperty( markColorMe );
        propsCont.addProperty( markColorNextInFront );
        propsCont.addProperty( markColorNextBehind );
        
        propsCont.addProperty( maxDisplayedVehicles );
        
        propsCont.addProperty( displayPositionNumbers );
        propsCont.addProperty( posNumberFont );
        propsCont.addProperty( posNumberFontColor );
    }
    
    public TrackPositionWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.35f, Size.PERCENT_OFFSET + 0.05859375f );
    }
}
