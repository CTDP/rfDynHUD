package net.ctdp.rfdynhud.widgets.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ModInfo;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.Track;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.LabelPositioning;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link MapWidget} renders a map overview of the current track.
 * 
 * @author Marvin Froehlich
 */
public class MapWidget extends Widget
{
    private TextureImage2D cacheTexture = null;
    private boolean isBgClean = false;
    private Track track = null;
    private float scale = 1f;
    private int baseItemRadius = 9;
    private int itemRadius = baseItemRadius;
    
    private final BooleanProperty rotationEnabled = new BooleanProperty( this, "rotationEnabled", false );
    
    private final ColorProperty roadColor = new ColorProperty( this, "roadColor", "color", "#000000" );
    private final ColorProperty roadBoundaryColor = new ColorProperty( this, "roadBoundaryColor", "boundaryColor", "#FFFFFF" );
    private final ColorProperty pitlaneColor = new ColorProperty( this, "pitlaneColor", "pitlaneColor", "#FFFF00" );
    private final IntProperty roadWidth = new IntProperty( this, "roadWidth", "width", 4, 2, 20, false )
    {
        @Override
        protected int fixValue( int value )
        {
            value = super.fixValue( value );
            
            return ( Math.round( value / 2f ) * 2 );
        }
    };
    
    private final ColorProperty markColorNormal = new ColorProperty( this, "markColorNormal", "colorNormal", StandardWidgetSet.POSITION_ITEM_COLOR_NORMAL );
    private final ColorProperty markColorLeader = new ColorProperty( this, "markColorLeader", "colorLeader", StandardWidgetSet.POSITION_ITEM_COLOR_LEADER );
    private final ColorProperty markColorMe = new ColorProperty( this, "markColorMe", "colorMe", StandardWidgetSet.POSITION_ITEM_COLOR_ME );
    private final BooleanProperty useMyColorForMe1st = new BooleanProperty( this, "useMyColorForMe1st", false );
    private final ColorProperty markColorNextInFront = new ColorProperty( this, "markColorNextInFront", "colorNextInFront", StandardWidgetSet.POSITION_ITEM_COLOR_NEXT_IN_FRONT );
    private final ColorProperty markColorNextBehind = new ColorProperty( this, "markColorNextBehind", "colorNextBehind", StandardWidgetSet.POSITION_ITEM_COLOR_NEXT_BEHIND );
    
    private final BooleanProperty displayPositionNumbers = new BooleanProperty( this, "displayPosNumbers", true );
    
    private final BooleanProperty displayNameLabels = new BooleanProperty( this, "displayNameLabels", false );
    private final EnumProperty<LabelPositioning> nameLabelPos = new EnumProperty<LabelPositioning>( this, "nameLabelPos", LabelPositioning.BELOW_RIGHT );
    private final FontProperty nameLabelFont = new FontProperty( this, "nameLabelFont", StandardWidgetSet.POSITION_ITEM_FONT_NAME );
    private final ColorProperty nameLabelFontColor = new ColorProperty( this, "nameLabelFontColor", StandardWidgetSet.POSITION_ITEM_COLOR_NORMAL );
    
    private int maxDisplayedVehicles = -1;
    
    private static final int ANTI_ALIAS_RADIUS_OFFSET = 1;
    
    private TransformableTexture[] subTextures = null;
    private int[] itemStates = null;
    
