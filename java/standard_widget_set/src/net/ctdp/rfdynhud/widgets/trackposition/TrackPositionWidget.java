package net.ctdp.rfdynhud.widgets.trackposition;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
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
    
    private int baseItemRadius = 9;
    private int itemRadius = baseItemRadius;
    
    private final ColorProperty markColorNormal = new ColorProperty( this, "markColorNormal", StandardWidgetSet.POSITION_ITEM_COLOR_NORMAL );
    private final ColorProperty markColorLeader = new ColorProperty( this, "markColorLeader", StandardWidgetSet.POSITION_ITEM_COLOR_LEADER );
    private final ColorProperty markColorMe = new ColorProperty( this, "markColorMe", StandardWidgetSet.POSITION_ITEM_COLOR_ME );
    private final ColorProperty markColorNextInFront = new ColorProperty( this, "markColorNextInFront", StandardWidgetSet.POSITION_ITEM_COLOR_NEXT_IN_FRONT );
    private final ColorProperty markColorNextBehind = new ColorProperty( this, "markColorNextBehind", StandardWidgetSet.POSITION_ITEM_COLOR_NEXT_BEHIND );
    
    private final IntProperty maxDisplayedVehicles = new IntProperty( this, "maxDisplayedVehicles", 22, 1, 50 );
    
    private final BooleanProperty displayPositionNumbers = new BooleanProperty( this, "displayPosNumbers", true );
    
    private static final int ANTI_ALIAS_RADIUS_OFFSET = 1;
    
    private TransformableTexture[] itemTextures = null;
    private int[] itemStates = null;
    
    private int lineLength = 0;
    
    @Override
    public String getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedColorValue( String name )
    {
        String result = super.getDefaultNamedColorValue( name );
        
        if ( result != null )
            return ( result );
        
        result = StandardWidgetSet.getDefaultNamedColorValue( name );
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        String result = super.getDefaultNamedFontValue( name );
        
        if ( result != null )
            return ( result );
        
        result = StandardWidgetSet.getDefaultNamedFontValue( name );
        
        return ( result );
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
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        initSubTextures( isEditorMode );
        
        for ( int i = 0; i < maxDisplayedVehicles.getIntValue(); i++ )
        {
            itemStates[i] = -1;
        }
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
        
        int off2 = ( isFontAntiAliased() ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
        
        short ownPlace = scoringInfo.getOwnPlace();
        
        final Font font = getFont();
        final boolean posNumberFontAntiAliased = isFontAntiAliased();
        
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
                
                Color color = null;
                if ( vsi.getPlace() == 1 )
                {
                    itemState |= 1 << 16;
                    color = markColorLeader.getColor();
                }
                else if ( vsi.isPlayer() )
                {
                    itemState |= 2 << 16;
                    color = markColorMe.getColor();
                }
                else if ( vsi.getPlace() == ownPlace - 1 )
                {
                    itemState |= 3 << 16;
                    color = markColorNextInFront.getColor();
                }
                else if ( vsi.getPlace() == ownPlace + 1 )
                {
                    itemState |= 4 << 16;
                    color = markColorNextBehind.getColor();
                }
                else
                {
                    itemState |= 5 << 16;
                    color = markColorNormal.getColor();
                }
                
                tt.setTranslation( LINE_PADDING + off2 + lapDistance * lineLength - itemRadius, off2 + height / 2 - itemRadius );
                
                if ( itemStates[i] != itemState )
                {
                    itemStates[i] = itemState;
                    
                    StandardWidgetSet.drawPositionItem( tt.getTexture(), 0, 0, itemRadius, vsi.getPlace(), color, true, font, posNumberFontAntiAliased, getFontColor() );
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
    }
    
    public TrackPositionWidget( String name )
    {
        super( name, Size.getPercent( 35.0f ), Size.getPercent( 5.859375f ) );
        
        getFontProperty().setFont( StandardWidgetSet.POSITION_ITEM_FONT_NAME );
        getFontColorProperty().setColor( StandardWidgetSet.POSITION_ITEM_FONT_COLOR_NAME );
    }
}
