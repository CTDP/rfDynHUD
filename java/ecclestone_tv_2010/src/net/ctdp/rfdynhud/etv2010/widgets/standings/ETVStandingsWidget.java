package net.ctdp.rfdynhud.etv2010.widgets.standings;

import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.etv2010.widgets._base.ETVWidgetBase;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.StandingsTools;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.values.StandingsView;
import net.ctdp.rfdynhud.values.StringValue;
import net.ctdp.rfdynhud.values.__ValPrivilegedAccess;

/**
 * The {@link ETVStandingsWidget} displays the list of drivers and gaps.
 * 
 * @author Marvin Froehlich
 */
public class ETVStandingsWidget extends ETVWidgetBase
{
    private final ColorProperty captionBackgroundColor1st = new ColorProperty( this, "captionBgColor1st", ETVUtils.ETV_STYLE_CAPTION_BACKGROUND_COLOR_1ST );
    private final ColorProperty dataBackgroundColor1st = new ColorProperty( this, "dataBgColor1st", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR_1ST );
    
    private final BooleanProperty forceLeaderDisplayed = new BooleanProperty( this, "forceLeaderDisplayed", true );
    private final BooleanProperty showFastestLapsInRace = new BooleanProperty( this, "showFastestLapsInRace", true );
    
    private DrawnString[] captionStrings = null;
    private DrawnString[] nameStrings = null;
    private DrawnString[] gapStrings = null;
    
    private IntValue[] positions = null;
    private StringValue[] driverNames = null;
    private FloatValue[] gaps = null;
    
    private int maxNumItems = 0;
    
    private int oldNumItems = 0;
    
    private Boolean[] itemsVisible = null;
    
    private final Size itemHeight = __ValPrivilegedAccess.newWidgetSize( 0f, true, 2.5f, true, this );
    
    private TextureImage2D itemClearImage = null;
    
    private static final int NUM_FLAG_TEXTURES = 3;
    
    private TransformableTexture[] flagTextures = null;
    private final FloatValue[] laptimes = new FloatValue[ NUM_FLAG_TEXTURES ];
    private final DrawnString[] laptimeStrings = new DrawnString[ NUM_FLAG_TEXTURES ];
    
    private VehicleScoringInfo[] vehicleScoringInfos = null;
    
    private int oldNumVehicles = -1;
    
    private boolean isOnLeftSide = true;
    
