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
package net.ctdp.rfdynhud.widgets.lessons.lesson3;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.lessons._util.LessonsWidgetSet;

/**
 * Of course you want your Widgets to be configurable. This is done through properties.
 * 
 * Some default properties are provided by the base Widget class. We will define our own properties here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Lesson3Widget_Properties extends Widget
{
    /*
     * This is a property definition for a color value.
     * It needs a name to be displayed in the editor and used in config files
     * and of course a default value.
     * 
     * See through the other constructors to learn about more possibilities
     * like giving separate names for config files and the editor.
     * 
     * There are properties for any type of value and a generic one for all other types.
     */
    private final ColorProperty coldColor = new ColorProperty( "coldColor", "#0000FF" );
    
    /*
     * This color uses a named color.
     * This Widget must implement the getDefaultNamedColorValue() method like below.
     */
    private final ColorProperty hotColor = new ColorProperty( "hotColor", LessonsWidgetSet.MY_FONT_COLOR_NAME );
    
    /*
     * This font uses a named font.
     * This Widget must implement the getDefaultNamedFontValue() method like below.
     */
    private final FontProperty myFont = new FontProperty( "myFont", LessonsWidgetSet.MY_FONT_NAME );
    
    /*
     * This is a simple selection on 'MyEnum'.
     */
    private final EnumProperty<MyEnum> myEnum = new EnumProperty<MyEnum>( "myEnum", MyEnum.ITEM0 );
    
    private DrawnString ds = null;
    private DrawnString ds2 = null;
    
    private final FloatValue v = new FloatValue( -1f, 0.1f );
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( LessonsWidgetSet.WIDGET_PACKAGE );
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
        
        result = LessonsWidgetSet.getDefaultNamedColorValue( name );
        
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
        
        result = LessonsWidgetSet.getDefaultNamedFontValue( name );
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        int h = TextureImage2D.getStringHeight( "XXX", getFontProperty() );
        
        ds = drawnStringFactory.newDrawnString( "ds", 0, 0, Alignment.LEFT, false, myFont.getFont(), myFont.isAntiAliased(), getFontColor() );
        ds2 = drawnStringFactory.newDrawnString( "ds2", 0, height - h, Alignment.LEFT, false, myFont.getFont(), myFont.isAntiAliased(), getFontColor() );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        v.update( gameData.getTelemetryData().getTireTemperature( Wheel.FRONT_LEFT ) );
        
        if ( needsCompleteRedraw || ( clock.c() && v.hasChanged() ) )
        {
            String tireTempFL = NumberUtil.formatFloat( v.getValue(), 1, true );
            
            /*
             * If the tire temperature is higher than 80°, we use our 'hotColor' value for the font.
             */
            Color color = getFontColor();
            if ( v.getValue() < 70.0f )
                color = coldColor.getColor();
            else if ( v.getValue() > 80.0f )
                color = hotColor.getColor();
            
            ds.draw( offsetX, offsetY, tireTempFL, color, texture );
        }
        
        if ( needsCompleteRedraw )
        {
            ds2.draw( offsetX, offsetY, myEnum.getEnumValue().getText(), texture );
        }
    }
    
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        /*
         * Here we write our properties to the configuration file in the given order.
         * We also add a short comment, which is written to the end of the ini line.
         */
        
        writer.writeProperty( coldColor, "The color for cold temperatures." );
        writer.writeProperty( hotColor, "The color for hot temperatures." );
        writer.writeProperty( myFont, "A font for our text." );
        writer.writeProperty( myEnum, "Some selection." );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        /*
         * Here we load our properties from the configuration file.
         * To do as less string compares as possible and to keep the loading code compact,
         * we use this trick with if/else if and a trailing semicolon.
		 * 
		 * So this code asks each listed property, if it claims owership over the property key,
		 * that is currently loaded through the provided PropertyLoader. And if and only if it does,
		 * the method finishes immediately without probing the following properties.
         */
        
        if ( loader.loadProperty( coldColor ) );
        else if ( loader.loadProperty( hotColor ) );
        else if ( loader.loadProperty( myFont ) );
        else if ( loader.loadProperty( myEnum ) );
    }
    
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        /*
         * To separate our own properties from the default ones we add a new group.
         */
        propsCont.addGroup( "My own Properties" );
        
        /*
         * Now we simply add our custom properties to the list of properties,
         * which is configurable in the editor.
         * 
         * Sometimes you may not want specific properties to appear in the editor
         * in certain situations (e.g. depending on other property values).
         * Then you must use the 'forceAll' parameter and only make sure,
         * that the property is added when this parameter is 'true'.
         * This is important, if the Widget's properties are copied.
         */
        
        propsCont.addProperty( coldColor );
        propsCont.addProperty( hotColor );
        
        propsCont.addProperty( myFont );
        
        propsCont.addProperty( myEnum );
        
        /*
         * To add online documentation to the editor you just have to add a 'doc' subfolder
         * to your widget package (java package) and add html files using the property names.
         * Please see the examples in this lesson.
         */
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        getFontProperty().setFont( "Dialog", Font.PLAIN, 9, false, true );
        myFont.setFont( "Dialog", Font.PLAIN, 9, false, true );
    }
    
    public Lesson3Widget_Properties()
    {
        super( 11.0f, 5.0f );
    }
}
