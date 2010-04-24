package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link Property} serves as a general data container and adapter.
 * You can use it to put data into a GUI component and live update it.
 * 
 * @author Marvin Froehlich
 */
public abstract class Property
{
    protected final Widget widget;
    
    private final String name;
    private final String nameForDisplay;
    private final boolean readonly;
    private final PropertyEditorType editorType;
    private final String buttonText;
    private final String buttonTooltip;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    public final String getPropertyName()
    {
        return ( name );
    }
    
    public String getNameForDisplay()
    {
        return ( nameForDisplay );
    }
    
    public final boolean isReadOnly()
    {
        return ( readonly );
    }
    
    public final PropertyEditorType getEditorType()
    {
        return ( editorType );
    }
    
    public String getButtonText()
    {
        return ( buttonText );
    }
    
    public String getButtonTooltip()
    {
        return ( buttonTooltip );
    }
    
    public abstract void setValue( Object value );
    
    public abstract Object getValue();
    
    public void onButtonClicked( Object button )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( this.getClass().getSimpleName() + "( \"" + getPropertyName() + "\" = \"" + String.valueOf( getValue() ) + "\" )" );
    }
    
    /**
     * 
     * @param widget
     * @param name
     * @param nameForDisplay
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( Widget widget, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.widget = widget;
        this.name = name;
        this.nameForDisplay = nameForDisplay;
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
    }
    
    public Property( Widget widget, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType )
    {
        this( widget, name, nameForDisplay, readonly, editorType, null, null );
    }
    
    public Property( Widget widget, String name, String nameForDisplay, PropertyEditorType editorType )
    {
        this( widget, name, nameForDisplay, false, editorType, null, null );
    }
    
    /**
     * 
     * @param name
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( Widget widget, String name, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this( widget, name, name, readonly, editorType, buttonText, buttonTooltip );
    }
    
    public Property( Widget widget, String name, boolean readonly, PropertyEditorType editorType )
    {
        this( widget, name, readonly, editorType, null, null );
    }
    
    public Property( Widget widget, String name, PropertyEditorType editorType )
    {
        this( widget, name, false, editorType, null, null );
    }
}
