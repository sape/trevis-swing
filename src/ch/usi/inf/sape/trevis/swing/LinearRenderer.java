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

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import ch.usi.inf.sape.trevis.model.ContextTreeNode;
import ch.usi.inf.sape.trevis.model.attribute.HeightAttribute;
import ch.usi.inf.sape.trevis.model.attribute.LongAttribute;
import ch.usi.inf.sape.trevis.swing.action.SetIntPropertyAction;
import ch.usi.inf.sape.trevis.swing.action.ToggleBooleanPropertyAction;
import ch.usi.inf.sape.util.Colors;


/**
 * A LinearRenderer is like a RadialRenderer, but with unwound (straight) rings (rectangles), 
 * starting at the top (instead of in the center).
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class LinearRenderer extends TreeViewRenderer {

	public static final String HORIZONTAL_GAP = "HORIZONTAL_GAP";
	public static final String VERTICAL_GAP = "VERTICAL_GAP";
	public static final String SHOW_LABELS = "SHOW_LABELS";
	
	
	public LinearRenderer() {
	}

	@Override
	public String getName() {
		return "Linear";
	}
	
	//--- configuration management
	@Override
	public void prepareConfiguration(final Configuration configuration) {
		configuration.addPropertyIfNotPresent(new Property(HORIZONTAL_GAP, "Horizontal gap", Integer.class, 1));
		configuration.addPropertyIfNotPresent(new Property(VERTICAL_GAP, "Vertical gap", Integer.class, 1));
		configuration.addPropertyIfNotPresent(new Property(SHOW_LABELS, "Show labels", Boolean.class, true));
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
	public boolean getShowLabels() {
		return getConfiguration().lookup(SHOW_LABELS).getBoolean();
	}

	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setShowLabels(final boolean show) {
		getConfiguration().lookup(SHOW_LABELS).setBoolean(show);
	}

	
	//--- statistics
	@Override
	public void recomputeStatistics() {
		// nothing to recompute
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
		
		final JMenu horizontalSizeMenu = new JMenu("Horizontal Gap");
		for (int i=0; i<7; i++) {
			horizontalSizeMenu.add(new SetIntPropertyAction(getConfiguration().lookup(HORIZONTAL_GAP), i, i+" pixels", "Set horizontal gap size to "+i+" pixels"));
		}
		popup.add(horizontalSizeMenu);
		
		popup.add(new ToggleBooleanPropertyAction(getConfiguration().lookup(SHOW_LABELS), "Show Labels", "Hide Labels", "Show or hide node labels"));
		
		popup.addSeparator();
	}

	
	//--- hit testing
	@Override
	public ContextTreeNode findNode(final int x, final int y) {
		if (getTop()==null) {
			return null;
		}
		final int height = (int)new HeightAttribute().evaluate(getTop());
		return findNode(getTop(), x, y, 0, getWidth(), height, 0);
	}
	
	private ContextTreeNode findNode(final ContextTreeNode node, final int mx, final int my, final int x, final int w, final int height, final int depth) {
		final int gap = getHorizontalGap();
		if (w<2*gap) {
			return null;
		}
		final LongAttribute sizeMetric = getView().getSizeAttribute();
		final long size = sizeMetric.evaluate(node);
		if (size==0) {
			return null;
		}

		long sum = 0;
		for (int c = 0; c<node.getChildCount(); c++) {
			final long childValue = sizeMetric.evaluate(node.getChild(c));
			final int childLeftX = (int)(x+(w*sum/size));
			final int childRightX = (int)(x+(w*(sum+childValue)/size));
			final int childWidth = childRightX-childLeftX;
			final ContextTreeNode hit = findNode(node.getChild(c), mx, my, childLeftX, childWidth, height, depth+1);
			if (hit!=null) {
				return hit;
			}
			sum += childValue;
		}
		
		final int yTop = getHeight()-1-(depth+1)*getHeight()/height;
		final int yBottom = getHeight()-1-(depth)*getHeight()/height;
		if (mx>=x && mx<x+w && my>=yTop && my<yBottom) {
			return node;
		} else {
			return null;
		}
	}

	
	//--- rendering
	@Override
	public void renderTree(final Graphics2D g2, final Surface surface) {
		final int height = (int)new HeightAttribute().evaluate(getTop());
		renderNode(g2, surface, getTop(), 0, surface.getWidth(), height, 0);
	}

	private void renderNode(final Graphics2D g2, final Surface surface, final ContextTreeNode node, final int x, final int w, final int height, final int depth) {
		final int gap = getHorizontalGap();
		if (w<2*gap || w<1) {
			return;
		}
		final LongAttribute sizeMetric = getView().getSizeAttribute();
		final long size = sizeMetric.evaluate(node);
		if (size==0) {
			return;
		}
		if (size < getView().getCutoff()*sizeMetric.evaluate(getRoot())/1000) {
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
		final int yTop = surface.getHeight()-1-(depth+1)*surface.getHeight()/height;
		final int yBottom = surface.getHeight()-1-(depth)*surface.getHeight()/height;
		final int h = yBottom-yTop-getVerticalGap();
		g2.fillRect(x+gap, yTop, w-gap, h);

		// name
		if (getShowLabels()) {
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
			final long childValue = sizeMetric.evaluate(node.getChild(c));
			final int childLeftX = (int)(x+(w*sum/size));
			final int childRightX = (int)(x+(w*(sum+childValue)/size));
			final int childWidth = childRightX-childLeftX;
			renderNode(g2, surface, node.getChild(c), childLeftX, childWidth, height, depth+1);
			sum += childValue;
		}
	}

}
