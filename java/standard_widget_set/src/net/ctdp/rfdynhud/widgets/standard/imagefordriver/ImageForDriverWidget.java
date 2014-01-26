/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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
package net.ctdp.rfdynhud.widgets.standard.imagefordriver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.FilenameProperty;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.standard.image.ImageWidget;

import org.jagatoo.logging.LogLevel;
import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * This is an {@link ImageWidget} extension, that selects an individual image for each driver.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImageForDriverWidget extends ImageWidget
{
    private final FilenameProperty iniFile = new FilenameProperty( "iniFile", "data/images_for_driver.ini", new String[] { "ini" }, new String[] { "ini-files" } );
    
    private ImagePropertyWithTexture imageName2 = null;
    private String defaultImageName = null;
    
    private final Map<String, String> mappings = new HashMap<String, String>();
    
    private int currentViewedDriverId = -1;
    
    public ImageForDriverWidget()
    {
        super( StandardWidgetSet.WIDGET_PACKAGE_EXTRA );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( iniFile, "Name of the ini file to load mappings from." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( iniFile ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addProperty( iniFile );
    }
    
    private void updateImage( VehicleScoringInfo viewedVSI )
    {
        if ( viewedVSI.getDriverId() != currentViewedDriverId )
        {
            currentViewedDriverId = viewedVSI.getDriverId();
            
            String mapping = mappings.get( viewedVSI.getDriverName( true ).trim() );
            if ( ( mapping == null ) && ( viewedVSI.getVehicleInfo() != null ) )
                mapping = mappings.get( viewedVSI.getVehicleInfo().getDriverName().toUpperCase() );
            
            if ( mapping == null )
                //mapping = imageName2.getDefaultValue();
                mapping = defaultImageName;
            
            if ( !mapping.equals( imageName2.getImageName() ) )
            {
                imageName2.setImageName( mapping );
            }
        }
    }
    
    @Override
    protected Boolean onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        if ( imageName2 != null )
        {
            updateImage( viewedVSI );
        }
        
        return ( result );
    }
    
    private void loadMappings()
    {
        mappings.clear();
        
        try
        {
            new AbstractIniParser()
            {
                private String basePath = "";
                
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( group == null )
                    {
                    }
                    else if ( group.equalsIgnoreCase( "Meta" ) )
                    {
                        if ( key.equalsIgnoreCase( "basePath" ) )
                        {
                            basePath = value;
                            if ( !basePath.endsWith( "/" ) )
                                basePath += "/";
                        }
                    }
                    else if ( group.equalsIgnoreCase( "Mappings" ) )
                    {
                        mappings.put( key.toUpperCase(), basePath + value );
                    }
                    
                    return ( true );
                }
            }.parse( iniFile.getFileValue() );
        }
        catch ( FileNotFoundException e )
        {
            log( LogLevel.EXCEPTION, "Cannot find file \"" + iniFile.getFileValue().getAbsolutePath() + "\"." );
        }
        catch ( Throwable t )
        {
            log( t );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        loadMappings();
        
        if ( imageName2 == null )
        {
            try
            {
                Field f = ImageWidget.class.getDeclaredField( "imageName" );
                boolean wasAccessible = f.isAccessible();
                f.setAccessible( true );
                imageName2 = (ImagePropertyWithTexture)f.get( this );
                f.setAccessible( wasAccessible );
                defaultImageName = imageName2.getImageName();
            }
            catch ( Throwable t )
            {
                log( t );
            }
            
            updateImage( gameData.getScoringInfo().getViewedVehicleScoringInfo() );
        }
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw )
        {
            imageName2.updateSize( width, height, isEditorMode );
        }
        
        super.drawWidget( clock, needsCompleteRedraw, gameData, isEditorMode, texture, offsetX, offsetY, width, height );
    }
}
