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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import ch.usi.inf.sape.trevis.swing.TreeView;
import ch.usi.inf.sape.trevis.swing.export.ImageExportDialog;


/**
 * Export the contents of the given TreeView as an image.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class ExportAsImageAction extends AbstractAction {

	private final ImageExportDialog dialog;
	private final TreeView view;
	
	
	public ExportAsImageAction(final TreeView view) {
		final Component root = SwingUtilities.getRoot(view);
		if (root instanceof Frame) {
			dialog = new ImageExportDialog((Frame)root, view);
		} else if (root instanceof Dialog) {
			dialog = new ImageExportDialog((Dialog)root, view); 
		} else {
			dialog = new ImageExportDialog((Frame)null, view);
		}
		setEnabled(true);
		putValue(NAME, "Export as image");
		putValue(SHORT_DESCRIPTION, "Export tree as image file");
		this.view = view;
	}
	
	public void actionPerformed(final ActionEvent ev) {
		dialog.setVisible(true);
	}
	
}
