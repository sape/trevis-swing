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


/**
 * Set the visualization cutoff.
 * 
 * TODO: Refactor so we can use an IntPropertyAction for this.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class SetCutoffAction extends AbstractAction {

	private final TreeView view;
	private final int cutoff;
	
	
	public SetCutoffAction(final int cutoff, final TreeView view) {
		this.cutoff = cutoff;
		setEnabled(view.getCutoff()!=cutoff);
		putValue(NAME, cutoff==0?"Show everything":(""+cutoff/10.0+"%"));
		putValue(SHORT_DESCRIPTION, cutoff==0?"Do not cut off any nodes":("Cut off nodes smaller than "+cutoff/10.0+"% of the root's inclusive size"));
		this.view = view;
	}
	
	public void actionPerformed(final ActionEvent ev) {
		view.setCutoff(cutoff);
	}
	
}
