/**
 * This piece of code has been provided by and with kind
 * permission of INFOLOG GmbH from Germany.
 * It is released under the terms of the GPL, but INFOLOG
 * is still permitted to use it in closed source software.
 */
package net.ctdp.rfdynhud.editor.hiergrid;

/**
 * @param <P> the property type
 * 
 * @author Marvin Froehlich (CTDP) (aka Qudus)
 */
public interface PropertySelectionListener<P extends Object>
{
    public void onPropertySelected( P property, int row );
}
