package net.ctdp.rfdynhud.editor.properties;

public interface PropertyChangeListener
{
    public void onPropertyChanged( Property property, Object oldValue, Object newValue, int row, int column );
}
