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

import java.awt.Graphics2D;

import javax.swing.JPopupMenu;

import ch.usi.inf.sape.trevis.model.ContextTree;
import ch.usi.inf.sape.trevis.model.ContextTreeNode;


/**
 * A TreeViewRender draws the specific visualization in a TreeView.
 * It also provides hit-testing (mapping back from pixels to ContextTreeNodes).
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public abstract class TreeViewRenderer {

	private TreeView view;
	
	
	public TreeViewRenderer() {
	}
	
	public final void setView(final TreeView view) {
		this.view = view;
	}

	public final TreeView getView() {
		return view;
	}

	public final ContextTree getTree() {
		return view.getTree();
	}

	public final ContextTreeNode getRoot() {
		return view.getRoot();
	}

	public final ContextTreeNode getTop() {
		return view.getTop();
	}

	public final ContextTreeNode getCurrent() {
		return view.getCurrent();
	}
	
	public final Configuration getConfiguration() {
		return view.getConfiguration();
	}
	
	public final int getWidth() {
		return view.getWidth();
	}
	
	public final int getHeight() {
		return view.getHeight();
	}
	
	protected final int getHsb(final ContextTreeNode node, final boolean focus) {
		return view.getHsb(node, focus);
	}
	
	public abstract String getName();
	public abstract void recomputeStatistics();
	public abstract void prepareConfiguration(final Configuration configuration);
	public abstract void addPopupMenuItems(final JPopupMenu popup);
	public abstract void renderTree(final Graphics2D g2, final Surface surface);
	public abstract ContextTreeNode findNode(int x, int y);

}
