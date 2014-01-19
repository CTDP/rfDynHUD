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
package net.ctdp.rfdynhud.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;

import net.ctdp.rfdynhud.gamedata.GameFileSystem;

public class FontUtils
{
    public static final Font FALLBACK_FONT = Font.decode( "Impact-PLAIN-18" );
    public static final Font FALLBACK_VIRTUAL_FONT = Font.decode( "Impact-PLAIN-14" );
    
    public static final char SEPARATOR = '|';
    
    private static int parseStyle( String style )
    {
        style = style.toUpperCase();
        
        if ( style.equals( "BOLD" ) )
            return ( Font.BOLD );
        
        if ( style.equals( "ITALIC" ) )
            return ( Font.ITALIC );
        
        if ( style.equals( "BOLDITALIC" ) )
            return ( Font.BOLD | Font.ITALIC );
        
        return ( Font.PLAIN );
    }
    
    public static int getStyle( boolean bold, boolean italic )
    {
        if ( bold )
        {
            if ( italic )
                return ( Font.BOLD | Font.ITALIC );
            
            return ( Font.BOLD );
        }
        
        if ( italic )
            return ( Font.ITALIC );
        
        return ( Font.PLAIN );
    }
    
    public static String getStyleString( int awtFontStyle )
    {
        if ( awtFontStyle == java.awt.Font.PLAIN )
            return ( "PLAIN" );
        
        String s = "";
        if ( ( awtFontStyle & java.awt.Font.BOLD ) != 0 )
            s += "BOLD";
        
        if ( ( awtFontStyle & java.awt.Font.ITALIC ) != 0 )
            s += "ITALIC";
        
        return ( s );
    }
    
    public static String getStyleString( boolean bold, boolean italic )
    {
        if ( !bold && !italic )
            return ( "PLAIN" );
        
        String s = "";
        if ( bold )
            s += "BOLD";
        
        if ( italic )
            s += "ITALIC";
        
        return ( s );
    }
    
    private static String getStyleString( java.awt.Font font )
    {
        if ( font.getStyle() == java.awt.Font.PLAIN )
            return ( "PLAIN" );
        
        String s = "";
        if ( ( font.getStyle() & java.awt.Font.BOLD ) != 0 )
            s += "BOLD";
        
        if ( ( font.getStyle() & java.awt.Font.ITALIC ) != 0 )
            s += "ITALIC";
        
        return ( s );
    }
    
    public static final String getFontString( Font font, boolean virtual, boolean antiAliased )
    {
        return ( font.getName() + SEPARATOR + getStyleString( font ) + SEPARATOR + font.getSize() + ( virtual ? "v" : "" ) + ( antiAliased ? "a" : "" ) );
    }
    
    public static final String getFontString( String name, int awtFontStyle, int size, boolean virtual, boolean antiAliased )
    {
        return ( name + SEPARATOR + getStyleString( awtFontStyle ) + SEPARATOR + size + ( virtual ? "v" : "" ) + ( antiAliased ? "a" : "" ) );
    }
    
    public static final String getFontString( String name, boolean bold, boolean italic, int size, boolean virtual, boolean antiAliased )
    {
        return ( name + SEPARATOR + getStyleString( bold, italic ) + SEPARATOR + size + ( virtual ? "v" : "" ) + ( antiAliased ? "a" : "" ) );
    }
    
    public static int getVirtualFontSize( int size, int gameResY )
    {
        return ( Math.round( size * 960f / gameResY ) );
    }
    
    public static int getConcreteFontSize( int size, int gameResY )
    {
        return ( Math.round( size * gameResY / 960f ) );
    }
    
    public static Font getFont( String name, int style, int size, int gameResY )
    {
        if ( gameResY > 0 )
            size = Math.round( size * gameResY / 960f );
        
        return ( new Font( name, style, size ) );
    }
    
    private static Object getFallback( IllegalArgumentException exception, boolean extractVirtualFlag, boolean extractAntialiasFlag, boolean throwException, boolean logException )
    {
        if ( throwException )
            throw exception;
        
        if ( logException )
            RFDHLog.exception( exception );
        
        if ( extractVirtualFlag || extractAntialiasFlag )
            return ( Boolean.FALSE );
        
        return ( FALLBACK_FONT );
    }
    
