package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.properties.Property;

public interface PropertyChangeListener
{
    public void onPropertyChanged( Property property, Object oldValue, Object newValue, int row, int column );
}
