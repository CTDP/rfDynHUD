package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.properties.Property;

public class WidgetPropertyChangeListener implements PropertyChangeListener
{
    private final RFDynHUDEditor editor;
    
    static final boolean needsAreaClear( Property p )
    {
        if ( p.getPropertyName().equals( "x" ) )
            return ( true );
        
        if ( p.getPropertyName().equals( "y" ) )
            return ( true );
        
        if ( p.getPropertyName().equals( "width" ) )
            return ( true );
        
        if ( p.getPropertyName().equals( "height" ) )
            return ( true );
        
        if ( p.getPropertyName().equals( "initialVisibility" ) )
            return ( true );
        
        return ( false );
    }
    
    @Override
    public void onPropertyChanged( Property property, Object oldValue, Object newValue, int row, int column )
    {
        if ( column == 2 )
        {
            if ( property != null )
            {
                if ( needsAreaClear( property ) )
                    editor.getEditorPanel().clearSelectedWidgetRegion();
                
                property.setValue( newValue );
                
                editor.getEditorPanel().repaint();
            }
        }
    }
    
    public WidgetPropertyChangeListener( RFDynHUDEditor editor )
    {
        this.editor = editor;
    }
}
