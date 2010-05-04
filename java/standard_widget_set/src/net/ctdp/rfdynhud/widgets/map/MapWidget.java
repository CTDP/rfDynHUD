package net.ctdp.rfdynhud.widgets.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
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
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.RFactorTools;
import net.ctdp.rfdynhud.util.Track;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link MapWidget} renders a map overview of the current track.
 * 
 * @author Marvin Froehlich
 */
public class MapWidget extends Widget
{
    private TextureImage2D texture2 = null;
    private Track track = null;
    private float scale = 1f;
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
    
    public void setItemRadius( int radius )
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
        
        if ( track == null )
        {
            Track track;
            if ( isEditorMode )
            {
                File sceneFolder = RFactorTools.getLastUsedTrackFile( null );
                if ( sceneFolder == null )
                {
                    track = null;
                    
                    if ( RFactorTools.isLastUsedTrackFileValid() )
                        Logger.log( "Warning: Couldn't read track data from file \"" + RFactorTools.getPlainLastUsedTrackFile() + "\"." );
                    else
                        Logger.log( "Warning: Unable to read last used track file from PLR file." );
                }
                else
                {
                    track = gameData.getTrack( sceneFolder.getParentFile() );
                }
            }
            else
            {
                track = gameData.getTrack();
            }
            //track = Track.parseTrackFromAIW( new File( "D:\\Spiele\\rFactor\\GameData\\Locations\\4r2009FSone\\Northamptonshire\\NAS_BritishGP\\NAS_BritishGP.AIW" ) );
            
            if ( track != this.track )
                this.track = track;
        }
        
        initSubTextures( isEditorMode );
        
        if ( ( texture2 == null ) || ( texture2.getUsedWidth() != width ) || ( texture2.getUsedHeight() != height ) )
        {
            texture2 = TextureImage2D.createOfflineTexture( width, height, true );
        }
        
        texture2.clear( true, null );
        
        if ( track == null )
        {
            Texture2DCanvas tc = texture2.getTextureCanvas();
            tc.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            
            tc.setColor( Color.BLACK );
            
            tc.drawArc( 3, 3, texture2.getWidth() - 6, texture2.getHeight() - 6, 0, 360 );
            
            tc.setColor( Color.RED );
            tc.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
            Rectangle2D bounds = tc.getFontMetrics().getStringBounds( "Ag", tc );
            tc.drawString( "Couldn't read track data.", 3, (int)bounds.getHeight() );
            tc.drawString( "Please see the log for more info.", 3, (int)bounds.getHeight() * 2  );
        }
        else if ( track.getNumWaypoints( false ) > 0 )
        {
            Texture2DCanvas tc = texture2.getTextureCanvas();
            tc.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            
            int off2 = ( isFontAntiAliased() ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
            int dia = itemRadius + itemRadius + off2 + off2;
            
            scale = track.getScale( width - dia, height - dia );
            
            Point p0 = new Point();
            Point p1 = new Point();
            
            int x0 = off2 + itemRadius;// + ( ( width - dia - track.getXExtend( scale ) ) / 2 );
            int y0 = off2 + itemRadius;// + ( ( height - dia - track.getZExtend( scale ) ) / 2 );
            
            int n = track.getNumWaypoints( false );
            
            Stroke oldStroke = tc.getStroke();
            
            tc.setColor( Color.WHITE );
            tc.setStroke( new BasicStroke( 4 ) );
            
            track.getWaypointPosition( false, n - 1, scale, p0 );
            for ( int i = 0; i < n - 1; i++ )
            {
                track.getWaypointPosition( false, i, scale, p1 );
                
                tc.drawLine( x0 + p0.x, y0 + p0.y, x0 + p1.x, y0 + p1.y );
                
                Point p = p1;
                p1 = p0;
                p0 = p;
            }
            
            tc.setColor( Color.BLACK );
            tc.setStroke( new BasicStroke( 2 ) );
            
            track.getWaypointPosition( false, n - 1, scale, p0 );
            for ( int i = 0; i < n - 1; i++ )
            {
                track.getWaypointPosition( false, i, scale, p1 );
                
                tc.drawLine( x0 + p0.x, y0 + p0.y, x0 + p1.x, y0 + p1.y );
                
                Point p = p1;
                p1 = p0;
                p0 = p;
            }
            
            n = track.getNumWaypoints( true );
            
            tc.setColor( Color.WHITE );
            tc.setStroke( oldStroke );
            
            track.getWaypointPosition( true, 0, scale, p0 );
            byte s0 = track.getWaypointSector( true, 0 );
            for ( int i = 1; i < n; i++ )
            {
                track.getWaypointPosition( true, i, scale, p1 );
                byte s1 = track.getWaypointSector( true, i );
                
                if ( s0 == s1 )
                    tc.drawLine( x0 + p0.x, y0 + p0.y, x0 + p1.x, y0 + p1.y );
                
                Point p = p1;
                p1 = p0;
                p0 = p;
            }
            
            tc.setStroke( oldStroke );
        }
        else
        {
            scale = 1f;
            texture2 = null;
        }
        
        for ( int i = 0; i < maxDisplayedVehicles.getIntValue(); i++ )
        {
            itemStates[i] = -1;
        }
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( texture2 == null )
            texture.clear( offsetX, offsetY, width, height, true, null );
        else
            texture.clear( texture2, offsetX, offsetY, width, height, true, null );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( ( track != null ) && ( texture2 != null ) )
        {
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
                    float lapDistance = ( vsi.getLapDistance() + vsi.getScalarVelocity() * scoringInfo.getExtrapolationTime() ) % track.getTrackLength();
                    
                    TransformableTexture tt = itemTextures[i];
                    itemTextures[i].setVisible( true );
                    int itemState = vsi.getPlace();
                    
                    if ( track.getInterpolatedPosition( vsi.isInPits(), lapDistance, scale, position ) )
                    {
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
                        
                        tt.setTranslation( off2 + position.x, off2 + position.y );
                        
                        if ( itemStates[i] != itemState )
                        {
                            itemStates[i] = itemState;
                            
                            StandardWidgetSet.drawPositionItem( tt.getTexture(), 0, 0, itemRadius, vsi.getPlace(), color, true, font, posNumberFontAntiAliased, getFontColor() );
                        }
                    }
                }
            }
            
            for ( int i = n; i < maxDisplayedVehicles.getIntValue(); i++ )
                itemTextures[i].setVisible( false );
        }
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
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    public MapWidget( String name )
    {
        super( name, 14.5f, 10.3f );
        
        getBackgroundColorProperty().setColor( (String)null );
        
        getFontProperty().setFont( StandardWidgetSet.POSITION_ITEM_FONT_NAME );
        getFontColorProperty().setColor( StandardWidgetSet.POSITION_ITEM_FONT_COLOR_NAME );
    }
}
