/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.ctdp.rfdynhud.editor.util.DefaultPropertyWriter;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.GameResolution;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.ListProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertiesKeeper;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.util.TextureManager;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

import org.jagatoo.util.Tools;

/**
 * Grid and rail settings for the {@link WidgetsEditorPanel}.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetsEditorPanelSettings implements PropertiesKeeper
{
    private final WidgetsConfiguration widgetsConfig;
    private final GameResolution gameResolution;
    
    private final RFDynHUDEditor editor;
    private final WidgetsEditorPanel editorPanel;
    
    private static final String DEFAULT_SCREENSHOT_SET = "CTDPF106_Fer_T-Cam";
    private String screenshotSet = DEFAULT_SCREENSHOT_SET;
    
    private boolean bgImageReloadSuppressed = false;
    
    public void setBGImageReloadSuppressed( boolean suppressed )
    {
        this.bgImageReloadSuppressed = suppressed;
    }
    
    public final File getScreenshotSetFolder()
    {
        File backgroundsFolder = new File( GameFileSystem.INSTANCE.getEditorFolder(), "backgrounds" );
        
        return ( new File( backgroundsFolder, screenshotSet ) );
    }
    
    public void setScreenshotSet( String screenshotSet )
    {
        File backgroundsFolder = new File( GameFileSystem.INSTANCE.getEditorFolder(), "backgrounds" );
        
        if ( !backgroundsFolder.exists() )
        {
            this.screenshotSet = DEFAULT_SCREENSHOT_SET;
            return;
        }
        
        File folder = new File( backgroundsFolder, screenshotSet );
        if ( folder.exists() && folder.isDirectory() )
        {
            this.screenshotSet = screenshotSet;
        }
        else
        {
            if ( new File( backgroundsFolder, DEFAULT_SCREENSHOT_SET ).exists() )
            {
                this.screenshotSet = DEFAULT_SCREENSHOT_SET;
            }
            else
            {
                for ( File f : backgroundsFolder.listFiles() )
                {
                    if ( f.isDirectory() )
                    {
                        this.screenshotSet = f.getName();
                        return;
                    }
                }
            }
            
            this.screenshotSet = DEFAULT_SCREENSHOT_SET;
        }
    }
    
    public final String getScreenshotSet()
    {
        return ( screenshotSet );
    }
    
    private List<String> getScreenshotSets()
    {
        ArrayList<String> list = new ArrayList<String>();
        
        File root = new File( GameFileSystem.INSTANCE.getEditorFolder(), "backgrounds" );
        for ( File f : root.listFiles() )
        {
            if ( f.isDirectory() && !f.getName().toLowerCase().equals( ".svn" ) )
            {
                list.add( f.getAbsolutePath().substring( root.getAbsolutePath().length() + 1 ) );
            }
        }
        
        return ( list );
    }
    
    public final File getBackgroundImageFile( int width, int height )
    {
        return ( new File( getScreenshotSetFolder(), File.separator + "background_" + width + "x" + height + ".jpg" ) );
    }
    
    private static BufferedImage createFallbackImage( int width, int height, String message )
    {
        BufferedImage bi = TextureManager.createMissingImage( width, height );
        
        Graphics2D g = bi.createGraphics();
        
        g.setColor( Color.RED );
        g.setFont( new java.awt.Font( "Verdana", java.awt.Font.BOLD, 14 ) );
        g.drawString( message, 50, 50 );
        
        return ( bi );
    }
    
    public BufferedImage loadBackgroundImage( int width, int height )
    {
        BufferedImage result = null;
        
        File file = getBackgroundImageFile( width, height );
        
        if ( file.exists() && file.isFile() )
        {
            try
            {
                result = ImageIO.read( file );
            }
            catch ( IOException e )
            {
                RFDHLog.error( "Unable to read background image file \"" + file.getAbsolutePath() + "\"" );
                RFDHLog.error( e );
                
                //result = null;
            }
        }
        
        if ( result == null )
        {
            String message = "Background image not found: \"" + file.getAbsolutePath() + "\"!";
            
            RFDHLog.error( message );
            
            result = createFallbackImage( width, height, message );
        }
        
        return ( result );
    }
    
    public final BufferedImage loadBackgroundImage()
    {
        return ( loadBackgroundImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() ) );
    }
    
    public boolean checkResolution( int resX, int resY )
    {
        return ( getBackgroundImageFile( resX, resY ).exists() );
    }
    
    public void switchScreenshotSet( String screenshotSet )
    {
        RFDHLog.printlnEx( "Switching to Screenshot Set \"" + screenshotSet + "\"..." );
        
        setScreenshotSet( screenshotSet );
        editorPanel.switchToGameResolution( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() );
        if ( editor != null )
            editor.getMenuBar().setNeedsDMCheck();
    }
    
    private final IntProperty railDistanceX = new IntProperty( "railDistanceX", 10, 0, 100 );
    private final IntProperty railDistanceY = new IntProperty( "railDistanceY", 10, 0, 100 );
    private final IntProperty maxRailDistance = new IntProperty( "maxRailDistance", 250, 0, 99999 );
    
    private final BooleanProperty drawGrid = new BooleanProperty( "drawGrid", false )
    {
        @Override
        protected void onValueChanged( Boolean oldValue, boolean newValue )
        {
            if ( !bgImageReloadSuppressed && ( WidgetsEditorPanelSettings.this.widgetsConfig != null ) )
                editorPanel.setBackgroundImage( loadBackgroundImage() );
        }
    };
    
    private final IntProperty gridOffsetX = new IntProperty( "gridOffsetX", 0 )
    {
        @Override
        protected void onValueChanged( Integer oldValue, int newValue )
        {
            if ( !bgImageReloadSuppressed && ( WidgetsEditorPanelSettings.this.widgetsConfig != null ) )
                editorPanel.setBackgroundImage( loadBackgroundImage() );
        }
    };
    
    private final IntProperty gridOffsetY = new IntProperty( "gridOffsetY", 0 )
    {
        @Override
        protected void onValueChanged( Integer oldValue, int newValue )
        {
            if ( !bgImageReloadSuppressed && ( WidgetsEditorPanelSettings.this.widgetsConfig != null ) )
                editorPanel.setBackgroundImage( loadBackgroundImage() );
        }
    };
    
    private final IntProperty gridSizeX = new IntProperty( "gridSizeX", 10, 0, 5000 )
    {
        @Override
        protected void onValueChanged( Integer oldValue, int newValue )
        {
            if ( !bgImageReloadSuppressed && ( WidgetsEditorPanelSettings.this.widgetsConfig != null ) )
                editorPanel.setBackgroundImage( loadBackgroundImage() );
        }
    };
    
    private final IntProperty gridSizeY = new IntProperty( "gridSizeY", 10, 0, 5000 )
    {
        @Override
        protected void onValueChanged( Integer oldValue, int newValue )
        {
            if ( !bgImageReloadSuppressed && ( WidgetsEditorPanelSettings.this.widgetsConfig != null ) )
                editorPanel.setBackgroundImage( loadBackgroundImage() );
        }
    };
    
    public void setDrawGrid( boolean drawGrid )
    {
        this.drawGrid.setBooleanValue( drawGrid );
    }
    
    public final int getRailDistanceX()
    {
        return ( railDistanceX.getIntValue() );
    }
    
    public final int getRailDistanceY()
    {
        return ( railDistanceY.getIntValue() );
    }
    
    public final int getMaxRailDistance()
    {
        return ( maxRailDistance.getIntValue() );
    }
    
    public final boolean getDrawGrid()
    {
        return ( drawGrid.getBooleanValue() );
    }
    
    public final int getGridOffsetX()
    {
        return ( gridOffsetX.getIntValue() );
    }
    
    public final int getGridOffsetY()
    {
        return ( gridOffsetY.getIntValue() );
    }
    
    public final int getGridSizeX()
    {
        return ( gridSizeX.getIntValue() );
    }
    
    public final int getGridSizeY()
    {
        return ( gridSizeY.getIntValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPropertyChanged( Property property, Object oldValue, Object newValue )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        ( (DefaultPropertyWriter)writer ).getIniWriter().writeSetting( "screenshotSet", getScreenshotSet() );
        
        writer.writeProperty( railDistanceX, null );
        writer.writeProperty( railDistanceY, null );
        writer.writeProperty( maxRailDistance, null );
        writer.writeProperty( drawGrid, null );
        writer.writeProperty( gridOffsetX, null );
        writer.writeProperty( gridOffsetY, null );
        writer.writeProperty( gridSizeX, null );
        writer.writeProperty( gridSizeY, null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        bgImageReloadSuppressed = true;
        
        if ( loader.loadProperty( railDistanceX ) );
        else if ( loader.loadProperty( railDistanceY ) );
        else if ( loader.loadProperty( maxRailDistance ) );
        else if ( loader.loadProperty( drawGrid ) );
        else if ( loader.loadProperty( gridOffsetX ) );
        else if ( loader.loadProperty( gridOffsetY ) );
        else if ( loader.loadProperty( gridSizeX ) );
        else if ( loader.loadProperty( gridSizeY ) );
        
        bgImageReloadSuppressed = false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( new ListProperty<String, List<String>>( "screenshotSet", getScreenshotSet(), getScreenshotSets() )
        {
            @Override
            public void setValue( Object value )
            {
                if ( !Tools.objectsEqual( value, getScreenshotSet() ) )
                    switchScreenshotSet( (String)value );
            }
            
            @Override
            public String getValue()
            {
                return ( getScreenshotSet() );
            }
        } );
                               
        propsCont.addProperty( railDistanceX );
        propsCont.addProperty( railDistanceY );
        propsCont.addProperty( maxRailDistance );
        propsCont.addProperty( drawGrid );
        propsCont.addProperty( gridOffsetX );
        propsCont.addProperty( gridOffsetY );
        propsCont.addProperty( gridSizeX );
        propsCont.addProperty( gridSizeY );
    }
    
    public final boolean isGridUsed()
    {
        return ( drawGrid.getBooleanValue() && ( this.gridSizeX.getIntValue() > 1 ) && ( this.gridSizeY.getIntValue() > 1 ) );
    }
    
    public final int snapXToGrid( int x )
    {
        if ( !isGridUsed() )
            return ( x );
        
        return ( gridOffsetX.getIntValue() + Math.min( Math.round( ( x - gridOffsetX.getIntValue() ) / (float)gridSizeX.getIntValue() ) * gridSizeX.getIntValue(), widgetsConfig.getGameResolution().getViewportWidth() - 1 ) );
    }
    
    public final int snapYToGrid( int y )
    {
        if ( !isGridUsed() )
            return ( y );
        
        return ( gridOffsetY.getIntValue() + Math.min( Math.round( ( y - gridOffsetY.getIntValue() ) / (float)gridSizeY.getIntValue() ) * gridSizeY.getIntValue(), widgetsConfig.getGameResolution().getViewportHeight() - 1 ) );
    }
    
    public void snapWidgetToGrid( Widget widget )
    {
        if ( !isGridUsed() )
            return;
        
        int x = widget.getPosition().getEffectiveX();
        int y = widget.getPosition().getEffectiveY();
        int w = widget.getSize().getEffectiveWidth();
        int h = widget.getSize().getEffectiveHeight();
        
        x = snapXToGrid( x );
        y = snapYToGrid( y );
        w = snapXToGrid( x + w - 1 ) + 1 - x;
        h = snapYToGrid( y + h - 1 ) + 1 - y;
        
        widget.getSize().setEffectiveSize( w, h );
        widget.getPosition().setEffectivePosition( x, y );
    }
    
    public void snapAllWidgetsToGrid()
    {
        final int n = widgetsConfig.getNumWidgets();
        Widget[] widgets = new Widget[ n ];
        for ( int i = 0; i < n; i++ )
            widgets[i] = widgetsConfig.getWidget( i );
        
        for ( int i = 0; i < n; i++ )
            snapWidgetToGrid( widgets[i] );
    }
    
    public WidgetsEditorPanelSettings( WidgetsConfiguration widgetsConfig, RFDynHUDEditor editor, WidgetsEditorPanel editorPanel )
    {
        this.widgetsConfig = widgetsConfig;
        this.gameResolution = widgetsConfig.getGameResolution();
        this.editor = editor;
        this.editorPanel = editorPanel;
    }
}
