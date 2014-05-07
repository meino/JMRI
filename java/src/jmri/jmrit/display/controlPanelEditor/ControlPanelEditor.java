package jmri.jmrit.display.controlPanelEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.ImageIndexEditor;

import jmri.jmrit.display.*;

import java.awt.*;
import java.awt.event.*;
//import java.awt.event.KeyEvent;

import java.awt.geom.Rectangle2D;

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.HashMap;
import javax.swing.*;

import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.shape.ShapeDrawer;
import jmri.jmrit.display.palette.ItemPalette;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.WarrantTableAction;
import jmri.util.HelpUtil;

/**
 * Provides a simple editor for adding jmri.jmrit.display items
 * to a captive JFrame.
 * <P>GUI is structured as a band of common parameters across the
 * top, then a series of things you can add.
 * <P>
 * All created objects are put specific levels depending on their
 * type (higher levels are in front):
 * <UL>
 * <LI>BKG background
 * <LI>ICONS icons and other drawing symbols
 * <LI>LABELS text labels
 * <LI>TURNOUTS turnouts and other variable track items
 * <LI>SENSORS sensors and other independently modified objects
 * </UL>
 * Note that higher numbers appear behind lower numbers.
 * <P>
 * The "contents" List keeps track of all the objects added to the target
 * frame for later manipulation.
 * Extends the behavior it shares with PanelPro
 * DnD implemented at JDK 1.2 for backward compatibility
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2009, 2010, 2011
 * @version $Revision: 21062 $
 * 
 */

public class ControlPanelEditor extends Editor implements DropTargetListener, ClipboardOwner/*, KeyListener*/ {

    public boolean _debug;
    protected JMenuBar _menuBar;
    private JMenu _editorMenu;
    protected JMenu _editMenu;
    protected JMenu _fileMenu;
    protected JMenu _optionMenu;
    protected JMenu _iconMenu;
    protected JMenu _zoomMenu;
    private JMenu _markerMenu;
    private JMenu _warrantMenu;
    private JMenu _circuitMenu;
    private JMenu _drawMenu;
    private CircuitBuilder _circuitBuilder;
    private ArrayList <Positionable>_secondSelectionGroup;
    private ShapeDrawer _shapeDrawer;
    private ItemPalette _itemPalette;
    private boolean _disableShapeSelection;
    private boolean _disablePortalSelection = true;		// only select PortalIcon in CircuitBuilder

    private JCheckBoxMenuItem useGlobalFlagBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxGlobalFlags"));
//    private JCheckBoxMenuItem editableBox = new JCheckBoxMenuItem(Bundle.getMessage("CloseEditor"));
    private JCheckBoxMenuItem positionableBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxPositionable"));
    private JCheckBoxMenuItem controllingBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxControlling"));
    private JCheckBoxMenuItem showTooltipBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxShowTooltips"));
    private JCheckBoxMenuItem hiddenBox = new JCheckBoxMenuItem(Bundle.getMessage("CheckBoxHidden"));
    private JCheckBoxMenuItem disableShapeSelect = new JCheckBoxMenuItem(Bundle.getMessage("disableShapeSelect"));
    private JRadioButtonMenuItem scrollBoth = new JRadioButtonMenuItem(Bundle.getMessage("ScrollBoth"));
    private JRadioButtonMenuItem scrollNone = new JRadioButtonMenuItem(Bundle.getMessage("ScrollNone"));
    private JRadioButtonMenuItem scrollHorizontal = new JRadioButtonMenuItem(Bundle.getMessage("ScrollHorizontal"));
    private JRadioButtonMenuItem scrollVertical = new JRadioButtonMenuItem(Bundle.getMessage("ScrollVertical"));

    public ControlPanelEditor() {
    }

    public ControlPanelEditor(String name) {
        super(name);
        init(name);
    }

    protected void init(String name) {
        setVisible(false);
        _debug = log.isDebugEnabled();
        java.awt.Container contentPane = this.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // make menus
        setGlobalSetsLocalFlag(false);
        setUseGlobalFlag(false);
        _menuBar = new JMenuBar();
        _circuitBuilder = new CircuitBuilder(this);
        _shapeDrawer = new ShapeDrawer(this);
        makeDrawMenu();
        makeCircuitMenu();
        makeIconMenu();
        makeZoomMenu();
        makeMarkerMenu();
        makeOptionMenu();
        makeEditMenu();
        makeWarrantMenu();
        makeFileMenu();

        setJMenuBar(_menuBar);
        addHelpMenu("package.jmri.jmrit.display.ControlPanelEditor", true);
        _itemPalette = new ItemPalette(Bundle.getMessage("MenuItemItemPallette"), this);        		

        super.setTargetPanel(null, null);
        super.setTargetPanelSize(300, 300);
        makeDataFlavors(); 

        // set scrollbar initial state
        setScroll(SCROLL_BOTH);
        scrollBoth.setSelected(true);
        super.setDefaultToolTip(new ToolTip(null,0,0,new Font("Serif", Font.PLAIN, 12),
                                            Color.black, new Color(255, 250, 210), Color.black));
        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(this);
        pack();
        setVisible(true);
 //       addKeyListener(this);
        class makeCatalog extends SwingWorker<CatalogPanel, Object> {
            @Override
            public CatalogPanel doInBackground() {
                return CatalogPanel.makeDefaultCatalog();
            }
        }
        (new makeCatalog()).execute();
        if (_debug) log.debug("Init SwingWorker launched");
    }
    
