package net.ctdp.rfdynhud.widgets.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.BooleanProperty;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.FontProperty;
import net.ctdp.rfdynhud.editor.properties.IntegerProperty;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.RFactorTools;
import net.ctdp.rfdynhud.util.Track;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link MapWidget} renders a map overview of the current track.
 * 
 * @author Marvin Froehlich
 */
public class MapWidget extends Widget
{
    private TextureImage2D texture = null;
    private Track track = null;
    private float scale = 1f;
    private int baseItemRadius = 7;
    private int itemRadius = baseItemRadius;
    
    private final ColorProperty markColorNormal = new ColorProperty( this, "markColorNormal", "#FFFFFFC0" );
    private final ColorProperty markColorLeader = new ColorProperty( this, "markColorLeader", "#FF0000C0" );
    private final ColorProperty markColorMe = new ColorProperty( this, "markColorMe", "#00FF00C0" );
    private final ColorProperty markColorNextInFront = new ColorProperty( this, "markColorNextInFront", "#0000FFC0" );
    private final ColorProperty markColorNextBehind = new ColorProperty( this, "markColorNextBehind", "#FFFF00C0" );
    
    private final IntegerProperty maxDisplayedVehicles = new IntegerProperty( this, "maxDisplayedVehicles", "maxDisplayedVehicles", 22, 1, 50, false );
    
    private final BooleanProperty displayPositionNumbers = new BooleanProperty( this, "displayPosNumbers", true );
    
    private final FontProperty posNumberFont = new FontProperty( this, "posNumberFont", "Monospaced-PLAIN-9va" );
    private final ColorProperty posNumberFontColor = new ColorProperty( this, "posNumberFontColor", "#000000" );
    
    private static final int ANTI_ALIAS_RADIUS_OFFSET = 1;
    
    private TransformableTexture[] itemTextures = null;
    private int[] itemStates = null;
    
    private final Point2D.Float position = new Point2D.Float();
    
    public void setItemRadius( int radius )
    {
        this.baseItemRadius = radius;
        
        forceAndSetDirty();
    }
    
    public final int getItemRadius()
    {
        return ( baseItemRadius );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTrackChanged( String trackname, LiveGameData gameData )
    {
        track = null;
    }
    
    private void initSubTextures( boolean isEditorMode )
    {
        final int maxDspVehicles = this.maxDisplayedVehicles.getIntegerValue();
        
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
    public TransformableTexture[] getSubTextures( boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
    {
        initSubTextures( isEditorMode );
        
        return ( itemTextures );
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
    protected boolean checkForChanges( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        if ( track == null )
        {
            Track track;
            if ( isEditorMode )
            {
                File sceneFolder = RFactorTools.getLastUsedTrackFile( null );
                if ( sceneFolder == null )
                    track = null;
                else
                    track = gameData.getTrack( sceneFolder.getParentFile() );
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
        
        if ( ( track != null ) && ( track.getNumWaypoints( false ) > 0 ) )
        {
            if ( ( texture == null ) || ( texture.getUsedWidth() != width ) || ( texture.getUsedHeight() != height ) )
            {
                texture = TextureImage2D.createOfflineTexture( width, height, true );
            }
            
            texture.clear( true, null );
            
            Texture2DCanvas tc = texture.getTextureCanvas();
            tc.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            
            int off2 = ( posNumberFont.isAntiAliased() ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
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
            texture = null;
        }
    }
    
    @Override
    protected void clearBackground( boolean isEditorMode, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        if ( texture == null )
            texCanvas.getImage().clear( offsetX, offsetY, width, height, true, null );
        else
            texCanvas.getImage().clear( texture, offsetX, offsetY, width, height, true, null );
    }
    
    @Override
    public void drawWidget( boolean isEditorMode, boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( ( track != null ) && ( texture != null ) )
        {
            int off2 = ( posNumberFont.isAntiAliased() ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
            
            short ownPlace = scoringInfo.getOwnPlace();
            
            final Font font = posNumberFont.getFont();
            final boolean posNumberFontAntiAliased = posNumberFont.isAntiAliased();
            FontMetrics metrics = texCanvas.getFontMetrics( font );
            
            boolean normal = false;
            int n = Math.min( scoringInfo.getNumVehicles(), maxDisplayedVehicles.getIntegerValue() );
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
                        
                        tt.setTranslation( off2 + position.x, off2 + position.y );
                        
                        if ( itemStates[i] != itemState )
                        {
                            itemStates[i] = itemState;
                            
                            tt.getTexture().clear( true, null );
                            
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
                        
                        if ( isEditorMode )
                        {
                            tt.drawInEditor( texCanvas, offsetX, offsetY );
                        }
                    }
                }
            }
            
            for ( int i = n; i < maxDisplayedVehicles.getIntegerValue(); i++ )
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
        
        writer.writeProperty( "itemRadius", getItemRadius(), "The abstract radius for any displayed driver item." );
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
    public void getProperties( FlaggedList propsList )
    {
        super.getProperties( propsList );
        
        FlaggedList props = new FlaggedList( "Specific", true );
        
        props.add( new Property( "itemRadius", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setItemRadius( (Integer)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getItemRadius() );
            }
        } );
        
        props.add( markColorNormal );
        props.add( markColorLeader );
        props.add( markColorMe );
        props.add( markColorNextInFront );
        props.add( markColorNextBehind );
        
        props.add( maxDisplayedVehicles );
        
        props.add( displayPositionNumbers );
        props.add( posNumberFont );
        props.add( posNumberFontColor );
        
        propsList.add( props );
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
    
    public MapWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.145f, Size.PERCENT_OFFSET + 0.103f );
        
        getBackgroundColorProperty().setColor( (String)null );
    }
}