    private IntValue[] lap = null;
    private float displayTime;
    private int lastVisibleIndex = -1;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersion()
    {
        return ( composeVersion( 1, 1, 0 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void bake()
    {
        super.bake();
        
        itemHeight.bake();
    }
    
    @Override
    public void setAllPosAndSizeToPercents()
    {
        super.setAllPosAndSizeToPercents();
        
        itemHeight.setHeightToPercents();
    }
    
    @Override
    public void setAllPosAndSizeToPixels()
    {
        super.setAllPosAndSizeToPixels();
        
        itemHeight.setHeightToPixels();
    }
    
    private final boolean getUseClassScoring()
    {
        return ( getConfiguration().getUseClassScoring() );
    }
    
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onSessionStarted( sessionType, gameData, editorPresets );
        
        if ( driverNames != null )
        {
            for ( int i = 0; i < driverNames.length; i++ )
            {
                positions[i].reset();
                driverNames[i].reset();
                gaps[i].reset();
                lap[i].reset();
            }
        }
        
        lastVisibleIndex = -1;
        
        forceReinitialization();
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        if ( laptimes != null )
        {
            for ( int i = 0; i < laptimes.length; i++ )
            {
                if ( laptimes[i] != null )
                    laptimes[i].reset();
            }
        }
        
        oldNumVehicles = -1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        if ( gameData.getScoringInfo().getSessionType().isRace() && !showFastestLapsInRace.getBooleanValue() )
        {
            flagTextures = null;
            
            return ( null );
        }
        
        int itemHeight = this.itemHeight.getEffectiveHeight();
        
        if ( ( flagTextures == null ) || ( flagTextures[0].getWidth() != widgetInnerWidth ) || ( flagTextures[0].getHeight() != itemHeight ) )
        {
            flagTextures = new TransformableTexture[ NUM_FLAG_TEXTURES ];
            
            for ( int i = 0; i < NUM_FLAG_TEXTURES; i++ )
            {
                flagTextures[i] = new TransformableTexture( widgetInnerWidth, itemHeight );
            }
        }
        
        return ( flagTextures );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        int numVehicles = getUseClassScoring() ? gameData.getScoringInfo().getNumVehiclesInSameClass( gameData.getScoringInfo().getViewedVehicleScoringInfo() ) : gameData.getScoringInfo().getNumVehicles();
        
        boolean result = ( numVehicles != oldNumVehicles );
        
        oldNumVehicles = numVehicles;
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        int itemHeight = this.itemHeight.getEffectiveHeight();
        maxNumItems = ( height + ETVUtils.ITEM_GAP ) / ( itemHeight + ETVUtils.ITEM_GAP );
        
        vehicleScoringInfos = new VehicleScoringInfo[ maxNumItems ];
        
        if ( ( itemClearImage == null ) || ( itemClearImage.getWidth() != width ) || ( itemClearImage.getHeight() != itemHeight * 2 ) )
        {
            itemClearImage = TextureImage2D.createOfflineTexture( width, itemHeight * 2, true );
            
            ETVUtils.drawLabeledDataBackground( 0, 0, width, itemHeight, "00", getFont(), captionBackgroundColor1st.getColor(), dataBackgroundColor1st.getColor(), itemClearImage, true );
            ETVUtils.drawLabeledDataBackground( 0, itemHeight, width, itemHeight, "00", getFont(), captionBackgroundColor.getColor(), getBackgroundColor(), itemClearImage, true );
        }
        else
        {
            itemClearImage.clear( false, null );
        }
        
        ETVUtils.drawLabeledDataBackground( 0, 0, width, itemHeight, "00", getFont(), captionBackgroundColor1st.getColor(), dataBackgroundColor1st.getColor(), itemClearImage, true );
        ETVUtils.drawLabeledDataBackground( 0, itemHeight, width, itemHeight, "00", getFont(), captionBackgroundColor.getColor(), getBackgroundColor(), itemClearImage, true );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        texCanvas.setFont( getFont() );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D numBounds = metrics.getStringBounds( "00", texCanvas );
        
        int capWidth = (int)Math.ceil( numBounds.getWidth() );
        int dataAreaLeft = ETVUtils.getLabeledDataDataLeft( numBounds );
        int dataAreaRight = ETVUtils.getLabeledDataDataRight( width );
        int vMiddle = ETVUtils.getLabeledDataVMiddle( itemHeight, numBounds );
        
        captionStrings = new DrawnString[ maxNumItems ];
        nameStrings = new DrawnString[ maxNumItems ];
        gapStrings = new DrawnString[ maxNumItems ];
        
        positions = new IntValue[ maxNumItems ];
        driverNames = new StringValue[ maxNumItems ];
        gaps = new FloatValue[ maxNumItems ];
        
        lap = new IntValue[ maxNumItems ];
        
        itemsVisible = new Boolean[ maxNumItems ];
        
        for ( int i = 0; i < maxNumItems; i++ )
        {
            captionStrings[i] = dsf.newDrawnString( "captionStrings" + i, ETVUtils.TRIANGLE_WIDTH + capWidth, vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
            nameStrings[i] = dsf.newDrawnString( "nameStrings" + i, dataAreaLeft, vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
            gapStrings[i] = dsf.newDrawnString( "gapStrings" + i, dataAreaRight, vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
            
            positions[i] = new IntValue();
            driverNames[i] = new StringValue();
            gaps[i] = new FloatValue();
            
            lap[i] = new IntValue();
            
            itemsVisible[i] = null;
        }
        
        TransformableTexture[] flagTextures = getSubTexturesImpl( gameData, editorPresets,  width, height );
        
        if ( flagTextures != null )
        {
            for ( int i = 0; i < NUM_FLAG_TEXTURES; i++ )
            {
                ETVUtils.drawDataBackground( 0, 0, flagTextures[i].getWidth(), flagTextures[i].getHeight(), getBackgroundColor(), flagTextures[i].getTexture(), true );
                
                laptimes[i] = new FloatValue();
                laptimeStrings[i] = dsf.newDrawnString( "laptimeStrings" + i, flagTextures[i].getWidth() / 2, vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
            }
        }
        
        isOnLeftSide = ( getPosition().getEffectiveX() < getConfiguration().getGameResolution().getViewportWidth() - getPosition().getEffectiveX() - getSize().getEffectiveWidth() );
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        texture.clear( offsetX, offsetY, width, height, true, null );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        final int itemHeight = this.itemHeight.getEffectiveHeight();
        
        final int numDrivers = StandingsTools.getDisplayedVSIsForScoring( scoringInfo, scoringInfo.getViewedVehicleScoringInfo(), getUseClassScoring(), StandingsView.RELATIVE_TO_LEADER, forceLeaderDisplayed.getBooleanValue(), vehicleScoringInfos );
        int numDisplayedLaptimes = 0;
        
        if ( flagTextures != null )
        {
            for ( int i = 0; i < flagTextures.length; i++ )
            {
                flagTextures[i].setVisible( false );
            }
        }
        
        if ( scoringInfo.getSessionTime() > displayTime )
        {
            lastVisibleIndex = -1;
        }
        
        int i2 = 0;
        if ( ( numDrivers > 1 ) && ( vehicleScoringInfos[1].getPlace( getUseClassScoring() ) - vehicleScoringInfos[0].getPlace( getUseClassScoring() ) > 1 ) )
        {
            i2 = 1;
        }
        
        for ( int i = 0; i < numDrivers; i++ )
        {
            VehicleScoringInfo vsi = vehicleScoringInfos[i];
            
            lap[i].update( vsi.getCurrentLap() );
            
            if ( lap[i].hasChanged() )
            {
                if ( ( i == 0 ) || ( i == i2 ) )
                {
                    lastVisibleIndex = 0;
                    displayTime = scoringInfo.getSessionTime() + 40f;
                }
                else if ( scoringInfo.getSessionTime() <= displayTime )
                {
                    lastVisibleIndex = Math.max( lastVisibleIndex, i );
                    displayTime = Math.max( displayTime, scoringInfo.getSessionTime() + 20f );
                }
            }
        }
        
        for ( int i = 0; i < numDrivers; i++ )
        {
            VehicleScoringInfo vsi = vehicleScoringInfos[i];
            short place = vsi.getPlace( getUseClassScoring() );
            
            Boolean visible;
            if ( isEditorMode )
            {
                visible = true;
            }
            else
            {
                if ( scoringInfo.getSessionType().isRace() )
                    visible = ( i <= lastVisibleIndex );
                else
                    visible = ( vsi.getBestLapTime() > 0.0f );
            }
            
            boolean drawBackground = needsCompleteRedraw;
            boolean visibilityChanged = false;
            
            if ( visible != itemsVisible[i] )
            {
                itemsVisible[i] = visible;
                drawBackground = true;
                visibilityChanged = true;
            }
            
            int offsetY2 = i * ( itemHeight + ETVUtils.ITEM_GAP );
            int srcOffsetY = ( place == 1 ) ? 0 : itemHeight;
            
            if ( drawBackground )
            {
                if ( visible )
                    texture.clear( itemClearImage, 0, srcOffsetY, width, itemHeight, offsetX, offsetY + offsetY2, width, itemHeight, true, null );
                else
                    texture.clear( offsetX, offsetY + offsetY2, width, itemHeight, true, null );
            }
            
            positions[i].update( place );
            
            if ( ( needsCompleteRedraw || visibilityChanged || positions[i].hasChanged() ) && visible )
                captionStrings[i].draw( offsetX, offsetY + offsetY2, positions[i].getValueAsString(), itemClearImage, offsetX, offsetY + offsetY2 - srcOffsetY, getFontColor(), texture );
            
            driverNames[i].update( vsi.getDriverNameTLC() );
            
            if ( ( needsCompleteRedraw || visibilityChanged || driverNames[i].hasChanged() ) && visible )
                nameStrings[i].draw( offsetX, offsetY + offsetY2, driverNames[i].getValue(), itemClearImage, offsetX, offsetY + offsetY2 - srcOffsetY, getFontColor(), texture );
            
            if ( place > 1 )
            {
                if ( scoringInfo.getSessionType() == SessionType.RACE )
                    gaps[i].update( ( vsi.getLapsBehindLeader( getUseClassScoring() ) > 0 ) ? -vsi.getLapsBehindLeader( getUseClassScoring() ) - 10000 : Math.abs( vsi.getTimeBehindLeader( getUseClassScoring() ) ) );
                else
                    gaps[i].update( vsi.getBestLapTime() - scoringInfo.getLeadersVehicleScoringInfo().getBestLapTime() );
                
                if ( ( needsCompleteRedraw || visibilityChanged || gaps[i].hasChanged() ) && visible )
                {
                    String s;
                    if ( vsi.getBestLapTime() < 0.0f )
                    {
                        s = "";
                    }
                    else if ( gaps[i].getValue() < -10000f )
                    {
                        int l = ( (int)-( gaps[i].getValue() + 10000.0f ) );
                        
                        if ( l == 1 )
                            s = "+" + l + Loc.gap_lap;
                        else
                            s = "+" + l + Loc.gap_laps;
                    }
                    else
                    {
                        s = TimingUtil.getTimeAsGapString( gaps[i].getValue() );
                    }
                    
                    gapStrings[i].draw( offsetX, offsetY + offsetY2, s, itemClearImage, offsetX, offsetY + offsetY2 - srcOffsetY, getFontColor(), texture );
                }
            }
            
            if ( flagTextures != null )
            {
                if ( !isEditorMode && visible && ( numDisplayedLaptimes < flagTextures.length - 1 ) )
                {
                    Laptime lt = vsi.getFastestLaptime();
                    if ( ( lt != null ) && ( lt.getLap() == vsi.getCurrentLap() - 1 ) && ( vsi.getStintStartLap() != vsi.getCurrentLap() ) && ( scoringInfo.getSessionTime() - vsi.getLapStartTime() < 20.0f ) )
                    {
                        int tti = numDisplayedLaptimes++;
                        TransformableTexture tt = flagTextures[tti];
                        
                        laptimes[tti].update( lt.getLapTime() );
                        
                        if ( laptimes[tti].hasChanged() )
                        {
                            laptimeStrings[tti].draw( 0, 0, TimingUtil.getTimeAsLaptimeString( laptimes[tti].getValue() ), getBackgroundColor(), tt.getTexture() );
                        }
                        
                        if ( isOnLeftSide )
                            tt.setTranslation( width - ( ETVUtils.TRIANGLE_WIDTH / 2.0f ), offsetY2 );
                        else
                            tt.setTranslation( -width + ( ETVUtils.TRIANGLE_WIDTH / 2.0f ), offsetY2 );
                        tt.setVisible( true );
                    }
                }
            }
        }
        
        for ( int i = numDrivers; i < oldNumItems; i++ )
        {
            int offsetY2 = i * ( itemHeight + ETVUtils.ITEM_GAP );
            
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
        
        writer.writeProperty( captionBackgroundColor1st, "The background color for the \"Position\" caption for first place." );
        writer.writeProperty( dataBackgroundColor1st, "The background color for the data area, for first place." );
        itemHeight.saveHeightProperty( "itemHeight", "The height of one item.", writer );
        writer.writeProperty( forceLeaderDisplayed, "Display leader regardless of maximum displayed drivers setting?" );
        writer.writeProperty( showFastestLapsInRace, "Display fastest lap flags in race session?" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( captionBackgroundColor1st.loadProperty( key, value ) );
        else if ( dataBackgroundColor1st.loadProperty( key, value ) );
        else if ( itemHeight.loadProperty( key, value, null, "itemHeight" ) );
        else if ( forceLeaderDisplayed.loadProperty( key, value ) );
        else if ( showFastestLapsInRace.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addProperty( captionBackgroundColor1st );
        propsCont.addProperty( dataBackgroundColor1st );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( itemHeight.createHeightProperty( "itemHeight" ) );
        propsCont.addProperty( forceLeaderDisplayed );
        propsCont.addProperty( showFastestLapsInRace );
    }
    
    public ETVStandingsWidget( String name )
    {
        super( name, 14.0f, 10f * 2.5f );
    }
}
