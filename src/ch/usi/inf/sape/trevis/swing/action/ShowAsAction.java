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
package ch.usi.inf.sape.trevis.swing.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ch.usi.inf.sape.trevis.swing.TreeView;
import ch.usi.inf.sape.trevis.swing.TreeViewAdapter;
import ch.usi.inf.sape.trevis.swing.TreeViewRenderer;


/**
 * Tell the TreeView to visualize the tree using the given TreeViewRenderer.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class ShowAsAction extends AbstractAction {

	private final TreeViewRenderer renderer;
	private final TreeView view;
	
	
	public ShowAsAction(final TreeViewRenderer renderer, final TreeView view) {
		this.renderer = renderer;
		this.view = view;
		setEnabled(renderer!=view.getRenderer());
		putValue(NAME, renderer.getName());
		putValue(SHORT_DESCRIPTION, "Show tree as a "+renderer.getName());
		
		// register listeners
		view.addTreeViewListener(new TreeViewAdapter() {
			@Override
			public void rendererChanged(final TreeView view) {
				setEnabled(view.getRenderer()!=renderer);
			}
		});
	}
	
	public void actionPerformed(final ActionEvent ev) {
		view.setRenderer(renderer);
	}
	
}
