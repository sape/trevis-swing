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
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import ch.usi.inf.sape.trevis.model.ContextTreeNode;
import ch.usi.inf.sape.util.Colors;


/**
 * A BreadcrumbBar is a Swing component that visualizes the path to the top and the currently selected node in a TreeView.
 * Ideally, a BreadcrumbBar is placed right above the TreeView.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class BreadcrumbBar extends JPanel {

	private static final Color COLOR = new Color(210, 210, 210);
	
	private final class Step extends JLabel {
		
		private final ContextTreeNode node;
		private boolean containsMouse;
		
		
		public Step(final ContextTreeNode node) {
			this.node = node;
			final String text = view.getLabelAttribute().evaluate(node);
			setText(text);
			final String tooltipText = view.getTooltipAttribute().evaluate(node);
			setToolTipText(tooltipText);
			setOpaque(true);
			setBackground(Color.WHITE);
			setForeground(new Color(Colors.hsbToRgb(view.getHsb(node, false))));
			
			// register listeners
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(final MouseEvent ev) {
					containsMouse = true;
					setBackground(new Color(Colors.hsbToRgb(view.getHsb(node, false))));
					setForeground(Color.WHITE);
				}
				@Override
				public void mouseExited(final MouseEvent ev) {
					containsMouse = false;
					setBackground(Color.WHITE);
					setForeground(new Color(Colors.hsbToRgb(view.getHsb(node, false))));
				}
				@Override
				public void mouseClicked(final MouseEvent ev) {
					if (ev.getClickCount()==2) {
						view.zoomTo(node);
					}
				}
			});
		}
		
	}
	
	private final TreeView view;
	
	
	public BreadcrumbBar(final TreeView view) {
		setOpaque(true);
		setBackground(Color.WHITE);
		setBorder(new MatteBorder(0, 0, 1, 0, COLOR));
		setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		this.view = view;
		
		// register listeners
		view.addTreeViewListener(new TreeViewListener() {
			public void treeChanged(TreeView view) {
				update();
			}
			public void topNodeChanged(TreeView view) {
				update();
			}
			public void rendererChanged(TreeView view) {
				// ignore
			}
			public void currentNodeChanged(TreeView view) {
				update();
			}
		});
		
		update();
	}
	
	private void update() {
		removeAll();
		final ContextTreeNode top = view.getTop();
		ContextTreeNode node = top;
		final ArrayList<JComponent> steps = new ArrayList<JComponent>();
		while (node!=null) {
			steps.add(new Step(node));
			node = node.getParent();
		}
		for (int i=steps.size()-1; i>=0; i--) {
			add(steps.get(i));
			if (i>0) {
				//final JLabel separator = new JLabel("\u00BB");
				final JLabel separator = new JLabel("\u203A");
				separator.setForeground(COLOR);
				add(separator);
			}
		}
		
		final ContextTreeNode current = view.getCurrent();
		if (current!=null) {
			final JLabel topSeparator = new JLabel(" \u2015 ");
			topSeparator.setForeground(COLOR);
			add(topSeparator);

			node = current;
			steps.clear();
			while (node!=top) {
				steps.add(new Step(node));
				node = node.getParent();
			}
			for (int i=steps.size()-1; i>=0; i--) {
				add(steps.get(i));
				if (i>0) {
					//final JLabel separator = new JLabel("\u00BB");
					final JLabel separator = new JLabel("\u203A");
					separator.setForeground(COLOR);
					add(separator);
				}
			}
		}
		
		invalidate();
		revalidate();
		repaint();
	}
	
}
