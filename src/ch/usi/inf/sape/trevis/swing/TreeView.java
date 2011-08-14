/*
 * This file is licensed to You under the "Simplified BSD License".
 * You may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/bsd-license.php
 *
 * See the COPYRIGHT file distributed with this work for information
 * regarding copyright ownership.
 */
package ch.usi.inf.sape.trevis.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import ch.usi.inf.sape.trevis.model.ContextTree;
import ch.usi.inf.sape.trevis.model.ContextTreeNode;
import ch.usi.inf.sape.trevis.model.attribute.BooleanAttribute;
import ch.usi.inf.sape.trevis.model.attribute.BooleanConstant;
import ch.usi.inf.sape.trevis.model.attribute.LeafCountAttribute;
import ch.usi.inf.sape.trevis.model.attribute.LongAttribute;
import ch.usi.inf.sape.trevis.model.attribute.LongConstant;
import ch.usi.inf.sape.trevis.model.attribute.StringAttribute;
import ch.usi.inf.sape.trevis.model.attribute.StringConstant;
import ch.usi.inf.sape.trevis.swing.action.ExportAsImageAction;
import ch.usi.inf.sape.trevis.swing.action.PageSetupAction;
import ch.usi.inf.sape.trevis.swing.action.PrintAction;
import ch.usi.inf.sape.trevis.swing.action.SetBooleanAttributePropertyAction;
import ch.usi.inf.sape.trevis.swing.action.SetBooleanPropertyAction;
import ch.usi.inf.sape.trevis.swing.action.SetCutoffAction;
import ch.usi.inf.sape.trevis.swing.action.SetLongAttributePropertyAction;
import ch.usi.inf.sape.trevis.swing.action.SetStringAttributePropertyAction;
import ch.usi.inf.sape.trevis.swing.action.ShowAsAction;
import ch.usi.inf.sape.trevis.swing.action.ToggleBooleanPropertyAction;
import ch.usi.inf.sape.trevis.swing.action.ZoomToAction;
import ch.usi.inf.sape.util.Colors;


