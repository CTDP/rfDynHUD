package org.jagatoo.gui.awt_swing.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FlatWidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

import org.openmali.vecmath2.util.ColorUtils;

public class ColorChooser extends JPanel
{
    private static final long serialVersionUID = 1202318317652636738L;
    
    private class ComponentSelector extends JPanel
    {
        private static final long serialVersionUID = 2639201006961993366L;
        
        private final JLabel caption;
        private final JSlider slider;
        private final JTextField textField;
        
        public void setValue( int value )
        {
            slider.setValue( value );
            textField.setText( String.valueOf( value ) );
        }
        
        public final int getValue()
        {
            return ( slider.getValue() );
        }
        
        public ComponentSelector( String caption, int initialValue )
        {
            super( new BorderLayout() );
            
            this.caption = new JLabel( caption );
            this.caption.setFont( new Font( this.caption.getFont().getFontName(), Font.BOLD, this.caption.getFont().getSize() ) );
            this.slider = new JSlider( 0, 255, initialValue );
            this.textField = new JTextField( String.valueOf( initialValue ) );
            
            slider.addChangeListener( new ChangeListener()
            {
                public void stateChanged( ChangeEvent e )
                {
                    textField.setText( String.valueOf( slider.getValue() ) );
                    ColorChooser.this.updateSelectedColorFromSelectors();
                }
            } );
            
            textField.addActionListener( new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    try
                    {
                        setValue( Integer.parseInt( textField.getText() ) );
                        ColorChooser.this.updateSelectedColorFromSelectors();
                    }
                    catch ( Throwable t )
                    {
                        textField.setText( String.valueOf( slider.getValue() ) );
                    }
                }
            } );
            
            textField.addFocusListener( new FocusAdapter()
            {
                @Override
                public void focusLost( FocusEvent e )
                {
                    try
                    {
                        setValue( Integer.parseInt( textField.getText() ) );
                    }
                    catch ( Throwable t )
                    {
                        textField.setText( String.valueOf( slider.getValue() ) );
                    }
                }
            } );
            
            this.caption.setMinimumSize( new Dimension( 40, 20 ) );
            this.caption.setPreferredSize( new Dimension( 40, 20 ) );
            this.caption.setMaximumSize( new Dimension( 40, 20 ) );
            
            this.slider.setPreferredSize( new Dimension( 500, 20 ) );
            this.slider.setBorder( new EmptyBorder( 0, 0, 0, 5 ) );
            
            this.textField.setMinimumSize( new Dimension( 50, 20 ) );
            this.textField.setPreferredSize( new Dimension( 50, 20 ) );
            this.textField.setMaximumSize( new Dimension( 50, 20 ) );
            
