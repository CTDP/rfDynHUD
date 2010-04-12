package net.ctdp.rfdynhud.etv2010.widgets.standings;

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
    private final ColorProperty captionBackgroundColor = new ColorProperty( this, "captionBackgroundColor", ETVUtils.TV_STYLE_CAPTION_BACKGROUND_COLOR );
    private final ColorProperty captionBackgroundColor1st = new ColorProperty( this, "captionBackgroundColor1st", "#FF0000" );
    private final ColorProperty captionColor = new ColorProperty( this, "captionColor", ETVUtils.TV_STYLE_CAPTION_FONT_COLOR );
    
    private DrawnString[] captionStrings = null;
    private DrawnString[] nameStrings = null;
    private DrawnString[] gapStrings = null;
    
    private StringValue[] driverNames = null;
    private FloatValue[] gaps = null;
    
    //private int itemHeight = 0;
    private static final int ITEM_GAP = 3;
    private int numItems = 0;
    
    private int oldNumItems = 0;
    
    private Boolean[] itemsVisible = null;
    
    private final Size itemHeight = new Size( 0, Size.PERCENT_OFFSET + 0.025f, this, true );
    
    private TextureImage2D itemClearImage = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void bake()
    {
        super.bake();
        
        itemHeight.bake();
    }
    
    public void setAllPosAndSizeToPercents()
    {
        super.setAllPosAndSizeToPercents();
        
        if ( !itemHeight.isHeightPercentageValue() )
            itemHeight.flipHeightPercentagePx();
    }
    
    public void setAllPosAndSizeToPixels()
    {
        super.setAllPosAndSizeToPixels();
        
        if ( itemHeight.isHeightPercentageValue() )
            itemHeight.flipHeightPercentagePx();
    }
    
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
    public void onBoundInputStateChanged( boolean isEditorMode, InputAction action, boolean state, int modifierMask )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        int itemHeight = this.itemHeight.getEffectiveHeight();
        numItems = ( height + ITEM_GAP ) / ( itemHeight + ITEM_GAP );
        
        if ( ( itemClearImage == null ) || ( itemClearImage.getWidth() != width ) || ( itemClearImage.getHeight() != itemHeight * 2 ) )
        {
            itemClearImage = TextureImage2D.createOfflineTexture( width, itemHeight * 2, true );
            
            ETVUtils.drawLabeledDataBackground( 0, 0, width, itemHeight, "00", getFont(), captionBackgroundColor1st.getColor(), getBackgroundColor(), itemClearImage, true );
            ETVUtils.drawLabeledDataBackground( 0, itemHeight, width, itemHeight, "00", getFont(), captionBackgroundColor.getColor(), getBackgroundColor(), itemClearImage, true );
        }
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        texCanvas.setFont( getFont() );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D numBounds = metrics.getStringBounds( "00", texCanvas );
        
        int capWidth = (int)Math.ceil( numBounds.getWidth() );
        int dataAreaLeft = ETVUtils.getLabeledDataDataLeft( width, numBounds );
        int dataAreaRight = ETVUtils.getLabeledDataDataRight( width );
        int vMiddle = ETVUtils.getLabeledDataVMiddle( itemHeight, numBounds );
        
        captionStrings = new DrawnString[ numItems ];
        nameStrings = new DrawnString[ numItems ];
        gapStrings = new DrawnString[ numItems ];
        
        driverNames = new StringValue[ numItems ];
        gaps = new FloatValue[ numItems ];
        
        itemsVisible = new Boolean[ numItems ];
        
        for ( int i = 0; i < numItems; i++ )
        {
            captionStrings[i] = new DrawnString( ETVUtils.TRIANGLE_WIDTH + capWidth, vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
            nameStrings[i] = new DrawnString( dataAreaLeft, vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
            gapStrings[i] = new DrawnString( dataAreaRight, vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
            
            driverNames[i] = new StringValue();
            gaps[i] = new FloatValue();
            
            itemsVisible[i] = null;
        }
    }
    
    @Override
    protected void clearBackground( boolean isEditorMode, LiveGameData gameData, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        texture.clear( offsetX, offsetY, width, height, true, null );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        final int itemHeight = this.itemHeight.getEffectiveHeight();
        
        final int numDrivers = Math.min( numItems, scoringInfo.getNumVehicles() );
        
        for ( int i = 0; i < numDrivers; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            Boolean visible;
            if ( isEditorMode )
            {
                visible = true;
            }
            else
            {
                if ( scoringInfo.getSessionType().isRace() )
                {
                    // TODO: better heuristic
                    if ( vsi.getLapDistance() / scoringInfo.getTrackLength() < 0.2f )
                        visible = true;
                    else
                        visible = false;
                }
                else
                {
                    visible = ( vsi.getBestLapTime() > 0.0f );
                }
            }
            
            boolean drawBackground = needsCompleteRedraw;
            boolean visibilityChanged = false;
            
            if ( visible != itemsVisible[i] )
            {
                itemsVisible[i] = visible;
                drawBackground = true;
                visibilityChanged = true;
            }
            
            int offsetY2 = i * ( itemHeight + ITEM_GAP );
            
            if ( drawBackground )
            {
                if ( visible )
                {
                    int srcOffsetY = ( i == 0 ) ? 0 : itemHeight;
                    
                    texture.clear( itemClearImage, 0, srcOffsetY, width, itemHeight, offsetX, offsetY + offsetY2, width, itemHeight, true, null );
                    
                    try
                    {
                        captionStrings[i].draw( offsetX, offsetY + offsetY2, String.valueOf( i + 1 ), itemClearImage, offsetX, offsetY + offsetY2 - srcOffsetY, texture );
                    }
                    catch ( Throwable t )
                    {
                    }
                }
                else
                {
                    texture.clear( offsetX, offsetY + offsetY2, width, itemHeight, true, null );
                }
            }
            
            driverNames[i].update( vsi.getDriverNameTLC() );
            
            if ( ( needsCompleteRedraw || visibilityChanged || driverNames[i].hasChanged() ) && visible )
            {
                nameStrings[i].draw( offsetX, offsetY + offsetY2, driverNames[i].getValue(), getBackgroundColor(), getFontColor(), texture );
            }
            
            if ( isEditorMode || ( i > 0 ) )
            {
                if ( scoringInfo.getSessionType() == SessionType.RACE )
                    gaps[i].update( ( vsi.getLapsBehindLeader() > 0 ) ? -vsi.getLapsBehindLeader() : -vsi.getTimeBehindLeader() );
                else
                    gaps[i].update( vsi.getBestLapTime() - scoringInfo.getVehicleScoringInfo( 0 ).getBestLapTime() );
                
                if ( ( needsCompleteRedraw || visibilityChanged || gaps[i].hasChanged() ) && visible )
                {
                    String s;
                    if ( vsi.getBestLapTime() < 0.0f )
                        s = "";
                    else
                        s = ( gaps[i].getValue() < 0f ) ? "+" + ( (int)-gaps[i].getValue() ) + "Lap(s)" : TimingUtil.getTimeAsGapString( gaps[i].getValue() );
                    
                    gapStrings[i].draw( offsetX, offsetY + offsetY2, s, getBackgroundColor(), getFontColor(), texture );
                }
            }
        }
        
        for ( int i = numDrivers; i < oldNumItems; i++ )
        {
            int offsetY2 = i * ( itemHeight + ITEM_GAP );
            
            texture.clear( offsetX, offsetY + offsetY2, width, itemHeight, true, null );
        }
        
        oldNumItems = numDrivers;
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
        writer.writeProperty( "itemHeight", Size.unparseValue( itemHeight.getHeight() ), "The height of one item." );
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
        else if ( itemHeight.loadProperty( key, value, "sdfsdfsdfsdf", "itemHeight" ) );
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
        propsCont.addProperty( itemHeight.createHeightProperty( "itemHeight" ) );
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
        super( name, Size.PERCENT_OFFSET + 0.14f, Size.PERCENT_OFFSET + ( 0.025f * 10f ) );
        
        getBackgroundColorProperty().setValue( ETVUtils.TV_STYLE_DATA_BACKGROUND_COLOR );
        getFontColorProperty().setValue( ETVUtils.TV_STYLE_DATA_FONT_COLOR );
        getFontProperty().setValue( ETVUtils.TV_STYLE_FONT );
    }
}
