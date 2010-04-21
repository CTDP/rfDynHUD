package net.ctdp.rfdynhud.properties;

/**
 * The {@link Property} serves as a general data container and adapter.
 * You can use it to put data into a GUI component and live update it.
 * 
 * @author Marvin Froehlich
 */
public abstract class Property
{
    private final String name;
    private final String nameForDisplay;
    private final boolean readonly;
    private final PropertyEditorType editorType;
    private final String buttonText;
    private final String buttonTooltip;
    
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
     * @param name
     * @param nameForDisplay
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.name = name;
        this.nameForDisplay = nameForDisplay;
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
    }
    
    public Property( String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType )
    {
        this( name, nameForDisplay, readonly, editorType, null, null );
    }
    
    public Property( String name, String nameForDisplay, PropertyEditorType editorType )
    {
        this( name, nameForDisplay, false, editorType, null, null );
    }
    
    /**
     * 
     * @param name
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( String name, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this( name, name, readonly, editorType, buttonText, buttonTooltip );
    }
    
    public Property( String name, boolean readonly, PropertyEditorType editorType )
    {
        this( name, readonly, editorType, null, null );
    }
    
    public Property( String name, PropertyEditorType editorType )
    {
        this( name, false, editorType, null, null );
    }
}
