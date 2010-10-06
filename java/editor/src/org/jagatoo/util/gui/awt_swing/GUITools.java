/**
 * Copyright (c) 2007-2009, JAGaToo Project Group all rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of the 'Xith3D Project Group' nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
 * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE
 */
package org.jagatoo.util.gui.awt_swing;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

/**
 * Utility methods for GUIs.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class GUITools
{
    /**
     * Gets the bounds of the screen device, that currently displays the mouse cursor.
     * 
     * @return the bounds of the screen device, that currently displays the mouse cursor.
     */
    public static Rectangle getCurrentScreenBounds()
    {
        Point cursorLoc = MouseInfo.getPointerInfo().getLocation();
        
        Rectangle screenBounds = null;
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for ( GraphicsDevice gd : ge.getScreenDevices() )
        {
            screenBounds = gd.getDefaultConfiguration().getBounds();
            if ( screenBounds.contains( cursorLoc ) )
            {
                break;
            }
        }
        
        if ( screenBounds == null )
        {
            screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        }
        
        return ( screenBounds );
    }
    
    /**
     * Centers the passed {@link Window} on the screen device, that currently displays the mouse cursor.
     * 
     * @param window the {@link Window} to reposition
     */
    public static void centerWindowOnCurrentScreen( Window window )
    {
        Rectangle screenBounds = getCurrentScreenBounds();
        
        if ( screenBounds == null )
        {
            window.setLocationRelativeTo( null );
        }
        else
        {
            window.setLocation( screenBounds.x + ( screenBounds.width - window.getWidth() ) / 2, screenBounds.y + ( screenBounds.height - window.getHeight() ) / 2 );
        }
    }
}
