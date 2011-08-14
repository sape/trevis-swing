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

import ch.usi.inf.sape.trevis.swing.Property;
import ch.usi.inf.sape.trevis.swing.PropertyListener;


/**
 * Toggle the boolean value of the given Property.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class ToggleBooleanPropertyAction extends AbstractAction {

	private final Property property;
	
	
	public ToggleBooleanPropertyAction(final Property property, final String trueName, final String falseName, final String description) {
		this.property = property;
		setEnabled(true);
		putValue(NAME, property.getBoolean()?falseName:trueName);
		putValue(SHORT_DESCRIPTION, description);
		
		// register listeners
		property.addPropertyListener(new PropertyListener() {
			public void propertyChanged(final Property property) {
				putValue(NAME, property.getBoolean()?falseName:trueName);
			}
		});
	}
		
	public void actionPerformed(final ActionEvent ev) {
		property.setBoolean(!property.getBoolean());
	}

}