            this.add( this.caption, BorderLayout.WEST );
            this.add( this.slider, BorderLayout.CENTER );
            this.add( this.textField, BorderLayout.EAST );
        }
    }
    
    private JComboBox combo;
    private int lastNameComboSelectedIndex = -1;
    private String[] namesCache;
    private JPanel colorTriangle;
    private JPanel sampleColor;
    private ComponentSelector redSelector;
    private ComponentSelector greenSelector;
    private ComponentSelector blueSelector;
    private ComponentSelector alphaSelector;
    
    private JLabel hexCaption;
    private JTextField hexValue;
    
    private String selectedColor;
    private boolean valueChanged = false;
    
    private JButton okButton;
    private JButton cancelButton;
    
    private String composeSelectedColor()
    {
        return ( ColorUtils.colorToHex( redSelector.getValue(), greenSelector.getValue(), blueSelector.getValue(), alphaSelector.getValue() ) );
    }
    
    protected void setSelectedColor( Color color, boolean setResult )
    {
        if ( color != null )
        {
            redSelector.setValue( color.getRed() );
            greenSelector.setValue( color.getGreen() );
            blueSelector.setValue( color.getBlue() );
            alphaSelector.setValue( color.getAlpha() );
            
            sampleColor.setBackground( new Color( color.getRed(), color.getGreen(), color.getBlue() ) );
            
            if ( setResult )
                hexValue.setText( ColorUtils.colorToHex( color ) );
            
            this.selectedColor = ColorUtils.colorToHex( color );
        }
        else
        {
            this.selectedColor = null;
        }
        
        this.repaint();
    }
    
    protected final void setSelectedColor( Color color )
    {
        setSelectedColor( color, true );
    }
    
    /**
     * Gets the selected Color.
     * 
     * @return the selected Color.
     */
    public final String getSelectedColor()
    {
        return ( selectedColor );
    }
    
    public final String getSelectedColorName()
    {
        if ( combo.getSelectedIndex() == 0 )
            return ( null );
        
        return ( (String)combo.getSelectedItem() );
    }
    
    public final boolean getValueChanged()
    {
        return ( valueChanged );
    }
    
    private void updateSelectedColorFromSelectors()
    {
        setSelectedColor( new Color( redSelector.getValue(), greenSelector.getValue(), blueSelector.getValue(), alphaSelector.getValue() ) );
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
        
        Set<String> namesSet = widgetsConfig.getColorNames();
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
    
    private static void resetColorPropertyValues( List<Property> list )
    {
        for ( Property prop : list )
        {
            if ( prop instanceof ColorProperty )
            {
                ColorProperty colorProp = (ColorProperty)prop;
                colorProp.setValue( colorProp.getValue() );
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
            widget.getProperties( propsCont, true );
            
            resetColorPropertyValues( propsCont.getList() );
        }
    }
    
    private boolean nameSelectionIgnored = false;
    
    private void checkNameComboValue( int selIndex, String newValue, WidgetsConfiguration widgetsConfig )
    {
        String oldValue = namesCache[selIndex - 1];
        
        if ( !oldValue.equals( newValue ) )
        {
            widgetsConfig.renameColor( oldValue, newValue );
            
            refillNameCombo( widgetsConfig, combo, newValue );
            lastNameComboSelectedIndex = combo.getSelectedIndex();
            
            setAllWidgetsDirty( widgetsConfig );
        }
    }
    
    private static boolean applyNamedColor( String colorKey, Color color, WidgetsConfiguration widgetsConfig )
    {
        if ( widgetsConfig.addNamedColor( colorKey, color ) )
        {
            FlatWidgetPropertiesContainer wpc = new FlatWidgetPropertiesContainer();
            
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
            {
                Widget widget = widgetsConfig.getWidget( i );
                
                wpc.clear();
                widget.getProperties( wpc, true );
                
                boolean propFound = false;
                
                for ( Property prop : wpc.getList() )
                {
                    if ( prop instanceof ColorProperty )
                    {
                        ColorProperty colorProp = (ColorProperty)prop;
                        
                        if ( colorKey.equals( colorProp.getColorKey() ) )
                        {
                            colorProp.refresh();
                            __WPrivilegedAccess.onPropertyChanged( colorProp, colorKey, colorKey, widget );
                            
                            propFound = true;
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
    
    protected JPanel createNamedFontSelector( String currentNamedColor, final WidgetsConfiguration widgetsConfig )
    {
        JPanel panel = new JPanel( new BorderLayout() );
        
        JPanel wrapper = new JPanel( new BorderLayout() );
        wrapper.setBorder( new EmptyBorder( 0, 5, 5, 5 ) );
        
        JPanel west = new JPanel( new BorderLayout() );
        
        final JButton remove = new JButton( "remove" );
        
        combo = new JComboBox();
        refillNameCombo( widgetsConfig, combo, currentNamedColor );
        if ( currentNamedColor == null )
            combo.setSelectedIndex( 0 );
        lastNameComboSelectedIndex = combo.getSelectedIndex();
        combo.setEditable( combo.getSelectedItem().equals( currentNamedColor ) );
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
                            applyNamedColor( String.valueOf( e.getItem() ), ColorUtils.hexToColor( composeSelectedColor() ), widgetsConfig );
                        }
                        break;
                    case ItemEvent.SELECTED:
                        if ( combo.getSelectedIndex() == 0 )
                        {
                            setSelectedColor( ColorUtils.hexToColor( composeSelectedColor() ) );
                        }
                        else if ( combo.getSelectedIndex() > 0 )
                        {
                            String colorName = (String)e.getItem();
                            
                            Color color = widgetsConfig.getNamedColor( colorName );
                            
                            setSelectedColor( color );
                        }
                        
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
            @Override
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
        add.setToolTipText( "Add a new named Color" );
        add.setPreferredSize( new Dimension( 75, 20 ) );
        remove.setActionCommand( "" );
        remove.setToolTipText( "Remove the current named Color" );
        remove.setPreferredSize( new Dimension( 75, 20 ) );
        east.add( add, BorderLayout.WEST );
        east.add( remove, BorderLayout.EAST );
        
        add.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                String initialValue = ( (JButton)e.getSource() ).getActionCommand();
                Window window = (Window)east.getRootPane().getParent();
                String newName = (String)JOptionPane.showInputDialog( window, "Please type the name for the new named Color.", "New Named Color", JOptionPane.INFORMATION_MESSAGE, null, null, initialValue );
                if ( ( newName == null ) || ( newName.length() == 0 ) )
                {
                    ( (JButton)e.getSource() ).setActionCommand( "" );
                    return;
                }
                
                Color color = widgetsConfig.getNamedColor( newName );
                if ( color != null )
                {
                    JOptionPane.showMessageDialog( window, "This name already exists!", "New Named Color (Error)", JOptionPane.ERROR_MESSAGE );
                    ( (JButton)e.getSource() ).setActionCommand( newName );
                    actionPerformed( e );
                    return;
                }
                
                ( (JButton)e.getSource() ).setActionCommand( "" );
                
                applyNamedColor( newName, ColorUtils.hexToColor( composeSelectedColor() ), widgetsConfig );
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
                int result = JOptionPane.showConfirmDialog( window, "Do you really want to delete the selected named Color?", "Delete Named Color", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                if ( result == JOptionPane.YES_OPTION )
                {
                    widgetsConfig.removeNamedColor( (String)combo.getSelectedItem() );
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
        
        panel.setBorder( new TitledBorder( "Named Color selection" ) );
        
        panel.add( wrapper );
        
        return ( panel );
    }
    
    /*
    private static final float pointsDistanceSquared( float px1, float py1, float px2, float py2 )
    {
        float result = 0.0f;
        
        float d = px1 - px2;
        result += d * d;
        d = py1 - py2;
        result += d * d;
        
        return ( result );
    }
    
    private static final float pointsDistance( float px1, float py1, float px2, float py2 )
    {
        return ( (float)Math.sqrt( pointsDistanceSquared( px1, py1, px2, py2 ) ) );
    }
    */
    
    private static final float sq( float x )
    {
        return ( x * x );
    }
    
    @SuppressWarnings( "unused" )
    private static final Color getColorForPoint( int px, int py, int width, int height, int length )
    {
        /*
        float x1 = px;
        float y1 = length - 1;
        float x2 = length - 1;
        float y2 = length - 1;
        float x3 = length / 2.0f;
        float y3 = 0;
        
        float lambda = ( ( x3 - x1 ) * x2 + ( y3 - y1 ) * y2 - x3 * px + x2 * px - y3 * py + y1 * py ) / sq( x3 - x1 ) + sq( y3 - y1 );
        
        int red = 0;
        //int green = Math.round( pointsDistance( px, py, width - 1, height - 1 ) );
        
        float x_ = px + lambda * ( x3 - x1 );
        float y_ = py + lambda * ( y3 - y1 );
        int green = Math.round( pointsDistance( px, py, x_, y_ ) ) / height;
        int blue = py / height;
        */
        
        int blue = 255 - py * 255 / height;
        
        AffineTransform at = AffineTransform.getRotateInstance( Math.toRadians( 120 ), width / 2, height / 2 );
        Point p0 = new Point( px, py );
        Point p1 = new Point();
        at.transform( p0, p1 );
        //System.err.println( p1.y + ", " + height );
        int red = Math.max( 0, Math.min( 255 - p1.y * 255 / height, 255 ) );
        
        at = AffineTransform.getRotateInstance( Math.toRadians( -120 ), width / 2, height / 2 );
        p0 = new Point( px, py );
        at.transform( p0, p1 );
        int green = Math.max( 0, Math.min( 255 - p1.y * 255 / height, 255 ) );
        
        //System.err.println( p0 + ", " + p1 + ", " + red + ", " + green + ", " + blue );
        
        return ( new Color( red, green, blue ) );
    }
    
    @SuppressWarnings( "unused" )
    protected void drawColorTriangle( Color color, Graphics2D g2, int x0, int y0, int width, int height )
    {
        height = (int)Math.round( Math.sqrt( 0.75 * sq( width - x0 - x0 ) ) ) + y0 + y0;
        
        for ( int y = 0; y < height; y++ )
        {
            int w = width * y / height;
            int wh = w / 2;
            
            for ( int x = 0; x < w; x++ )
            {
                int x2 = width / 2 - wh + x;
                Color c = getColorForPoint( x2, y, width, height, width );
                
                g2.setColor( c );
                g2.drawLine( x0 + x2, y0 + y, x0 + x2, y0 + y );
            }
        }
        
        /*
        g2.setColor( Color.BLACK );
        
        int[] xPoints = new int[]
        {
            x0, x0 + width - 1, x0 + width / 2, x0
        };
        
        int[] yPoints = new int[]
        {
            y0 + height -1, y0 + height - 1, y0, y0 + height -1
        };
                                
        g2.drawPolyline( xPoints, yPoints, 4 );
        */
    }
    
    @SuppressWarnings( "unused" )
    protected JPanel createWestPanel( Color startColor )
    {
        JPanel panel = new JPanel( new BorderLayout() );
        
        colorTriangle = new JPanel()
        {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent( Graphics g )
            {
                super.paintComponent( g );
                
                drawColorTriangle( ColorUtils.hexToColor( selectedColor ), (Graphics2D)g, 5, 5, getWidth() - 10, getHeight() - 10 );
            }
        };
        
        JPanel triangWrapper = new JPanel( new BorderLayout() );
        triangWrapper.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        triangWrapper.add( colorTriangle, BorderLayout.CENTER );
        panel.add( triangWrapper, BorderLayout.CENTER );
        
        JPanel sampleWrapper = new JPanel( new BorderLayout() );
        sampleWrapper.setBorder( new EmptyBorder( 10, 0, 0, 0 ) );
        sampleColor = new JPanel();
        sampleColor.setPreferredSize( new Dimension( Integer.MAX_VALUE, 20 ) );
        sampleWrapper.add( sampleColor, BorderLayout.CENTER );
        panel.add( sampleWrapper, BorderLayout.SOUTH );
        
        panel.setMinimumSize( new Dimension( 150, 150 ) );
        panel.setPreferredSize( new Dimension( 150, 150 ) );
        panel.setMaximumSize( new Dimension( 150, Integer.MAX_VALUE ) );
        
        return ( panel );
    }
    
    protected JPanel createEastPanel( Color startColor )
    {
        JPanel panel = new JPanel( new BorderLayout() );
        
        JPanel center = new JPanel();
        center.setLayout( new BoxLayout( center, BoxLayout.Y_AXIS ) );
        
        this.redSelector = new ComponentSelector( "Red", startColor.getRed() );
        this.greenSelector = new ComponentSelector( "Green", startColor.getGreen() );
        this.blueSelector = new ComponentSelector( "Blue", startColor.getBlue() );
        this.alphaSelector = new ComponentSelector( "Alpha", startColor.getAlpha() );
        
        redSelector.setMaximumSize( new Dimension( Integer.MAX_VALUE, 20 ) );
        greenSelector.setMaximumSize( new Dimension( Integer.MAX_VALUE, 20 ) );
        blueSelector.setMaximumSize( new Dimension( Integer.MAX_VALUE, 20 ) );
        alphaSelector.setMaximumSize( new Dimension( Integer.MAX_VALUE, 20 ) );
        
        center.add( redSelector );
        center.add( Box.createVerticalStrut( 5 ) );
        center.add( greenSelector );
        center.add( Box.createVerticalStrut( 5 ) );
        center.add( blueSelector );
        center.add( Box.createVerticalStrut( 5 ) );
        center.add( alphaSelector );
        center.add( Box.createVerticalGlue() );
        
        panel.add( center, BorderLayout.CENTER );
        
        JPanel bottom = new JPanel( new BorderLayout() );
        
        hexCaption = new JLabel( "Hex:" );
        hexCaption.setFont( new Font( hexCaption.getFont().getFontName(), Font.BOLD, hexCaption.getFont().getSize() ) );
        hexCaption.setPreferredSize( new Dimension( 40, 20 ) );
        hexCaption.setMinimumSize( new Dimension( 40, 20 ) );
        hexCaption.setMaximumSize( new Dimension( 40, 20 ) );
        hexValue = new JTextField();
        
        hexValue.addKeyListener( new KeyAdapter()
        {
            @Override
            public void keyPressed( KeyEvent e )
            {
                try
                {
                    setSelectedColor( ColorUtils.hexToColor( hexValue.getText() ), false );
                }
                catch ( Throwable t )
                {
                }
            }
        } );
        
        bottom.add( hexCaption, BorderLayout.WEST );
        bottom.add( hexValue, BorderLayout.CENTER );
        bottom.setBorder( new EmptyBorder( 10, 0, 0, 0 ) );
        
        panel.add( bottom, BorderLayout.SOUTH );
        
        panel.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
        
        return ( panel );
    }
    
    private JPanel createButtonsPanel()
    {
        JPanel p = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        
        okButton = new JButton( "OK" );
        p.add( okButton );
        
        cancelButton = new JButton( "Cancel" );
        p.add( cancelButton );
        
        return ( p );
    }
    
    public ColorChooser( String startColor, WidgetsConfiguration widgetsConfig )
    {
        super( new BorderLayout() );
        
        JPanel main = new JPanel( new BorderLayout( 0, 5 ) );
        
        Color color = widgetsConfig.getNamedColor( startColor );
        boolean isName = ( color != null );
        if ( !isName )
        {
            color = ColorUtils.hexToColor( startColor, false );
            if ( color == null )
                color = ColorProperty.FALLBACK_COLOR;
        }
        
        main.add( createNamedFontSelector( isName ? startColor : null, widgetsConfig ), BorderLayout.NORTH );
        
        main.add( createWestPanel( color ), BorderLayout.WEST );
        main.add( createEastPanel( color ), BorderLayout.CENTER );
        main.add( createButtonsPanel(), BorderLayout.SOUTH );
        
        main.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        
        this.add( main, BorderLayout.CENTER );
        
        this.setPreferredSize( new Dimension( 400, 270 ) );
        
        updateSelectedColorFromSelectors();
    }
    
    public static class ColorChooserDialog extends JDialog
    {
        private static final long serialVersionUID = 3853781746419856706L;
        
        private final ColorChooser colorChooser;
        
        public final ColorChooser getColorChooser()
        {
            return ( colorChooser );
        }
        
        /**
         * Gets the selected Color.
         * 
         * @return the selected Color.
         */
        public final String getSelectedColor()
        {
            return ( getColorChooser().getSelectedColor() );
        }
        
        public final String getSelectedColorName()
        {
            return ( getColorChooser().getSelectedColorName() );
        }
        
        public final boolean getValueChanged()
        {
            return ( getColorChooser().getValueChanged() );
        }
        
        private void init( final ColorChooser colorChooser, final String startColor, final WidgetsConfiguration widgetsConfig )
        {
            this.setContentPane( colorChooser );
            this.pack();
            this.setResizable( false );
            
            this.addWindowListener( new WindowAdapter()
            {
                @Override
                public void windowClosing( WindowEvent e )
                {
                    //d.getFontChooser().setSelectedFont( null );
                }
            } );
            
            colorChooser.getOKButton().addActionListener( new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    if ( getSelectedColorName() == null )
                    {
                        colorChooser.valueChanged = !startColor.equals( getSelectedColor() );                            
                    }
                    else
                    {
                        Color color = ColorUtils.hexToColor( colorChooser.composeSelectedColor() );
                        colorChooser.setSelectedColor( color );
                        colorChooser.valueChanged = applyNamedColor( getSelectedColorName(), color, widgetsConfig ) || !getSelectedColorName().equals( startColor );
                        
                        colorChooser.setAllWidgetsDirty( widgetsConfig );
                    }
                    
                    ColorChooserDialog.this.setVisible( false );
                }
            } );
            
            colorChooser.getCancelButton().addActionListener( new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    ColorChooserDialog.this.getColorChooser().setSelectedColor( null );
                    ColorChooserDialog.this.setVisible( false );
                }
            } );
            
            this.setModal( true );
        }
        
        public ColorChooserDialog( java.awt.Dialog owner, String title, ColorChooser colorChooser, String startColor, WidgetsConfiguration widgetsConfig )
        {
            super ( owner, title );
            
            init( colorChooser, startColor, widgetsConfig );
            
            this.colorChooser = colorChooser;
        }
        
        public ColorChooserDialog( java.awt.Frame owner, String title, ColorChooser colorChooser, String startColor, WidgetsConfiguration widgetsConfig )
        {
            super ( owner, title );
            
            init( colorChooser, startColor, widgetsConfig );
            
            this.colorChooser = colorChooser;
        }
    }
    
    private static ColorChooserDialog getAsDialogInternal( Object owner, String title, String startColor, WidgetsConfiguration widgetsConfig )
    {
        final ColorChooserDialog d;
        if ( owner instanceof java.awt.Dialog )
            d = new ColorChooserDialog( (java.awt.Dialog)owner, title, new ColorChooser( startColor, widgetsConfig ), startColor, widgetsConfig );
        else
            d = new ColorChooserDialog( (java.awt.Frame)owner, title, new ColorChooser( startColor, widgetsConfig ), startColor, widgetsConfig );
        
        return ( d );
    }
    
    public static ColorChooserDialog getAsDialog( java.awt.Frame owner, String title, String startColor, WidgetsConfiguration widgetsConfig )
    {
        return ( getAsDialogInternal( owner, title, startColor, widgetsConfig ) );
    }
    
    public static ColorChooserDialog getAsDialog( java.awt.Frame owner, String startColor, WidgetsConfiguration widgetsConfig )
    {
        return ( getAsDialog( owner, "Select a Color", startColor, widgetsConfig ) );
    }
    
    public static ColorChooserDialog getAsDialog( java.awt.Dialog owner, String title, String startColor, WidgetsConfiguration widgetsConfig )
    {
        return ( getAsDialogInternal( owner, title, startColor, widgetsConfig ) );
    }
    
    public static ColorChooserDialog getAsDialog( java.awt.Dialog owner, String startColor, WidgetsConfiguration widgetsConfig )
    {
        return ( getAsDialog( owner, "Select a Color", startColor, widgetsConfig ) );
    }
}
