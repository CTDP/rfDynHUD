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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.ctdp.rfdynhud.editor.presets.ScaleType;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BackgroundProperty.BackgroundType;
import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FlatPropertiesContainer;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

import org.openmali.vecmath2.util.ColorUtils;

/**
 * Handles {@link Widget} imports.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetImportManager
{
    public static enum ImportDecision
    {
        USE_DESTINATION_ALIASES,
        RENAME_ALIASES,
        CONVERT_TO_LOCAL,
        OVERWRITE_ALIASES,
        ;
    }
    
    private final RFDynHUDEditor editor;
    private final WidgetsConfiguration widgetsConfig;
    private final JPanel importPanel;
    
    private boolean checkDoubleImport( Widget newWidget )
    {
        for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            if ( ( newWidget != widget ) && ( widget.getClass() == newWidget.getClass() ) )
            {
                if ( ( widget.getPosition().getEffectiveX() == newWidget.getPosition().getEffectiveX() ) && ( widget.getPosition().getEffectiveY() == newWidget.getPosition().getEffectiveY() ) &&
                     ( widget.getSize().getEffectiveWidth() == newWidget.getSize().getEffectiveWidth() ) && ( widget.getSize().getEffectiveHeight() == newWidget.getSize().getEffectiveHeight() ) )
                {
                    return ( true );
                }
            }
        }
        
        return ( false );
    }
    
    private boolean getDifferingNamedItems( Widget widget, List<Property> propsList, ArrayList<BorderProperty> differingBorderAliases, ArrayList<BackgroundProperty> differingNamedBackgroundColors, ArrayList<ColorProperty> differingNamedColors, ArrayList<FontProperty> differingNamedFonts )
    {
        boolean differenceFound = false;
        
        for ( Property property : propsList )
        {
            if ( property instanceof BorderProperty )
            {
                BorderProperty borderProp = (BorderProperty)property;
                
                String border1 = widget.getConfiguration().getBorderName( borderProp.getBorderAlias() );
                if ( border1 != null )
                {
                    String border2 = widgetsConfig.getBorderName( borderProp.getBorderAlias() );
                    if ( ( border2 != null ) && !border2.equals( border1 ) )
                    {
                        //System.out.println( "Differing border alias: " + borderProp.getBorderName() );
                        differingBorderAliases.add( borderProp );
                        differenceFound = true;
                    }
                }
            }
            else if ( ( property instanceof BackgroundProperty ) && ( ( (BackgroundProperty)property ).getBackgroundType() == BackgroundType.COLOR ) )
            {
                BackgroundProperty bgProp = (BackgroundProperty)property;
                
                if ( bgProp.getValue() != null )
                {
                    if ( bgProp.getBackgroundType() == BackgroundType.COLOR )
                    {
                        Color color1 = widget.getConfiguration().getNamedColor( bgProp.getColorProperty().getColorKey() );
                        if ( color1 != null )
                        {
                            Color color2 = widgetsConfig.getNamedColor( bgProp.getColorProperty().getColorKey() );
                            if ( ( color2 != null ) && !color2.equals( color1 ) )
                            {
                                //System.out.println( "Differing named color: " + colorProp.getColorKey() );
                                differingNamedBackgroundColors.add( bgProp );
                                differenceFound = true;
                            }
                        }
                    }
                }
            }
            else if ( property instanceof ColorProperty )
            {
                ColorProperty colorProp = (ColorProperty)property;
                
                Color color1 = widget.getConfiguration().getNamedColor( colorProp.getColorKey() );
                if ( color1 != null )
                {
                    Color color2 = widgetsConfig.getNamedColor( colorProp.getColorKey() );
                    if ( ( color2 != null ) && !color2.equals( color1 ) )
                    {
                        //System.out.println( "Differing named color: " + colorProp.getColorKey() );
                        differingNamedColors.add( colorProp );
                        differenceFound = true;
                    }
                }
            }
            else if ( property instanceof FontProperty )
            {
                FontProperty fontProp = (FontProperty)property;
                
                String font1 = widget.getConfiguration().getNamedFontString( fontProp.getFontKey() );
                if ( font1 != null )
                {
                    String font2 = widgetsConfig.getNamedFontString( fontProp.getFontKey() );
                    if ( ( font2 != null ) && !font2.equals( font1 ) )
                    {
                        //System.out.println( "Differing named font: " + fontProp.getFontKey() );
                        differingNamedFonts.add( fontProp );
                        differenceFound = true;
                    }
                }
            }
        }
        
        return ( differenceFound );
    }
    
    private void setBorderPropsToLocalBorders( List<Property> propsList, List<BorderProperty> affectedProps )
    {
        if ( affectedProps.size() == 0 )
            return;
        
        for ( Property property : propsList )
        {
            if ( property instanceof BorderProperty )
            {
                BorderProperty borderProp = (BorderProperty)property;
                
                for ( BorderProperty borderProp2 : affectedProps )
                {
                    if ( borderProp2.getName().equals( borderProp.getName() ) )
                    {
                        borderProp.setBorder( ( (Widget)borderProp2.getKeeper() ).getConfiguration().getBorderName( borderProp2.getBorderAlias() ) );
                    }
                }
            }
        }
    }
    
    private void setBackgroundPropsToLocalColors( List<Property> propsList, List<BackgroundProperty> affectedProps )
    {
        if ( affectedProps.size() == 0 )
            return;
        
        for ( Property property : propsList )
        {
            if ( property instanceof BackgroundProperty )
            {
                BackgroundProperty bgProp = (BackgroundProperty)property;
                
                for ( BackgroundProperty bgProp2 : affectedProps )
                {
                    if ( bgProp2.getName().equals( bgProp.getName() ) )
                    {
                        bgProp.setColorValue( ColorUtils.colorToHex( bgProp2.getColorValue() ) );
                    }
                }
            }
        }
    }
    
    private void setColorPropsToLocalColors( List<Property> propsList, List<ColorProperty> affectedProps )
    {
        if ( affectedProps.size() == 0 )
            return;
        
        for ( Property property : propsList )
        {
            if ( property instanceof ColorProperty )
            {
                ColorProperty colorProp = (ColorProperty)property;
                
                for ( ColorProperty colorProp2 : affectedProps )
                {
                    if ( colorProp2.getName().equals( colorProp.getName() ) )
                    {
                        colorProp.setColor( ( (Widget)colorProp2.getKeeper() ).getConfiguration().getNamedColor( colorProp2.getColorKey() ) );
                    }
                }
            }
        }
    }
    
    private void setFontPropsToLocalFonts( List<Property> propsList, List<FontProperty> affectedProps )
    {
        if ( affectedProps.size() == 0 )
            return;
        
        for ( Property property : propsList )
        {
            if ( property instanceof FontProperty )
            {
                FontProperty fontProp = (FontProperty)property;
                
                for ( FontProperty fontProp2 : affectedProps )
                {
                    if ( fontProp2.getName().equals( fontProp.getName() ) )
                    {
                        fontProp.setFont( ( (Widget)fontProp2.getKeeper() ).getConfiguration().getNamedFontString( fontProp2.getFontKey() ) );
                    }
                }
            }
        }
    }
    
    private void overwriteBorderAliases( List<Property> propsList, List<BorderProperty> affectedProps )
    {
        if ( affectedProps.size() == 0 )
            return;
        
        for ( Property property : propsList )
        {
            if ( property instanceof BorderProperty )
            {
                BorderProperty borderProp = (BorderProperty)property;
                
                for ( BorderProperty borderProp2 : affectedProps )
                {
                    if ( borderProp2.getName().equals( borderProp.getName() ) )
                    {
                        widgetsConfig.addBorderAlias( borderProp2.getBorderAlias(), ( (Widget)borderProp2.getKeeper() ).getConfiguration().getBorderName( borderProp2.getBorderAlias() ) );
                        borderProp.refresh();
                    }
                }
            }
        }
    }
    
    private void overwriteBackgroundPropsColors( List<Property> propsList, List<BackgroundProperty> affectedProps )
    {
        if ( affectedProps.size() == 0 )
            return;
        
        for ( Property property : propsList )
        {
            if ( property instanceof BackgroundProperty )
            {
                BackgroundProperty bgProp = (BackgroundProperty)property;
                
                for ( BackgroundProperty bgProp2 : affectedProps )
                {
                    if ( bgProp2.getName().equals( bgProp.getName() ) )
                    {
                        widgetsConfig.addNamedColor( bgProp2.getColorProperty().getColorKey(), ( (Widget)bgProp2.getKeeper() ).getConfiguration().getNamedColor( bgProp2.getColorProperty().getColorKey() ) );
                        bgProp.getColorProperty().refresh();
                    }
                }
            }
        }
    }
    
    private void overwriteColorNameValues( List<Property> propsList, List<ColorProperty> affectedProps )
    {
        if ( affectedProps.size() == 0 )
            return;
        
        for ( Property property : propsList )
        {
            if ( property instanceof ColorProperty )
            {
                ColorProperty colorProp = (ColorProperty)property;
                
                for ( ColorProperty colorProp2 : affectedProps )
                {
                    if ( colorProp2.getName().equals( colorProp.getName() ) )
                    {
                        widgetsConfig.addNamedColor( colorProp2.getColorKey(), ( (Widget)colorProp2.getKeeper() ).getConfiguration().getNamedColor( colorProp2.getColorKey() ) );
                        colorProp.refresh();
                    }
                }
            }
        }
    }
    
    private void overwriteNamedFontValues( List<Property> propsList, List<FontProperty> affectedProps )
    {
        if ( affectedProps.size() == 0 )
            return;
        
        for ( Property property : propsList )
        {
            if ( property instanceof FontProperty )
            {
                FontProperty fontProp = (FontProperty)property;
                
                for ( FontProperty fontProp2 : affectedProps )
                {
                    if ( fontProp2.getName().equals( fontProp.getName() ) )
                    {
                        widgetsConfig.addNamedFont( fontProp2.getFontKey(), ( (Widget)fontProp2.getKeeper() ).getConfiguration().getNamedFontString( fontProp2.getFontKey() ) );
                        fontProp.refresh();
                    }
                }
            }
        }
    }
    
    private void renameAliases( List<Property> propsList, ArrayList<BorderProperty> differingBorderAliases, ArrayList<BackgroundProperty> differingNamedBackgroundColors, ArrayList<ColorProperty> differingNamedColors, ArrayList<FontProperty> differingNamedFonts )
    {
        for ( BorderProperty borderProp : differingBorderAliases )
        {
            String baseAlias = borderProp.getBorderAlias();
            while ( ( baseAlias.charAt( baseAlias.length() - 1 ) >= '0' ) && ( baseAlias.charAt( baseAlias.length() - 1 ) <= '9' ) )
                baseAlias = baseAlias.substring( 0, baseAlias.length() - 1 );
            
            int number = 2;
            
            while ( widgetsConfig.getBorderAliases().contains( baseAlias + number ) )
                number++;
            
            widgetsConfig.addBorderAlias( baseAlias + number, ( (Widget)borderProp.getKeeper() ).getConfiguration().getBorderName( borderProp.getBorderAlias() ) );
            
            for ( Property property : propsList )
            {
                if ( property instanceof BorderProperty )
                {
                    BorderProperty borderProp2 = (BorderProperty)property;
                    
                    if ( borderProp2.getName().equals( borderProp.getName() ) )
                    {
                        borderProp2.setBorder( baseAlias + number );
                    }
                }
            }
        }
        
        for ( BackgroundProperty bgProp : differingNamedBackgroundColors )
        {
            String baseName = bgProp.getColorProperty().getColorKey();
            while ( ( baseName.charAt( baseName.length() - 1 ) >= '0' ) && ( baseName.charAt( baseName.length() - 1 ) <= '9' ) )
                baseName = baseName.substring( 0, baseName.length() - 1 );
            
            int number = 2;
            
            while ( widgetsConfig.getColorNames().contains( baseName + number ) )
                number++;
            
            widgetsConfig.addNamedColor( baseName + number, ( (Widget)bgProp.getKeeper() ).getConfiguration().getNamedColor( bgProp.getColorProperty().getColorKey() ) );
            
            for ( Property property : propsList )
            {
                if ( property instanceof BackgroundProperty )
                {
                    BackgroundProperty bgProp2 = (BackgroundProperty)property;
                    
                    if ( bgProp2.getName().equals( bgProp.getName() ) )
                    {
                        bgProp2.setColorValue( baseName + number );
                    }
                }
            }
        }
        
        for ( ColorProperty colorProp : differingNamedColors )
        {
            String baseName = colorProp.getColorKey();
            while ( ( baseName.charAt( baseName.length() - 1 ) >= '0' ) && ( baseName.charAt( baseName.length() - 1 ) <= '9' ) )
                baseName = baseName.substring( 0, baseName.length() - 1 );
            
            int number = 2;
            
            while ( widgetsConfig.getColorNames().contains( baseName + number ) )
                number++;
            
            widgetsConfig.addNamedColor( baseName + number, ( (Widget)colorProp.getKeeper() ).getConfiguration().getNamedColor( colorProp.getColorKey() ) );
            
            for ( Property property : propsList )
            {
                if ( property instanceof ColorProperty )
                {
                    ColorProperty colorProp2 = (ColorProperty)property;
                    
                    if ( colorProp2.getName().equals( colorProp.getName() ) )
                    {
                        colorProp2.setColor( baseName + number );
                    }
                }
            }
        }
        
        for ( FontProperty fontProp : differingNamedFonts )
        {
            String baseName = fontProp.getFontKey();
            while ( ( baseName.charAt( baseName.length() - 1 ) >= '0' ) && ( baseName.charAt( baseName.length() - 1 ) <= '9' ) )
                baseName = baseName.substring( 0, baseName.length() - 1 );
            
            int number = 2;
            
            while ( widgetsConfig.getFontNames().contains( baseName + number ) )
                number++;
            
            widgetsConfig.addNamedFont( baseName + number, ( (Widget)fontProp.getKeeper() ).getConfiguration().getNamedFontString( fontProp.getFontKey() ) );
            
            for ( Property property : propsList )
            {
                if ( property instanceof FontProperty )
                {
                    FontProperty fontProp2 = (FontProperty)property;
                    
                    if ( fontProp2.getName().equals( fontProp.getName() ) )
                    {
                        fontProp2.setFont( baseName + number );
                    }
                }
            }
        }
    }
    
    private void refreshAllProperties()
    {
        for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
        {
            FlatPropertiesContainer propsCont3 = new FlatPropertiesContainer();
            widgetsConfig.getWidget( i ).getProperties( propsCont3, true );
            for ( Property prop3 : propsCont3.getList() )
            {
                if ( prop3 instanceof BorderProperty )
                    ( (BorderProperty)prop3 ).refresh();
                if ( ( prop3 instanceof BackgroundProperty ) && ( ( (BackgroundProperty)prop3 ).getBackgroundType() == BackgroundType.COLOR ) )
                    ( (BackgroundProperty)prop3 ).getColorProperty().refresh();
                if ( prop3 instanceof ColorProperty )
                    ( (ColorProperty)prop3 ).refresh();
                if ( prop3 instanceof FontProperty )
                    ( (FontProperty)prop3 ).refresh();
            }
        }
        
        for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
        {
            widgetsConfig.getWidget( i ).forceReinitialization();
        }
    }
    
    private void importMissingAliases( List<Property> propsList )
    {
        for ( Property property : propsList )
        {
            if ( property instanceof BorderProperty )
            {
                BorderProperty borderProp = (BorderProperty)property;
                
                String border1 = ( (Widget)borderProp.getKeeper() ).getConfiguration().getBorderName( borderProp.getBorderAlias() );
                if ( border1 != null )
                {
                    String border2 = widgetsConfig.getBorderName( borderProp.getBorderAlias() );
                    if ( border2 == null )
                    {
                        widgetsConfig.addBorderAlias( borderProp.getBorderAlias(), border1 );
                    }
                }
            }
            else if ( ( property instanceof BackgroundProperty ) && ( ( (BackgroundProperty)property ).getBackgroundType() == BackgroundType.COLOR ) )
            {
                BackgroundProperty bgProp = (BackgroundProperty)property;
                
                if ( bgProp.getValue() != null )
                {
                    if ( bgProp.getBackgroundType() == BackgroundType.COLOR )
                    {
                        Color color1 = ( (Widget)bgProp.getKeeper() ).getConfiguration().getNamedColor( bgProp.getColorProperty().getColorKey() );
                        if ( color1 != null )
                        {
                            Color color2 = widgetsConfig.getNamedColor( bgProp.getColorProperty().getColorKey() );
                            if ( color2 == null )
                            {
                                widgetsConfig.addNamedColor( bgProp.getColorProperty().getColorKey(), color1 );
                            }
                        }
                    }
                }
            }
            else if ( property instanceof ColorProperty )
            {
                ColorProperty colorProp = (ColorProperty)property;
                
                Color color1 = ( (Widget)colorProp.getKeeper() ).getConfiguration().getNamedColor( colorProp.getColorKey() );
                if ( color1 != null )
                {
                    Color color2 = widgetsConfig.getNamedColor( colorProp.getColorKey() );
                    if ( color2 == null )
                    {
                        widgetsConfig.addNamedColor( colorProp.getColorKey(), color1 );
                    }
                }
            }
            else if ( property instanceof FontProperty )
            {
                FontProperty fontProp = (FontProperty)property;
                
                String font1 = ( (Widget)fontProp.getKeeper() ).getConfiguration().getNamedFontString( fontProp.getFontKey() );
                if ( font1 != null )
                {
                    String font2 = widgetsConfig.getNamedFontString( fontProp.getFontKey() );
                    if ( font2 == null )
                    {
                        widgetsConfig.addNamedFont( fontProp.getFontKey(), font1 );
                    }
                }
            }
        }
    }
    
    /**
     * Imports a {@link Widget} into the local configuration.
     * 
     * @param widget the widget to import
     * 
     * @return success?
     */
    public Boolean importWidget( Widget widget )
    {
        try
        {
            FlatPropertiesContainer propsCont = new FlatPropertiesContainer();
            widget.getProperties( propsCont, true );
            List<Property> propsList = propsCont.getList();
            
            ArrayList<BorderProperty> differingBorderAliases = new ArrayList<BorderProperty>();
            ArrayList<BackgroundProperty> differingNamedBackgroundColors = new ArrayList<BackgroundProperty>();
            ArrayList<ColorProperty> differingNamedColors = new ArrayList<ColorProperty>();
            ArrayList<FontProperty> differingNamedFonts = new ArrayList<FontProperty>();
            
            boolean hasDifferingNamedProps = getDifferingNamedItems( widget, propsList, differingBorderAliases, differingNamedBackgroundColors, differingNamedColors, differingNamedFonts );
            
            String widgetType = widget.getClass().getSimpleName();
            
            RFDHLog.println( "Importing Widget of type \"" + widgetType + "\"..." );
            
            String name = widget.getName();
            if ( widgetsConfig.getWidget( name ) != null )
            {
                name = widgetsConfig.findFreeName( widgetType );
            }
            
            Widget newWidget = widget.clone();
            
            if ( newWidget == null )
                return ( false );
            
            newWidget.setName( name );
            
            __WCPrivilegedAccess.addWidget( widgetsConfig, newWidget, false );
            
            widget.setAllPosAndSizeToPercents();
            
            ImportDecision decision = null;
            
            if ( checkDoubleImport( newWidget ) )
            {
                int answer = JOptionPane.showConfirmDialog( importPanel, "Do you really want to import the Widget more than once?", "Double import", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
                
                if ( answer == JOptionPane.NO_OPTION )
                {
                    editor.getEditorPanel().clearWidgetRegion( newWidget );
                    __WCPrivilegedAccess.removeWidget( widgetsConfig, newWidget );
                    
                    return ( null );
                }
            }
            
            if ( hasDifferingNamedProps )
            {
                ImportDecisionsWindow decisionsWindow = new ImportDecisionsWindow( (JDialog)importPanel.getRootPane().getParent() );
                decisionsWindow.setDecision( editor.lastImportDecision );
                decisionsWindow.setVisible( true );
                
                decision = decisionsWindow.getDecision();
                if ( decision == null )
                {
                    editor.getEditorPanel().clearWidgetRegion( newWidget );
                    __WCPrivilegedAccess.removeWidget( widgetsConfig, newWidget );
                    
                    return ( null );
                }
                
                editor.lastImportDecision = decision;
            }
            
            if ( editor.presetsWindow.getDefaultScaleType() == ScaleType.PERCENTS )
                newWidget.setAllPosAndSizeToPercents();
            else if ( editor.presetsWindow.getDefaultScaleType() == ScaleType.ABSOLUTE_PIXELS )
                newWidget.setAllPosAndSizeToPixels();
            
            if ( hasDifferingNamedProps )
            {
                FlatPropertiesContainer propsCont2 = new FlatPropertiesContainer();
                newWidget.getProperties( propsCont2, true );
                List<Property> propsList2 = propsCont2.getList();
                
                switch ( decision )
                {
                    case USE_DESTINATION_ALIASES:
                        for ( Property property : propsList2 )
                        {
                            if ( property instanceof BorderProperty )
                                ( (BorderProperty)property ).refresh();
                            else if ( ( property instanceof BackgroundProperty ) && ( ( (BackgroundProperty)property ).getBackgroundType() == BackgroundType.COLOR ) )
                                ( (BackgroundProperty)property ).getColorProperty().refresh();
                            else if ( property instanceof ColorProperty )
                                ( (ColorProperty)property ).refresh();
                            else if ( property instanceof FontProperty )
                                ( (FontProperty)property ).refresh();
                        }
                        break;
                    case RENAME_ALIASES:
                        renameAliases( propsList2, differingBorderAliases, differingNamedBackgroundColors, differingNamedColors, differingNamedFonts );
                        refreshAllProperties();
                        break;
                    case CONVERT_TO_LOCAL:
                        setBorderPropsToLocalBorders( propsList2, differingBorderAliases );
                        setBackgroundPropsToLocalColors( propsList2, differingNamedBackgroundColors );
                        setColorPropsToLocalColors( propsList2, differingNamedColors );
                        setFontPropsToLocalFonts( propsList2, differingNamedFonts );
                        break;
                    case OVERWRITE_ALIASES:
                        overwriteBorderAliases( propsList2, differingBorderAliases );
                        overwriteBackgroundPropsColors( propsList2, differingNamedBackgroundColors );
                        overwriteColorNameValues( propsList2, differingNamedColors );
                        overwriteNamedFontValues( propsList2, differingNamedFonts );
                        
                        refreshAllProperties();
                        break;
                }
            }
            
            importMissingAliases( propsList );
            
            editor.getEditorPanel().setSelectedWidget( newWidget, false );
            
            editor.setDirtyFlag();
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        return ( true );
    }
    
    public WidgetImportManager( RFDynHUDEditor editor, JPanel importPanel )
    {
        this.editor = editor;
        this.widgetsConfig = editor.getWidgetsConfiguration();
        
        this.importPanel = importPanel;
    }
}
