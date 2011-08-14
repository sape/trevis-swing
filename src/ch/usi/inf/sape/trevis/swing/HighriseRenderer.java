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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import ch.usi.inf.sape.trevis.model.ContextTreeNode;
import ch.usi.inf.sape.trevis.model.attribute.ChildCountAttribute;
import ch.usi.inf.sape.trevis.model.attribute.LongAttribute;
import ch.usi.inf.sape.trevis.swing.action.SetEnumPropertyAction;
import ch.usi.inf.sape.trevis.swing.action.SetIntPropertyAction;
import ch.usi.inf.sape.trevis.swing.action.SetLongAttributePropertyAction;
import ch.usi.inf.sape.util.Colors;


/**
 * A HighriseRenderer is like a LinearRenderer, 
 * except that the height of the rectangles is not constant but depends on an attribute.
 * 
 * Based on Andrea Adamoli's idea.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class HighriseRenderer extends TreeViewRenderer {

	private static enum LabelVisibility { 
		HIDE_ALL("Hide all"), HIDE_ZERO("Hide for zero size nodes"), SHOW_ALL("Show all");
		
		private final String name;
		
		private LabelVisibility(final String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public static final String HORIZONTAL_GAP = "HORIZONTAL_GAP";
	public static final String VERTICAL_GAP = "VERTICAL_GAP";
	public static final String FIXED_HEIGHT = "FIXED_HEIGHT";
	public static final String MAX_VARIABLE_HEIGHT = "MAX_VARIABLE_HEIGHT";
	public static final String LABEL_VISIBILITY = "LABEL_VISIBILITY";
	public static final String HEIGHT_ATTRIBUTE = "HEIGHT_ATTRIBUTE";
	public static final String AVAILABLE_HEIGHT_ATTRIBUTES = "AVAILABLE_HEIGHT_ATTRIBUTES";
	
	private long maxHeightMetricValue;

	
	public HighriseRenderer() {
	}

	@Override
	public String getName() {
		return "Highrise";
	}

	
	//--- configuration management
	@Override
	public void prepareConfiguration(final Configuration configuration) {
		configuration.addPropertyIfNotPresent(new Property(HORIZONTAL_GAP, "Horizontal gap", Integer.class, 1));
		configuration.addPropertyIfNotPresent(new Property(VERTICAL_GAP, "Vertical gap", Integer.class, 1));
		configuration.addPropertyIfNotPresent(new Property(FIXED_HEIGHT, "Fixed height", Integer.class, 3));
		configuration.addPropertyIfNotPresent(new Property(MAX_VARIABLE_HEIGHT, "Max variable height", Integer.class, 80));
		configuration.addPropertyIfNotPresent(new Property(LABEL_VISIBILITY, "Label visibility", LabelVisibility.class, LabelVisibility.SHOW_ALL));
		final LongAttribute defaultHeightAttribute = new ChildCountAttribute();
		configuration.addPropertyIfNotPresent(new Property(HEIGHT_ATTRIBUTE, "Height", LongAttribute.class, defaultHeightAttribute));
		configuration.addPropertyIfNotPresent(new Property(AVAILABLE_HEIGHT_ATTRIBUTES, "Available heights", LongAttribute[].class, new LongAttribute[] {defaultHeightAttribute}));

		configuration.lookup(HEIGHT_ATTRIBUTE).addPropertyListener(new PropertyListener() {
			public void propertyChanged(Property property) {
				recomputeMaxHeightMetricValue();
			}
		});
	}
		
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public int getHorizontalGap() {
		return getConfiguration().lookup(HORIZONTAL_GAP).getInt();
	}
		
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setHorizontalGap(final int gap) {
		getConfiguration().lookup(HORIZONTAL_GAP).setInt(gap);
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public int getVerticalGap() {
		return getConfiguration().lookup(VERTICAL_GAP).getInt();
	}
		
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setVerticalGap(final int gap) {
		getConfiguration().lookup(VERTICAL_GAP).setInt(gap);
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public int getFixedHeight() {
		return getConfiguration().lookup(FIXED_HEIGHT).getInt();
	}
		
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setFixedHeight(final int height) {
		getConfiguration().lookup(FIXED_HEIGHT).setInt(height);
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public int getMaxVariableHeight() {
		return getConfiguration().lookup(MAX_VARIABLE_HEIGHT).getInt();
	}
		
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setMaxVariableHeight(final int height) {
		getConfiguration().lookup(MAX_VARIABLE_HEIGHT).setInt(height);
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public LabelVisibility getLabelVisibility() {
		return (LabelVisibility)getConfiguration().lookup(LABEL_VISIBILITY).getValue();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setLabelVisibility(final LabelVisibility visibility) {
		getConfiguration().lookup(LABEL_VISIBILITY).setValue(visibility);
	}
	
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public LongAttribute getHeightAttribute() {
		return getConfiguration().lookup(HEIGHT_ATTRIBUTE).getLongAttribute();
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setHeightAttribute(final LongAttribute attribute) {
		getConfiguration().lookup(HEIGHT_ATTRIBUTE).setLongAttribute(attribute);
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public List<LongAttribute> getAvailableHeightAttributes() {
		return getConfiguration().lookup(AVAILABLE_HEIGHT_ATTRIBUTES).getLongAttributes();
	}

	/**
	 * @param metric must be an inclusive metric, that is, 
	 * the value of a node must be greater or equal to the sum of the values of its children. 
	 */
	public void addAvailableHeightAttribute(final LongAttribute attribute) {
		getConfiguration().lookup(AVAILABLE_HEIGHT_ATTRIBUTES).addLongAttribute(attribute);
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void clearAvailableHeightAttributes() {
		getConfiguration().lookup(AVAILABLE_HEIGHT_ATTRIBUTES).clear();
	}
	
	
	//--- statistics
	@Override
	public void recomputeStatistics() {
		recomputeMaxHeightMetricValue();
	}

	private void recomputeMaxHeightMetricValue() {
		if (getHeightAttribute()!=null) {
			maxHeightMetricValue = Long.MIN_VALUE;
			if (getRoot()!=null) {
				recomputeMaxHeightMetricValue(getRoot());
			}
		}
	}

	private void recomputeMaxHeightMetricValue(final ContextTreeNode node) {
		maxHeightMetricValue = Math.max(maxHeightMetricValue, getHeightAttribute().evaluate(node));
		for (final ContextTreeNode child : node) {
			recomputeMaxHeightMetricValue(child);
		}
	}

	
	//--- menu management
	@Override
	public void addPopupMenuItems(final JPopupMenu popup) {
		popup.addSeparator();
		
		final JMenu verticalGapMenu = new JMenu("Vertical Gap");
		for (int i=0; i<7; i++) {
			verticalGapMenu.add(new SetIntPropertyAction(getConfiguration().lookup(VERTICAL_GAP), i, i+" pixels", "Set vertical gap size to "+i+" pixels"));
		}
		popup.add(verticalGapMenu);
		
		final JMenu horizontalGapMenu = new JMenu("Horizontal Gap");
		for (int i=0; i<7; i++) {
			horizontalGapMenu.add(new SetIntPropertyAction(getConfiguration().lookup(HORIZONTAL_GAP), i, i+" pixels", "Set horizontal gap size to "+i+" pixels"));
		}
		popup.add(horizontalGapMenu);
		
		final JMenu fixedHeightMenu = new JMenu("Fixed Height");
		for (int i : new int[] {0, 1, 2, 3, 4, 6, 8, 10, 12, 14, 16, 20}) {
			fixedHeightMenu.add(new SetIntPropertyAction(getConfiguration().lookup(FIXED_HEIGHT), i, i+" pixels", "Set fixed height to "+i+" pixels"));
		}
		popup.add(fixedHeightMenu);

		final JMenu maxVariableHeightMenu = new JMenu("Max Variable Height");
		for (int i : new int[] {0, 10, 40, 80, 120}) {
			maxVariableHeightMenu.add(new SetIntPropertyAction(getConfiguration().lookup(MAX_VARIABLE_HEIGHT), i, i+" pixels", "Set maximum variable height to "+i+" pixels"));
		}
		popup.add(maxVariableHeightMenu);
		
		final JMenu labelVisibilityMenu = new JMenu("Labels");
		for (final LabelVisibility v : LabelVisibility.values()) {
			labelVisibilityMenu.add(new SetEnumPropertyAction<LabelVisibility>(getConfiguration().lookup(LABEL_VISIBILITY), v, v.toString(), v.toString()));
		}
		popup.add(labelVisibilityMenu);
		
		popup.addSeparator();
		
		final JMenu heightMenu = new JMenu("Height");
		for (final LongAttribute attribute : getAvailableHeightAttributes()) {
			heightMenu.add(new SetLongAttributePropertyAction(getConfiguration().lookup(HEIGHT_ATTRIBUTE), attribute));
		}
		popup.add(heightMenu);

	}

	
	//--- hit testing
	@Override
	public ContextTreeNode findNode(final int x, final int y) {
		if (getTop()==null) {
			return null;
		}
		return findNode(getTop(), x, y, 0, getWidth(), 0);
	}
	
	private ContextTreeNode findNode(final ContextTreeNode node, final int mx, final int my, final int x, final int w, final int baseHeight) {
		final int gap = getHorizontalGap();
		if (w<2*gap) {
			return null;
		}
		final LongAttribute sizeMetric = getView().getSizeAttribute();
		final long size = sizeMetric.evaluate(node);
		if (size==0) {
			return null;
		}
		if (size < getView().getCutoff()*sizeMetric.evaluate(getRoot())/1000) {
			// cut-off at cutoff 1000ths of root's angle
			return null;
		}

		final LongAttribute heightMetric = getHeightAttribute();
		final long heightMetricValue = heightMetric.evaluate(node);
		final int variableHeight = (int)(getMaxVariableHeight()*heightMetricValue/maxHeightMetricValue);

		final int yBottom = getHeight()-1-baseHeight;
		final int yTop = getHeight()-1-baseHeight-getFixedHeight()-variableHeight;

		long sum = 0;
		for (int c = 0; c<node.getChildCount(); c++) {
			final long childValue = sizeMetric.evaluate(node.getChild(c));
			final int childLeftX = (int)(x+(w*sum/size));
			final int childRightX = (int)(x+(w*(sum+childValue)/size));
			final int childWidth = childRightX-childLeftX;
			final ContextTreeNode hit = findNode(node.getChild(c), mx, my, childLeftX, childWidth, baseHeight+getFixedHeight()+variableHeight);
			if (hit!=null) {
				return hit;
			}
			sum += childValue;
		}
		
		if (mx>=x && mx<x+w && my>=yTop && my<yBottom) {
			return node;
		} else {
			return null;
		}
	}

	
	//--- rendering
	@Override
	public void renderTree(final Graphics2D g2, final Surface surface) {
		renderNode(g2, surface, getTop(), 0, surface.getWidth(), 0);
	}

	private void renderNode(final Graphics2D g2, final Surface surface, final ContextTreeNode node, final int x, final int w, final int baseHeight) {
		final int gap = getHorizontalGap();
		if (w<2*gap || w<1) {
			return;
		}
		final LongAttribute widthMetric = getView().getSizeAttribute();
		final long size = widthMetric.evaluate(node);
		if (size==0) {
			return;
		}
		if (size < getView().getCutoff()*widthMetric.evaluate(getRoot())/1000) {
			// cut-off at cutoff 1000ths of root's angle
			return;
		}


		// background
		final boolean focusSame = getView().getFocusSame();
		final ContextTreeNode current = getCurrent();
		final boolean focused = node==current || (focusSame && 
				(current!=null && node!=null && current.getLabel()!=null && node.getLabel()!=null && current.getLabel().equals(node.getLabel())));
		final int hsb = getHsb(node, focused);
		g2.setColor(new Color(Colors.hsbToRgb(hsb)));

		final LongAttribute heightMetric = getHeightAttribute();
		final long heightMetricValue = heightMetric.evaluate(node);
		final int variableHeight = (int)(getMaxVariableHeight()*heightMetricValue/maxHeightMetricValue);

		final int yBottom = surface.getHeight()-1-baseHeight;
		final int yTop = surface.getHeight()-1-baseHeight-getFixedHeight()-variableHeight;
		final int h = yBottom-yTop-getVerticalGap();
		g2.fillRect(x+gap, yTop, w-gap, h);

		// name
		final LabelVisibility labelVisibility = getLabelVisibility();
		if (labelVisibility==LabelVisibility.SHOW_ALL || (labelVisibility==LabelVisibility.HIDE_ZERO && variableHeight>0)) {
			g2.setColor(Color.WHITE);
			final int cx = x+w/2;
			final int cy = yTop+h/2;
			final String label = ""+getView().getLabelAttribute().getValue(node); 
			final FontMetrics fm = g2.getFontMetrics();
			final float labelWidth = (float)fm.getStringBounds(label, g2).getWidth();
			final int labelOffset = fm.getHeight()/2-fm.getDescent();
			final Shape clip = g2.getClip();
			g2.setClip(x, yTop, w, h);
			g2.drawString(label, cx-labelWidth/2, cy+labelOffset);
			g2.setClip(clip);
		}
		
		// children
		long sum = 0;
		for (int c = 0; c<node.getChildCount(); c++) {
			final long childValue = widthMetric.evaluate(node.getChild(c));
			final int childLeftX = (int)(x+(w*sum/size));
			final int childRightX = (int)(x+(w*(sum+childValue)/size));
			final int childWidth = childRightX-childLeftX;
			renderNode(g2, surface, node.getChild(c), childLeftX, childWidth, baseHeight+getFixedHeight()+variableHeight);
			sum += childValue;
		}
	}
	
}
