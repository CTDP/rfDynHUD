package net.ctdp.rfdynhud.editor.properties;


/**
 * The {@link Property} serves as a general data container and adapter.
 * You can use it to put data into a GUI component and live update it.
 * 
 * @author Marvin Froehlich
 */
public abstract class Property
{
    private final String key;
    private final boolean readonly;
    private final PropertyEditorType editorType;
    private final String buttonText;
    private final String buttonTooltip;
    
    public final String getKey()
    {
        return ( key );
    }
    
    public String getKeyForDisplay()
    {
        return ( getKey() );
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
        return ( this.getClass().getSimpleName() + "( \"" + getKey() + "\" = \"" + String.valueOf( getValue() ) + "\" )" );
    }
    
    /**
     * 
     * @param key
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( String key, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.key = key;
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
    }
    
    public Property( String key, boolean readonly, PropertyEditorType editorType )
    {
        this( key, readonly, editorType, null, null );
    }
    
    public Property( String key, PropertyEditorType editorType )
    {
        this( key, false, editorType, null, null );
    }
}
