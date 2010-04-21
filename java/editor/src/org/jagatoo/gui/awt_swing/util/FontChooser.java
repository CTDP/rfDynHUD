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
package org.jagatoo.gui.awt_swing.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import net.ctdp.rfdynhud.properties.FlatWidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

/**
 * The {@link FontChooser} provides the ability to choose a font through a GUI dialog.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class FontChooser extends JPanel
{
    private static final long serialVersionUID = 9197134250358596790L;
    
    private JComboBox combo;
    private int lastNameComboSelectedIndex = -1;
    private String[] namesCache;
    private JList fontNamesList;
    private JList sizeList;
    private JCheckBox boldBox;
    private JCheckBox italicBox;
    private JCheckBox virtualBox;
    private JCheckBox antiAliasedBox;
    private JLabel sampleLabel;
    private String selectedFont;
    private boolean valueChanged = false;
    
    private JButton okButton;
    private JButton cancelButton;
    
    protected void applySelectedFont( String fontString, int gameResY )
    {
        Font font = FontUtils.parseVirtualFont( fontString, false );
        boolean virtual = FontUtils.parseVirtualFlag( fontString, false );
        boolean antiAliased = FontUtils.parseAntiAliasFlag( fontString, false );
        
        fontNamesList.setSelectedIndex( -1 );
        for ( int i = 0; i < fontNamesList.getModel().getSize(); i++ )
        {
            if ( fontNamesList.getModel().getElementAt( i ).equals( font.getFamily() ) )
            {
                fontNamesList.setSelectedIndex( i );
                fontNamesList.scrollRectToVisible( fontNamesList.getCellBounds( i, i ) );
                break;
            }
        }
        
        sizeList.setSelectedIndex( -1 );
        for ( int i = 0; i < sizeList.getModel().getSize(); i++ )
        {
            if ( sizeList.getModel().getElementAt( i ).equals( font.getSize() ) )
            {
                sizeList.setSelectedIndex( i );
                sizeList.scrollRectToVisible( sizeList.getCellBounds( i, i ) );
                break;
            }
        }
        
        boldBox.setSelected( font.isBold() );
        italicBox.setSelected( font.isItalic() );
        virtualBox.setSelected( virtual );
        antiAliasedBox.setSelected( antiAliased );
        
        sampleLabel.setFont( FontUtils.parseFont( fontString, gameResY, false ) );
    }
    
    private String composeSelectedFont()
    {
        return ( FontUtils.getFontString( getSelectedFontFamily(), getSelectedFontBold(), getSelectedFontItalic(), getSelectedFontSize(), getSelectedFontVirtual(), getSelectedFontAntiAliased() ) );
    }
    
    protected void setSelectedFont( String fontString, int gameResY )
    {
        if ( fontString != null )
            sampleLabel.setFont( FontUtils.parseFont( fontString, gameResY, false ) );
        
        this.selectedFont = fontString;
    }
    
    /**
     * Gets the selected Font.
     * 
     * @return the selected Font.
     */
    public final String getSelectedFont()
    {
        return ( selectedFont );
    }
    
    public final String getSelectedFontName()
    {
        if ( combo.getSelectedIndex() == 0 )
            return ( null );
        
        return ( (String)combo.getSelectedItem() );
    }
    
    public final String getSelectedFontFamily()
    {
        if ( fontNamesList.getSelectedIndex() < 0 )
            return ( null );
        
        return ( (String)fontNamesList.getSelectedValue() );
    }
    
    public final int getSelectedFontSize()
    {
        if ( sizeList.getSelectedIndex() < 0 )
            return ( -1 );
        
        return ( (Integer)sizeList.getSelectedValue() );
    }
    
    public final boolean getSelectedFontBold()
    {
        return ( boldBox.isSelected() );
    }
    
    public final boolean getSelectedFontItalic()
    {
        return ( italicBox.isSelected() );
    }
    
    public final boolean getSelectedFontVirtual()
    {
        return ( virtualBox.isSelected() );
    }
    
    public final boolean getSelectedFontAntiAliased()
    {
        return ( antiAliasedBox.isSelected() );
    }
    
    public final boolean getValueChanged()
    {
        return ( valueChanged );
    }
    
    public final JButton getOKButton()
    {
        return ( okButton );
    }
    
    public final JButton getCancelButton()
    {
        return ( cancelButton );
    }
    
    private boolean isRefillingNameCombo = false;
    
    private void refillNameCombo( WidgetsConfiguration widgetsConfig, JComboBox combo, String selectedItem )
    {
        isRefillingNameCombo = true;
        
        Set<String> namesSet = widgetsConfig.getFontNames();
        namesCache = namesSet.toArray( new String[ namesSet.size() ] );
        Arrays.sort( namesCache, String.CASE_INSENSITIVE_ORDER );
        
        combo.removeAllItems();
        combo.addItem( "<NONE>" );
        for ( String s : namesCache )
        {
            combo.addItem( s );
        }
        
        if ( selectedItem != null )
        {
            nameSelectionIgnored = true;
            combo.setEditable( false );
            combo.setSelectedItem( selectedItem );
            combo.setEditable( combo.getSelectedIndex() > 0 );
            nameSelectionIgnored = false;
            lastNameComboSelectedIndex = combo.getSelectedIndex();
        }
        
        isRefillingNameCombo = false;
    }
    
    private static void resetFontPropertyValues( List<Property> list )
    {
        for ( Property prop : list )
        {
            if ( prop instanceof FontProperty )
            {
                FontProperty fontProp = (FontProperty)prop;
                fontProp.setValue( fontProp.getValue() );
            }
        }
    }
    
    private void setAllWidgetsDirty( WidgetsConfiguration widgetsConfig )
    {
        int n = widgetsConfig.getNumWidgets();
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            widget.forceAndSetDirty();
            
            propsCont.clear();
            widget.getProperties( propsCont );
            
            resetFontPropertyValues( propsCont.getList() );
        }
    }
    
    private boolean nameSelectionIgnored = false;
    
    private void checkNameComboValue( int selIndex, String newValue, WidgetsConfiguration widgetsConfig )
    {
        //Thread.dumpStack();
        String oldValue = namesCache[selIndex - 1];
        
        //System.out.println( oldValue + ", " + newValue );
        if ( !oldValue.equals( newValue ) )
        {
            widgetsConfig.renameFont( oldValue, newValue );
            
            refillNameCombo( widgetsConfig, combo, newValue );
            lastNameComboSelectedIndex = combo.getSelectedIndex();
            
            setAllWidgetsDirty( widgetsConfig );
        }
    }
    
    private static boolean applyNamedFont( String fontName, String fontStr, WidgetsConfiguration widgetsConfig )
    {
        if ( widgetsConfig.addNamedFont( fontName, fontStr ) )
        {
            FlatWidgetPropertiesContainer wpc = new FlatWidgetPropertiesContainer();
            
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
            {
                Widget widget = widgetsConfig.getWidget( i );
                
                wpc.clear();
                widget.getProperties( wpc );
                
                boolean propFound = false;
                
                for ( Property prop : wpc.getList() )
                {
                    if ( prop instanceof FontProperty )
                    {
                        FontProperty fontProp = (FontProperty)prop;
                        
                        if ( fontName.equals( fontProp.getFontKey() ) )
                        {
                            fontProp.refresh();
                            __WPrivilegedAccess.onFontChanged( fontProp, fontName, fontName, widget );
                            
                            propFound = true;
                            break;
                        }
                    }
                }
                
                if ( propFound )
                {
                    widget.forceAndSetDirty();
                }
            }
            
            
            return ( true );
        }
        
        return ( false );
    }
    
    protected JPanel createNamedFontSelector( String currentNamedFont, final WidgetsConfiguration widgetsConfig )
    {
        JPanel panel = new JPanel( new BorderLayout() );
        
        JPanel wrapper = new JPanel( new BorderLayout() );
        wrapper.setBorder( new EmptyBorder( 0, 5, 5, 5 ) );
        
        JPanel west = new JPanel( new BorderLayout() );
        
        final JButton remove = new JButton( "remove" );
        
        combo = new JComboBox();
        refillNameCombo( widgetsConfig, combo, currentNamedFont );
        if ( currentNamedFont == null )
            combo.setSelectedIndex( 0 );
        lastNameComboSelectedIndex = combo.getSelectedIndex();
        combo.setEditable( combo.getSelectedItem().equals( currentNamedFont ) );
        combo.addItemListener( new ItemListener()
        {
            //private int revertIndex = -1;
            
            public void itemStateChanged( ItemEvent e )
            {
                if ( isRefillingNameCombo || nameSelectionIgnored )
                    return;
                
                switch ( e.getStateChange() )
                {
                    case ItemEvent.DESELECTED:
                        if ( lastNameComboSelectedIndex > 0 )
                        {
                            applyNamedFont( String.valueOf( e.getItem() ), composeSelectedFont(), widgetsConfig );
                        }
                        
                        /*
                        if ( ( lastNameComboSelectedIndex > 0 ) && ( lastNameComboEditorValue != null ) && !lastNameComboEditorValue.equals( e.getItem() ) )
                        {
                            revertIndex = lastNameComboSelectedIndex;
                        }
                        */
                        break;
                    case ItemEvent.SELECTED:
                        if ( combo.getSelectedIndex() == 0 )
                        {
                            setSelectedFont( composeSelectedFont(), widgetsConfig.getGameResY() );
                        }
                        else if ( combo.getSelectedIndex() > 0 )
                        {
                            String fontName = (String)e.getItem();
                            
                            String fontStr = widgetsConfig.getNamedFontString( fontName );
                            
                            applySelectedFont( fontStr, widgetsConfig.getGameResY() );
                            setSelectedFont( fontStr, widgetsConfig.getGameResY() );
                        }
                        
                        /*
                        if ( revertIndex >= 0 )
                        {
                            int index = revertIndex;
                            revertIndex = -1;
                            combo.setSelectedIndex( index );
                            System.out.println( combo.getEditor().getEditorComponent() );
                            if ( index > 0 )
                                combo.getEditor().setItem( lastNameComboEditorValue );
                        }
                        */
                        
                        if ( combo.getSelectedIndex() >= 0 )
                        {
                            lastNameComboSelectedIndex = combo.getSelectedIndex();
                        }
                        
                        remove.setEnabled( combo.getSelectedIndex() > 0 );
                        break;
                }
                
                combo.setEditable( combo.getSelectedIndex() != 0 );
            }
        } );
        combo.addPopupMenuListener( new PopupMenuListener()
        {
            public void popupMenuWillBecomeVisible( PopupMenuEvent e )
            {
                if ( isRefillingNameCombo )
                    return;
                
                if ( combo.getSelectedIndex() >= 0 )
                {
                    lastNameComboSelectedIndex = combo.getSelectedIndex();
                }
                
                if ( combo.getSelectedIndex() > 0 )
                {
                    checkNameComboValue( lastNameComboSelectedIndex, String.valueOf( combo.getEditor().getItem() ), widgetsConfig );
                }
            }
            
            public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
            {
            }
            
            public void popupMenuCanceled( PopupMenuEvent e )
            {
            }
        } );
        combo.getEditor().getEditorComponent().addKeyListener( new KeyAdapter()
        {
            @Override
            public void keyTyped( KeyEvent e )
            {
                if ( ( e.getKeyChar() == 10 ) || ( e.getKeyChar() == 13 ) ) // Enter
                {
                    if ( lastNameComboSelectedIndex > 0 )
                        checkNameComboValue( lastNameComboSelectedIndex, String.valueOf( combo.getEditor().getItem() ), widgetsConfig );
                }
            }
        } );
        combo.addFocusListener( new FocusAdapter()
        {
            public void focusLost( FocusEvent e )
            {
                if ( lastNameComboSelectedIndex > 0 )
                    checkNameComboValue( lastNameComboSelectedIndex, String.valueOf( combo.getEditor().getItem() ), widgetsConfig );
                
                lastNameComboSelectedIndex = combo.getSelectedIndex();
            }
        } );
        
        west.add( combo, BorderLayout.NORTH );
        wrapper.add( west, BorderLayout.CENTER );
        
        final JPanel east = new JPanel( new BorderLayout( 5, 0 ) );
        east.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
        final JButton add = new JButton( "add new" );
        add.setActionCommand( "" );
        add.setToolTipText( "Add a new named Font" );
        add.setPreferredSize( new Dimension( 75, 20 ) );
        remove.setActionCommand( "" );
        remove.setToolTipText( "Remove the current named Font" );
        remove.setPreferredSize( new Dimension( 75, 20 ) );
        east.add( add, BorderLayout.WEST );
        east.add( remove, BorderLayout.EAST );
        
        add.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                String initialValue = ( (JButton)e.getSource() ).getActionCommand();
                Window window = (Window)east.getRootPane().getParent();
                String newName = (String)JOptionPane.showInputDialog( window, "Please type the name for the new named Font.", "New Named Font", JOptionPane.INFORMATION_MESSAGE, null, null, initialValue );
                if ( ( newName == null ) || ( newName.length() == 0 ) )
                {
                    ( (JButton)e.getSource() ).setActionCommand( "" );
                    return;
                }
                
                Font font = widgetsConfig.getNamedFont( newName );
                if ( font != null )
                {
                    JOptionPane.showMessageDialog( window, "This name already exists!", "New Named Font (Error)", JOptionPane.ERROR_MESSAGE );
                    ( (JButton)e.getSource() ).setActionCommand( newName );
                    actionPerformed( e );
                    return;
                }
                
                ( (JButton)e.getSource() ).setActionCommand( "" );
                
                applyNamedFont( newName, getSelectedFont(), widgetsConfig );
                refillNameCombo( widgetsConfig, combo, newName );
                remove.setEnabled( combo.getSelectedIndex() > 0 );
            }
        } );
        
        remove.setEnabled( combo.getSelectedIndex() > 0 );
        
        remove.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                if ( combo.getSelectedIndex() < 1 )
                    return;
                
                Window window = (Window)east.getRootPane().getParent();
                int result = JOptionPane.showConfirmDialog( window, "Do you really want to delete the selected named Font?", "Delete Named Font", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                if ( result == JOptionPane.YES_OPTION )
                {
                    widgetsConfig.removeNamedFont( (String)combo.getSelectedItem() );
                }
                
                refillNameCombo( widgetsConfig, combo, null );
                
                nameSelectionIgnored = true;
                combo.setEditable( false );
                combo.setSelectedIndex( 0 );
                nameSelectionIgnored = false;
                lastNameComboSelectedIndex = combo.getSelectedIndex();
            }
        } );
        
        wrapper.add( east, BorderLayout.EAST );
        
        panel.setBorder( new TitledBorder( "Named Font selection" ) );
        
        panel.add( wrapper );
        
        return ( panel );
    }
    
    protected JList createFontList( String startFamily, final int gameResY )
    {
        /*
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        
        String[] fontNames = new String[ fonts.length ];
        int initialIndex = -1;
        for ( int i = 0; i < fonts.length; i++ )
        {
            fontNames[i] = fonts[i].getName();
            if ( fonts[i].getName().equals( startFont.getName() ) )
                initialIndex = i;
        }
        
        if ( initialIndex == -1 )
        {
            for ( int i = 0; i < fonts.length; i++ )
            {
                if ( fonts[i].getFamily().equals( startFont.getFamily() ) )
                {
                    initialIndex = i;
                    break;
                }
            }
        }
        
        JList list = new JList( fontNames );
        */
        
        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        int initialIndex = -1;
        for ( int i = 0; i < families.length; i++ )
        {
            if ( families[i].equals( startFamily ) )
            {
                initialIndex = i;
                break;
            }
        }
        
        final JList list = new JList( families );
        
        if ( initialIndex != -1 )
        {
            list.setSelectedIndex( initialIndex );
        }
        
        list.addListSelectionListener( new ListSelectionListener()
        {
            @Override
            public void valueChanged( ListSelectionEvent e )
            {
                setSelectedFont( composeSelectedFont(), gameResY );
            }
        } );
        
        return ( list );
    }
    
    protected JPanel createEastPanel( Font startFont, boolean virtual, boolean antiAliased, final int gameResY )
    {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setBorder( new EmptyBorder( 0, 5, 5, 0 ) );
        
        Integer[] sizes = new Integer[ 100 ];
        int initialIndex = -1;
        for ( int i = 0; i < sizes.length; i++ )
        {
            sizes[i] = i + 1;
            if ( i + 1 == startFont.getSize() )
                initialIndex = i;
        }
        
        sizeList = new JList( sizes );
        JScrollPane sizeListScrollPane = new JScrollPane( sizeList );
        
        if ( initialIndex != -1 )
        {
            sizeList.setSelectedIndex( initialIndex );
            sizeList.scrollRectToVisible( sizeList.getCellBounds( initialIndex, initialIndex ) );
        }
        
        panel.add( sizeListScrollPane, BorderLayout.CENTER );
        
        sizeList.addListSelectionListener( new ListSelectionListener()
        {
            @Override
            public void valueChanged( ListSelectionEvent e )
            {
                if ( !e.getValueIsAdjusting() )
                    setSelectedFont( FontUtils.getFontString( getSelectedFontFamily(), getSelectedFontBold(), getSelectedFontItalic(), getSelectedFontSize(), getSelectedFontVirtual(), getSelectedFontAntiAliased() ), gameResY );
            }
        } );
        
        JPanel p = new JPanel( new BorderLayout() );
        
        boldBox = new JCheckBox( "bold" );
        boldBox.setSelected( startFont.isBold() );
        p.add( boldBox, BorderLayout.NORTH );
        
        boldBox.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                setSelectedFont( FontUtils.getFontString( getSelectedFontFamily(), getSelectedFontBold(), getSelectedFontItalic(), getSelectedFontSize(), getSelectedFontVirtual(), getSelectedFontAntiAliased() ), gameResY );
            }
        } );
        
        italicBox = new JCheckBox( "italic" );
        italicBox.setSelected( startFont.isItalic() );
        p.add( italicBox, BorderLayout.CENTER );
        
        italicBox.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                setSelectedFont( FontUtils.getFontString( getSelectedFontFamily(), getSelectedFontBold(), getSelectedFontItalic(), getSelectedFontSize(), getSelectedFontVirtual(), getSelectedFontAntiAliased() ), gameResY );
            }
        } );
        
        JPanel pp = new JPanel( new BorderLayout() );
        
        virtualBox = new JCheckBox( "virtual" );
        virtualBox.setSelected( virtual );
        pp.add( virtualBox, BorderLayout.NORTH );
        
        antiAliasedBox = new JCheckBox( "antialiased" );
        antiAliasedBox.setSelected( antiAliased );
        pp.add( antiAliasedBox, BorderLayout.SOUTH );
        
        p.add( pp, BorderLayout.SOUTH );
        
        virtualBox.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                int size = getSelectedFontSize();
                
                if ( getSelectedFontVirtual() )
                    size = FontUtils.getVirtualFontSize( size, gameResY );
                else
                    size = FontUtils.getConcreteFontSize( size, gameResY );
                
                sizeList.setSelectedValue( size, true );
                
                setSelectedFont( FontUtils.getFontString( getSelectedFontFamily(), getSelectedFontBold(), getSelectedFontItalic(), size, getSelectedFontVirtual(), getSelectedFontAntiAliased() ), gameResY );
            }
        } );
        
        antiAliasedBox.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                setSelectedFont( FontUtils.getFontString( getSelectedFontFamily(), getSelectedFontBold(), getSelectedFontItalic(), getSelectedFontSize(), getSelectedFontVirtual(), getSelectedFontAntiAliased() ), gameResY );
            }
        } );
        
        panel.add( p, BorderLayout.SOUTH );
        
        return ( panel );
    }
    
    private JPanel createButtonsPanel()
    {
        JPanel p = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        
        p.setBorder( new EmptyBorder( 0, 5, 5, 5 ) );
        
        okButton = new JButton( "OK" );
        p.add( okButton );
        
        cancelButton = new JButton( "Cancel" );
        p.add( cancelButton );
        
        return ( p );
    }
    
    public FontChooser( String startFont, WidgetsConfiguration widgetsConfig )
    {
        super( new BorderLayout() );
        
        JPanel wrapper = new JPanel( new BorderLayout( 5, 5 ) );
        wrapper.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        
        Font font = widgetsConfig.getNamedFont( startFont );
        boolean virtual, antiAliased;
        boolean isName = ( font != null );
        if ( isName )
        {
            String fontString = widgetsConfig.getNamedFontString( startFont );
            font = FontUtils.parseVirtualFont( fontString, false );
            virtual = widgetsConfig.getNamedFontVirtual( startFont );
            antiAliased = FontUtils.parseAntiAliasFlag( fontString, false );
        }
        else
        {
            font = FontUtils.parseVirtualFont( startFont, false );
            virtual = FontUtils.parseVirtualFlag( startFont, false );
            antiAliased = FontUtils.parseAntiAliasFlag( startFont, false );
        }
        
        wrapper.add( createNamedFontSelector( isName ? startFont : null, widgetsConfig ), BorderLayout.NORTH );
        
        JPanel main = new JPanel( new BorderLayout( 0, 5 ) );
        
        
        sampleLabel = new JLabel( "The quick brown fox jumps over the lazy dog" );
        sampleLabel.setFont( font );
        JPanel south0 = new JPanel( new GridLayout( 1, 1 ) );
        south0.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        JPanel south1 = new JPanel( new GridLayout( 1, 1 ) );
        south1.setBorder( new EmptyBorder( 15, 15, 15, 15 ) );
        south1.add( sampleLabel );
        south0.add( south1 );
        
        main.add( south0, BorderLayout.SOUTH );
        
        fontNamesList = createFontList( font.getFamily(), widgetsConfig.getGameResY() );
        JScrollPane fontNamesListScrollPanel = new JScrollPane( fontNamesList );
        if ( fontNamesList.getSelectedIndex() != -1 )
            fontNamesList.scrollRectToVisible( fontNamesList.getCellBounds( fontNamesList.getSelectedIndex(), fontNamesList.getSelectedIndex() ) );
        main.add( fontNamesListScrollPanel, BorderLayout.CENTER );
        
        final JPanel eastPanel = createEastPanel( font, virtual, antiAliased, widgetsConfig.getGameResY() );
        main.add( eastPanel, BorderLayout.EAST );
        
        wrapper.add( main, BorderLayout.CENTER );
        
        wrapper.add( createButtonsPanel(), BorderLayout.SOUTH );
        
        this.add( wrapper );
        
        this.setPreferredSize( new Dimension( 600, 350 ) );
    }
    
    public static class FontChooserDialog extends JDialog
    {
        private static final long serialVersionUID = 3853781746419856706L;
        
        private final FontChooser fontChooser;
        
        public final FontChooser getFontChooser()
        {
            return ( fontChooser );
        }
        
        /**
         * Gets the selected Font.
         * 
         * @return the selected Font.
         */
        public final String getSelectedFont()
        {
            return ( getFontChooser().getSelectedFont() );
        }
        
        public final String getSelectedFontName()
        {
            return ( getFontChooser().getSelectedFontName() );
        }
        
        public final boolean getValueChanged()
        {
            return ( getFontChooser().getValueChanged() );
        }
        
        private void init( final FontChooser fontChooser, final String startFont, final WidgetsConfiguration widgetsConfig )
        {
            this.setContentPane( fontChooser );
            this.pack();
            
            this.addWindowListener( new WindowAdapter()
            {
                public void windowClosing( WindowEvent e )
                {
                    //d.getFontChooser().setSelectedFont( null );
                }
            } );
            
            fontChooser.getOKButton().addActionListener( new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    if ( getSelectedFontName() == null )
                    {
                        fontChooser.valueChanged = !startFont.equals( getSelectedFont() );                            
                    }
                    else
                    {
                        fontChooser.setSelectedFont( fontChooser.composeSelectedFont(), widgetsConfig.getGameResY() );
                        fontChooser.valueChanged = applyNamedFont( getSelectedFontName(), getSelectedFont(), widgetsConfig ) || !getSelectedFontName().equals( startFont );
                        
                        fontChooser.setAllWidgetsDirty( widgetsConfig );
                    }
                    
                    FontChooserDialog.this.setVisible( false );
                }
            } );
            
            fontChooser.getCancelButton().addActionListener( new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    FontChooserDialog.this.getFontChooser().setSelectedFont( null, 0 );
                    FontChooserDialog.this.setVisible( false );
                }
            } );
            
            this.setModal( true );
        }
        
        public FontChooserDialog( java.awt.Dialog owner, String title, FontChooser fontChooser, String startFont, WidgetsConfiguration widgetsConfig )
        {
            super ( owner, title );
            
            init( fontChooser, startFont, widgetsConfig );
            
            this.fontChooser = fontChooser;
        }
        
        public FontChooserDialog( java.awt.Frame owner, String title, FontChooser fontChooser, String startFont, WidgetsConfiguration widgetsConfig )
        {
            super ( owner, title );
            
            init( fontChooser, startFont, widgetsConfig );
            
            this.fontChooser = fontChooser;
        }
    }
    
    private static FontChooserDialog getAsDialogInternal( Object owner, String title, String startFont, WidgetsConfiguration widgetsConfig )
    {
        final FontChooserDialog d;
        if ( owner instanceof java.awt.Dialog )
            d = new FontChooserDialog( (java.awt.Dialog)owner, title, new FontChooser( startFont, widgetsConfig ), startFont, widgetsConfig );
        else
            d = new FontChooserDialog( (java.awt.Frame)owner, title, new FontChooser( startFont, widgetsConfig ), startFont, widgetsConfig );
        
        return ( d );
    }
    
    public static FontChooserDialog getAsDialog( java.awt.Frame owner, String title, String startFont, WidgetsConfiguration widgetsConfig )
    {
        return ( getAsDialogInternal( owner, title, startFont, widgetsConfig ) );
    }
    
    public static FontChooserDialog getAsDialog( java.awt.Frame owner, String startFont, WidgetsConfiguration widgetsConfig )
    {
        return ( getAsDialog( owner, "Select a Font", startFont, widgetsConfig ) );
    }
    
    public static FontChooserDialog getAsDialog( java.awt.Dialog owner, String title, String startFont, WidgetsConfiguration widgetsConfig )
    {
        return ( getAsDialogInternal( owner, title, startFont, widgetsConfig ) );
    }
    
    public static FontChooserDialog getAsDialog( java.awt.Dialog owner, String startFont, WidgetsConfiguration widgetsConfig )
    {
        return ( getAsDialog( owner, "Select a Font", startFont, widgetsConfig ) );
    }
}
