package net.ctdp.rfdynhud.widgets.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.FontProperty;
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
import net.ctdp.rfdynhud.widgets._util.FontUtils;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.openmali.vecmath2.util.ColorUtils;

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
    
    private boolean antialiasPositions = true;
    
    private String markColorNormalKey = "#FFFFFFC0";
    private Color markColorNormal = null;
    private String markColorLeaderKey = "#FF0000C0";
    private Color markColorLeader = null;
    private String markColorMeKey = "#00FF00C0";
    private Color markColorMe = null;
    private String markColorNextInFrontKey = "#0000FFC0";
    private Color markColorNextInFront = null;
    private String markColorNextBehindKey = "#FFFF00C0";
    private Color markColorNextBehind = null;
    
    private int maxDisplayedVehicles = 22;
    
    private boolean displayPositionNumbers = true;
    
    private String posNumberFontKey = "Monospaced-PLAIN-9v";
    private Font posNumberFont = null;
    
    private String posNumberFontColorKey = "#000000";
    private Color posNumberFontColor = null;
    
    private static final int ANTI_ALIAS_RADIUS_OFFSET = 1;
    
    private TransformableTexture[] itemTextures = null;
    private int[] itemStates = null;
    
    private final Point position = new Point();
    
    public void setItemRadius( int radius )
    {
        this.baseItemRadius = radius;
        
        forceAndSetDirty();
    }
    
    public final int getItemRadius()
    {
        return ( baseItemRadius );
    }
    
    public void setMarkColorNormal( String color )
    {
        this.markColorNormalKey = color;
        this.markColorNormal = null;
        
        forceAndSetDirty();
    }
    
    public final void setMarkColorNormal( Color color )
    {
        setMarkColorNormal( ColorUtils.colorToHex( color ) );
    }
    
    public final void setMarkColorNormal( int red, int green, int blue )
    {
        setMarkColorNormal( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getMarkColorNormal()
    {
        markColorNormal = ColorProperty.getColorFromColorKey( markColorNormalKey, markColorNormal, getConfiguration() );
        
        return ( markColorNormal );
    }
    
    public void setMarkColorLeader( String color )
    {
        this.markColorLeaderKey = color;
        this.markColorLeader = null;
        
        forceAndSetDirty();
    }
    
    public final void setMarkColorLeader( Color color )
    {
        setMarkColorLeader( ColorUtils.colorToHex( color ) );
    }
    
    public final void setMarkColorLeader( int red, int green, int blue )
    {
        setMarkColorLeader( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getMarkColorLeader()
    {
        markColorLeader = ColorProperty.getColorFromColorKey( markColorLeaderKey, markColorLeader, getConfiguration() );
        
        return ( markColorLeader );
    }
    
    public void setMarkColorMe( String color )
    {
        this.markColorMeKey = color;
        this.markColorMe = null;
        
        forceAndSetDirty();
    }
    
    public final void setMarkColorMe( Color color )
    {
        setMarkColorMe( ColorUtils.colorToHex( color ) );
    }
    
    public final void setMarkColorMe( int red, int green, int blue )
    {
        setMarkColorMe( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getMarkColorMe()
    {
        markColorMe = ColorProperty.getColorFromColorKey( markColorMeKey, markColorMe, getConfiguration() );
        
        return ( markColorMe );
    }
    
    public void setMarkColorNextInFront( String color )
    {
        this.markColorNextInFrontKey = color;
        this.markColorNextInFront = null;
        
        forceAndSetDirty();
    }
    
    public final void setMarkColorNextInFront( Color color )
    {
        setMarkColorNextInFront( ColorUtils.colorToHex( color ) );
    }
    
    public final void setMarkColorNextInFront( int red, int green, int blue )
    {
        setMarkColorNextInFront( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getMarkColorNextInFront()
    {
        markColorNextInFront = ColorProperty.getColorFromColorKey( markColorNextInFrontKey, markColorNextInFront, getConfiguration() );
        
        return ( markColorNextInFront );
    }
    
    public void setMarkColorNextBehind( String color )
    {
        this.markColorNextBehindKey = color;
        this.markColorNextBehind = null;
        
        forceAndSetDirty();
    }
    
    public final void setMarkColorNextBehind( Color color )
    {
        setMarkColorNextBehind( ColorUtils.colorToHex( color ) );
    }
    
    public final void setMarkColorNextBehind( int red, int green, int blue )
    {
        setMarkColorNextBehind( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getMarkColorNextBehind()
    {
        markColorNextBehind = ColorProperty.getColorFromColorKey( markColorNextBehindKey, markColorNextBehind, getConfiguration() );
        
        return ( markColorNextBehind );
    }
    
    public void setMaxDisplayedVehicles( int number )
    {
        this.maxDisplayedVehicles = Math.max( 1, Math.min( number, 50 ) );
        
        forceAndSetDirty();
    }
    
    public final int getMaxDisplayedVehicles()
    {
        return ( maxDisplayedVehicles );
    }
    
    public void setDisplayPositionNumbers( boolean display )
    {
        this.displayPositionNumbers = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayPositionNumbers()
    {
        return ( displayPositionNumbers );
    }
    
    public void setPosNumberFont( String font )
    {
        this.posNumberFontKey = font;
        this.posNumberFont = null;
        
        forceAndSetDirty();
    }
    
    public final void setPosNumberFont( Font font, boolean virtual )
    {
        setPosNumberFont( FontUtils.getFontString( font, virtual ) );
    }
    
    public final Font getPosNumberFont()
    {
        posNumberFont = FontProperty.getFontFromFontKey( posNumberFontKey, posNumberFont, getConfiguration() );
        
        return ( posNumberFont );
    }
    
    public void setPosNumberFontColor( String color )
    {
        this.posNumberFontColorKey = color;
        this.posNumberFontColor = null;
        
        forceAndSetDirty();
    }
    
    public final void setPosNumberFontColor( Color color )
    {
        setPosNumberFontColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setPosNumberFontColor( int red, int green, int blue )
    {
        setPosNumberFontColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getPosNumberFontColor()
    {
        posNumberFontColor = ColorProperty.getColorFromColorKey( posNumberFontColorKey, posNumberFontColor, getConfiguration() );
        
        return ( posNumberFontColor );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTrackChanged( String trackname, LiveGameData gameData )
    {
        track = null;
    }
    
    private void initSubTextures()
    {
        itemRadius = Math.round( baseItemRadius * getConfiguration().getGameResY() / 960f );
        
        if ( ( itemTextures != null ) && ( itemTextures.length == getMaxDisplayedVehicles() ) && ( itemTextures[0].getWidth() == itemRadius + itemRadius ) && ( itemTextures[0].getHeight() == itemRadius + itemRadius ) )
            return;
        
        itemTextures = new TransformableTexture[ getMaxDisplayedVehicles() ];
        itemStates = new int[ getMaxDisplayedVehicles() ];
        
        for ( int i = 0; i < getMaxDisplayedVehicles(); i++ )
        {
            itemTextures[i] = new TransformableTexture( itemRadius + itemRadius, itemRadius + itemRadius, 0, 0, 0, 0, 0f, 1f, 1f );
            itemTextures[i].setVisible( false );
            
            if ( antialiasPositions )
                itemTextures[i].getTexture().getTextureCanvas().setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            else
                itemTextures[i].getTexture().getTextureCanvas().setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
            
            itemStates[i] = -1;
        }
    }
    
    @Override
    public TransformableTexture[] getSubTextures( boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
    {
        initSubTextures();
        
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
        
        initSubTextures();
        
        if ( ( track != null ) && ( track.getNumWaypoints( false ) > 0 ) )
        {
            if ( ( texture == null ) || ( texture.getUsedWidth() != width ) || ( texture.getUsedHeight() != height ) )
            {
                texture = TextureImage2D.createOfflineTexture( width, height, true );
            }
            
            texture.clear( true, null );
            
            Texture2DCanvas tc = texture.getTextureCanvas();
            tc.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            
            int off2 = ( antialiasPositions ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
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
    
    /*
    private void clearPosition( Point p, TextureImage2D image, int offsetX, int offsetY, int width, int height )
    {
        int off2X = ( antialiasPositions ? ANTI_ALIAS_RADIUS_OFFSET : 0 ) + itemRadius;// + ( ( width - itemRadius - itemRadius - track.getXExtend( scale ) ) / 2 );
        int off2Y = ( antialiasPositions ? ANTI_ALIAS_RADIUS_OFFSET : 0 ) + itemRadius;// + ( ( width - itemRadius - itemRadius - track.getZExtend( scale ) ) / 2 );
        
        int x0 = p.x - itemRadius - off2X;
        int y0 = p.y - itemRadius - off2Y;
        int x1 = x0 + itemRadius + itemRadius + off2X + off2X - 1;
        int y1 = y0 + itemRadius + itemRadius + off2Y + off2Y - 1;
        
        x0 = Math.max( -off2X, x0 );
        y0 = Math.max( -off2Y, y0 );
        x1 = Math.min( x1, width + off2X - 1 );
        y1 = Math.min( y1, height + off2Y - 1 );
        
        int w = x1 - x0 + 1;
        int h = y1 - y0 + 1;
        
        image.clear( texture, off2X + x0, off2Y + y0, w, h, offsetX + off2X + x0, offsetY + off2Y + y0, true, null );
        //image.clear( Color.RED, offsetX + off2X + x0, offsetY + off2Y + y0, w, h, true, null );
    }
    */
    
    @Override
    public void drawWidget( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, boolean needsCompleteRedraw )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( ( track != null ) && ( texture != null ) )
        {
            int off2 = ( antialiasPositions ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
            
            short ownPlace = scoringInfo.getOwnPlace();
            
            Font font = getPosNumberFont();
            FontMetrics metrics = texCanvas.getFontMetrics( font );
            
            boolean normal = false;
            int n = Math.min( scoringInfo.getNumVehicles(), getMaxDisplayedVehicles() );
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
                            tt.getTextureCanvas().setColor( getMarkColorLeader() );
                            normal = false;
                        }
                        else if ( vsi.isPlayer() )
                        {
                            itemState |= 2 << 16;
                            tt.getTextureCanvas().setColor( getMarkColorMe() );
                            normal = false;
                        }
                        else if ( vsi.getPlace() == ownPlace + 1 )
                        {
                            itemState |= 3 << 16;
                            tt.getTextureCanvas().setColor( getMarkColorNextInFront() );
                            normal = false;
                        }
                        else if ( vsi.getPlace() == ownPlace - 1 )
                        {
                            itemState |= 4 << 16;
                            tt.getTextureCanvas().setColor( getMarkColorNextBehind() );
                            normal = false;
                        }
                        else if ( !normal )
                        {
                            itemState |= 5 << 16;
                            tt.getTextureCanvas().setColor( getMarkColorNormal() );
                            normal = true;
                        }
                        
                        tt.setTranslation( off2 + position.x, off2+ position.y );
                        
                        if ( itemStates[i] != itemState )
                        {
                            itemStates[i] = itemState;
                            
                            tt.getTexture().clear( true, null );
                            
                            tt.getTextureCanvas().fillArc( 0, 0, itemRadius + itemRadius, itemRadius + itemRadius, 0, 360 );
                            
                            if ( getDisplayPositionNumbers() )
                            {
                                String posStr = String.valueOf( vsi.getPlace() );
                                Rectangle2D bounds = metrics.getStringBounds( posStr, tt.getTextureCanvas() );
                                float fw = (float)bounds.getWidth();
                                float fh = (float)( metrics.getAscent() - metrics.getDescent() );
                                
                                tt.getTexture().drawString( posStr, itemRadius - (int)( fw / 2 ), itemRadius + (int)( fh / 2 ), bounds, font, getPosNumberFontColor(), false, null );
                            }
                            
                            if ( isEditorMode )
                            {
                                tt.drawInEditor( texCanvas, offsetX, offsetY );
                            }
                        }
                    }
                }
            }
            
            for ( int i = n; i < getMaxDisplayedVehicles(); i++ )
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
        writer.writeProperty( "markColorNormal", markColorNormalKey, "The color used for all, but special cars in #RRGGBBAA (hex)." );
        writer.writeProperty( "markColorLeader", markColorLeaderKey, "The color used for the leader's car in #RRGGBBAA (hex)." );
        writer.writeProperty( "markColorMe", markColorMeKey, "The color used for your own car in #RRGGBBAA (hex)." );
        writer.writeProperty( "markColorNextInFront", markColorNextInFrontKey, "The color used for the car in front of you in #RRGGBBAA (hex)." );
        writer.writeProperty( "markColorNextBehind", markColorNextBehindKey, "The color used for the car behind you in #RRGGBBAA (hex)." );
        writer.writeProperty( "maxDisplayedVehicles", getMaxDisplayedVehicles(), "The maximum number of displayed vehicles." );
        writer.writeProperty( "displayPosNumbers", getDisplayPositionNumbers(), "Display numbers on the position markers?" );
        writer.writeProperty( "posNumberFont", posNumberFontKey, "The font used for position numbers." );
        writer.writeProperty( "posNumberFontColor", posNumberFontColorKey, "The font color used for position numbers in the format #RRGGBB (hex)." );
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
        
        else if ( key.equals( "markColorNormal" ) )
            this.markColorNormalKey = value;
        
        else if ( key.equals( "markColorLeader" ) )
            this.markColorLeaderKey = value;
        
        else if ( key.equals( "markColorMe" ) )
            this.markColorMeKey = value;
        
        else if ( key.equals( "markColorNextInFront" ) )
            this.markColorNextInFrontKey = value;
        
        else if ( key.equals( "markColorNextBehind" ) )
            this.markColorNextBehindKey = value;
        
        else if ( key.equals( "maxDisplayedVehicles" ) )
            this.maxDisplayedVehicles = Integer.parseInt( value );
        
        else if ( key.equals( "displayPosNumbers" ) )
            this.displayPositionNumbers = Boolean.parseBoolean( value );
        
        else if ( key.equals( "posNumberFont" ) )
            this.posNumberFontKey = value;
        
        else if ( key.equals( "posNumberFontColor" ) )
            this.posNumberFontColorKey = value;
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
        
        props.add( new ColorProperty( "markColorNormal", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setMarkColorNormal( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( markColorNormalKey );
            }
        } );
        
        props.add( new ColorProperty( "markColorLeader", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setMarkColorLeader( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( markColorLeaderKey );
            }
        } );
        
        props.add( new ColorProperty( "markColorMe", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setMarkColorMe( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( markColorMeKey );
            }
        } );
        
        props.add( new ColorProperty( "markColorNextInFront", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setMarkColorNextInFront( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( markColorNextInFrontKey );
            }
        } );
        
        props.add( new ColorProperty( "markColorNextBehind", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setMarkColorNextBehind( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( markColorNextBehindKey );
            }
        } );
        
        props.add( new Property( "maxDisplayedVehicles", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setMaxDisplayedVehicles( (Integer)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getMaxDisplayedVehicles() );
            }
        } );
        
        props.add( new Property( "displayPosNumbers", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayPositionNumbers( (Boolean)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayPositionNumbers() );
            }
        } );
        
        props.add( new FontProperty( "posNumberFont", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setPosNumberFont( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( posNumberFontKey );
            }
        } );
        
        props.add( new ColorProperty( "posNumberFontColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setPosNumberFontColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( posNumberFontColorKey );
            }
        } );
        
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
        
        setBackgroundColor( (String)null );
    }
}
