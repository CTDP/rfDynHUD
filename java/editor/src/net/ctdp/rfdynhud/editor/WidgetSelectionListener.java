package net.ctdp.rfdynhud.editor;

import net.ctdp.rfdynhud.widgets.widget.Widget;

public interface WidgetSelectionListener
{
    public void onWidgetSelected( Widget widget, boolean selectionChanged, boolean doubleClick );
}
