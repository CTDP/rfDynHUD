package net.ctdp.rfdynhud.widgets._util;

import java.awt.Font;

import net.ctdp.rfdynhud.util.Logger;

public class FontUtils
{
    public static final Font FALLBACK_FONT = Font.decode( "Impact-PLAIN-18" );
    public static final Font FALLBACK_VIRTUAL_FONT = Font.decode( "Impact-PLAIN-14" );
    
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
        return ( font.getName() + "-" + getStyleString( font ) + "-" + font.getSize() + ( virtual ? "v" : "" ) + ( antiAliased ? "a" : "" ) );
    }
    
    public static final String getFontString( String name, int awtFontStyle, int size, boolean virtual, boolean antiAliased )
    {
        return ( name + "-" + getStyleString( awtFontStyle ) + "-" + size + ( virtual ? "v" : "" ) + ( antiAliased ? "a" : "" ) );
    }
    
    public static final String getFontString( String name, boolean bold, boolean italic, int size, boolean virtual, boolean antiAliased )
    {
        return ( name + "-" + getStyleString( bold, italic ) + "-" + size + ( virtual ? "v" : "" ) + ( antiAliased ? "a" : "" ) );
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
    
    private static Object getFallback( IllegalArgumentException exception, boolean extractVirtualFlag, boolean extractAntialiasFlag, boolean throwException )
    {
        if ( throwException )
            throw exception;
        
        Logger.log( exception );
        
        if ( extractVirtualFlag || extractAntialiasFlag )
            return ( Boolean.FALSE );
        
        return ( FALLBACK_FONT );
    }
    
    private static Object _parseFont( String str, int gameResY, boolean extractVirtualFlag, boolean extractAntialiasFlag, boolean throwException )
    {
        if ( ( str == null ) || ( str.length() < 5 ) )
            return ( getFallback( new IllegalArgumentException( "Illegal font string " + str ), extractVirtualFlag, extractAntialiasFlag, throwException ) );
        
        int p0 = 0;
        int p1 = str.indexOf( '-' );
        if ( ( p1 == -1 ) || ( p1 == p0 ) )
            return ( getFallback( new IllegalArgumentException( "Illegal font string " + str ), extractVirtualFlag, extractAntialiasFlag, throwException ) );
        
        String name = str.substring( p0, p1 );
        
        p0 = p1 + 1;
        
        if ( str.length() <= p0 )
            return ( getFallback( new IllegalArgumentException( "Illegal font string " + str ), extractVirtualFlag, extractAntialiasFlag, throwException ) );
        
        p1 = str.indexOf( '-', p0 );
        
        if ( ( p1 == -1 ) || ( p1 == p0 ) )
            return ( getFallback( new IllegalArgumentException( "Illegal font string " + str ), extractVirtualFlag, extractAntialiasFlag, throwException ) );
        
        String style = str.substring( p0, p1 );
        
        p0 = p1 + 1;
        
        if ( str.length() <= p0 )
            return ( getFallback( new IllegalArgumentException( "Illegal font string " + str ), extractVirtualFlag, extractAntialiasFlag, throwException ) );
        
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
    
    public static Font parseFont( String str, int gameResY, boolean throwException )
    {
        return ( (Font)_parseFont( str, gameResY, false, false, throwException ) );
    }
    
    public static Font parseVirtualFont( String str, boolean throwException )
    {
        return ( (Font)_parseFont( str, -1, false, false, throwException ) );
    }
    
    public static boolean parseVirtualFlag( String str, boolean throwException )
    {
        return ( (Boolean)_parseFont( str, -1, true, false, throwException ) );
    }
    
    public static boolean parseAntiAliasFlag( String str, boolean throwException )
    {
        return ( (Boolean)_parseFont( str, -1, false, true, throwException ) );
    }
}