    private static Object _parseFont( String str, int gameResY, boolean extractVirtualFlag, boolean extractAntialiasFlag, boolean throwException, boolean logException )
    {
        if ( ( str == null ) || ( str.length() < 5 ) )
            return ( getFallback( new IllegalArgumentException( "Illegal font string " + str ), extractVirtualFlag, extractAntialiasFlag, throwException, logException ) );
        
        int p0 = 0;
        int p1 = str.indexOf( SEPARATOR );
        if ( ( p1 == -1 ) || ( p1 == p0 ) )
            return ( getFallback( new IllegalArgumentException( "Illegal font string " + str ), extractVirtualFlag, extractAntialiasFlag, throwException, logException ) );
        
        String name = str.substring( p0, p1 );
        
        p0 = p1 + 1;
        
        if ( str.length() <= p0 )
            return ( getFallback( new IllegalArgumentException( "Illegal font string " + str ), extractVirtualFlag, extractAntialiasFlag, throwException, logException ) );
        
        p1 = str.indexOf( SEPARATOR, p0 );
        
        if ( ( p1 == -1 ) || ( p1 == p0 ) )
            return ( getFallback( new IllegalArgumentException( "Illegal font string " + str ), extractVirtualFlag, extractAntialiasFlag, throwException, logException ) );
        
        String style = str.substring( p0, p1 );
        
        p0 = p1 + 1;
        
        if ( str.length() <= p0 )
            return ( getFallback( new IllegalArgumentException( "Illegal font string " + str ), extractVirtualFlag, extractAntialiasFlag, throwException, logException ) );
        
        String size = str.substring( p0 );
        
        boolean virtual = size.endsWith( "v" ) || size.endsWith( "va" );
        boolean antiAliased = size.endsWith( "a" ) || size.endsWith( "va" );
        
        if ( extractVirtualFlag )
            return ( virtual );
        
        if ( extractAntialiasFlag )
            return ( antiAliased );
        
        if ( virtual && antiAliased )
            size = size.substring( 0, size.length() - 2 );
        else if ( virtual || antiAliased )
            size = size.substring( 0, size.length() - 1 );
        
        int pointSize;
        try
        {
            pointSize = Integer.parseInt( size );
        }
        catch ( NumberFormatException e )
        {
            pointSize = 12;
        }
        
        return ( getFont( name, parseStyle( style ), pointSize, virtual ? gameResY : -1 ) );
    }
    
    public static Font parseFont( String str, int gameResY, boolean throwException, boolean logException )
    {
        return ( (Font)_parseFont( str, gameResY, false, false, throwException, logException ) );
    }
    
    public static Font parseVirtualFont( String str, boolean throwException, boolean logException )
    {
        return ( (Font)_parseFont( str, -1, false, false, throwException, logException ) );
    }
    
    public static boolean parseVirtualFlag( String str, boolean throwException, boolean logException )
    {
        return ( (Boolean)_parseFont( str, -1, true, false, throwException, logException ) );
    }
    
    public static boolean parseAntiAliasFlag( String str, boolean throwException, boolean logException )
    {
        return ( (Boolean)_parseFont( str, -1, false, true, throwException, logException ) );
    }
    
    public static void loadCustomFonts( GameFileSystem fileSystem )
    {
        File folder = new File( ResourceManager.isCompleteIDEMode() ? fileSystem.getConfigFolder().getParentFile() : fileSystem.getPluginFolder(), "fonts" );
        
        if ( !folder.exists() )
            return;
        
        RFDHLog.printlnEx( "Loading custom fonts..." );
        for ( File file : folder.listFiles() )
        {
            if ( file.isFile() && file.getName().toLowerCase().endsWith( "ttf" ) )
            {
                Font font = null;
                try
                {
                    font = Font.createFont( Font.TRUETYPE_FONT, file );
                }
                catch ( Throwable t )
                {
                    RFDHLog.exception( "Couldn't load font file \"" + file.getAbsolutePath() + "\". Message was: " + t.getMessage() );
                }
                
                if ( font != null )
                {
                    Font testFont = new Font( font.getName(), font.getStyle(), font.getSize() );
                    if ( testFont.getName().equals( font.getName() ) && testFont.getFamily().equals( font.getFamily() ) )
                        RFDHLog.exception( "Couldn't register font \"" + font.getName() + "\". It is already registered." );
                    else if ( GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont( font ) )
                        RFDHLog.printlnEx( "    Loaded and registered custom font \"" + font.getName() + "\"." );
                    else
                        RFDHLog.exception( "Couldn't register font \"" + font.getName() + "\"." );
                }
            }
        }
    }
}