/**
 * A Swing GUI component for visualizing a ContextTree.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class TreeView extends JComponent implements Printable {

	private static final Dimension PREFERRED_SIZE = new Dimension(300, 300);

	public static final String FOCUS_SAME = "FOCUS_SAME";
	public static final String CUTOFF = "CUTOFF";
	public static final String SIZE_ATTRIBUTE = "SIZE_ATTRIBUTE";
	public static final String AVAILABLE_SIZE_ATTRIBUTES = "AVALIABLE_SIZE_ATTRIBUTES";
	public static final String SATURATION_ATTRIBUTE = "SATURATION_ATTRIBUTE";
	public static final String AVAILABLE_SATURATION_ATTRIBUTES = "AVALIABLE_SATURATION_ATTRIBUTES";
	public static final String HIGHLIGHT_ATTRIBUTE = "HIGHLIGHT_ATTRIBUTE";
	public static final String AVAILABLE_HIGHLIGHT_ATTRIBUTES = "AVAILABLE_HIGHLIGHT_ATTRIBUTES";
	public static final String HUE_ATTRIBUTE = "HUE_ATTRIBUTE";
	public static final String AVAILABLE_HUE_ATTRIBUTES = "AVAILABLE_HUE_ATTRIBUTES";
	public static final String LABEL_ATTRIBUTE = "LABEL_ATTRIBUTE";
	public static final String TOOLTIP = "TOOLTIP";
	public static final String INFO_LINES = "INFO_LINES";
	public static final String SHOW_TOOLTIPS = "SHOW_TOOLTIPS";
	public static final String SHOW_INFO_OVERLAY = "SHOW_INFO_OVERLAY";
	public static final String SHOW_PROPERTIES_OVERLAY = "SHOW_PROPERTIES_OVERLAY";
	
	
	private Configuration configuration;
	private ConfigurationListener configurationListener;
	private long maxSaturation;
	private ContextTree tree;
	private ContextTreeNode root;
	private ContextTreeNode top;
	private ContextTreeNode current;
	private TreeViewRenderer renderer;
	private final TreeViewRenderer[] availableRenderers;

	private boolean mouseInside;
	private int mouseX;
	private int mouseY;

	private final ArrayList<TreeViewListener> listeners;

	private PageFormat pageFormat; // for printing
	

	public TreeView() {
		this(true);
	}

	public TreeView(final TreeViewRenderer[] availableRenderers) {
		this(true, new Configuration(), availableRenderers);
	}

	public TreeView(boolean interactive) {
		this(interactive, new Configuration());
	}

	public TreeView(final boolean interactive, final Configuration configuration) {
		this(interactive, configuration, new TreeViewRenderer[] {new LinearRenderer(), new HighriseRenderer()});
	}
	
	public TreeView(final boolean interactive, final Configuration configuration, final TreeViewRenderer[] availableRenderers) {
		this.configuration = configuration;
		prepareConfiguration(configuration);
		this.availableRenderers = availableRenderers;
		for (final TreeViewRenderer renderer : availableRenderers) {
			renderer.setView(this);
			renderer.prepareConfiguration(configuration);
		}
		renderer = availableRenderers[0];
		configuration.dump();
		pageFormat = null;
		setBackground(Color.WHITE);
		listeners = new ArrayList<TreeViewListener>();

		configurationListener = new ConfigurationListener() {
			public void treeViewConfigurationChanged(final Property property) {
				repaint();
			}
		};

		configuration.addConfigurationListener(configurationListener);

		if (interactive) {
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(final MouseEvent ev) {
					mouseX = ev.getX();
					mouseY = ev.getY();
					current = findNode(mouseX, mouseY);
					repaint();
					fireCurrentNodeChanged();
				}
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(final MouseEvent ev) {
					mouseInside = true;
					repaint();
				}
				@Override
				public void mouseExited(final MouseEvent ev) {
					mouseInside = false;
					repaint();
				}
				@Override
				public void mousePressed(final MouseEvent ev) {
					if (ev.isPopupTrigger()) {
						showPopup(ev.getX(), ev.getY());
					}
					requestFocus();
				}
				@Override
				public void mouseReleased(final MouseEvent ev) {
					if (ev.isPopupTrigger()) {
						showPopup(ev.getX(), ev.getY());
					}
					if (ev.getButton()==MouseEvent.BUTTON1 && ev.getClickCount()==2) {
						final ContextTreeNode node = findNode(ev.getX(), ev.getY());
						if (node!=null) {
							top = node;
							repaint();
							fireTopNodeChanged();
						}
					}
				}
			});
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(final KeyEvent ev) {
					if (ev.getKeyCode()==KeyEvent.VK_MINUS) {
						final ContextTreeNode node = findParent(null, root, top);
						if (node!=null) {
							zoomTo(node);
						}
					}
				}
			});
		}
		ToolTipManager.sharedInstance().registerComponent(this);
	}
	
	public void prepareConfiguration(final Configuration configuration) {
		configuration.addPropertyIfNotPresent(new Property(FOCUS_SAME, "Focus", Boolean.class, false));
		configuration.addPropertyIfNotPresent(new Property(CUTOFF, "Cutoff", Integer.class, 1));
		final LongAttribute defaultSizeAttribute = new LeafCountAttribute();
		configuration.addPropertyIfNotPresent(new Property(SIZE_ATTRIBUTE, "Size", LongAttribute.class, defaultSizeAttribute));
		configuration.addPropertyIfNotPresent(new Property(AVAILABLE_SIZE_ATTRIBUTES, "Available sizes", LongAttribute[].class, new LongAttribute[] {defaultSizeAttribute}));
		final LongAttribute defaultSaturationAttribute = new LongConstant("Off", 0);
		configuration.addPropertyIfNotPresent(new Property(SATURATION_ATTRIBUTE, "Saturation", LongAttribute.class, defaultSaturationAttribute));
		configuration.addPropertyIfNotPresent(new Property(AVAILABLE_SATURATION_ATTRIBUTES, "Avaliable saturations", LongAttribute[].class, new LongAttribute[] {defaultSaturationAttribute}));
		final BooleanAttribute defaultHighlightAttribute = new BooleanConstant("All", true);
		configuration.addPropertyIfNotPresent(new Property(HIGHLIGHT_ATTRIBUTE, "Highlight", BooleanAttribute.class, defaultHighlightAttribute));
		configuration.addPropertyIfNotPresent(new Property(AVAILABLE_HIGHLIGHT_ATTRIBUTES, "Avaliable highlights", BooleanAttribute[].class, new BooleanAttribute[] {defaultHighlightAttribute, new BooleanConstant("None", false)}));
		final StringAttribute defaultHueAttribute = new StringConstant("Off", "");
		configuration.addPropertyIfNotPresent(new Property(HUE_ATTRIBUTE, "Hue", StringAttribute.class, defaultHueAttribute));
		configuration.addPropertyIfNotPresent(new Property(AVAILABLE_HUE_ATTRIBUTES, "Avaliable hues", StringAttribute[].class, new StringAttribute[] {defaultHueAttribute}));
		configuration.addPropertyIfNotPresent(new Property(LABEL_ATTRIBUTE, "Label", StringAttribute.class, new StringConstant("label")));
		configuration.addPropertyIfNotPresent(new Property(TOOLTIP, "Tooltip", StringAttribute.class, new StringConstant("tooltip")));
		configuration.addPropertyIfNotPresent(new Property(INFO_LINES, "Info lines", StringAttribute[].class, new StringAttribute[0]));
		configuration.addPropertyIfNotPresent(new Property(SHOW_TOOLTIPS, "Show tooltip", Boolean.class, true));
		configuration.addPropertyIfNotPresent(new Property(SHOW_INFO_OVERLAY, "Show info", Boolean.class, true));
		configuration.addPropertyIfNotPresent(new Property(SHOW_PROPERTIES_OVERLAY, "Show properties", Boolean.class, true));
		
		configuration.lookup(SATURATION_ATTRIBUTE).addPropertyListener(new PropertyListener() {
			public void propertyChanged(Property property) {
				recomputeMaxSaturation();
			}
		});
	}
	
	public final int getMouseX() {
		return mouseX;
	}

	public final int getMouseY() {
		return mouseY;
	}

	public final boolean isMouseInside() {
		return mouseInside;
	}

	public final ContextTree getTree() {
		return tree;
	}

	public final ContextTreeNode getRoot() {
		return root;
	}

	public final ContextTreeNode getTop() {
		return top;
	}

	public final ContextTreeNode getCurrent() {
		return current;
	}
	
	public TreeViewRenderer getRenderer() {
		return renderer;
	}
	
	public void setRenderer(final TreeViewRenderer renderer) {
		this.renderer = renderer;
		renderer.recomputeStatistics();
		fireRendererChanged();
		repaint();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(final Configuration configuration) {
		if (this.configuration!=null) {
			this.configuration.removeConfigurationListener(configurationListener);
		}
		this.configuration = configuration;
		if (this.configuration!=null) {
			this.configuration.addConfigurationListener(configurationListener);
		}
		repaint();
	}

	
	//--- configuration methods
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public boolean getFocusSame() {
		return configuration.lookup(FOCUS_SAME).getBoolean();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setFocusSame(final boolean same) {
		configuration.lookup(FOCUS_SAME).setBoolean(same);
	}

	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public BooleanAttribute getHighlightAttribute() {
		return configuration.lookup(HIGHLIGHT_ATTRIBUTE).getBooleanAttribute();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setHighlightAttribute(final BooleanAttribute attribute) {
		configuration.lookup(HIGHLIGHT_ATTRIBUTE).setBooleanAttribute(attribute);
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public List<BooleanAttribute> getAvailableHighlightAttributes() {
		return configuration.lookup(AVAILABLE_HIGHLIGHT_ATTRIBUTES).getBooleanAttributes();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void addAvailableHighlightAttribute(final BooleanAttribute attribute) {
		configuration.lookup(AVAILABLE_HIGHLIGHT_ATTRIBUTES).addBooleanAttribute(attribute);
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void clearAvailableHighlightAttributes() {
		configuration.lookup(AVAILABLE_HIGHLIGHT_ATTRIBUTES).clear();
	}

	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public StringAttribute getHueAttribute() {
		return configuration.lookup(HUE_ATTRIBUTE).getStringAttribute();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setHueAttribute(final StringAttribute attribute) {
		configuration.lookup(HUE_ATTRIBUTE).setStringAttribute(attribute);
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public List<StringAttribute> getAvailableHueAttributes() {
		return configuration.lookup(AVAILABLE_HUE_ATTRIBUTES).getStringAttributes();
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void addAvailableHueAttribute(final StringAttribute attribute) {
		configuration.lookup(AVAILABLE_HUE_ATTRIBUTES).addStringAttribute(attribute);
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void clearAvailableHueAttributes() {
		configuration.lookup(AVAILABLE_HUE_ATTRIBUTES).clear();
	}
	
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public LongAttribute getSaturationAttribute() {
		return configuration.lookup(SATURATION_ATTRIBUTE).getLongAttribute();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setSaturationAttribute(final LongAttribute attribute) {
		configuration.lookup(SATURATION_ATTRIBUTE).setLongAttribute(attribute);
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public List<LongAttribute> getAvailableSaturationAttributes() {
		return configuration.lookup(AVAILABLE_SATURATION_ATTRIBUTES).getLongAttributes();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void addAvailableSaturationAttribute(final LongAttribute attribute) {
		configuration.lookup(AVAILABLE_SATURATION_ATTRIBUTES).addLongAttribute(attribute);
	}

	public void clearAvailableSaturationAttributes() {
		configuration.lookup(AVAILABLE_SATURATION_ATTRIBUTES).clear();
	}
	
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public LongAttribute getSizeAttribute() {
		return configuration.lookup(SIZE_ATTRIBUTE).getLongAttribute();
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 * @param attribute must be an inclusive attribute, that is, 
	 * the value of a node must be greater or equal to the sum of the values of its children. 
	 */
	public void setSizeAttribute(final LongAttribute attribute) {
		configuration.lookup(SIZE_ATTRIBUTE).setLongAttribute(attribute);
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public List<LongAttribute> getAvailableSizeAttributes() {
		return configuration.lookup(AVAILABLE_SIZE_ATTRIBUTES).getLongAttributes();
	}

	/**
	 * @param metric must be an inclusive metric, that is, 
	 * the value of a node must be greater or equal to the sum of the values of its children. 
	 */
	public void addAvailableSizeAttribute(final LongAttribute attribute) {
		configuration.lookup(AVAILABLE_SIZE_ATTRIBUTES).addLongAttribute(attribute);
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void clearAvailableSizeAttributes() {
		configuration.lookup(AVAILABLE_SIZE_ATTRIBUTES).clear();
	}
	
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public StringAttribute getLabelAttribute() {
		return configuration.lookup(LABEL_ATTRIBUTE).getStringAttribute();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setLabelAttribute(final StringAttribute attribute) {
		configuration.lookup(LABEL_ATTRIBUTE).setStringAttribute(attribute);
	}

	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public List<StringAttribute> getInfoLineAttributes() {
		return configuration.lookup(INFO_LINES).getStringAttributes();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void addInfoLineAttribute(final StringAttribute attribute) {
		configuration.lookup(INFO_LINES).addStringAttribute(attribute);
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void clearInfoLineAttributes() {
		configuration.lookup(INFO_LINES).clear();
	}
	
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public StringAttribute getTooltipAttribute() {
		return configuration.lookup(TOOLTIP).getStringAttribute();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setTooltipAttribute(final StringAttribute attribute) {
		configuration.lookup(TOOLTIP).setStringAttribute(attribute);
	}
	
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public boolean getShowTooltips() {
		return configuration.lookup(SHOW_TOOLTIPS).getBoolean();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setShowTooltips(final boolean show) {
		configuration.lookup(SHOW_TOOLTIPS).setBoolean(show);
	}
	
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public boolean getShowInfoOverlay() {
		return configuration.lookup(SHOW_INFO_OVERLAY).getBoolean();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setShowInfoOverlay(final boolean show) {
		configuration.lookup(SHOW_INFO_OVERLAY).setBoolean(show);
	}

	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public boolean getShowPropertiesOverlay() {
		return configuration.lookup(SHOW_PROPERTIES_OVERLAY).getBoolean();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setShowPropertiesOverlay(final boolean show) {
		configuration.lookup(SHOW_PROPERTIES_OVERLAY).setBoolean(show);
	}

	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public int getCutoff() {
		return configuration.lookup(CUTOFF).getInt();
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setCutoff(final int cutoff) {
		configuration.lookup(CUTOFF).setInt(cutoff);
	}
	

	//---
	private void showPopup(final int x, final int y) {
		final JPopupMenu popup = new JPopupMenu();
		//final ContextTreeNode node = findNode(x, y);
		final ContextTreeNode parentOfTop = findParent(null, root, top);
		final ContextTreeNode topSplit = findSplit(root);
		final StringAttribute nodeNameAttribute = getLabelAttribute();
		popup.add(new ZoomToAction("Zoom in", nodeNameAttribute, current, this));
		popup.add(new ZoomToAction("Zoom out", nodeNameAttribute, parentOfTop, this));
		popup.add(new ZoomToAction("Zoom all", nodeNameAttribute, root, this));
		popup.add(new ZoomToAction("Zoom to top split", nodeNameAttribute, topSplit, this));
		
		final JMenu cutoffMenu = new JMenu("Cutoff");
		cutoffMenu.add(new SetCutoffAction(0, this));
		cutoffMenu.add(new SetCutoffAction(1, this));
		cutoffMenu.add(new SetCutoffAction(10, this));
		cutoffMenu.add(new SetCutoffAction(100, this));
		popup.add(cutoffMenu);

		popup.addSeparator();
		
		final JMenu showAsMenu = new JMenu("Show As");
		for (final TreeViewRenderer renderer : availableRenderers) {
			showAsMenu.add(new ShowAsAction(renderer, this));
		}
		popup.add(showAsMenu);
		
		renderer.addPopupMenuItems(popup);
		
		final JMenu sizeMenu = new JMenu("Size");
		final List<LongAttribute> attributes = getAvailableSizeAttributes();
		for (final LongAttribute attribute : attributes) {
			sizeMenu.add(new SetLongAttributePropertyAction(getConfiguration().lookup(SIZE_ATTRIBUTE), attribute));
		}
		popup.add(sizeMenu);

		final JMenu saturationMenu = new JMenu("Saturation");
		for (final LongAttribute attribute : getAvailableSaturationAttributes()) {
			saturationMenu.add(new SetLongAttributePropertyAction(configuration.lookup(SATURATION_ATTRIBUTE), attribute));
		}
		popup.add(saturationMenu);
		
		final JMenu hueMenu = new JMenu("Hue");
		for (final StringAttribute attribute : getAvailableHueAttributes()) {
			hueMenu.add(new SetStringAttributePropertyAction(configuration.lookup(HUE_ATTRIBUTE), attribute));
		}
		popup.add(hueMenu);
		
		popup.addSeparator();
		
		final JMenu focusMenu = new JMenu("Focus");
		focusMenu.add(new SetBooleanPropertyAction(configuration.lookup(FOCUS_SAME), true, "Same", "Focus all nodes with same label"));
		focusMenu.add(new SetBooleanPropertyAction(configuration.lookup(FOCUS_SAME), false, "Selected", "Focus only the selected node"));
		popup.add(focusMenu);
		
		final JMenu highlightMenu = new JMenu("Highlight");
		for (final BooleanAttribute attribute : getAvailableHighlightAttributes()) {
			highlightMenu.add(new SetBooleanAttributePropertyAction(configuration.lookup(HIGHLIGHT_ATTRIBUTE), attribute));
		}
		popup.add(highlightMenu);
		
		popup.add(new ToggleBooleanPropertyAction(getConfiguration().lookup(SHOW_TOOLTIPS), "Show Tooltips", "Hide Tooltips", "Show or hide tooltips"));
		popup.add(new ToggleBooleanPropertyAction(getConfiguration().lookup(SHOW_INFO_OVERLAY), "Show Info Overlay", "Hide Info Overlay", "Show or hide info overlay"));
		popup.add(new ToggleBooleanPropertyAction(getConfiguration().lookup(SHOW_PROPERTIES_OVERLAY), "Show Properties Overlay", "Hide Properties Overlay", "Show or hide properties overlay"));
				
		final JMenu exportMenu = new JMenu("Export");
		exportMenu.add(new PrintAction(this));
		exportMenu.add(new PageSetupAction(this));
		exportMenu.addSeparator();
		exportMenu.add(new ExportAsImageAction(this));
		/*
		for (final ActionProvider ap : PluginManager.getInstance().createExtensionInstances("ch.usi.inf.sape.trevis.action.ActionProvider", ActionProvider.class)) {
			ap.addActions(this, exportMenu);
		}
		*/
		popup.add(exportMenu);
		popup.show(this, x, y);
	}

	
	private ContextTreeNode findNode(int x, int y) {
		return renderer.findNode(x, y);
	}

	protected final ContextTreeNode findParent(final ContextTreeNode parent, final ContextTreeNode node, final ContextTreeNode child) {
		if (node==child) {
			return parent;
		} else {
			for (int i=0; i<node.getChildCount(); i++) {
				final ContextTreeNode n = findParent(node, node.getChild(i), child);
				if (n!=null) {
					return n;
				}
			}
		}
		return null;
	}

	private ContextTreeNode findSplit(final ContextTreeNode node) {
		if (node==null) {
			return null;
		}
		if (node.getChildCount()==1) {
			return findSplit(node.getChild(0));
		} else {
			return node;
		}
	}


	/**
	 * Show a complete ContextTree.
	 */
	public void setTree(final ContextTree tree) {
		setRoot(tree, tree==null?null:tree.getRoot());
	}

	/**
	 * Show a subtree, where we do not know the ContextTree to which that subtree belongs.
	 * However, use setRoot(ContextTree tree, ContextTreeNode root) instead, 
	 * if you want to show a complete ContextTree.
	 * That way we can also render the tree's label (ContextTree.getLabel()).
	 */
	public void setRoot(final ContextTreeNode subtreeRoot) {
		setRoot(null, subtreeRoot);
	}

	/**
	 * Show a subtree, where we know the ContextTree to which that subtree belongs.
	 */
	public void setRoot(final ContextTree tree, final ContextTreeNode subtreeRoot) {
		this.tree = tree;
		this.root = subtreeRoot;
		this.top = subtreeRoot;
		current = null;
		recomputeStatistics();
		repaint();
		fireTreeChanged();
	}
	
	public void recomputeStatistics() {
		recomputeMaxSaturation();
		renderer.recomputeStatistics(); //TODO: if switching between multiple renderers, maybe ask all of them to recompute?
		// Maybe our current CategoryMap has seen this tree for the first time,
		// so update the CategoryMap so it knows about all possible categories
		//recomputeMaxHue();
	}


	public void zoomToTopSplit() {
		zoomTo(findSplit(getRoot()));
	}
	
	public void zoomTo(final ContextTreeNode top) {
		this.top = top;
		repaint();
		fireTopNodeChanged();
	}

	@Override
	public Dimension getPreferredSize() {
		return isPreferredSizeSet()?super.getPreferredSize():PREFERRED_SIZE;
	}

	@Override
	public String getToolTipText(final MouseEvent ev) {
		if (getShowTooltips()) {
			final StringAttribute toolTipAttribute = getTooltipAttribute();
			final ContextTreeNode node = findNode(ev.getX(), ev.getY());
			if (node!=null) {
				return (String)toolTipAttribute.getValue(node);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D g2 = (Graphics2D)g; 
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		final int width = getWidth();
		final int height = getHeight();
		g.setColor(getBackground());
		g.fillRect(0, 0, width, height);
		render(g2, new Surface(getWidth(), getHeight()));
	}

	public void render(final Graphics2D g2, final Surface surface) {
		final ContextTreeNode top = getTop();
		if (top!=null) {
			renderTree(g2, surface);
			if (getShowInfoOverlay()) {
				renderInfoOverlay(g2, surface);
			}
			if (getShowPropertiesOverlay()) {
				renderPropertiesOverlay(g2, surface);
			}
		}		
	}
	
	private void renderTree(final Graphics2D g2, final Surface surface) {
		renderer.renderTree(g2, surface);
	}

	protected void renderInfoOverlay(final Graphics2D g, final Surface surface) {
		final ContextTree tree = getTree();
		final ContextTreeNode current = getCurrent();
		final List<StringAttribute> infoAttributes = getInfoLineAttributes();

		final FontMetrics fm = g.getFontMetrics();
		final int lineHeight = fm.getHeight();
		final int infoLines = (current==null?0:infoAttributes.size())+((tree==null||tree.getName()==null)?0:1);
		if (infoLines>0) {
			g.setColor(new Color(255,255,255,128));
			final int offset = 10;
			g.fillRoundRect(3, -offset, surface.getWidth()-6, offset+lineHeight*infoLines+fm.getDescent(), 6, 6);
			g.setColor(Color.WHITE);
			g.drawRoundRect(3, -offset, surface.getWidth()-6, offset+lineHeight*infoLines+fm.getDescent(), 6, 6);
			int y = lineHeight-2;
			g.setColor(Color.BLACK);
			if (tree!=null && tree.getName()!=null) {
				final Font f = g.getFont();
				g.setFont(f.deriveFont(Font.BOLD));
				g.drawString(tree.getName(), 5, y);
				g.setFont(f);
				y += lineHeight;	
			}
			if (current!=null) {
				for (final StringAttribute attribute : infoAttributes) {
					g.drawString(attribute.getValue(current).toString(), 5, y);
					y += lineHeight;
				}
			}
		}
	}

	protected void renderPropertiesOverlay(final Graphics2D g, final Surface surface) {
		final ContextTreeNode root = getRoot();
		final ContextTreeNode current = getCurrent();
		final StringAttribute hueMetric = getHueAttribute();
		final LongAttribute saturationMetric = getSaturationAttribute();
		final LongAttribute sizeMetric = getSizeAttribute();
		final FontMetrics fm = g.getFontMetrics();
		if (current!=null) {
			final long sizeRootValue = sizeMetric.evaluate(root); 
			if (sizeRootValue!=0) {
				final int lines = 1+(saturationMetric==null?0:1)+(hueMetric==null?0:1);
				int y = surface.getHeight();
				g.setColor(new Color(255,255,255,128));
				g.fillRoundRect(3, y-(lines*15+3), surface.getWidth()-6, 60, 6, 6);
				g.setColor(Color.WHITE);
				g.drawRoundRect(3, y-(lines*15+3), surface.getWidth()-6, 60, 6, 6);
				final long angleInclusiveValue = sizeMetric.evaluate(current);
				long angleExclusiveValue = angleInclusiveValue;
				for (final ContextTreeNode child : current) {
					angleExclusiveValue -= sizeMetric.evaluate(child);
				}
				// size
				g.setColor(Color.BLACK);
				g.drawString(
						sizeMetric.getName()+": "+
						angleInclusiveValue+" ("+(angleInclusiveValue*100/sizeRootValue)+"%), Exclusive: "+
						angleExclusiveValue+" ("+(angleExclusiveValue*100/sizeRootValue)+"%)", 23, y-fm.getDescent()); //NORMALIZATION BY ROOT
				// saturation
				if (saturationMetric!=null) {
					y -= 15;
					g.setColor(new Color(Colors.hsbToRgb(Colors.createHsb(0, getSaturation(current), 200))));
					g.fillOval(5, y-15, 14, 14);
					g.setColor(Color.BLACK);
					g.drawString(saturationMetric.getName()+": "+saturationMetric.evaluate(current), 23, y-fm.getDescent());
				}
				// hue
				if (hueMetric!=null) {
					final String hueMetricValue = hueMetric.evaluate(current);
					y -= 15;
					g.setColor(new Color(Colors.hsbToRgb(Colors.createHsb(getHue(current), 255, 200))));
					g.fillOval(5, y-15, 14, 14);
					g.setColor(Color.BLACK);
					g.drawString(hueMetric.getName()+": "+(hueMetricValue==null?"":hueMetricValue), 23, y-fm.getDescent());
				}
			}
		}
	}

	public final int getPathLengthToRoot(final ContextTreeNode node) {
		if (node.isRoot()) {
			return 0;
		} else {
			return getPathLengthToRoot(node.getParent())+1;
		}
	}

	public final int getHsb(final ContextTreeNode node, final boolean focus) {
		if (getHighlightAttribute().evaluate(node)) {
			final int hue = getHue(node);
			final int saturation = getSaturation(node);
			return Colors.createHsb(hue, saturation, focus?100:200);
		} else {
			return Colors.createHsb(180, 0, focus?100:200);
		}
	}

	private final int getHue(final ContextTreeNode node) {
		final StringAttribute hueMetric = getHueAttribute();
		if (hueMetric==null) {
			return 0;
		} else {
			final String category = hueMetric.evaluate(node);
			return Math.abs(category.hashCode())%360;
			//return hueMapping.getCategoryId(category)*359/hueMapping.getNumberOfCategories();
		}
	}

	private final int getSaturation(final ContextTreeNode node) {
		final LongAttribute saturationMetric = getSaturationAttribute();
		if (saturationMetric==null) {
			return 200;
		} else {
			return (int)(maxSaturation==0?255:255*saturationMetric.evaluate(node)/maxSaturation); //NORMALIZATION BY MAX
		}
	}


	private void recomputeMaxSaturation() {
		if (getSaturationAttribute()!=null) {
			maxSaturation = Long.MIN_VALUE;
			if (root!=null) {
				recomputeMaxSaturation(root);
			}
		}
	}

	private void recomputeMaxSaturation(final ContextTreeNode node) {
		maxSaturation = Math.max(maxSaturation, getSaturationAttribute().evaluate(node));
		for (final ContextTreeNode child : node) {
			recomputeMaxSaturation(child);
		}
	}

	/*
	private void recomputeMaxHue() {
		if (getHueAttribute()!=null) {
			hueMapping.reset();
			if (root!=null) {
				recomputeMaxHue(root);
			}
		}
	}

	private void recomputeMaxHue(final ContextTreeNode<T> node) {
		final String category = getHueAttribute().evaluate(node);
		hueMapping.addCategory(category);
		for (final ContextTreeNode child : node) {
			recomputeMaxHue(child);
		}		
	}
	*/
	
	//--- printing
	public PageFormat getPageFormat() {
		return pageFormat;
	}
	
	public void setPageFormat(final PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}

	public int print(final Graphics g, final PageFormat pageFormat, final int pageIndex) {
		if (pageIndex>0) {
			return NO_SUCH_PAGE;
		}
		final Graphics2D g2 = (Graphics2D)g;
		System.out.println("PageFormat.getWidth(): "+pageFormat.getWidth());
		System.out.println("PageFormat.getHeight(): "+pageFormat.getHeight());
		System.out.println("PageFormat.getImageableX(): "+pageFormat.getImageableX());
		System.out.println("PageFormat.getImageableY(): "+pageFormat.getImageableY());
		System.out.println("PageFormat.getImageableWidth(): "+pageFormat.getImageableWidth());
		System.out.println("PageFormat.getImageableHeight(): "+pageFormat.getImageableHeight());
		System.out.println("TreeView.getWidth(): "+getWidth());
		System.out.println("TreeView.getHeight(): "+getHeight());
		// move origin to top-left imaginable corner (skip margins)
		g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		final double scaleX = pageFormat.getImageableWidth()/getWidth();
		final double scaleY = pageFormat.getImageableHeight()/getHeight();
		// make sure to preserve aspect ratio
		final double scale = Math.min(scaleX, scaleY);
		g2.scale(scale, scale);
		final boolean wasBuffered = isDoubleBuffered();
		setDoubleBuffered(false);
		paint(g2);
		setDoubleBuffered(wasBuffered);
		return PAGE_EXISTS;
	}

	
	//--- listener management
	public void addTreeViewListener(final TreeViewListener li) {
		listeners.add(li);
	}

	public void removeTreeViewListener(final TreeViewListener li) {
		listeners.remove(li);
	}

	private void fireTreeChanged() {
		for (final TreeViewListener li : listeners) {
			li.treeChanged(this);
		}		
	}

	private void fireTopNodeChanged() {
		for (final TreeViewListener li : listeners) {
			li.topNodeChanged(this);
		}		
	}

	private void fireCurrentNodeChanged() {
		for (final TreeViewListener li : listeners) {
			li.currentNodeChanged(this);
		}		
	}

	private void fireRendererChanged() {
		for (final TreeViewListener li : listeners) {
			li.rendererChanged(this);
		}		
	}

}
