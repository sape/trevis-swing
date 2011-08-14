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

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A set of TreeViewProperties used to configure the visual appearance of a TreeView.
 * Multiple TreeViews can share a common TreeViewConfiguration,
 * which means that if a user adjusts the visualization of one TreeView,
 * all the others are updated accordingly, too.
 * 
 * TODO: Allow for partial sharing of a subset of the properties
 * (some kind of delegation from a private to a shared Configuration).
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class Configuration {

	private final HashMap<String, Property> properties;
	private final PropertyListener propertyListener;
	private final ArrayList<ConfigurationListener> listeners;
	
	
	public Configuration() {
		properties = new HashMap<String, Property>();
		listeners = new ArrayList<ConfigurationListener>();
		propertyListener = new PropertyListener() {
			public void propertyChanged(final Property property) {
				fireConfigurationChanged(property);
			}
		};
	}
	
	public void addPropertyIfNotPresent(final Property property) {
		if (!properties.containsKey(property.getKey())) {
			properties.put(property.getKey(), property);
			property.addPropertyListener(propertyListener);
			fireConfigurationChanged(property);
		}
	}
	
	public void addOrReplaceProperty(final Property property) {
		final Property existingProperty = properties.get(property.getKey());
		if (existingProperty!=null) {
			existingProperty.removePropertyListener(propertyListener);
		}
		properties.put(property.getKey(), property);
		property.addPropertyListener(propertyListener);
		fireConfigurationChanged(property);
	}
	
	public boolean containsProperty(final String key) {
		return properties.containsKey(key);
	}
	
	public Property lookup(final String key) {
		final Property p = properties.get(key);
		if (p==null) {
			throw new IllegalArgumentException("Configuration does not contain a property with key '"+key+"'");
		}
		return p;
	}
	
	
	//--- debug
	public void dump() {
		System.out.println("Configuration:");
		for (final String key : properties.keySet()) {
			final Property property = properties.get(key);
			property.dump();
		}
	}

	
	//--- listener management
	public void addConfigurationListener(final ConfigurationListener li) {
		listeners.add(li);
	}
	
	public void removeConfigurationListener(final ConfigurationListener li) {
		listeners.remove(li);
	}
	
	private void fireConfigurationChanged(final Property property) {
		for (final ConfigurationListener li : listeners) {
			li.treeViewConfigurationChanged(property);
		}
	}
	
}
