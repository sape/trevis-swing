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
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.AbstractAction;

import ch.usi.inf.sape.trevis.swing.TreeView;


/**
 * TODO
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class PrintAction extends AbstractAction {

	private final TreeView view;


	public PrintAction(final TreeView view) {
		setEnabled(true);
		putValue(NAME, "Print");
		putValue(SHORT_DESCRIPTION, "Print (experimental)");
		this.view = view;
	}

	public void actionPerformed(final ActionEvent ev) {
		final PrinterJob printerJob = PrinterJob.getPrinterJob();
		PageFormat pageFormat = view.getPageFormat();
		if (pageFormat==null) {
			pageFormat = printerJob.defaultPage();
		}
		printerJob.setPrintable(view, pageFormat);
		if (printerJob.printDialog()) {
			try {
				printerJob.print();
			} catch (final PrinterException ex) {
				System.out.println(ex);
			}
		}
	}

}