    private final Point2D.Float position = new Point2D.Float();
    
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMasterCanvas( boolean isEditorMode )
    {
        return ( !rotationEnabled.getBooleanValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNeededData()
    {
        return ( Widget.NEEDED_DATA_SCORING );
    }
    
    private void setItemRadius( int radius )
    {
        this.baseItemRadius = radius;
        
        forceAndSetDirty();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTrackChanged( String trackname, LiveGameData gameData, EditorPresets editorPresets )
    {
        track = null;
    }
    
    private void initMaxDisplayedVehicles( boolean isEditorMode, ModInfo modInfo )
    {
        if ( isEditorMode )
            this.maxDisplayedVehicles = 23;
        else
            this.maxDisplayedVehicles = modInfo.getMaxOpponents() + 1;
    }
    
    private void initSubTextures( boolean isEditorMode, ModInfo modInfo, int widgetWidth, int widgetHeight )
    {
        initMaxDisplayedVehicles( isEditorMode, modInfo );
        
        int numTextures = maxDisplayedVehicles;
        int subTexOff = 0;
        if ( !hasMasterCanvas( isEditorMode ) )
        {
            numTextures++;
            subTexOff++;
        }
        
        if ( ( subTextures == null ) || ( subTextures.length != numTextures ) )
        {
            subTextures = new TransformableTexture[ numTextures ];
            itemStates = new int[ numTextures ];
        }
        
        if ( !hasMasterCanvas( isEditorMode ) && ( ( subTextures[0] == null ) || ( subTextures[0].getWidth() != widgetWidth ) || ( subTextures[0].getHeight() != widgetHeight ) ) )
        {
            subTextures[0] = new TransformableTexture( widgetWidth, widgetHeight, isEditorMode );
        }
        
        itemRadius = Math.round( baseItemRadius * getConfiguration().getGameResolution().getResY() / 960f );
        
        if ( subTextures[subTexOff] == null )
            subTextures[subTexOff] = new TransformableTexture( 1, 1, isEditorMode );
        
        java.awt.Dimension size = StandardWidgetSet.getPositionItemSize( subTextures[0].getTexture(), itemRadius, displayNameLabels.getBooleanValue() ? nameLabelPos.getEnumValue() : null, nameLabelFont.getFont(), nameLabelFont.isAntiAliased() );
        int w = size.width;
        int h = size.height;
        
        if ( ( subTextures[subTexOff].getWidth() == w ) && ( subTextures[subTexOff].getHeight() == h ) )
            return;
        
        for ( int i = 0; i < maxDisplayedVehicles; i++ )
        {
            subTextures[subTexOff + i] = new TransformableTexture( w, h, isEditorMode );
            subTextures[subTexOff + i].setVisible( false );
            
            itemStates[i] = -1;
        }
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        initSubTextures( editorPresets != null, gameData.getModInfo(), widgetInnerWidth, widgetInnerHeight );
        
        return ( subTextures );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        initMaxDisplayedVehicles( isEditorMode, gameData.getModInfo() );
        
        if ( track == null )
        {
            File sceneFolder = gameData.getTrackInfo().getTrackFolder();
            if ( sceneFolder == null )
            {
                if ( gameData.getTrackInfo().getSceneFile() == null )
                    Logger.log( "Warning: Unable to read last used track file from PLR file." );
                else
                    Logger.log( "Warning: Couldn't read track data from file \"" + gameData.getTrackInfo().getSceneFile() + "\"." );
            }
            else
            {
                track = gameData.getTrackInfo().getTrack();
            }
            //track = Track.parseTrackFromAIW( new File( "D:\\Spiele\\rFactor\\GameData\\Locations\\4r2009FSone\\Northamptonshire\\NAS_BritishGP\\NAS_BritishGP.AIW" ) );
        }
        
        initSubTextures( isEditorMode, gameData.getModInfo(), width, height );
        
        if ( ( cacheTexture == null ) || ( cacheTexture.getUsedWidth() != width ) || ( cacheTexture.getUsedHeight() != height ) )
        {
            cacheTexture = TextureImage2D.createOfflineTexture( width, height, true );
        }
        
        cacheTexture.clear( true, null );
        
        if ( track == null )
        {
            Texture2DCanvas tc = cacheTexture.getTextureCanvas();
            tc.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            
            tc.setColor( roadColor.getColor() );
            
            tc.drawArc( 3, 3, cacheTexture.getWidth() - 6, cacheTexture.getHeight() - 6, 0, 360 );
            
            tc.setColor( Color.RED );
            tc.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
            Rectangle2D bounds = tc.getFontMetrics().getStringBounds( "Ag", tc );
            tc.drawString( "Couldn't read track data.", 3, (int)bounds.getHeight() );
            tc.drawString( "Please see the log for more info.", 3, (int)bounds.getHeight() * 2  );
        }
        else if ( track.getNumWaypoints( false ) > 0 )
        {
            Texture2DCanvas tc = cacheTexture.getTextureCanvas();
            tc.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            
            int off2 = ( isFontAntiAliased() ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
            int dia = itemRadius + itemRadius + off2 + off2;
            
            if ( rotationEnabled.getBooleanValue() )
                scale = track.getScale( Math.min( width - dia + itemRadius + itemRadius - subTextures[1].getWidth(), height - dia ), Math.min( width - dia + itemRadius + itemRadius - subTextures[1].getWidth(), height - dia ) );
            else
                scale = track.getScale( width - dia - itemRadius - itemRadius + subTextures[0].getWidth(), height - dia );
            
            Point p0 = new Point();
            Point p1 = new Point();
            
            int x0 = off2 + itemRadius + ( ( width - dia - track.getXExtend( scale ) ) / 2 );
            int y0 = off2 + itemRadius + ( ( height - dia - track.getZExtend( scale ) ) / 2 );
            
            if ( rotationEnabled.getBooleanValue() )
            {
                subTextures[0].setRotationCenter( x0 + track.getXExtend( scale ) / 2, y0 + track.getZExtend( scale ) / 2 );
            }
            
            Stroke oldStroke = tc.getStroke();
            
            int n = track.getNumWaypoints( false );
            
            int[] xPoints = new int[ n ];
            int[] yPoints = new int[ n ];
            
            track.getWaypointPosition( false, 0, scale, p0 );
            xPoints[0] = x0 + p0.x;
            yPoints[0] = y0 + p0.y;
            
            int j = 1;
            for ( int i = 1; i < n; i++ )
            {
                track.getWaypointPosition( false, i, scale, p1 );
                
                double dsq = ( p0.getX() - p1.getX() ) * ( p0.getX() - p1.getX() ) + ( p0.getY() - p1.getY() ) * ( p0.getY() - p1.getY() );
                
                if ( ( dsq >= 16.0 ) || ( i == n - 1 ) )
                {
                    xPoints[j] = x0 + p1.x;
                    yPoints[j] = y0 + p1.y;
                    
                    j++;
                    
                    Point p = p1;
                    p1 = p0;
                    p0 = p;
                }
            }
            
            tc.setColor( roadBoundaryColor.getColor() );
            tc.setStroke( new BasicStroke( roadWidth.getIntValue() ) );
            tc.setAntialiazingEnabled( true );
            
            tc.drawPolygon( xPoints, yPoints, j );
            
            tc.setColor( roadColor.getColor() );
            tc.setStroke( new BasicStroke( roadWidth.getIntValue() - 1.5f ) );
            tc.setAntialiazingEnabled( true );
            
            tc.drawPolygon( xPoints, yPoints, j );
            
            tc.setColor( pitlaneColor.getColor() );
            tc.setStroke( new BasicStroke( 2f ) );
            tc.setAntialiazingEnabled( true );
            
            n = track.getNumWaypoints( true );
            
            xPoints = new int[ n + 1 ];
            yPoints = new int[ n + 1 ];
            
            int k = 0;
            track.getWaypointPosition( true, k, scale, p0 );
            xPoints[0] = x0 + p0.x;
            yPoints[0] = y0 + p0.y;
            
            j = 1;
            for ( int i = k + 1; i < n; i++ )
            {
                track.getWaypointPosition( true, i, scale, p1 );
                
                double dsq = ( p0.getX() - p1.getX() ) * ( p0.getX() - p1.getX() ) + ( p0.getY() - p1.getY() ) * ( p0.getY() - p1.getY() );
                
                if ( dsq > 50 * 50 )
                {
                    if ( k < i - 1 )
                    {
                        track.getWaypointPosition( true, i - 1, scale, p0 );
                        
                        xPoints[j - 1] = x0 + p0.x;
                        yPoints[j - 1] = y0 + p0.y;
                    }
                    
                    tc.drawPolyline( xPoints, yPoints, j );
                    
                    xPoints[0] = x0 + p1.x;
                    yPoints[0] = y0 + p1.y;
                    
                    j = 1;
                    
                    Point p = p1;
                    p1 = p0;
                    p0 = p;
                }
                else if ( ( dsq >= 16.0 ) || ( i == n - 1 ) )
                {
                    xPoints[j] = x0 + p1.x;
                    yPoints[j] = y0 + p1.y;
                    
                    j++;
                    
                    Point p = p1;
                    p1 = p0;
                    p0 = p;
                    
                    k = i;
                }
            }
            
            track.getWaypointPosition( true, 0, scale, p0 );
            track.getWaypointPosition( true, n - 1, scale, p1 );
            double dsq = ( p0.getX() - p1.getX() ) * ( p0.getX() - p1.getX() ) + ( p0.getY() - p1.getY() ) * ( p0.getY() - p1.getY() );
            if ( dsq <= 16.0 )
            {
                xPoints[j] = x0 + p0.x;
                yPoints[j] = y0 + p0.y;
                j++;
            }
            
            tc.drawPolyline( xPoints, yPoints, j );
            
            tc.setStroke( oldStroke );
        }
        else
        {
            scale = 1f;
            cacheTexture = null;
        }
        
        for ( int i = 0; i < maxDisplayedVehicles; i++ )
        {
            itemStates[i] = -1;
        }
        
        if ( editorPresets != null )
        {
            isBgClean = false;
        }
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( hasMasterCanvas( editorPresets != null ) )
        {
            if ( cacheTexture == null )
                texture.clear( offsetX, offsetY, width, height, true, null );
            else
                texture.clear( cacheTexture, offsetX, offsetY, width, height, true, null );
            
            isBgClean = false;
        }
        else
        {
            if ( ( editorPresets != null ) && !isBgClean )
            {
                texture.clear( offsetX, offsetY, width, height, true, null );
                isBgClean = true;
            }
            
            if ( cacheTexture == null )
                subTextures[0].getTexture().clear( 0, 0, width, height, true, null );
            else
                subTextures[0].getTexture().clear( cacheTexture, 0, 0, width, height, true, null );
        }
    }
    
    private final AffineTransform at = new AffineTransform();
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( ( track != null ) && ( cacheTexture != null ) )
        {
            int off2 = ( isFontAntiAliased() ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
            int x0 = off2 - itemRadius + ( ( width - track.getXExtend( scale ) ) / 2 );
            int y0 = off2 - itemRadius + ( ( height - track.getZExtend( scale ) ) / 2 );
            
            short ownPlace = scoringInfo.getOwnPlace();
            
            final Font font = getFont();
            final boolean posNumberFontAntiAliased = isFontAntiAliased();
            
            int subTexOff = hasMasterCanvas( editorPresets != null ) ? 0 : 1;
            
            float rotation = rotationEnabled.getBooleanValue() ? track.getInterpolatedAngleToRoad( scoringInfo ) : 0f;
            if ( rotationEnabled.getBooleanValue() )
            {
                subTextures[0].setRotation( rotation );
                at.setToRotation( rotation, x0 + track.getXExtend( scale ) / 2, y0 + track.getZExtend( scale ) / 2 );
            }
            
            int n = Math.min( scoringInfo.getNumVehicles(), maxDisplayedVehicles );
            for ( int i = 0; i < n; i++ )
            {
                VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
                if ( /*!vsi.isInPits() &&*/ ( vsi.getFinishStatus().isNone() || vsi.getFinishStatus().isFinished() ) )
                {
                    float lapDistance = ( vsi.getLapDistance() + vsi.getScalarVelocityMPS() * scoringInfo.getExtrapolationTime() ) % track.getTrackLength();
                    
                    TransformableTexture tt = subTextures[subTexOff + i];
                    subTextures[subTexOff + i].setVisible( true );
                    int itemState = ( vsi.getPlace() << 0 ) | ( vsi.getDriverId() << 9 );
                    
                    if ( track.getInterpolatedPosition( vsi.isInPits(), lapDistance, scale, position ) )
                    {
                        Color color = null;
                        if ( vsi.getPlace() == 1 )
                        {
                            itemState |= 1 << 26;
                            if ( vsi.isPlayer() && useMyColorForMe1st.getBooleanValue() )
                                color = markColorMe.getColor();
                            else
                                color = markColorLeader.getColor();
                        }
                        else if ( vsi.isPlayer() )
                        {
                            itemState |= 1 << 27;
                            color = markColorMe.getColor();
                        }
                        else if ( vsi.getPlace() == ownPlace - 1 )
                        {
                            itemState |= 1 << 28;
                            color = markColorNextInFront.getColor();
                        }
                        else if ( vsi.getPlace() == ownPlace + 1 )
                        {
                            itemState |= 1 << 29;
                            color = markColorNextBehind.getColor();
                        }
                        else
                        {
                            itemState |= 1 << 30;
                            color = markColorNormal.getColor();
                        }
                        
                        if ( itemStates[i] != itemState )
                        {
                            itemStates[i] = itemState;
                            
                            StandardWidgetSet.drawPositionItem( tt.getTexture(), 0, 0, itemRadius, vsi.getPlace(), color, true, displayPositionNumbers.getBooleanValue() ? font : null, posNumberFontAntiAliased, getFontColor(), displayNameLabels.getBooleanValue() ? nameLabelPos.getEnumValue() : null, vsi.getDriverNameTLC(), nameLabelFont.getFont(), nameLabelFont.isAntiAliased(), nameLabelFontColor.getColor() );
                        }
                        
                        position.x += x0;
                        position.y += y0;
                        
                        if ( rotationEnabled.getBooleanValue() )
                            at.transform( position, position );
                        
                        tt.setTranslation( position.x, position.y );
                    }
                }
                else
                {
                    subTextures[subTexOff + i].setVisible( false );
                }
                
                subTextures[subTexOff + i].setVisible( true );
            }
            
            for ( int i = n; i < maxDisplayedVehicles; i++ )
                subTextures[subTexOff + i].setVisible( false );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( rotationEnabled, "Map rotation enabled?" );
        writer.writeProperty( roadColor, "The color used for the road in #RRGGBBAA (hex)." );
        writer.writeProperty( roadBoundaryColor, "The color used for the road boundary in #RRGGBBAA (hex)." );
        writer.writeProperty( pitlaneColor, "The color used for the pitlane in #RRGGBBAA (hex)." );
        writer.writeProperty( roadWidth, "The width of the roadin absolute pixels." );
        writer.writeProperty( "itemRadius", baseItemRadius, "The abstract radius for any displayed driver item." );
        writer.writeProperty( markColorNormal, "The color used for all, but special cars in #RRGGBBAA (hex)." );
        writer.writeProperty( markColorLeader, "The color used for the leader's car in #RRGGBBAA (hex)." );
        writer.writeProperty( markColorMe, "The color used for your own car in #RRGGBBAA (hex)." );
        writer.writeProperty( useMyColorForMe1st, "Use 'markColorMe' for my item when I am at 1st place?" );
        writer.writeProperty( markColorNextInFront, "The color used for the car in front of you in #RRGGBBAA (hex)." );
        writer.writeProperty( markColorNextBehind, "The color used for the car behind you in #RRGGBBAA (hex)." );
        writer.writeProperty( displayPositionNumbers, "Display numbers on the position markers?" );
        writer.writeProperty( displayNameLabels, "Display name label near the position markers?" );
        writer.writeProperty( nameLabelPos, "Positioning of the name labels." );
        writer.writeProperty( nameLabelFont, "Font for the name labels." );
        writer.writeProperty( nameLabelFontColor, "Font color for the name labels." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( rotationEnabled.loadProperty( key, value ) );
        else if ( roadColor.loadProperty( key, value ) );
        else if ( roadBoundaryColor.loadProperty( key, value ) );
        else if ( pitlaneColor.loadProperty( key, value ) );
        else if ( roadWidth.loadProperty( key, value ) );
        if ( key.equals( "itemRadius" ) )
            this.baseItemRadius = Integer.parseInt( value );
        else if ( markColorNormal.loadProperty( key, value ) );
        else if ( markColorLeader.loadProperty( key, value ) );
        else if ( markColorMe.loadProperty( key, value ) );
        else if ( useMyColorForMe1st.loadProperty( key, value ) );
        else if ( markColorNextInFront.loadProperty( key, value ) );
        else if ( markColorNextBehind.loadProperty( key, value ) );
        else if ( displayPositionNumbers.loadProperty( key, value ) );
        else if ( displayNameLabels.loadProperty( key, value ) );
        else if ( nameLabelPos.loadProperty( key, value ) );
        else if ( nameLabelFont.loadProperty( key, value ) );
        else if ( nameLabelFontColor.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( rotationEnabled );
        
        propsCont.addGroup( "Road" );
        
        propsCont.addProperty( roadColor );
        propsCont.addProperty( roadBoundaryColor );
        propsCont.addProperty( pitlaneColor );
        propsCont.addProperty( roadWidth );
        
        propsCont.addGroup( "Items" );
        
        propsCont.addProperty( new Property( this, "itemRadius", "radius", PropertyEditorType.INTEGER )
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
        propsCont.addProperty( useMyColorForMe1st );
        propsCont.addProperty( markColorNextInFront );
        propsCont.addProperty( markColorNextBehind );
        
        propsCont.addProperty( displayPositionNumbers );
        
        propsCont.addProperty( displayNameLabels );
        //if ( displayNameLabels.getBooleanValue() || forceAll )
        {
            //propsCont.addProperty( nameLabelPos );
            propsCont.addProperty( nameLabelFont );
            propsCont.addProperty( nameLabelFontColor );
        }
    }
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    public MapWidget( String name )
    {
        super( name, 16f, 24f );
        
        getBackgroundColorProperty().setColor( (String)null );
        
        getFontProperty().setFont( StandardWidgetSet.POSITION_ITEM_FONT_NAME );
        getFontColorProperty().setColor( StandardWidgetSet.POSITION_ITEM_FONT_COLOR_NAME );
    }
}
