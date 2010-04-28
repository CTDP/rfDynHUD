package net.ctdp.rfdynhud.widgets._util;

public class StandardWidgetSet
{
    public static final String WIDGET_PACKAGE = "";
    
    public static final String POSITION_ITEM_FONT_COLOR_NAME = "PositionItemFontColor";
    public static final String POSITION_ITEM_COLOR_NORMAL = "PositionItemColorNormal";
    public static final String POSITION_ITEM_COLOR_LEADER = "PositionItemColorLeader";
    public static final String POSITION_ITEM_COLOR_ME = "PositionItemColorMe";
    public static final String POSITION_ITEM_COLOR_NEXT_IN_FRONT = "PositionItemColorNextInFront";
    public static final String POSITION_ITEM_COLOR_NEXT_BEHIND = "PositionItemColorNextBehind";
    
    public static final String POSITION_ITEM_FONT_NAME = "PositionItemFont";
    
    public static String getDefaultNamedColorValue( String name )
    {
        if ( name.equals( POSITION_ITEM_FONT_COLOR_NAME ) )
            return ( "#000000" );
        
        if ( name.equals( POSITION_ITEM_COLOR_NORMAL ) )
            return ( "#FFFFFFC0" );
        
        if ( name.equals( POSITION_ITEM_COLOR_LEADER ) )
            return ( "#FF0000C0" );
        
        if ( name.equals( POSITION_ITEM_COLOR_ME ) )
            return ( "#00FF00C0" );
        
        if ( name.equals( POSITION_ITEM_COLOR_NEXT_IN_FRONT ) )
            return ( "#0000FFC0" );
        
        if ( name.equals( POSITION_ITEM_COLOR_NEXT_BEHIND ) )
            return ( "#FFFF00C0" );
        
        return ( null );
    }
    
    public static String getDefaultNamedFontValue( String name )
    {
        if ( name.equals( POSITION_ITEM_FONT_NAME ) )
            return ( "Monospaced-PLAIN-9va" );
        
        return ( null );
    }
}
