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
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import ch.usi.inf.sape.trevis.model.ContextTreeNode;
import ch.usi.inf.sape.trevis.model.attribute.LongAttribute;
import ch.usi.inf.sape.trevis.swing.action.SetIntPropertyAction;
import ch.usi.inf.sape.util.Colors;


/**
 * Corresponds to the TreeRingView as described in the SoftVis'10 Trevis paper.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class RadialRenderer extends TreeViewRenderer {

	public static final String CENTER_SIZE = "CENTER_SIZE";
	public static final String RING_WIDTH = "RING_WIDTH";

	
	public RadialRenderer() {
	}
	
	@Override
	public String getName() {
		return "Radial";
	}
	
	//--- configuration management
	@Override
	public void prepareConfiguration(final Configuration configuration) {
		configuration.addPropertyIfNotPresent(new Property(CENTER_SIZE, "Center size", Integer.class, 50));
		configuration.addPropertyIfNotPresent(new Property(RING_WIDTH, "Ring width", Integer.class, 5));
	}
		
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public int getCenterSize() {
		return getConfiguration().lookup(CENTER_SIZE).getInt();
	}
		
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setCenterSize(final int size) {
		getConfiguration().lookup(CENTER_SIZE).setInt(size);
	}
	
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public int getRingWidth() {
		return getConfiguration().lookup(RING_WIDTH).getInt();
	}
		
	/**
	 * Convenience method delegating to TreeViewConfiguration.
	 */
	public void setRingWidth(final int width) {
		getConfiguration().lookup(RING_WIDTH).setInt(width);
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
		final JMenu ringWidthMenu = new JMenu("Ring Width");
		for (int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 16, 20, 30, 40}) {
			ringWidthMenu.add(new SetIntPropertyAction(getConfiguration().lookup(RING_WIDTH), i, i+" pixels", "Set ring width to "+i+" pixels"));
		}
		popup.add(ringWidthMenu);
	}
	

	//--- hit testing
	@Override
	public ContextTreeNode findNode(final int mx, final int my) {
		final ContextTreeNode top = getTop();
		if (top==null) {
			return null;
		}
		final int x = mx-getWidth()/2;
		final int y = my-getHeight()/2;
		final double radius = Math.sqrt(x*x+y*y);
		final double angleRadians = Math.atan2(-y, x);
		double angleDegrees = angleRadians/Math.PI/2*360;
		angleDegrees = angleDegrees<0?angleDegrees+360:angleDegrees;
		final int centerSize = getCenterSize();
		final int ringWidth = getRingWidth();
		final int expectedLevel = (int)((radius-centerSize/2)/ringWidth)+1;
		
		if (expectedLevel<=0) {
			return top;
		}
		final LongAttribute angleMetric = getView().getSizeAttribute();
		long sum = 0;
		for (int c = 0; c<top.getChildCount(); c++) {
			final long cc = angleMetric.evaluate(top.getChild(c));
			final double sa = 360.0*sum/angleMetric.evaluate(top); //NORMALIZATION BY PARENT
			final double a = 360.0*cc/angleMetric.evaluate(top); //NORMALIZATION BY PARENT
			final ContextTreeNode node = findNode(expectedLevel, angleDegrees, top.getChild(c), 1, sa, a);
			if (node!=null) {
				return node;
			}
			sum += cc;
		}
		return null;
	}
	
	private ContextTreeNode findNode(final int expectedLevel, final double expectedAngle, final ContextTreeNode node, final int level, final double sa, final double a) {
		if (level>expectedLevel) {
			return null;
		}
		if (expectedAngle<sa || expectedAngle>sa+a) {
			return null;
		}
		if (expectedLevel==level) {
			return node;
		}
		final LongAttribute angleMetric = getView().getSizeAttribute();
		long sum = 0;
		for (int c = 0; c<node.getChildCount(); c++) {
			final long cc = angleMetric.evaluate(node.getChild(c));
			final ContextTreeNode child = findNode(expectedLevel, expectedAngle, node.getChild(c), level+1, sa+(a*sum/angleMetric.evaluate(node)), a*cc/angleMetric.evaluate(node)); //NORMALIZATION BY PARENT
			if (child!=null) {
				return child;
			}
			sum += cc;
		}
		return null;
	}

	
	//--- rendering
	@Override
	public void renderTree(final Graphics2D g2, final Surface surface) {
		final ContextTreeNode root = getRoot();
		final ContextTreeNode top = getTop();
		final ContextTreeNode current = getCurrent();
		final int centerSize = getCenterSize();
		final int ringWidth = getRingWidth();
		final LongAttribute angleMetric = getView().getSizeAttribute();
		
		if (top!=null) {
			final int cx = surface.getWidth()/2;
			final int cy = surface.getHeight()/2;
			
			long sum = 0;
			for (int c = 0; c<top.getChildCount(); c++) {
				final long cc = angleMetric.evaluate(top.getChild(c));
				final double sa = 360.0*sum/angleMetric.evaluate(top); //NORMALIZATION BY PARENT
				final double a = 360.0*cc/angleMetric.evaluate(top); //NORMALIZATION BY PARENT
				renderNode(top.getChild(c), 1, sa, a, g2, surface);
				sum += cc;
			}
			
			final int hsb = getHsb(top, current==top);				
			g2.setColor(new Color(Colors.hsbToRgb(hsb)));
			g2.fill(new Ellipse2D.Double(cx-centerSize/2, cy-centerSize/2, centerSize, centerSize));
			if (ringWidth>2) {
				g2.setColor(getView().getBackground());
				g2.draw(new Ellipse2D.Double(cx-centerSize/2, cy-centerSize/2, centerSize, centerSize));
			}
			
			if (root!=top) {
				g2.setColor(getView().getBackground());
				g2.fillOval(cx-centerSize/4, cy-centerSize/4, centerSize/2, centerSize/2);
			}
			
			g2.setColor(Color.BLACK);
			final String label = ""+angleMetric.evaluate(top); //UNNORMALIZED VALUE OF TOP
			final FontMetrics fm = g2.getFontMetrics();
			final float labelWidth = (float)fm.getStringBounds(label, g2).getWidth();
			final int labelOffset = fm.getHeight()/2-fm.getDescent();
			g2.drawString(label, cx-labelWidth/2, cy+labelOffset);
			
			if (getView().isMouseInside()) {
				g2.setColor(Color.RED);
				final int x = getView().getMouseX();
				final int y = getView().getMouseY();
				g2.drawLine(cx, cy, x, y);
				final int rx = x-cx;
				final int ry = y-cy;
				final double radius = Math.sqrt(rx*rx+ry*ry);
				final double angleRadians = Math.atan2(-ry, rx);
				double angleDegrees = angleRadians/Math.PI/2*360;
				angleDegrees = angleDegrees<0?angleDegrees+360:angleDegrees;
				g2.draw(new Arc2D.Double(cx-radius, cy-radius, 2*radius, 2*radius, angleDegrees-5, 10, Arc2D.OPEN));
			}
		}
	}

	private void renderNode(final ContextTreeNode node, final int level, final double sa, final double a, final Graphics2D g, final Surface surface) {
		final ContextTreeNode root = getRoot();
		final ContextTreeNode current = getCurrent();

		final int width = surface.getWidth();
		final int height = surface.getHeight();
		final int cx = width/2;
		final int cy = height/2;
		final int centerSize = getCenterSize();
		final int ringWidth = getRingWidth();
		final boolean focusSame = getView().getFocusSame();
		final LongAttribute angleMetric = getView().getSizeAttribute();
		
		if (angleMetric.evaluate(node) >= getView().getCutoff()*angleMetric.evaluate(root)/1000) {
			// cut-off at cutoff 1000ths of root's angle
			final int x = cx-centerSize/2-level*ringWidth;
			final int y = cy-centerSize/2-level*ringWidth;
			final int s = centerSize+level*2*ringWidth;
			long sum = 0;
			for (int c = 0; c<node.getChildCount(); c++) {
				final long cc = angleMetric.evaluate(node.getChild(c));
				renderNode(node.getChild(c), level+1, sa+(a*sum/angleMetric.evaluate(node)), a*cc/angleMetric.evaluate(node), g, surface); //NORMALIZATION BY PARENT
				sum += cc;
			}		
			final int hsb = getHsb(node, node==current || (focusSame && (current!=null && node.getLabel().equals(current.getLabel()))));			
			g.setColor(new Color(Colors.hsbToRgb(hsb)));
			g.fill(new Arc2D.Double(x, y, s, s, sa, a, Arc2D.PIE));
			if (ringWidth>2) {
				g.setColor(getView().getBackground());
				g.draw(new Arc2D.Double(x, y, s, s, sa, a, Arc2D.OPEN));
			}

			g.setColor(getView().getBackground());
			final double x1 = cx+(centerSize/2+level*ringWidth)*Math.cos(sa*Math.PI*2/360);
			final double y1 = cy-(centerSize/2+level*ringWidth)*Math.sin(sa*Math.PI*2/360);
			g.draw(new Line2D.Double(cx, cy, x1, y1));
		}
	}
	
}