    protected void makeIconMenu() {
        _iconMenu = new JMenu(Bundle.getMessage("MenuIcon"));
        _menuBar.add(_iconMenu, 0);
        JMenuItem mi = new JMenuItem(Bundle.getMessage("MenuItemItemPallette"));
        mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _itemPalette.setVisible(true);        	
                }
            });
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        _iconMenu.add(mi);
        _iconMenu.add(new jmri.jmrit.beantable.OBlockTableAction(Bundle.getMessage("MenuItemOBlockTable"))); 
        mi = (JMenuItem)_iconMenu.getMenuComponent(1); 
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));        		
        _iconMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemTableList")));                                                  
        mi = (JMenuItem)_iconMenu.getMenuComponent(2); 
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
    }
    
    protected void makeCircuitMenu() {
    	if (_circuitMenu==null) {
        	_circuitMenu = _circuitBuilder.makeMenu();
    	}
        _menuBar.add(_circuitMenu, 0);
    }

    protected void makeDrawMenu() {
    	if (_drawMenu==null) {
    		_drawMenu = _shapeDrawer.makeMenu();
            _drawMenu.add(disableShapeSelect);
            disableShapeSelect.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        _disableShapeSelection = disableShapeSelect.isSelected();
                    }
                });
    	}
        _menuBar.add(_drawMenu, 0);
    }

    protected void makeZoomMenu() {
        _zoomMenu = new JMenu(Bundle.getMessage("MenuZoom"));
        _menuBar.add(_zoomMenu, 0);
        JMenuItem addItem = new JMenuItem(Bundle.getMessage("NoZoom"));
        _zoomMenu.add(addItem);
        addItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    zoomRestore();
                }
            });

        addItem = new JMenuItem(Bundle.getMessage("Zoom"));
        _zoomMenu.add(addItem);
        PositionableJComponent z = new PositionableJComponent(this);
        z.setScale(getPaintScale());
        addItem.addActionListener(CoordinateEdit.getZoomEditAction(z));

        addItem = new JMenuItem(Bundle.getMessage("ZoomFit"));
        _zoomMenu.add(addItem);
        addItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					zoomToFit();
                }
            });
    }
    
    protected void makeWarrantMenu() {
        _warrantMenu = jmri.jmrit.logix.WarrantTableAction.makeWarrantMenu();
        if (_warrantMenu==null) {
        	_warrantMenu = new JMenu(ResourceBundle.getBundle("jmri.jmrit.logix.WarrantBundle").getString("MenuWarrant"));
            JMenuItem aboutItem = new JMenuItem("About Warrants");
            HelpUtil.getGlobalHelpBroker().enableHelpOnButton(aboutItem, "package.jmri.jmrit.logix.Warrant", null);
            _warrantMenu.add(aboutItem);
            aboutItem = new JMenuItem("About OBlocks&Portals");
            HelpUtil.getGlobalHelpBroker().enableHelpOnButton(aboutItem, "package.jmri.jmrit.logix.OBlockTable", null);
            _warrantMenu.add(aboutItem);
        }
    	_menuBar.add(_warrantMenu, 0);
    }
    
    protected void makeMarkerMenu() {
        _markerMenu = new JMenu(Bundle.getMessage("MenuMarker"));
        _menuBar.add(_markerMenu);
        _markerMenu.add(new AbstractAction(Bundle.getMessage("AddLoco")){
        	public void actionPerformed(ActionEvent e) {
        		locoMarkerFromInput();
            }
        });
        _markerMenu.add(new AbstractAction(Bundle.getMessage("AddLocoRoster")){
        	public void actionPerformed(ActionEvent e) {
        		locoMarkerFromRoster();
            }
        });
        _markerMenu.add(new AbstractAction(Bundle.getMessage("RemoveMarkers")){
        	public void actionPerformed(ActionEvent e) {
        		removeMarkers();
            }
        });
    }
    
    protected void makeOptionMenu() {
        _optionMenu = new JMenu(Bundle.getMessage("MenuOption"));
        _menuBar.add(_optionMenu, 0);
        // use globals item
        _optionMenu.add(useGlobalFlagBox);
        useGlobalFlagBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setUseGlobalFlag(useGlobalFlagBox.isSelected());
                }
            });                    
        useGlobalFlagBox.setSelected(useGlobalFlag());
        // positionable item
        _optionMenu.add(positionableBox);
        positionableBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setAllPositionable(positionableBox.isSelected());
                }
            });                    
        positionableBox.setSelected(allPositionable());
        // controlable item
        _optionMenu.add(controllingBox);
        controllingBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setAllControlling(controllingBox.isSelected());
                }
            });                    
        controllingBox.setSelected(allControlling());
        // hidden item
        _optionMenu.add(hiddenBox);
        hiddenBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setShowHidden(hiddenBox.isSelected());
                }
            });                    
        hiddenBox.setSelected(showHidden());

        _optionMenu.add(showTooltipBox);
        showTooltipBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAllShowTooltip(showTooltipBox.isSelected());
            }
        });
        showTooltipBox.setSelected(showTooltip());

		// Show/Hide Scroll Bars
        JMenu scrollMenu = new JMenu(Bundle.getMessage("ComboBoxScrollable"));
        _optionMenu.add(scrollMenu);
        ButtonGroup scrollGroup = new ButtonGroup();
        scrollGroup.add(scrollBoth);
        scrollMenu.add(scrollBoth);
        scrollBoth.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setScroll(SCROLL_BOTH);
                }
            });
        scrollGroup.add(scrollNone);
        scrollMenu.add(scrollNone);
        scrollNone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setScroll(SCROLL_NONE);
                }
            });
        scrollGroup.add(scrollHorizontal);
        scrollMenu.add(scrollHorizontal);
        scrollHorizontal.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setScroll(SCROLL_HORIZONTAL);
                }
            });
        scrollGroup.add(scrollVertical);
        scrollMenu.add(scrollVertical);
        scrollVertical.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setScroll(SCROLL_VERTICAL);
                }
            });
    }
    
    private void makeFileMenu() {
        _fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        _menuBar.add(_fileMenu, 0);
        _fileMenu.add(new jmri.jmrit.display.NewPanelAction(Bundle.getMessage("MenuItemNew")));

        _fileMenu.add(new jmri.configurexml.StoreXmlUserAction(Bundle.getMessage("MenuItemStore")));
        JMenuItem storeIndexItem = new JMenuItem(Bundle.getMessage("MIStoreImageIndex"));
        _fileMenu.add(storeIndexItem);
        storeIndexItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					jmri.jmrit.catalog.ImageIndexEditor.storeImageIndex();
                }
            });

        JMenuItem editItem = new JMenuItem(Bundle.getMessage("renamePanelMenu"));
        PositionableJComponent z = new PositionableJComponent(this);
        z.setScale(getPaintScale());
        editItem.addActionListener(CoordinateEdit.getNameEditAction(z));
        _fileMenu.add(editItem);

        editItem = new JMenuItem(Bundle.getMessage("editIndexMenu"));
        _fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
                ControlPanelEditor panelEd;
                public void actionPerformed(ActionEvent e) {
                    ImageIndexEditor ii = ImageIndexEditor.instance(panelEd);
                    ii.pack();
                    ii.setVisible(true);
                }
                ActionListener init(ControlPanelEditor pe) {
                    panelEd = pe;
                    return this;
                }
            }.init(this));

        editItem = new JMenuItem(Bundle.getMessage("PEView"));
        _fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					changeView("jmri.jmrit.display.panelEditor.PanelEditor");
					_itemPalette.dispose();
                }
            });

        _fileMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem(Bundle.getMessage("DeletePanel"));
        _fileMenu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (deletePanel() ) {
                        dispose(true);
                    }
                }
            });
        _fileMenu.addSeparator();
        editItem = new JMenuItem(Bundle.getMessage("CloseEditor"));
        _fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setAllEditable(false);
                }
            });
    }

    /**
     * Create an Edit menu to support cut/copy/paste.
     * An incredible hack to get some semblance of CCP between panels.  The hack works for
     * one of two problems.
     * 1. Invoking a copy to the system clipboard causes a delayed repaint placed on the
     *    EventQueue whenever ScrollBars are invoked.  This repaint ends with a null
     *    pointer exception at 
     *    javax.swing.plaf.basic.BasicScrollPaneUI.paint(BasicScrollPaneUI.java:90)
     *    This error occurs regardless of the method used to put the copy in the 
     *    clipboard - JDK 1.2 style or 1.4 TransferHandler
     *    Fixed! Get the plaf glue (BasicScrollPaneUI) and call installUI(_panelScrollPane)
     *    See copyToClipboard() below, line 527 (something the Java code should have done)
     *    No scrollbars - no problem.  Hack does not fix this problem.
     * 2. The clipboard provides a shallow copy of what was placed there.  For things that
     *    have an icon Map (ArrayLists) the Tranferable data is shallow.  The Hack to 
     *    work around this is:  Place a reference to the panel copying to the clipboad
     *    in the clipboard and let the pasting panel callback to the copying panel 
     *    to get the data.
     *    See public ArrayList<Positionable> getClipGroup() {} below.
     */
    protected void makeEditMenu() {
        _editMenu = new JMenu("Edit");
        _menuBar.add(_editMenu, 0);
        _editMenu.setMnemonic(KeyEvent.VK_E);
/*
    Tutorial recommended method not satisfactory.
        TransferActionListener actionListener = new TransferActionListener();
        JMenuItem menuItem = new JMenuItem("Cut");
        menuItem.setActionCommand((String)TransferHandler.getCutAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_T);
        _editMenu.add(menuItem);
        
        menuItem = new JMenuItem("Copy");
        menuItem.setActionCommand((String)TransferHandler.getCopyAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_C);
        _editMenu.add(menuItem);
        
        menuItem = new JMenuItem("Paste");
        menuItem.setActionCommand((String)TransferHandler.getPasteAction().getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_P);
        _editMenu.add(menuItem);
        */

        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("MenuItemCut"));
        menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    copyToClipboard();
                    removeSelections(null);
                }
            });
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_T);
        _editMenu.add(menuItem);
        
        menuItem = new JMenuItem(Bundle.getMessage("MenuItemCopy"));
        menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    copyToClipboard();
                }
            });
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_C);
        _editMenu.add(menuItem);
        
        menuItem = new JMenuItem(Bundle.getMessage("MenuItemPaste"));
        menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    pasteFromClipboard();
                }
            });
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_P);
        _editMenu.add(menuItem);

        _editMenu.add(makeSelectTypeMenu());
        _editMenu.add(makeSelectLevelMenu());

        menuItem = new JMenuItem(Bundle.getMessage("SelectAll"));
        menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    _selectionGroup = _contents;
                    _targetPanel.repaint();
                }
            });
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        _editMenu.add(menuItem);
    }
    private JMenu makeSelectTypeMenu() {
        JMenu menu = new JMenu(Bundle.getMessage("SelectType"));
        ButtonGroup typeGroup = new ButtonGroup();
        JRadioButtonMenuItem button = makeSelectTypeButton("IndicatorTrack", "jmri.jmrit.display.IndicatorTrackIcon"); 
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("IndicatorTO", "jmri.jmrit.display.IndicatorTurnoutIcon");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("Turnout", "jmri.jmrit.display.TurnoutIcon");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("Sensor", "jmri.jmrit.display.SensorIcon");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("Shape", "jmri.jmrit.display.controlPanelEditor.shape.PositionableShape");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("SignalMast", "jmri.jmrit.display.SignalMastIcon");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("SignalHead", "jmri.jmrit.display.SignalHeadIcon");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("Memory", "jmri.jmrit.display.MemoryIcon");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("MemoryInput", "jmri.jmrit.display.PositionableJPanel");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("MultiSensor", "jmri.jmrit.display.MultiSensorIcon");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("LocoID", "jmri.jmrit.display.LocoIcon");
        typeGroup.add(button);
        menu.add(button);
        button = makeSelectTypeButton("Light", "jmri.jmrit.display.LightIcon");
        typeGroup.add(button);
        menu.add(button);
    	return menu;
    }
    private JRadioButtonMenuItem makeSelectTypeButton(String label, String className) {
        JRadioButtonMenuItem button = new JRadioButtonMenuItem(Bundle.getMessage(label));
        button.addActionListener(new ActionListener() {
        	String className;
        	ActionListener init(String name) {
        		className = name;
        		return this;
        	}
        	public void actionPerformed(ActionEvent event) {
        		selectType(className);
        	}
        }.init(className));
        return button;
    }
    private void selectType(String name) {
    	try {
    		Class cl = Class.forName(name);
        	_selectionGroup = new ArrayList<Positionable>();
        	Iterator<Positionable> it = _contents.iterator();
        	while (it.hasNext()) {
        		Positionable pos = it.next();
        		if (cl.isInstance(pos)) {
        			_selectionGroup.add(pos);
        		}
        	}   	
    	} catch (ClassNotFoundException cnfe) {
    		log.error("selectType Menu "+cnfe.toString());
    	}
        _targetPanel.repaint();
    }
    private JMenu makeSelectLevelMenu() {
    	JMenu menu = new JMenu(Bundle.getMessage("SelectLevel"));
        ButtonGroup levelGroup = new ButtonGroup();
        JRadioButtonMenuItem button = null;
    	for (int i=0; i<11; i++) {
            button = new JRadioButtonMenuItem(Bundle.getMessage("selectLevel", ""+i));
            levelGroup.add(button);
            menu.add(button);
            button.addActionListener(new ActionListener() {
            	int i;
            	ActionListener init(int k) {
            		i=k;
            		return this;
            	}
                    public void actionPerformed(ActionEvent event) {
                        selectLevel(i);
                    }
                }.init(i));
    	}
    	return menu;
    }
    private void selectLevel(int i) {
    	_selectionGroup = new ArrayList<Positionable>();
    	Iterator<Positionable> it = _contents.iterator();
    	while (it.hasNext()) {
    		Positionable pos = it.next();
    		if (pos.getDisplayLevel()==i) {
    			_selectionGroup.add(pos);
    		}
    	}   	
        _targetPanel.repaint();
    }	

    private void pasteFromClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        DataFlavor[] flavors = clipboard.getAvailableDataFlavors();
        for (int k=0; k<flavors.length; k++) {
            if (_positionableListDataFlavor.equals(flavors[k])) {
                try{
                    @SuppressWarnings("unchecked")
                    List<Positionable> clipGroup = (List<Positionable>)clipboard.getData(_positionableListDataFlavor);
                    if (clipGroup!=null && clipGroup.size()>0) {
                        Positionable pos = clipGroup.get(0);
                        int minX = pos.getLocation().x;
                        int minY = pos.getLocation().y;
                        // locate group at mouse point
                        for (int i=1; i<clipGroup.size(); i++) {
                            pos = clipGroup.get(i);
                            minX = Math.min(minX, pos.getLocation().x);
                            minY = Math.min(minY, pos.getLocation().y);
                        }
                        if (_pastePending) {
                            abortPasteItems();
                        }
                        _selectionGroup = new ArrayList<Positionable>();
                        for (int i=0; i<clipGroup.size(); i++) {
                            pos = clipGroup.get(i);
                            // make positionable belong to this editor
                            pos.setEditor(this);
                            pos.setLocation(pos.getLocation().x+_anchorX-minX, pos.getLocation().y+_anchorY-minY);
                            // now set display level in the pane.
                            pos.setDisplayLevel(pos.getDisplayLevel());
                            putItem(pos);
                            pos.updateSize();
                            pos.setVisible(true);
                            _selectionGroup.add(pos);
                            if (pos instanceof PositionableIcon) {
                            	jmri.NamedBean bean = pos.getNamedBean();
                            	if (bean!=null) {
                                	((PositionableIcon)pos).displayState(bean.getState());                            		
                            	}
                            }
                            else if (pos instanceof MemoryIcon) {
                            	((MemoryIcon)pos).displayState();
                            }
                            else if (pos instanceof PositionableJComponent) {
                            	((PositionableJComponent)pos).displayState();
                            }
                            if (_debug) log.debug("Paste Added at ("+pos.getLocation().x+", "+pos.getLocation().y+")");
                        }
                    }
                    return;
                } catch(IOException ioe) {
                    log.warn("Editor Paste caught IOException", ioe);
                } catch(UnsupportedFlavorException ufe) {
                    log.warn("Editor Paste caught UnsupportedFlavorException",ufe);
                }
            }
        }
    }

    /*
    * The editor instance is dragged.  When dropped this editor will reference
    * the list of positionables (_clipGroup) for pasting 
    */
    private void copyToClipboard() {
        if (_selectionGroup!=null) {
            ArrayList <Positionable> dragGroup = new ArrayList <Positionable>();

            for (int i=0; i<_selectionGroup.size(); i++) {
                Positionable pos = _selectionGroup.get(i).deepClone();
                dragGroup.add(pos);                	
                removeFromTarget(pos);   // cloned item gets added to _targetPane during cloning
            }
            if (_debug) log.debug("copyToClipboard: cloned _selectionGroup, size= "+_selectionGroup.size());
            _clipGroup = dragGroup;

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new PositionableListDnD(_clipGroup), this);
            if (_debug) log.debug("copyToClipboard: setContents _selectionGroup, size= "+_selectionGroup.size());
        } else {
            _clipGroup = null;
        }
    }

    ArrayList <Positionable> _clipGroup;
    public ArrayList <Positionable> getClipGroup() {
        if (_debug) log.debug("getClipGroup: _clipGroup"+(_clipGroup==null?"=null":", size= "+_clipGroup.size()));
        if (_clipGroup==null) {
            return null;
        }
        ArrayList<Positionable> clipGrp = new ArrayList<Positionable>();
        for (int i=0; i<_clipGroup.size(); i++) {
            Positionable pos = _clipGroup.get(i).deepClone();
            clipGrp.add(pos);
            removeFromTarget(pos);   // cloned item gets added to _targetPane during cloning
        }
        return clipGrp;
    }
    
    ///// implementation of ClipboardOwner
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
           /* don't care */
        if (_debug) log.debug("lostOwnership: content flavor[0] = "+contents.getTransferDataFlavors()[0]);
    }

    // override
    public void setAllEditable(boolean edit) {
        if (edit) {
            if (_editorMenu!=null) {
                _menuBar.remove(_editorMenu);
            }
            if (_markerMenu!=null) {
                _menuBar.remove(_markerMenu);
            }
            if (_warrantMenu!=null) {
        		_menuBar.remove(_warrantMenu);
            }           
            if (_drawMenu==null) {
            	makeDrawMenu();
            } else {
                _menuBar.add(_drawMenu, 0);
            }          
            if (_circuitMenu==null) {
            	makeCircuitMenu();
            } else {
                _menuBar.add(_circuitMenu, 0);
            }           
            makeWarrantMenu();
 
            if (_iconMenu==null) {
                makeIconMenu();
            } else {
                _menuBar.add(_iconMenu, 0);
            }
            if (_zoomMenu==null) {
                makeZoomMenu();
            } else {
                _menuBar.add(_zoomMenu, 0);
            }
            if (_optionMenu==null) {
                makeOptionMenu();
            } else {
                _menuBar.add(_optionMenu, 0);
            }
            if (_editMenu==null) {
                makeEditMenu();
            } else {
                _menuBar.add(_editMenu, 0);
            }
            if (_fileMenu==null) {
                makeFileMenu();
            } else {
                _menuBar.add(_fileMenu, 0);
            }
        } else {
        	if (_fileMenu!=null) {
        		_menuBar.remove(_fileMenu);
        	}
        	if (_editMenu!=null) {
        		_menuBar.remove(_editMenu);
        	}
        	if (_optionMenu!=null) {
        		_menuBar.remove(_optionMenu);
        	}
        	if (_zoomMenu!=null) {
        		_menuBar.remove(_zoomMenu);
        	}
        	if (_iconMenu!=null) {
        		_menuBar.remove(_iconMenu);
        	}
            if (_warrantMenu!=null) {
        		_menuBar.remove(_warrantMenu);
            }
            if (_circuitMenu!=null) {
        		_menuBar.remove(_circuitMenu);
            }
            if (_drawMenu!=null) {
        		_menuBar.remove(_drawMenu);
            }
    		if (InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getSystemNameList().size() > 1) {
    			makeWarrantMenu();
    		}	
            if (_markerMenu==null) {
                makeMarkerMenu();
            } else {
                _menuBar.add(_markerMenu, 0);
            }
            if (_editorMenu==null) {
                _editorMenu = new JMenu(Bundle.getMessage("MenuEdit"));
                _editorMenu.add(new AbstractAction(Bundle.getMessage("OpenEditor")) {
                        public void actionPerformed(ActionEvent e) {
                            setAllEditable(true);
                        }
                });
            }
            _menuBar.add(_editorMenu, 0);
        }
        super.setAllEditable(edit);
        setTitle();
        _menuBar.validate();
    }
    // override
    public void setUseGlobalFlag(boolean set) {
        positionableBox.setEnabled(set);
        controllingBox.setEnabled(set);
        super.setUseGlobalFlag(set);      
    }

    private void zoomRestore() {
        List <Positionable> contents = getContents();
        for (int i=0; i<contents.size(); i++) {
            Positionable p = contents.get(i);
            p.setLocation(p.getX()+_fitX, p.getY()+_fitY);
        }
        setPaintScale(1.0);
    }

    int _fitX = 0;
    int _fitY = 0;
    private void zoomToFit() {
        double minX = 1000.0;
        double maxX = 0.0;
        double minY = 1000.0;
        double maxY = 0.0;
        List <Positionable> contents = getContents();
        for (int i=0; i<contents.size(); i++) {
            Positionable p = contents.get(i);
            minX = Math.min(p.getX(), minX); 
            minY = Math.min(p.getY(), minY);
            maxX = Math.max(p.getX()+p.getWidth(), maxX);
            maxY = Math.max(p.getY()+p.getHeight(), maxY);
        }
        _fitX = (int)Math.floor(minX);
        _fitY = (int)Math.floor(minY);

        JFrame frame = getTargetFrame();
        Container contentPane = getTargetFrame().getContentPane();
        Dimension dim = contentPane.getSize();
        Dimension d = getTargetPanel().getSize();
        getTargetPanel().setSize((int)Math.ceil(maxX-minX), (int)Math.ceil(maxY-minY));

        JScrollPane scrollPane = getPanelScrollPane();
        scrollPane.getHorizontalScrollBar().setValue(0);
        scrollPane.getVerticalScrollBar().setValue(0);
        JViewport viewPort = scrollPane.getViewport();
        Dimension dv = viewPort.getExtentSize();

        int dX = frame.getWidth()-dv.width;
        int dY = frame.getHeight()-dv.height;
        if (_debug) log.debug("zoomToFit: layoutWidth= "+(maxX-minX)+", layoutHeight= "+(maxY-minY)+
                              "\n\tframeWidth= "+frame.getWidth()+", frameHeight= "+frame.getHeight()+
                              ", viewWidth= "+dv.width+", viewHeight= "+dv.height+
                              "\n\tconWidth= "+dim.width+", conHeight= "+dim.height+
                              ", panelWidth= "+d.width+", panelHeight= "+d.height);
        double ratioX = dv.width/(maxX-minX);
        double ratioY = dv.height/(maxY-minY);
        double ratio = Math.min(ratioX, ratioY);
        /*
        if (ratioX<ratioY) {
            if (ratioX>1.0) {
                ratio = ratioX;
            } else {
                ratio = ratioY;
            }
        } else {
            if (ratioY<1.0) {
                ratio = ratioX;
            } else {
                ratio = ratioY;
            }
        } */
        _fitX = (int)Math.floor(minX);
        _fitY = (int)Math.floor(minY);
        for (int i=0; i<contents.size(); i++) {
            Positionable p = contents.get(i);
            p.setLocation(p.getX()-_fitX, p.getY()-_fitY);
        }
        setScroll(SCROLL_BOTH);
        setPaintScale(ratio);
        setScroll(SCROLL_NONE);
        scrollNone.setSelected(true);
        //getTargetPanel().setSize((int)Math.ceil(maxX), (int)Math.ceil(maxY));
        frame.setSize((int)Math.ceil((maxX-minX)*ratio)+dX, (int)Math.ceil((maxY-minY)*ratio)+dY);
        scrollPane.getHorizontalScrollBar().setValue(0);
        scrollPane.getVerticalScrollBar().setValue(0);
        if (_debug) log.debug("zoomToFit: ratio= "+ratio+", w= "+(maxX-minX)+", h= "+(maxY-minY)+ 
                              ", frameWidth= "+frame.getWidth()+", frameHeight= "+frame.getHeight());
    }

    public void setTitle() {
        String name = getName();
        if (name==null || name.length()==0) {
            name = "Control Panel";
        }
        if (isEditable()) {
            super.setTitle(name+" "+Bundle.getMessage("LabelEditor"));
        } else {
            super.setTitle(name);
        }
    }

    // all content loaded from file.
    public void loadComplete() {
        if (_debug) log.debug("loadComplete");
    }
    
    /**
     * After construction, initialize all the widgets to their saved config settings.
     */
    public void initView() {
        positionableBox.setSelected(allPositionable());
        controllingBox.setSelected(allControlling());
        //showCoordinatesBox.setSelected(showCoordinates());
        showTooltipBox.setSelected(showTooltip());
        hiddenBox.setSelected(showHidden());
        switch (_scrollState) {
            case SCROLL_NONE:
                scrollNone.setSelected(true);
                break;
            case SCROLL_BOTH:
                scrollBoth.setSelected(true);
                break;
            case SCROLL_HORIZONTAL:
                scrollHorizontal.setSelected(true);
                break;
            case SCROLL_VERTICAL:
                scrollVertical.setSelected(true);
                break;
        }
        if (_debug) log.debug("InitView done");
    }

    /***************** Overridden methods of Editor *******************/

    private boolean _manualSelection = false; 
    protected Positionable getCurrentSelection(MouseEvent event) {
        if (_pastePending && !event.isPopupTrigger() && !event.isMetaDown() && !event.isAltDown()) {
            return getCopySelection(event);
        }
        List <Positionable> selections = getSelectedItems(event);
        if (_disableShapeSelection || _disablePortalSelection) {
        	ArrayList<Positionable> list = new ArrayList<Positionable>();
        	Iterator<Positionable> it = selections.iterator();
        	while (it.hasNext()) {
        		Positionable pos = it.next();
        		if (_disableShapeSelection && pos instanceof jmri.jmrit.display.controlPanelEditor.shape.PositionableShape) {
        			continue;
        		}
        		if (_disablePortalSelection && pos instanceof PortalIcon) {
        			continue;
        		}
        		list.add(pos);
        	}
        	selections = list;
        }
        Positionable selection = null;
        if (selections.size() > 0) {
            if (event.isControlDown()) {
                if (event.isShiftDown() && selections.size() > 3) {
                	if (_manualSelection) {
                		// selection made - don't change it
                        _selectionGroup = null;
                		return _currentSelection;
                	}
                	// show list
                	String[] selects = new String[selections.size()];
                	Iterator<Positionable> iter = selections.iterator();
                	int i = 0;
                	while (iter.hasNext()) {
                		Positionable pos = iter.next();
                		if (pos instanceof jmri.NamedBean) {
                    		selects[i++] = ((jmri.NamedBean)pos).getDisplayName();                			
                		} else {
                			selects[i++] =  pos.getNameString();
                		}
                	}
                	Object select = JOptionPane.showInputDialog(this,Bundle.getMessage("multipleSelections"),
                					Bundle.getMessage("questionTitle"), JOptionPane.QUESTION_MESSAGE, 
                					null, selects, null);
                	if (select !=null) {
                    	iter = selections.iterator();
                    	while (iter.hasNext()) {
                    		Positionable pos = iter.next();
                    		String name =null;
                    		if (pos instanceof jmri.NamedBean) {
                    			name = ((jmri.NamedBean)pos).getDisplayName();                			
                    		} else {
                    			name =  pos.getNameString();
                    		}
                    		if (((String)select).equals(name)) {
                        		_manualSelection = true;
                        		return pos;
                    		}
                    	}
                	} else {
                		selection = selections.get(selections.size()-1);
                	}
                } else {
                	// select bottom-most item over the background, otherwise take the background item
                    selection = selections.get(selections.size()-1);
                    if (selection.getDisplayLevel()<=BKG && selections.size() > 1) {
                    	selection = selections.get(selections.size()-2);
                    }
//            		_manualSelection = false;
                }
            } else {
                if (event.isShiftDown() && selections.size() > 1) {
                    selection = selections.get(1); 
                } else {
                    selection = selections.get(0); 
                }            
            	if (selection.getDisplayLevel()<=BKG) {
                    selection = null;
            	}
        		_manualSelection = false;
            }
        }
        if (!isEditable() && selection!=null && selection.isHidden()) {
        	selection = null;
        }
        return selection;
    }

    private Positionable getCopySelection(MouseEvent event) {
        if (_selectionGroup==null) {
            return null;
        }
        double x = event.getX();
        double y = event.getY();

        for (int i=0; i<_selectionGroup.size(); i++) {
            Positionable p = _selectionGroup.get(i);
            Rectangle2D.Double rect2D = new Rectangle2D.Double(p.getX()*_paintScale,
                                                               p.getY()*_paintScale,
                                                               p.maxWidth()*_paintScale,
                                                               p.maxHeight()*_paintScale);
            if (rect2D.contains(x, y)) {
                return p;
            }
        }
        return null;
    }
    
    /*********** KeyListener *********
    public void keyTyped(KeyEvent e) {
    }*/
    // override
    public void keyPressed(KeyEvent e) {
    	if (_selectionGroup==null) {
    		return;
    	}
        int x = 0;
        int y = 0;
        switch (e.getKeyCode()){
        	case KeyEvent.VK_UP: 
        	case KeyEvent.VK_KP_UP:
        	case KeyEvent.VK_NUMPAD8:
            	y=-1;
                break;
            case KeyEvent.VK_DOWN: 
            case KeyEvent.VK_KP_DOWN: 
        	case KeyEvent.VK_NUMPAD2:
            	y=1;
                break;
            case KeyEvent.VK_LEFT: 
            case KeyEvent.VK_KP_LEFT: 
        	case KeyEvent.VK_NUMPAD4:
            	x=-1;
                break;
            case KeyEvent.VK_RIGHT: 
            case KeyEvent.VK_KP_RIGHT: 
        	case KeyEvent.VK_NUMPAD6:
            	x=1;
                break;
        }
        for (int i=0; i<_selectionGroup.size(); i++){
            moveItem(_selectionGroup.get(i), x, y);
        }
        repaint();
    }

    /*********** Mouse ***************/

    public void mousePressed(MouseEvent event) {
    	setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mousePressed at ("+event.getX()+","+event.getY()+") _dragging="+_dragging);
                            //  " _selectionGroup= "+(_selectionGroup==null?"null":_selectionGroup.size()));
        boolean circuitBuilder = _circuitBuilder.saveSelectionGroup(_selectionGroup);
        _anchorX = event.getX();
        _anchorY = event.getY();
        _lastX = _anchorX;
        _lastY = _anchorY;

        _currentSelection = getCurrentSelection(event);

        if (!event.isPopupTrigger()&& !event.isMetaDown() && !event.isAltDown() && !circuitBuilder) {
            if (_shapeDrawer.doMousePressed(event, _currentSelection)) {
              	_selectionGroup = null;
              	_currentSelection = null;
              	return;
            }
            if (_currentSelection!=null) {
                _currentSelection.doMousePressed(event);
                if (isEditable()) {
                    if ( !event.isControlDown() &&
                         (_selectionGroup!=null && !_selectionGroup.contains(_currentSelection)) ) {
                        if (_pastePending) {
                            abortPasteItems();
                        }
                        _selectionGroup = null;
                    }
                }
            } else {
                _highlightcomponent = null;
                if (_pastePending) {
                    abortPasteItems();
                }
                _selectionGroup = null;
            }
        } else {
            if (_currentSelection==null || (_selectionGroup!=null && !_selectionGroup.contains(_currentSelection)) ) {
            	_selectionGroup = null;
            }
        }
		_circuitBuilder.doMousePressed(event, _currentSelection);
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseReleased(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mouseReleased at ("+event.getX()+","+event.getY()+") dragging= "+_dragging
                              +" pastePending= "+_pastePending+" selectRect "+(_selectRect==null?"=":"!")+"= null");
        Positionable selection = getCurrentSelection(event);

        if ((event.isPopupTrigger() || event.isMetaDown() || event.isAltDown()) /*&& !_dragging*/) {
            if (selection!=null) {
                _highlightcomponent = null;
                showPopUp(selection, event);
            } else if (_selectRect!=null) {
                makeSelectionGroup(event);
            }
        } else {
            if (selection!=null) {
                selection.doMouseReleased(event);
            }
            boolean circuitBuilder =_circuitBuilder.doMouseReleased(selection, event);
            // when dragging, don't change selection group
            if (_pastePending && _dragging) {
                pasteItems();
            }
            if (isEditable()) {
                if (_shapeDrawer.doMouseReleased(selection, event)) {
                	_selectRect = null;
                }
                if (selection!=null && !circuitBuilder) {
                	if (!_dragging) {
                    	modifySelectionGroup(selection, event);
                    }
                }
                if (_selectRect!=null && !circuitBuilder) {
                    makeSelectionGroup(event);
                }
                _currentSelection = selection;            	
                if (!circuitBuilder) {
                	if (_currentSelection!=null && (_selectionGroup==null || _selectionGroup.size()==0)) {
                		if (_selectionGroup==null) _selectionGroup = new ArrayList <Positionable>();
                		_selectionGroup.add(_currentSelection);
                	}        	
                }
            } else {
            	_selectionGroup = null;
            	_currentSelection = null;
                _highlightcomponent = null;
            }
        }
        _selectRect = null;

        // if not sending MouseClicked, do it here
        if (jmri.util.swing.SwingSettings.getNonStandardMouseEvent())
            mouseClicked(event);

        _lastX = event.getX();
        _lastY = event.getY();
        _dragging = false;
        _currentSelection = null;
        _targetPanel.repaint(); // needed for ToolTip
//        if (_debug) log.debug("mouseReleased at ("+event.getX()+","+event.getY()+
//        " _selectionGroup= "+(_selectionGroup==null?"null":_selectionGroup.size()));
    }

    private long _clickTime;
    public void mouseClicked(MouseEvent event) {
        if (jmri.util.swing.SwingSettings.getNonStandardMouseEvent()) {
            long time = System.currentTimeMillis();
            if (time-_clickTime < 20) {
                return;
            }
            _clickTime = time;
        }

        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mouseClicked at ("+event.getX()+","+event.getY()+")");

        Positionable selection = getCurrentSelection(event);
        if (_shapeDrawer.doMouseClicked(event)) {
        	return;
        }

        if (event.isPopupTrigger() || event.isMetaDown() || event.isAltDown()) {
            if (selection!=null) {
                _highlightcomponent = null;
                showPopUp(selection, event);
            }
        } else {
            if (selection!=null) {
            	if (!_circuitBuilder.doMouseClicked(getSelectedItems(event), event)) {
                    selection.doMouseClicked(event);
                }
                if (selection instanceof IndicatorTrack) {
                	WarrantTableAction.mouseClickedOnBlock(((IndicatorTrack)selection).getOccBlock());
                }
            }
        }
        if (!isEditable()) {
        	_selectionGroup = null;
        	_currentSelection = null;
            _highlightcomponent = null;
        }
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseDragged(MouseEvent event) {
        //if (_debug) log.debug("mouseDragged at ("+event.getX()+","+event.getY()+")"); 
        setToolTip(null); // ends tooltip if displayed
        
        if (_circuitBuilder.doMouseDragged(_currentSelection, event) ) {
        	return;
        }
        if (_shapeDrawer.doMouseDragged(event) ) {
        	return;
        }
        if (!event.isPopupTrigger() && !event.isMetaDown() && !event.isAltDown() && (isEditable() || _currentSelection instanceof LocoIcon)) {
            moveIt:
            if (_currentSelection!=null && getFlag(OPTION_POSITION, _currentSelection.isPositionable())) {
                int deltaX = event.getX() - _lastX;
                int deltaY = event.getY() - _lastY;
                int minX = getItemX(_currentSelection, deltaX);
                int minY = getItemY(_currentSelection, deltaY);
                if (_selectionGroup!=null && _selectionGroup.contains(_currentSelection)) {
                    for (int i=0; i<_selectionGroup.size(); i++){
                        minX = Math.min(getItemX(_selectionGroup.get(i), deltaX), minX);
                        minY = Math.min(getItemY(_selectionGroup.get(i), deltaY), minY);
                    }
                }
                if (minX<0 || minY<0) {
                    // Don't allow move beyond the left or top borders
                    break moveIt;
                    /*
                    // or use this choice:
                    // Expand the panel to the left or top as needed by the move
                    // Probably not the preferred solution - use the above break
                    if (_selectionGroup!=null && _selectionGroup.contains(_currentSelection)) {
                        List <Positionable> allItems = getContents();
                        for (int i=0; i<allItems.size(); i++){
                            moveItem(allItems.get(i), -deltaX, -deltaY);
                        }
                    } else {
                        moveItem(_currentSelection, -deltaX, -deltaY);
                    }
                    */
                }
                if (_selectionGroup!=null && _selectionGroup.contains(_currentSelection)
                		&& !_circuitBuilder.dragPortal()) {
                    for (int i=0; i<_selectionGroup.size(); i++){
                        moveItem(_selectionGroup.get(i), deltaX, deltaY);
                    }
                    _highlightcomponent = null;
                } else {
                    moveItem(_currentSelection, deltaX, deltaY);
                }
            } else {
                if ((isEditable() && _selectionGroup==null)) {
                    drawSelectRect(event.getX(), event.getY());
                }
            }
        }
        _dragging = true;
        _lastX = event.getX();
        _lastY = event.getY();
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseMoved(MouseEvent event) {
        //if (_debug) log.debug("mouseMoved at ("+event.getX()+","+event.getY()+")"); 
        if (_dragging || event.isPopupTrigger() || event.isMetaDown() || event.isAltDown()) { return; }
    	if (!(event.isShiftDown() && event.isControlDown()) && !_shapeDrawer.doMouseMoved(event)) {
            Positionable selection = getCurrentSelection(event);
            if (selection!=null && selection.getDisplayLevel()>BKG && selection.showTooltip()) {
                showToolTip(selection, event);
                //selection.highlightlabel(true);
            } else {
                setToolTip(null);
            }    		
    	}
        _targetPanel.repaint();
    }
    
    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
        setToolTip(null);
        _targetPanel.repaint();  // needed for ToolTip
    }


    /*************** implementation of Abstract Editor methods ***********/

    /**
     * The target window has been requested to close, don't delete it at this
	 *   time.  Deletion must be accomplished via the Delete this panel menu item.
     */
    protected void targetWindowClosingEvent(java.awt.event.WindowEvent e) {
        jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex();
        targetWindowClosing(true);
    }

    protected void setSecondSelectionGroup(ArrayList <Positionable> list) {
    	_secondSelectionGroup = list;
    }
    protected void paintTargetPanel(Graphics g) {
    	// needed to create PositionablePolygon
    	_shapeDrawer.paint(g);
        if (_secondSelectionGroup!=null){
        	Graphics2D g2d = (Graphics2D)g;
            g2d.setColor(new Color(150, 150, 255));
            g2d.setStroke(new java.awt.BasicStroke(2.0f));
            for(int i=0; i<_secondSelectionGroup.size();i++){
            	Positionable p = _secondSelectionGroup.get(i);
                if (!(p instanceof jmri.jmrit.display.controlPanelEditor.shape.PositionableShape)) {
                    g.drawRect(p.getX(), p.getY(), p.maxWidth(), p.maxHeight());                        	
                }
            }
        }
    }

    /**
     * Set an object's location when it is created.
     */
    public void setNextLocation(Positionable obj) {
        obj.setLocation(0, 0);
    }    

    /**
    * Set up selections for a paste.  Note a copy of _selectionGroup is made that is
    * NOT in the _contents.  This disconnected ArrayList is added to the _contents
    * when (if) a paste is made.  The disconnected _selectionGroup can be dragged to
    * a new location.
    */
    protected void copyItem(Positionable p) {
        if (_debug) log.debug("Enter copyItem: _selectionGroup "+(_selectionGroup!=null ?
                                                  "size= "+_selectionGroup.size() : "null"));
        // If popup menu hit again, Paste selections and make another copy
        if (_pastePending) {
            pasteItems();
        }
        if (_selectionGroup!=null && !_selectionGroup.contains(p)) {
            _selectionGroup = null;
        }
        if (_selectionGroup==null) {
            _selectionGroup = new ArrayList <Positionable>();
            _selectionGroup.add(p);
        }
        ArrayList<Positionable> selectionGroup = new ArrayList<Positionable>();
        for (int i=0; i<_selectionGroup.size(); i++) {
            Positionable pos = _selectionGroup.get(i).deepClone();
            selectionGroup.add(pos);
        }
        _selectionGroup = selectionGroup;  // group is now disconnected
        _pastePending = true;
//        if (_debug) log.debug("Exit copyItem: _selectionGroup.size()= "+_selectionGroup.size());
    }

    void pasteItems() {
        if (_selectionGroup!=null) {
            for (int i=0; i<_selectionGroup.size(); i++) {
            	Positionable pos = _selectionGroup.get(i);
                if (pos instanceof PositionableIcon) {
                	jmri.NamedBean bean = pos.getNamedBean();
                	if (bean!=null) {
                    	((PositionableIcon)pos).displayState(bean.getState());                            		
                	}
                }
                putItem(pos);
                if (_debug) log.debug("Add "+_selectionGroup.get(i).getNameString());
            }
        }
        if (_selectionGroup.get(0) instanceof LocoIcon) {
        	LocoIcon p =(LocoIcon)_selectionGroup.get(0);
            CoordinateEdit f = new CoordinateEdit();
            f.init("Train Name", p, false);
            f.initText();
            f.setVisible(true);	
            f.setLocationRelativeTo(p);
        }
        _pastePending = false;
    }
        
    /**
    * Showing the popup of a member of _selectionGroup causes an image to be placed
    * in to the _targetPanel.  If the objects are not put into _contents (putItem(p))
    * the image will persist.  Thus set these transitory object invisible.
    */
    void abortPasteItems() {
        if (_debug) log.debug("abortPasteItems: _selectionGroup"+
                              (_selectionGroup==null?"=null":(".size="+_selectionGroup.size())));
        if (_selectionGroup!=null) {
            for (int i=0; i<_selectionGroup.size(); i++) {
                _selectionGroup.get(i).setVisible(false);
                _selectionGroup.get(i).remove();
            }
        }
        _selectionGroup = null;
        _pastePending = false;
    }
        
    /**
    * Add an action to copy the Positionable item and the group to which is may belong.
    */
    public void setCopyMenu(Positionable p, JPopupMenu popup) {
        JMenuItem edit = new JMenuItem(Bundle.getMessage("MenuItemDuplicate"));
        edit.addActionListener(new ActionListener() {
            Positionable comp;
            public void actionPerformed(ActionEvent e) {
                copyItem(comp);
            }
            ActionListener init(Positionable pos) {
                comp = pos;
                return this;
            }
        }.init(p));
        popup.add(edit);
    }
    
    protected void setSelectionsScale(double s, Positionable p) {
    	if (_circuitBuilder.saveSelectionGroup(_selectionGroup)) {
            p.setScale(s);    		
    	} else {
    		super.setSelectionsScale(s, p);
    	}
    }
        
    protected void setSelectionsRotation(int k, Positionable p) { 
    	if (_circuitBuilder.saveSelectionGroup(_selectionGroup)) {
            p.rotate(k);    		
    	} else {
    		super.setSelectionsRotation(k, p);   		
    	}
    }

    /**
    *  Create popup for a Positionable object
    * Popup items common to all positionable objects are done before
    * and after the items that pertain only to specific Positionable
    * types.
    */
    protected void showPopUp(Positionable p, MouseEvent event) {
        if (!((JComponent)p).isVisible()) {
            return;     // component must be showing on the screen to determine its location
        }
        JPopupMenu popup = new JPopupMenu();

        PositionablePopupUtil util = p.getPopupUtility();
        if (p.isEditable()) {
            // items common to all
            if (p.doViemMenu()) {
                popup.add(p.getNameString());
                setPositionableMenu(p, popup);
                if (p.isPositionable()) {
                    setShowCoordinatesMenu(p, popup);
                    setShowAlignmentMenu(p, popup);
                }
                setDisplayLevelMenu(p, popup);
                setHiddenMenu(p, popup);
                popup.addSeparator();
                setCopyMenu(p, popup);
            }

            // items with defaults or using overrides
            boolean popupSet = false;
//            popupSet |= p.setRotateOrthogonalMenu(popup);        
            popupSet |= p.setRotateMenu(popup);        
            popupSet |= p.setScaleMenu(popup);        
            if (popupSet) { 
                popup.addSeparator();
                popupSet = false;
            }
            popupSet = p.setEditItemMenu(popup);        
            if (popupSet) { 
                popup.addSeparator();
                popupSet = false;
            }
            popupSet = p.setTextEditMenu(popup);
            if (p instanceof PositionableLabel) {
            	PositionableLabel pl = (PositionableLabel)p;
            	if (!pl.isIcon()) {
                    popupSet |= setTextAttributes(pl, popup);            	            		
            	}
            } else if (p instanceof PositionableJPanel) {
                popupSet |= setTextAttributes(p, popup);            	            		            	
            }
            if (p instanceof LinkingObject) {
            	((LinkingObject)p).setLinkMenu(popup);
            }
            if (popupSet) { 
                popup.addSeparator();
                popupSet = false;
            }
            p.setDisableControlMenu(popup);
            if (util!=null) {
                util.setAdditionalEditPopUpMenu(popup);
            }
            // for Positionables with unique settings
            p.showPopUp(popup);

            if (p.doViemMenu()) {
                setShowTooltipMenu(p, popup);
                setRemoveMenu(p, popup);
            }
        } else {
        	if (p instanceof LocoIcon) {
                setCopyMenu(p, popup);
        	}
            p.showPopUp(popup);
            if (util!=null) {
                util.setAdditionalViewPopUpMenu(popup);
            }
        }
        popup.show((Component)p, p.getWidth()/2+(int)((getPaintScale()-1.0)*p.getX()),
                    p.getHeight()/2+(int)((getPaintScale()-1.0)*p.getY()));
    }
 
    private HashMap <String, NamedIcon> _portalIconMap;

    private void makePortalIconMap() {
		_portalIconMap = new HashMap <String, NamedIcon>();
		_portalIconMap.put(PortalIcon.VISIBLE, 
				new NamedIcon("resources/icons/throttles/RoundRedCircle20.png","resources/icons/throttles/RoundRedCircle20.png"));
		_portalIconMap.put(PortalIcon.PATH, 
				new NamedIcon("resources/icons/greenSquare.gif","resources/icons/greenSquare.gif"));
		_portalIconMap.put(PortalIcon.HIDDEN, 
				new NamedIcon("resources/icons/Invisible.gif","resources/icons/Invisible.gif"));
		_portalIconMap.put(PortalIcon.TO_ARROW, 
				new NamedIcon("resources/icons/track/toArrow.gif","resources/icons/track/toArrow.gif"));
		_portalIconMap.put(PortalIcon.FROM_ARROW, 
				new NamedIcon("resources/icons/track/fromArrow.gif","resources/icons/track/fromArrow.gif"));    	
    }
    protected NamedIcon getPortalIcon (String name) {
    	if (_portalIconMap==null) {		// set defaults
    		makePortalIconMap();
    	}
        return _portalIconMap.get(name);    	
    }
    
    public HashMap <String, NamedIcon> getPortalIconMap() {
    	if (_portalIconMap==null) {		// set defaults
    		makePortalIconMap();
    	}
    	return _portalIconMap;
    }
    
    public void setDefaultPortalIcons(HashMap <String, NamedIcon> map) {
    	_portalIconMap = map;
    	Iterator<Positionable> it = _contents.iterator();
    	while (it.hasNext()) {
    		Positionable pos = it.next();
    		if (pos instanceof PortalIcon) {
    			((PortalIcon)pos).initMap();
    		}
    	}
    }
    
    /********************* Circuitbuilder ************************************/

    protected void disableMenus() {
    	_drawMenu.setEnabled(false);
    	_warrantMenu.setEnabled(false);
        _iconMenu.setEnabled(false);
        _zoomMenu.setEnabled(false);
        _optionMenu.setEnabled(false);
        _editMenu.setEnabled(false);
        _fileMenu.setEnabled(false);
    	_disablePortalSelection = false;
    }
    
    public void resetEditor() {
    	// enable menus
    	_drawMenu.setEnabled(true);
    	_warrantMenu.setEnabled(true);
        _iconMenu.setEnabled(true);
        _zoomMenu.setEnabled(true);
        _optionMenu.setEnabled(true);
        _editMenu.setEnabled(true);
        _fileMenu.setEnabled(true);
        // reset colors
        _highlightcomponent = null;
        TargetPane targetPane = (TargetPane)getTargetPanel();
        targetPane.setDefaultColors();
        targetPane.validate();
        setSelectionGroup(null);
    	_disablePortalSelection = true;
    }

    /************************ Called by CircuitBuilder **********************/
    
    protected void highlight(Positionable pos) {
    	if (pos==null) {
    		_highlightcomponent = null;
    	} else {
    		_highlightcomponent = new Rectangle(pos.getX(), pos.getY(), 
                                            pos.maxWidth(), pos.maxHeight());
    	}
    	repaint();
    }
   
    protected void setSelectionGroup(ArrayList<Positionable> group) {
    	_highlightcomponent = null;
//        _currentSelection = null;		need non-null for Portal dragging in CircuitBuilder
    	_selectionGroup = group;
    	repaint();
    }

    protected ArrayList<Positionable> getSelectionGroup() {
        return _selectionGroup;
    }
    
    /**************************** DnD **************************************/

    protected void makeDataFlavors() {
//        _targetPanel.setTransferHandler(new DnDIconHandler(this));
        try {
            _positionableDataFlavor = new DataFlavor(POSITIONABLE_FLAVOR);
            _namedIconDataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
            _positionableListDataFlavor = new DataFlavor(List.class, "JComponentList");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }

    DataFlavor _positionableDataFlavor;
    DataFlavor _positionableListDataFlavor;
    DataFlavor _namedIconDataFlavor;

    /*************************** DropTargetListener ************************/

    public void dragExit(DropTargetEvent evt) {}
    public void dragEnter(DropTargetDragEvent evt) {}
    public void dragOver(DropTargetDragEvent evt) {}
    public void dropActionChanged(DropTargetDragEvent evt) {}

    @SuppressWarnings("unchecked")
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    public void drop(DropTargetDropEvent evt) {
        try {
            //Point pt = evt.getLocation(); coords relative to entire window
            Point pt = _targetPanel.getMousePosition(true);
            Transferable tr = evt.getTransferable();
            if (_debug) {
                DataFlavor[] flavors = tr.getTransferDataFlavors();
                String flavor = "";
                for (int i=0; i<flavors.length; i++) {
                    flavor += flavors[i].getRepresentationClass().getName()+", ";
                }
                if (_debug) log.debug("Editor Drop: flavor classes= "+flavor);
            }
            if (tr.isDataFlavorSupported(_positionableDataFlavor)) {
                Positionable item = (Positionable)tr.getTransferData(_positionableDataFlavor);
                if (item==null) {
                    return;
                }
                item.setLocation(pt.x, pt.y);
                // now set display level in the pane.
                item.setDisplayLevel(item.getDisplayLevel());
                item.setEditor(this);
                putItem(item);
                item.updateSize();
                //if (_debug) log.debug("Drop positionable "+item.getNameString()+
                //                                    " as "+item.getClass().getName()+
                //                                    ", w= "+item.maxWidth()+", h= "+item.maxHeight());
                evt.dropComplete(true);
                return;
            } else if (tr.isDataFlavorSupported(_namedIconDataFlavor)) {
                  NamedIcon newIcon = new NamedIcon((NamedIcon)tr.getTransferData(_namedIconDataFlavor));
                  String url = newIcon.getURL();
                  NamedIcon icon = NamedIcon.getIconByName(url);
                  PositionableLabel ni = new PositionableLabel(icon, this);
                  ni.setPopupUtility(null);        // no text
                  // infer a background icon from the size
                  if (icon.getIconHeight()>500 || icon.getIconWidth()>600) {
                      ni.setDisplayLevel(BKG);
                  } else {
                      ni.setDisplayLevel(ICONS);
                  }
                  ni.setLocation(pt.x, pt.y);
                  ni.setEditor(this);
                  putItem(ni);
                  ni.updateSize();
                  evt.dropComplete(true);
                  return;
            } else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String)tr.getTransferData(DataFlavor.stringFlavor);
                PositionableLabel l = new PositionableLabel(text, this);
                l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
                l.setDisplayLevel(LABELS);
                l.setLocation(pt.x, pt.y);
                l.setEditor(this);
                putItem(l);
                evt.dropComplete(true);
            } else if (tr.isDataFlavorSupported(_positionableListDataFlavor)) {
                List<Positionable> dragGroup = 
                        (List<Positionable>)tr.getTransferData(_positionableListDataFlavor);
                for (int i=0; i<dragGroup.size(); i++) {
                    Positionable pos = dragGroup.get(i);
                    pos.setEditor(this);
                    putItem(pos);
                    pos.updateSize();
                    if (_debug) log.debug("DnD Add "+pos.getNameString());
                }
            } else {  
                log.warn("Editor DropTargetListener  supported DataFlavors not avaialable at drop from "
                         +tr.getClass().getName());
            }
        } catch(IOException ioe) {
            log.warn("Editor DropTarget caught IOException", ioe);
        } catch(UnsupportedFlavorException ufe) {
            log.warn("Editor DropTarget caught UnsupportedFlavorException",ufe);
        }
        if (_debug) log.debug("Editor DropTargetListener drop REJECTED!");
        evt.rejectDrop();
    }

    static protected class PositionableListDnD implements Transferable {
//        ControlPanelEditor _sourceEditor;
        List<Positionable> _sourceEditor;
        DataFlavor _dataFlavor;

        PositionableListDnD(List<Positionable> source) {
            _sourceEditor = source;
            _dataFlavor = new DataFlavor(List.class, "JComponentList");
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (log.isDebugEnabled()) log.debug("PositionableListDnD.getTransferData:");
            if (flavor.equals(_dataFlavor)) {
                return _sourceEditor;
            }
            throw new UnsupportedFlavorException(flavor);
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { _dataFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor.equals(_dataFlavor)) {
                return true;
            }
            return false;
        }
    }

    static Logger log = LoggerFactory.getLogger(ControlPanelEditor.class.getName());
}