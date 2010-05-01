package net.ctdp.rfdynhud.render;

import java.awt.Color;

public abstract class BorderRenderer
{
    public abstract void drawBorder( Color backgroundColor, BorderMeasures measures, TextureImage2D texture, int offsetX, int offsetY, int width, int height );
}
