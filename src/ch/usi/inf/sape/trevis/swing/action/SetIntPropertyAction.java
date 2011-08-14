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
 * Set the given Property to the given int value.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class SetIntPropertyAction extends AbstractAction {

	private final Property property;
	private final int value;
	
	
	public SetIntPropertyAction(final Property property, final int value, final String valueName, final String valueDescription) {
		this.property = property;
		this.value = value;
		setEnabled(property.getInt()!=value);
		putValue(NAME, valueName);
		putValue(SHORT_DESCRIPTION, valueDescription);
		
		// register listeners
		property.addPropertyListener(new PropertyListener() {
			public void propertyChanged(final Property property) {
				setEnabled(property.getInt()!=value);
			}
		});
	}
		
	public void actionPerformed(final ActionEvent ev) {
		property.setInt(value);
	}

}
