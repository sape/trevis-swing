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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import ch.usi.inf.sape.trevis.model.ContextTreeNode;
import ch.usi.inf.sape.trevis.model.attribute.LongAttribute;
import ch.usi.inf.sape.trevis.swing.action.SetIntPropertyAction;
import ch.usi.inf.sape.util.Colors;


/**
 * Renders the tree in the form of a basic tree map.
 * 
 * Based on Ben Shneiderman's treemap: http://www.cs.umd.edu/hcil/treemap-history/
 * 
 * Check out "Ordered Treemap Layout"
 * ftp://ftp.cs.umd.edu/pub/hcil/Reports-Abstracts-Bibliography/2001-06html/2001-06.htm
 * It compares five different tree map layout algorithms.
 * We use the traditional slice&dice, and thus often end up with bad aspect ratios
 * (but we have more stability, allowing for better visual comparison between trees).
 * 
 * Another relevant publication is this CHI 2010 paper:
 * "A Comparative Evaluation on Tree Visualization Methods for Hierarchical Structures with Large Fan-outs"
 * http://research.microsoft.com/en-us/um/redmond/groups/cue/publications/CHI2010-PittaTree.pdf
 * 
 * Note: JavaTreeProfiler uses a treemap to show a CCT. See http://jcoverage.sourceforge.net/
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class TreeMapRenderer extends TreeViewRenderer {


	public static final String GAP = "GAP";
	private int gapSize;

	
	public TreeMapRenderer() {
		gapSize = 3;
	}
	
	@Override
	public String getName() {
		return "Tree Map";
	}
	
	//--- configuration management
	@Override
	public void prepareConfiguration(final Configuration configuration) {
		configuration.addPropertyIfNotPresent(new Property(GAP, "Gap", Integer.class, 3));
	}
		
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public int getGap() {
		return getConfiguration().lookup(GAP).getInt();
	}
		
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setGap(final int size) {
		getConfiguration().lookup(GAP).setInt(size);
	}
	
	
	//--- statistics
	@Override
	public void recomputeStatistics() {
		// nothing to recompute
	}
	
	
	//--- menu management
	@Override
	public void addPopupMenuItems(final JPopupMenu popup) {
		final JMenu gapMenu = new JMenu("Gap");
		for (int i=0; i<7; i++) {
			gapMenu.add(new SetIntPropertyAction(getConfiguration().lookup(GAP), i, i+" pixels", "Set gap to "+i+" pixels"));
		}
		popup.add(gapMenu);
	}

	
	//--- hit testing
	@Override
	public ContextTreeNode findNode(final int mx, final int my) {
		if (getTop()==null) {
			return null;
		}
		return findNode(getTop(), mx, my, 0, 0, getWidth(), getHeight(), (getView().getPathLengthToRoot(getTop())%2)==0);
	}

	private ContextTreeNode findNode(final ContextTreeNode node, final int mx, final int my, final int x, final int y, final int w, final int h, final boolean horizontal) {
		final int gap = getGap();
		if (w<2*gap || h<2*gap) {
			return null;
		}
		final LongAttribute sizeMetric = getView().getSizeAttribute();
		final long size = sizeMetric.evaluate(node);
		if (size==0) {
			return null;
		}
		long sum = 0;
		for (int c = 0; c<node.getChildCount(); c++) {
			final long cc = sizeMetric.evaluate(node.getChild(c));
			if (horizontal) {
				final ContextTreeNode hit = findNode(node.getChild(c), mx, my, (int)(x+gap+((w-2*gap)*sum/size)), y+gap, (int)((w-2*gap)*cc/size), h-2*gap, false);
				if (hit!=null) {
					return hit;
				}
			} else {
				final ContextTreeNode hit = findNode(node.getChild(c), mx, my, x+gap, (int)(y+gap+((h-2*gap)*sum/size)), w-2*gap, (int)((h-2*gap)*cc/size), true);
				if (hit!=null) {
					return hit;
				}
			}
			sum += cc;
		}
		if (mx>=x && mx<x+w && my>=y && my<y+h) {
			return node;
		} else {
			return null;
		}
	}

	
	//--- rendering
	@Override
	public void renderTree(final Graphics2D g2, final Surface surface) {
		renderNode(g2, surface, getTop(), 0, 0, surface.getWidth(), surface.getHeight(), (getView().getPathLengthToRoot(getTop())%2)==0);
	}

	private void renderNode(final Graphics2D g2, final Surface surface, final ContextTreeNode node, final int x, final int y, final int w, final int h, final boolean horizontal) {
		 final int gap = getGap();
		 if (w<2*gap || h<2*gap || w<1 || h<1) {
			 return;
		 }
		 final LongAttribute sizeMetric = getView().getSizeAttribute();
		 final long size = sizeMetric.evaluate(node);
		 
		 if (size < getView().getCutoff()*sizeMetric.evaluate(getRoot())/1000) {
			 // cut-off at cutoff 1000ths of root's angle
			 return;
		 }

		 // background
		 final boolean focusSame = getView().getFocusSame();
		 final ContextTreeNode current = getCurrent();
		 final boolean focused = node==current || (focusSame && (current!=null && node!=null && node.getLabel()!=null && node.getLabel().equals(current.getLabel())));
		 final int hsb = getHsb(node, focused);
		 g2.setColor(new Color(Colors.hsbToRgb(hsb)));
		 g2.fillRect(x, y, w, h);

		 if (size==0) {
			 return;
		 }

		 long sum = 0;
		 for (int c = 0; c<node.getChildCount(); c++) {
			 sum += sizeMetric.evaluate(node.getChild(c));
		 }

		 // hatch for too-small children
		 final BufferedImage bi = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
		 final Graphics2D big = bi.createGraphics();
		 big.setColor(focused?Color.BLACK:new Color(Colors.hsbToRgb(getHsb(node, true))));
		 if (horizontal) {
			 big.fillRect(0, 0, 1, 2);
		 } else {
			 big.fillRect(0, 0, 2, 1);			
		 }
		 big.setColor(new Color(Colors.hsbToRgb(getHsb(node, focused))));
		 if (horizontal) {
			 big.fillRect(1, 0, 1, 2);
		 } else {
			 big.fillRect(0, 1, 2, 1);			
		 }
		 final Rectangle r = new Rectangle(0, 0, 2, 2);
		 g2.setPaint(new TexturePaint(bi, r));
		 if (horizontal) {
			 g2.fillRect(x+gap, y+gap, (int)((w-2*gap)*sum/size), h-2*gap);
			 if (gap>1) {
				 g2.setColor(Color.BLACK);
				 g2.drawRect(x+gap, y+gap, (int)((w-2*gap)*sum/size)-1, h-2*gap-1);
			 }
		 } else {
			 g2.fillRect(x+gap, y+gap, w-2*gap, (int)((h-2*gap)*sum/size));
			 if (gap>1) {
				 g2.setColor(Color.BLACK);
				 g2.drawRect(x+gap, y+gap, w-2*gap-1, (int)((h-2*gap)*sum/size)-1);
			 }
		 }

		 // border around children
		 if (gap>1) {
			 g2.setColor(Color.BLACK);
			 g2.drawRect(x, y, w-1, h-1);
		 }

		 // name
		 g2.setColor(Color.WHITE);
		 final int cx = x+w/2;
		 final int cy = y+h/2;
		 final String label = ""+getView().getLabelAttribute().getValue(node); 
		 final FontMetrics fm = g2.getFontMetrics();
		 final float labelWidth = (float)fm.getStringBounds(label, g2).getWidth();
		 final int labelOffset = fm.getHeight()/2-fm.getDescent();
		 final Shape clip = g2.getClip();
		 g2.setClip(x, y, w, h);
		 g2.drawString(label, cx-labelWidth/2, cy+labelOffset);
		 g2.setClip(clip);

		 // children
		 sum = 0;
		 for (int c = 0; c<node.getChildCount(); c++) {
			 final long cc = sizeMetric.evaluate(node.getChild(c));
			 if (horizontal) {
				 renderNode(g2, surface, node.getChild(c), (int)(x+gap+((w-2*gap)*sum/size)), y+gap, (int)((w-2*gap)*cc/size), h-2*gap, false);
			 } else {
				 renderNode(g2, surface, node.getChild(c), x+gap, (int)(y+gap+((h-2*gap)*sum/size)), w-2*gap, (int)((h-2*gap)*cc/size), true);
			 }
			 sum += cc;
		 }
	 }

}
