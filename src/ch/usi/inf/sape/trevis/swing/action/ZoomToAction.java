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

import ch.usi.inf.sape.trevis.model.ContextTreeNode;
import ch.usi.inf.sape.trevis.model.attribute.NodeAttribute;
import ch.usi.inf.sape.trevis.swing.TreeView;


/**
 * Zoom to the given ContextTreeNode.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class ZoomToAction  extends AbstractAction {

	private final ContextTreeNode node;
	private final TreeView view;
	
	
	public ZoomToAction(final String label, final NodeAttribute nodeNameAttribute, final ContextTreeNode node, final TreeView view) {
		setEnabled(node!=null);
		putValue(NAME, label);
		if (node!=null) {
			putValue(SHORT_DESCRIPTION, "<html>"+label+"<br><b>"+nodeNameAttribute.getValue(node)+"</b>");
		}
		this.node = node;
		this.view = view;
	}
	
	public void actionPerformed(final ActionEvent ev) {
		view.zoomTo(node);
	}
	
}
